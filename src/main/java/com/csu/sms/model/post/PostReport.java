package com.csu.sms.model.post;

import com.csu.sms.model.enums.ReportStatus;
import lombok.Data;
import java.time.LocalDateTime;

//举报记录
@Data
public class PostReport {
    private Long id;
    private Long postId;
    private Long reporterId;
    private String reason;
    private String description;
    private ReportStatus status; // PENDING, PROCESSED, REJECTED
    private Long handlerId; // 处理的管理员ID
    private LocalDateTime createTime;
    private LocalDateTime handleTime;
}
