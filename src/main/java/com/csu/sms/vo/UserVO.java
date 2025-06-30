package com.csu.sms.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String statusDesc;  // 状态描述
    private String roleDesc;    // 角色描述
    private LocalDateTime createTime;
}
