package com.csu.sms.controller;

import com.csu.sms.annotation.LogOperation;
import com.csu.sms.common.ApiControllerResponse;
import com.csu.sms.dto.StudyRecordDTO;
import com.csu.sms.model.course.StudyRecord;
import com.csu.sms.service.StudyRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/study-records")
@RequiredArgsConstructor
@Slf4j
public class StudyRecordController {
    private final StudyRecordService studyRecordService;

    @PostMapping
    @LogOperation(module = "学习分析", operation = "保存学习记录", description = "保存用户学习记录")
    public ApiControllerResponse<Boolean> saveStudyRecord(
            @RequestBody @Valid StudyRecordDTO studyRecordDTO
    ) {
        boolean success = studyRecordService.saveStudyRecord(studyRecordDTO);
        if (success) {
            return ApiControllerResponse.success(true);
        } else {
            return ApiControllerResponse.error(500, "学习记录创建失败");
        }
    }

    @GetMapping("/user/{userId}/video/{videoId}")
    public ApiControllerResponse<StudyRecord> getStudyRecordByUserIdAndVideoId(
            @PathVariable Long userId,
            @PathVariable Long videoId
    ) {
        StudyRecord studyRecord = studyRecordService.getStudyRecordByUserIdAndVideoId(userId, videoId);
        if (studyRecord != null) {
            return ApiControllerResponse.success(studyRecord);
        } else {
            return ApiControllerResponse.success("未找到学习记录", null);
        }
    }
}
