package com.b2b.approval.controller;

import com.b2b.approval.api.dto.ApprovalRequestDTO;
import com.b2b.approval.service.ApprovalApplicationService;
import com.b2b.common.api.ApiResponse;
import com.b2b.common.tenant.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单审批工作流控制器 (ApprovalController)
 *
 * <p>遵循 ADR 0001 & ADR 0003，买方端仅提供审批进度与时间线查询，严禁暴露通过/驳回操作给买方。</p>
 *
 * @author b2b-commerce-backend
 */
@Tag(name = "04. 订单审批流", description = "订单审批工作项进度与轨迹查询接口")
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalApplicationService approvalApplicationService;

    @Operation(summary = "查询当前企业审批列表", description = "获取当前企业的订单审批单及流转状态")
    @GetMapping
    public ApiResponse<List<ApprovalRequestDTO>> listApprovals(
            @Parameter(description = "审批状态 (pending, approved, rejected)")
            @RequestParam(value = "status", required = false) String status
    ) {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        return ApiResponse.success(approvalApplicationService.listApprovals(companyId, status));
    }

    @Operation(summary = "查询订单关联的审批详情", description = "根据采购订单 ID 查询关联审批单与时间线节点")
    @GetMapping("/{orderId}")
    public ApiResponse<ApprovalRequestDTO> getApprovalByOrderId(
            @Parameter(description = "采购订单 ID", required = true)
            @PathVariable("orderId") String orderId
    ) {
        return ApiResponse.success(approvalApplicationService.getApprovalByOrderId(orderId));
    }
}
