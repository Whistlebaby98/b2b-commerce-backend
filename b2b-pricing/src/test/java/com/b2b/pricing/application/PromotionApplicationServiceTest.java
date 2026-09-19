package com.b2b.pricing.application;

import com.b2b.pricing.api.dto.PromotionDTO;
import com.b2b.pricing.infrastructure.persistence.entity.PromotionPO;
import com.b2b.pricing.infrastructure.persistence.mapper.PromotionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 促销活动应用服务单元测试 (PromotionApplicationServiceTest)
 *
 * @author b2b-commerce-backend
 */
@ExtendWith(MockitoExtension.class)
class PromotionApplicationServiceTest {

    @Mock
    private PromotionMapper promotionMapper;

    @InjectMocks
    private PromotionApplicationService promotionApplicationService;

    private PromotionPO samplePO;

    @BeforeEach
    void setUp() {
        samplePO = PromotionPO.builder()
                .id("promo-september-coupon")
                .code("NOVA50")
                .title("九月采购补贴")
                .description("订单满 ¥5,000 减 ¥50。")
                .type("coupon")
                .scope("cart")
                .startsAt(Instant.parse("2026-09-01T00:00:00Z"))
                .endsAt(Instant.parse("2026-09-30T23:59:59Z"))
                .isActive(true)
                .minSubtotal(new BigDecimal("5000.00"))
                .discountAmount(new BigDecimal("50.00"))
                .maxDiscount(new BigDecimal("50.00"))
                .stackable(true)
                .priority(3)
                .build();
    }

    @Test
    @DisplayName("测试查询有效促销活动列表")
    void testListActivePromotions() {
        when(promotionMapper.selectList(any())).thenReturn(List.of(samplePO));

        List<PromotionDTO> list = promotionApplicationService.listActivePromotions();

        assertThat(list).hasSize(1);
        PromotionDTO dto = list.get(0);
        assertThat(dto.getId()).isEqualTo("promo-september-coupon");
        assertThat(dto.getCode()).isEqualTo("NOVA50");
        assertThat(dto.getType()).isEqualTo("coupon");
        assertThat(dto.getDiscountAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("测试根据 ID 查询促销活动详情")
    void testGetPromotionById() {
        when(promotionMapper.selectById("promo-september-coupon")).thenReturn(samplePO);

        PromotionDTO dto = promotionApplicationService.getPromotionById("promo-september-coupon");

        assertThat(dto).isNotNull();
        assertThat(dto.getTitle()).isEqualTo("九月采购补贴");
        assertThat(dto.getMinSubtotal()).isEqualByComparingTo("5000.00");
    }
}
