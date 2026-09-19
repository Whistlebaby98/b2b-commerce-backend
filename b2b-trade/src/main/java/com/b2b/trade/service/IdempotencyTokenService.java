package com.b2b.trade.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 结算防重与幂等 Token 服务 (IdempotencyTokenService)
 *
 * <p>遵循 ADR 0007 规范，为结算页下发单次有效 checkout_token，
 * 订单提交时原子核销，优先使用 Redis，无 Redis 环境下自动降级为高并发内存锁。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@Service
public class IdempotencyTokenService {

    private static final String TOKEN_PREFIX = "checkout:token:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redisTemplate;

    /**
     * 本地内存降级缓存 (存储 token -> expireAt)
     */
    private final Map<String, Instant> localTokenStore = new ConcurrentHashMap<>();

    @Autowired
    public IdempotencyTokenService(@Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成并保存单次有效的结算 Token
     *
     * @param companyId 企业 ID
     * @param userId    用户 ID
     * @return checkoutToken 字符串
     */
    public String generateCheckoutToken(String companyId, String userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = TOKEN_PREFIX + token;
        String val = companyId + ":" + userId;

        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(key, val, DEFAULT_TTL);
                log.info("[Idempotency] Redis 下发结算 Token 成功: token={}, companyId={}", token, companyId);
                return token;
            } catch (Exception e) {
                log.warn("[Idempotency] Redis 访问异常，降级使用内存存储: {}", e.getMessage());
            }
        }

        // 内存降级存储
        localTokenStore.put(token, Instant.now().plus(DEFAULT_TTL));
        log.info("[Idempotency] 内存降级下发结算 Token 成功: token={}, companyId={}", token, companyId);
        return token;
    }

    /**
     * 原子核销结算 Token
     *
     * @param token 客户端传入的 checkoutToken
     * @return 核销成功返回 true；Token 不存在或已核销返回 false
     */
    public boolean consumeCheckoutToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String key = TOKEN_PREFIX + token;
        if (redisTemplate != null) {
            try {
                Boolean deleted = redisTemplate.delete(key);
                if (Boolean.TRUE.equals(deleted)) {
                    log.info("[Idempotency] Redis 结算 Token 原子核销成功: token={}", token);
                    return true;
                } else {
                    log.warn("[Idempotency] Redis 结算 Token 核销失败 (已核销或不存在): token={}", token);
                    return false;
                }
            } catch (Exception e) {
                log.warn("[Idempotency] Redis 访问异常，尝试核销内存存储: {}", e.getMessage());
            }
        }

        // 内存原子核销
        Instant expireAt = localTokenStore.remove(token);
        if (expireAt != null && expireAt.isAfter(Instant.now())) {
            log.info("[Idempotency] 内存结算 Token 原子核销成功: token={}", token);
            return true;
        }

        log.warn("[Idempotency] 内存结算 Token 核销失败 (已过期或已核销): token={}", token);
        return false;
    }
}
