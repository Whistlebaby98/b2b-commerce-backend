package com.b2b.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 添加商品至采购车请求 (AddToCart Request)
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "加购商品请求")
public class AddToCartRequest implements Serializable {

    @Schema(description = "目标 SKU ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "sku-nsk-6205-zz")
    @NotBlank(message = "SKU ID 不能为空")
    private String skuId;

    @Schema(description = "加购数量 (必须大于等于 1)", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "加购数量不能为空")
    @Min(value = 1, message = "加购数量必须大于等于 1")
    private Integer quantity;
}
