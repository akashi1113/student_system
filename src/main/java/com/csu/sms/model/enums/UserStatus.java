package com.csu.sms.model.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE(0, "正常"),
    DISABLED(1, "禁用");

    private final int code;
    private final String description;

    UserStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UserStatus fromCode(int code) {
        for (UserStatus status : UserStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return ACTIVE; // 默认为正常状态
    }
}
