package com.example.tab.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.tab.model.dto.AuthDTO;
import com.example.tab.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody AuthDTO dto) {
        userService.register(dto);
        return "注册成功";
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody AuthDTO dto) {
        return userService.login(dto);
    }

    @PostMapping("/logout")
    public String logout() {
        StpUtil.logout();
        return "登出成功";
    }

    @GetMapping("/info")
    public Map<String, Object> getUserInfo() {
        // 确保已登录
        StpUtil.checkLogin();

        Map<String, Object> result = new HashMap<>();
        result.put("userId", StpUtil.getLoginIdAsLong());
        result.put("isLogin", StpUtil.isLogin());
        return result;
    }
}