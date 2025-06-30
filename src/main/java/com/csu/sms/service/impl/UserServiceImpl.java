package com.csu.sms.service.impl;

import com.csu.sms.common.PageResult;
import com.csu.sms.dto.UserDTO;
import com.csu.sms.model.User;
import com.csu.sms.model.enums.UserRole;
import com.csu.sms.model.enums.UserStatus;
import com.csu.sms.persistence.UserDao;
import com.csu.sms.service.UserService;
import com.csu.sms.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Cacheable(value = "users", key = "'id_' + #id")
    @Override
    public UserVO getUserById(Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            return null;
        }
        return convertToVO(user);
    }

    @Override
    public UserVO login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            return null;
        }

        // 检查用户是否被禁用
        if (user.getStatus() == UserStatus.DISABLED.getCode()) {
            log.warn("User {} is disabled and tried to login", username);
            return null;
        }
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Invalid password for user {}", username);
            return null;
        }

        return convertToVO(user);
    }

    @Override
    public Long register(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (userDao.findByUsername(userDTO.getUsername()) != null) {
            log.warn("Username {} already exists", userDTO.getUsername());
            return null;
        }

        // 检查邮箱是否已存在
        if (userDao.findByEmail(userDTO.getEmail()) != null) {
            log.warn("Email {} already exists", userDTO.getEmail());
            return null;
        }

        // 创建新用户
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setAvatar(userDTO.getAvatar());
        user.setStatus(UserStatus.ACTIVE.getCode());
        user.setRole(UserRole.USER.getCode());  // 默认为普通用户
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        int rows = userDao.insertUser(user);
        if (rows > 0) {
            return user.getId();
        }
        return null;
    }

    @CacheEvict(value = "users", key = "'id_' + #userDTO.id")
    @Override
    public boolean updateUserProfile(UserDTO userDTO) {
        User existingUser = userDao.findById(userDTO.getId());
        if (existingUser == null) {
            return false;
        }

        // 如果要更改用户名，检查新用户名是否已存在
        if (!existingUser.getUsername().equals(userDTO.getUsername()) &&
                userDao.findByUsername(userDTO.getUsername()) != null) {
            return false;
        }

        // 如果要更改邮箱，检查新邮箱是否已存在
        if (!existingUser.getEmail().equals(userDTO.getEmail()) &&
                userDao.findByEmail(userDTO.getEmail()) != null) {
            return false;
        }

        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setAvatar(userDTO.getAvatar());

        return userDao.updateUser(user) > 0;
    }

    @CacheEvict(value = "users", key = "'id_' + #userId")
    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userDao.findById(userId);
        if (user == null) {
            return false;
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        // 更新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        return userDao.updatePassword(userId, encodedPassword) > 0;
    }

    @Override
    public PageResult<UserVO> listUsers(String keyword, Integer status, Integer page, Integer size) {
        int offset = (page - 1) * size;
        int total = userDao.countUsers(keyword, status);

        if (total == 0) {
            return PageResult.of(List.of(), 0, page, size);
        }

        List<User> users = userDao.findUsersByPage(keyword, status, offset, size);
        List<UserVO> voList = users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, total, page, size);
    }

    @CacheEvict(value = "users", key = "'id_' + #userId")
    @Override
    public boolean enableUser(Long userId, Long adminId) {
        // 验证管理员身份
        User admin = userDao.findById(adminId);
        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
            return false;
        }

        return userDao.updateUserStatus(userId, UserStatus.ACTIVE.getCode()) > 0;
    }

    @CacheEvict(value = "users", key = "'id_' + #userId")
    @Override
    public boolean disableUser(Long userId, Long adminId) {
        // 验证管理员身份
        User admin = userDao.findById(adminId);
        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
            return false;
        }

        // 不能禁用管理员账户
        User user = userDao.findById(userId);
        if (user == null || user.getRole() == UserRole.ADMIN.getCode()) {
            return false;
        }

        return userDao.updateUserStatus(userId, UserStatus.DISABLED.getCode()) > 0;
    }

    @CacheEvict(value = "users", key = "'id_' + #userId")
    @Override
    public boolean setUserRole(Long userId, Integer role, Long adminId) {
        // 验证管理员身份
        User admin = userDao.findById(adminId);
        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
            return false;
        }

        // 验证角色值是否有效
        if (role != UserRole.USER.getCode() && role != UserRole.ADMIN.getCode()) {
            return false;
        }

        return userDao.updateUserRole(userId, role) > 0;
    }

    @Override
    public boolean isAdminRole(Long adminId, Integer role){
        User admin = userDao.findById(adminId);
        return admin != null && admin.getRole() == UserRole.ADMIN.getCode();
    }

    @Override
    public boolean resetPassword(Long id, Long adminId){
        User admin = userDao.findById(adminId);
        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
            return false;
        }
        return userDao.updatePassword(id, passwordEncoder.encode("123456")) > 0;
    }

    // 转换为VO
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setStatusDesc(UserStatus.fromCode(user.getStatus()).getDescription());
        vo.setRoleDesc(UserRole.fromCode(user.getRole()).getDescription());
        return vo;
    }
}
