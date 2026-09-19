package com.b2b.trade.api;

import com.b2b.common.exception.BizException;

/**
 * 交易与履约域对外门面接口 (TradeFacade)
 *
 * <p>供其他模块（如审批结果回调、财务结算反写）推进订单状态，
 * 严禁外部模块直接更新订单表。</p>
 *
 * @author b2b-commerce-backend
 */
public interface TradeFacade {

    /**
     * 推进订单审批通过流转
     *
     * <p>当审批工作流通过时触发，订单状态从 pending_approval 推进为 approved。</p>
     *
     * @param orderId   采购订单 ID，不可为空
     * @param operatorId 审批人用户 ID
     * @param comment   审批通过附言
     * @throws BizException 当订单不存在或状态不为 pending_approval 时抛出
     */
    void onApprovalPassed(String orderId, String operatorId, String comment) throws BizException;

    /**
     * 推进订单审批驳回流转
     *
     * <p>当审批驳回时触发，订单状态推进为 rejected，并触发授信额度释放。</p>
     *
     * @param orderId   采购订单 ID，不可为空
     * @param operatorId 审批人用户 ID
     * @param reason    驳回原因说明
     * @throws BizException 当订单不存在或状态不为 pending_approval 时抛出
     */
    void onApprovalRejected(String orderId, String operatorId, String reason) throws BizException;
}
