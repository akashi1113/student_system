package com.csu.sms.service.impl;

import com.csu.sms.model.course.Course;
import com.csu.sms.model.course.StudyRecord;
import com.csu.sms.vo.CourseVO;
import com.csu.sms.common.PageResult;
import com.csu.sms.common.ServiceException; // 引入自定义异常
import com.csu.sms.model.course.CourseVideo;
import com.csu.sms.persistence.CourseDao;
import com.csu.sms.persistence.CourseVideoDao;
import com.csu.sms.persistence.StudyRecordDao;
import com.csu.sms.service.CourseService;
import com.csu.sms.service.FileStorageService; // 引入文件存储服务
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value; // 引入 Value 注解
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 引入事务注解
import org.springframework.web.multipart.MultipartFile; // 引入 MultipartFile

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {
    private final CourseDao courseDao;
    private final CourseVideoDao courseVideoDao;
    private final StudyRecordDao studyRecordDao;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FileStorageService fileStorageService; // 注入文件存储服务

    @Value("${app.upload.course-cover-folder}")
    private String courseCoverFolder; // 课程封面文件夹
    @Value("${app.upload.default-course-cover-url}")
    private String defaultCourseCoverUrl; // 默认课程封面 URL
    @Value("${app.upload.video-folder}") // 用于删除视频文件时获取文件夹路径
    private String videoFolder;

    private static final String COURSE_LIST_CACHE_KEY = "course:list:user_%d:page_%d:size_%d";
    private static final long CACHE_TTL_HOURS = 24;

    @Override
    public PageResult<CourseVO> listCourses(Long userId, Integer pageNum, Integer pageSize) {
        //尝试从缓存获取
        //直接使用 RedisTemplate 操作 Redis，手动控制缓存的读取和写入。
        //复杂键生成逻辑，精确控制缓存过期时间，存储复杂对象
        String cacheKey = String.format(COURSE_LIST_CACHE_KEY, userId, pageNum, pageSize);
        @SuppressWarnings("unchecked")
        PageResult<CourseVO> cachedResult = (PageResult<CourseVO>) redisTemplate.opsForValue().get(cacheKey);

        if (cachedResult != null) {
            log.debug("Course list cache hit for user: {}, page: {}, size: {}", userId, pageNum, pageSize);
            return cachedResult;
        }

        // 缓存未命中，查询数据库
        log.debug("Course list cache miss for user: {}, page: {}, size: {}", userId, pageNum, pageSize);

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询总记录数
        int total = courseDao.countCourses();

        // 如果没有记录，返回空结果
        if (total == 0) {
            return PageResult.of(new ArrayList<>(), 0, pageNum, pageSize);
        }

        // 1. 获取分页课程数据
        List<Course> courses = courseDao.findCoursesByPage(offset, pageSize);
        if (courses.isEmpty()) {
            return PageResult.of(new ArrayList<>(), total, pageNum, pageSize);
        }

        // 2. 获取所有课程ID
        List<Long> courseIds = courses.stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        // 3. 获取所有课程的视频
        List<CourseVideo> allVideos = courseVideoDao.findVideosByCourseIds(courseIds);

        // 4. 统计每个课程的视频
        Map<Long, List<CourseVideo>> courseVideoMap = allVideos.stream()
                .collect(Collectors.groupingBy(CourseVideo::getCourseId));

        // 5. 获取视频ID列表
        List<Long> videoIds = allVideos.stream()
                .map(CourseVideo::getId)
                .collect(Collectors.toList());

        // 6. 获取用户学习记录
        List<StudyRecord> studyRecords = videoIds.isEmpty() ?
                new ArrayList<>() :
                studyRecordDao.findByUserIdAndVideoIds(userId, videoIds);

        // 7. 按视频ID分组学习记录
        Map<Long, List<StudyRecord>> videoStudyMap = studyRecords.stream()
                .collect(Collectors.groupingBy(StudyRecord::getVideoId));

        // 8. 转换为VO
        List<CourseVO> voList = courses.stream().map(course -> {
            CourseVO vo = new CourseVO();
            BeanUtils.copyProperties(course, vo);

            // 设置教师名称
            vo.setTeacherName(course.getTeacherName());

            // 设置视频数量
            List<CourseVideo> videos = courseVideoMap.getOrDefault(course.getId(), new ArrayList<>());
            vo.setVideoCount(videos.size());

            // 计算学习进度
            if (!videos.isEmpty()) {
                int totalVideos = videos.size();
                int completedVideos = 0;

                for (CourseVideo video : videos) {
                    List<StudyRecord> records = videoStudyMap.getOrDefault(video.getId(), new ArrayList<>());
                    if (!records.isEmpty() && records.get(0).getCompleted() == 1) {
                        completedVideos++;
                    }
                }

                vo.setStudyProgress(totalVideos > 0 ? (int) ((float) completedVideos / totalVideos * 100) : 0);
            } else {
                vo.setStudyProgress(0);
            }

            return vo;
        }).collect(Collectors.toList());

        // 创建分页结果
        PageResult<CourseVO> result = PageResult.of(voList, total, pageNum, pageSize);

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL_HOURS, TimeUnit.HOURS);

        return result;
    }

    @Cacheable(value = "courseDetail", key = "'id_' + #id + ':user_' + #userId")
    @Override
    public CourseVO getCourseDetail(Long id, Long userId) {
        // 1. 获取课程基本信息
        Course course = courseDao.findById(id);
        if (course == null) {
            return null;
        }

        // 2. 转换为VO
        CourseVO vo = new CourseVO();
        //将 course 对象中的属性值复制到 vo 对象中
        BeanUtils.copyProperties(course, vo);
        vo.setTeacherName(course.getTeacherName());

        // 3. 获取课程视频
        List<CourseVideo> videos = courseVideoDao.findVideosByCourseId(id);
        vo.setVideoCount(videos.size());

        // 4. 计算学习进度
        if (!videos.isEmpty()) {
            List<Long> videoIds = videos.stream()
                    .map(CourseVideo::getId)
                    .collect(Collectors.toList());

            // 获取已完成的视频数量
            int completedCount = studyRecordDao.countCompletedVideosByUserIdAndVideoIds(userId, videoIds);

            vo.setStudyProgress(videos.size() > 0 ? (int) ((float) completedCount / videos.size() * 100) : 0);
        } else {
            vo.setStudyProgress(0);
        }

        return vo;
    }


    @Transactional // 确保数据库操作和文件操作的原子性（虽然文件操作无法回滚，但能保证数据库一致性）
    @Override
    public Long createCourse(Course course, MultipartFile coverImageFile) {
        // 处理封面图片上传
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            try {
                String coverImgUrl = fileStorageService.uploadFile(coverImageFile, courseCoverFolder);
                course.setCoverImg(coverImgUrl); // 设置封面图URL
            } catch (IOException e) {
                log.error("Failed to upload course cover image for new course: {}", e.getMessage());
                // 抛出自定义异常，让Controller捕获并返回给前端
                throw new ServiceException(500, "课程封面上传失败: " + e.getMessage());
            }
        } else {
            // 没有上传封面，使用默认封面 URL
            course.setCoverImg(defaultCourseCoverUrl);
        }

        // 设置其他默认属性
        course.setStatus(0); // 默认上架
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());

        int rows = courseDao.insertCourse(course);
        if (rows <= 0) {
            // 数据库插入失败，抛出异常，触发事务回滚（如果上面文件上传成功，文件会留在OSS上）
            // 更好的做法是：在ServiceException中包含错误码，前端根据错误码判断是否提示用户重试。
            throw new ServiceException(500, "创建课程失败，请稍后重试。");
        }

        // 清除列表缓存
        clearCoursesListCache();
        log.info("Course added and list cache cleared: {}", course.getId());
        return course.getId();
    }

    @Transactional
    @Override
    public boolean updateCourse(Course course, MultipartFile coverImageFile) {
        Course existingCourse = courseDao.findById(course.getId());
        if (existingCourse == null) {
            throw new ServiceException(404, "要更新的课程不存在。");
        }

        // 2. 逐个判断并更新字段：只更新前端传入的非null字段
        if (course.getTitle() != null) {
            existingCourse.setTitle(course.getTitle());
        }
        if (course.getDescription() != null) {
            existingCourse.setDescription(course.getDescription());
        }
        if (course.getTeacherName() != null) {
            existingCourse.setTeacherName(course.getTeacherName());
        }
        if (course.getStatus() != null) {
            existingCourse.setStatus(course.getStatus());
        }

        String newCoverImgUrl = existingCourse.getCoverImg(); // 默认保留旧URL
        boolean oldCoverDeleted = false; // 标记旧封面是否已删除

        // 3. 处理封面图片上传或清除逻辑
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            // 情况A: 前端上传了新的封面图片
            try {
                newCoverImgUrl = fileStorageService.uploadFile(coverImageFile, courseCoverFolder);
                // 删除旧封面图片 (如果存在且不是默认封面)
                if (existingCourse.getCoverImg() != null && !existingCourse.getCoverImg().equals(defaultCourseCoverUrl)) {
                    oldCoverDeleted = fileStorageService.deleteFile(existingCourse.getCoverImg());
                    if (!oldCoverDeleted) {
                        log.warn("Failed to delete old course cover image: {}", existingCourse.getCoverImg());
                        // 不中断操作，只是记录警告
                    }
                }
            } catch (IOException e) {
                log.error("Failed to upload new course cover image for course {}: {}", course.getId(), e.getMessage(), e);
                throw new ServiceException(500, "课程封面上传失败: " + e.getMessage());
            }
        } else {
            // 情况B: 前端没有上传文件。
            // 需要判断前端是否明确指示要清除现有封面。
            // 约定：如果前端在Course对象中将coverImg字段设为""（空字符串），表示明确清除。
            // 这要求Controller层在接收到 clearCoverImage=true 时，将course.setCoverImg("")。
            if (course.getCoverImg() != null && course.getCoverImg().isEmpty()) {
                // 如果 existingCourse 有旧封面且不是默认封面，则删除
                if (existingCourse.getCoverImg() != null && !existingCourse.getCoverImg().equals(defaultCourseCoverUrl)) {
                    oldCoverDeleted = fileStorageService.deleteFile(existingCourse.getCoverImg());
                    if (!oldCoverDeleted) {
                        log.warn("Failed to delete old course cover image when clearing: {}", existingCourse.getCoverImg());
                    }
                }
                newCoverImgUrl = defaultCourseCoverUrl; // 清除后，将URL指定为默认封面URL
            }
            // 情况C: coverImageFile 为 null，且 course.getCoverImg() 也为 null
            // 这表示前端没有上传新文件，也没有明确指示清除，所以保持现有封面不变。
            // 此时 newCoverImgUrl 仍是 existingCourse.getCoverImg() 的值，无需额外处理。
        }
        // 4. 设置更新后的封面URL到 existingCourse
        existingCourse.setCoverImg(newCoverImgUrl);

        // 5. 设置更新时间
        existingCourse.setUpdateTime(LocalDateTime.now());

        // 6. 执行更新操作：将合并了新旧数据的 existingCourse 传给 DAO
        int result = courseDao.updateCourse(existingCourse);
        if (result <= 0) {
            throw new ServiceException(500, "更新课程信息失败，请稍后重试。");
        }

        clearCourseDetailCache(course.getId());
        clearCoursesListCache();
        log.info("Course updated and cache cleared: {}", course.getId());
        return true;
    }

    @Transactional
    @Override
    public boolean deleteCourse(Long id) {
        Course course = courseDao.findById(id);
        if (course == null) {
            throw new ServiceException(404, "要删除的课程不存在。");
        }

        // 1. 删除课程下的所有视频文件和数据库记录
        List<CourseVideo> videos = courseVideoDao.findVideosByCourseId(id);
        for (CourseVideo video : videos) {
            if (video.getUrl() != null && !video.getUrl().isEmpty()) {
                // 删除OSS文件
                boolean deleted = fileStorageService.deleteFile(video.getUrl());
                if (!deleted) {
                    log.warn("Failed to delete video file from OSS during course deletion: {}", video.getUrl());
                    // 警告，但不中断操作，因为主要目标是删除数据库记录
                }
            }
        }
        // 删除数据库中的视频记录
        int videoDeleteResult = courseVideoDao.deleteVideosByCourseId(id);
        log.info("Deleted {} video records for course {}", videoDeleteResult, id);


        // 2. 删除课程封面图 (如果存在且不是默认封面)
        if (course.getCoverImg() != null && !course.getCoverImg().isEmpty() && !course.getCoverImg().equals(defaultCourseCoverUrl)) {
            boolean deleted = fileStorageService.deleteFile(course.getCoverImg());
            if (!deleted) {
                log.warn("Failed to delete course cover image from OSS: {}", course.getCoverImg());
            }
        }

        // 3. 删除课程记录
        int courseDeleteResult = courseDao.deleteCourse(id);
        if (courseDeleteResult <= 0) {
            // 如果课程本身删除失败，则回滚前面的视频记录删除操作 (但文件无法回滚)
            throw new ServiceException(500, "删除课程失败，请稍后重试。");
        }

        // 4. 清除相关缓存
        clearCoursesListCache();
        clearCourseDetailCache(id); // 针对所有用户清除该课程的详情缓存

        log.info("Course deleted and cache cleared: {}", id);
        return true;
    }


    //清除所有课程列表缓存
    //使用 pattern 匹配批量删除相关缓存
    //按模式清除多个缓存，数据变更影响面广
    public void clearCoursesListCache() {
        Set<String> keys = redisTemplate.keys("course:list:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Cleared {} course list cache keys", keys.size());
        }
    }

    // 清除特定课程的所有详情缓存（针对所有用户）
    public void clearCourseDetailCache(Long courseId) {
        Set<String> keys = redisTemplate.keys("course:detail:id_" + courseId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Cleared {} course detail cache keys for course {}", keys.size(), courseId);
        }
    }

    // 清除特定用户对特定课程的详情缓存（更精确的版本）
    @CacheEvict(value = "courseDetail", key = "'id_' + #courseId + ':user_' + #userId")
    public void clearUserCourseDetailCache(Long courseId, Long userId) {
        // 注解会自动处理缓存清除，方法体为空
        log.debug("Cleared course detail cache for course {} and user {}", courseId, userId);
    }

    // 在学习记录更新时清除相关缓存
    public void onStudyRecordUpdated(Long userId, Long videoId) {
        // 首先获取视频所属的课程ID
        CourseVideo video = courseVideoDao.getVideoById(videoId);
        if (video != null) {
            Long courseId = video.getCourseId();

            // 清除该用户的课程详情缓存
            clearUserCourseDetailCache(courseId, userId);

            // 清除该用户的课程列表缓存
            Set<String> keys = redisTemplate.keys("course:list:user_" + userId + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Cleared course list cache for user {} after study record update", userId);
            }
        }
    }
}





