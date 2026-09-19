package com.b2b.iam.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户登录请求参数
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "买方用户登录请求")
public class LoginRequest implements Serializable {

    @NotBlank(message = "企业邮箱不可为空")
    @Email(message = "企业邮箱格式不正确")
    @Schema(description = "登录企业邮箱", example = "procurement@lantu-mfg.example")
    private String email;

    @NotBlank(message = "登录密码不可为空")
    @Schema(description = "登录密码", example = "123456")
    private String password;
}
