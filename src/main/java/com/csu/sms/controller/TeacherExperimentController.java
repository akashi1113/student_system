package com.csu.sms.controller;

import com.csu.sms.annotation.LogOperation;
import com.csu.sms.common.PageResult;
import com.csu.sms.dto.*;
import com.csu.sms.service.ExperimentService;
import com.csu.sms.service.TeacherExperimentService;
import com.csu.sms.util.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
        allowCredentials = "true")
@RestController
@RequestMapping("/api/teacher/experiment")
public class TeacherExperimentController {

    @Autowired
    private TeacherExperimentService teacherExperimentService;
    // private ExperimentService experimentService;



    // 创建实验
    @PostMapping
    @LogOperation(module = "教师实验管理", operation = "创建实验")
    public Result<Long> createExperiment(
            @RequestBody @Valid TeacherExperimentDTO dto) {
        Long id = teacherExperimentService.createExperiment(dto, dto.getCreatedBy());
        return Result.success(id);
    }

    // 获取实验列表
    // 分页查询实验列表
    @GetMapping("/page")
    public Result<PageResult<TeacherExperimentDTO>> getExperiments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<TeacherExperimentDTO> result = teacherExperimentService.getExperiments(page, size);
        return Result.success(result);
    }

    // 保存实验模板
    @PostMapping("/template")
    @LogOperation(module = "教师实验管理", operation = "保存实验模板", description = "保存实验模板")
    public ResponseEntity<Void> saveTemplate(@RequestBody ExperimentTemplateDTO templateDTO) {
        teacherExperimentService.saveExperimentTemplate(templateDTO);
        return ResponseEntity.ok().build();
    }

    // 删除实验模板
    @DeleteMapping("/template/{id}")
    @LogOperation(module = "教师实验管理", operation = "删除实验模板", description = "删除实验模板")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        teacherExperimentService.deleteExperimentTemplate(id);
        return ResponseEntity.ok().build();
    }

    // 获取实验模板
    @GetMapping("/template/{experimentId}")
    public ResponseEntity<ExperimentTemplateDTO> getTemplate(@PathVariable Long experimentId) {
        ExperimentTemplateDTO template = teacherExperimentService.getTemplateByExperimentId(experimentId);
        return ResponseEntity.ok(template);
    }

    // 创建时间段
    @PostMapping("/time-slot")
    @LogOperation(module = "教师实验管理", operation = "创建时间段", description = "创建实验时间段")
    public Result<Long> createTimeSlot(
            @RequestBody TimeSlotDTO timeSlotDTO) {
        Long id = teacherExperimentService.createTimeSlot(timeSlotDTO);
        return Result.success(id);
    }

    // 更新时间段
    @PutMapping("/time-slot/{id}")
    @LogOperation(module = "教师实验管理", operation = "更新时间段", description = "更新实验时间段")
    public Result<Void> updateTimeSlot(
            @PathVariable Long id,
            @RequestBody TimeSlotDTO timeSlotDTO) {
        teacherExperimentService.updateTimeSlot(id, timeSlotDTO);
        return Result.success();
    }

    // 删除时间段
    @DeleteMapping("/time-slot/{id}")
    @LogOperation(module = "教师实验管理", operation = "删除时间段", description = "删除实验时间段")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable Long id) {
        teacherExperimentService.deleteTimeSlot(id);
        return ResponseEntity.ok().build();
    }

    // 获取时间段列表
    @GetMapping("/time-slots/{experimentId}")
    public ResponseEntity<List<TimeSlotDTO>> getTimeSlots(@PathVariable Long experimentId) {
        List<TimeSlotDTO> slots = teacherExperimentService.getTimeSlotsByExperimentId(experimentId);
        System.out.println("返回的时间段数量: " + slots.size()); // 调试日志
        return ResponseEntity.ok(slots);
    }

    // 审批预约
    @PostMapping("/approve-booking")
    @LogOperation(module = "教师实验管理", operation = "审批预约", description = "审批学生预约")
    public ResponseEntity<Void> approveBooking(@RequestBody BookingApprovalDTO approvalDTO) {
        teacherExperimentService.approveBooking(approvalDTO);
        return ResponseEntity.ok().build();
    }

    // 添加发布/取消发布接口
    @PostMapping("/{id}/publish")
    @LogOperation(module = "实验管理", operation = "发布实验", description = "教师发布实验")
    public ResponseEntity<Void> publishExperiment(
            @PathVariable Long id,
            @RequestParam Boolean isPublished) {
        teacherExperimentService.togglePublishStatus(id, isPublished);
        return ResponseEntity.ok().build();
    }

    // 获取待审批预约列表
    @GetMapping("/pending-bookings")
    public ResponseEntity<List<ExperimentBookingDTO>> getPendingBookings() {
        List<ExperimentBookingDTO> bookings = teacherExperimentService.getPendingBookings();
        return ResponseEntity.ok(bookings);
    }

    // 更新实验
    @PutMapping("/{id}")
    @LogOperation(module = "教师实验管理", operation = "更新实验")
    public Result<Void> updateExperiment(
            @PathVariable Long id,
            @RequestBody @Valid TeacherExperimentDTO dto) {
        teacherExperimentService.updateExperiment(id, dto);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<TeacherExperimentDTO> getExperiment(@PathVariable Long id) {
        return Result.success(teacherExperimentService.getExperimentById(id));
    }

    // 条件查询实验
    @GetMapping("/query")
    public Result<PageResult<TeacherExperimentDTO>> queryExperiments(
            @Valid TeacherExperimentQueryDTO queryDTO,
            @RequestAttribute Long teacherId) {
        queryDTO.setTeacherId(teacherId);

        // 添加调试日志（生产环境可移除）
        PageResult<TeacherExperimentDTO> result = teacherExperimentService.queryExperiments(queryDTO);
        System.out.println("[DEBUG] 实际返回类型: " + result.getClass().getName());

        return Result.success(result);
    }

}
