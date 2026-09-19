package com.b2b.trade.api.dto;

import com.b2b.finance.api.dto.AddressDTO;
import com.b2b.finance.api.dto.CreditCheckDTO;
import com.b2b.finance.api.dto.InvoiceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 结算试算响应数据传输对象 (CheckoutPreview DTO)
 *
 * <p>对齐前端 CheckoutState 契约，返回结算金额汇总、防重 Token、选中地址/发票及企业授信预检结果。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "结算试算会话响应")
public class CheckoutPreviewDTO implements Serializable {

    @Schema(description = "防重提交结算 Token (单次原子核销)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String checkoutToken;

    @Schema(description = "结算 Token 有效期 (秒)", example = "1800")
    private Long tokenExpiresInSeconds;

    @Schema(description = "本次参与结算的商品行项列表")
    private List<CartItemDTO> items;

    @Schema(description = "结算金额与件数汇总")
    private CartTotalsDTO totals;

    @Schema(description = "选中的收货地址")
    private AddressDTO selectedAddress;

    @Schema(description = "选中的开票资质")
    private InvoiceDTO selectedInvoice;

    @Schema(description = "企业授信额度预检结果")
    private CreditCheckDTO creditCheck;
}
