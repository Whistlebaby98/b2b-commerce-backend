package com.b2b.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 采购订单分页筛选查询请求 (OrderQuery Request)
 *
 * <p>支持根据订单业务状态、订单号或商品名称关键词进行多租户检索。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单分页筛选请求")
public class OrderQueryRequest implements Serializable {

    @Schema(description = "当前页码", defaultValue = "1", example = "1")
    @Builder.Default
    private Integer page = 1;

    @Schema(description = "每页数量", defaultValue = "10", example = "10")
    @Builder.Default
    private Integer pageSize = 10;

    @Schema(description = "订单状态筛选 (pending_approval, approved, awaiting_payment, processing, shipped, completed, cancelled, rejected)", example = "pending_approval")
    private String status;

    @Schema(description = "检索关键词 (订单号或买方附言)", example = "PO2026")
    private String query;
}
