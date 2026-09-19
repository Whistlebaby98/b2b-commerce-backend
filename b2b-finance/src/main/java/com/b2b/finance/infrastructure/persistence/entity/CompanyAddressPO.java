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
 * 企业地址档案持久化实体 (CompanyAddressPO)
 *
 * <p>映射数据表 {@code fms_company_address}，管理企业多收货地址与开票地址。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fms_company_address")
public class CompanyAddressPO implements Serializable {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("company_id")
    private String companyId;

    @TableField("label")
    private String label;

    @TableField("kind")
    private String kind;

    @TableField("recipient")
    private String recipient;

    @TableField("phone")
    private String phone;

    @TableField("province")
    private String province;

    @TableField("city")
    private String city;

    @TableField("district")
    private String district;

    @TableField("detail")
    private String detail;

    @TableField("postal_code")
    private String postalCode;

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
