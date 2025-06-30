package com.csu.sms.model.user;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserNotification {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer type;  // 通知类型：1-系统通知 2-帖子通知 3-评论通知
    private Integer status; // 0-未读 1-已读
    private Long relatedId; // 关联ID，如帖子ID
    private LocalDateTime createTime;
}
