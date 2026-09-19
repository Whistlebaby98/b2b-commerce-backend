package com.b2b.iam.infrastructure.persistence.entity;

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
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 客户企业组织持久化实体 (CompanyPO)
 *
 * <p>映射表 {@code org_company}，包含企业资质、授信与账期属性。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("org_company")
public class CompanyPO implements Serializable {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("name")
    private String name;

    @TableField("short_name")
    private String shortName;

    @TableField("tax_id")
    private String taxId;

    @TableField("customer_tier")
    private String customerTier;

    @TableField("is_verified")
    private Boolean isVerified;

    @TableField("credit_limit")
    private BigDecimal creditLimit;

    @TableField("credit_used")
    private BigDecimal creditUsed;

    @TableField("payment_term_days")
    private Integer paymentTermDays;

    @TableField("currency")
    private String currency;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;

    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
