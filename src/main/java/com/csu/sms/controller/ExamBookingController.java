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

import com.csu.sms.util.JwtUtil;
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

    @Autowired
    private JwtUtil jwtUtil;

    // ============================== 考试时间段管理 ==============================

    @GetMapping("/time-slots/{examId}")
    public ApiResponse<List<ExamTimeSlot>> getAvailableTimeSlots(@PathVariable("examId") Long examId) {
        return examBookingService.getAvailableTimeSlots(examId);
    }

    @GetMapping("/time-slots/by-date")
    public ApiResponse<List<ExamTimeSlot>> getTimeSlotsByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) String examMode) {
        return examBookingService.getTimeSlotsByDate(date, examMode);
    }

    @PostMapping("/time-slots")
    public ApiResponse<ExamTimeSlot> createTimeSlot(@Valid @RequestBody TimeSlotCreateDTO createDTO,
                                                    @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token);
        createDTO.setCreatedBy(userId);
        return examBookingService.createTimeSlot(createDTO);
    }

    @PostMapping("/time-slots/batch")
    public ApiResponse<List<ExamTimeSlot>> batchCreateTimeSlots(@Valid @RequestBody List<TimeSlotCreateDTO> createDTOs,
                                                                @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token);
        createDTOs.forEach(dto -> dto.setCreatedBy(userId));
        return examBookingService.batchCreateTimeSlots(createDTOs);
    }

    @PutMapping("/time-slots/{timeSlotId}")
    public ApiResponse<ExamTimeSlot> updateTimeSlot(@PathVariable Long timeSlotId,
                                                    @Valid @RequestBody TimeSlotCreateDTO updateDTO,
                                                    @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token);
        updateDTO.setCreatedBy(userId);
        return examBookingService.updateTimeSlot(timeSlotId, updateDTO);
    }

    @DeleteMapping("/time-slots/{timeSlotId}")
    public ApiResponse<Void> deleteTimeSlot(@PathVariable Long timeSlotId) {
        return examBookingService.deleteTimeSlot(timeSlotId);
    }

    // ============================== 考试预约管理 ==============================

    @PostMapping("/bookings")
    public ApiResponse<ExamBooking> bookExam(@Valid @RequestBody BookingRequestDTO bookingRequest,
                                             @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token);
        bookingRequest.setUserId(userId);
        return examBookingService.bookExam(bookingRequest);
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public ApiResponse<Void> cancelBooking(@PathVariable Long bookingId,
                                           @Valid @RequestBody CancelBookingDTO cancelDTO,
                                           @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token);
        return examBookingService.cancelBooking(bookingId, userId, cancelDTO.getCancelReason());
    }

    @PostMapping("/bookings/{bookingId}/confirm")
    public ApiResponse<Void> confirmBooking(@PathVariable Long bookingId,
                                            @RequestHeader("Authorization") String token) {
        return examBookingService.confirmBooking(bookingId);
    }

    @GetMapping("/bookings/user")
    public ApiResponse<List<BookingDetailsDTO>> getUserBookings(@RequestHeader("Authorization") String token,
                                                                @RequestParam(required = false) String status) {
        Long userId = jwtUtil.extractUserId(token);
        return examBookingService.getUserBookings(userId, status);
    }

    @GetMapping("/bookings/{bookingId}")
    public ApiResponse<BookingDetailsDTO> getBookingDetails(@PathVariable("bookingId") Long bookingId) {
        return examBookingService.getBookingDetails(bookingId);
    }

    @GetMapping("/bookings/by-user-exam")
    public ApiResponse<Long> getBookingIdByUserAndExam(
            @RequestParam("examId") Long examId,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token);
        return examBookingService.getBookingIdByUserAndExam(userId, examId);
    }

    @GetMapping("/bookings/by-number/{bookingNumber}")
    public ApiResponse<BookingDetailsDTO> getBookingDetailsByNumber(@PathVariable String bookingNumber) {
        return examBookingService.getBookingDetailsByNumber(bookingNumber);
    }

    @PostMapping("/bookings/{bookingId}/check-in")
    public ApiResponse<Void> checkIn(@PathVariable Long bookingId,
                                     @Valid @RequestBody CheckInDTO checkInDTO) {
        return examBookingService.checkIn(bookingId, checkInDTO.getCheckInStatus());
    }

    // ============================== 统计查询 ==============================

    @GetMapping("/stats/time-slot/{timeSlotId}")
    public ApiResponse<Map<String, Object>> getTimeSlotStats(@PathVariable Long timeSlotId) {
        return examBookingService.getTimeSlotStats(timeSlotId);
    }

    @GetMapping("/stats/user")
    public ApiResponse<Map<String, Object>> getUserBookingStats(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token);
        return examBookingService.getUserBookingStats(userId);
    }

    // ============================== 通知管理 ==============================

    @GetMapping("/notifications")
    public ApiResponse<List<ExamNotification>> getUserNotifications(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token);
        return examBookingService.getUserNotifications(userId);
    }

    @GetMapping("/notifications/unread")
    public ApiResponse<List<ExamNotification>> getUnreadNotifications(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token);
        return examBookingService.getUnreadNotifications(userId);
    }

    @PostMapping("/notifications/{notificationId}/read")
    public ApiResponse<Void> markNotificationAsRead(@PathVariable("notificationId") Long notificationId) {
        return examBookingService.markNotificationAsRead(notificationId);
    }

    @PostMapping("/notifications/batch-read")
    public ApiResponse<Void> batchMarkAsRead(@RequestBody List<Long> notificationIds,
                                             @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token);
        return examBookingService.batchMarkAsRead(userId, notificationIds);
    }

    @PostMapping("/notifications/exam-reminder/{examId}")
    public ApiResponse<Void> sendExamReminder(@PathVariable Long examId) {
        return examBookingService.sendExamReminder(examId);
    }

    // ============================== 管理功能 ==============================

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
            @RequestParam(defaultValue = "20") int pageSize) {
        return examBookingService.getBookings(status, startDate, endDate, pageNum, pageSize);
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> getBookingStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return examBookingService.getBookingStats(startDate, endDate);
    }

    @GetMapping("/{examId}/time-slots")
    public ApiResponse<List<ExamTimeSlot>> getTimeSlots(
            @PathVariable("examId") Long examId) {
        return examBookingService.getTimeSlots(examId);
    }

    @PostMapping("/time-slots/{timeSlotId}/toggle-status")
    public ApiResponse<Void> toggleTimeSlotStatus(
            @PathVariable("timeSlotId") Long timeSlotId) {
        return examBookingService.toggleTimeSlotStatus(timeSlotId);
    }
}