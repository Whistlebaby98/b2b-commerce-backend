package com.b2b.catalog.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 批量采购清单解析响应载荷 (BulkResolveResponse)
 *
 * <p>对齐前端 /bulk-buying 页面契约，返回成功行与错误行，保证错误行精确提示行号与原因。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量采购清单解析结果")
public class BulkResolveResponse implements Serializable {

    @Schema(description = "成功解析并匹配到有效 SKU 的行数", example = "2")
    private Integer successCount;

    @Schema(description = "解析失败或未匹配到有效 SKU 的行数", example = "1")
    private Integer errorCount;

    @Schema(description = "逐行解析明细列表")
    private List<BulkRowItem> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "批量清单单行解析结果")
    public static class BulkRowItem implements Serializable {

        @Schema(description = "原始行号 (从 1 开始)", example = "1")
        private Integer lineNumber;

        @Schema(description = "原始输入的文本行", example = "SKU-NSK-6205ZZ 100")
        private String raw;

        @Schema(description = "解析出的 SKU 业务编码", example = "SKU-NSK-6205ZZ")
        private String skuCode;

        @Schema(description = "解析出的采购数量", example = "100")
        private Integer quantity;

        @Schema(description = "匹配到的有效 SKU 详情 (失败时为 null)")
        private SkuDTO sku;

        @Schema(description = "命中阶梯后的单件成交单价 (失败时为 null)", example = "16.74")
        private BigDecimal unitPrice;

        @Schema(description = "行级错误提示 (成功时为 null)", example = "缺少 SKU 编码或数量格式不正确")
        private String error;
    }
}
