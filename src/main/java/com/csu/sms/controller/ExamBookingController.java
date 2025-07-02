package com.csu.sms.controller;

import com.csu.sms.common.PageResult;
import com.csu.sms.service.ExamBookingService;
import com.csu.sms.model.booking.ExamTimeSlot;
import com.csu.sms.model.booking.ExamBooking;
import com.csu.sms.model.booking.ExamNotification;
import com.csu.sms.dto.booking.BookingDetailsDTO;
import com.csu.sms.dto.booking.BookingRequestDTO;
import com.csu.sms.dto.booking.TimeSlotCreateDTO;
import com.csu.sms.dto.booking.CancelBookingDTO;
import com.csu.sms.dto.booking.CheckInDTO;
import com.csu.sms.common.ApiResponse;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam-booking")
@CrossOrigin(origins = "http://localhost:5173")
public class ExamBookingController {

    @Autowired
    private ExamBookingService examBookingService;

    // ============================== 考试时间段管理 ==============================

    /**
     * 查询考试的可预约时间段
     */
    @GetMapping("/time-slots/{examId}")
    public ApiResponse<List<ExamTimeSlot>> getAvailableTimeSlots(@PathVariable("examId") Long examId) {
        return examBookingService.getAvailableTimeSlots(examId);
    }

