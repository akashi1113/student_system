package com.csu.sms.dto.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class TimeSlotCreateDTO {

    @NotNull(message = "考试ID不能为空")
    private Long examId;

    @NotNull(message = "考试日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate slotDate;

    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotBlank(message = "考试地点不能为空")
    private String examLocation;

    @NotBlank(message = "考试模式不能为空")
    private String examMode; // ONLINE, OFFLINE, HYBRID

    @NotNull(message = "最大容量不能为空")
    @Min(value = 1, message = "最大容量不能小于1")
    @Max(value = 1000, message = "最大容量不能超过1000")
    private Integer maxCapacity;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookingStartTime;

    @NotNull(message = "预约截止时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookingEndTime;

    private Boolean allowCancel = true;

    @Min(value = 1, message = "取消截止时间不能小于1小时")
    @Max(value = 168, message = "取消截止时间不能超过168小时")
    private Integer cancelDeadlineHours = 24;

    private String requirements;
    private String equipmentNeeded;
    private Long createdBy;

    public TimeSlotCreateDTO() {}

    // Getters and Setters
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

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

    public LocalDateTime getBookingStartTime() { return bookingStartTime; }
    public void setBookingStartTime(LocalDateTime bookingStartTime) { this.bookingStartTime = bookingStartTime; }

    public LocalDateTime getBookingEndTime() { return bookingEndTime; }
    public void setBookingEndTime(LocalDateTime bookingEndTime) { this.bookingEndTime = bookingEndTime; }

    public Boolean getAllowCancel() { return allowCancel; }
    public void setAllowCancel(Boolean allowCancel) { this.allowCancel = allowCancel; }

    public Integer getCancelDeadlineHours() { return cancelDeadlineHours; }
    public void setCancelDeadlineHours(Integer cancelDeadlineHours) { this.cancelDeadlineHours = cancelDeadlineHours; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getEquipmentNeeded() { return equipmentNeeded; }
    public void setEquipmentNeeded(String equipmentNeeded) { this.equipmentNeeded = equipmentNeeded; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}