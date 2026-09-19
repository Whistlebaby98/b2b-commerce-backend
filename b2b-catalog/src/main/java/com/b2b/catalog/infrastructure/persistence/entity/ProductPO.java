package com.b2b.catalog.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * 商品族档案持久化实体 (ProductPO)
 *
 * <p>映射表 {@code pms_product}，包含品牌、类目、徽标（JSONB）与标签（JSONB）。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "pms_product", autoResultMap = true)
public class ProductPO implements Serializable {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("slug")
    private String slug;

    @TableField("title")
    private String title;

    @TableField("subtitle")
    private String subtitle;

    @TableField("brand")
    private String brand;

    @TableField("category_id")
    private String categoryId;

    @TableField("description")
    private String description;

    @TableField("unit")
    private String unit;

    @TableField("default_sku_id")
    private String defaultSkuId;

    @TableField("tone")
    private String tone;

    @TableField("accent")
    private String accent;

    @TableField("mark")
    private String mark;

    @TableField(value = "badges", typeHandler = JacksonTypeHandler.class)
    private List<String> badges;

    @TableField(value = "tags", typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    @TableField("status")
    private String status;

    @TableField("is_featured")
    private Boolean isFeatured;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
