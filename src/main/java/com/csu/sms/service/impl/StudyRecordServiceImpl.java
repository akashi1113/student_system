package com.csu.sms.service.impl;

import com.csu.sms.dto.StudyRecordDTO;
import com.csu.sms.model.course.CourseVideo;
import com.csu.sms.model.course.StudyRecord;
import com.csu.sms.persistence.CourseVideoDao;
import com.csu.sms.persistence.StudyRecordDao;
import com.csu.sms.service.StudyRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyRecordServiceImpl implements StudyRecordService {
    private final StudyRecordDao studyRecordDao;
    private final CourseVideoDao courseVideoDao;
    private final CourseServiceImpl courseService;
    private final VideoServiceImpl videoService;

    // 查询方法
    @Cacheable(value = "studyRecords", key = "'user_' + #userId + '_video_' + #videoId")
    @Override
    public StudyRecord getStudyRecordByUserIdAndVideoId(Long userId, Long videoId) {
        return studyRecordDao.getStudyRecordByUserIdAndVideoId(userId, videoId);
    }

    @CachePut(value = "studyRecords", key = "'user_' + #dto.userId + '_video_' + #dto.videoId")
    @Override
    public boolean saveStudyRecord(StudyRecordDTO dto) {
        // 查询视频信息
        CourseVideo video = courseVideoDao.getVideoById(dto.getVideoId());
        if (video == null) {
            log.warn("视频不存在, videoId: {}", dto.getVideoId());
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isNewRecord = false;
        boolean isCompletionStatusChanged = false;

        // 查询是否已有学习记录
        StudyRecord existingRecord = studyRecordDao.getStudyRecordByUserIdAndVideoId(
                dto.getUserId(), dto.getVideoId());

        if (existingRecord == null) {
            // 创建新记录
            isNewRecord = true;
            StudyRecord newRecord = new StudyRecord();
            newRecord.setUserId(dto.getUserId());
            newRecord.setVideoId(dto.getVideoId());
            newRecord.setProgress(dto.getProgress());
            newRecord.setDuration(dto.getDuration());
            newRecord.setLastStudyTime(now);
            newRecord.setCreateTime(now);
            newRecord.setUpdateTime(now);

            // 判断是否完成学习
            boolean isCompleted = dto.getProgress() >= video.getDuration();
            newRecord.setCompleted(isCompleted ? 1 : 0);
            isCompletionStatusChanged = isCompleted;

            int rows = studyRecordDao.insertStudyRecord(newRecord);

            // 如果成功创建记录，通知课程服务更新缓存
            if (rows > 0 && (isNewRecord || isCompletionStatusChanged)) {
                courseService.onStudyRecordUpdated(dto.getUserId(), dto.getVideoId());
            }
            // 通知视频服务更新缓存 (新增)
            videoService.onStudyRecordUpdated(existingRecord);

            return rows > 0;
        } else {
            // 记录原来的完成状态
            boolean wasCompleted = existingRecord.getCompleted() == 1;

            // 更新记录
            existingRecord.setProgress(dto.getProgress());
            existingRecord.setDuration(existingRecord.getDuration() + dto.getDuration());
            existingRecord.setLastStudyTime(now);
            existingRecord.setUpdateTime(now);

            // 判断是否完成学习
            boolean isCompleted = dto.getProgress() >= video.getDuration() ;
            if (isCompleted) {
                existingRecord.setCompleted(1);
            }

            // 检查完成状态是否改变
            isCompletionStatusChanged = (wasCompleted != isCompleted);

            int rows = studyRecordDao.updateStudyRecord(existingRecord);

            // 如果成功更新记录且完成状态改变，通知课程服务更新缓存
            if (rows > 0 && isCompletionStatusChanged) {
                courseService.onStudyRecordUpdated(dto.getUserId(), dto.getVideoId());
            }

            // 通知视频服务更新缓存 (新增)
            videoService.onStudyRecordUpdated(existingRecord);

            return rows > 0;
        }
    }
}
