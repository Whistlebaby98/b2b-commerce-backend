package com.b2b.trade.controller;

import com.b2b.common.api.ApiResponse;
import com.b2b.common.api.PageResult;
import com.b2b.trade.api.dto.CreateOrderRequest;
import com.b2b.trade.api.dto.OrderDTO;
import com.b2b.trade.api.dto.OrderQueryRequest;
import com.b2b.trade.service.OrderApplicationService;
import com.b2b.trade.service.OrderQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购订单管理控制器 (OrderController)
 *
 * <p>提供采购订单创建（防重幂等核销）、企业订单分页检索及订单详情（含不可变快照与审批时间线）查询。</p>
 *
 * @author b2b-commerce-backend
 */
@Tag(name = "01. 采购订单中心", description = "采购订单防重提交、分页检索与订单详情快照查询")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderApplicationService;
    private final OrderQueryService orderQueryService;

    @Operation(summary = "提交创建采购订单", description = "核销 checkout_token，CAS 预占企业授信，持久化不可变快照并生成 pending_approval 订单")
    @PostMapping
    public ApiResponse<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest req) {
        return ApiResponse.success(orderApplicationService.createOrder(req));
    }

    @Operation(summary = "分页查询当前企业订单列表", description = "支持按订单状态、订单号或附言关键词进行多租户订单分页查询")
    @GetMapping
    public ApiResponse<PageResult<OrderDTO>> listOrders(OrderQueryRequest req) {
        if (req == null) {
            req = new OrderQueryRequest();
        }
        return ApiResponse.success(orderQueryService.listOrders(req));
    }

    @Operation(summary = "查询采购订单详情", description = "根据订单 ID 获取订单主档、订单行不可变阶梯快照、地址发票快照及审批流转时间线")
    @GetMapping("/{id}")
    public ApiResponse<OrderDTO> getOrderDetail(
            @Parameter(description = "采购订单 ID", required = true)
            @PathVariable("id") String id
    ) {
        return ApiResponse.success(orderQueryService.getOrderDetail(id));
    }
}
