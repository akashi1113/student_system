package com.csu.sms.model.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    USER(0, "普通用户"),
    ADMIN(1, "管理员");

    private final int code;
    private final String description;

    UserRole(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UserRole fromCode(int code) {
        for (UserRole role : UserRole.values()) {
            if (role.getCode() == code) {
                return role;
            }
        }
        return USER; // 默认为普通用户
    }
}
