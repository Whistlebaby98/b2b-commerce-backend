package com.b2b.pricing.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 算价请求入参数据传输对象 (PricingRequest DTO)
 *
 * <p>输入当前企业上下文以及待核验的行项目（SKU + 采购数量），
 * 由计价流水线一次性批量计算最终价格与明细。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量算价请求参数")
public class PricingRequestDTO implements Serializable {

    @Schema(description = "目标客户企业 ID (租户上下文)", example = "org_1001")
    private String companyId;

    @Schema(description = "待计价的明细项列表")
    private List<PricingItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "待计价的单行项目")
    public static class PricingItem implements Serializable {

        @Schema(description = "SKU 唯一标识", example = "sku_001")
        private String skuId;

        @Schema(description = "采购数量", example = "120")
        private Integer quantity;
    }
}
