package com.csu.sms.persistence;

import com.csu.sms.model.course.StudyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudyRecordDao {
    StudyRecord getStudyRecordByUserIdAndVideoId(@Param("userId") Long userId, @Param("videoId") Long videoId);

    int insertStudyRecord(StudyRecord record);

    int updateStudyRecord(StudyRecord record);

    List<StudyRecord> findByUserIdAndVideoIds(@Param("userId") Long userId,
                                              @Param("videoIds") List<Long> videoIds);

    int countCompletedVideosByUserIdAndVideoIds(@Param("userId") Long userId,
                                                @Param("videoIds") List<Long> videoIds);

    List<StudyRecord> findAllByUserId(@Param("userId") Long userId);
}
