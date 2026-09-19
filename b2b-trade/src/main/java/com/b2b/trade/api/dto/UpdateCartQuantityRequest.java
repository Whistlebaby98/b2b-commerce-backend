package com.b2b.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 修改采购车行数量请求 (UpdateCartQuantity Request)
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改采购车数量请求")
public class UpdateCartQuantityRequest implements Serializable {

    @Schema(description = "调整后的数量 (必须大于等于 1)", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于等于 1")
    private Integer quantity;
}
