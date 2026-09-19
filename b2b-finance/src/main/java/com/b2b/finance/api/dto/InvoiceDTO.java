package com.b2b.finance.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 企业开票资质数据传输对象 (InvoiceProfile DTO)
 *
 * <p>对齐前端 InvoiceProfile 契约，支持增值税专票、普票与电票资质管理。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "企业开票资质信息")
public class InvoiceDTO implements Serializable {

    @Schema(description = "资质唯一标识", example = "invoice-special-default")
    private String id;

    @Schema(description = "所属企业 ID", example = "company-lantu")
    private String companyId;

    @Schema(description = "发票类型 (vat_special, vat_normal, electronic)", example = "vat_special")
    private String type;

    @Schema(description = "开票抬头", example = "深圳市蓝图精密制造有限公司")
    private String title;

    @Schema(description = "统一社会信用代码/税号", example = "91440300MA5XXXXXX")
    private String taxId;

    @Schema(description = "开户银行名称", example = "招商银行深圳科技园支行")
    private String bankName;

    @Schema(description = "银行基本户账号", example = "7559 **** **** 8812")
    private String bankAccount;

    @Schema(description = "企业注册地址", example = "广东省深圳市南山区科技园")
    private String registeredAddress;

    @Schema(description = "企业注册电话", example = "0755-8234-1902")
    private String registeredPhone;

    @Schema(description = "发票接收电子邮箱", example = "finance@lantu-mfg.example")
    private String receiveEmail;

    @Schema(description = "资质核验状态 (active, pending_verification, disabled)", example = "active")
    private String status;

    @Schema(description = "是否默认资质", example = "true")
    private Boolean isDefault;
}
