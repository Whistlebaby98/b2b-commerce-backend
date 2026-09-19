package com.b2b.pricing.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 阶梯价格区间持久化实体 (PriceTierPO)
 *
 * <p>映射表 {@code price_tier}，存储 SKU 在各起订量区间下的成交单价。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("price_tier")
public class PriceTierPO implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("sku_id")
    private String skuId;

    @TableField("min_quantity")
    private Integer minQuantity;

    @TableField("unit_price")
    private BigDecimal unitPrice;

    @TableField("label")
    private String label;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
