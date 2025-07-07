package com.csu.sms.controller;

import com.csu.sms.annotation.LogOperation;

import com.csu.sms.dto.*;

import com.csu.sms.common.ApiResponse;
import com.csu.sms.dto.ExperimentBookingDTO;
import com.csu.sms.dto.ExperimentDTO;
import com.csu.sms.dto.ExperimentRecordDTO;
import com.csu.sms.dto.ExperimentReportDTO;
import com.csu.sms.model.experiment.CodeHistory;
import com.csu.sms.model.experiment.Experiment;
import com.csu.sms.model.experiment.ExperimentRecord;

import com.csu.sms.service.ExperimentService;
import com.csu.sms.util.JwtUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:5173"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
        allowCredentials = "true")
@RestController
@RequestMapping("/api/experiment")
public class ExperimentController {

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private JwtUtil jwtUtil;

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

        //开始实验，注意step格式
        @PostMapping("/{experimentId}/start")
        public ApiResponse<ExperimentRecord> startExperiment (
                @PathVariable Long experimentId,
                @RequestHeader("Authorization") String token){
            try {
                Long userId = jwtUtil.extractUserId(token);
                ExperimentRecord record = experimentService.startExperiment(experimentId, userId);
                return ApiResponse.success("实验开始成功", record);
            } catch (Exception e) {
                return ApiResponse.error(500, "开始实验失败: " + e.getMessage());
            }
        }

        @GetMapping("/record/{experimentRecordId}/detail")
        public ApiResponse<Map<String, Object>> getExperimentDetail (@PathVariable Long experimentRecordId){
            try {
                Map<String, Object> detail = experimentService.getExperimentRecordDetail(experimentRecordId);
                if (detail == null) {
                    return ApiResponse.error(404, "实验记录不存在");
                }
                return ApiResponse.success("获取实验详情成功", detail);
            } catch (Exception e) {
                return ApiResponse.error(500, "获取实验详情失败: " + e.getMessage());
            }
        }

        @PostMapping("/code-history")
        public ApiResponse<Void> saveCodeHistory (@RequestBody CodeHistoryRequest request){
            try {
                experimentService.saveCodeHistory(
                        request.getExperimentRecordId(),
                        request.getCode(),
                        request.getLanguage(),
                        request.getActionType(),
                        request.getExecutionResult()
                );
                return ApiResponse.success("代码历史保存成功", null);
            } catch (Exception e) {
                return ApiResponse.error(500, "保存代码历史失败: " + e.getMessage());
            }
        }

        @GetMapping("/record/{experimentRecordId}/code-history")
        public ApiResponse<List<CodeHistory>> getCodeHistory (@PathVariable Long experimentRecordId){
            try {
                List<CodeHistory> history = experimentService.getCodeHistory(experimentRecordId);
                return ApiResponse.success("获取代码历史成功", history);
            } catch (Exception e) {
                return ApiResponse.error(500, "获取代码历史失败: " + e.getMessage());
            }
        }

        @PutMapping("/step-record")
        public ApiResponse<Void> updateStepRecord (@RequestBody StepRecordRequest request){
            try {
                experimentService.updateStepRecord(request);
                return ApiResponse.success("步骤记录更新成功", null);
            } catch (Exception e) {
                return ApiResponse.error(500, "更新步骤记录失败: " + e.getMessage());
            }
        }

        @PostMapping("/complete")
        public ApiResponse<Void> completeExperiment (@RequestBody CompleteExperimentRequest request){
            try {
                experimentService.completeExperiment(
                        request.getExperimentRecordId(),
                        request.getFinalCode(),
                        request.getFinalLanguage(),
                        request.getExecutionResult()
                );
                return ApiResponse.success("实验完成成功", null);
            } catch (Exception e) {
                return ApiResponse.error(500, "完成实验失败: " + e.getMessage());
            }
        }

        @GetMapping("/record/{experimentRecordId}/report")
        public ApiResponse<Map<String, Object>> generateReport (@PathVariable Long experimentRecordId){
            try {
                Map<String, Object> reportData = experimentService.generateReportData(experimentRecordId);
                return ApiResponse.success("报告生成成功", reportData);
            } catch (Exception e) {
                return ApiResponse.error(500, "生成报告失败: " + e.getMessage());
            }
        }

        @GetMapping("/user-records")
        public ApiResponse<List<ExperimentRecord>> getUserExperimentRecords (
                @RequestHeader("Authorization") String token){
            try {
                Long userId = jwtUtil.extractUserId(token);
                List<ExperimentRecord> records = experimentService.getUserExperimentRecords(userId);
                return ApiResponse.success("获取用户实验记录成功", records);
            } catch (Exception e) {
                return ApiResponse.error(500, "获取用户实验记录失败: " + e.getMessage());
            }
        }

