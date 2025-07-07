package com.csu.sms.service.impl;

import com.csu.sms.common.PageResult;
import com.csu.sms.common.ServiceException;
import com.csu.sms.model.course.Course;
import com.csu.sms.model.course.CourseVideo;
import com.csu.sms.model.course.StudyRecord;
import com.csu.sms.persistence.CourseDao;
import com.csu.sms.persistence.CourseVideoDao;
import com.csu.sms.persistence.StudyRecordDao;
import com.csu.sms.service.FileStorageService;
import com.csu.sms.service.VideoService;
import com.csu.sms.vo.VideoVO;
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
public class VideoServiceImpl implements VideoService {
    private final CourseVideoDao videoDao;
    private final CourseDao courseDao;
    private final StudyRecordDao studyRecordDao;
    private final FileStorageService fileStorageService;

    @Value("${app.upload.video-folder}")
    private String videoFolder;
    @Value("${app.upload.default-video-url}")
    private String defaultVideoUrl;

    @Override
    public VideoVO getVideoDetail(Long id, Long userId) {
        CourseVideo video = videoDao.getVideoById(id);
        if (video == null) {
            throw new ServiceException(404, "视频不存在");
        }

        Course course = courseDao.findById(video.getCourseId());
        if (course == null) {
            log.warn("Video {} exists but its course {} does not exist", id, video.getCourseId());
            throw new ServiceException(500, "视频所属课程不存在，数据异常");
        }

        StudyRecord studyRecord = studyRecordDao.getStudyRecordByUserIdAndVideoId(userId, id);

        VideoVO vo = new VideoVO();
        BeanUtils.copyProperties(video, vo);
        vo.setCourseName(course.getTitle());

        if (studyRecord != null) {
            vo.setProgress(studyRecord.getProgress());
            vo.setCompleted(studyRecord.getIsCompleted() == true);
        } else {
            vo.setProgress(0);
            vo.setCompleted(false);
        }
        return vo;
    }

