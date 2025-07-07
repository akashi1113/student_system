package com.csu.sms.controller;

import com.csu.sms.annotation.LogOperation;
import com.csu.sms.dto.*;
import com.csu.sms.service.ExperimentService;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
        allowCredentials = "true")
@RestController
@RequestMapping("/api/experiment")
public class ExperimentController {

    @Autowired
    private ExperimentService experimentService;

    // 获取所有实验项目
    @GetMapping("/projects")
    public ApiResponse<List<ExperimentDTO>> getAllExperiments() {
        List<ExperimentDTO> experiments = experimentService.getAllExperiments();
        // 为每个实验添加预约状态信息
        experiments.forEach(exp -> {
            List<ExperimentBookingDTO> bookings = experimentService.getBookingsByExperimentId(exp.getId());
            if (!bookings.isEmpty()) {
                // 假设我们只关心最新的预约状态
                exp.setStatus(bookings.get(0).getStatus());
                exp.setApprovalStatus(bookings.get(0).getApprovalStatus());
            }
        });
        return ApiResponse.success(experiments);
    }

    // 获取预约详情
    @GetMapping("/bookings/{bookingId}")
    public ApiResponse<ExperimentBookingDTO> getBooking(@PathVariable Long bookingId) {
        System.out.println("Received bookingId: " + bookingId); // 添加日志
        return ApiResponse.success(experimentService.getBooking(bookingId));
    }

    // 新增基于时间段的预约方法
    @PostMapping("/book-with-slot")
    @LogOperation(module = "实验管理", operation = "预约实验(时间段)", description = "学生通过时间段预约实验")
    public ApiResponse<ExperimentBookingDTO> bookExperimentWithSlot(@RequestBody BookingWithSlotRequest request) {
        System.out.println("Received: " + request);
        return ApiResponse.success(experimentService.bookExperimentWithTimeSlot(
                request.getExperimentId(),
                request.getUserId(),
                request.getTimeSlotId()
        ));
    }

    // 预约实验
    @Data
    public static class BookingRequest {
        private Long experimentId;
        private Long userId;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
    }

    @Data
    public static class BookingWithSlotRequest {
        private Long experimentId;
        private Long userId;
        private Long timeSlotId;
    }

    @PostMapping("/book")
    @LogOperation(module = "实验管理", operation = "预约实验", description = "学生预约实验")
    public ApiResponse<ExperimentBookingDTO> bookExperiment(@RequestBody BookingRequest request) {
        System.out.println("Received: " + request); // 确保日志打印
        return ApiResponse.success(experimentService.bookExperiment(
                request.getExperimentId(),
                request.getUserId(),
                request.getStartTime(),
                request.getEndTime()
        ));
    }


    // 开始实验
    @PostMapping("/start/{bookingId}")
    @LogOperation(module = "实验管理", operation = "开始实验", description = "学生开始实验")
    public ApiResponse<ExperimentRecordDTO> startExperiment(@PathVariable Long bookingId) {
        return ApiResponse.success(experimentService.startExperiment(bookingId));
    }

    // 保存实验记录
    @PostMapping("/save-record")
    @LogOperation(module = "实验管理", operation = "保存实验记录", description = "保存实验过程记录")
    public ApiResponse<ExperimentRecordDTO> saveExperimentRecord(@RequestBody ExperimentRecordDTO recordDTO) {
        return ApiResponse.success(experimentService.saveExperimentRecord(recordDTO));
    }

    // 结束实验
    @PostMapping("/end/{recordId}")
    @LogOperation(module = "实验管理", operation = "结束实验", description = "学生结束实验")
    public ApiResponse<ExperimentRecordDTO> endExperiment(@PathVariable Long recordId) {
        return ApiResponse.success(experimentService.endExperiment(recordId));
    }

    // 生成实验报告
    @PostMapping("/generate-report")
    @LogOperation(module = "实验管理", operation = "生成实验报告", description = "生成实验报告")
    public ApiResponse<ExperimentReportDTO> generateReport(@RequestBody ExperimentReportDTO reportDTO) {
        return ApiResponse.success(experimentService.generateReport(reportDTO));
    }

    // 导出报告
    @PostMapping("/export-report")
    @LogOperation(module = "实验管理", operation = "导出实验报告", description = "导出实验报告")
    public ApiResponse<String> exportReport(
            @RequestParam Long reportId,
            @RequestParam String format) {
        return ApiResponse.success(experimentService.exportReport(reportId, format));
    }

    // 导入实验数据
    @PostMapping("/import-data")
    @LogOperation(module = "实验管理", operation = "导入实验数据", description = "导入实验数据文件")
    public ApiResponse<ExperimentRecordDTO> importExperimentData(
            @RequestParam Long recordId,
            @RequestParam MultipartFile file) {
        return ApiResponse.success(experimentService.importExperimentData(recordId, file));
    }

    // 获取单个实验详情
    @GetMapping("/{id}")
    public ApiResponse<ExperimentDTO> getExperimentById(@PathVariable Long id) {
        return ApiResponse.success(experimentService.getExperimentById(id));
    }

    @GetMapping("/published")
    public ApiResponse<List<ExperimentDTO>> getPublishedExperiments() {
        return ApiResponse.success(experimentService.getPublishedExperiments());
    }

    @GetMapping("/{experimentId}/time-slots")
    public ApiResponse<List<TimeSlotDTO>> getTimeSlotsByExperimentId(@PathVariable Long experimentId) {
        List<TimeSlotDTO> timeSlots = experimentService.getTimeSlotsByExperimentId(experimentId);
        return ApiResponse.success(timeSlots);
    }

    @PutMapping("/{id}/status")
    @LogOperation(module = "实验管理", operation = "更新实验状态", description = "更新实验状态")
    public ApiResponse<Void> updateExperimentStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        experimentService.updateExperimentStatus(id, status);
        return ApiResponse.success(null);
    }

}
