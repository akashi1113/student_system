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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyRecordServiceImpl implements StudyRecordService {
    private final StudyRecordDao studyRecordDao;
    private final CourseVideoDao courseVideoDao;
    private final CourseDao courseDao;


//    @Override//用这个
//    public StudyRecord getStudyRecordByUserIdAndVideoId(Long userId, Long videoId) {
//        StudyRecord record = studyRecordDao.getStudyRecordByUserIdAndVideoId(userId, videoId);
//
//        // 处理新记录情况
//        if (record == null) {
//            record = new StudyRecord();
//            record.setUserId(userId);
//            record.setVideoId(videoId);
//            record.setHistoricalProgress(0);
//            record.setCurrentProgress(0);
//            record.setCompleted(0);
//            record.setDuration(0);
//
//            CourseVideo video = courseVideoDao.getVideoById(videoId);
//            if (video != null) {
//                record.setVideoDuration(video.getDuration());
//            }
//        }
//        // 修复脏数据
//        else if (record.getVideoDuration() == 0) {
//            CourseVideo video = courseVideoDao.getVideoById(videoId);
//            if (video != null) {
//                record.setVideoDuration(video.getDuration());
//            }
//        }
//     saveStudyRecord   return record;
//    }

    @Override
    public StudyRecord getStudyRecordByUserIdAndVideoId(Long userId, Long videoId) {
        // 1. 从数据库查询记录
        StudyRecord record = studyRecordDao.getStudyRecordByUserIdAndVideoId(userId, videoId);

        // 2. 如果是第一次观看，创建一条新记录
        if (record == null) {
            CourseVideo video = courseVideoDao.getVideoById(videoId);
            if (video == null) {
                // 如果视频不存在，可以抛出异常或返回null
                log.error("尝试为不存在的视频创建学习记录, videoId: {}", videoId);
                return null;
            }

            record = new StudyRecord();
            record.setUserId(userId);
            record.setVideoId(videoId);
            record.setVideoDuration(video.getDuration()); // 从视频信息中获取总时长
            record.setLastPlaybackPosition(0);           // 上次播放位置从0开始
            record.setMaxProgress(0);                    // 最远进度从0开始
            record.setIsCompleted(false);                // 未完成
            record.setTotalWatchTime(0);                 // 累计观看时长为0

            // 注意：这里可以先不保存到数据库，等用户实际产生播放行为后再保存
            // 或者直接在这里插入一条初始记录也可以
            // studyRecordDao.insertStudyRecord(record);
        }

        // 3. 返回记录给前端，前端根据 lastPlaybackPosition 设置播放器起始时间
        return record;
    }

//    @Override//用这个
//    public boolean saveStudyRecord(StudyRecordDTO dto) {
//        CourseVideo video = courseVideoDao.getVideoById(dto.getVideoId());
//        if (video == null) return false;
//
//        int videoDuration = video.getDuration();
//        double threshold = videoDuration * 0.9;
//        LocalDateTime now = LocalDateTime.now();
//
//        StudyRecord existingRecord = studyRecordDao.getStudyRecordByUserIdAndVideoId(
//                dto.getUserId(), dto.getVideoId());
//
//        if (existingRecord == null) {
//            // 创建新记录
//            StudyRecord newRecord = new StudyRecord();
//            newRecord.setVideoDuration(videoDuration);
//            newRecord.setUserId(dto.getUserId());
//            newRecord.setVideoId(dto.getVideoId());
//            newRecord.setHistoricalProgress(dto.getCurrentProgress()); // 使用当前进度
//            newRecord.setCurrentProgress(dto.getCurrentProgress());
//            newRecord.setDuration(dto.getDuration());
//            newRecord.setCompleted(dto.getCurrentProgress() >= threshold ? 1 : 0);
//            return studyRecordDao.insertStudyRecord(newRecord) > 0;
//        } else {
//            // 更新记录
//            existingRecord.setCurrentProgress(dto.getCurrentProgress()); // 更新当前进度
//
//            // 更新历史进度（只增不减）
//            if (dto.getCurrentProgress() > existingRecord.getHistoricalProgress()) {
//                existingRecord.setHistoricalProgress(dto.getCurrentProgress());
//            }
//
//            // 更新完成状态
//            boolean isCompleted = existingRecord.getHistoricalProgress() >= threshold;
//            if (isCompleted) {
//                existingRecord.setCompleted(1);
//            }
//
//            existingRecord.setDuration(dto.getDuration());
//            existingRecord.setLastStudyTime(now);
//            existingRecord.setUpdateTime(now);
//
//            return studyRecordDao.updateStudyRecord(existingRecord) > 0;
//        }
//    }

    @Override
    public boolean saveStudyRecord(StudyRecordDTO dto) {
        // 1. 获取已有的学习记录，如果没有，说明是首次上报，需要先创建
        StudyRecord record = studyRecordDao.getStudyRecordByUserIdAndVideoId(dto.getUserId(), dto.getVideoId());

        CourseVideo video = courseVideoDao.getVideoById(dto.getVideoId());
        if (video == null) {
            log.warn("学习记录保存失败，视频不存在, videoId: {}", dto.getVideoId());
            return false;
        }

        // 如果记录不存在（可能是get接口没预创建，或者并发等情况），在这里创建一条
        if (record == null) {
            record = new StudyRecord();
            record.setUserId(dto.getUserId());
            record.setVideoId(dto.getVideoId());
            record.setVideoDuration(video.getDuration());
            record.setIsCompleted(false);
            record.setLastPlaybackPosition(0);
            record.setMaxProgress(0);
            record.setTotalWatchTime(0);
            // 对于新记录，我们走插入逻辑
            return createNewRecord(record, dto);
        }

        // 2. 如果视频已经标记为“已完成”，则不再更新进度和完成状态
        //    只更新“最后播放位置”和“累计观看时长”，以便了解用户是否在复习
        if (record.getIsCompleted()) {
            record.setLastPlaybackPosition(dto.getCurrentPlaybackPosition());
            record.setTotalWatchTime(record.getTotalWatchTime() + dto.getWatchDurationSinceLastSave());
            record.setUpdateTime(LocalDateTime.now());
            return studyRecordDao.updateStudyRecord(record) > 0;
        }

        // 3. 更新常规信息
        record.setLastPlaybackPosition(dto.getCurrentPlaybackPosition());
        record.setTotalWatchTime(record.getTotalWatchTime() + dto.getWatchDurationSinceLastSave());

        // 4. 更新最远进度（只增不减）
        if (dto.getCurrentPlaybackPosition() > record.getMaxProgress()) {
            record.setMaxProgress(dto.getCurrentPlaybackPosition());
        }

        // 5. 判断是否达到完成阈值
        //    通常我们不要求100%，比如95%，因为片尾可能很长。这里我用95%举例。
        double completionThreshold = record.getVideoDuration() * 0.95;
        if (!record.getIsCompleted() && record.getMaxProgress() >= completionThreshold) {
            record.setIsCompleted(true);
            // 标记完成后，可以将最远进度直接设置为视频总时长，让其显示为100%
            record.setMaxProgress(record.getVideoDuration());
        }

        record.setUpdateTime(LocalDateTime.now());

        // 6. 保存到数据库
        return studyRecordDao.updateStudyRecord(record) > 0;
    }

    // 辅助方法，用于处理首次创建记录的逻辑
    private boolean createNewRecord(StudyRecord newRecord, StudyRecordDTO dto) {
        // 更新从DTO来的初始信息
        newRecord.setLastPlaybackPosition(dto.getCurrentPlaybackPosition());
        newRecord.setTotalWatchTime(dto.getWatchDurationSinceLastSave());
        newRecord.setMaxProgress(dto.getCurrentPlaybackPosition());

        // 首次上报时也可能直接拖到最后完成了
        double completionThreshold = newRecord.getVideoDuration() * 0.95;
        if (newRecord.getMaxProgress() >= completionThreshold) {
            newRecord.setIsCompleted(true);
            newRecord.setMaxProgress(newRecord.getVideoDuration());
        }

        newRecord.setCreateTime(LocalDateTime.now());
        newRecord.setUpdateTime(LocalDateTime.now());

        return studyRecordDao.insertStudyRecord(newRecord) > 0;
    }


    //    @Override
//    public List<StudyRecordVO> getStudyRecordsByUserId(Long userId) {
//        List<StudyRecord> records = studyRecordDao.findAllByUserId(userId);
//        if (records == null || records.isEmpty()) {
//            return java.util.Collections.emptyList();
//        }
//        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
//        return records.stream().map(record -> {
//            StudyRecordVO vo = new StudyRecordVO();
//            vo.setUserId(record.getUserId());
//            vo.setVideoId(record.getVideoId());
//            vo.setProgress(record.getProgress());
//            vo.setDuration(record.getDuration());
//            vo.setLastStudyTime(record.getLastStudyTime() != null ? record.getLastStudyTime().format(formatter) : null);
//            // 查视频
//            CourseVideo video = courseVideoDao.getVideoById(record.getVideoId());
//            if (video != null) {
//                vo.setVideoTitle(video.getTitle());
//                vo.setCourseId(video.getCourseId());
//                // 查课程
//                Course course = courseDao.findById(video.getCourseId());
//                if (course != null) {
//                    vo.setCourseTitle(course.getTitle());
//                }
//            }
//            return vo;
//        }).collect(java.util.stream.Collectors.toList());
//    }
//}
    @Override
    public List<StudyRecordVO> getStudyRecordsByUserId(Long userId) {
        // 1. 一次性获取用户所有的学习记录
        List<StudyRecord> records = studyRecordDao.findAllByUserId(userId);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 从学习记录中，提取所有需要关联查询的 ID
        List<Long> videoIds = records.stream()
                .map(StudyRecord::getVideoId)
                .distinct()
                .collect(Collectors.toList());

        // 3. 一次性批量查询所有相关的视频信息
        // (需要在 CourseVideoDao 中添加 List<CourseVideo> findByIds(List<Long> ids) 方法)
        List<CourseVideo> videos = courseVideoDao.findByIds(videoIds);
        Map<Long, CourseVideo> videoMap = videos.stream()
                .collect(Collectors.toMap(CourseVideo::getId, v -> v));

        // 4. 从视频信息中，提取所有需要关联查询的课程 ID
        List<Long> courseIds = videos.stream()
                .map(CourseVideo::getCourseId)
                .distinct()
                .collect(Collectors.toList());

        // 5. 一次性批量查询所有相关的课程信息
        // (需要在 CourseDao 中添加 List<Course> findByIds(List<Long> ids) 方法)
        List<Course> courses = courseDao.findByIds(courseIds);
        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        // 准备时间格式化工具
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        // 6. 在内存中组装数据，不再进行循环查询
        return records.stream().map(record -> {
            // 使用我们新的VO
            StudyRecordVO vo = new StudyRecordVO();
            vo.setUserId(record.getUserId());
            vo.setVideoId(record.getVideoId());

            // --- 关键：使用新的字段进行映射 ---
            // 注意：这里的 record 对象应该是你已经修改过的、包含新字段的 StudyRecord 实体类
            vo.setLastPlaybackPosition(record.getLastPlaybackPosition());
            vo.setMaxProgress(record.getMaxProgress());
            vo.setIsCompleted(record.getIsCompleted()); // 假设数据库中 1=true, 0=false
            vo.setVideoDuration(record.getVideoDuration());
            vo.setTotalWatchTime(record.getTotalWatchTime());

            // 使用 record 的 updateTime 作为最后学习时间
            vo.setLastStudyTime(record.getUpdateTime() != null ?
                    record.getUpdateTime().format(formatter) : null);

            // 从Map中高效获取关联信息
            CourseVideo video = videoMap.get(record.getVideoId());
            if (video != null) {
                vo.setVideoTitle(video.getTitle());
                vo.setCourseId(video.getCourseId());

                Course course = courseMap.get(video.getCourseId());
                if (course != null) {
                    vo.setCourseTitle(course.getTitle());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    // StudyRecordServiceImpl.java

    @Override
    public StudyRecordVO getStudyRecordVOByUserIdAndVideoId(Long userId, Long videoId) {
        // 1. 调用我们已有的方法，获取StudyRecord实体。这部分逻辑会处理首次观看的情况。
        StudyRecord record = this.getStudyRecordByUserIdAndVideoId(userId, videoId);

        // 如果视频不存在或记录创建失败，返回null
        if (record == null) {
            return null;
        }

        // 2. 将实体（Entity）转换为视图对象（VO）
        StudyRecordVO vo = new StudyRecordVO();
        vo.setUserId(record.getUserId());
        vo.setVideoId(record.getVideoId());

        // 使用新字段映射
        vo.setLastPlaybackPosition(record.getLastPlaybackPosition());
        vo.setMaxProgress(record.getMaxProgress());
        vo.setIsCompleted(record.getIsCompleted());
        vo.setVideoDuration(record.getVideoDuration());
        vo.setTotalWatchTime(record.getTotalWatchTime());

        // 格式化最后学习时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        vo.setLastStudyTime(record.getUpdateTime() != null ?
                record.getUpdateTime().format(formatter) : null);

        // 3. 补充视频和课程的标题等信息（这里需要查询数据库）
        CourseVideo video = courseVideoDao.getVideoById(videoId);
        if (video != null) {
            vo.setVideoTitle(video.getTitle());
            vo.setCourseId(video.getCourseId());

            Course course = courseDao.findById(video.getCourseId());
            if (course != null) {
                vo.setCourseTitle(course.getTitle());
            }
        }

        return vo;
    }

}
