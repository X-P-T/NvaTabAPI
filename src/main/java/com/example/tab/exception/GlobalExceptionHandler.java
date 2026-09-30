package com.example.tab.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception e) {
        log.error("捕获到全局未处理异常:", e);

        // 循环递归获取最底层的根本原因 (Root Cause)
        Throwable rootCause = e;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("errorType", e.getClass().getName());
        // 直接输出最底层的异常描述（如：Unknown database 或 Column not found）
        result.put("message", rootCause.getMessage());
        return result;
    }
}