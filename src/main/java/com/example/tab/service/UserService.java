package com.example.tab.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tab.mapper.UserMapper;
import com.example.tab.model.dto.AuthDTO;
import com.example.tab.model.entity.User;
import com.example.tab.util.PermissionUtils;
import com.example.tab.exception.BusinessException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 用户注册
     * 所有自主注册账号默认是学生
     */
    public void register(@Valid @RequestBody AuthDTO dto) {
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
}