package com.csu.sms.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private Long postId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String content;
    private Long parentId;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean isLiked;
    private Integer replyCount;
}
