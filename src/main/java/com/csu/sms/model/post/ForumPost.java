package com.csu.sms.model.post;

import com.csu.sms.model.enums.PostStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumPost {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String category;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private PostStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
