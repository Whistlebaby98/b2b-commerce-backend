package com.b2b.iam.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 客户组织信息数据传输对象 (CustomerOrganization DTO)
 *
 * <p>对齐前端 CompanyProfile 契约，描述当前企业的资质、授信与账期属性。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "客户企业组织信息")
public class OrganizationDTO implements Serializable {

    @Schema(description = "企业唯一标识", example = "org_1001")
    private String id;

    @Schema(description = "企业全称", example = "深圳市蓝图精密制造有限公司")
    private String name;

    @Schema(description = "企业简称", example = "蓝图精密")
    private String shortName;

    @Schema(description = "统一社会信用代码/税号", example = "91440300MA5XXXXXX")
    private String taxId;

    @Schema(description = "客群等级 (standard, silver, gold, strategic)", example = "gold")
    private String customerTier;

    @Schema(description = "资质是否认证", example = "true")
    private Boolean isVerified;

    @Schema(description = "总授信额度", example = "500000.00")
    private BigDecimal creditLimit;

    @Schema(description = "已用授信额度", example = "128000.00")
    private BigDecimal creditUsed;

    @Schema(description = "账期结算天数", example = "30")
    private Integer paymentTermDays;

    @Schema(description = "币种", example = "CNY")
    private String currency;
}
