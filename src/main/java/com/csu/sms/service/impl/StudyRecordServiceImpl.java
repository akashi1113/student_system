package com.csu.sms.service.impl;

import com.csu.sms.dto.StudyRecordDTO;
import com.csu.sms.model.course.CourseVideo;
import com.csu.sms.model.course.StudyRecord;
import com.csu.sms.model.course.Course;
import com.csu.sms.persistence.CourseDao;
import com.csu.sms.persistence.CourseVideoDao;
import com.csu.sms.persistence.StudyRecordDao;
import com.csu.sms.service.StudyRecordService;
import com.csu.sms.vo.StudyRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyRecordServiceImpl implements StudyRecordService {
    private final StudyRecordDao studyRecordDao;
    private final CourseVideoDao courseVideoDao;
    private final CourseDao courseDao;

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

    @Override
    public List<StudyRecordVO> getStudyRecordsByUserId(Long userId) {
        List<StudyRecord> records = studyRecordDao.findAllByUserId(userId);
        if (records == null || records.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return records.stream().map(record -> {
            StudyRecordVO vo = new StudyRecordVO();
            vo.setUserId(record.getUserId());
            vo.setVideoId(record.getVideoId());
            vo.setProgress(record.getProgress());
            vo.setDuration(record.getDuration());
            vo.setLastStudyTime(record.getLastStudyTime() != null ? record.getLastStudyTime().format(formatter) : null);
            // 查视频
            CourseVideo video = courseVideoDao.getVideoById(record.getVideoId());
            if (video != null) {
                vo.setVideoTitle(video.getTitle());
                vo.setCourseId(video.getCourseId());
                // 查课程
                Course course = courseDao.findById(video.getCourseId());
                if (course != null) {
                    vo.setCourseTitle(course.getTitle());
                }
            }
            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }
}