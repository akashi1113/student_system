package com.csu.sms.service.impl;

import com.csu.sms.dto.StudyRecordDTO;
import com.csu.sms.model.course.CourseVideo;
import com.csu.sms.model.course.StudyRecord;
import com.csu.sms.persistence.CourseVideoDao;
import com.csu.sms.persistence.StudyRecordDao;
import com.csu.sms.service.StudyRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyRecordServiceImpl implements StudyRecordService {
    private final StudyRecordDao studyRecordDao;
    private final CourseVideoDao courseVideoDao;

    @Override
    public StudyRecord getStudyRecordByUserIdAndVideoId(Long userId, Long videoId) {
        return studyRecordDao.getStudyRecordByUserIdAndVideoId(userId, videoId);
    }

    @Override
    public boolean saveStudyRecord(StudyRecordDTO dto) {
        // 查询视频信息
        CourseVideo video = courseVideoDao.getVideoById(dto.getVideoId());
        if (video == null) {
            log.warn("视频不存在, videoId: {}", dto.getVideoId());
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        // 查询是否已有学习记录
        StudyRecord existingRecord = studyRecordDao.getStudyRecordByUserIdAndVideoId(
                dto.getUserId(), dto.getVideoId());

        if (existingRecord == null) {
            // 创建新记录
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

            int rows = studyRecordDao.insertStudyRecord(newRecord);
            return rows > 0;
        } else {
            // 更新记录
            existingRecord.setProgress(dto.getProgress());
            existingRecord.setDuration(existingRecord.getDuration() + dto.getDuration());
            existingRecord.setLastStudyTime(now);
            existingRecord.setUpdateTime(now);

            // 判断是否完成学习
            boolean isCompleted = dto.getProgress() >= video.getDuration();
            if (isCompleted) {
                existingRecord.setCompleted(1);
            }

            int rows = studyRecordDao.updateStudyRecord(existingRecord);
            return rows > 0;
        }
    }
}