package com.b2b.trade.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 结算防重 Token 幂等核销测试 (IdempotencyTokenServiceTest)
 *
 * <p>遵循 ADR 0007，验证 Token 生成、单次原子核销与防重复提交机制。</p>
 *
 * @author b2b-commerce-backend
 */
class IdempotencyTokenServiceTest {

    private IdempotencyTokenService idempotencyTokenService;

    @BeforeEach
    void setUp() {
        // 传入 null 模拟无 Redis 环境下的内存降级并发控制
        idempotencyTokenService = new IdempotencyTokenService(null);
    }

    @Test
    @DisplayName("验证结算 Token 生成与单次原子核销")
    void testTokenGenerationAndAtomicConsumption() {
        String companyId = "company-lantu";
        String userId = "user-lin-yue";

        // 1. 生成结算 Token
        String token = idempotencyTokenService.generateCheckoutToken(companyId, userId);
        assertThat(token).isNotBlank();

        // 2. 第一次核销必须成功
        boolean firstConsume = idempotencyTokenService.consumeCheckoutToken(token);
        assertThat(firstConsume).isTrue();

        // 3. 第二次重复核销必须失败 (防止表单重复提交)
        boolean secondConsume = idempotencyTokenService.consumeCheckoutToken(token);
        assertThat(secondConsume).isFalse();
    }

    @Test
    @DisplayName("验证伪造或空 Token 核销均返回 false")
    void testInvalidTokenConsumption() {
        assertThat(idempotencyTokenService.consumeCheckoutToken("fake_token_123")).isFalse();
        assertThat(idempotencyTokenService.consumeCheckoutToken("")).isFalse();
        assertThat(idempotencyTokenService.consumeCheckoutToken(null)).isFalse();
    }
}
