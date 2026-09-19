package com.b2b.finance.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 企业授信额度数据传输对象 (CompanyCredit DTO)
 *
 * <p>展示企业当前总额度、已用额度与可用余额。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "企业授信概览信息")
public class CompanyCreditDTO implements Serializable {

    @Schema(description = "客户企业 ID", example = "company-lantu")
    private String companyId;

    @Schema(description = "企业名称", example = "深圳市蓝图精密制造有限公司")
    private String companyName;

    @Schema(description = "总授信额度", example = "500000.00")
    private BigDecimal creditLimit;

    @Schema(description = "已用授信额度", example = "128000.00")
    private BigDecimal creditUsed;

    @Schema(description = "可用授信额度 (Limit - Used)", example = "372000.00")
    private BigDecimal availableCredit;

    @Schema(description = "币种", example = "CNY")
    private String currency;
}
