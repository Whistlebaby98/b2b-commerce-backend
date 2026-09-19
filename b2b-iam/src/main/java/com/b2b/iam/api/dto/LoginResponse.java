package com.b2b.iam.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户登录成功响应载荷
 *
 * <p>遵循 ADR 0008 规范，包含访问令牌、刷新令牌、当前买方用户档案、当前企业及关联的所有可选企业。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录成功响应数据")
public class LoginResponse implements Serializable {

    @Schema(description = "短期访问令牌 (JWT Access Token, 2小时有效)")
    private String accessToken;

    @Schema(description = "长期刷新令牌 (JWT Refresh Token, 7天有效)")
    private String refreshToken;

    @Schema(description = "访问令牌有效期 (秒)", example = "7200")
    private Long expiresIn;

    @Schema(description = "买方用户信息")
    private UserDTO user;

    @Schema(description = "当前选定的企业组织信息 (ActiveOrganizationContext)")
    private OrganizationDTO currentCompany;

    @Schema(description = "该用户有权代表的所有企业组织列表")
    private List<OrganizationDTO> availableCompanies;
}
