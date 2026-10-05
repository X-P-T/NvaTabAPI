package com.example.tab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tab.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.example.tab.mapper.UserMapper;
import com.example.tab.model.entity.User;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailCodeService {
    private final UserMapper userMapper;

    private static final String CODE_PREFIX = "email:code:";
    private static final String COOLDOWN_PREFIX = "email:code:cooldown:";

    private final StringRedisTemplate stringRedisTemplate;
    private final MailService mailService;

    /**
     * 发送邮箱验证码
     */
    public void sendCode(String email) {

        String cooldownKey = COOLDOWN_PREFIX + email;

        // 检查 60 秒发送冷却
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            throw new BusinessException(
                    429,
                    "验证码发送过于频繁，请60秒后再试");
        }

        // 使用安全随机数生成 6 位验证码
        SecureRandom random = new SecureRandom();

        String code = String.format(
                "%06d",
                random.nextInt(1_000_000));

        // Redis Key
        String codeKey = CODE_PREFIX + email;

        // 保存验证码，5 分钟后自动过期
        stringRedisTemplate.opsForValue()
                .set(
                        codeKey,
                        code,
                        5,
                        TimeUnit.MINUTES);

        String content = "您好！\n\n"
                + "您的 NvaTabAPI 验证码是："
                + code
                + "\n\n"
                + "验证码有效期为 5 分钟，请勿将验证码泄露给他人。";

        try {

            // 发送邮件
            mailService.sendSimpleMail(
                    email,
                    "NvaTabAPI 邮箱验证码",
                    content);

            // 邮件发送成功后，设置 60 秒冷却
            stringRedisTemplate.opsForValue()
                    .set(
                            cooldownKey,
                            "1",
                            60,
                            TimeUnit.SECONDS);

        } catch (Exception e) {

            // 邮件发送失败，删除已经保存的验证码
            stringRedisTemplate.delete(codeKey);

            throw e;
        }
    }

    /**
     * 验证邮箱验证码
     */
    public void verifyCode(String email, String code) {

        String codeKey = CODE_PREFIX + email;

        String savedCode = stringRedisTemplate.opsForValue()
                .get(codeKey);

        if (savedCode == null) {
            throw new BusinessException(
                    400,
                    "验证码不存在或已过期");
        }

        if (!savedCode.equals(code)) {
            throw new BusinessException(
                    400,
                    "验证码错误");
        }

        // 验证成功后立即删除验证码
        stringRedisTemplate.delete(codeKey);
    }

    /**
     * 忘记密码发送验证码
     */
    public void sendForgotPasswordCode(String email) {

        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getEmail, email);

        User user = userMapper.selectOne(query);

        if (user == null) {
            // 为了防止邮箱枚举，不告诉用户邮箱是否存在
            return;
        }

        sendCode(email);
    }
}