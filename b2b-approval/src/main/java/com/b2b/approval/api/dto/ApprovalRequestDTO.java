package com.b2b.approval.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * 审批请求数据传输对象 (ApprovalRequest DTO)
 *
 * <p>遵循 ADR 0001 规范，记录采购订单提交后触发的审批工作项与进度轨迹。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单审批请求详情")
public class ApprovalRequestDTO implements Serializable {

    @Schema(description = "审批单唯一标识", example = "appr_1001")
    private String id;

    @Schema(description = "关联的采购订单 ID", example = "ord_1001")
    private String orderId;

    @Schema(description = "关联的采购订单号", example = "PO202609190001")
    private String orderNo;

    @Schema(description = "所属客户企业 ID", example = "org_1001")
    private String companyId;

    @Schema(description = "审批状态 (pending, approved, rejected)", example = "pending")
    private String status;

    @Schema(description = "当前所处节点说明", example = "等待部门主管审批")
    private String currentNode;

    @Schema(description = "提交人 ID", example = "usr_001")
    private String submittedBy;

    @Schema(description = "审批处理时间线")
    private List<ApprovalTimelineNode> timeline;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "审批时间线节点")
    public static class ApprovalTimelineNode implements Serializable {

        @Schema(description = "节点动作 (submitted, approved, rejected)", example = "submitted")
        private String action;

        @Schema(description = "处理人姓名或角色", example = "张采购 (提交人)")
        private String operatorName;

        @Schema(description = "处理意见/备注", example = "采购订单已提交，进入组织审批流程")
        private String comment;

        @Schema(description = "发生时间")
        private Instant timestamp;
    }
}
