package com.example.tab.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获 Spring Boot 3+ 的 404 静态资源/接口未找到异常 (NoResourceFoundException)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Map<String, Object> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("访问了不存在的接口路径: {} {}", e.getHttpMethod(), e.getResourcePath());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 404);
        result.put("errorType", e.getClass().getName());
        result.put("message", "请求的接口路径不存在 [" + e.getHttpMethod() + " /" + e.getResourcePath() + "]");
        return result;
    }

    /**
     * 捕获其他全局未处理异常 (500)
     */
    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception e) {
        log.error("捕获到全局未处理异常:", e);

        Throwable rootCause = e;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("errorType", e.getClass().getName());
        result.put("message", rootCause.getMessage());
        return result;
    }
}