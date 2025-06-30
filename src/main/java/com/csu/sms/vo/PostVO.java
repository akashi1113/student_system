package com.csu.sms.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostVO {
    private Long id;
    private String title;
    private String content;
    private String category;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createTime;

    private Long userId;
    private String userName;
    private String userAvatar;  // 发帖人头像
    private Integer status;  // 帖子状态
    private String statusDesc;  // 状态描述
    private Boolean isLiked;  // 当前用户是否已点赞
}

