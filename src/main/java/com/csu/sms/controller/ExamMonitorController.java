package com.csu.sms.controller;

import com.csu.sms.common.ApiResponse;
import com.csu.sms.model.ExamMonitor;
import com.csu.sms.model.ExamMonitorSummary;
import com.csu.sms.service.ExamMonitorService;
import com.csu.sms.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/exam/monitor")
@CrossOrigin(origins = "5173")
public class ExamMonitorController {

    private static final Logger logger = LoggerFactory.getLogger(ExamMonitorController.class);

    @Autowired
    private ExamMonitorService examMonitorService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 上传监考图片
     */
    @PostMapping("/upload")
    public ApiResponse<String> uploadMonitorImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam("examId") Long examId,
            @RequestParam("timestamp") Long timestamp,
            @RequestHeader("Authorization") String token) {
        Long userId=jwtUtil.extractUserId(token);

        try {
            // 参数验证
            if (image.isEmpty()) {
                return ApiResponse.error("图片文件不能为空");
            }
            if (examId == null || timestamp == null) {
                return ApiResponse.error("参数不完整");
            }

            // 文件大小限制 (5MB)
            if (image.getSize() > 5 * 1024 * 1024) {
                return ApiResponse.error("图片文件大小不能超过5MB");
            }

            // 文件类型验证
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error("只支持图片格式文件");
            }

            // 保存图片文件
            String imagePath = saveMonitorImage(image, examId, userId, timestamp);

            // 异步处理监考分析
            CompletableFuture.runAsync(() -> {
                try {
                    examMonitorService.processMonitorImage(imagePath, examId, userId, timestamp);
                } catch (Exception e) {
                    logger.error("异步处理监考图片失败: examId={}, userId={}", examId, userId, e);
                }
            });

            return ApiResponse.success("监考图片上传成功", imagePath);

        } catch (IOException e) {
            logger.error("保存监考图片失败: examId={}, userId={}", examId, userId, e);
            return ApiResponse.error(500, "图片保存失败");
        } catch (Exception e) {
            logger.error("上传监考图片异常: examId={}, userId={}", examId, userId, e);
            return ApiResponse.error(500, "系统内部错误");
        }
    }

    /**
     * 获取监考记录
     */
    @GetMapping("/records")
    public ApiResponse<List<ExamMonitor>> getMonitorRecords(
            @RequestParam("examId") Long examId,
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "abnormalOnly", defaultValue = "false") Boolean abnormalOnly) {

        Long userId=jwtUtil.extractUserId(token);
        try {
            if (examId == null) {
                return ApiResponse.error("考试ID不能为空");
            }

            List<ExamMonitor> records;
            if (abnormalOnly) {
                records = examMonitorService.getAbnormalMonitorRecords(examId, userId);
            } else {
                records = examMonitorService.getMonitorRecords(examId, userId);
            }

            return ApiResponse.success("获取监考记录成功", records);

        } catch (Exception e) {
            logger.error("获取监考记录失败: examId={}, userId={}", examId, userId, e);
            return ApiResponse.error(500, "获取监考记录失败");
        }
    }

    /**
     * 获取监考统计信息
     */
    @GetMapping("/summary")
    public ApiResponse<Object> getMonitorSummary(
            @RequestParam("examId") Long examId,
            @RequestHeader("Authorization") String token) {
        Long userId=jwtUtil.extractUserId(token);
        try {
            if (examId == null) {
                return ApiResponse.error("考试ID不能为空");
            }

            if (userId != null) {
                // 获取单个用户的监考统计
                ExamMonitorSummary summary = examMonitorService.getMonitorSummary(examId, userId);
                return ApiResponse.success("获取监考统计成功", summary);
            } else {
                // 获取整个考试的监考统计
                List<ExamMonitorSummary> summaries = examMonitorService.getExamMonitorSummaries(examId);
                return ApiResponse.success("获取监考统计成功", summaries);
            }

        } catch (Exception e) {
            logger.error("获取监考统计失败: examId={}, userId={}", examId, userId, e);
            return ApiResponse.error(500, "获取监考统计失败");
        }
    }

    /**
     * 获取监考状态
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getMonitorStatus(
            @RequestParam("examId") Long examId,
            @RequestHeader("Authorization") String token) {
        Long userId=jwtUtil.extractUserId(token);
        try {
            if (examId == null || userId == null) {
                return ApiResponse.error("参数不完整");
            }

            Map<String, Object> status = examMonitorService.getMonitorStatus(examId, userId);
            return ApiResponse.success("获取监考状态成功", status);

        } catch (Exception e) {
            logger.error("获取监考状态失败: examId={}, userId={}", examId, userId, e);
            return ApiResponse.error(500, "获取监考状态失败");
        }
    }

    /**
     * 获取时间范围内的监考记录
     */
    @GetMapping("/records/time-range")
    public ApiResponse<List<ExamMonitor>> getMonitorRecordsByTimeRange(
            @RequestParam("examId") Long examId,
            @RequestParam("startTime") Long startTime,
            @RequestParam("endTime") Long endTime,
            @RequestHeader("Authorization") String token) {
        Long userId=jwtUtil.extractUserId(token);
        try {
            if (examId == null || userId == null || startTime == null || endTime == null) {
                return ApiResponse.error("参数不完整");
            }

            if (startTime >= endTime) {
                return ApiResponse.error("时间范围参数错误");
            }

            List<ExamMonitor> records = examMonitorService.getMonitorRecordsByTimeRange(
                    examId, userId, startTime, endTime);

            return ApiResponse.success("获取监考记录成功", records);

        } catch (Exception e) {
            logger.error("获取时间范围监考记录失败: examId={}, userId={}", examId, userId, e);
            return ApiResponse.error(500, "获取监考记录失败");
        }
    }

    /**
     * 删除监考记录
     */
    @DeleteMapping("/records/{id}")
    public ApiResponse<String> deleteMonitorRecord(@PathVariable("id") Long id) {
        try {
            if (id == null) {
                return ApiResponse.error("记录ID不能为空");
            }

            boolean deleted = examMonitorService.deleteMonitorRecord(id);
            if (deleted) {
                return ApiResponse.success("删除监考记录成功");
            } else {
                return ApiResponse.error("监考记录不存在");
            }

        } catch (Exception e) {
            logger.error("删除监考记录失败: id={}", id, e);
            return ApiResponse.error(500, "删除监考记录失败");
        }
    }

    /**
     * 获取监考异常统计
     */
    @GetMapping("/abnormal-stats")
    public ApiResponse<Map<String, Object>> getAbnormalStats(
            @RequestParam("examId") Long examId,
            @RequestHeader("Authorization") String token) {
        Long userId=jwtUtil.extractUserId(token);
        try {
            if (examId == null) {
                return ApiResponse.error("考试ID不能为空");
            }

            Map<String, Object> stats = examMonitorService.getAbnormalStats(examId, userId);
            return ApiResponse.success("获取异常统计成功", stats);

        } catch (Exception e) {
            logger.error("获取异常统计失败: examId={}, userId={}", examId, userId, e);
            return ApiResponse.error(500, "获取异常统计失败");
        }
    }

    /**
     * 保存监考图片
     */
    private String saveMonitorImage(MultipartFile image, Long examId, Long userId, Long timestamp)
            throws IOException {

        String uploadDir = "uploads/monitor/" + examId + "/" + userId + "/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = image.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = timestamp + "_monitor" + extension;
        String filePath = uploadDir + fileName;

        Files.copy(image.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);

        return filePath;
    }
}