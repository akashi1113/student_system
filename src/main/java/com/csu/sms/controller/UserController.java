package com.csu.sms.controller;

import com.csu.sms.annotation.LogOperation;
import com.csu.sms.annotation.RequireAdmin;
import com.csu.sms.common.ApiControllerResponse;
import com.csu.sms.common.PageResult;
import com.csu.sms.common.ServiceException;
import com.csu.sms.dto.UserDTO;
import com.csu.sms.service.UserService;
import com.csu.sms.util.UserContext;
import com.csu.sms.vo.UserVO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@CrossOrigin(origins = {"http://localhost:5173"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
        allowCredentials = "true")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserService userService;

    // ===================================
    //  公开访问接口 (Public APIs)
    // ===================================

    // 用户登录
    @PostMapping("/login")
    @LogOperation(module = "用户管理", operation = "用户登录", description = "用户登录系统")
    public ApiControllerResponse<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password
    ) {
        try {
            Map<String, Object> result = userService.login(username, password);
            return ApiControllerResponse.success(result);
        } catch (ServiceException e) {
            log.warn("Login failed for user {}: {}", username, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during login for user {}: {}", username, e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，登录失败，请稍后再试。");
        }
    }

    // 用户注册 (支持头像上传)
    // 注意：同时接收表单数据和文件，不能用 @RequestBody UserDTO
    // 前端请求 Content-Type 必须是 multipart/form-data
    @PostMapping("/register")
    @LogOperation(module = "用户管理", operation = "用户注册", description = "用户注册账号")
    public ApiControllerResponse<Long> register(@RequestBody UserDTO userDTO) {
        Long userId = userService.register(userDTO, null); // 头像如需上传建议单独接口
        return ApiControllerResponse.success(userId);
    }

    // 发送验证码
    @PostMapping("/sendCode")
    public ApiControllerResponse<Boolean> sendVerificationCode(@RequestParam @NotBlank @Email String email) {
        try {
            userService.sendVerificationCode(email);
            return ApiControllerResponse.success(true);
        } catch (ServiceException e) {
            log.warn("Failed to send verification code to {}: {}", email, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while sending verification code to {}: {}", email, e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，发送验证码失败。");
        }
    }

    // 邮箱验证码登录
    @PostMapping("/loginByCode")
    public ApiControllerResponse<Map<String, Object>> loginByCode(
            @RequestParam @NotBlank @Email String email,
            @RequestParam @NotBlank @Size(min = 6, max = 6) String code
    ) {
        try {
            Map<String, Object> result = userService.loginByCode(email, code);
            return ApiControllerResponse.success(result);
        } catch (ServiceException e) {
            log.warn("Login by code failed for email {}: {}", email, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during login by code for email {}: {}", email, e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，验证码登录失败。");
        }
    }

    // ===================================
    //  需要登录的普通用户接口 (User Authenticated APIs)
    // ===================================

