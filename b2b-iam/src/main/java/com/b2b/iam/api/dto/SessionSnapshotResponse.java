package com.b2b.iam.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 前端会话快照响应数据 (SessionSnapshotResponse)
 *
 * <p>对齐前端 store-provider.tsx 中 SessionState 契约，
 * 包含认证状态、登录邮箱、当前买方用户档案与当前企业组织。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "当前买方会话快照")
public class SessionSnapshotResponse implements Serializable {

    @Schema(description = "是否已认证", example = "true")
    private Boolean authenticated;

    @Schema(description = "登录企业邮箱", example = "procurement@lantu-mfg.example")
    private String email;

    @Schema(description = "买方用户信息")
    private UserDTO user;

    @Schema(description = "当前企业组织信息 (ActiveOrganizationContext)")
    private OrganizationDTO company;
}
