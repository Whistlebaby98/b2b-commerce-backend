package com.b2b.trade.infrastructure.persistence.entity;

import com.b2b.finance.api.dto.AddressSnapshotDTO;
import com.b2b.finance.api.dto.InvoiceSnapshotDTO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

/**
 * 采购订单持久化实体 (OrderPO)
 *
 * <p>映射数据表 {@code oms_order}，承载采购单主档、不可变快照及状态机字段。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "oms_order", autoResultMap = true)
public class OrderPO implements Serializable {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("order_no")
    private String orderNo;

    @TableField("company_id")
    private String companyId;

    @TableField("created_by")
    private String createdBy;

    @TableField("status")
    private String status;

    @TableField("approval_status")
    private String approvalStatus;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField("subtotal")
    private BigDecimal subtotal;

    @TableField("list_subtotal")
    private BigDecimal listSubtotal;

    @TableField("promotion_discount")
    private BigDecimal promotionDiscount;

    @TableField("shipping_fee")
    private BigDecimal shippingFee;

    @TableField("tax")
    private BigDecimal tax;

    @TableField("total")
    private BigDecimal total;

    @TableField("currency")
    private String currency;

    @TableField(value = "shipping_address_snapshot", typeHandler = JacksonTypeHandler.class)
    private AddressSnapshotDTO shippingAddressSnapshot;

    @TableField(value = "invoice_snapshot", typeHandler = JacksonTypeHandler.class)
    private InvoiceSnapshotDTO invoiceSnapshot;

    @TableField("note")
    private String note;

    @TableField("requested_delivery_date")
    private LocalDate requestedDeliveryDate;

    @TableField("submitted_at")
    private Instant submittedAt;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
