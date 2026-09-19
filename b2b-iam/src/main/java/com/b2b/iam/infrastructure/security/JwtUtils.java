package com.b2b.iam.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/**
 * JWT 令牌工具类 (JwtUtils)
 *
 * <p>遵循 ADR 0008 规范，基于 JJWT 0.12.x 提供无状态双 Token（Access Token 与 Refresh Token）
 * 的签名生成、验签与载荷声明解析服务。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@Component
public class JwtUtils {

    /**
     * 签名密钥 (默认 256 位 HMAC 密钥，生产可通过环境变量覆盖)
     */
    @Value("${b2b.jwt.secret:B2BCommercePlatformSecretKeyMustBeLongEnoughForHMACSHA256Signature2026}")
    private String secret;

    /**
     * Access Token 有效期 (默认 2 小时)
     */
    @Value("${b2b.jwt.access-token-expiration-hours:2}")
    private long accessTokenExpirationHours;

    /**
     * Refresh Token 有效期 (默认 7 天)
     */
    @Value("${b2b.jwt.refresh-token-expiration-days:7}")
    private long refreshTokenExpirationDays;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成买方访问令牌 (Access Token)
     *
     * @param userId    买方用户 ID
     * @param companyId 当前选定的企业组织 ID
     * @return 签名后的 JWT 字符串
     */
    public String generateAccessToken(String userId, String companyId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessTokenExpirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(userId)
                .claim("companyId", companyId)
                .claim("tokenType", "ACCESS")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成长期刷新令牌 (Refresh Token)
     *
     * @param userId 买方用户 ID
     * @return 签名后的 JWT 字符串
     */
    public String generateRefreshToken(String userId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(userId)
                .claim("tokenType", "REFRESH")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析并校验 JWT 令牌
     *
     * @param token 待校验的 JWT 字符串
     * @return 令牌中的 Claims 声明载荷
     * @throws JwtException 当令牌过期、签名不匹配或伪造时抛出
     */
    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 快速校验令牌是否合法且未过期
     *
     * @param token JWT 字符串
     * @return 合法返回 true，非法或过期返回 false
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 校验未通过: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从令牌中直接提取用户 ID
     *
     * @param token JWT 字符串
     * @return 用户 ID，解析失败返回 null
     */
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
