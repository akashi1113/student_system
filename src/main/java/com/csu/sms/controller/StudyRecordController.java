package com.csu.sms.controller;

import com.csu.sms.annotation.LogOperation;
import com.csu.sms.common.ApiControllerResponse;
import com.csu.sms.dto.StudyRecordDTO;
import com.csu.sms.service.StudyRecordService;
import com.csu.sms.util.UserContext;
import com.csu.sms.vo.StudyRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/study-records")
@RequiredArgsConstructor
@Slf4j
public class StudyRecordController {
    private final StudyRecordService studyRecordService;

    // 保存学习记录
    @PostMapping("/save")
    @LogOperation(module = "学习分析", operation = "保存学习记录", description = "保存用户学习记录")
    public ApiControllerResponse<Boolean> saveStudyRecord(@RequestBody StudyRecordDTO dto) {
        // ✨ 修改：从 UserContext 获取用户ID，并且强制要求用户必须登录
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        dto.setUserId(currentUserId);

        boolean success = studyRecordService.saveStudyRecord(dto);
        return success ? ApiControllerResponse.success(true) : ApiControllerResponse.error(500,"保存失败");
    }

    /**
     * 【新增】获取单个视频的学习记录
     * 用户点进视频播放页时调用，用于实现断点续传
     * @param videoId 视频ID
     * @return 包含上次播放位置的视频学习记录
     */
    @GetMapping("/video/{videoId}")
    public ApiControllerResponse<StudyRecordVO> getStudyRecordForVideo(
            @PathVariable Long videoId
            // ✨ 修改：移除了 @RequestParam Long userId
    ) {
        // ✨ 修改：从 UserContext 获取用户ID，并且强制要求用户必须登录
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        StudyRecordVO recordVO = studyRecordService.getStudyRecordVOByUserIdAndVideoId(currentUserId, videoId);
        return ApiControllerResponse.success(recordVO);
    }

    /**
     * 【重构】获取当前用户的所有学习记录列表
     * 用于“我的学习”页面展示
     * @return 学习记录列表
     */
    @GetMapping("/my-list")
    public ApiControllerResponse<List<StudyRecordVO>> getUserStudyRecords(
            // ✨ 修改：移除了 @RequestParam Long userId
    ) {
        // ✨ 修改：从 UserContext 获取用户ID，并且强制要求用户必须登录
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        List<StudyRecordVO> records = studyRecordService.getStudyRecordsByUserId(currentUserId);
        return ApiControllerResponse.success(records);
    }
}






//package com.csu.sms.controller;
//
//import com.csu.sms.annotation.LogOperation;
//import com.csu.sms.common.ApiControllerResponse;
//import com.csu.sms.dto.StudyRecordDTO;
//import com.csu.sms.model.course.StudyRecord;
//import com.csu.sms.service.StudyRecordService;
//import com.csu.sms.vo.StudyRecordVO;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//import static com.csu.sms.util.UserContext.getCurrentUserId;
//
//@CrossOrigin(origins = "http://localhost:5173")
//@RestController
//@RequestMapping("/api/study-records")
//@RequiredArgsConstructor
//@Slf4j
//public class StudyRecordController {
//    private final StudyRecordService studyRecordService;
//
//    @PostMapping("/save")
//    @LogOperation(module = "学习分析", operation = "保存学习记录", description = "保存用户学习记录")
//    public ApiControllerResponse<Boolean> saveStudyRecord(@RequestBody StudyRecordDTO dto) {
//        // 将当前用户ID设置到DTO中
////        dto.setUserId(getCurrentUserId());
//        boolean success = studyRecordService.saveStudyRecord(dto);
//        return success ? ApiControllerResponse.success(true) : ApiControllerResponse.error(500,"保存失败");
//    }
//
//    /**
//     * 【新增】获取单个视频的学习记录
//     * 用户点进视频播放页时调用，用于实现断点续传
//     * @param videoId 视频ID
//     * @return 包含上次播放位置的视频学习记录
//     */
//    @GetMapping("/video/{videoId}")
//    public ApiControllerResponse<StudyRecordVO> getStudyRecordForVideo(
//            @PathVariable Long videoId,
//            @RequestParam(defaultValue = "1") Long userId) {
////        Long userId = getCurrentUserId();
//        // 调用我们重构后的Service方法获取单个记录（Service中会处理首次观看的情况）
//        StudyRecordVO recordVO = studyRecordService.getStudyRecordVOByUserIdAndVideoId(userId, videoId);
//        return ApiControllerResponse.success(recordVO);
//    }
//
//    /**
//     * 【重构】获取当前用户的所有学习记录列表
//     * 用于“我的学习”页面展示
//     * @return 学习记录列表
//     */
//    @GetMapping("/my-list")
//    public ApiControllerResponse<List<StudyRecordVO>> getUserStudyRecords(
//            @RequestParam(defaultValue = "1") Long userId
//    ) {
////        Long userId = getCurrentUserId();
//        // 调用我们重构并优化后的Service方法
//        List<StudyRecordVO> records = studyRecordService.getStudyRecordsByUserId(userId);
//        return ApiControllerResponse.success(records);
//    }
//}
