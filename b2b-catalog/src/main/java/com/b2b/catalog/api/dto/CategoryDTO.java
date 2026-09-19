package com.b2b.catalog.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品品类数据传输对象 (ProductCategory DTO)
 *
 * <p>对齐前端 ProductCategory 契约，包含品类标识、名称、描述及包含的商品数量。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品品类信息")
public class CategoryDTO implements Serializable {

    @Schema(description = "品类唯一标识", example = "precision-parts")
    private String id;

    @Schema(description = "品类名称", example = "精密零件")
    private String name;

    @Schema(description = "品类描述说明", example = "轴承、联轴器及机械传动件")
    private String description;

    @Schema(description = "品类下商品数量", example = "128")
    private Integer productCount;
}
