package com.csu.sms.model.user;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String avatar;
    private int status; // 状态 0-正常 1-禁用
    private int role;   // 角色 0-普通用户 1-管理员 2-老师
    private int tokenVersion = 0; // 新增：令牌版本，用于强制失效
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getRoleString() {
        switch (role){
            case 1:
                return "admin";
                case 2:
                    return "teacher";
            default:
                return "student";
        }
    }
}