//    // ✨ 新增：获取当前登录用户的信息
//    @GetMapping("/me")
//    public ApiControllerResponse<UserVO> getMyProfile() {
//        try {
//            // ✨ 修改：从 UserContext 获取用户ID，确保用户只能获取自己的信息
//            Long currentUserId = UserContext.getRequiredCurrentUserId();
//            UserVO userVO = userService.getUserById(currentUserId);
//            if (userVO == null) {
//                // 这种情况理论上不应发生，因为token有效但数据库无此用户
//                return ApiControllerResponse.error(404, "用户不存在。");
//            }
//            return ApiControllerResponse.success(userVO);
//        } catch (Exception e) {
//            log.error("An unexpected error occurred while getting current user profile: {}", e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，获取用户信息失败。");
//        }
//    }

        // 获取用户详情
    @GetMapping("/{id}")
    public ApiControllerResponse<UserVO> getUserById(@PathVariable Long id) {
        try {
            UserVO userVO = userService.getUserById(id);
            if (userVO == null) {
                return ApiControllerResponse.error(404, "用户不存在。");
            }
            return ApiControllerResponse.success(userVO);
        } catch (ServiceException e) {
            log.warn("Failed to get user by id {}: {}", id, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while getting user by id {}: {}", id, e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，获取用户失败。");
        }
    }

    // 更新个人信息 (支持头像上传/清除)
    // 同样，同时接收表单数据和文件，不能用 @RequestBody UserDTO
    // 前端请求 Content-Type 必须是 multipart/form-data
    @PutMapping("/{id}")
    @LogOperation(module = "用户管理", operation = "修改个人信息", description = "用户修改个人信息")
    public ApiControllerResponse<Boolean> updateProfile(
            @PathVariable Long id,
            @NotBlank(message = "用户名不能为空")
            @Size(min = 3, max = 20, message = "用户名长度需在3到20字符之间")
            @RequestParam String username, // 用户名也允许更新

            @NotBlank(message = "邮箱不能为空")
            @Email(message = "邮箱格式不正确")
            @RequestParam String email, // 邮箱也允许更新

            // 接收头像文件，可选
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            // 接收avatar字符串，用于清除操作。如果前端明确传空字符串，Service层会处理清除逻辑
            @RequestParam(value = "avatar", required = false) String avatarStringFromForm
    ) {
        try {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(id);
            userDTO.setUsername(username);
            userDTO.setEmail(email);
            userDTO.setAvatar(avatarStringFromForm); // 将前端传来的avatar字符串设置到DTO，Service层会根据此判断是否清除

            boolean success = userService.updateUserProfile(userDTO, avatarFile); // 传递文件
            return ApiControllerResponse.success(success);
        } catch (ServiceException e) {
            log.warn("Failed to update profile for user {}: {}", id, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during profile update for user {}: {}", id, e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，更新个人信息失败。");
        }
    }

