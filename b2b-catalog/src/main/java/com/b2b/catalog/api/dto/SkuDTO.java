package com.b2b.catalog.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 库存单位数据传输对象 (ProductSku DTO)
 *
 * <p>对齐前端 ProductSku 契约，SKU 是可交易、可下单、持有库存与交付承诺的最小业务单元。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SKU 可交易规格信息")
public class SkuDTO implements Serializable {

    @Schema(description = "SKU 唯一标识", example = "sku_001")
    private String id;

    @Schema(description = "所属商品 (SPU) ID", example = "prod_001")
    private String productId;

    @Schema(description = "业务规格编码 (如 OEM 码或型号)", example = "BEAR-6205-2RS")
    private String code;

    @Schema(description = "规格全称", example = "深沟球轴承 6205-2RS 内径25mm")
    private String name;

    @Schema(description = "动态工业规格参数")
    private Map<String, String> attributes;

    @Schema(description = "计量单位", example = "件")
    private String unit;

    @Schema(description = "基准对公协议单价", example = "128.00")
    private BigDecimal price;

    @Schema(description = "公开目录标价/划线价", example = "160.00")
    private BigDecimal listPrice;

    @Schema(description = "币种", example = "CNY")
    private String currency;

    @Schema(description = "阶梯价格规则列表")
    private java.util.List<com.b2b.pricing.api.dto.PriceTierDTO> priceTiers;

    @Schema(description = "当前可售库存", example = "1500")
    private Integer stock;

    @Schema(description = "库存状态 (in_stock, low_stock, preorder, out_of_stock)", example = "in_stock")
    private String availability;

    @Schema(description = "交付承诺时效说明", example = "现货，工作日当日发货")
    private String leadTimeLabel;

    @Schema(description = "可售状态 (available, out_of_stock, discontinued)", example = "available")
    private String status;

    @Schema(description = "单件重量 (kg)", example = "0.13")
    private BigDecimal weightKg;
}
