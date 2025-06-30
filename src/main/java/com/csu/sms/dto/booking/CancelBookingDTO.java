package com.csu.sms.dto.booking;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CancelBookingDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "取消原因不能为空")
    private String cancelReason;

    public CancelBookingDTO() {}

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
}