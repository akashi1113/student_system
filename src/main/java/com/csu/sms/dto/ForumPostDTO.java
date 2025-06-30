package com.csu.sms.dto;

import com.csu.sms.model.enums.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class ForumPostDTO {
    private Long id;
    private Long userId;

    @NotBlank(message = "帖子标题不能为空")
    @Size(min = 5, max = 100, message = "标题长度需在5到100字符之间")
    private String title;

    @NotBlank(message = "帖子内容不能为空")
    @Size(min = 10, message = "内容至少需要10个字符")
    private String content;

    @NotBlank(message = "帖子分类不能为空")
    private String category;

    private PostStatus status = PostStatus.PUBLISHED; // 默认为已发布状态
}

