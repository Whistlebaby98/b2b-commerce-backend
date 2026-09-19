package com.b2b.trade.controller;

import com.b2b.common.api.ApiResponse;
import com.b2b.trade.api.dto.AddToCartRequest;
import com.b2b.trade.api.dto.BatchSelectCartRequest;
import com.b2b.trade.api.dto.CartDTO;
import com.b2b.trade.api.dto.UpdateCartQuantityRequest;
import com.b2b.trade.service.CartApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购车接口控制器 (CartController)
 *
 * <p>提供当前企业上下文下的采购车查询与行项操作，对齐前端 StoreProvider 契约。</p>
 *
 * @author b2b-commerce-backend
 */
@Tag(name = "01. 采购车管理", description = "提供采购车查询、增删改商品及勾选结算状态管理")
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartApplicationService cartApplicationService;

    @Operation(summary = "获取当前企业采购车", description = "根据请求头 X-Organization-Id 查询属于当前企业上下文的采购车及阶梯金额汇总")
    @GetMapping
    public ApiResponse<CartDTO> getCart() {
        return ApiResponse.success(cartApplicationService.getCart());
    }

    @Operation(summary = "添加商品至采购车", description = "支持同 SKU 增量合并，并依据新总量重新计算阶梯单价")
    @PostMapping("/items")
    public ApiResponse<CartDTO> addItem(@Valid @RequestBody AddToCartRequest req) {
        return ApiResponse.success(cartApplicationService.addItem(req));
    }

    @Operation(summary = "修改采购车行数量", description = "修改指定商品加购数量，触发阶梯区间匹配与总金额重算")
    @PutMapping("/items/{id}/quantity")
    public ApiResponse<CartDTO> updateQuantity(
            @Parameter(description = "采购车行 ID", required = true)
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateCartQuantityRequest req
    ) {
        return ApiResponse.success(cartApplicationService.updateQuantity(id, req.getQuantity()));
    }

    @Operation(summary = "切换商品勾选状态", description = "切换指定商品的勾选/取消勾选状态，联动重算结算总额")
    @PutMapping("/items/{id}/select")
    public ApiResponse<CartDTO> toggleSelect(
            @Parameter(description = "采购车行 ID", required = true)
            @PathVariable("id") String id
    ) {
        return ApiResponse.success(cartApplicationService.toggleSelect(id));
    }

    @Operation(summary = "批量勾选商品", description = "支持全选、全不选或批量指定采购车行勾选状态")
    @PutMapping("/items/batch-select")
    public ApiResponse<CartDTO> batchSelect(@Valid @RequestBody BatchSelectCartRequest req) {
        return ApiResponse.success(cartApplicationService.batchSelect(req));
    }

    @Operation(summary = "删除采购车单行项", description = "从当前采购车中移除指定行项商品")
    @DeleteMapping("/items/{id}")
    public ApiResponse<CartDTO> removeItem(
            @Parameter(description = "采购车行 ID", required = true)
            @PathVariable("id") String id
    ) {
        return ApiResponse.success(cartApplicationService.removeItem(id));
    }

    @Operation(summary = "清空当前企业采购车", description = "清空当前企业买方采购车中的所有商品")
    @DeleteMapping("/clear")
    public ApiResponse<Void> clearCart() {
        cartApplicationService.clearCart();
        return ApiResponse.success(null);
    }
}
