package com.csu.sms.service;

import com.csu.sms.dto.StudyRecordDTO;
import com.csu.sms.model.course.StudyRecord;
import jakarta.validation.Valid;

public interface StudyRecordService {
    public boolean saveStudyRecord(@Valid StudyRecordDTO studyRecordDTO);
    public StudyRecord getStudyRecordByUserIdAndVideoId(Long userId, Long videoId);
}