    @Override
    public List<VideoVO> getVideosByCourseId(Long courseId, Long userId) {
        Course course = courseDao.findById(courseId);
        if (course == null) {
            throw new ServiceException(404, "课程不存在");
        }

        List<CourseVideo> videos = videoDao.findVideosByCourseId(courseId);
        if (videos.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> videoIds = videos.stream().map(CourseVideo::getId).collect(Collectors.toList());
        List<StudyRecord> studyRecords = studyRecordDao.findByUserIdAndVideoIds(userId, videoIds);

        Map<Long, StudyRecord> videoStudyMap = studyRecords.stream()
                .collect(Collectors.toMap(StudyRecord::getVideoId, record -> record, (r1, r2) -> r1));

        return videos.stream().map(video -> {
            VideoVO vo = new VideoVO();
            BeanUtils.copyProperties(video, vo);
            vo.setCourseName(course.getTitle());

            StudyRecord record = videoStudyMap.get(video.getId());
            if (record != null) {
                vo.setProgress(record.getProgress());
                vo.setCompleted(record.getIsCompleted());
            } else {
                vo.setProgress(0);
                vo.setCompleted(false);
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public Long addVideo(CourseVideo video, MultipartFile videoFile) {
        if (videoFile == null || videoFile.isEmpty()) {
            throw new ServiceException(400, "视频文件不能为空。");
        }

        Course course = courseDao.findById(video.getCourseId());
        if (course == null) {
            throw new ServiceException(404, "所属课程不存在，无法添加视频。");
        }

        try {
            String videoUrl = fileStorageService.uploadFile(videoFile, videoFolder);
            video.setUrl(videoUrl);
        } catch (IOException e) {
            log.error("Failed to upload video file for course {}: {}", video.getCourseId(), e.getMessage(), e);
            throw new ServiceException(500, "视频文件上传失败: " + e.getMessage());
        }

        video.setCreateTime(LocalDateTime.now());
        video.setUpdateTime(LocalDateTime.now());

        int result = videoDao.insertVideo(video);
        if (result <= 0) {
            fileStorageService.deleteFile(video.getUrl());
            throw new ServiceException(500, "添加视频到数据库失败，请稍后重试。");
        }

        log.info("Video added: {}", video.getId());
        return video.getId();
    }

    @Transactional
    @Override
    public boolean updateVideo(CourseVideo video, MultipartFile videoFile) {
        CourseVideo oldVideo = videoDao.getVideoById(video.getId());
        if (oldVideo == null) {
            throw new ServiceException(404, "要更新的视频不存在。");
        }

        String newVideoUrl = oldVideo.getUrl();

        if (videoFile != null && !videoFile.isEmpty()) {
            try {
                newVideoUrl = fileStorageService.uploadFile(videoFile, videoFolder);
                if (oldVideo.getUrl() != null && !oldVideo.getUrl().isEmpty()) {
                    fileStorageService.deleteFile(oldVideo.getUrl());
                }
            } catch (IOException e) {
                log.error("Failed to upload new video file for video {}: {}", video.getId(), e.getMessage(), e);
                throw new ServiceException(500, "视频文件上传失败: " + e.getMessage());
            }
        } else if (video.getUrl() != null && video.getUrl().isEmpty()) {
            if (oldVideo.getUrl() != null && !oldVideo.getUrl().isEmpty()) {
                fileStorageService.deleteFile(oldVideo.getUrl());
            }
            newVideoUrl = null;
        }
        video.setUrl(newVideoUrl);

        if (video.getCourseId() != null && !video.getCourseId().equals(oldVideo.getCourseId())) {
            Course newCourse = courseDao.findById(video.getCourseId());
            if (newCourse == null) {
                throw new ServiceException(404, "指定的新所属课程不存在。");
            }
        }

        video.setUpdateTime(LocalDateTime.now());
        int result = videoDao.updateVideo(video);
        if (result <= 0) {
            throw new ServiceException(500, "更新视频信息失败，请稍后重试。");
        }

        log.info("Video updated: {}", video.getId());
        return true;
    }

    @Transactional
    @Override
    public boolean deleteVideo(Long id) {
        CourseVideo video = videoDao.getVideoById(id);
        if (video == null) {
            throw new ServiceException(404, "要删除的视频不存在。");
        }

        if (video.getUrl() != null && !video.getUrl().isEmpty()) {
            fileStorageService.deleteFile(video.getUrl());
        }

        int result = videoDao.deleteVideo(id);
        if (result <= 0) {
            throw new ServiceException(500, "删除视频失败，请稍后重试。");
        }

        log.info("Video deleted: {}", id);
        return true;
    }

    @Override
    public PageResult<VideoVO> listVideos(Integer pageNum, Integer pageSize){
        int offset = (pageNum - 1) * pageSize;
        int total = videoDao.countAll();
        // 如果没有记录，返回空结果
        if (total == 0) {
            return PageResult.of(new ArrayList<>(), 0, pageNum, pageSize);
        }

        // 1. 获取分页课程数据
        List<CourseVideo> videos = videoDao.findVideosByPage(offset, pageSize);
        if (videos.isEmpty()) {
            return PageResult.of(new ArrayList<>(), total, pageNum, pageSize);
        }

        // 2. 收集当前页所有视频涉及的 courseId，并去重
        List<Long> distinctCourseIds = videos.stream()
                .map(CourseVideo::getCourseId)
                .distinct() // 去重
                .collect(Collectors.toList());

        // 3. 批量查询这些 courseId 对应的 Course 信息，并构建 Map 方便查找
        Map<Long, String> courseNameMap;
        if (distinctCourseIds.isEmpty()) { // 避免空列表传入 findByIds
            courseNameMap = Map.of(); // 返回一个空的不可变Map
        } else {
            List<Course> courses = courseDao.findByIds(distinctCourseIds);
            courseNameMap = courses.stream()
                    .collect(Collectors.toMap(Course::getId, Course::getTitle));
        }


        // 4. 转换为 VideoVO 列表并填充 courseName
        List<VideoVO> voList = videos.stream().map(video -> {
            VideoVO vo = new VideoVO();
            BeanUtils.copyProperties(video, vo);

            // 根据 courseId 从 Map 中获取 courseName
            String courseName = courseNameMap.getOrDefault(video.getCourseId(), "未知课程");
            vo.setCourseName(courseName);

            // 管理员列表通常不需要 progress 和 completed，保持默认或设置null
             vo.setProgress(null);
             vo.setCompleted(null);

            return vo;
        }).collect(Collectors.toList());

        // 5. 创建分页结果
        return PageResult.of(voList, total, pageNum, pageSize);
    }
}
