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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * 库存单位持久化实体 (SkuPO)
 *
 * <p>映射表 {@code pms_sku}，包含动态规格参数（JSONB）、基准协议价与可售库存。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "pms_sku", autoResultMap = true)
public class SkuPO implements Serializable {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("product_id")
    private String productId;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField(value = "attributes", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> attributes;

    @TableField("unit")
    private String unit;

    @TableField("price")
    private BigDecimal price;

    @TableField("list_price")
    private BigDecimal listPrice;

    @TableField("currency")
    private String currency;

    @TableField("stock")
    private Integer stock;

    @TableField("availability")
    private String availability;

    @TableField("lead_time_label")
    private String leadTimeLabel;

    @TableField("status")
    private String status;

    @TableField("weight_kg")
    private BigDecimal weightKg;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
