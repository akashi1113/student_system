package com.csu.sms.service.impl;

import com.csu.sms.common.ServiceException; // 引入自定义异常
import com.csu.sms.model.course.Course;
import com.csu.sms.model.course.CourseVideo;
import com.csu.sms.model.course.StudyRecord;
import com.csu.sms.persistence.CourseDao;
import com.csu.sms.persistence.CourseVideoDao;
import com.csu.sms.persistence.StudyRecordDao;
import com.csu.sms.service.FileStorageService; // 引入文件存储服务
import com.csu.sms.service.VideoService;
import com.csu.sms.vo.VideoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value; // 引入 Value
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 引入事务注解
import org.springframework.web.multipart.MultipartFile; // 引入 MultipartFile

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoServiceImpl implements VideoService {
    private final CourseVideoDao videoDao;
    private final CourseDao courseDao;
    private final StudyRecordDao studyRecordDao;
    private final CourseServiceImpl courseService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FileStorageService fileStorageService; // 注入文件存储服务

    @Value("${app.upload.video-folder}")
    private String videoFolder; // 视频文件夹
    @Value("${app.upload.default-video-url}") // 可以设置一个默认视频，比如一个占位符或提示
    private String defaultVideoUrl;

    private static final String COURSE_VIDEOS_CACHE_KEY = "video:list:course_%d:user_%d";
    private static final long CACHE_TTL_HOURS = 24;

    @Cacheable(value = "videoDetail", key = "'id_' + #id + ':user_' + #userId")
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
            vo.setCompleted(studyRecord.getCompleted() == 1);
        } else {
            vo.setProgress(0);
            vo.setCompleted(false);
        }
        return vo;
    }

    @Override
    public List<VideoVO> getVideosByCourseId(Long courseId, Long userId) {
        String cacheKey = String.format(COURSE_VIDEOS_CACHE_KEY, courseId, userId);
        @SuppressWarnings("unchecked")
        List<VideoVO> cachedList = (List<VideoVO>) redisTemplate.opsForValue().get(cacheKey);

        if (cachedList != null) {
            log.debug("Course videos cache hit for course: {}, user: {}", courseId, userId);
            return cachedList;
        }

        log.debug("Course videos cache miss for course: {}, user: {}", courseId, userId);

        Course course = courseDao.findById(courseId);
        if (course == null) {
            throw new  ServiceException(404, "课程不存在");
        }

        List<CourseVideo> videos = videoDao.findVideosByCourseId(courseId);
        if (videos.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> videoIds = videos.stream().map(CourseVideo::getId).collect(Collectors.toList());
        List<StudyRecord> studyRecords = studyRecordDao.findByUserIdAndVideoIds(userId, videoIds);

        var videoStudyMap = studyRecords.stream()
                .collect(Collectors.toMap(StudyRecord::getVideoId, record -> record, (r1, r2) -> r1));

        List<VideoVO> result = videos.stream().map(video -> {
            VideoVO vo = new VideoVO();
            BeanUtils.copyProperties(video, vo);
            vo.setCourseName(course.getTitle());

            StudyRecord record = videoStudyMap.get(video.getId());
            if (record != null) {
                vo.setProgress(record.getProgress());
                vo.setCompleted(record.getCompleted() == 1);
            } else {
                vo.setProgress(0);
                vo.setCompleted(false);
            }
            return vo;
        }).collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL_HOURS, TimeUnit.HOURS);
        return result;
    }


    @Transactional
    @Override
    public Long addVideo(CourseVideo video, MultipartFile videoFile) {
        if (videoFile == null || videoFile.isEmpty()) {
            throw new ServiceException(400, "视频文件不能为空。");
        }

        // 检查课程是否存在
        Course course = courseDao.findById(video.getCourseId());
        if (course == null) {
            throw new ServiceException(404, "所属课程不存在，无法添加视频。");
        }

        // 上传视频文件到OSS
        try {
            String videoUrl = fileStorageService.uploadFile(videoFile, videoFolder);
            video.setUrl(videoUrl); // 设置视频URL
        } catch (IOException e) {
            log.error("Failed to upload video file for course {}: {}", video.getCourseId(), e.getMessage(), e);
            throw new ServiceException(500, "视频文件上传失败: " + e.getMessage());
        }

        // 设置默认属性
        video.setCreateTime(LocalDateTime.now());
        video.setUpdateTime(LocalDateTime.now());

        int result = videoDao.insertVideo(video);
        if (result <= 0) {
            // 如果数据库插入失败，删除已上传的视频文件
            fileStorageService.deleteFile(video.getUrl());
            throw new ServiceException(500, "添加视频到数据库失败，请稍后重试。");
        }

        // 清除缓存
        clearCourseVideosCache(video.getCourseId());
        // 通知课程服务更新课程缓存（视频数量和学习进度可能变化）
        courseService.clearCourseDetailCache(video.getCourseId());

        log.info("Video added and cache cleared: {}", video.getId());
        return video.getId();
    }


    @Transactional
    @Override
    public boolean updateVideo(CourseVideo video, MultipartFile videoFile) {
        CourseVideo oldVideo = videoDao.getVideoById(video.getId());
        if (oldVideo == null) {
            throw new ServiceException(404, "要更新的视频不存在。");
        }

        String newVideoUrl = oldVideo.getUrl(); // 默认保留旧URL

        // 处理视频文件上传
        if (videoFile != null && !videoFile.isEmpty()) {
            try {
                newVideoUrl = fileStorageService.uploadFile(videoFile, videoFolder);
                // 删除旧视频文件
                if (oldVideo.getUrl() != null && !oldVideo.getUrl().isEmpty()) {
                    boolean deleted = fileStorageService.deleteFile(oldVideo.getUrl());
                    if (!deleted) {
                        log.warn("Failed to delete old video file: {}", oldVideo.getUrl());
                    }
                }
            } catch (IOException e) {
                log.error("Failed to upload new video file for video {}: {}", video.getId(), e.getMessage(), e);
                throw new ServiceException(500, "视频文件上传失败: " + e.getMessage());
            }
        } else {
            // 如果前端明确传递了空文件，或者DTO中的URL被设为空字符串，则表示清除视频
            if (video.getUrl() != null && video.getUrl().isEmpty()) {
                if (oldVideo.getUrl() != null && !oldVideo.getUrl().isEmpty()) {
                    boolean deleted = fileStorageService.deleteFile(oldVideo.getUrl());
                    if (!deleted) {
                        log.warn("Failed to delete old video file when clearing: {}", oldVideo.getUrl());
                    }
                }
                newVideoUrl = null; // 清除后设置为null，或设置为默认视频URL
            }
        }
        video.setUrl(newVideoUrl); // 设置更新后的URL

        // 如果课程ID发生变化，需要检查新课程是否存在
        if (video.getCourseId() != null && !video.getCourseId().equals(oldVideo.getCourseId())) {
            Course newCourse = courseDao.findById(video.getCourseId());
            if (newCourse == null) {
                throw new ServiceException(404, "指定的新所属课程不存在。");
            }
        }

        // 更新视频信息
        video.setUpdateTime(LocalDateTime.now());
        int result = videoDao.updateVideo(video);
        if (result <= 0) {
            throw new ServiceException(500, "更新视频信息失败，请稍后重试。");
        }

        // 清除缓存
        clearVideoDetailCache(video.getId());
        clearCourseVideosCache(video.getCourseId());

        // 如果课程ID发生变化，还需要清除原课程的视频列表缓存
        if (!oldVideo.getCourseId().equals(video.getCourseId())) {
            clearCourseVideosCache(oldVideo.getCourseId());
            // 通知课程服务更新两个课程的缓存
            courseService.clearCourseDetailCache(oldVideo.getCourseId());
        }
        courseService.clearCourseDetailCache(video.getCourseId()); // 更新当前课程的缓存

        log.info("Video updated and cache cleared: {}", video.getId());
        return true;
    }


    @Transactional
    @Override
    public boolean deleteVideo(Long id) {
        CourseVideo video = videoDao.getVideoById(id);
        if (video == null) {
            throw new ServiceException(404, "要删除的视频不存在。");
        }

        // 1. 删除OSS中的视频文件
        if (video.getUrl() != null && !video.getUrl().isEmpty()) {
            boolean deleted = fileStorageService.deleteFile(video.getUrl());
            if (!deleted) {
                log.warn("Failed to delete video file from OSS: {}", video.getUrl());
                // 警告，但不中断操作，核心是删除数据库记录
            }
        }

        // 2. 删除数据库记录
        int result = videoDao.deleteVideo(id);
        if (result <= 0) {
            throw new ServiceException(500, "删除视频失败，请稍后重试。");
        }

        // 3. 清除相关缓存
        clearVideoDetailCache(id);
        clearCourseVideosCache(video.getCourseId());
        // 通知课程服务更新课程缓存（视频数量和学习进度可能变化）
        courseService.clearCourseDetailCache(video.getCourseId());

        log.info("Video deleted and cache cleared: {}", id);
        return true;
    }

    // 清除视频详情缓存
    private void clearVideoDetailCache(Long videoId) {
        Set<String> keys = redisTemplate.keys("video:detail:id_" + videoId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Cleared {} video detail cache keys for video {}", keys.size(), videoId);
        }
    }

    // 清除课程视频列表缓存
    private void clearCourseVideosCache(Long courseId) {
        Set<String> keys = redisTemplate.keys("video:list:course_" + courseId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Cleared {} course videos cache keys for course {}", keys.size(), courseId);
        }
    }

    // 用于被StudyRecordService调用，当学习记录更新时
    @CacheEvict(value = "videoDetail", key = "'id_' + #videoId + ':user_' + #userId")
    public void clearVideoDetailCacheForUser(Long videoId, Long userId) {
        log.debug("Cleared video detail cache for video {} and user {}", videoId, userId);
    }

    // 当用户学习记录更新时被调用
    public void onStudyRecordUpdated(StudyRecord record) {
        clearVideoDetailCacheForUser(record.getVideoId(), record.getUserId());
        CourseVideo video = videoDao.getVideoById(record.getVideoId());
        if (video != null) {
            String cacheKey = String.format(COURSE_VIDEOS_CACHE_KEY, video.getCourseId(), record.getUserId());
            redisTemplate.delete(cacheKey);
            log.debug("Cleared course videos cache for course {} and user {}",
                    video.getCourseId(), record.getUserId());
        }
    }
}
