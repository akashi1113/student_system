package com.csu.sms.service.impl;

import com.csu.sms.common.PageResult;
import com.csu.sms.dto.UserDTO;
import com.csu.sms.common.ServiceException; // 引入自定义异常
import com.csu.sms.model.user.User;
import com.csu.sms.model.enums.UserRole;
import com.csu.sms.model.enums.UserStatus;
import com.csu.sms.persistence.UserDao;
import com.csu.sms.service.FileStorageService; // 引入文件存储服务
import com.csu.sms.service.UserService;
import com.csu.sms.util.MailUtil;
import com.csu.sms.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value; // 引入 Value
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 引入事务注解
import org.springframework.web.multipart.MultipartFile; // 引入 MultipartFile

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.csu.sms.service.VerificationCodeService;
import com.csu.sms.util.JwtUtil;
import java.util.HashMap;
import java.util.Map;

import com.csu.sms.util.UserContext;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final FileStorageService fileStorageService; // 注入文件存储服务

    private final VerificationCodeService verificationCodeService;
    private final JwtUtil jwtUtil;

    @Value("${app.upload.avatar-folder}")
    private String avatarFolder; // 头像文件夹
    @Value("${app.upload.default-avatar-url}")
    private String defaultAvatarUrl; // 默认头像 URL

    @Cacheable(value = "users", key = "'id_' + #id")
    @Override
    public UserVO getUserById(Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            return null;
        }
        return convertToVO(user);
    }

    // 修改登录方法
    @Override
    public Map<String, Object> login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            throw new ServiceException(400, "用户名或密码错误。");
        }
        if (user.getStatus() == UserStatus.DISABLED.getCode()) {
            log.warn("User {} is disabled and tried to login", username);
            throw new ServiceException(403, "账号已被禁用，请联系管理员。");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Invalid password for user {}", username);
            throw new ServiceException(400, "用户名或密码错误。");
        }

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getTokenVersion());
        
        // 设置用户上下文和登录类型
        UserContext.setCurrentUserId(user.getId());
        UserContext.setCurrentUsername(user.getUsername());
        UserContext.setLoginType("PASSWORD_LOGIN");

        Map<String, Object> result = new HashMap<>();
        result.put("user", convertToVO(user));
        result.put("token", token);
        return result;
    }

    // 新增：发送验证码
    @Override
    public void sendVerificationCode(String email) {
        // 检查邮箱是否已注册
        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new ServiceException(404, "该邮箱未注册");
        }

        // 生成验证码
        String code = verificationCodeService.generateCode(email);

        // 发送邮件
        try {
            MailUtil.setReceiverMail(email);
            MailUtil.setReceiverName(user.getUsername());
            String html = "您的验证码是：" + code + "，5分钟内有效。";

            // 实际发送邮件
            MailUtil.sendEmail(html);
            log.info("Send verification code {} to {}", code, email);
        } catch (Exception e) {
            log.error("发送验证码邮件失败: {}", e.getMessage());
            throw new ServiceException(500, "发送验证码失败，请稍后重试");
        }
    }

    // 新增：邮箱验证码登录
    @Override
    public Map<String, Object> loginByCode(String email, String code) {
        // 验证验证码
        if (!verificationCodeService.verifyCode(email, code)) {
            throw new ServiceException(400, "验证码错误或已过期");
        }

        // 验证通过后，移除验证码
        verificationCodeService.removeCode(email);

        // 获取用户
        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new ServiceException(404, "用户不存在");
        }
        if (user.getStatus() == UserStatus.DISABLED.getCode()) {
            throw new ServiceException(403, "账号已被禁用，请联系管理员。");
        }

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getTokenVersion());
        
        // 设置用户上下文和登录类型
        UserContext.setCurrentUserId(user.getId());
        UserContext.setCurrentUsername(user.getUsername());
        UserContext.setLoginType("EMAIL_CODE_LOGIN");

        Map<String, Object> result = new HashMap<>();
        result.put("user", convertToVO(user));
        result.put("token", token);
        return result;
    }

    // 新增：登出
    @Override
    public void logout(String token) {
        jwtUtil.invalidateToken(token);
    }

    // 新增：强制下线
    @Override
    public boolean forceLogout(Long userId, Long adminId) {
        // 验证管理员权限
        if (!isAdminRole(adminId)) {
            throw new ServiceException(403, "没有权限");
        }

        // 增加用户的tokenVersion，使旧令牌失效
        int updated = userDao.incrementTokenVersion(userId);
        return updated > 0;
    }


    @Transactional // 确保数据库操作和文件操作的原子性
    @Override
    public Long register(UserDTO userDTO, MultipartFile avatarFile) { // 添加 avatarFile 参数
        if (userDao.findByUsername(userDTO.getUsername()) != null) {
            log.warn("Username {} already exists", userDTO.getUsername());
            throw new ServiceException(409, "用户名已被注册。"); // 409 Conflict
        }
        if (userDao.findByEmail(userDTO.getEmail()) != null) {
            log.warn("Email {} already exists", userDTO.getEmail());
            throw new ServiceException(409, "邮箱已被注册。");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());

        // 处理头像上传
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String avatarUrl = fileStorageService.uploadFile(avatarFile, avatarFolder);
                user.setAvatar(avatarUrl);
            } catch (IOException e) {
                log.error("Failed to upload avatar during registration for user {}: {}", userDTO.getUsername(), e.getMessage());
                // 如果头像上传失败，使用默认头像并继续注册，或者直接抛出异常
                user.setAvatar(defaultAvatarUrl); // 使用默认头像
                // throw new ServiceException(500, "头像上传失败: " + e.getMessage()); // 或者这样处理
            }
        } else {
            // 没有上传头像，使用默认头像
            user.setAvatar(defaultAvatarUrl);
        }

        user.setStatus(UserStatus.ACTIVE.getCode());
        user.setRole(UserRole.STUDENT.getCode());

        int rows = userDao.insertUser(user);
        if (rows <= 0) {
            throw new ServiceException(500, "用户注册失败，请稍后重试。");
        }
        return user.getId();
    }


    @CacheEvict(value = "users", key = "'id_' + #userDTO.id")
    @Transactional
    @Override
    public boolean updateUserProfile(UserDTO userDTO, MultipartFile avatarFile) { // 添加 avatarFile 参数
        User existingUser = userDao.findById(userDTO.getId());
        if (existingUser == null) {
            throw new ServiceException(404, "要更新的用户不存在。");
        }

        // 如果要更改用户名，检查新用户名是否已存在且不是当前用户
        if (!existingUser.getUsername().equals(userDTO.getUsername()) &&
                userDao.findByUsername(userDTO.getUsername()) != null) {
            throw new ServiceException(409, "新用户名已被其他用户使用。");
        }

        // 如果要更改邮箱，检查新邮箱是否已存在且不是当前用户
        if (!existingUser.getEmail().equals(userDTO.getEmail()) &&
                userDao.findByEmail(userDTO.getEmail()) != null) {
            throw new ServiceException(409, "新邮箱已被其他用户使用。");
        }

        String newAvatarUrl = existingUser.getAvatar(); // 默认保留旧头像URL
        boolean oldAvatarDeleted = false; // 标记旧头像是否已删除

        // 处理头像文件上传
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                newAvatarUrl = fileStorageService.uploadFile(avatarFile, avatarFolder);
                // 删除旧头像文件 (如果存在且不是默认头像)
                if (existingUser.getAvatar() != null && !existingUser.getAvatar().equals(defaultAvatarUrl)) {
                    oldAvatarDeleted = fileStorageService.deleteFile(existingUser.getAvatar());
                    if (!oldAvatarDeleted) {
                        log.warn("Failed to delete old avatar: {}", existingUser.getAvatar());
                        // 不中断操作，只是记录警告
                    }
                }
            } catch (IOException e) {
                log.error("Failed to upload new avatar for user {}: {}", userDTO.getId(), e.getMessage());
                throw new ServiceException(500, "头像上传失败: " + e.getMessage());
            }
        } else {
            // 如果前端明确传递了空文件，或者DTO中的avatar被设为空字符串，则表示清除头像
            // 这里的判断逻辑需要和前端的约定保持一致
            // 假设 userDTO.getAvatar() 为空字符串时表示清除
            if (userDTO.getAvatar() != null && userDTO.getAvatar().isEmpty()) {
                if (existingUser.getAvatar() != null && !existingUser.getAvatar().equals(defaultAvatarUrl)) {
                    oldAvatarDeleted = fileStorageService.deleteFile(existingUser.getAvatar());
                    if (!oldAvatarDeleted) {
                        log.warn("Failed to delete old avatar when clearing: {}", existingUser.getAvatar());
                    }
                }
                newAvatarUrl = null; // 清除后设置为null，或者设置为默认头像URL
            }
            // 否则（avatarFile为空，且userDTO.getAvatar()不为空），保持现有URL
        }

        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setAvatar(newAvatarUrl); // 使用处理后的头像URL
        user.setUpdateTime(LocalDateTime.now()); // 更新时间

        int result = userDao.updateUser(user);
        if (result <= 0) {
            throw new ServiceException(500, "更新用户资料失败，请稍后重试。");
        }
        return true;
    }

    @CacheEvict(value = "users", key = "'id_' + #userId")
    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new ServiceException(404, "用户不存在。");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ServiceException(400, "旧密码不正确。");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        boolean success = userDao.updatePassword(userId, encodedPassword) > 0;
        if (!success) {
            // 密码修改后增加token版本，使旧token失效
            userDao.incrementTokenVersion(userId);
        }
        return true;
    }

    @Override
    public PageResult<UserVO> listUsers(Integer page, Integer size) {
        int offset = (page - 1) * size;
        int total = userDao.countUsers();

        if (total == 0) {
            return PageResult.of(List.of(), 0, page, size);
        }

        List<User> users = userDao.findUsersByPage(offset, size);
        List<UserVO> voList = users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, total, page, size);
    }

    @CacheEvict(value = "users", key = "'id_' + #userId")
    @Override
    public boolean enableUser(Long userId, Long adminId) {
        User admin = userDao.findById(adminId);
        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
            throw new ServiceException(403, "权限不足，只有管理员可以操作。");
        }
        boolean success = userDao.updateUserStatus(userId, UserStatus.ACTIVE.getCode()) > 0;
        if (!success) {
            throw new ServiceException(500, "启用用户失败，请稍后重试。");
        }
        return true;
    }

    @CacheEvict(value = "users", key = "'id_' + #userId")
    @Override
    public boolean disableUser(Long userId, Long adminId) {
        User admin = userDao.findById(adminId);
        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
            throw new ServiceException(403, "权限不足，只有管理员可以操作。");
        }
        User user = userDao.findById(userId);
        if (user == null) {
            throw new ServiceException(404, "要禁用的用户不存在。");
        }
        if (user.getRole() == UserRole.ADMIN.getCode()) {
            throw new ServiceException(400, "不能禁用管理员账户。");
        }
        boolean success = userDao.updateUserStatus(userId, UserStatus.DISABLED.getCode()) > 0;
        if (!success) {
            throw new ServiceException(500, "禁用用户失败，请稍后重试。");
        }
        return true;
    }

    @CacheEvict(value = "users", key = "'id_' + #userId")
    @Override
    public boolean setUserRole(Long userId, Integer role, Long adminId) {
        User admin = userDao.findById(adminId);
        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
            throw new ServiceException(403, "权限不足，只有管理员可以操作。");
        }
        if (role != UserRole.STUDENT.getCode() && role != UserRole.TEACHER.getCode() && role != UserRole.ADMIN.getCode()) {
            throw new ServiceException(400, "无效的角色值。");
        }
        boolean success = userDao.updateUserRole(userId, role) > 0;
        if (!success) {
            throw new ServiceException(500, "设置用户角色失败，请稍后重试。");
        }
        return true;
    }

    @Override
    public boolean isAdminRole(Long adminId){
        User admin = userDao.findById(adminId);
        return admin != null && admin.getRole() == UserRole.ADMIN.getCode();
    }

    @Override
    public boolean resetPassword(Long id, Long adminId){
        User admin = userDao.findById(adminId);
        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
            throw new ServiceException(403, "权限不足，只有管理员可以操作。");
        }
        boolean success = userDao.updatePassword(id, passwordEncoder.encode("123456")) > 0;
        if (!success) {
            // 重置密码后增加token版本，使旧token失效
            userDao.incrementTokenVersion(id);
        }
        return true;
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setStatusDesc(UserStatus.fromCode(user.getStatus()).getDescription());
        vo.setRoleDesc(UserRole.fromCode(user.getRole()).getDescription());
        return vo;
    }
}
//
//import com.csu.sms.common.PageResult;
//import com.csu.sms.dto.UserDTO;
//import com.csu.sms.model.User;
//import com.csu.sms.model.enums.UserRole;
//import com.csu.sms.model.enums.UserStatus;
//import com.csu.sms.persistence.UserDao;
//import com.csu.sms.service.UserService;
//import com.csu.sms.vo.UserVO;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeanUtils;
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class UserServiceImpl implements UserService {
//    private final UserDao userDao;
//    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
//    @Cacheable(value = "users", key = "'id_' + #id")
//    @Override
//    public UserVO getUserById(Long id) {
//        User user = userDao.findById(id);
//        if (user == null) {
//            return null;
//        }
//        return convertToVO(user);
//    }
//
//    @Override
//    public UserVO login(String username, String password) {
//        User user = userDao.findByUsername(username);
//        if (user == null) {
//            return null;
//        }
//
//        // 检查用户是否被禁用
//        if (user.getStatus() == UserStatus.DISABLED.getCode()) {
//            log.warn("User {} is disabled and tried to login", username);
//            return null;
//        }
//        // 验证密码
//        if (!passwordEncoder.matches(password, user.getPassword())) {
//            log.warn("Invalid password for user {}", username);
//            return null;
//        }
//
//        return convertToVO(user);
//    }
//
//    @Override
//    public Long register(UserDTO userDTO) {
//        // 检查用户名是否已存在
//        if (userDao.findByUsername(userDTO.getUsername()) != null) {
//            log.warn("Username {} already exists", userDTO.getUsername());
//            return null;
//        }
//
//        // 检查邮箱是否已存在
//        if (userDao.findByEmail(userDTO.getEmail()) != null) {
//            log.warn("Email {} already exists", userDTO.getEmail());
//            return null;
//        }
//
//        // 创建新用户
//        User user = new User();
//        user.setUsername(userDTO.getUsername());
//        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
//        user.setEmail(userDTO.getEmail());
//        user.setAvatar(userDTO.getAvatar());
//        user.setStatus(UserStatus.ACTIVE.getCode());
//        user.setRole(UserRole.USER.getCode());  // 默认为普通用户
//        user.setCreateTime(LocalDateTime.now());
//        user.setUpdateTime(LocalDateTime.now());
//
//        int rows = userDao.insertUser(user);
//        if (rows > 0) {
//            return user.getId();
//        }
//        return null;
//    }
//
//    @CacheEvict(value = "users", key = "'id_' + #userDTO.id")
//    @Override
//    public boolean updateUserProfile(UserDTO userDTO) {
//        User existingUser = userDao.findById(userDTO.getId());
//        if (existingUser == null) {
//            return false;
//        }
//
//        // 如果要更改用户名，检查新用户名是否已存在
//        if (!existingUser.getUsername().equals(userDTO.getUsername()) &&
//                userDao.findByUsername(userDTO.getUsername()) != null) {
//            return false;
//        }
//
//        // 如果要更改邮箱，检查新邮箱是否已存在
//        if (!existingUser.getEmail().equals(userDTO.getEmail()) &&
//                userDao.findByEmail(userDTO.getEmail()) != null) {
//            return false;
//        }
//
//        User user = new User();
//        user.setId(userDTO.getId());
//        user.setUsername(userDTO.getUsername());
//        user.setEmail(userDTO.getEmail());
//        user.setAvatar(userDTO.getAvatar());
//
//        return userDao.updateUser(user) > 0;
//    }
//
//    @CacheEvict(value = "users", key = "'id_' + #userId")
//    @Override
//    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
//        User user = userDao.findById(userId);
//        if (user == null) {
//            return false;
//        }
//
//        // 验证旧密码
//        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
//            return false;
//        }
//
//        // 更新密码
//        String encodedPassword = passwordEncoder.encode(newPassword);
//        return userDao.updatePassword(userId, encodedPassword) > 0;
//    }
//
//    @Override
//    public PageResult<UserVO> listUsers(String keyword, Integer status, Integer page, Integer size) {
//        int offset = (page - 1) * size;
//        int total = userDao.countUsers(keyword, status);
//
//        if (total == 0) {
//            return PageResult.of(List.of(), 0, page, size);
//        }
//
//        List<User> users = userDao.findUsersByPage(keyword, status, offset, size);
//        List<UserVO> voList = users.stream()
//                .map(this::convertToVO)
//                .collect(Collectors.toList());
//
//        return PageResult.of(voList, total, page, size);
//    }
//
//    @CacheEvict(value = "users", key = "'id_' + #userId")
//    @Override
//    public boolean enableUser(Long userId, Long adminId) {
//        // 验证管理员身份
//        User admin = userDao.findById(adminId);
//        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
//            return false;
//        }
//
//        return userDao.updateUserStatus(userId, UserStatus.ACTIVE.getCode()) > 0;
//    }
//
//    @CacheEvict(value = "users", key = "'id_' + #userId")
//    @Override
//    public boolean disableUser(Long userId, Long adminId) {
//        // 验证管理员身份
//        User admin = userDao.findById(adminId);
//        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
//            return false;
//        }
//
//        // 不能禁用管理员账户
//        User user = userDao.findById(userId);
//        if (user == null || user.getRole() == UserRole.ADMIN.getCode()) {
//            return false;
//        }
//
//        return userDao.updateUserStatus(userId, UserStatus.DISABLED.getCode()) > 0;
//    }
//
//    @CacheEvict(value = "users", key = "'id_' + #userId")
//    @Override
//    public boolean setUserRole(Long userId, Integer role, Long adminId) {
//        // 验证管理员身份
//        User admin = userDao.findById(adminId);
//        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
//            return false;
//        }
//
//        // 验证角色值是否有效
//        if (role != UserRole.USER.getCode() && role != UserRole.ADMIN.getCode()) {
//            return false;
//        }
//
//        return userDao.updateUserRole(userId, role) > 0;
//    }
//
//    @Override
//    public boolean isAdminRole(Long adminId, Integer role){
//        User admin = userDao.findById(adminId);
//        return admin != null && admin.getRole() == UserRole.ADMIN.getCode();
//    }
//
//    @Override
//    public boolean resetPassword(Long id, Long adminId){
//        User admin = userDao.findById(adminId);
//        if (admin == null || admin.getRole() != UserRole.ADMIN.getCode()) {
//            return false;
//        }
//        return userDao.updatePassword(id, passwordEncoder.encode("123456")) > 0;
//    }
//
//    // 转换为VO
//    private UserVO convertToVO(User user) {
//        UserVO vo = new UserVO();
//        BeanUtils.copyProperties(user, vo);
//        vo.setStatusDesc(UserStatus.fromCode(user.getStatus()).getDescription());
//        vo.setRoleDesc(UserRole.fromCode(user.getRole()).getDescription());
//        return vo;
//    }
//}
