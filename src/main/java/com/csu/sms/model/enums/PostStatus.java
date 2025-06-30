package com.csu.sms.model.enums;

import lombok.Getter;

@Getter
public enum PostStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    DELETED(2, "已删除"),
    PENDING_REVIEW(3, "待审核");

    private final int code;
    private final String description;

    PostStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

}


