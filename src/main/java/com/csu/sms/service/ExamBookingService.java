package com.csu.sms.service;

import com.csu.sms.persistence.ExamBookingMapper;
import com.csu.sms.persistence.ExamMapper;
import com.csu.sms.model.booking.ExamTimeSlot;
import com.csu.sms.model.booking.ExamBooking;
import com.csu.sms.model.booking.ExamNotification;
import com.csu.sms.model.exam.Exam;
import com.csu.sms.dto.booking.BookingDetailsDTO;
import com.csu.sms.dto.booking.BookingRequestDTO;
import com.csu.sms.dto.booking.TimeSlotCreateDTO;
import com.csu.sms.common.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ExamBookingService {

    @Autowired
    private ExamBookingMapper examBookingMapper;

    @Autowired
    private ExamMapper examMapper;

    public ApiResponse<List<ExamTimeSlot>> getAvailableTimeSlots(Long examId) {
        try {
            // 验证考试是否存在
            Exam exam = examMapper.findById(examId);
            if (exam == null) {
                return ApiResponse.error("考试不存在");
            }

            List<ExamTimeSlot> timeSlots = examBookingMapper.findAvailableTimeSlots(examId);

            // 计算剩余名额
            timeSlots.forEach(slot -> {
                slot.setAvailableSlots(slot.getMaxCapacity() - slot.getCurrentBookings());
                slot.setExamTitle(exam.getTitle());
                slot.setExamDuration(exam.getDuration());
            });

            return ApiResponse.success(timeSlots);
        } catch (Exception e) {
            return ApiResponse.error("查询时间段失败：" + e.getMessage());
        }
    }

    public ApiResponse<List<ExamTimeSlot>> getTimeSlotsByDate(LocalDate date, String examMode) {
        try {
            List<ExamTimeSlot> timeSlots = examBookingMapper.findTimeSlotsByDate(date, examMode);
            return ApiResponse.success(timeSlots);
        } catch (Exception e) {
            return ApiResponse.error("查询时间段失败：" + e.getMessage());
        }
    }

    public ApiResponse<ExamTimeSlot> createTimeSlot(TimeSlotCreateDTO createDTO) {
        try {
            // 验证考试是否存在
            Exam exam = examMapper.findById(createDTO.getExamId());
            if (exam == null) {
                return ApiResponse.error("考试不存在");
            }

            // 验证时间有效性
            if (createDTO.getStartTime().isAfter(createDTO.getEndTime())) {
                return ApiResponse.error("开始时间不能晚于结束时间");
            }

            if (createDTO.getBookingEndTime().isAfter(
                    LocalDateTime.of(createDTO.getSlotDate(), createDTO.getStartTime()))) {
                return ApiResponse.error("预约截止时间不能晚于考试开始时间");
            }

            ExamTimeSlot timeSlot = new ExamTimeSlot();
            timeSlot.setExamId(createDTO.getExamId());
            timeSlot.setSlotDate(createDTO.getSlotDate());
            timeSlot.setStartTime(createDTO.getStartTime());
            timeSlot.setEndTime(createDTO.getEndTime());
            timeSlot.setExamLocation(createDTO.getExamLocation());
            timeSlot.setExamMode(createDTO.getExamMode());
            timeSlot.setMaxCapacity(createDTO.getMaxCapacity());
            timeSlot.setBookingStartTime(createDTO.getBookingStartTime());
            timeSlot.setBookingEndTime(createDTO.getBookingEndTime());
            timeSlot.setAllowCancel(createDTO.getAllowCancel());
            timeSlot.setCancelDeadlineHours(createDTO.getCancelDeadlineHours());
            timeSlot.setRequirements(createDTO.getRequirements());
            timeSlot.setEquipmentNeeded(createDTO.getEquipmentNeeded());
            timeSlot.setCreatedBy(createDTO.getCreatedBy());
            timeSlot.setStatus("AVAILABLE");
            timeSlot.setCurrentBookings(0);

            int rows = examBookingMapper.insertTimeSlot(timeSlot);
            if (rows > 0) {
                return ApiResponse.success("时间段创建成功", timeSlot);
            } else {
                return ApiResponse.error("创建时间段失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("创建时间段失败：" + e.getMessage());
        }
    }

    public ApiResponse<List<ExamTimeSlot>> batchCreateTimeSlots(List<TimeSlotCreateDTO> createDTOs) {
        try {
            // 验证所有考试是否存在
            for (TimeSlotCreateDTO dto : createDTOs) {
                Exam exam = examMapper.findById(dto.getExamId());
                if (exam == null) {
                    return ApiResponse.error("考试ID " + dto.getExamId() + " 不存在");
                }
            }

            List<ExamTimeSlot> timeSlots = createDTOs.stream().map(dto -> {
                ExamTimeSlot timeSlot = new ExamTimeSlot();
                timeSlot.setExamId(dto.getExamId());
                timeSlot.setSlotDate(dto.getSlotDate());
                timeSlot.setStartTime(dto.getStartTime());
                timeSlot.setEndTime(dto.getEndTime());
                timeSlot.setExamLocation(dto.getExamLocation());
                timeSlot.setExamMode(dto.getExamMode());
                timeSlot.setMaxCapacity(dto.getMaxCapacity());
                timeSlot.setBookingStartTime(dto.getBookingStartTime());
                timeSlot.setBookingEndTime(dto.getBookingEndTime());
                timeSlot.setAllowCancel(dto.getAllowCancel());
                timeSlot.setCancelDeadlineHours(dto.getCancelDeadlineHours());
                timeSlot.setRequirements(dto.getRequirements());
                timeSlot.setEquipmentNeeded(dto.getEquipmentNeeded());
                timeSlot.setCreatedBy(dto.getCreatedBy());
                timeSlot.setStatus("AVAILABLE");
                timeSlot.setCurrentBookings(0);
                return timeSlot;
            }).toList();

            int rows = examBookingMapper.batchInsertTimeSlots(timeSlots);
            if (rows > 0) {
                return ApiResponse.success("批量创建成功，共创建 " + rows + " 个时间段", timeSlots);
            } else {
                return ApiResponse.error("批量创建时间段失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("批量创建时间段失败：" + e.getMessage());
        }
    }

    public ApiResponse<ExamTimeSlot> updateTimeSlot(Long timeSlotId, TimeSlotCreateDTO updateDTO) {
        try {
            ExamTimeSlot existingSlot = examBookingMapper.findTimeSlotById(timeSlotId);
            if (existingSlot == null) {
                return ApiResponse.error("时间段不存在");
            }

            // 检查是否有预约，如果有预约则限制修改
            if (existingSlot.getCurrentBookings() > 0) {
                return ApiResponse.error("该时间段已有预约，不能修改");
            }

            ExamTimeSlot timeSlot = new ExamTimeSlot();
            timeSlot.setId(timeSlotId);
            timeSlot.setSlotDate(updateDTO.getSlotDate());
            timeSlot.setStartTime(updateDTO.getStartTime());
            timeSlot.setEndTime(updateDTO.getEndTime());
            timeSlot.setExamLocation(updateDTO.getExamLocation());
            timeSlot.setExamMode(updateDTO.getExamMode());
            timeSlot.setMaxCapacity(updateDTO.getMaxCapacity());
            timeSlot.setBookingStartTime(updateDTO.getBookingStartTime());
            timeSlot.setBookingEndTime(updateDTO.getBookingEndTime());
            timeSlot.setAllowCancel(updateDTO.getAllowCancel());
            timeSlot.setCancelDeadlineHours(updateDTO.getCancelDeadlineHours());
            timeSlot.setRequirements(updateDTO.getRequirements());
            timeSlot.setEquipmentNeeded(updateDTO.getEquipmentNeeded());

            int rows = examBookingMapper.updateTimeSlot(timeSlot);
            if (rows > 0) {
                ExamTimeSlot updatedSlot = examBookingMapper.findTimeSlotById(timeSlotId);
                return ApiResponse.success("时间段更新成功", updatedSlot);
            } else {
                return ApiResponse.error("更新时间段失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("更新时间段失败：" + e.getMessage());
        }
    }

    public ApiResponse<Void> deleteTimeSlot(Long timeSlotId) {
        try {
            ExamTimeSlot timeSlot = examBookingMapper.findTimeSlotById(timeSlotId);
            if (timeSlot == null) {
                return ApiResponse.error("时间段不存在");
            }

            if (timeSlot.getCurrentBookings() > 0) {
                return ApiResponse.error("该时间段已有预约，不能删除");
            }

            int rows = examBookingMapper.deleteTimeSlot(timeSlotId);
            if (rows > 0) {
                return ApiResponse.success("时间段删除成功", null);
            } else {
                return ApiResponse.error("删除时间段失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("删除时间段失败：" + e.getMessage());
        }
    }

    public ApiResponse<ExamBooking> bookExam(BookingRequestDTO bookingRequest) {
        try {
            // 验证时间段是否存在且可预约
            ExamTimeSlot timeSlot = examBookingMapper.findTimeSlotById(bookingRequest.getTimeSlotId());
            if (timeSlot == null) {
                return ApiResponse.error("考试时间段不存在");
            }

            if (!"AVAILABLE".equals(timeSlot.getStatus())) {
                return ApiResponse.error("考试时间段不可预约");
            }

            if (timeSlot.getCurrentBookings() >= timeSlot.getMaxCapacity()) {
                return ApiResponse.error("考试时间段已满");
            }

            if (timeSlot.getBookingEndTime().isBefore(LocalDateTime.now())) {
                return ApiResponse.error("预约时间已截止");
            }

            // 检查用户是否已预约该时间段
            ExamBooking existingBooking = examBookingMapper.findBookingByUserAndTimeSlot(
                    bookingRequest.getTimeSlotId(), bookingRequest.getUserId());
            if (existingBooking != null) {
                return ApiResponse.error("您已预约该考试时间段");
            }

            // 生成预约号
            String bookingNumber = generateBookingNumber(bookingRequest.getTimeSlotId(), bookingRequest.getUserId());

            // 创建预约记录
            ExamBooking booking = new ExamBooking();
            booking.setTimeSlotId(bookingRequest.getTimeSlotId());
            booking.setUserId(bookingRequest.getUserId());
            booking.setBookingNumber(bookingNumber);
            booking.setContactPhone(bookingRequest.getContactPhone());
            booking.setContactEmail(bookingRequest.getContactEmail());
            booking.setSpecialRequirements(bookingRequest.getSpecialRequirements());
            booking.setRemarks(bookingRequest.getRemarks());
            booking.setStatus("BOOKED");
            booking.setCheckInStatus("NOT_CHECKED");

            int rows = examBookingMapper.insertBooking(booking);
            if (rows > 0) {
                // 更新时间段预约人数
                examBookingMapper.updateTimeSlotBookingCount(
                        bookingRequest.getTimeSlotId(), timeSlot.getCurrentBookings() + 1);

                // 创建预约成功通知
                createBookingNotification(booking.getId(), bookingRequest.getUserId(),
                        "BOOKING_CONFIRMED", "考试预约成功",
                        String.format("您已成功预约考试，预约号：%s", bookingNumber));

                return ApiResponse.success("预约成功", booking);
            } else {
                return ApiResponse.error("预约失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("预约失败：" + e.getMessage());
        }
    }

    public ApiResponse<Void> cancelBooking(Long bookingId, Long userId, String cancelReason) {
        try {
            ExamBooking booking = examBookingMapper.findBookingById(bookingId);
            if (booking == null) {
                return ApiResponse.error("预约记录不存在");
            }

            if (!booking.getUserId().equals(userId)) {
                return ApiResponse.error("无权取消该预约");
            }

            if ("CANCELLED".equals(booking.getStatus())) {
                return ApiResponse.error("预约已取消");
            }

            // 检查是否允许取消
            ExamTimeSlot timeSlot = examBookingMapper.findTimeSlotById(booking.getTimeSlotId());
            if (!timeSlot.getAllowCancel()) {
                return ApiResponse.error("该考试不允许取消预约");
            }

            // 检查取消截止时间
            LocalDateTime examDateTime = LocalDateTime.of(timeSlot.getSlotDate(), timeSlot.getStartTime());
            LocalDateTime cancelDeadline = examDateTime.minusHours(timeSlot.getCancelDeadlineHours());
            if (LocalDateTime.now().isAfter(cancelDeadline)) {
                return ApiResponse.error("已超过取消截止时间");
            }

            // 更新预约状态
            int rows = examBookingMapper.updateBookingStatus(bookingId, "CANCELLED", cancelReason, userId);
            if (rows > 0) {
                // 更新时间段预约人数
                examBookingMapper.updateTimeSlotBookingCount(
                        booking.getTimeSlotId(), timeSlot.getCurrentBookings() - 1);

                // 创建取消通知
                createBookingNotification(bookingId, userId,
                        "BOOKING_CANCELLED", "考试预约已取消",
                        String.format("您的考试预约（预约号：%s）已取消", booking.getBookingNumber()));

                return ApiResponse.success("取消预约成功", null);
            } else {
                return ApiResponse.error("取消预约失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("取消预约失败：" + e.getMessage());
        }
    }

    public ApiResponse<Void> confirmBooking(Long bookingId) {
        try {
            ExamBooking booking = examBookingMapper.findBookingById(bookingId);
            if (booking == null) {
                return ApiResponse.error("预约记录不存在");
            }

            if (!"BOOKED".equals(booking.getStatus())) {
                return ApiResponse.error("预约状态不正确");
            }

            int rows = examBookingMapper.updateBookingStatus(bookingId, "CONFIRMED", null, null);
            if (rows > 0) {
                // 创建确认通知
                createBookingNotification(bookingId, booking.getUserId(),
                        "BOOKING_CONFIRMED", "考试预约已确认",
                        String.format("您的考试预约（预约号：%s）已确认", booking.getBookingNumber()));

                return ApiResponse.success("预约确认成功", null);
            } else {
                return ApiResponse.error("确认预约失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("确认预约失败：" + e.getMessage());
        }
    }

    public ApiResponse<List<BookingDetailsDTO>> getUserBookings(Long userId, String status) {
        try {
            List<BookingDetailsDTO> bookings = examBookingMapper.findUserBookingDetails(userId, status);
            return ApiResponse.success(bookings);
        } catch (Exception e) {
            return ApiResponse.error("查询预约列表失败：" + e.getMessage());
        }
    }

    public ApiResponse<Long> getBookingIdByUserAndExam(Long userId, Long examId) {
        try {
            ExamBooking booking = examBookingMapper.findBookingByUserAndExam(userId, examId);
            if (booking == null) {
                return ApiResponse.error(404, "未找到预约记录");
            }
            return ApiResponse.success(booking.getId());
        } catch (Exception e) {
            return ApiResponse.error(500, "获取预约ID失败");
        }
    }

    public ApiResponse<BookingDetailsDTO> getBookingDetails(Long bookingId) {
        try {
            BookingDetailsDTO details = examBookingMapper.findBookingDetails(bookingId);
            if (details == null) {
                return ApiResponse.error("预约记录不存在");
            }
            return ApiResponse.success(details);
        } catch (Exception e) {
            return ApiResponse.error("查询预约详情失败：" + e.getMessage());
        }
    }

    public ApiResponse<BookingDetailsDTO> getBookingDetailsByNumber(String bookingNumber) {
        try {
            ExamBooking booking = examBookingMapper.findBookingByNumber(bookingNumber);
            if (booking == null) {
                return ApiResponse.error("预约记录不存在");
            }
            BookingDetailsDTO details = examBookingMapper.findBookingDetails(booking.getId());
            return ApiResponse.success(details);
        } catch (Exception e) {
            return ApiResponse.error("查询预约详情失败：" + e.getMessage());
        }
    }

    public ApiResponse<Void> checkIn(Long bookingId, String checkInStatus) {
        try {
            ExamBooking booking = examBookingMapper.findBookingById(bookingId);
            if (booking == null) {
                return ApiResponse.error("预约记录不存在");
            }

            if (!"CONFIRMED".equals(booking.getStatus()) && !"BOOKED".equals(booking.getStatus())) {
                return ApiResponse.error("预约状态不允许签到");
            }

            int rows = examBookingMapper.checkIn(bookingId, checkInStatus);
            if (rows > 0) {
                return ApiResponse.success("签到成功", null);
            } else {
                return ApiResponse.error("签到失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("签到失败：" + e.getMessage());
        }
    }

    public ApiResponse<Map<String, Object>> getTimeSlotStats(Long timeSlotId) {
        try {
            Map<String, Object> stats = examBookingMapper.getTimeSlotBookingStats(timeSlotId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error("查询统计失败：" + e.getMessage());
        }
    }

    public ApiResponse<Map<String, Object>> getUserBookingStats(Long userId) {
        try {
            Map<String, Object> stats = examBookingMapper.getUserBookingStats(userId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error("查询统计失败：" + e.getMessage());
        }
    }

    public ApiResponse<List<ExamNotification>> getUserNotifications(Long userId) {
        try {
            List<ExamNotification> notifications = examBookingMapper.findNotificationsByUserId(userId);
            return ApiResponse.success(notifications);
        } catch (Exception e) {
            return ApiResponse.error("查询通知列表失败：" + e.getMessage());
        }
    }

    public ApiResponse<List<ExamNotification>> getUnreadNotifications(Long userId) {
        try {
            List<ExamNotification> notifications = examBookingMapper.findUnreadNotifications(userId);
            return ApiResponse.success(notifications);
        } catch (Exception e) {
            return ApiResponse.error("查询未读通知失败：" + e.getMessage());
        }
    }

    public ApiResponse<Void> markNotificationAsRead(Long notificationId) {
        try {
            int rows = examBookingMapper.markNotificationAsRead(notificationId);
            if (rows > 0) {
                return ApiResponse.success("标记成功", null);
            } else {
                return ApiResponse.error("标记失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("标记失败：" + e.getMessage());
        }
    }

    public ApiResponse<Void> batchMarkAsRead(Long userId, List<Long> notificationIds) {
        try {
            int rows = examBookingMapper.batchMarkAsRead(userId, notificationIds);
            if (rows > 0) {
                return ApiResponse.success("批量标记成功", null);
            } else {
                return ApiResponse.error("批量标记失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("批量标记失败：" + e.getMessage());
        }
    }

    public ApiResponse<Void> sendExamReminder(Long examId) {
        try {
            // 查询明天有考试的所有预约
            List<ExamTimeSlot> timeSlots = examBookingMapper.findTimeSlotsByExamId(examId);

            for (ExamTimeSlot timeSlot : timeSlots) {
                // 查询该时间段的所有预约
                List<ExamBooking> bookings = examBookingMapper.findBookingsByTimeSlotId(timeSlot.getId());

                for (ExamBooking booking : bookings) {
                    if ("BOOKED".equals(booking.getStatus()) || "CONFIRMED".equals(booking.getStatus())) {
                        createBookingNotification(
                                booking.getId(),
                                booking.getUserId(),
                                "EXAM_REMINDER",
                                "考试提醒",
                                String.format("您有一场考试将于 %s %s 举行，请准时参加。地点：%s",
                                        timeSlot.getSlotDate(), timeSlot.getStartTime(), timeSlot.getExamLocation())
                        );
                    }
                }
            }

            return ApiResponse.success("考试提醒发送成功", null);
        } catch (Exception e) {
            return ApiResponse.error("发送考试提醒失败：" + e.getMessage());
        }
    }

    public ApiResponse<Void> handleExpiredBookings() {
        try {
            // 查询所有过期的预约（考试时间已过但状态仍为BOOKED或CONFIRMED）
            // 这里需要添加相应的查询方法

            // 更新过期预约状态
            // examBookingMapper.updateExpiredBookings();

            return ApiResponse.success("过期预约处理完成", null);
        } catch (Exception e) {
            return ApiResponse.error("处理过期预约失败：" + e.getMessage());
        }
    }

    // 私有辅助方法
    private String generateBookingNumber(Long timeSlotId, Long userId) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("BK%s%04d%06d", date, timeSlotId % 10000, userId % 1000000);
    }

    private void createBookingNotification(Long bookingId, Long userId, String type, String title, String content) {
        try {
            ExamNotification notification = new ExamNotification();
            notification.setBookingId(bookingId);
            notification.setUserId(userId);
            notification.setNotificationType(type);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setSendMethod("SYSTEM");
            notification.setPriority("NORMAL");

            examBookingMapper.insertNotification(notification);
        } catch (Exception e) {
            // 记录日志，但不影响主流程
            System.err.println("创建通知失败：" + e.getMessage());
        }
    }
}