    /**
     * 查询指定日期的时间段
     */
    @GetMapping("/time-slots/by-date")
    public ApiResponse<List<ExamTimeSlot>> getTimeSlotsByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) String examMode) {
        return examBookingService.getTimeSlotsByDate(date, examMode);
    }

    /**
     * 创建考试时间段
     */
    @PostMapping("/time-slots")
    public ApiResponse<ExamTimeSlot> createTimeSlot(@Valid @RequestBody TimeSlotCreateDTO createDTO) {
        return examBookingService.createTimeSlot(createDTO);
    }

    /**
     * 批量创建时间段
     */
    @PostMapping("/time-slots/batch")
    public ApiResponse<List<ExamTimeSlot>> batchCreateTimeSlots(@Valid @RequestBody List<TimeSlotCreateDTO> createDTOs) {
        return examBookingService.batchCreateTimeSlots(createDTOs);
    }

    /**
     * 更新时间段信息
     */
    @PutMapping("/time-slots/{timeSlotId}")
    public ApiResponse<ExamTimeSlot> updateTimeSlot(@PathVariable Long timeSlotId,
                                                    @Valid @RequestBody TimeSlotCreateDTO updateDTO) {
        return examBookingService.updateTimeSlot(timeSlotId, updateDTO);
    }

    /**
     * 删除时间段
     */
    @DeleteMapping("/time-slots/{timeSlotId}")
    public ApiResponse<Void> deleteTimeSlot(@PathVariable Long timeSlotId) {
        return examBookingService.deleteTimeSlot(timeSlotId);
    }

    // ============================== 考试预约管理 ==============================

    /**
     * 预约考试
     */
    @PostMapping("/bookings")
    public ApiResponse<ExamBooking> bookExam(@Valid @RequestBody BookingRequestDTO bookingRequest) {
        return examBookingService.bookExam(bookingRequest);
    }

    /**
     * 取消预约
     */
    @PostMapping("/bookings/{bookingId}/cancel")
    public ApiResponse<Void> cancelBooking(@PathVariable Long bookingId,
                                           @Valid @RequestBody CancelBookingDTO cancelDTO) {
        return examBookingService.cancelBooking(bookingId, cancelDTO.getUserId(), cancelDTO.getCancelReason());
    }

    /**
     * 确认预约
     */
    @PostMapping("/bookings/{bookingId}/confirm")
    public ApiResponse<Void> confirmBooking(@PathVariable Long bookingId) {
        return examBookingService.confirmBooking(bookingId);
    }

    /**
     * 查询用户的预约列表
     */
    @GetMapping("/bookings/user/{userId}")
    public ApiResponse<List<BookingDetailsDTO>> getUserBookings(@PathVariable("userId") Long userId,
                                                                @RequestParam(required = false) String status) {
        return examBookingService.getUserBookings(userId, status);
    }

    /**
     * 查询预约详情
     */
    @GetMapping("/bookings/{bookingId}")
    public ApiResponse<BookingDetailsDTO> getBookingDetails(@PathVariable("bookingId") Long bookingId) {
        return examBookingService.getBookingDetails(bookingId);
    }

    /**
     * 根据用户ID和考试ID获取预约ID
     */
    @GetMapping("/bookings/by-user-exam")
    public ApiResponse<Long> getBookingIdByUserAndExam(
            @RequestParam("userId") Long userId,
            @RequestParam("examId") Long examId
    ) {
        return examBookingService.getBookingIdByUserAndExam(userId, examId);
    }

    /**
     * 通过预约号查询预约详情
     */
    @GetMapping("/bookings/by-number/{bookingNumber}")
    public ApiResponse<BookingDetailsDTO> getBookingDetailsByNumber(@PathVariable String bookingNumber) {
        return examBookingService.getBookingDetailsByNumber(bookingNumber);
    }

    /**
     * 签到
     */
    @PostMapping("/bookings/{bookingId}/check-in")
    public ApiResponse<Void> checkIn(@PathVariable Long bookingId,
                                     @Valid @RequestBody CheckInDTO checkInDTO) {
        return examBookingService.checkIn(bookingId, checkInDTO.getCheckInStatus());
    }

    // ============================== 统计查询 ==============================

    /**
     * 查询时间段的预约统计
     */
    @GetMapping("/stats/time-slot/{timeSlotId}")
    public ApiResponse<Map<String, Object>> getTimeSlotStats(@PathVariable Long timeSlotId) {
        return examBookingService.getTimeSlotStats(timeSlotId);
    }

    /**
     * 查询用户的预约统计
     */
    @GetMapping("/stats/user/{userId}")
    public ApiResponse<Map<String, Object>> getUserBookingStats(@PathVariable("userId") Long userId) {
        return examBookingService.getUserBookingStats(userId);
    }

    // ============================== 通知管理 ==============================

    /**
     * 查询用户通知列表
     */
    @GetMapping("/notifications/{userId}")
    public ApiResponse<List<ExamNotification>> getUserNotifications(@PathVariable("userId") Long userId) {
        return examBookingService.getUserNotifications(userId);
    }

    /**
     * 查询未读通知
     */
    @GetMapping("/notifications/unread/{userId}")
    public ApiResponse<List<ExamNotification>> getUnreadNotifications(@PathVariable Long userId) {
        return examBookingService.getUnreadNotifications(userId);
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/notifications/{notificationId}/read")
    public ApiResponse<Void> markNotificationAsRead(@PathVariable("notificationId") Long notificationId) {
        return examBookingService.markNotificationAsRead(notificationId);
    }

    /**
     * 批量标记通知为已读
     */
    @PostMapping("/notifications/batch-read")
    public ApiResponse<Void> batchMarkAsRead(@RequestParam Long userId,
                                             @RequestBody List<Long> notificationIds) {
        return examBookingService.batchMarkAsRead(userId, notificationIds);
    }

    /**
     * 发送考试提醒通知
     */
    @PostMapping("/notifications/exam-reminder/{examId}")
    public ApiResponse<Void> sendExamReminder(@PathVariable Long examId) {
        return examBookingService.sendExamReminder(examId);
    }

    // ============================== 管理功能 ==============================

    /**
     * 处理过期预约
     */
    @PostMapping("/admin/handle-expired")
    public ApiResponse<Void> handleExpiredBookings() {
        return examBookingService.handleExpiredBookings();
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<BookingDetailsDTO>> getBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return examBookingService.getBookings(status, startDate, endDate, pageNum, pageSize);
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> getBookingStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return examBookingService.getBookingStats(startDate, endDate);
    }

    // 获取考试时间段列表
    @GetMapping("/{examId}/time-slots")
    public ApiResponse<List<ExamTimeSlot>> getTimeSlots(
            @PathVariable("examId") Long examId) {
        return examBookingService.getTimeSlots(examId);
    }

    // 切换时间段状态
    @PostMapping("/time-slots/{timeSlotId}/toggle-status")
    public ApiResponse<Void> toggleTimeSlotStatus(
            @PathVariable("timeSlotId") Long timeSlotId) {
        return examBookingService.toggleTimeSlotStatus(timeSlotId);
    }

}