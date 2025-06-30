package com.csu.sms.controller;

import com.csu.sms.common.ApiResponse;
import com.csu.sms.common.PageResult;
import com.csu.sms.dto.UserDTO;
import com.csu.sms.service.UserService;
import com.csu.sms.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUserById(@PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        if (userVO == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        return ApiResponse.success(userVO);
    }

    @PostMapping("/login")
    public ApiResponse<UserVO> login(
            @RequestParam String username,
            @RequestParam String password
    ) {
        UserVO userVO = userService.login(username, password);
        if (userVO == null) {
            return ApiResponse.error(401, "用户名或密码错误");
        }
        return ApiResponse.success(userVO);
    }

    @PostMapping("/register")
    public ApiResponse<Long> register(@RequestBody @Valid UserDTO userDTO) {
        Long userId = userService.register(userDTO);
        if (userId == null) {
            return ApiResponse.error(500, "注册失败，请稍后再试");
        }
        return ApiResponse.success("注册成功！", userId);
    }

    @PutMapping("/{id}")
    public ApiResponse<Boolean> updateProfile(
            @PathVariable Long id,
            @RequestBody @Valid UserDTO userDTO
    ) {
        userDTO.setId(id);
        boolean success = userService.updateUserProfile(userDTO);
        if (!success) {
            return ApiResponse.error(500, "更新个人信息失败");
        }
        return ApiResponse.success(true);
    }

    @PutMapping("/{id}/password")
    public ApiResponse<Boolean> changePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {
        boolean success = userService.changePassword(id, oldPassword, newPassword);
        if (!success) {
            return ApiResponse.error(500, "修改密码失败，请确认旧密码是否正确");
        }
        return ApiResponse.success(true);
    }

    // 管理员接口
    @GetMapping("/admin/list")
    public ApiResponse<PageResult<UserVO>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long adminId,
            @RequestParam Integer role
    ) {
        if(!userService.isAdminRole(adminId, role)){
            return ApiResponse.error(403, "没有权限");
        }
        PageResult<UserVO> result = userService.listUsers(keyword, status, page, size);
        return ApiResponse.success(result);
    }

    @PutMapping("/admin/{id}/enable")
    public ApiResponse<Boolean> enableUser(
            @PathVariable Long id,
            @RequestParam Long adminId
    ) {
        boolean success = userService.enableUser(id, adminId);
        if (!success) {
            return ApiResponse.error(500, "启用用户失败");
        }
        return ApiResponse.success(true);
    }

    @PutMapping("/admin/{id}/disable")
    public ApiResponse<Boolean> disableUser(
            @PathVariable Long id,
            @RequestParam Long adminId
    ) {
        boolean success = userService.disableUser(id, adminId);
        if (!success) {
            return ApiResponse.error(500, "禁用用户失败");
        }
        return ApiResponse.success(true);
    }

    @PutMapping("/admin/{id}/role")
    public ApiResponse<Boolean> setUserRole(
            @PathVariable Long id,
            @RequestParam Integer role,
            @RequestParam Long adminId
    ) {
        boolean success = userService.setUserRole(id, role, adminId);
        if (!success) {
            return ApiResponse.error(500, "设置用户角色失败");
        }
        return ApiResponse.success(true);
    }

    @PutMapping("/admin/{id}/password")
    public ApiResponse<Boolean> resetPassword(
            @PathVariable Long id,
            @RequestParam Long adminId
    ) {
        boolean success = userService.resetPassword(id, adminId);
        if (!success) {
            return ApiResponse.error(500, "重置密码失败");
        }
        return ApiResponse.success(true);
    }
}
