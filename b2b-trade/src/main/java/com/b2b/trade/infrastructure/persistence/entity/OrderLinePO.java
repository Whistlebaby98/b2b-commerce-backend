package com.b2b.trade.infrastructure.persistence.entity;

import com.b2b.pricing.api.dto.PricingSnapshotDTO;
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
 * 采购订单行项持久化实体 (OrderLinePO)
 *
 * <p>映射数据表 {@code oms_order_line}，记录商品成交单价、规格属性快照与阶梯算价快照。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "oms_order_line", autoResultMap = true)
public class OrderLinePO implements Serializable {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("order_id")
    private String orderId;

    @TableField("product_id")
    private String productId;

    @TableField("sku_id")
    private String skuId;

    @TableField("sku_code")
    private String skuCode;

    @TableField("product_title")
    private String productTitle;

    @TableField("sku_name")
    private String skuName;

    @TableField(value = "attributes", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> attributes;

    @TableField("unit")
    private String unit;

    @TableField("quantity")
    private Integer quantity;

    @TableField("unit_price")
    private BigDecimal unitPrice;

    @TableField("list_unit_price")
    private BigDecimal listUnitPrice;

    @TableField("promotion_discount")
    private BigDecimal promotionDiscount;

    @TableField("subtotal")
    private BigDecimal subtotal;

    @TableField(value = "pricing_snapshot", typeHandler = JacksonTypeHandler.class)
    private PricingSnapshotDTO.LinePricingSnapshot pricingSnapshot;

    @TableField("created_at")
    private Instant createdAt;
}
