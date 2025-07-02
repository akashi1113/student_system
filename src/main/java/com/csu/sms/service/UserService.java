package com.csu.sms.service;

import com.csu.sms.common.PageResult;
import com.csu.sms.dto.UserDTO;
import com.csu.sms.vo.UserVO;
import org.springframework.web.multipart.MultipartFile; // 导入 MultipartFile

import java.util.List;
import java.util.Map;

public interface UserService {
    UserVO getUserById(Long id);
    // UserVO login(String username, String password);
    Map<String, Object> login(String username, String password); // 修改返回类型
    Map<String, Object> loginByCode(String email, String code); // 新增邮箱验证码登录

    Long register(UserDTO userDTO, MultipartFile avatarFile);
    boolean updateUserProfile(UserDTO userDTO, MultipartFile avatarFile); // 修改方法签名

    boolean changePassword(Long userId, String oldPassword, String newPassword);
    PageResult<UserVO> listUsers(Integer page, Integer size);
    boolean enableUser(Long userId, Long adminId);
    boolean disableUser(Long userId, Long adminId);
    boolean setUserRole(Long userId, Integer role, Long adminId);
    boolean isAdminRole(Long adminId);
    boolean resetPassword(Long id, Long adminId);

    // 新增方法
    void sendVerificationCode(String email); // 发送验证码
    void logout(String token); // 登出
    boolean forceLogout(Long userId, Long adminId); // 强制下线
}

//package com.csu.sms.service;
//
//import com.csu.sms.common.PageResult;
//import com.csu.sms.dto.UserDTO;
//import com.csu.sms.vo.UserVO;
//
//public interface UserService {
//    // 用户基本操作
//    UserVO getUserById(Long id);
//    UserVO login(String username, String password);
//    Long register(UserDTO userDTO);
//    boolean updateUserProfile(UserDTO userDTO);
//    boolean changePassword(Long userId, String oldPassword, String newPassword);
//
//    // 管理员操作
//    PageResult<UserVO> listUsers(String keyword, Integer status, Integer page, Integer size);
//    boolean enableUser(Long userId, Long adminId);
//    boolean disableUser(Long userId, Long adminId);
//    boolean setUserRole(Long userId, Integer role, Long adminId);
//
//    boolean isAdminRole(Long adminId, Integer role);
//
//    boolean resetPassword(Long id, Long adminId);
//}
