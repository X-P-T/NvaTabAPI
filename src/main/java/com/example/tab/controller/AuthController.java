package com.example.tab.controller;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

import com.example.tab.model.dto.EmailCodeVerifyDTO;
import com.example.tab.model.dto.ForgotPasswordDTO;
import com.example.tab.model.dto.AuthDTO;
import com.example.tab.model.dto.ChangePasswordDTO;
import com.example.tab.model.entity.User;
import com.example.tab.service.EmailCodeService;
import com.example.tab.service.UserService;
import com.example.tab.common.Result;
import com.example.tab.model.dto.ForgotPasswordResetDTO;
import com.example.tab.model.dto.UpdateProfileDTO;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final EmailCodeService emailCodeService;
    private final UserService userService;

    @PostMapping("/register")
    public Result<Void> register(
            @Valid @RequestBody AuthDTO dto) {

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

    @PutMapping("/password")
    public Result<Void> changePassword(
            @Valid @RequestBody ChangePasswordDTO dto) {

        userService.changePassword(dto);

        return Result.success("密码修改成功", null);
    }

    @PostMapping("/admin/reset-password/{userId}")
    public Result<Map<String, Object>> resetPassword(
            @PathVariable Long userId) {

        String temporaryPassword = userService.resetPasswordByAdmin(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("temporaryPassword", temporaryPassword);

        return Result.success("密码重置成功", data);
    }

    @PostMapping("/email/send-code")
    public Result<Void> sendEmailCode(
            @RequestParam("email") @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email) {

        emailCodeService.sendCode(email);

        return Result.success("验证码发送成功", null);
    }

    @PostMapping("/email/verify-code")
    public Result<Void> verifyEmailCode(
            @Valid @RequestBody EmailCodeVerifyDTO dto) {

        emailCodeService.verifyCode(
                dto.getEmail(),
                dto.getCode());

        return Result.success("验证码验证成功", null);
    }

    @PostMapping("/forgot-password/send-code")
    public Result<Void> sendForgotPasswordCode(
            @Valid @RequestBody ForgotPasswordDTO dto) {

        emailCodeService.sendForgotPasswordCode(dto.getEmail());

        return Result.success("验证码发送成功", null);
    }

    @PostMapping("/forgot-password/reset")
    public Result<Void> resetPassword(
            @Valid @RequestBody ForgotPasswordResetDTO dto) {

        // 先验证验证码
        emailCodeService.verifyCode(
                dto.getEmail(),
                dto.getCode());

        // 验证成功后修改密码
        userService.resetPasswordByEmail(
                dto.getEmail(),
                dto.getNewPassword());

        return Result.success(
                "密码重置成功，请使用新密码重新登录",
                null);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(
            @Valid @RequestBody UpdateProfileDTO dto) {

        userService.updateCurrentUser(dto);

        return Result.success("个人信息修改成功", null);
    }

    @PutMapping("/admin/user/{userId}")
    public Result<Void> updateUserByAdmin(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileDTO dto) {

        userService.updateUserByAdmin(userId, dto);

        return Result.success("用户信息修改成功", null);
    }
}