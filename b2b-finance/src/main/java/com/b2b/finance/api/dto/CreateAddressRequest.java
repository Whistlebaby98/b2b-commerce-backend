package com.b2b.finance.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 新增企业地址请求参数 (CreateAddress Request)
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增企业地址请求")
public class CreateAddressRequest implements Serializable {

    @Schema(description = "地址标签/别名", requiredMode = Schema.RequiredMode.REQUIRED, example = "深圳工厂（默认）")
    @NotBlank(message = "地址标签不能为空")
    private String label;

    @Schema(description = "地址类型 (shipping: 收货地址, billing: 开票地址)", requiredMode = Schema.RequiredMode.REQUIRED, example = "shipping")
    @NotBlank(message = "地址类型不能为空")
    private String kind;

    @Schema(description = "收件人/联系人", requiredMode = Schema.RequiredMode.REQUIRED, example = "陈志远")
    @NotBlank(message = "联系人不能为空")
    private String recipient;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotBlank(message = "联系电话不能为空")
    private String phone;

    @Schema(description = "省份", requiredMode = Schema.RequiredMode.REQUIRED, example = "广东省")
    @NotBlank(message = "省份不能为空")
    private String province;

    @Schema(description = "城市", requiredMode = Schema.RequiredMode.REQUIRED, example = "深圳市")
    @NotBlank(message = "城市不能为空")
    private String city;

    @Schema(description = "区/县", requiredMode = Schema.RequiredMode.REQUIRED, example = "宝安区")
    @NotBlank(message = "区县不能为空")
    private String district;

    @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "福海街道和平社区桥和路 18 号蓝图智造园 2 栋 1 楼")
    @NotBlank(message = "详细地址不能为空")
    private String detail;

    @Schema(description = "邮政编码", example = "518103")
    private String postalCode;

    @Schema(description = "是否设为默认地址", example = "true")
    private Boolean isDefault;
}
