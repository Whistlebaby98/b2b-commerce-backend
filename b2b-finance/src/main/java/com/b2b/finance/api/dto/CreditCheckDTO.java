package com.b2b.finance.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 企业授信额度校验结果数据传输对象 (CreditCheck DTO)
 *
 * <p>用于结算试算与下单前核验可用额度是否足以支付采购金额。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "企业授信额度核验结果")
public class CreditCheckDTO implements Serializable {

    @Schema(description = "客户企业 ID", example = "org_1001")
    private String companyId;

    @Schema(description = "总授信额度", example = "500000.00")
    private BigDecimal creditLimit;

    @Schema(description = "当前已用额度", example = "128000.00")
    private BigDecimal creditUsed;

    @Schema(description = "可用额度 (Limit - Used)", example = "372000.00")
    private BigDecimal availableCredit;

    @Schema(description = "本次申请扣减/占用的金额", example = "13800.00")
    private BigDecimal requestAmount;

    @Schema(description = "额度是否充足", example = "true")
    private Boolean isSufficient;
}
