package com.csu.sms.dto.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class BookingDetailsDTO {

    // 预约信息
    private Long bookingId;
    private String bookingNumber;
    private String bookingStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookingTime;
    private String checkInStatus;

    // 时间段信息
    private Long timeSlotId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate slotDate;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
    private String examLocation;
    private String examMode;
    private Integer maxCapacity;
    private Integer currentBookings;

    // 考试信息
    private Long examId;
    private String examTitle;
    private String examDescription;
    private Integer examDuration;

    // 用户信息
    private Long userId;
    private String username;
    private String studentName;
    private String email;
    private String phone;

    // 联系信息
    private String contactPhone;
    private String contactEmail;
    private String specialRequirements;
    private String remarks;

    // 扩展信息
    private Integer availableSlots; // 剩余名额
    private Boolean canCancel; // 是否可以取消
    private LocalDateTime cancelDeadline; // 取消截止时间
    private String examStatusText; // 考试状态文本描述

    public BookingDetailsDTO() {}

    // Getters and Setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public String getCheckInStatus() { return checkInStatus; }
    public void setCheckInStatus(String checkInStatus) { this.checkInStatus = checkInStatus; }

    public Long getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(Long timeSlotId) { this.timeSlotId = timeSlotId; }

    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getExamLocation() { return examLocation; }
    public void setExamLocation(String examLocation) { this.examLocation = examLocation; }

    public String getExamMode() { return examMode; }
    public void setExamMode(String examMode) { this.examMode = examMode; }

    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }

    public Integer getCurrentBookings() { return currentBookings; }
    public void setCurrentBookings(Integer currentBookings) { this.currentBookings = currentBookings; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public String getExamDescription() { return examDescription; }
    public void setExamDescription(String examDescription) { this.examDescription = examDescription; }

    public Integer getExamDuration() { return examDuration; }
    public void setExamDuration(Integer examDuration) { this.examDuration = examDuration; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getSpecialRequirements() { return specialRequirements; }
    public void setSpecialRequirements(String specialRequirements) { this.specialRequirements = specialRequirements; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Integer getAvailableSlots() {
        if (maxCapacity != null && currentBookings != null) {
            return maxCapacity - currentBookings;
        }
        return availableSlots;
    }
    public void setAvailableSlots(Integer availableSlots) { this.availableSlots = availableSlots; }

    public Boolean getCanCancel() { return canCancel; }
    public void setCanCancel(Boolean canCancel) { this.canCancel = canCancel; }

    public LocalDateTime getCancelDeadline() { return cancelDeadline; }
    public void setCancelDeadline(LocalDateTime cancelDeadline) { this.cancelDeadline = cancelDeadline; }

    public String getExamStatusText() { return examStatusText; }
    public void setExamStatusText(String examStatusText) { this.examStatusText = examStatusText; }
}