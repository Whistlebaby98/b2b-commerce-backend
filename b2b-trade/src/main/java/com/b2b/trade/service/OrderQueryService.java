package com.b2b.trade.service;

import com.b2b.approval.api.ApprovalFacade;
import com.b2b.approval.api.dto.ApprovalRequestDTO;
import com.b2b.common.api.PageResult;
import com.b2b.common.api.ResultCode;
import com.b2b.common.tenant.TenantContextHolder;
import com.b2b.common.exception.BizException;
import com.b2b.trade.api.dto.OrderDTO;
import com.b2b.trade.api.dto.OrderLineDTO;
import com.b2b.trade.api.dto.OrderQueryRequest;
import com.b2b.trade.api.dto.OrderTotalsDTO;
import com.b2b.trade.infrastructure.persistence.entity.OrderLinePO;
import com.b2b.trade.infrastructure.persistence.entity.OrderPO;
import com.b2b.trade.infrastructure.persistence.mapper.OrderLineMapper;
import com.b2b.trade.infrastructure.persistence.mapper.OrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购订单查询应用服务 (OrderQueryService)
 *
 * <p>提供租户级订单多维筛选、分页列表与订单完整详情（含快照与审批轨迹）检索。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderMapper orderMapper;
    private final OrderLineMapper orderLineMapper;
    private final ApprovalFacade approvalFacade;

    /**
     * 分页多维筛选查询订单列表
     */
    public PageResult<OrderDTO> listOrders(OrderQueryRequest req) {
        String companyId = TenantContextHolder.getRequiredCompanyId();

        int pageNum = req.getPage() != null && req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() != null && req.getPageSize() > 0 ? req.getPageSize() : 10;

        LambdaQueryWrapper<OrderPO> queryWrapper = new LambdaQueryWrapper<OrderPO>()
                .eq(OrderPO::getCompanyId, companyId)
                .orderByDesc(OrderPO::getCreatedAt);

        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            queryWrapper.eq(OrderPO::getStatus, req.getStatus());
        }

        if (req.getQuery() != null && !req.getQuery().isBlank()) {
            queryWrapper.and(w -> w.like(OrderPO::getOrderNo, req.getQuery())
                    .or()
                    .like(OrderPO::getNote, req.getQuery()));
        }

        Page<OrderPO> mpPage = new Page<>(pageNum, pageSize);
        Page<OrderPO> resultPage = orderMapper.selectPage(mpPage, queryWrapper);

        List<OrderPO> records = resultPage.getRecords();
        if (records.isEmpty()) {
            return PageResult.<OrderDTO>builder()
                    .items(Collections.emptyList())
                    .total(resultPage.getTotal())
                    .page(pageNum)
                    .pageSize(pageSize)
                    .totalPages((int) resultPage.getPages())
                    .build();
        }

        // 批量查询所有订单行
        List<String> orderIds = records.stream().map(OrderPO::getId).collect(Collectors.toList());
        List<OrderLinePO> allLines = orderLineMapper.selectList(
                new LambdaQueryWrapper<OrderLinePO>().in(OrderLinePO::getOrderId, orderIds)
        );

        Map<String, List<OrderLinePO>> lineMap = allLines.stream()
                .collect(Collectors.groupingBy(OrderLinePO::getOrderId));

        List<OrderDTO> dtos = records.stream().map(po -> {
            List<OrderLinePO> lines = lineMap.getOrDefault(po.getId(), Collections.emptyList());
            return toDTO(po, lines, null);
        }).collect(Collectors.toList());

        return PageResult.<OrderDTO>builder()
                .items(dtos)
                .total(resultPage.getTotal())
                .page(pageNum)
                .pageSize(pageSize)
                .totalPages((int) resultPage.getPages())
                .build();
    }

    /**
     * 获取指定订单完整详情 (包含不可变快照与审批时间线)
     */
    public OrderDTO getOrderDetail(String orderId) {
        String companyId = TenantContextHolder.getRequiredCompanyId();

        OrderPO orderPO = orderMapper.selectOne(
                new LambdaQueryWrapper<OrderPO>()
                        .eq(OrderPO::getCompanyId, companyId)
                        .eq(OrderPO::getId, orderId)
        );

        if (orderPO == null) {
            throw new BizException(ResultCode.ORDER_NOT_FOUND, "采购订单不存在: " + orderId);
        }

        List<OrderLinePO> lines = orderLineMapper.selectList(
                new LambdaQueryWrapper<OrderLinePO>().eq(OrderLinePO::getOrderId, orderId)
        );

        ApprovalRequestDTO approval = approvalFacade.getApprovalByOrderId(orderId);

        return toDTO(orderPO, lines, approval);
    }

    private OrderDTO toDTO(OrderPO po, List<OrderLinePO> lines, ApprovalRequestDTO approval) {
        List<OrderLineDTO> lineDTOs = lines.stream().map(l -> OrderLineDTO.builder()
                .id(l.getId())
                .productId(l.getProductId())
                .skuId(l.getSkuId())
                .skuCode(l.getSkuCode())
                .productTitle(l.getProductTitle())
                .skuName(l.getSkuName())
                .attributes(l.getAttributes())
                .unit(l.getUnit())
                .quantity(l.getQuantity())
                .unitPrice(l.getUnitPrice())
                .listUnitPrice(l.getListUnitPrice())
                .promotionDiscount(l.getPromotionDiscount())
                .subtotal(l.getSubtotal())
                .pricingSnapshot(l.getPricingSnapshot())
                .build()
        ).collect(Collectors.toList());

        return OrderDTO.builder()
                .id(po.getId())
                .orderNo(po.getOrderNo())
                .companyId(po.getCompanyId())
                .createdBy(po.getCreatedBy())
                .status(po.getStatus())
                .approvalStatus(po.getApprovalStatus())
                .paymentMethod(po.getPaymentMethod())
                .lines(lineDTOs)
                .totals(OrderTotalsDTO.builder()
                        .currency(po.getCurrency())
                        .subtotal(po.getSubtotal())
                        .listSubtotal(po.getListSubtotal())
                        .promotionDiscount(po.getPromotionDiscount())
                        .shippingFee(po.getShippingFee())
                        .tax(po.getTax())
                        .total(po.getTotal())
                        .build())
                .shippingAddress(po.getShippingAddressSnapshot())
                .invoice(po.getInvoiceSnapshot())
                .note(po.getNote())
                .requestedDeliveryDate(po.getRequestedDeliveryDate())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .submittedAt(po.getSubmittedAt())
                .approval(approval)
                .build();
    }
}
