package com.b2b.trade.api.dto;

import com.b2b.pricing.api.dto.PricingSnapshotDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 订单行项数据传输对象 (OrderLine DTO)
 *
 * <p>对齐前端 OrderLine 契约，包含规格快照与阶梯算价快照。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单单品行项明细")
public class OrderLineDTO implements Serializable {

    @Schema(description = "订单行项唯一标识", example = "line-202609140012-1")
    private String id;

    @Schema(description = "商品 SPU ID", example = "prod-sick-wl12g3")
    private String productId;

    @Schema(description = "SKU ID", example = "sku-sick-wl12g3")
    private String skuId;

    @Schema(description = "SKU 编码", example = "SKU-SICK-WL12G3")
    private String skuCode;

    @Schema(description = "SPU 标题", example = "SICK 光电传感器 WL12G-3")
    private String productTitle;

    @Schema(description = "SKU 名称", example = "WL12G-3 · PNP 常开")
    private String skuName;

    @Schema(description = "规格属性快照", example = "{\"检测距离\": \"100 mm\", \"输出\": \"PNP\"}")
    private Map<String, String> attributes;

    @Schema(description = "计量单位", example = "件")
    private String unit;

    @Schema(description = "采购数量", example = "18")
    private Integer quantity;

    @Schema(description = "最终成交结算单价", example = "156.00")
    private BigDecimal unitPrice;

    @Schema(description = "目录原价", example = "179.00")
    private BigDecimal listUnitPrice;

    @Schema(description = "行项优惠折扣分摊", example = "0.00")
    private BigDecimal promotionDiscount;

    @Schema(description = "行项成交小计", example = "2808.00")
    private BigDecimal subtotal;

    @Schema(description = "行项阶梯算价不可变快照")
    private PricingSnapshotDTO.LinePricingSnapshot pricingSnapshot;
}
