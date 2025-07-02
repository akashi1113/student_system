package com.csu.sms.persistence;

import com.csu.sms.common.PageResult;
import com.csu.sms.model.booking.ExamNotification;
import com.csu.sms.model.booking.ExamTimeSlot;
import com.csu.sms.model.booking.ExamBooking;
import com.csu.sms.dto.booking.BookingDetailsDTO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ExamBookingMapper {

    // ============================== 考试时间段相关 ==============================

    /**
     * 查询考试的所有时间段
     */
    List<ExamTimeSlot> findTimeSlotsByExamId(Long examId);

    /**
     * 查询用户已预约的考试ID列表
     */
    List<Long> findBookedExamIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询可预约的时间段
     */
    List<ExamTimeSlot> findAvailableTimeSlots(Long examId);

    /**
     * 根据ID查询时间段
     */
    ExamTimeSlot findTimeSlotById(Long id);

    /**
     * 查询指定日期的时间段
     */
    List<ExamTimeSlot> findTimeSlotsByDate(@Param("date") LocalDate date,
                                           @Param("examMode") String examMode);

    /**
     * 插入时间段
     */
    int insertTimeSlot(ExamTimeSlot timeSlot);

    /**
     * 批量插入时间段
     */
    int batchInsertTimeSlots(@Param("timeSlots") List<ExamTimeSlot> timeSlots);

    /**
     * 更新时间段
     */
    int updateTimeSlot(ExamTimeSlot timeSlot);

    /**
     * 更新时间段预约人数
     */
    int updateTimeSlotBookingCount(@Param("timeSlotId") Long timeSlotId,
                                   @Param("count") Integer count);

    /**
     * 删除时间段
     */
    int deleteTimeSlot(Long id);

    // ============================== 考试预约相关 ==============================

    /**
     * 查询用户在指定考试的预约
     */
    ExamBooking findBookingByUserAndExam(@Param("userId") Long userId,
                                         @Param("examId") Long examId);

    /**
     * 根据ID查询预约
     */
    ExamBooking findBookingById(Long id);

    /**
     * 根据预约号查询预约
     */
    ExamBooking findBookingByNumber(String bookingNumber);

    /**
     * 查询用户的所有预约
     */
    List<ExamBooking> findBookingsByUserId(Long userId);

    /**
     * 查询时间段的所有预约
     */
    List<ExamBooking> findBookingsByTimeSlotId(Long timeSlotId);

    /**
     * 查询用户在指定时间段的预约
     */
    ExamBooking findBookingByUserAndTimeSlot(@Param("timeSlotId") Long timeSlotId,
                                             @Param("userId") Long userId);

    /**
     * 查询预约详情
     */
    BookingDetailsDTO findBookingDetails(Long bookingId);

    /**
     * 查询用户的预约详情列表
     */
    List<BookingDetailsDTO> findUserBookingDetails(@Param("userId") Long userId,
                                                   @Param("status") String status);

    /**
     * 插入预约记录
     */
    int insertBooking(ExamBooking booking);

    /**
     * 更新预约状态
     */
    int updateBookingStatus(@Param("bookingId") Long bookingId,
                            @Param("status") String status,
                            @Param("cancelReason") String cancelReason,
                            @Param("cancelledBy") Long cancelledBy);

    /**
     * 更新预约信息
     */
    int updateBooking(ExamBooking booking);

    /**
     * 签到
     */
    int checkIn(@Param("bookingId") Long bookingId,
                @Param("checkInStatus") String checkInStatus);

    /**
     * 删除预约
     */
    int deleteBooking(Long id);

    // ============================== 通知相关 ==============================

    /**
     * 查询用户通知列表
     */
    List<ExamNotification> findNotificationsByUserId(Long userId);

    /**
     * 查询未读通知
     */
    List<ExamNotification> findUnreadNotifications(Long userId);

    /**
     * 查询待发送通知
     */
    List<ExamNotification> findPendingNotifications();

    /**
     * 插入通知
     */
    int insertNotification(ExamNotification notification);

    /**
     * 批量插入通知
     */
    int batchInsertNotifications(@Param("notifications") List<ExamNotification> notifications);

    /**
     * 更新通知状态
     */
    int updateNotificationStatus(@Param("notificationId") Long notificationId,
                                 @Param("status") String status);

    /**
     * 标记通知为已读
     */
    int markNotificationAsRead(Long notificationId);

    /**
     * 批量标记为已读
     */
    int batchMarkAsRead(@Param("userId") Long userId,
                        @Param("notificationIds") List<Long> notificationIds);

    /**
     * 删除通知
     */
    int deleteNotification(Long id);

    /**
     * 删除过期通知
     */
    int deleteExpiredNotifications(@Param("days") Integer days);

    // ============================== 统计查询 ==============================

    /**
     * 统计时间段预约情况
     */
    Map<String, Object> getTimeSlotBookingStats(Long timeSlotId);

    /**
     * 统计用户预约情况
     */
    Map<String, Object> getUserBookingStats(Long userId);

    /**
     * 预约考试（调用存储过程）
     */
    Map<String, Object> bookExam(@Param("timeSlotId") Long timeSlotId,
                                 @Param("userId") Long userId,
                                 @Param("contactPhone") String contactPhone,
                                 @Param("contactEmail") String contactEmail,
                                 @Param("specialRequirements") String specialRequirements,
                                 @Param("bookingId") Long bookingId,
                                 @Param("bookingNumber") String bookingNumber,
                                 @Param("resultCode") Integer resultCode,
                                 @Param("resultMessage") String resultMessage);

    /**
     * 取消预约（调用存储过程）
     */
    Map<String, Object> cancelBooking(@Param("bookingId") Long bookingId,
                                      @Param("userId") Long userId,
                                      @Param("cancelReason") String cancelReason,
                                      @Param("resultCode") Integer resultCode,
                                      @Param("resultMessage") String resultMessage);

    List<BookingDetailsDTO> getBookings(
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    Long countBookings(@Param("status") String status,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate);

    // 切换时间段状态
    int toggleTimeSlotStatus(@Param("timeSlotId") Long timeSlotId);

    List<ExamTimeSlot> findAvailableTimeSlotsByExamIds(@Param("examIds") List<Long> examIds);

}