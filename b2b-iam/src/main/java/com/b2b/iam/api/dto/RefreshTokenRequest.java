package com.b2b.iam.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 刷新令牌请求参数
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刷新访问令牌请求")
public class RefreshTokenRequest implements Serializable {

    @NotBlank(message = "刷新令牌不可为空")
    @Schema(description = "长期刷新令牌 (Refresh Token)")
    private String refreshToken;

    @Schema(description = "企业组织 ID (可选，若不传则沿用上次企业)", example = "company-lantu")
    private String companyId;
}
