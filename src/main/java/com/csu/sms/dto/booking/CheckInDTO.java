package com.csu.sms.dto.booking;


import jakarta.validation.constraints.NotBlank;

public class CheckInDTO {

    @NotBlank(message = "签到状态不能为空")
    private String checkInStatus; // CHECKED_IN, LATE, ABSENT

    public CheckInDTO() {}

    // Getters and Setters
    public String getCheckInStatus() { return checkInStatus; }
    public void setCheckInStatus(String checkInStatus) { this.checkInStatus = checkInStatus; }
}