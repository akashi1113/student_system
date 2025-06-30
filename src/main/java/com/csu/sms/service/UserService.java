package com.csu.sms.service;

import com.csu.sms.common.PageResult;
import com.csu.sms.dto.UserDTO;
import com.csu.sms.vo.UserVO;

public interface UserService {
    // 用户基本操作
    UserVO getUserById(Long id);
    UserVO login(String username, String password);
    Long register(UserDTO userDTO);
    boolean updateUserProfile(UserDTO userDTO);
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    // 管理员操作
    PageResult<UserVO> listUsers(String keyword, Integer status, Integer page, Integer size);
    boolean enableUser(Long userId, Long adminId);
    boolean disableUser(Long userId, Long adminId);
    boolean setUserRole(Long userId, Integer role, Long adminId);

    boolean isAdminRole(Long adminId, Integer role);

    boolean resetPassword(Long id, Long adminId);
}
