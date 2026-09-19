package com.b2b.finance.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * 企业开票资质档案持久化实体 (InvoiceProfilePO)
 *
 * <p>映射数据表 {@code fms_invoice_profile}，记录增值税专用发票、普通发票及电子发票资质。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fms_invoice_profile")
public class InvoiceProfilePO implements Serializable {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("company_id")
    private String companyId;

    @TableField("type")
    private String type;

    @TableField("title")
    private String title;

    @TableField("tax_id")
    private String taxId;

    @TableField("bank_name")
    private String bankName;

    @TableField("bank_account")
    private String bankAccount;

    @TableField("registered_address")
    private String registeredAddress;

    @TableField("registered_phone")
    private String registeredPhone;

    @TableField("receive_email")
    private String receiveEmail;

    @TableField("status")
    private String status;

    @TableField("is_default")
    private Boolean isDefault;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
