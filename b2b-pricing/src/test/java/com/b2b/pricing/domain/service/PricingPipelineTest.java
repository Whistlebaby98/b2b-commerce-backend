package com.b2b.pricing.domain.service;

import com.b2b.pricing.api.dto.PriceTierDTO;
import com.b2b.pricing.api.dto.PricingSnapshotDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 计价流水线纯领域单元测试 (PricingPipelineTest)
 *
 * <p>遵循 ADR 0006，验证纯内存阶梯算价、区间匹配与不可变快照生成正确性。</p>
 *
 * @author b2b-commerce-backend
 */
class PricingPipelineTest {

    private PricingPipeline pricingPipeline;

    @BeforeEach
    void setUp() {
        pricingPipeline = new PricingPipeline();
    }

    @Test
    @DisplayName("验证阶梯价匹配：采购量处于不同阶梯区间时，正确匹配单价")
    void testTierPriceMatching() {
        BigDecimal basePrice = new BigDecimal("100.00");
        List<PriceTierDTO> tiers = List.of(
                PriceTierDTO.builder().minQuantity(1).unitPrice(new BigDecimal("100.00")).label("协议价").build(),
                PriceTierDTO.builder().minQuantity(10).unitPrice(new BigDecimal("90.00")).label("10+ 阶梯价").build(),
                PriceTierDTO.builder().minQuantity(50).unitPrice(new BigDecimal("80.00")).label("50+ 阶梯价").build()
        );

        // 1. 采购 1 件 -> 协议价 100.00
        BigDecimal price1 = pricingPipeline.calculateTierUnitPrice(basePrice, tiers, 1);
        assertThat(price1).isEqualByComparingTo("100.00");

        // 2. 采购 9 件 -> 依然是协议价 100.00
        BigDecimal price9 = pricingPipeline.calculateTierUnitPrice(basePrice, tiers, 9);
        assertThat(price9).isEqualByComparingTo("100.00");

        // 3. 采购 10 件 -> 命中 10+ 阶梯价 90.00
        BigDecimal price10 = pricingPipeline.calculateTierUnitPrice(basePrice, tiers, 10);
        assertThat(price10).isEqualByComparingTo("90.00");

        // 4. 采购 49 件 -> 依然是 90.00
        BigDecimal price49 = pricingPipeline.calculateTierUnitPrice(basePrice, tiers, 49);
        assertThat(price49).isEqualByComparingTo("90.00");

        // 5. 采购 50 件及以上 -> 命中 50+ 阶梯价 80.00
        BigDecimal price50 = pricingPipeline.calculateTierUnitPrice(basePrice, tiers, 50);
        assertThat(price50).isEqualByComparingTo("80.00");
    }

    @Test
    @DisplayName("验证无阶梯或异常数量时，回退到商品基准协议单价")
    void testFallbackToBasePrice() {
        BigDecimal basePrice = new BigDecimal("150.00");

        // 阶梯列表为空
        BigDecimal priceNoTiers = pricingPipeline.calculateTierUnitPrice(basePrice, null, 10);
        assertThat(priceNoTiers).isEqualByComparingTo("150.00");

        // 采购数量非法 (<= 0)
        BigDecimal priceInvalidQty = pricingPipeline.calculateTierUnitPrice(basePrice, List.of(), 0);
        assertThat(priceInvalidQty).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("验证可解释算价快照生成与金额汇总计算")
    void testBuildSnapshot() {
        List<PriceTierDTO> tiers1 = List.of(
                PriceTierDTO.builder().minQuantity(1).unitPrice(new BigDecimal("26.50")).label("协议价").build(),
                PriceTierDTO.builder().minQuantity(10).unitPrice(new BigDecimal("24.00")).label("10+ 阶梯价").build()
        );

        List<PriceTierDTO> tiers2 = List.of(
                PriceTierDTO.builder().minQuantity(1).unitPrice(new BigDecimal("156.00")).label("协议价").build()
        );

        PricingPipeline.PricingItemContext item1 = PricingPipeline.PricingItemContext.builder()
                .skuId("sku-nsk-6205")
                .quantity(12) // 命中 10+ 阶梯价 24.00, 原价 32.00
                .basePrice(new BigDecimal("26.50"))
                .listPrice(new BigDecimal("32.00"))
                .tiers(tiers1)
                .build();

        PricingPipeline.PricingItemContext item2 = PricingPipeline.PricingItemContext.builder()
                .skuId("sku-sick-wl12g3")
                .quantity(2) // 协议价 156.00, 原价 179.00
                .basePrice(new BigDecimal("156.00"))
                .listPrice(new BigDecimal("179.00"))
                .tiers(tiers2)
                .build();

        PricingSnapshotDTO snapshot = pricingPipeline.buildSnapshot(List.of(item1, item2));

        // 行 1 小计: 24.00 * 12 = 288.00; 原价小计: 32.00 * 12 = 384.00
        // 行 2 小计: 156.00 * 2 = 312.00; 原价小计: 179.00 * 2 = 358.00
        // 成交小计 (subtotal): 288.00 + 312.00 = 600.00
        // 原价小计 (listSubtotal): 384.00 + 358.00 = 742.00
        // 优惠减免 (promotionDiscount): 742.00 - 600.00 = 142.00
        assertThat(snapshot.getSubtotal()).isEqualByComparingTo("600.00");
        assertThat(snapshot.getListSubtotal()).isEqualByComparingTo("742.00");
        assertThat(snapshot.getPromotionDiscount()).isEqualByComparingTo("142.00");
        assertThat(snapshot.getTotal()).isEqualByComparingTo("600.00");
        assertThat(snapshot.getLineSnapshots()).hasSize(2);

        // 验证行级快照明细
        PricingSnapshotDTO.LinePricingSnapshot line1 = snapshot.getLineSnapshots().get(0);
        assertThat(line1.getSkuId()).isEqualTo("sku-nsk-6205");
        assertThat(line1.getFinalUnitPrice()).isEqualByComparingTo("24.00");
        assertThat(line1.getMatchedTierMinQuantity()).isEqualTo(10);
        assertThat(line1.getTierLabel()).isEqualTo("10+ 阶梯价");
        assertThat(line1.getLineTotal()).isEqualByComparingTo("288.00");
    }
}
