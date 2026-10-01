package com.example.tab.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tab.mapper.UserMapper;
import com.example.tab.model.dto.AuthDTO;
import com.example.tab.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
     * 用户注册（BCrypt 密码加密存储）
     */
    public void register(AuthDTO dto) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(query) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        // 使用 BCrypt 对原始密码进行哈希加密
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
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

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被冻结");
        }

        StpUtil.login(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("tokenName", StpUtil.getTokenName());
        result.put("tokenValue", StpUtil.getTokenValue());
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());

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