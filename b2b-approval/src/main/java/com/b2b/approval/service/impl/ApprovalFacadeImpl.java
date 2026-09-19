package com.b2b.approval.service.impl;

import com.b2b.approval.api.ApprovalFacade;
import com.b2b.approval.api.dto.ApprovalRequestDTO;
import com.b2b.approval.service.ApprovalApplicationService;
import com.b2b.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 审批域门面实现类 (ApprovalFacadeImpl)
 *
 * <p>遵循 ADR 0001 与 ADR 0003，为交易域提供审批单创建与进度查询支持。</p>
 *
 * @author b2b-commerce-backend
 */
@Component
@RequiredArgsConstructor
public class ApprovalFacadeImpl implements ApprovalFacade {

    private final ApprovalApplicationService approvalApplicationService;

    @Override
    public ApprovalRequestDTO getApprovalByOrderId(String orderId) {
        return approvalApplicationService.getApprovalByOrderId(orderId);
    }

    @Override
    public ApprovalRequestDTO createApprovalRequest(String orderId, String orderNo, String companyId, String submittedBy) throws BizException {
        return approvalApplicationService.createApprovalRequest(orderId, orderNo, companyId, submittedBy);
    }
}
