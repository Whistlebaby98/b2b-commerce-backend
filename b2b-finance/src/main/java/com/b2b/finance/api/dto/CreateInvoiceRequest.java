package com.b2b.finance.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 新增企业开票资质请求参数 (CreateInvoice Request)
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增企业开票资质请求")
public class CreateInvoiceRequest implements Serializable {

    @Schema(description = "发票类型 (vat_special: 增值税专用发票, vat_normal: 增值税普通发票, electronic: 电子普票)", requiredMode = Schema.RequiredMode.REQUIRED, example = "vat_special")
    @NotBlank(message = "发票类型不能为空")
    private String type;

    @Schema(description = "开票抬头/企业全称", requiredMode = Schema.RequiredMode.REQUIRED, example = "深圳市蓝图精密制造有限公司")
    @NotBlank(message = "开票抬头不能为空")
    private String title;

    @Schema(description = "纳税人识别号/统一社会信用代码", requiredMode = Schema.RequiredMode.REQUIRED, example = "91440300MA5F8N7X2K")
    @NotBlank(message = "税号不能为空")
    private String taxId;

    @Schema(description = "开户银行名称 (专票必填)", example = "招商银行深圳科技园支行")
    private String bankName;

    @Schema(description = "开户银行账号 (专票必填)", example = "755902188812")
    private String bankAccount;

    @Schema(description = "企业注册地址 (专票必填)", example = "广东省深圳市南山区科技园")
    private String registeredAddress;

    @Schema(description = "企业注册电话 (专票必填)", example = "0755-8234-1902")
    private String registeredPhone;

    @Schema(description = "发票接收电子邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "finance@lantu-mfg.example")
    @NotBlank(message = "接收邮箱不能为空")
    @Email(message = "请输入有效的电子邮箱")
    private String receiveEmail;

    @Schema(description = "是否设为默认开票资质", example = "true")
    private Boolean isDefault;
}
