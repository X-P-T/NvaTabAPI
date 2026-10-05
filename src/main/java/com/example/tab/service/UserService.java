package com.example.tab.service;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tab.mapper.UserMapper;
import com.example.tab.model.dto.AuthDTO;
import com.example.tab.model.dto.ChangePasswordDTO;
import com.example.tab.model.dto.UpdateProfileDTO;
import com.example.tab.model.entity.User;
import com.example.tab.util.PermissionUtils;
import com.example.tab.exception.BusinessException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder;

    private void updateUserInfo(User user, UpdateProfileDTO dto) {

        boolean changed = false;

        if (dto.getNickname() != null) {

            user.setNickname(dto.getNickname().trim());
            changed = true;
        }

        if (dto.getAvatar() != null) {

            user.setAvatar(dto.getAvatar().trim());
            changed = true;
        }

        if (dto.getEmail() != null) {

            String email = dto.getEmail().trim();

            if (!email.equals(user.getEmail())) {

                LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();

                query.eq(User::getEmail, email)
                        .ne(User::getId, user.getId());

                Long count = userMapper.selectCount(query);

                if (count > 0) {
                    throw new BusinessException(
                            400,
                            "该邮箱已被其他用户使用");
                }

                user.setEmail(email);

                // 邮箱发生变化，需要重新验证
                user.setEmailVerified(0);

                changed = true;
            }
        }

        if (!changed) {
            throw BusinessException.badRequest(
                    "没有需要修改的用户信息");
        }

        userMapper.updateById(user);
    }

    /**
     * 用户注册
     * 所有自主注册账号默认是学生
     */
    public void register(AuthDTO dto) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, dto.getUsername());

        if (userMapper.selectCount(query) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(
                dto.getNickname() != null && !dto.getNickname().isBlank()
                        ? dto.getNickname()
                        : dto.getUsername());

        // 关键：角色由后端指定，忽略客户端传来的角色
        user.setRole(PermissionUtils.ROLE_STUDENT);

        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);
    }

    /**
     * 用户登录（BCrypt 密文比对）
     */
    public Map<String, Object> login(AuthDTO dto) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(query);

        // 进行 trim() 处理防隐形空格，校验用户与密码
        String rawPassword = dto.getPassword() != null ? dto.getPassword().trim() : "";
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword().trim())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被冻结");
        }

        String role = user.getRole();

        if (!PermissionUtils.ROLE_SUPER_ADMIN.equals(role)
                && !PermissionUtils.ROLE_TEACHER.equals(role)
                && !PermissionUtils.ROLE_STUDENT.equals(role)) {
            throw new BusinessException(500, "账号角色异常，请联系管理员");
        }

        // Sa-Token 登录与 Session 缓存角色
        StpUtil.login(user.getId());
        StpUtil.getSession().set("role", user.getRole());

        // 封装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("tokenName", StpUtil.getTokenName());
        result.put("tokenValue", StpUtil.getTokenValue());
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("role", user.getRole());
        result.put(
                "mustChangePassword",
                user.getMustChangePassword() != null
                        && user.getMustChangePassword() == 1);
        return result;
    }

    /**
     * 获取当前登录用户信息
     */
    public User getCurrentUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null); // 脱敏，不把密码返回给前端
        }
        return user;
    }

    /**
     * 修改当前用户密码
     */
    public void changePassword(ChangePasswordDTO dto) {

        Long userId = StpUtil.getLoginIdAsLong();

        User user = userMapper.selectById(userId);

        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        // 验证原密码
        if (user.getPassword() == null
                || !passwordEncoder.matches(
                        dto.getOldPassword(),
                        user.getPassword())) {

            throw new BusinessException(400, "原密码错误");
        }

        // 新密码不能与原密码相同
        if (passwordEncoder.matches(
                dto.getNewPassword(),
                user.getPassword())) {

            throw new BusinessException(400, "新密码不能与原密码相同");
        }

        // BCrypt 加密新密码
        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());

        user.setPassword(encodedPassword);

        // 修改完成后取消强制修改密码状态
        user.setMustChangePassword(0);

        userMapper.updateById(user);
        StpUtil.logout();
    }

    /**
     * 生成随机临时密码
     */
    private String generateTemporaryPassword() {

        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ"
                + "abcdefghijkmnopqrstuvwxyz"
                + "23456789";

        StringBuilder password = new StringBuilder();

        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        return password.toString();
    }

    /**
     * 超级管理员重置用户密码
     */
    public String resetPasswordByAdmin(Long userId) {

        // 只有超级管理员可以执行
        PermissionUtils.checkSuperAdmin();

        if (userId == null) {
            throw BusinessException.badRequest("用户ID不能为空");
        }

        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 不允许超管通过此接口重置自己的密码
        if (currentUserId.equals(userId)) {
            throw BusinessException.badRequest(
                    "不能通过管理员重置功能重置自己的密码，请使用修改密码功能");
        }

        User user = userMapper.selectById(userId);

        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        // 生成临时密码
        String temporaryPassword = generateTemporaryPassword();

        // BCrypt 加密
        String encodedPassword = passwordEncoder.encode(temporaryPassword);

        user.setPassword(encodedPassword);

        // 强制用户下次登录修改密码
        user.setMustChangePassword(1);

        userMapper.updateById(user);

        StpUtil.logout(userId);

        return temporaryPassword;
    }

    public void resetPasswordByEmail(
            String email,
            String newPassword) {

        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getEmail, email);

        User user = userMapper.selectOne(query);

        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        // 新密码不能和旧密码相同
        if (user.getPassword() != null
                && passwordEncoder.matches(
                        newPassword,
                        user.getPassword())) {

            throw new BusinessException(
                    400,
                    "新密码不能与原密码相同");
        }

        // BCrypt 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);

        user.setPassword(encodedPassword);

        // 忘记密码重置成功后，不再要求强制修改密码
        user.setMustChangePassword(0);

        userMapper.updateById(user);

        // 注销该用户已有的登录状态
        StpUtil.logout(user.getId());
    }

    public void updateCurrentUser(UpdateProfileDTO dto) {

        Long userId = StpUtil.getLoginIdAsLong();

        User user = userMapper.selectById(userId);

        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        updateUserInfo(user, dto);
    }

    public void updateUserByAdmin(
            Long userId,
            UpdateProfileDTO dto) {

        PermissionUtils.checkSuperAdmin();

        if (userId == null) {
            throw BusinessException.badRequest("用户ID不能为空");
        }

        User user = userMapper.selectById(userId);

        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        updateUserInfo(user, dto);
    }
}