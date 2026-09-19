package com.b2b.pricing.infrastructure.persistence.entity;

import com.b2b.pricing.api.dto.PromotionDTO;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 营销与促销规则持久化实体 (PromotionPO)
 *
 * <p>映射表 {@code mkt_promotion}，支持多种规则类型与适用范围。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "mkt_promotion", autoResultMap = true)
public class PromotionPO implements Serializable {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("code")
    private String code;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("type")
    private String type;

    @TableField("scope")
    private String scope;

    @TableField(value = "category_ids", typeHandler = JacksonTypeHandler.class)
    private List<String> categoryIds;

    @TableField(value = "product_ids", typeHandler = JacksonTypeHandler.class)
    private List<String> productIds;

    @TableField(value = "sku_ids", typeHandler = JacksonTypeHandler.class)
    private List<String> skuIds;

    @TableField("starts_at")
    private Instant startsAt;

    @TableField("ends_at")
    private Instant endsAt;

    @TableField("is_active")
    private Boolean isActive;

    @TableField("discount_rate")
    private BigDecimal discountRate;

    @TableField("discount_amount")
    private BigDecimal discountAmount;

    @TableField("min_subtotal")
    private BigDecimal minSubtotal;

    @TableField("min_quantity")
    private Integer minQuantity;

    @TableField("max_discount")
    private BigDecimal maxDiscount;

    @TableField(value = "tiers", typeHandler = JacksonTypeHandler.class)
    private List<PromotionDTO.PromotionTierDTO> tiers;

    @TableField("stackable")
    private Boolean stackable;

    @TableField("priority")
    private Integer priority;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
