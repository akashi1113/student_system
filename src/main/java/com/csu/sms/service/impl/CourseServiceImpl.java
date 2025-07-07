package com.csu.sms.service.impl;

import com.csu.sms.model.course.Course;
import com.csu.sms.model.course.StudyRecord;
import com.csu.sms.vo.CourseVO;
import com.csu.sms.common.PageResult;
import com.csu.sms.common.ServiceException;
import com.csu.sms.model.course.CourseVideo;
import com.csu.sms.persistence.CourseDao;
import com.csu.sms.persistence.CourseVideoDao;
import com.csu.sms.persistence.StudyRecordDao;
import com.csu.sms.service.CourseService;
import com.csu.sms.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {
    private final CourseDao courseDao;
    private final CourseVideoDao courseVideoDao;
    private final StudyRecordDao studyRecordDao;
    private final FileStorageService fileStorageService;

    @Value("${app.upload.course-cover-folder}")
    private String courseCoverFolder;
    @Value("${app.upload.default-course-cover-url}")
    private String defaultCourseCoverUrl;
    @Value("${app.upload.video-folder}")
    private String videoFolder;

    @Override
    public PageResult<CourseVO> listCourses(Long userId, Integer pageNum, Integer pageSize) {
        // 移除尝试从缓存获取的逻辑
        log.debug("Fetching course list from database for user: {}, page: {}, size: {}", userId, pageNum, pageSize);

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
                    if (!records.isEmpty() && records.get(0).getIsCompleted() == true) {
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

        // 移除缓存结果的逻辑

        return result;
    }

    // 移除 @Cacheable 注解
    @Override
    public CourseVO getCourseDetail(Long id, Long userId) {
        // 1. 获取课程基本信息
        Course course = courseDao.findById(id);
        if (course == null) {
            return null;
        }

        // 2. 转换为VO
        CourseVO vo = new CourseVO();
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


    @Transactional
    @Override
    public Long createCourse(Course course, MultipartFile coverImageFile) {
        // 处理封面图片上传
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            try {
                String coverImgUrl = fileStorageService.uploadFile(coverImageFile, courseCoverFolder);
                course.setCoverImg(coverImgUrl); // 设置封面图URL
            } catch (IOException e) {
                log.error("Failed to upload course cover image for new course: {}", e.getMessage());
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
            throw new ServiceException(500, "创建课程失败，请稍后重试。");
        }

        // 移除清除列表缓存的逻辑
        log.info("Course added: {}", course.getId()); // 修改日志信息
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

        // 移除清除缓存的逻辑
        log.info("Course updated: {}", course.getId()); // 修改日志信息
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
                // 删除文件
                boolean deleted = fileStorageService.deleteFile(video.getUrl());
                if (!deleted) {
                    log.warn("Failed to delete video file during course deletion: {}", video.getUrl());
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
                log.warn("Failed to delete course cover image: {}", course.getCoverImg());
            }
        }

        // 3. 删除课程记录
        int courseDeleteResult = courseDao.deleteCourse(id);
        if (courseDeleteResult <= 0) {
            throw new ServiceException(500, "删除课程失败，请稍后重试。");
        }

        // 移除清除相关缓存的逻辑
        log.info("Course deleted: {}", id); // 修改日志信息
        return true;
    }

    @Override
    public PageResult<CourseVO> listCoursesForAdmin(Long userId,Integer pageNum, Integer pageSize){
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询总记录数
        int total = courseDao.countCoursesForAdmin();

        // 如果没有记录，返回空结果
        if (total == 0) {
            return PageResult.of(new ArrayList<>(), 0, pageNum, pageSize);
        }

        // 1. 获取分页课程数据
        List<Course> courses = courseDao.findCoursesByPageForAdmin(offset, pageSize);
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
        List<StudyRecord> studyRecords = new ArrayList<>();

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
                    if (!records.isEmpty() && records.get(0).getIsCompleted() == true) {
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

        // 移除缓存结果的逻辑

        return result;
    }

    @Override
    public double calculateCourseProgress(Long courseId, Long userId) {
        // 1. 获取该课程下的所有视频列表
        List<CourseVideo> videosInCourse = courseVideoDao.findVideosByCourseId(courseId);
        if (videosInCourse == null || videosInCourse.isEmpty()) {
            return 0.0; // 如果课程没有视频，进度为0
        }
        int totalVideos = videosInCourse.size();

        // 2. 获取用户对这些视频的所有学习记录
        List<Long> videoIds = videosInCourse.stream().map(CourseVideo::getId).collect(Collectors.toList());
        List<StudyRecord> studyRecords = studyRecordDao.findByUserIdAndVideoIds(userId, videoIds);

        // 为了方便查找，将List转为Map
        Map<Long, StudyRecord> recordMap = studyRecords.stream()
                .collect(Collectors.toMap(StudyRecord::getVideoId, record -> record));

        double totalProgressPercentage = 0.0;

        // 3. 遍历课程下的每一个视频，计算其贡献的进度
        for (CourseVideo video : videosInCourse) {
            StudyRecord record = recordMap.get(video.getId());

            if (record != null) {
                // 如果记录存在
                if (record.getIsCompleted()) {
                    // 如果已完成，该视频进度贡献为100%
                    totalProgressPercentage += 1.0;
                } else {
                    // 如果未完成，按最远进度计算
                    if (record.getVideoDuration() > 0) {
                        totalProgressPercentage += (double) record.getMaxProgress() / record.getVideoDuration();
                    }
                }
            }
            // 如果 record 为 null，说明用户还没看过这个视频，进度贡献为0，所以不用加。
        }

        // 4. 计算最终的课程总进度（百分比）
        if (totalVideos > 0) {
            return (totalProgressPercentage / totalVideos) * 100;
        }

        return 0.0;
    }

}
