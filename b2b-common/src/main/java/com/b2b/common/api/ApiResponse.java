package com.b2b.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一 RESTful API 响应信封对象
 *
 * <p>遵循 ADR 0009 架构规范，所有 Controller 层响应必须由此对象统一包装，
 * 包含状态码、业务提示、数据载荷与响应时间戳。</p>
 *
 * @param <T> 业务数据泛型
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "全局统一 API 响应信封")
public class ApiResponse<T> implements Serializable {

    @Schema(description = "业务响应码，200 为成功", example = "200")
    private Integer code;

    @Schema(description = "响应消息或业务提示", example = "success")
    private String message;

    @Schema(description = "业务数据载荷")
    private T data;

    @Schema(description = "响应时间戳 (毫秒)", example = "1726758400000")
    private Long timestamp;

    /**
     * 构建成功响应（带数据）
     *
     * @param data 响应数据载荷
     * @param <T>  数据类型
     * @return 统一响应实体
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构建无数据的成功响应
     *
     * @param <T> 数据类型
     * @return 统一响应实体
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 构建指定错误码与提示的失败响应
     *
     * @param code    错误码
     * @param message 错误提示
     * @param <T>     数据类型
     * @return 统一响应实体
     */
    public static <T> ApiResponse<T> failure(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构建标准枚举的失败响应
     *
     * @param resultCode 错误码枚举
     * @param <T>        数据类型
     * @return 统一响应实体
     */
    public static <T> ApiResponse<T> failure(ResultCode resultCode) {
        return failure(resultCode.getCode(), resultCode.getMessage());
    }
}
