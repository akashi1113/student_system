package com.csu.sms.model.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    STUDENT(0, "学生"),
    TEACHER(1, "教师"),
    ADMIN(2, "管理员");

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
        return STUDENT; // 默认为学生
    }
}
