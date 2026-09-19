package com.b2b.trade.service.impl;

import com.b2b.common.api.ResultCode;
import com.b2b.common.exception.BizException;
import com.b2b.finance.api.FinanceFacade;
import com.b2b.trade.api.TradeFacade;
import com.b2b.trade.domain.enums.ApprovalStatus;
import com.b2b.trade.domain.enums.OrderStatus;
import com.b2b.trade.domain.enums.PaymentMethod;
import com.b2b.trade.infrastructure.persistence.entity.OrderPO;
import com.b2b.trade.infrastructure.persistence.mapper.OrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 交易与履约域门面实现类 (TradeFacadeImpl)
 *
 * <p>承接审批通过/驳回回调，驱动订单状态机流转并联动授信释放。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TradeFacadeImpl implements TradeFacade {

    private final OrderMapper orderMapper;
    private final FinanceFacade financeFacade;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onApprovalPassed(String orderId, String operatorId, String comment) throws BizException {
        log.info("[Trade] 收到审批通过回调: orderId={}, operatorId={}, comment={}", orderId, operatorId, comment);

        OrderPO order = orderMapper.selectOne(
                new LambdaQueryWrapper<OrderPO>().eq(OrderPO::getId, orderId)
        );

        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND, "采购订单不存在: " + orderId);
        }

        if (!OrderStatus.PENDING_APPROVAL.getCode().equals(order.getStatus())) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID, "订单状态不是待审批，无法推进: " + order.getStatus());
        }

        order.setStatus(OrderStatus.APPROVED.getCode());
        order.setApprovalStatus(ApprovalStatus.APPROVED.getCode());
        order.setUpdatedAt(Instant.now());
        orderMapper.updateById(order);

        log.info("[Trade] 订单状态已推进为已审批: orderId={}, orderNo={}", orderId, order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onApprovalRejected(String orderId, String operatorId, String reason) throws BizException {
        log.info("[Trade] 收到审批驳回回调: orderId={}, operatorId={}, reason={}", orderId, operatorId, reason);

        OrderPO order = orderMapper.selectOne(
                new LambdaQueryWrapper<OrderPO>().eq(OrderPO::getId, orderId)
        );

        if (order == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND, "采购订单不存在: " + orderId);
        }

        if (!OrderStatus.PENDING_APPROVAL.getCode().equals(order.getStatus())) {
            throw new BizException(ResultCode.ORDER_STATUS_INVALID, "订单状态不是待审批，无法驳回: " + order.getStatus());
        }

        order.setStatus(OrderStatus.REJECTED.getCode());
        order.setApprovalStatus(ApprovalStatus.REJECTED.getCode());
        order.setUpdatedAt(Instant.now());
        orderMapper.updateById(order);

        // 若为授信账期支付，自动释放已占用的授信额度
        if (PaymentMethod.CREDIT_ACCOUNT.getCode().equalsIgnoreCase(order.getPaymentMethod())) {
            log.info("[Trade] 审批驳回，开始释放企业预占授信: companyId={}, orderId={}, amount={}",
                    order.getCompanyId(), order.getId(), order.getTotal());
            financeFacade.releaseCredit(order.getCompanyId(), order.getId(), order.getTotal());
        }

        log.info("[Trade] 订单状态已流转为已驳回: orderId={}, orderNo={}", orderId, order.getOrderNo());
    }
}
