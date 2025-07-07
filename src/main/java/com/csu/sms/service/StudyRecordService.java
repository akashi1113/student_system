package com.csu.sms.service;

import com.csu.sms.dto.StudyRecordDTO;
import com.csu.sms.model.course.StudyRecord;
import com.csu.sms.vo.StudyRecordVO;
import jakarta.validation.Valid;
import java.util.List;

public interface StudyRecordService {
    public boolean saveStudyRecord(@Valid StudyRecordDTO studyRecordDTO);
    public StudyRecord getStudyRecordByUserIdAndVideoId(Long userId, Long videoId);
    public List<StudyRecordVO> getStudyRecordsByUserId(Long userId);
}
