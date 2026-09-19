package com.b2b.approval.api;

import com.b2b.approval.api.dto.ApprovalRequestDTO;
import com.b2b.common.exception.BizException;

/**
 * 审批域对外门面接口 (ApprovalFacade)
 *
 * <p>遵循 ADR 0001 & ADR 0003 规范，为交易域提供审批单生成与状态查询服务，
 * 严禁交易模块直接操作审批数据表或暴露审批通过/驳回接口给买方前端。</p>
 *
 * @author b2b-commerce-backend
 */
public interface ApprovalFacade {

    /**
     * 根据订单 ID 查询关联的审批请求详情与时间线
     *
     * @param orderId 采购订单 ID，不可为空
     * @return 审批请求详情 DTO，若尚未生成返回 null
     */
    ApprovalRequestDTO getApprovalByOrderId(String orderId);

    /**
     * 创建审批请求工作项
     *
     * <p>在订单提交成功后调用，初始状态必须且只能为 pending（待审批）。</p>
     *
     * @param orderId     采购订单 ID，不可为空
     * @param orderNo     采购订单号，不可为空
     * @param companyId   所属企业 ID，不可为空
     * @param submittedBy 提交人用户 ID，不可为空
     * @return 生成的审批请求 DTO
     * @throws BizException 当参数不合法或重复创建时抛出
     */
    ApprovalRequestDTO createApprovalRequest(String orderId, String orderNo, String companyId, String submittedBy) throws BizException;
}
