package com.b2b.catalog.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品目录检索与筛选请求参数 (CatalogQueryRequest)
 *
 * <p>对齐前端 CatalogFilters 契约，支持关键词、类目、品牌、现货状态、价格区间与排序。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品目录查询与筛选参数")
public class CatalogQueryRequest implements Serializable {

    @Schema(description = "综合搜索词（标题、品牌、SKU 编码）", example = "轴承")
    private String query;

    @Schema(description = "品类 ID", example = "precision-parts")
    private String categoryId;

    @Schema(description = "品牌", example = "NSK")
    private String brand;

    @Schema(description = "现货/库存状态 (in_stock, low_stock, preorder, out_of_stock)", example = "in_stock")
    private String availability;

    @Schema(description = "最低协议价格", example = "10.00")
    private BigDecimal minPrice;

    @Schema(description = "最高协议价格", example = "500.00")
    private BigDecimal maxPrice;

    @Schema(description = "排序方式: recommended(推荐), price_asc(价格升序), price_desc(价格降序), newest(最新)", example = "recommended")
    @Builder.Default
    private String sortBy = "recommended";

    @Schema(description = "页码 (从 1 开始)", example = "1")
    @Builder.Default
    private Integer page = 1;

    @Schema(description = "每页数量", example = "20")
    @Builder.Default
    private Integer pageSize = 20;
}
