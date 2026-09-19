package com.b2b.iam.infrastructure.security;

import com.b2b.common.api.ApiResponse;
import com.b2b.common.api.ResultCode;
import com.b2b.common.tenant.TenantContext;
import com.b2b.common.tenant.TenantContextHolder;
import com.b2b.iam.infrastructure.persistence.entity.MembershipPO;
import com.b2b.iam.infrastructure.persistence.mapper.MembershipMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 多租户安全与鉴权过滤器 (TenantSecurityFilter)
 *
 * <p>遵循 ADR 0005 & ADR 0008 规范，拦截所有受保护的 HTTP 请求：
 * 1. 从 Authorization 头提取并验证 JWT 访问令牌；
 * 2. 从 X-Organization-Id 头提取目标企业组织 ID，并校验该买方的组织隶属资格；
 * 3. 成功后绑定至 {@link TenantContextHolder}，请求结束在 finally 块安全清理。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantSecurityFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ORG_HEADER = "X-Organization-Id";

    private final JwtUtils jwtUtils;
    private final MembershipMapper membershipMapper;
    private final ObjectMapper objectMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 白名单接口路径（无需 Bearer Token 与 X-Organization-Id 拦截）
     */
    private static final List<String> WHITE_LIST = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh-token",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/**"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 提取并校验 Authorization Header
            String authHeader = request.getHeader(AUTH_HEADER);
            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, ResultCode.UNAUTHORIZED);
                return;
            }

            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            if (!jwtUtils.validateToken(token)) {
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, ResultCode.UNAUTHORIZED);
                return;
            }

            String userId = jwtUtils.getUserIdFromToken(token);
            if (!StringUtils.hasText(userId)) {
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, ResultCode.UNAUTHORIZED);
                return;
            }

            // 2. 提取 X-Organization-Id Header
            String companyId = request.getHeader(ORG_HEADER);
            if (!StringUtils.hasText(companyId)) {
                writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, ResultCode.MISSING_TENANT_HEADER);
                return;
            }

            // 3. 校验该买方用户是否在当前企业拥有合法隶属资格 (org_membership)
            MembershipPO membership = membershipMapper.selectOne(
                    new LambdaQueryWrapper<MembershipPO>()
                            .eq(MembershipPO::getUserId, userId)
                            .eq(MembershipPO::getCompanyId, companyId)
            );

            if (membership == null) {
                log.warn("用户越权访问拦截: userId={}, companyId={}", userId, companyId);
                writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, ResultCode.MEMBERSHIP_INVALID);
                return;
            }

            // 4. 将经鉴权认证的企业上下文安全绑定至当前线程 (基于 TTL)
            TenantContext context = TenantContext.builder()
                    .userId(userId)
                    .companyId(companyId)
                    .role(membership.getRole())
                    .build();
            TenantContextHolder.setContext(context);

            // 5. 放行请求
            filterChain.doFilter(request, response);

        } finally {
            // 必须在 finally 块中清理 ThreadLocal，防止线程池复用导致的上下文泄露
            TenantContextHolder.clear();
        }
    }

    private void writeErrorResponse(HttpServletResponse response, int httpStatus, ResultCode resultCode) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ApiResponse<Void> apiResponse = ApiResponse.failure(resultCode);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
