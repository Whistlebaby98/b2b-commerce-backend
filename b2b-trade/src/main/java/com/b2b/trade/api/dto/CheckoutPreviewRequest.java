package com.b2b.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 结算试算与预检请求 (CheckoutPreview Request)
 *
 * <p>进入结算页时发起试算，预检地址、开票资质与授信可用性，并领取单次有效 checkout_token。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "结算试算与预检请求")
public class CheckoutPreviewRequest implements Serializable {

    @Schema(description = "指定的收货地址 ID (可选，未指定时默认使用企业默认地址)", example = "addr-shenzhen-factory")
    private String addressId;

    @Schema(description = "指定的开票资质 ID (可选，未指定时默认使用企业默认专票)", example = "invoice-special-default")
    private String invoiceId;

    @Schema(description = "直接结算的商品列表 (可选，若为空则默认取采购车中所有已勾选行项)")
    private List<DirectCheckoutItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "直接结算单品项")
    public static class DirectCheckoutItem implements Serializable {
        @Schema(description = "SKU ID", example = "sku-nsk-6205-zz")
        private String skuId;

        @Schema(description = "采购数量", example = "10")
        private Integer quantity;
    }
}
