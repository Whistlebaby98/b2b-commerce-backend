package com.b2b.pricing.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 阶梯价格区间数据传输对象 (PriceTier DTO)
 *
 * <p>对齐前端 PriceTier 契约，描述特定采购起订量对应的阶梯优惠单价。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "阶梯价格区间")
public class PriceTierDTO implements Serializable {

    @Schema(description = "该阶梯的最小起订量 (包含)", example = "50")
    private Integer minQuantity;

    @Schema(description = "命中该阶梯时的单件成交单价", example = "17.48")
    private BigDecimal unitPrice;

    @Schema(description = "阶梯价格展示标签", example = "50+ 阶梯特惠")
    private String label;
}
