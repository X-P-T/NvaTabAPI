package com.example.tab.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.example.tab.common.Result;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        /**
         * 统一构建异常响应
         */
        private ResponseEntity<Result<Void>> buildResponse(
                        int code, String message) {

                return ResponseEntity
                                .status(code)
                                .body(Result.error(code, message));
        }

        /**
         * 处理业务异常
         */
        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<Result<Void>> handleBusinessException(
                        BusinessException e) {

                log.warn("业务异常: {}", e.getMessage());

                return buildResponse(e.getCode(), e.getMessage());
        }

        /**
         * 处理未登录异常
         */
        @ExceptionHandler(NotLoginException.class)
        public ResponseEntity<Result<Void>> handleNotLoginException(
                        NotLoginException e) {

                log.warn("用户未登录: {}", e.getMessage());

                return buildResponse(401, "请先登录或重新登录");
        }

        /**
         * 处理请求参数校验异常
         *
         * 例如：
         * 
         * @NotBlank
         * @NotNull
         * @Size
         * @Min
         *      等 Bean Validation 校验失败
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Result<Void>> handleValidationException(
                        MethodArgumentNotValidException e) {

                String message = e.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getDefaultMessage())
                                .findFirst()
                                .orElse("请求参数校验失败");

                log.warn("请求参数校验失败: {}", message);

                return buildResponse(400, message);
        }

        /**
         * 处理 JSON 请求体格式错误
         */
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Result<Void>> handleHttpMessageNotReadableException(
                        HttpMessageNotReadableException e) {

                log.warn("请求 JSON 格式错误: {}", e.getMessage());

                Throwable cause = e.getCause();

                if (cause instanceof UnrecognizedPropertyException ex) {

                        String fieldName = ex.getPropertyName();

                        return buildResponse(
                                        400,
                                        "不允许修改字段：" + fieldName);
                }

                return buildResponse(
                                400,
                                "请求参数格式错误，请检查 JSON 数据");
        }

        /**
         * 处理路径参数 / 请求参数类型错误
         */
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<Result<Void>> handleTypeMismatchException(
                        MethodArgumentTypeMismatchException e) {

                log.warn("请求参数类型错误: 参数={}, 值={}",
                                e.getName(), e.getValue());

                return buildResponse(
                                400,
                                "请求参数类型错误，请检查参数格式");
        }

        /**
         * 处理缺少请求参数
         */
        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<Result<Void>> handleMissingParameterException(
                        MissingServletRequestParameterException e) {

                log.warn("缺少请求参数: {}", e.getParameterName());

                return buildResponse(
                                400,
                                "缺少请求参数: " + e.getParameterName());
        }

        /**
         * 处理 HTTP 请求方法错误
         */
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<Result<Void>> handleMethodNotSupportedException(
                        HttpRequestMethodNotSupportedException e) {

                log.warn("不支持的请求方法: {}", e.getMethod());

                return buildResponse(
                                405,
                                "不支持的请求方法: " + e.getMethod());
        }

        /**
         * 处理不存在的接口
         */
        @ExceptionHandler(NoHandlerFoundException.class)
        public ResponseEntity<Result<Void>> handleNoHandlerFoundException(
                        NoHandlerFoundException e) {

                log.warn("访问了不存在的接口: {} {}",
                                e.getHttpMethod(),
                                e.getRequestURL());

                return buildResponse(
                                404,
                                "请求的接口不存在");
        }

        /**
         * 处理不存在的接口或静态资源
         */
        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<Result<Void>> handleNoResourceFoundException(
                        NoResourceFoundException e) {

                log.warn("访问了不存在的路径: {} {}",
                                e.getHttpMethod(),
                                e.getResourcePath());

                return buildResponse(
                                404,
                                "请求的接口路径不存在");
        }

        /**
         * 处理其他未预期异常
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Result<Void>> handleException(
                        Exception e) {

                log.error("捕获到全局未处理异常:", e);

                return buildResponse(
                                500,
                                "服务器内部错误，请稍后重试");
        }

        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<Result<Void>> handleHttpMediaTypeNotSupportedException(
                        HttpMediaTypeNotSupportedException e) {

                log.warn("不支持的请求媒体类型: {}", e.getContentType());

                return buildResponse(415, "不支持的请求数据格式");
        }
}