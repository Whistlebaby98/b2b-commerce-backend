package com.b2b.trade.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * 采购车行项持久化实体 (CartItemPO)
 *
 * <p>映射数据表 {@code oms_cart_item}，记录多租户企业买方的加购商品与勾选状态。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("oms_cart_item")
public class CartItemPO implements Serializable {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("company_id")
    private String companyId;

    @TableField("user_id")
    private String userId;

    @TableField("sku_id")
    private String skuId;

    @TableField("quantity")
    private Integer quantity;

    @TableField("selected")
    private Boolean selected;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
