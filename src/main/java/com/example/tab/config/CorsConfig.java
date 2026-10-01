package com.example.tab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // 允许的前端来源（开发阶段使用 allowedOriginPatterns 来兼容带凭证的跨域）
                .allowedOriginPatterns("*")
                // 允许的请求方式
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头（确保自定义的 satoken 头部可以通过）
                .allowedHeaders("*")
                // 暴露的响应头
                .exposedHeaders("satoken")
                // 允许携带 Cookie / Authentication 凭证
                .allowCredentials(true)
                // 跨域预检请求（OPTIONS）的缓存时间（秒）
                .maxAge(3600);
    }
}