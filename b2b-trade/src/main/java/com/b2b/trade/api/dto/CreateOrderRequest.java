package com.b2b.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 提交采购订单请求 (CreateOrder Request)
 *
 * <p>遵循 ADR 0007 规范，必须携带 preview 接口生成的 checkoutToken 原子核销。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建采购订单请求")
public class CreateOrderRequest implements Serializable {

    @Schema(description = "结算防重核销 Token", requiredMode = Schema.RequiredMode.REQUIRED, example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank(message = "checkoutToken 不能为空")
    private String checkoutToken;

    @Schema(description = "收货地址 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "addr-shenzhen-factory")
    @NotBlank(message = "收货地址不能为空")
    private String addressId;

    @Schema(description = "开票资质 ID (可选)", example = "invoice-special-default")
    private String invoiceId;

    @Schema(description = "支付方式 (credit_account: 授信账期, bank_transfer: 银行转账)", requiredMode = Schema.RequiredMode.REQUIRED, example = "credit_account")
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    @Schema(description = "订单买方备注", example = "加急发货，用于产线替换")
    private String note;

    @Schema(description = "期望送达日期 (YYYY-MM-DD)", example = "2026-09-25")
    private LocalDate requestedDeliveryDate;

    @Schema(description = "直接下单商品列表 (可选，若为空则结算采购车中所有已勾选行项)")
    private List<CheckoutPreviewRequest.DirectCheckoutItem> items;
}
