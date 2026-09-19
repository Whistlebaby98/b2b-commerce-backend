package com.b2b.trade.service;

import com.b2b.catalog.api.CatalogFacade;
import com.b2b.catalog.api.dto.SkuDTO;
import com.b2b.common.api.ResultCode;
import com.b2b.common.tenant.TenantContextHolder;
import com.b2b.common.exception.BizException;
import com.b2b.pricing.api.PricingFacade;
import com.b2b.trade.api.dto.AddToCartRequest;
import com.b2b.trade.api.dto.BatchSelectCartRequest;
import com.b2b.trade.api.dto.CartDTO;
import com.b2b.trade.api.dto.CartItemDTO;
import com.b2b.trade.api.dto.CartTotalsDTO;
import com.b2b.trade.infrastructure.persistence.entity.CartItemPO;
import com.b2b.trade.infrastructure.persistence.mapper.CartItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 采购车应用服务 (CartApplicationService)
 *
 * <p>提供当前企业买方采购车增删改查、批量勾选、阶梯价重算及金额汇总。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartApplicationService {

    private final CartItemMapper cartItemMapper;
    private final CatalogFacade catalogFacade;
    private final PricingFacade pricingFacade;

    /**
     * 获取当前企业用户采购车聚合详情
     */
    public CartDTO getCart() {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        String userId = TenantContextHolder.getUserId();

        List<CartItemPO> pos = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItemPO>()
                        .eq(CartItemPO::getCompanyId, companyId)
                        .eq(userId != null, CartItemPO::getUserId, userId)
                        .orderByDesc(CartItemPO::getCreatedAt)
        );

        if (pos.isEmpty()) {
            return CartDTO.builder()
                    .id("cart_" + companyId)
                    .companyId(companyId)
                    .currency("CNY")
                    .items(Collections.emptyList())
                    .couponCode(null)
                    .totals(CartTotalsDTO.builder()
                            .currency("CNY")
                            .itemCount(0)
                            .subtotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .listSubtotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .promotionDiscount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .shippingFee(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .tax(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .total(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                            .build())
                    .updatedAt(Instant.now())
                    .build();
        }

        List<String> skuIds = pos.stream().map(CartItemPO::getSkuId).distinct().collect(Collectors.toList());
        Map<String, SkuDTO> skuMap = catalogFacade.getSkusByIds(skuIds);

        List<CartItemDTO> itemDTOs = new ArrayList<>();
        int selectedItemCount = 0;
        BigDecimal selectedSubtotal = BigDecimal.ZERO;
        BigDecimal selectedListSubtotal = BigDecimal.ZERO;

        for (CartItemPO po : pos) {
            SkuDTO sku = skuMap.get(po.getSkuId());
            if (sku == null) {
                log.warn("[Cart] 采购车中的 SKU 已失效或下架: skuId={}", po.getSkuId());
                continue;
            }

            // 动态根据加购数量获取阶梯成交价
            BigDecimal unitPrice = pricingFacade.getTierUnitPrice(companyId, po.getSkuId(), po.getQuantity());
            BigDecimal listPrice = sku.getListPrice() != null ? sku.getListPrice() : (sku.getPrice() != null ? sku.getPrice() : unitPrice);

            CartItemDTO dto = CartItemDTO.builder()
                    .id(po.getId())
                    .productId(sku.getProductId())
                    .categoryId(null)
                    .skuId(sku.getId())
                    .skuCode(sku.getCode())
                    .productTitle(sku.getName())
                    .skuName(sku.getName())
                    .unit(sku.getUnit() != null ? sku.getUnit() : "件")
                    .unitPrice(unitPrice)
                    .listUnitPrice(listPrice)
                    .quantity(po.getQuantity())
                    .selected(po.getSelected())
                    .promotionIds(List.of("promo-september-agreement"))
                    .addedAt(po.getCreatedAt())
                    .build();

            itemDTOs.add(dto);

            if (Boolean.TRUE.equals(po.getSelected())) {
                selectedItemCount += po.getQuantity();
                BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(po.getQuantity()));
                BigDecimal lineListSubtotal = listPrice.multiply(BigDecimal.valueOf(po.getQuantity()));
                selectedSubtotal = selectedSubtotal.add(lineSubtotal);
                selectedListSubtotal = selectedListSubtotal.add(lineListSubtotal);
            }
        }

        BigDecimal promotionDiscount = selectedListSubtotal.subtract(selectedSubtotal);
        if (promotionDiscount.compareTo(BigDecimal.ZERO) < 0) {
            promotionDiscount = BigDecimal.ZERO;
        }

        BigDecimal shippingFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = selectedSubtotal.add(shippingFee);

        CartTotalsDTO totals = CartTotalsDTO.builder()
                .currency("CNY")
                .itemCount(selectedItemCount)
                .subtotal(selectedSubtotal.setScale(2, RoundingMode.HALF_UP))
                .listSubtotal(selectedListSubtotal.setScale(2, RoundingMode.HALF_UP))
                .promotionDiscount(promotionDiscount.setScale(2, RoundingMode.HALF_UP))
                .shippingFee(shippingFee)
                .tax(tax)
                .total(total.setScale(2, RoundingMode.HALF_UP))
                .build();

        return CartDTO.builder()
                .id("cart_" + companyId)
                .companyId(companyId)
                .currency("CNY")
                .items(itemDTOs)
                .couponCode(null)
                .totals(totals)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 添加商品至采购车 (同 SKU 增量合并)
     */
    @Transactional(rollbackFor = Exception.class)
    public CartDTO addItem(AddToCartRequest req) {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        String userId = TenantContextHolder.getUserId();

        // 校验 SKU 是否存在
        catalogFacade.getSkuById(req.getSkuId());

        CartItemPO existing = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItemPO>()
                        .eq(CartItemPO::getCompanyId, companyId)
                        .eq(userId != null, CartItemPO::getUserId, userId)
                        .eq(CartItemPO::getSkuId, req.getSkuId())
        );

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + req.getQuantity());
            existing.setSelected(true);
            existing.setUpdatedAt(Instant.now());
            cartItemMapper.updateById(existing);
        } else {
            CartItemPO newItem = CartItemPO.builder()
                    .id("cart_item_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                    .companyId(companyId)
                    .userId(userId != null ? userId : "guest")
                    .skuId(req.getSkuId())
                    .quantity(req.getQuantity())
                    .selected(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            cartItemMapper.insert(newItem);
        }

        return getCart();
    }

    /**
     * 修改采购车行数量
     */
    @Transactional(rollbackFor = Exception.class)
    public CartDTO updateQuantity(String itemId, Integer quantity) {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        String userId = TenantContextHolder.getUserId();

        CartItemPO item = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItemPO>()
                        .eq(CartItemPO::getCompanyId, companyId)
                        .eq(userId != null, CartItemPO::getUserId, userId)
                        .eq(CartItemPO::getId, itemId)
        );

        if (item == null) {
            throw new BizException(ResultCode.NOT_FOUND, "采购车行项不存在: " + itemId);
        }

        item.setQuantity(quantity);
        item.setUpdatedAt(Instant.now());
        cartItemMapper.updateById(item);

        return getCart();
    }

    /**
     * 切换单个商品勾选状态
     */
    @Transactional(rollbackFor = Exception.class)
    public CartDTO toggleSelect(String itemId) {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        String userId = TenantContextHolder.getUserId();

        CartItemPO item = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItemPO>()
                        .eq(CartItemPO::getCompanyId, companyId)
                        .eq(userId != null, CartItemPO::getUserId, userId)
                        .eq(CartItemPO::getId, itemId)
        );

        if (item == null) {
            throw new BizException(ResultCode.NOT_FOUND, "采购车行项不存在: " + itemId);
        }

        item.setSelected(!Boolean.TRUE.equals(item.getSelected()));
        item.setUpdatedAt(Instant.now());
        cartItemMapper.updateById(item);

        return getCart();
    }

    /**
     * 批量勾选/取消勾选商品
     */
    @Transactional(rollbackFor = Exception.class)
    public CartDTO batchSelect(BatchSelectCartRequest req) {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        String userId = TenantContextHolder.getUserId();

        LambdaUpdateWrapper<CartItemPO> updateWrapper = new LambdaUpdateWrapper<CartItemPO>()
                .eq(CartItemPO::getCompanyId, companyId)
                .eq(userId != null, CartItemPO::getUserId, userId)
                .set(CartItemPO::getSelected, req.getSelected())
                .set(CartItemPO::getUpdatedAt, Instant.now());

        if (req.getItemIds() != null && !req.getItemIds().isEmpty()) {
            updateWrapper.in(CartItemPO::getId, req.getItemIds());
        }

        cartItemMapper.update(null, updateWrapper);
        return getCart();
    }

    /**
     * 删除采购车单行项
     */
    @Transactional(rollbackFor = Exception.class)
    public CartDTO removeItem(String itemId) {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        String userId = TenantContextHolder.getUserId();

        cartItemMapper.delete(
                new LambdaQueryWrapper<CartItemPO>()
                        .eq(CartItemPO::getCompanyId, companyId)
                        .eq(userId != null, CartItemPO::getUserId, userId)
                        .eq(CartItemPO::getId, itemId)
        );

        return getCart();
    }

    /**
     * 清空当前企业采购车
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearCart() {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        String userId = TenantContextHolder.getUserId();

        cartItemMapper.delete(
                new LambdaQueryWrapper<CartItemPO>()
                        .eq(CartItemPO::getCompanyId, companyId)
                        .eq(userId != null, CartItemPO::getUserId, userId)
        );
    }
}
