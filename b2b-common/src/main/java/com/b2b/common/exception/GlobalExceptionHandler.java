package com.b2b.common.exception;

import com.b2b.common.api.ApiResponse;
import com.b2b.common.api.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * <p>遵循 ADR 0009 规范，捕获系统异常、参数校验异常及业务异常，
 * 统一包装为 {@link ApiResponse} 响应。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     *
     * @param e 业务异常
     * @return 统一响应
     */
    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException e) {
        log.warn("业务异常触发: code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.failure(e.getCode(), e.getMessage());
    }

    /**
     * 捕获 Spring @Valid 参数绑定校验异常
     *
     * @param e 参数绑定异常
     * @return 统一响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", errorMsg);
        return ApiResponse.failure(ResultCode.BAD_REQUEST.getCode(), errorMsg);
    }

    /**
     * 捕获一般绑定异常
     *
     * @param e 绑定异常
     * @return 统一响应
     */
    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("数据绑定校验失败: {}", errorMsg);
        return ApiResponse.failure(ResultCode.BAD_REQUEST.getCode(), errorMsg);
    }

    /**
     * 捕获约束违规异常
     *
     * @param e 约束异常
     * @return 统一响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("约束校验违规: {}", e.getMessage());
        return ApiResponse.failure(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    /**
     * 捕获未预期的系统兜底异常
     *
     * @param e 未知异常
     * @return 统一响应
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统发生未捕获异常", e);
        return ApiResponse.failure(ResultCode.INTERNAL_SERVER_ERROR);
    }
}
