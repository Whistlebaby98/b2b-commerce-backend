package com.b2b.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 采购车单品行项数据传输对象 (CartItem DTO)
 *
 * <p>对齐前端 CartItem 契约，支持规格属性、阶梯实时单价及勾选状态展示。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "采购车单品行项")
public class CartItemDTO implements Serializable {

    @Schema(description = "行项 ID", example = "cart_item_1")
    private String id;

    @Schema(description = "商品 SPU ID", example = "prod-nsk-6205")
    private String productId;

    @Schema(description = "商品所属分类 ID", example = "transmission-parts")
    private String categoryId;

    @Schema(description = "SKU ID", example = "sku-nsk-6205-zz")
    private String skuId;

    @Schema(description = "SKU 编码", example = "SKU-NSK-6205-ZZ")
    private String skuCode;

    @Schema(description = "SPU 标题", example = "NSK 深沟球轴承 6205ZZ")
    private String productTitle;

    @Schema(description = "SKU 名称与规格", example = "6205ZZ · 25×52×15 mm")
    private String skuName;

    @Schema(description = "计价计量单位", example = "套")
    private String unit;

    @Schema(description = "当前命中阶梯后的单价", example = "26.50")
    private BigDecimal unitPrice;

    @Schema(description = "目录原价/公开价", example = "32.00")
    private BigDecimal listUnitPrice;

    @Schema(description = "加购数量", example = "12")
    private Integer quantity;

    @Schema(description = "是否勾选结算", example = "true")
    private Boolean selected;

    @Schema(description = "应用的促销/协议规则标识列表")
    private List<String> promotionIds;

    @Schema(description = "加入采购车时间")
    private Instant addedAt;
}
