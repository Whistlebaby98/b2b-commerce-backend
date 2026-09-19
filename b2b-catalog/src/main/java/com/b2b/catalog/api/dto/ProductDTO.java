package com.b2b.catalog.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 商品族数据传输对象 (Product / SPU DTO)
 *
 * <p>对齐前端 Product 契约，Product 是展示与归类的基本单元，其下挂载多个具体可交易的 SKU。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品族档案信息")
public class ProductDTO implements Serializable {

    @Schema(description = "商品唯一标识", example = "prod_001")
    private String id;

    @Schema(description = "URL Slug", example = "precision-bearings-6200-series")
    private String slug;

    @Schema(description = "商品标题", example = "工业级精密深沟球轴承 6200 系列")
    private String title;

    @Schema(description = "副标题/卖点", example = "高转速 · 低噪音 · 优选高碳铬钢")
    private String subtitle;

    @Schema(description = "品牌", example = "NSK / 恩斯克")
    private String brand;

    @Schema(description = "所属类目 ID", example = "precision-parts")
    private String categoryId;

    @Schema(description = "所属类目名称", example = "精密机械零部件")
    private String category;

    @Schema(description = "详细描述说明")
    private String description;

    @Schema(description = "基础计量单位", example = "件")
    private String unit;

    @Schema(description = "下属 SKU 列表")
    private List<SkuDTO> skus;

    @Schema(description = "默认选中的 SKU ID", example = "sku_001")
    private String defaultSkuId;

    @Schema(description = "商品状态 (active, inactive, draft)", example = "active")
    private String status;

    @Schema(description = "缩略图 URL")
    private String thumbnail;

    @Schema(description = "卡片背景色调", example = "#e4f4f3")
    private String tone;

    @Schema(description = "主题强调色", example = "#199d97")
    private String accent;

    @Schema(description = "视觉标徽文字", example = "NSK")
    private String mark;

    @Schema(description = "商品角标列表 (agreement, bestseller, new, spot, promotion)")
    private List<String> badges;

    @Schema(description = "检索标签列表")
    private List<String> tags;

    @Schema(description = "是否推荐/精选商品", example = "true")
    private Boolean isFeatured;

    @Schema(description = "创建时间 (ISO-8601)", example = "2026-07-02T08:00:00Z")
    private String createdAt;

    @Schema(description = "更新时间 (ISO-8601)", example = "2026-09-14T08:00:00Z")
    private String updatedAt;
}
