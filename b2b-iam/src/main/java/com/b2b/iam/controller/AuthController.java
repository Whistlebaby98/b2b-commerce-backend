package com.b2b.iam.controller;

import com.b2b.common.api.ApiResponse;
import com.b2b.common.tenant.TenantContextHolder;
import com.b2b.iam.api.dto.LoginRequest;
import com.b2b.iam.api.dto.LoginResponse;
import com.b2b.iam.api.dto.RefreshTokenRequest;
import com.b2b.iam.api.dto.SessionSnapshotResponse;
import com.b2b.iam.api.dto.SwitchContextRequest;
import com.b2b.iam.application.AuthApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 组织与身份认证控制器 (AuthController)
 *
 * <p>提供买方用户登录、企业组织上下文切换、会话状态快照与令牌刷新接口。</p>
 *
 * @author b2b-commerce-backend
 */
@Tag(name = "00. 组织与认证中心", description = "买方登录认证、双 Token 颁发、企业组织上下文切换与会话快照")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authApplicationService;

    /**
     * 买方用户账号密码登录
     *
     * @param request 登录入参（企业邮箱 + 密码）
     * @return 包含访问令牌、买方档案及可选企业列表
     */
    @Operation(summary = "买方用户登录", description = "使用企业邮箱与密码登录，成功后颁发双 Token 与当前企业上下文")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authApplicationService.login(request);
        return ApiResponse.success(response);
    }

    /**
     * 获取当前买方会话快照 (SessionSnapshot)
     *
     * @return 会话状态数据（对齐前端 SessionState）
     */
    @Operation(summary = "获取当前会话快照", description = "根据请求头携带的 Token 与 X-Organization-Id 获取当前会话状态")
    @GetMapping("/session")
    public ApiResponse<SessionSnapshotResponse> getSession() {
        String userId = TenantContextHolder.getUserId();
        String companyId = TenantContextHolder.getCompanyId();
        SessionSnapshotResponse response = authApplicationService.getSession(userId, companyId);
        return ApiResponse.success(response);
    }

    /**
     * 切换当前代表的企业组织上下文
     *
     * @param request 目标企业 ID 请求
     * @return 重新签发绑定新企业的访问令牌
     */
    @Operation(summary = "切换企业组织上下文", description = "在用户所属的多家企业间切换代表企业，生成新上下文令牌")
    @PostMapping("/switch-context")
    public ApiResponse<LoginResponse> switchOrganization(@Valid @RequestBody SwitchContextRequest request) {
        String userId = TenantContextHolder.getUserId();
        LoginResponse response = authApplicationService.switchOrganization(userId, request);
        return ApiResponse.success(response);
    }

    /**
     * 刷新访问令牌 (Access Token)
     *
     * @param request 刷新令牌请求
     * @return 新生成的访问令牌
     */
    @Operation(summary = "刷新访问令牌", description = "使用长期 Refresh Token 换取新的短期 Access Token")
    @PostMapping("/refresh-token")
    public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authApplicationService.refreshToken(request);
        return ApiResponse.success(response);
    }
}
