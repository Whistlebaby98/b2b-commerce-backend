package com.b2b.trade.service;

import com.b2b.common.api.ResultCode;
import com.b2b.common.exception.BizException;
import com.b2b.finance.api.FinanceFacade;
import com.b2b.trade.domain.enums.ApprovalStatus;
import com.b2b.trade.domain.enums.OrderStatus;
import com.b2b.trade.domain.enums.PaymentMethod;
import com.b2b.trade.infrastructure.persistence.entity.OrderPO;
import com.b2b.trade.infrastructure.persistence.mapper.OrderMapper;
import com.b2b.trade.service.impl.TradeFacadeImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 交易域门面状态机流转测试 (TradeFacadeTest)
 *
 * <p>验证审批通过/驳回回调、状态机流转与授信额度自动回滚。</p>
 *
 * @author b2b-commerce-backend
 */
@ExtendWith(MockitoExtension.class)
class TradeFacadeTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private FinanceFacade financeFacade;

    @InjectMocks
    private TradeFacadeImpl tradeFacade;

    @Test
    @DisplayName("验证审批通过流转：订单状态从 pending_approval 推进为 approved")
    void testOnApprovalPassed() {
        OrderPO order = OrderPO.builder()
                .id("ord-1")
                .orderNo("PO202609190001")
                .companyId("company-lantu")
                .status(OrderStatus.PENDING_APPROVAL.getCode())
                .approvalStatus(ApprovalStatus.PENDING.getCode())
                .paymentMethod(PaymentMethod.CREDIT_ACCOUNT.getCode())
                .total(new BigDecimal("5000.00"))
                .build();

        when(orderMapper.selectOne(any())).thenReturn(order);

        tradeFacade.onApprovalPassed("ord-1", "approver-01", "同意批量采购");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.APPROVED.getCode());
        assertThat(order.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED.getCode());
        verify(orderMapper).updateById(order);
    }

    @Test
    @DisplayName("验证审批驳回流转：订单状态流转为 rejected，并自动触发授信额度释放")
    void testOnApprovalRejected() {
        OrderPO order = OrderPO.builder()
                .id("ord-2")
                .orderNo("PO202609190002")
                .companyId("company-lantu")
                .status(OrderStatus.PENDING_APPROVAL.getCode())
                .approvalStatus(ApprovalStatus.PENDING.getCode())
                .paymentMethod(PaymentMethod.CREDIT_ACCOUNT.getCode())
                .total(new BigDecimal("8800.00"))
                .build();

        when(orderMapper.selectOne(any())).thenReturn(order);

        tradeFacade.onApprovalRejected("ord-2", "approver-01", "本月部门预算已超支");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.REJECTED.getCode());
        assertThat(order.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED.getCode());
        verify(orderMapper).updateById(order);

        // 验证必须联动调用 financeFacade 释放 8800.00 授信额度
        verify(financeFacade).releaseCredit("company-lantu", "ord-2", new BigDecimal("8800.00"));
    }

    @Test
    @DisplayName("验证非法状态流转拦截：非 pending_approval 状态不可审批")
    void testOnApprovalInvalidStatus() {
        OrderPO order = OrderPO.builder()
                .id("ord-3")
                .status(OrderStatus.APPROVED.getCode())
                .build();

        when(orderMapper.selectOne(any())).thenReturn(order);

        assertThatThrownBy(() -> tradeFacade.onApprovalPassed("ord-3", "approver-01", "重复审批"))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", ResultCode.ORDER_STATUS_INVALID.getCode());
    }
}
