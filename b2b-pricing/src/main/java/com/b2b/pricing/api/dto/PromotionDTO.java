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
 * 营销与促销活动数据传输对象 (Promotion DTO)
 *
 * <p>对齐前端 Promotion 契约，支持目录级协议、品类阶梯折扣、满减券及免运费等营销规则。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "营销与促销活动规则信息")
public class PromotionDTO implements Serializable {

    @Schema(description = "活动唯一标识", example = "promo-september-agreement")
    private String id;

    @Schema(description = "优惠码/券码 (可选)", example = "NOVA50")
    private String code;

    @Schema(description = "活动标题", example = "金牌客户专享协议价")
    private String title;

    @Schema(description = "规则说明描述", example = "已根据企业等级自动匹配，无需输入优惠码")
    private String description;

    @Schema(description = "优惠类型 (percentage: 比例折扣, fixed_amount: 固定减免, tier_price: 阶梯优惠, coupon: 补贴券, free_shipping: 免运费)", example = "percentage")
    private String type;

    @Schema(description = "作用范围 (catalogue: 全目录, product: 指定商品, sku: 指定规格, category: 指定类目, cart: 采购车满减)", example = "catalogue")
    private String scope;

    @Schema(description = "适用商品 ID 列表")
    private List<String> productIds;

    @Schema(description = "适用 SKU ID 列表")
    private List<String> skuIds;

    @Schema(description = "适用类目 ID 列表")
    private List<String> categoryIds;

    @Schema(description = "活动生效开始时间 (ISO-8601)", example = "2026-09-01T00:00:00Z")
    private String startsAt;

    @Schema(description = "活动截止结束时间 (ISO-8601)", example = "2026-09-30T23:59:59Z")
    private String endsAt;

    @Schema(description = "是否启用生效", example = "true")
    private Boolean isActive;

    @Schema(description = "折扣比例 (如 0.06 表示 6% 折扣)", example = "0.06")
    private BigDecimal discountRate;

    @Schema(description = "固定减免金额 (元)", example = "50.00")
    private BigDecimal discountAmount;

    @Schema(description = "满减门槛金额 (元)", example = "5000.00")
    private BigDecimal minSubtotal;

    @Schema(description = "起订起用数量门槛 (件)", example = "10")
    private Integer minQuantity;

    @Schema(description = "单笔最大优惠封顶金额 (元)", example = "50.00")
    private BigDecimal maxDiscount;

    @Schema(description = "阶梯促销规则档位 (针对 tier_price 类型)")
    private List<PromotionTierDTO> tiers;

    @Schema(description = "是否支持与其他活动叠加", example = "false")
    private Boolean stackable;

    @Schema(description = "规则优先级 (数值越大优先级越高)", example = "1")
    private Integer priority;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "阶梯促销档位明细")
    public static class PromotionTierDTO implements Serializable {
        @Schema(description = "阶梯门槛数量", example = "10")
        private Integer minQuantity;

        @Schema(description = "阶梯门槛金额", example = "1000.00")
        private BigDecimal minSubtotal;

        @Schema(description = "阶梯折扣率", example = "0.04")
        private BigDecimal discountRate;

        @Schema(description = "阶梯固定减免金额", example = "20.00")
        private BigDecimal discountAmount;

        @Schema(description = "阶梯特价单价", example = "143.52")
        private BigDecimal unitPrice;
    }
}
