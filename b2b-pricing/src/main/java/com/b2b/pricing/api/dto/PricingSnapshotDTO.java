package com.b2b.pricing.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 可解释算价快照数据传输对象 (PricingSnapshot DTO)
 *
 * <p>遵循 ADR 0006 规范，包含单品成交单价、阶梯命中区间、优惠分摊明细及总省钱金额，
 * 订单提交时固化存入 order_line，绝对不可动态回算。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单算价快照与明细")
public class PricingSnapshotDTO implements Serializable {

    @Schema(description = "商品行项目小计金额", example = "15360.00")
    private BigDecimal subtotal;

    @Schema(description = "公开目录原价小计", example = "19200.00")
    private BigDecimal listSubtotal;

    @Schema(description = "营销活动与优惠减免总额", example = "1560.00")
    private BigDecimal promotionDiscount;

    @Schema(description = "运费", example = "0.00")
    private BigDecimal shippingFee;

    @Schema(description = "税额", example = "0.00")
    private BigDecimal tax;

    @Schema(description = "最终应付结算总额", example = "13800.00")
    private BigDecimal total;

    @Schema(description = "币种", example = "CNY")
    private String currency;

    @Schema(description = "各行项目的算价明细快照")
    private List<LinePricingSnapshot> lineSnapshots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "单行 SKU 算价轨迹快照")
    public static class LinePricingSnapshot implements Serializable {

        @Schema(description = "SKU 唯一标识", example = "sku_001")
        private String skuId;

        @Schema(description = "采购数量", example = "120")
        private Integer quantity;

        @Schema(description = "目录原价", example = "160.00")
        private BigDecimal listUnitPrice;

        @Schema(description = "客户协议基准单价", example = "128.00")
        private BigDecimal baseContractPrice;

        @Schema(description = "命中的阶梯起订量", example = "100")
        private Integer matchedTierMinQuantity;

        @Schema(description = "命中的阶梯单价", example = "115.00")
        private BigDecimal matchedTierUnitPrice;

        @Schema(description = "阶梯标签说明", example = "100-499件 享阶梯特惠")
        private String tierLabel;

        @Schema(description = "单件分摊的活动折扣", example = "5.75")
        private BigDecimal promotionDiscountPerUnit;

        @Schema(description = "最终成交结算单价", example = "109.25")
        private BigDecimal finalUnitPrice;

        @Schema(description = "本行成交小计金额", example = "13110.00")
        private BigDecimal lineTotal;
    }
}
