package com.csu.sms.service;

import com.csu.sms.dto.StudyRecordDTO;
import com.csu.sms.model.course.StudyRecord;
import com.csu.sms.vo.StudyRecordVO;
import jakarta.validation.Valid;
import java.util.List;

public interface StudyRecordService {
    boolean saveStudyRecord(@Valid StudyRecordDTO studyRecordDTO);
    StudyRecord getStudyRecordByUserIdAndVideoId(Long userId, Long videoId);
    List<StudyRecordVO> getStudyRecordsByUserId(Long userId);
    // StudyRecordService.java (接口)
    StudyRecordVO getStudyRecordVOByUserIdAndVideoId(Long userId, Long videoId);


}
