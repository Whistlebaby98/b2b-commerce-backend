package com.b2b.approval.service;

import com.b2b.approval.api.dto.ApprovalRequestDTO;
import com.b2b.approval.infrastructure.persistence.entity.ApprovalRequestPO;
import com.b2b.approval.infrastructure.persistence.mapper.ApprovalRequestMapper;
import com.b2b.common.api.ResultCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 审批应用服务 (ApprovalApplicationService)
 *
 * <p>遵循 ADR 0003 两阶段审批设计，实现待审批请求生成与进度查询。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalApplicationService {

    private final ApprovalRequestMapper approvalRequestMapper;

    /**
     * 创建订单审批工作项
     */
    @Transactional(rollbackFor = Exception.class)
    public ApprovalRequestDTO createApprovalRequest(String orderId, String orderNo, String companyId, String submittedBy) {
        log.info("[Approval] 创建订单审批请求: orderId={}, orderNo={}, companyId={}", orderId, orderNo, companyId);

        // 幂等校验：已存在则直接返回
        ApprovalRequestPO existing = approvalRequestMapper.selectOne(
                new LambdaQueryWrapper<ApprovalRequestPO>().eq(ApprovalRequestPO::getOrderId, orderId)
        );
        if (existing != null) {
            return toDTO(existing);
        }

        List<ApprovalRequestDTO.ApprovalTimelineNode> timeline = new ArrayList<>();
        timeline.add(ApprovalRequestDTO.ApprovalTimelineNode.builder()
                .action("submitted")
                .operatorName("采购员 (系统提交)")
                .comment("采购订单已提交，进入组织审批流程")
                .timestamp(Instant.now())
                .build());

        ApprovalRequestPO po = ApprovalRequestPO.builder()
                .id("appr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                .orderId(orderId)
                .orderNo(orderNo)
                .companyId(companyId)
                .status("pending")
                .currentNode("待审批")
                .submittedBy(submittedBy)
                .timeline(timeline)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        approvalRequestMapper.insert(po);
        return toDTO(po);
    }

    /**
     * 根据订单 ID 查询审批请求
     */
    public ApprovalRequestDTO getApprovalByOrderId(String orderId) {
        ApprovalRequestPO po = approvalRequestMapper.selectOne(
                new LambdaQueryWrapper<ApprovalRequestPO>().eq(ApprovalRequestPO::getOrderId, orderId)
        );
        return po != null ? toDTO(po) : null;
    }

    /**
     * 查询企业名下审批单列表
     */
    public List<ApprovalRequestDTO> listApprovals(String companyId, String status) {
        LambdaQueryWrapper<ApprovalRequestPO> query = new LambdaQueryWrapper<ApprovalRequestPO>()
                .eq(ApprovalRequestPO::getCompanyId, companyId)
                .orderByDesc(ApprovalRequestPO::getCreatedAt);

        if (status != null && !status.isBlank()) {
            query.eq(ApprovalRequestPO::getStatus, status);
        }

        List<ApprovalRequestPO> list = approvalRequestMapper.selectList(query);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ApprovalRequestDTO toDTO(ApprovalRequestPO po) {
        return ApprovalRequestDTO.builder()
                .id(po.getId())
                .orderId(po.getOrderId())
                .orderNo(po.getOrderNo())
                .companyId(po.getCompanyId())
                .status(po.getStatus())
                .currentNode(po.getCurrentNode())
                .submittedBy(po.getSubmittedBy())
                .timeline(po.getTimeline() != null ? po.getTimeline() : Collections.emptyList())
                .build();
    }
}
