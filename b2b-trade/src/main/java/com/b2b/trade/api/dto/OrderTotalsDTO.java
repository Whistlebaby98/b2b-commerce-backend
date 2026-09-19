package com.b2b.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单金额汇总数据传输对象 (OrderTotals DTO)
 *
 * <p>对齐前端 OrderTotals 契约。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单金额汇总")
public class OrderTotalsDTO implements Serializable {

    @Schema(description = "币种", example = "CNY")
    private String currency;

    @Schema(description = "商品成交小计", example = "2808.00")
    private BigDecimal subtotal;

    @Schema(description = "目录原价小计", example = "3222.00")
    private BigDecimal listSubtotal;

    @Schema(description = "优惠减免总额", example = "414.00")
    private BigDecimal promotionDiscount;

    @Schema(description = "运费", example = "0.00")
    private BigDecimal shippingFee;

    @Schema(description = "税金", example = "0.00")
    private BigDecimal tax;

    @Schema(description = "实付总金额", example = "2808.00")
    private BigDecimal total;
}
