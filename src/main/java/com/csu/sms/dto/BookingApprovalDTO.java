package com.csu.sms.dto;

import lombok.Data;

@Data
public class BookingApprovalDTO {
    private Long bookingId;
    private Integer status; // 1-待审批, 2-通过, 3-拒绝
}
