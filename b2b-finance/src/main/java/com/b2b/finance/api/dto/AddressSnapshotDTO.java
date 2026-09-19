package com.b2b.finance.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单收货/开票地址不可变快照 (AddressSnapshot DTO)
 *
 * <p>对齐前端 AddressSnapshot 契约，订单生成后冻结留存，不受后续企业地址修改影响。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单地址不可变快照")
public class AddressSnapshotDTO implements Serializable {

    @Schema(description = "原始地址 ID", example = "addr-shenzhen-factory")
    private String id;

    @Schema(description = "地址标签", example = "深圳工厂（默认）")
    private String label;

    @Schema(description = "地址类型 (shipping, billing)", example = "shipping")
    private String kind;

    @Schema(description = "收件人/联系人姓名", example = "陈志远")
    private String recipient;

    @Schema(description = "联系电话", example = "138****6721")
    private String phone;

    @Schema(description = "省份", example = "广东省")
    private String province;

    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Schema(description = "区/县", example = "宝安区")
    private String district;

    @Schema(description = "详细地址", example = "福海街道和平社区桥和路 18 号蓝图智造园 2 栋 1 楼")
    private String detail;

    @Schema(description = "邮政编码", example = "518103")
    private String postalCode;
}
