package com.csu.sms.model.post;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumComment {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    //父评论ID
    private Long parentId;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Data
    public static class CommentLike {
        private Long id;
        private Long commentId;
        private Long userId;
        private LocalDateTime createTime;
    }
}