//    // 更新个人信息（不含头像）
//    @PutMapping("/me")
//    @LogOperation(module = "用户管理", operation = "修改个人信息", description = "用户修改个人信息")
//    public ApiControllerResponse<Boolean> updateMyProfile(@RequestBody UserDTO userDTO) {
//        try {
//            // ✨ 修改：从 UserContext 获取用户ID，确保用户只能修改自己的信息
//            Long currentUserId = UserContext.getRequiredCurrentUserId();
//            userDTO.setId(currentUserId);
//
//            boolean success = userService.updateUserProfile(userDTO, null); // 头像在单独接口处理
//            return ApiControllerResponse.success(success);
//        } catch (ServiceException e) {
//            log.warn("Failed to update profile for user {}: {}", UserContext.getCurrentUserId(), e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred during profile update for user {}: {}", UserContext.getCurrentUserId(), e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，更新个人信息失败。");
//        }
//    }
//
//    // ✨ 新增：单独的头像上传接口
//    @PostMapping("/me/avatar")
//    @LogOperation(module = "用户管理", operation = "上传头像", description = "用户上传或更新头像")
//    public ApiControllerResponse<String> uploadMyAvatar(@RequestParam("avatarFile") MultipartFile avatarFile) {
//        try {
//            Long currentUserId = UserContext.getRequiredCurrentUserId();
//            String avatarUrl = userService.updateAvatar(currentUserId, avatarFile);
//            return ApiControllerResponse.success("头像上传成功", avatarUrl);
//        } catch (ServiceException e) {
//            log.warn("Failed to upload avatar for user {}: {}", UserContext.getCurrentUserId(), e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred during avatar upload for user {}: {}", UserContext.getCurrentUserId(), e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，上传头像失败。");
//        }
//    }


    // 修改密码
    @PutMapping("/{id}/password")
    @LogOperation(module = "用户管理", operation = "修改密码", description = "用户修改登录密码")
    public ApiControllerResponse<Boolean> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            // ✨ 修改：从 UserContext 获取用户ID
            Long currentUserId = UserContext.getRequiredCurrentUserId();
            boolean success = userService.changePassword(currentUserId, oldPassword, newPassword);
            return ApiControllerResponse.success(success);
        } catch (ServiceException e) {
            log.warn("Failed to change password for user {}: {}", UserContext.getCurrentUserId(), e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during password change for user {}: {}", UserContext.getCurrentUserId(), e.getMessage(), e);
            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，修改密码失败。");
        }
    }

    // 登出
    @PostMapping("/logout")
    public ApiControllerResponse<Boolean> logout(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                userService.logout(token.substring(7));
            }
            return ApiControllerResponse.success(true);
        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage());
            return ApiControllerResponse.error(500, "登出失败");
        }
    }

    // ===================================
    //  管理员专属接口 (Admin Only APIs)
    // ===================================

    // 获取用户详情（管理员）
    // ✨ 修改：保留此接口，供管理员查询任意用户
    @GetMapping("/admin/{id}")
    @RequireAdmin
    public ApiControllerResponse<UserVO> getUserByIdForAdmin(@PathVariable Long id) {
        try {
            UserVO userVO = userService.getUserById(id);
            if (userVO == null) {
                return ApiControllerResponse.error(404, "用户不存在。");
            }
            return ApiControllerResponse.success(userVO);
        } catch (Exception e) {
            log.error("An unexpected error occurred while admin getting user by id {}: {}", id, e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，获取用户失败。");
        }
    }

    // 获取用户列表
    @GetMapping("/admin/list")
    @RequireAdmin // ✨ 修改：使用注解进行权限校验
    public ApiControllerResponse<PageResult<UserVO>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
            // ✨ 修改：移除 adminId 和手动权限校验
    ) {
        try {
            PageResult<UserVO> result = userService.listUsers(page, size);
            return ApiControllerResponse.success(result);
        } catch (Exception e) {
            log.error("An unexpected error occurred while admin listing users: {}", e.getMessage(), e);
            return ApiControllerResponse.error(500, "服务器内部错误，获取用户列表失败。");
        }
    }

    // 启用用户
    @PutMapping("/admin/{id}/enable")
    @RequireAdmin // ✨ 修改：使用注解
    @LogOperation(module = "用户管理", operation = "启用用户", description = "管理员启用用户账号")
    public ApiControllerResponse<Boolean> enableUser(@PathVariable Long id) {
        try {
            // ✨ 修改：从 UserContext 获取管理员ID
            Long adminId = UserContext.getRequiredCurrentUserId();
            boolean success = userService.enableUser(id, adminId);
            return ApiControllerResponse.success(success);
        } catch (ServiceException e) {
            log.warn("Admin {} failed to enable user {}: {}", UserContext.getCurrentUserId(), id, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while admin {} enabling user {}: {}", UserContext.getCurrentUserId(), id, e.getMessage(), e);
            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，启用用户失败。");
        }
    }

    // 禁用用户
    @PutMapping("/admin/{id}/disable")
    @RequireAdmin // ✨ 修改：使用注解
    @LogOperation(module = "用户管理", operation = "禁用用户", description = "管理员禁用用户账号")
    public ApiControllerResponse<Boolean> disableUser(@PathVariable Long id) {
        try {
            Long adminId = UserContext.getRequiredCurrentUserId();
            boolean success = userService.disableUser(id, adminId);
            return ApiControllerResponse.success(success);
        } catch (ServiceException e) {
            log.warn("Admin {} failed to disable user {}: {}", UserContext.getCurrentUserId(), id, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while admin {} disabling user {}: {}", UserContext.getCurrentUserId(), id, e.getMessage(), e);
            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，禁用用户失败。");
        }
    }

    // 设置用户角色
    @PutMapping("/admin/{id}/role")
    @RequireAdmin // ✨ 修改：使用注解
    @LogOperation(module = "用户管理", operation = "设置用户角色", description = "管理员设置用户角色")
    public ApiControllerResponse<Boolean> setUserRole(
            @PathVariable Long id,
            @RequestParam Integer role
    ) {
        try {
            Long adminId = UserContext.getRequiredCurrentUserId();
            boolean success = userService.setUserRole(id, role, adminId);
            return ApiControllerResponse.success(success);
        } catch (ServiceException e) {
            log.warn("Admin {} failed to set role for user {}: {}", UserContext.getCurrentUserId(), id, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while admin {} setting role for user {}: {}", UserContext.getCurrentUserId(), id, e.getMessage(), e);
            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，设置用户角色失败。");
        }
    }

    // 重置密码
    @PutMapping("/admin/{id}/password")
    @RequireAdmin // ✨ 修改：使用注解
    @LogOperation(module = "用户管理", operation = "重置用户密码", description = "管理员重置用户密码")
    public ApiControllerResponse<Boolean> resetPassword(@PathVariable Long id) {
        try {
            Long adminId = UserContext.getRequiredCurrentUserId();
            boolean success = userService.resetPassword(id, adminId);
            return ApiControllerResponse.success(success);
        } catch (ServiceException e) {
            log.warn("Admin {} failed to reset password for user {}: {}", UserContext.getCurrentUserId(), id, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while admin {} resetting password for user {}: {}", UserContext.getCurrentUserId(), id, e.getMessage(), e);
            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，重置密码失败。");
        }
    }

    // 强制下线
    @PostMapping("/admin/{userId}/forceLogout")
    @RequireAdmin // ✨ 修改：使用注解
    public ApiControllerResponse<Boolean> forceLogout(@PathVariable Long userId) {
        try {
            Long adminId = UserContext.getRequiredCurrentUserId();
            boolean success = userService.forceLogout(userId, adminId);
            return ApiControllerResponse.success(success);
        } catch (ServiceException e) {
            log.warn("Admin {} failed to force logout user {}: {}", UserContext.getCurrentUserId(), userId, e.getMessage());
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while force logout user {}: {}", UserContext.getCurrentUserId(), userId, e.getMessage(), e);
            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，强制下线失败。");
        }
    }
}





//package com.csu.sms.controller;
//
//import com.csu.sms.common.ApiControllerResponse;
//import com.csu.sms.common.PageResult;
//import com.csu.sms.dto.UserDTO;
//import com.csu.sms.common.ServiceException;
//import com.csu.sms.service.UserService;
//import com.csu.sms.vo.UserVO;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.validation.annotation.Validated; // 用于对 @RequestParam 参数进行校验
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile; // 引入 MultipartFile
//import com.csu.sms.annotation.LogOperation;
//
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
//
//import java.util.Map;
//
////@CrossOrigin(origins = "http://localhost:5173")
//@CrossOrigin(origins = {"http://localhost:5173"},
//        allowedHeaders = "*",
//        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
//        allowCredentials = "true")
//@RestController
//@RequestMapping("/api/users")
//@RequiredArgsConstructor
//@Slf4j
//@Validated//确保@RequestParam 上的校验注解也能生效
//public class UserController {
//    private final UserService userService;
//
//    // 获取用户详情
//    @GetMapping("/{id}")
//    public ApiControllerResponse<UserVO> getUserById(@PathVariable Long id) {
//        try {
//            UserVO userVO = userService.getUserById(id);
//            if (userVO == null) {
//                return ApiControllerResponse.error(404, "用户不存在。");
//            }
//            return ApiControllerResponse.success(userVO);
//        } catch (ServiceException e) {
//            log.warn("Failed to get user by id {}: {}", id, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred while getting user by id {}: {}", id, e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，获取用户失败。");
//        }
//    }
//
//    // 用户登录
//    @PostMapping("/login")
//    @LogOperation(module = "用户管理", operation = "用户登录", description = "用户登录系统")
//    public ApiControllerResponse<Map<String, Object>> login(
//            @RequestParam String username,
//            @RequestParam String password
//    ) {
//        try {
//            Map<String, Object> result = userService.login(username, password);
//            return ApiControllerResponse.success(result);
//        } catch (ServiceException e) {
//            log.warn("Login failed for user {}: {}", username, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred during login for user {}: {}", username, e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，登录失败，请稍后再试。");
//        }
//    }
//
//    // 用户注册 (支持头像上传)
//    // 注意：同时接收表单数据和文件，不能用 @RequestBody UserDTO
//    // 前端请求 Content-Type 必须是 multipart/form-data
//    @PostMapping("/register")
//    @LogOperation(module = "用户管理", operation = "用户注册", description = "用户注册账号")
//    public ApiControllerResponse<Long> register(@RequestBody UserDTO userDTO) {
//        Long userId = userService.register(userDTO, null); // 头像如需上传建议单独接口
//        return ApiControllerResponse.success(userId);
//    }
//
//    // 更新个人信息 (支持头像上传/清除)
//    // 同样，同时接收表单数据和文件，不能用 @RequestBody UserDTO
//    // 前端请求 Content-Type 必须是 multipart/form-data
//    @PutMapping("/{id}")
//    @LogOperation(module = "用户管理", operation = "修改个人信息", description = "用户修改个人信息")
//    public ApiControllerResponse<Boolean> updateProfile(
//            @PathVariable Long id,
//            @NotBlank(message = "用户名不能为空")
//            @Size(min = 3, max = 20, message = "用户名长度需在3到20字符之间")
//            @RequestParam String username, // 用户名也允许更新
//
//            @NotBlank(message = "邮箱不能为空")
//            @Email(message = "邮箱格式不正确")
//            @RequestParam String email, // 邮箱也允许更新
//
//            // 接收头像文件，可选
//            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
//            // 接收avatar字符串，用于清除操作。如果前端明确传空字符串，Service层会处理清除逻辑
//            @RequestParam(value = "avatar", required = false) String avatarStringFromForm
//    ) {
//        try {
//            UserDTO userDTO = new UserDTO();
//            userDTO.setId(id);
//            userDTO.setUsername(username);
//            userDTO.setEmail(email);
//            userDTO.setAvatar(avatarStringFromForm); // 将前端传来的avatar字符串设置到DTO，Service层会根据此判断是否清除
//
//            boolean success = userService.updateUserProfile(userDTO, avatarFile); // 传递文件
//            return ApiControllerResponse.success(success);
//        } catch (ServiceException e) {
//            log.warn("Failed to update profile for user {}: {}", id, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred during profile update for user {}: {}", id, e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，更新个人信息失败。");
//        }
//    }
//
//    // 修改密码
//    @PutMapping("/{id}/password")
//    @LogOperation(module = "用户管理", operation = "修改密码", description = "用户修改登录密码")
//    public ApiControllerResponse<Boolean> changePassword(
//            @PathVariable Long id,
//            @RequestParam String oldPassword,
//            @RequestParam String newPassword
//    ) {
//        try {
//            boolean success = userService.changePassword(id, oldPassword, newPassword);
//            return ApiControllerResponse.success(success);
//        } catch (ServiceException e) {
//            log.warn("Failed to change password for user {}: {}", id, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred during password change for user {}: {}", id, e.getMessage(), e);
//            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，修改密码失败。");
//        }
//    }
//
//    // 管理员接口：获取用户列表
//    @GetMapping("/admin/list")
//    public ApiControllerResponse<PageResult<UserVO>> listUsers(
//            @RequestParam(defaultValue = "1") Integer page,
//            @RequestParam(defaultValue = "10") Integer size,
//            @RequestParam Long adminId
//    ) {
//        try {
//            if (!userService.isAdminRole(adminId)) {
//                return ApiControllerResponse.error(401, "没有权限。");
//            }
//            PageResult<UserVO> result = userService.listUsers(page, size);
//            return ApiControllerResponse.success(result);
//        } catch (ServiceException e) {
//            log.warn("Admin failed to list users: {}", e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred while admin listing users: {}", e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，获取用户列表失败。");
//        }
//    }
//
//    // 管理员接口：启用用户
//    @PutMapping("/admin/{id}/enable")
//    @LogOperation(module = "用户管理", operation = "启用用户", description = "管理员启用用户账号")
//    public ApiControllerResponse<Boolean> enableUser(
//            @PathVariable Long id,
//            @RequestParam Long adminId
//    ) {
//        try {
//            boolean success = userService.enableUser(id, adminId);
//            return ApiControllerResponse.success(success);
//        } catch (ServiceException e) {
//            log.warn("Admin failed to enable user {}: {}", id, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred while admin enabling user {}: {}", id, e.getMessage(), e);
//            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，启用用户失败。");
//        }
//    }
//
//    // 管理员接口：禁用用户
//    @PutMapping("/admin/{id}/disable")
//    @LogOperation(module = "用户管理", operation = "禁用用户", description = "管理员禁用用户账号")
//    public ApiControllerResponse<Boolean> disableUser(
//            @PathVariable Long id,
//            @RequestParam Long adminId
//    ) {
//        try {
//            boolean success = userService.disableUser(id, adminId);
//            return ApiControllerResponse.success(success);
//        } catch (ServiceException e) {
//            log.warn("Admin failed to disable user {}: {}", id, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred while admin disabling user {}: {}", id, e.getMessage(), e);
//            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，禁用用户失败。");
//        }
//    }
//
//    // 管理员接口：设置用户角色
//    @PutMapping("/admin/{id}/role")
//    @LogOperation(module = "用户管理", operation = "设置用户角色", description = "管理员设置用户角色")
//    public ApiControllerResponse<Boolean> setUserRole(
//            @PathVariable Long id,
//            @RequestParam Integer role,
//            @RequestParam Long adminId
//    ) {
//        try {
//            boolean success = userService.setUserRole(id, role, adminId);
//            return ApiControllerResponse.success(success);
//        } catch (ServiceException e) {
//            log.warn("Admin failed to set role for user {}: {}", id, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred while admin setting role for user {}: {}", id, e.getMessage(), e);
//            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，设置用户角色失败。");
//        }
//    }
//
//    // 管理员接口：重置密码
//    @PutMapping("/admin/{id}/password")
//    @LogOperation(module = "用户管理", operation = "重置用户密码", description = "管理员重置用户密码")
//    public ApiControllerResponse<Boolean> resetPassword(
//            @PathVariable Long id,
//            @RequestParam Long adminId
//    ) {
//        try {
//            boolean success = userService.resetPassword(id, adminId);
//            return ApiControllerResponse.success(success);
//        } catch (ServiceException e) {
//            log.warn("Admin failed to reset password for user {}: {}", id, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred while admin resetting password for user {}: {}", id, e.getMessage(), e);
//            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，重置密码失败。");
//        }
//    }
//
//    // 新增：发送验证码接口
//    @PostMapping("/sendCode")
//    public ApiControllerResponse<Boolean> sendVerificationCode(
//            @RequestParam @NotBlank @Email String email
//    ) {
//        try {
//            userService.sendVerificationCode(email);
//            return ApiControllerResponse.success(true);
//        } catch (ServiceException e) {
//            log.warn("Failed to send verification code to {}: {}", email, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred while sending verification code to {}: {}", email, e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，发送验证码失败。");
//        }
//    }
//
//    // 新增：邮箱验证码登录接口
//    @PostMapping("/loginByCode")
//    public ApiControllerResponse<Map<String, Object>> loginByCode(
//            @RequestParam @NotBlank @Email String email,
//            @RequestParam @NotBlank @Size(min = 6, max = 6) String code
//    ) {
//        try {
//            Map<String, Object> result = userService.loginByCode(email, code);
//            return ApiControllerResponse.success(result);
//        } catch (ServiceException e) {
//            log.warn("Login by code failed for email {}: {}", email, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred during login by code for email {}: {}", email, e.getMessage(), e);
//            return ApiControllerResponse.error(500, "服务器内部错误，验证码登录失败。");
//        }
//    }
//
//    // 新增：登出接口
//    @PostMapping("/logout")
//    public ApiControllerResponse<Boolean> logout(@RequestHeader("Authorization") String token) {
//        try {
//            if (token != null && token.startsWith("Bearer ")) {
//                userService.logout(token.substring(7));
//            }
//            return ApiControllerResponse.success(true);
//        } catch (Exception e) {
//            log.error("Logout error: {}", e.getMessage());
//            return ApiControllerResponse.error(500, "登出失败");
//        }
//    }
//
//    // 新增：管理员强制下线接口
//    @PostMapping("/admin/{userId}/forceLogout")
//    public ApiControllerResponse<Boolean> forceLogout(
//            @PathVariable Long userId,
//            @RequestParam Long adminId
//    ) {
//        try {
//            boolean success = userService.forceLogout(userId, adminId);
//            return ApiControllerResponse.success(success);
//        } catch (ServiceException e) {
//            log.warn("Admin failed to force logout user {}: {}", userId, e.getMessage());
//            return ApiControllerResponse.error(e.getCode(), e.getMessage());
//        } catch (Exception e) {
//            log.error("An unexpected error occurred while force logout user {}: {}", userId, e.getMessage(), e);
//            return ApiControllerResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，强制下线失败。");
//        }
//    }
//}
