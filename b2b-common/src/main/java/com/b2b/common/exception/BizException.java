package com.b2b.common.exception;

import com.b2b.common.api.ResultCode;
import lombok.Getter;

/**
 * 业务异常基类 (BizException)
 *
 * <p>所有已知业务冲突、规则校验失败均抛出此类，由 {@link GlobalExceptionHandler}
 * 转化为语义明确的 {@link com.b2b.common.api.ApiResponse}，禁止向前端泄露原始异常堆栈。</p>
 *
 * @author b2b-commerce-backend
 */
@Getter
public class BizException extends RuntimeException {

    private final Integer code;
    private final String message;

    /**
     * 基于预定义错误码枚举构建业务异常
     *
     * @param resultCode 错误码枚举
     */
    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 基于预定义错误码枚举与自定义提示消息构建业务异常
     *
     * @param resultCode 错误码枚举
     * @param customMessage 自定义错误提示
     */
    public BizException(ResultCode resultCode, String customMessage) {
        super(customMessage);
        this.code = resultCode.getCode();
        this.message = customMessage;
    }

    /**
     * 自定义错误码与提示消息构建业务异常
     *
     * @param code    错误码
     * @param message 错误提示
     */
    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 使用通用 400 错误码与自定义提示
     *
     * @param message 错误提示
     */
    public BizException(String message) {
        super(message);
        this.code = ResultCode.BAD_REQUEST.getCode();
        this.message = message;
    }
}
