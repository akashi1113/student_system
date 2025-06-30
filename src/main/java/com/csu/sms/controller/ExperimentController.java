package com.csu.sms.controller;

import com.csu.sms.dto.ApiResponse;
import com.csu.sms.dto.ExperimentBookingDTO;
import com.csu.sms.dto.ExperimentDTO;
import com.csu.sms.dto.ExperimentRecordDTO;
import com.csu.sms.dto.ExperimentReportDTO;
import com.csu.sms.service.ExperimentService;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/experiment")
public class ExperimentController {

    @Autowired
    private ExperimentService experimentService;

    // 获取所有实验项目
    @GetMapping("/projects")
    public ApiResponse<List<ExperimentDTO>> getAllExperiments() {
        return ApiResponse.success(experimentService.getAllExperiments());
    }

    // 获取预约详情
    @GetMapping("/bookings/{bookingId}")
    public ApiResponse<ExperimentBookingDTO> getBooking(@PathVariable Long bookingId) {
        System.out.println("Received bookingId: " + bookingId); // 添加日志
        return ApiResponse.success(experimentService.getBooking(bookingId));
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

    @PostMapping("/book")
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
    public ApiResponse<ExperimentRecordDTO> startExperiment(@PathVariable Long bookingId) {
        return ApiResponse.success(experimentService.startExperiment(bookingId));
    }

    // 保存实验记录
    @PostMapping("/save-record")
    public ApiResponse<ExperimentRecordDTO> saveExperimentRecord(@RequestBody ExperimentRecordDTO recordDTO) {
        return ApiResponse.success(experimentService.saveExperimentRecord(recordDTO));
    }

    // 结束实验
    @PostMapping("/end/{recordId}")
    public ApiResponse<ExperimentRecordDTO> endExperiment(@PathVariable Long recordId) {
        return ApiResponse.success(experimentService.endExperiment(recordId));
    }

    // 生成实验报告
    @PostMapping("/generate-report")
    public ApiResponse<ExperimentReportDTO> generateReport(@RequestBody ExperimentReportDTO reportDTO) {
        return ApiResponse.success(experimentService.generateReport(reportDTO));
    }

    // 导出报告
    @PostMapping("/export-report")
    public ApiResponse<String> exportReport(
            @RequestParam Long reportId,
            @RequestParam String format) {
        return ApiResponse.success(experimentService.exportReport(reportId, format));
    }

    // 导入实验数据
    @PostMapping("/import-data")
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

}
