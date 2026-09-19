package com.b2b.iam.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 买方用户注册请求参数 (RegisterRequest)
 *
 * <p>用于买方用户在门户自主注册企业采购账号。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "买方用户注册请求")
public class RegisterRequest implements Serializable {

    @Schema(description = "联系人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "林悦")
    @NotBlank(message = "联系人姓名不能为空")
    private String name;

    @Schema(description = "企业邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "lin.yue@example.com")
    @NotBlank(message = "企业邮箱不能为空")
    @Email(message = "请输入有效的企业邮箱")
    private String email;

    @Schema(description = "登录密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "demo1234")
    @NotBlank(message = "登录密码不能为空")
    @Size(min = 6, message = "密码至少需要 6 位字符")
    private String password;

    @Schema(description = "企业名称 (可选，默认加入示范企业或按姓名创建)", example = "深圳市蓝图精密制造有限公司")
    private String companyName;
}
