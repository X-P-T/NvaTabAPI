package com.example.tab.controller;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;

import com.example.tab.model.dto.AuthDTO;
import com.example.tab.model.entity.User;
import com.example.tab.service.UserService;
import com.example.tab.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody AuthDTO dto) {
        userService.register(dto);
        return Result.success("注册成功", null);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody AuthDTO dto) {
        return Result.success(userService.login(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success("登出成功", null);
    }

    @GetMapping("/info")
    public User getUserInfo() {
        return userService.getCurrentUserInfo();
    }

}