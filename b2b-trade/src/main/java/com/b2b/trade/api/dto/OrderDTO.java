package com.b2b.trade.api.dto;

import com.b2b.approval.api.dto.ApprovalRequestDTO;
import com.b2b.finance.api.dto.AddressSnapshotDTO;
import com.b2b.finance.api.dto.InvoiceSnapshotDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

/**
 * 采购订单详情数据传输对象 (Order DTO)
 *
 * <p>对齐前端 Order 契约，聚合主档、订单行不可变快照、地址发票快照及审批时间线。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "采购订单完整详情")
public class OrderDTO implements Serializable {

    @Schema(description = "采购订单唯一标识", example = "ord-202609140012")
    private String id;

    @Schema(description = "采购订单号", example = "PO202609140012")
    private String orderNo;

    @Schema(description = "所属企业 ID", example = "company-lantu")
    private String companyId;

    @Schema(description = "下单人用户 ID", example = "user-lin-yue")
    private String createdBy;

    @Schema(description = "订单业务状态", example = "pending_approval")
    private String status;

    @Schema(description = "审批流转状态", example = "pending")
    private String approvalStatus;

    @Schema(description = "支付方式", example = "credit_account")
    private String paymentMethod;

    @Schema(description = "订单商品行项列表")
    private List<OrderLineDTO> lines;

    @Schema(description = "订单金额汇总")
    private OrderTotalsDTO totals;

    @Schema(description = "收货地址不可变快照")
    private AddressSnapshotDTO shippingAddress;

    @Schema(description = "开票资质不可变快照")
    private InvoiceSnapshotDTO invoice;

    @Schema(description = "买方附言/备注", example = "生产线急需传感器替换件，请优先排单")
    private String note;

    @Schema(description = "期望送达日期")
    private LocalDate requestedDeliveryDate;

    @Schema(description = "创建时间")
    private Instant createdAt;

    @Schema(description = "更新时间")
    private Instant updatedAt;

    @Schema(description = "提交时间")
    private Instant submittedAt;

    @Schema(description = "关联的审批工作流及时间线详情")
    private ApprovalRequestDTO approval;
}
