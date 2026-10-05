package com.example.tab.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgotPasswordResetDTO {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Pattern(
            regexp = "^\\d{6}$",
            message = "验证码必须为6位数字"
    )
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Size(
            min = 8,
            max = 32,
            message = "新密码长度必须为8-32位"
    )
    private String newPassword;
}