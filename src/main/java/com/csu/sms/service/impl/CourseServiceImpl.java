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
        return PageResult.of(voList, total, pageNum, pageSize);
    }

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
                course.setCoverImg(coverImgUrl);
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

        log.info("Course added: {}", course.getId());
        return course.getId();
    }

    @Transactional
    @Override
    public boolean updateCourse(Course course, MultipartFile coverImageFile) {
        Course existingCourse = courseDao.findById(course.getId());
        if (existingCourse == null) {
            throw new ServiceException(404, "要更新的课程不存在。");
        }

        // 2. 逐个判断并更新字段
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

        String newCoverImgUrl = existingCourse.getCoverImg();
        boolean oldCoverDeleted = false;

        // 3. 处理封面图片上传或清除逻辑
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            try {
                newCoverImgUrl = fileStorageService.uploadFile(coverImageFile, courseCoverFolder);
                if (existingCourse.getCoverImg() != null && !existingCourse.getCoverImg().equals(defaultCourseCoverUrl)) {
                    oldCoverDeleted = fileStorageService.deleteFile(existingCourse.getCoverImg());
                    if (!oldCoverDeleted) {
                        log.warn("Failed to delete old course cover image: {}", existingCourse.getCoverImg());
                    }
                }
            } catch (IOException e) {
                log.error("Failed to upload new course cover image for course {}: {}", course.getId(), e.getMessage(), e);
                throw new ServiceException(500, "课程封面上传失败: " + e.getMessage());
            }
        } else {
            if (course.getCoverImg() != null && course.getCoverImg().isEmpty()) {
                if (existingCourse.getCoverImg() != null && !existingCourse.getCoverImg().equals(defaultCourseCoverUrl)) {
                    oldCoverDeleted = fileStorageService.deleteFile(existingCourse.getCoverImg());
                    if (!oldCoverDeleted) {
                        log.warn("Failed to delete old course cover image when clearing: {}", existingCourse.getCoverImg());
                    }
                }
                newCoverImgUrl = defaultCourseCoverUrl;
            }
        }

        existingCourse.setCoverImg(newCoverImgUrl);
        existingCourse.setUpdateTime(LocalDateTime.now());

        int result = courseDao.updateCourse(existingCourse);
        if (result <= 0) {
            throw new ServiceException(500, "更新课程信息失败，请稍后重试。");
        }

        log.info("Course updated: {}", course.getId());
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
                boolean deleted = fileStorageService.deleteFile(video.getUrl());
                if (!deleted) {
                    log.warn("Failed to delete video file from OSS during course deletion: {}", video.getUrl());
                }
            }
        }
        int videoDeleteResult = courseVideoDao.deleteVideosByCourseId(id);
        log.info("Deleted {} video records for course {}", videoDeleteResult, id);

        // 2. 删除课程封面图
        if (course.getCoverImg() != null && !course.getCoverImg().isEmpty() && !course.getCoverImg().equals(defaultCourseCoverUrl)) {
            boolean deleted = fileStorageService.deleteFile(course.getCoverImg());
            if (!deleted) {
                log.warn("Failed to delete course cover image from OSS: {}", course.getCoverImg());
            }
        }

        // 3. 删除课程记录
        int courseDeleteResult = courseDao.deleteCourse(id);
        if (courseDeleteResult <= 0) {
            throw new ServiceException(500, "删除课程失败，请稍后重试。");
        }

        log.info("Course deleted: {}", id);
        return true;
    }
}