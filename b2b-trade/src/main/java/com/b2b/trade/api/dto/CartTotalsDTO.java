package com.b2b.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 采购车金额与件数汇总数据传输对象 (CartTotals DTO)
 *
 * <p>对齐前端 CartTotals 契约，提供勾选商品的阶梯总价、立减优惠与应付结算总额。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "采购车结算金额汇总")
public class CartTotalsDTO implements Serializable {

    @Schema(description = "币种", example = "CNY")
    private String currency;

    @Schema(description = "已勾选结算的商品总件数", example = "12")
    private Integer itemCount;

    @Schema(description = "商品协议成交小计", example = "318.00")
    private BigDecimal subtotal;

    @Schema(description = "目录公开原价小计", example = "384.00")
    private BigDecimal listSubtotal;

    @Schema(description = "阶梯价与促销优惠总额", example = "66.00")
    private BigDecimal promotionDiscount;

    @Schema(description = "配送运费", example = "0.00")
    private BigDecimal shippingFee;

    @Schema(description = "税金", example = "0.00")
    private BigDecimal tax;

    @Schema(description = "最终应付结算总额", example = "318.00")
    private BigDecimal total;
}
