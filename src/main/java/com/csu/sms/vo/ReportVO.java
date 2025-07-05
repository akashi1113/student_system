package com.csu.sms.vo;

import com.csu.sms.model.enums.ReportStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportVO {
    private Long id;
    private Long postId;
    private String reason;
    private ReportStatus status;  // 举报状态：0-待处理，1-已处理, 2-已驳回
    private String postTitle;
    private String reporterName;
    private LocalDateTime reportTime;
}
