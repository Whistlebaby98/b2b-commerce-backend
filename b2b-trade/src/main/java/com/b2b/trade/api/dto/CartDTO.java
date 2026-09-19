package com.b2b.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * 采购车聚合数据传输对象 (Cart DTO)
 *
 * <p>对齐前端 Cart 契约，聚合当前企业的全部采购车行项与试算总额。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "企业采购车详情")
public class CartDTO implements Serializable {

    @Schema(description = "采购车 ID", example = "cart_company_lantu")
    private String id;

    @Schema(description = "所属企业组织 ID", example = "company-lantu")
    private String companyId;

    @Schema(description = "结算币种", example = "CNY")
    private String currency;

    @Schema(description = "采购车行项列表")
    private List<CartItemDTO> items;

    @Schema(description = "已适用的优惠券码", example = "NOVA50")
    private String couponCode;

    @Schema(description = "结算金额与件数汇总")
    private CartTotalsDTO totals;

    @Schema(description = "最后更新时间")
    private Instant updatedAt;
}