        @GetMapping("/record/{experimentRecordId}")
        public ApiResponse<ExperimentRecord> getExperimentRecord (@PathVariable Long experimentRecordId){
            try {
                ExperimentRecord record = experimentService.getExperimentRecordById(experimentRecordId);
                if (record == null) {
                    return ApiResponse.error(404, "实验记录不存在");
                }
                return ApiResponse.success("获取实验记录成功", record);
            } catch (Exception e) {
                return ApiResponse.error(500, "获取实验记录失败: " + e.getMessage());
            }
        }

        /**
         * 获取学生最后一次提交的实验报告
         */
        @GetMapping("/reports/{experimentId}")
        public ApiResponse<Map<String, Object>> getStudentFinalReport (
                @PathVariable Long experimentId,
                @RequestHeader("Authorization") String token){
            try {
                Long studentId = jwtUtil.extractUserId(token);
                Map<String, Object> report = experimentService.getStudentFinalReport(experimentId, studentId);
                return ApiResponse.success("获取报告成功", report);
            } catch (Exception e) {
                return ApiResponse.error(400, e.getMessage());
            }
        }

        /**
         * 获取实验所有学生的报告列表
         * @param experimentId 实验ID
         * @return 统一响应体
         */
        @GetMapping("/reports/{experimentId}/all")
        public ApiResponse<List<Map<String, Object>>> getExperimentReports (@PathVariable("experimentId") Long
        experimentId){
            try {
                List<ExperimentRecord> records = experimentService.getExperimentRecords(experimentId);

                List<Map<String, Object>> reports = new ArrayList<>();
                for (ExperimentRecord record : records) {
                    if ("COMPLETED".equals(record.getStatus())) {
                        Map<String, Object> report = experimentService.getStudentFinalReport(
                                experimentId, record.getUserId());
                        reports.add(report);
                    }
                }

                return ApiResponse.success("获取报告列表成功", reports);
            } catch (Exception e) {
                return ApiResponse.error(400, e.getMessage());
            }
        }

        // 请求对象
        public static class CodeHistoryRequest {
            private Long experimentRecordId;
            private String code;
            private String language;
            private String actionType;
            private Object executionResult;

            // getter and setter
            public Long getExperimentRecordId() {
                return experimentRecordId;
            }

            public void setExperimentRecordId(Long experimentRecordId) {
                this.experimentRecordId = experimentRecordId;
            }

            public String getCode() {
                return code;
            }

            public void setCode(String code) {
                this.code = code;
            }

            public String getLanguage() {
                return language;
            }

            public void setLanguage(String language) {
                this.language = language;
            }

            public String getActionType() {
                return actionType;
            }

            public void setActionType(String actionType) {
                this.actionType = actionType;
            }

            public Object getExecutionResult() {
                return executionResult;
            }

            public void setExecutionResult(Object executionResult) {
                this.executionResult = executionResult;
            }
        }

        public static class StepRecordRequest {
            private Long experimentRecordId;
            private Integer stepIndex;
            private Boolean completed;
            private String notes;

            // getter and setter
            public Long getExperimentRecordId() {
                return experimentRecordId;
            }

            public void setExperimentRecordId(Long experimentRecordId) {
                this.experimentRecordId = experimentRecordId;
            }

            public Integer getStepIndex() {
                return stepIndex;
            }

            public void setStepIndex(Integer stepIndex) {
                this.stepIndex = stepIndex;
            }

            public Boolean getCompleted() {
                return completed;
            }

            public void setCompleted(Boolean completed) {
                this.completed = completed;
            }

            public String getNotes() {
                return notes;
            }

            public void setNotes(String notes) {
                this.notes = notes;
            }
        }

        public static class CompleteExperimentRequest {
            private Long experimentRecordId;
            private String finalCode;
            private String finalLanguage;
            private Object executionResult;

            // getter and setter
            public Long getExperimentRecordId() {
                return experimentRecordId;
            }

            public void setExperimentRecordId(Long experimentRecordId) {
                this.experimentRecordId = experimentRecordId;
            }

            public String getFinalCode() {
                return finalCode;
            }

            public void setFinalCode(String finalCode) {
                this.finalCode = finalCode;
            }

            public String getFinalLanguage() {
                return finalLanguage;
            }

            public void setFinalLanguage(String finalLanguage) {
                this.finalLanguage = finalLanguage;
            }

            public Object getExecutionResult() {
                return executionResult;
            }

            public void setExecutionResult(Object executionResult) {
                this.executionResult = executionResult;
            }
        }

        public static class CancelExperimentRequest {
            private Long experimentRecordId;

            // getter and setter
            public Long getExperimentRecordId() {
                return experimentRecordId;
            }

            public void setExperimentRecordId(Long experimentRecordId) {
                this.experimentRecordId = experimentRecordId;
            }
        }

    }
