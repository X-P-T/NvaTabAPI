package com.example.tab.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.example.tab.exception.BusinessException;
import com.example.tab.mapper.UserMapper;
import com.example.tab.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class SaTokenConfigure implements WebMvcConfigurer {

    private final UserMapper userMapper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(
                new SaInterceptor(handle -> {

                    // 1. 检查是否登录
                    StpUtil.checkLogin();

                    // 2. 获取当前用户 ID
                    Long userId = StpUtil.getLoginIdAsLong();

                    // 3. 查询当前用户
                    User user = userMapper.selectById(userId);

                    if (user == null) {
                        StpUtil.logout();

                        throw new BusinessException(
                                401,
                                "用户不存在，请重新登录"
                        );
                    }

                    // 4. 判断是否必须修改密码
                    boolean mustChangePassword =
                            user.getMustChangePassword() != null
                                    && user.getMustChangePassword() == 1;

                    if (mustChangePassword) {

                        // 获取当前请求路径
                        String requestPath =
                                SaHolder.getRequest().getRequestPath();

                        // 临时密码状态下，只允许修改密码
                        if (!"/api/auth/password".equals(requestPath)) {

                            throw new BusinessException(
                                    403,
                                    "当前账号必须先修改密码"
                            );
                        }
                    }
                }))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/forgot-password/send-code",
                        "/api/auth/forgot-password/reset",
                        "/api/nav/tree",
                        "/api/nav/site/click/*");
    }
}