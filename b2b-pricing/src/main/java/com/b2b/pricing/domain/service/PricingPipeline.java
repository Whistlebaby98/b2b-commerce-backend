package com.b2b.pricing.domain.service;

import com.b2b.pricing.api.dto.PriceTierDTO;
import com.b2b.pricing.api.dto.PricingSnapshotDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 纯领域无副作用算价流水线 (PricingPipeline)
 *
 * <p>遵循 ADR 0006 规范，本类为纯内存函数计算，无任何外部 I/O 依赖：
 * 接收已批量查询出的商品基价、阶梯规则与购买数量，按规则执行阶梯区间命中、
 * 优惠分摊与金额汇总，输出具备对账审计能力的可解释算价快照。</p>
 *
 * @author b2b-commerce-backend
 */
@Component
public class PricingPipeline {

    /**
     * 纯函数：根据采购数量匹配阶梯成交单价
     *
     * <p>阶梯匹配规则说明（与前端 domain.ts getTierUnitPrice 严格对称）：
     * 1. 阶梯按起订量 minQuantity 升序排列；
     * 2. 筛选出所有满足 {@code quantity >= minQuantity} 的阶梯；
     * 3. 取门槛最高（最后一个）的阶梯所标定的 unitPrice 作为成交单价；
     * 4. 若未命中任何阶梯，回退至商品基准协议单价 basePrice。</p>
     *
     * @param basePrice 协议基准单价
     * @param tiers     该 SKU 拥有的全部阶梯列表
     * @param quantity  采购数量
     * @return 计算后的成交单价
     */
    public BigDecimal calculateTierUnitPrice(BigDecimal basePrice, List<PriceTierDTO> tiers, int quantity) {
        if (tiers == null || tiers.isEmpty() || quantity <= 0) {
            return basePrice;
        }

        PriceTierDTO matched = matchTier(tiers, quantity);
        return matched != null ? matched.getUnitPrice() : basePrice;
    }

    /**
     * 匹配特定采购量命中的具体阶梯对象
     *
     * @param tiers    阶梯价格规则列表
     * @param quantity 采购数量
     * @return 命中的阶梯对象，未命中返回 null
     */
    public PriceTierDTO matchTier(List<PriceTierDTO> tiers, int quantity) {
        if (tiers == null || tiers.isEmpty() || quantity <= 0) {
            return null;
        }

        return tiers.stream()
                .filter(t -> t.getMinQuantity() != null && quantity >= t.getMinQuantity())
                .max(Comparator.comparingInt(PriceTierDTO::getMinQuantity))
                .orElse(null);
    }

    /**
     * 构建包含行级轨迹的可解释算价快照
     *
     * @param items 待计价的明细项数据（包含 skuId, quantity, basePrice, listPrice, tiers）
     * @return 完整的算价快照 DTO
     */
    public PricingSnapshotDTO buildSnapshot(List<PricingItemContext> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal listSubtotal = BigDecimal.ZERO;
        List<PricingSnapshotDTO.LinePricingSnapshot> lineSnapshots = new ArrayList<>();

        for (PricingItemContext item : items) {
            int qty = item.getQuantity();
            BigDecimal basePrice = item.getBasePrice();
            BigDecimal listPrice = item.getListPrice() != null ? item.getListPrice() : basePrice;

            // 1. 命中阶梯价计算
            PriceTierDTO matchedTier = matchTier(item.getTiers(), qty);
            BigDecimal finalUnitPrice = matchedTier != null ? matchedTier.getUnitPrice() : basePrice;

            // 2. 单行金额计算 (保留 2 位小数，四舍五入)
            BigDecimal lineTotal = finalUnitPrice.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineListTotal = listPrice.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

            subtotal = subtotal.add(lineTotal);
            listSubtotal = listSubtotal.add(lineListTotal);

            // 3. 构建单行算价轨迹快照
            lineSnapshots.add(PricingSnapshotDTO.LinePricingSnapshot.builder()
                    .skuId(item.getSkuId())
                    .quantity(qty)
                    .listUnitPrice(listPrice)
                    .baseContractPrice(basePrice)
                    .matchedTierMinQuantity(matchedTier != null ? matchedTier.getMinQuantity() : 1)
                    .matchedTierUnitPrice(finalUnitPrice)
                    .tierLabel(matchedTier != null ? matchedTier.getLabel() : "协议价")
                    .promotionDiscountPerUnit(BigDecimal.ZERO)
                    .finalUnitPrice(finalUnitPrice)
                    .lineTotal(lineTotal)
                    .build());
        }

        // 优惠总额 = 原价总额 - 成交总额
        BigDecimal promotionDiscount = listSubtotal.subtract(subtotal).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        return PricingSnapshotDTO.builder()
                .subtotal(subtotal)
                .listSubtotal(listSubtotal)
                .promotionDiscount(promotionDiscount)
                .shippingFee(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .total(subtotal)
                .currency("CNY")
                .lineSnapshots(lineSnapshots)
                .build();
    }

    /**
     * 算价流水线内存上下文载荷
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PricingItemContext {
        private String skuId;
        private Integer quantity;
        private BigDecimal basePrice;
        private BigDecimal listPrice;
        private List<PriceTierDTO> tiers;
    }
}
