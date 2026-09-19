package com.b2b.iam.infrastructure.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JWT 工具类单元测试 (JwtUtilsTest)
 *
 * <p>遵循 ADR 0008，验证 Access Token / Refresh Token 生成、验签与载荷解析。</p>
 *
 * @author b2b-commerce-backend
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", "B2BCommercePlatformSecretKeyMustBeLongEnoughForHMACSHA256Signature2026");
        ReflectionTestUtils.setField(jwtUtils, "accessTokenExpirationHours", 2L);
        ReflectionTestUtils.setField(jwtUtils, "refreshTokenExpirationDays", 7L);
    }

    @Test
    @DisplayName("验证 Access Token 签发与声明解析")
    void testAccessTokenGenerationAndParsing() {
        String userId = "user-lin-yue";
        String companyId = "company-lantu";

        String token = jwtUtils.generateAccessToken(userId, companyId);
        assertThat(token).isNotBlank();

        // 验签通过
        assertThat(jwtUtils.validateToken(token)).isTrue();

        // 提取用户标识
        assertThat(jwtUtils.getUserIdFromToken(token)).isEqualTo(userId);

        // 解析完整 Claims
        Claims claims = jwtUtils.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(userId);
        assertThat(claims.get("companyId", String.class)).isEqualTo(companyId);
        assertThat(claims.get("tokenType", String.class)).isEqualTo("ACCESS");
    }

    @Test
    @DisplayName("验证 Refresh Token 签发与声明解析")
    void testRefreshTokenGenerationAndParsing() {
        String userId = "user-lin-yue";

        String token = jwtUtils.generateRefreshToken(userId);
        assertThat(token).isNotBlank();
        assertThat(jwtUtils.validateToken(token)).isTrue();

        Claims claims = jwtUtils.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(userId);
        assertThat(claims.get("tokenType", String.class)).isEqualTo("REFRESH");
    }

    @Test
    @DisplayName("验证非法或被篡改的 Token 无法通过验签")
    void testInvalidTokenVerification() {
        String validToken = jwtUtils.generateAccessToken("user-test", "company-test");
        String tamperedToken = validToken + "tampered";

        assertThat(jwtUtils.validateToken(tamperedToken)).isFalse();
        assertThat(jwtUtils.validateToken("invalid.jwt.token")).isFalse();
        assertThat(jwtUtils.validateToken("")).isFalse();
        assertThat(jwtUtils.validateToken(null)).isFalse();
    }
}
