package com.b2b.trade.service;

import com.b2b.catalog.api.CatalogFacade;
import com.b2b.catalog.api.dto.SkuDTO;
import com.b2b.common.api.ResultCode;
import com.b2b.common.tenant.TenantContextHolder;
import com.b2b.common.exception.BizException;
import com.b2b.finance.api.FinanceFacade;
import com.b2b.finance.api.dto.AddressDTO;
import com.b2b.finance.api.dto.CreditCheckDTO;
import com.b2b.finance.api.dto.InvoiceDTO;
import com.b2b.pricing.api.PricingFacade;
import com.b2b.pricing.api.dto.PricingRequestDTO;
import com.b2b.pricing.api.dto.PricingSnapshotDTO;
import com.b2b.trade.api.dto.CartItemDTO;
import com.b2b.trade.api.dto.CartTotalsDTO;
import com.b2b.trade.api.dto.CheckoutPreviewDTO;
import com.b2b.trade.api.dto.CheckoutPreviewRequest;
import com.b2b.trade.infrastructure.persistence.entity.CartItemPO;
import com.b2b.trade.infrastructure.persistence.mapper.CartItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 结算试算会话服务 (CheckoutApplicationService)
 *
 * <p>提供结算金额试算、地址/发票匹配、企业授信预核验及防重 Token 发放。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutApplicationService {

    private final CartItemMapper cartItemMapper;
    private final CatalogFacade catalogFacade;
    private final PricingFacade pricingFacade;
    private final FinanceFacade financeFacade;
    private final IdempotencyTokenService idempotencyTokenService;

    /**
     * 结算金额试算与防重 Token 预领
     */
    public CheckoutPreviewDTO previewCheckout(CheckoutPreviewRequest req) {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        String userId = TenantContextHolder.getUserId();

        List<CheckoutItemHolder> itemsToCheckout = new ArrayList<>();

        if (req.getItems() != null && !req.getItems().isEmpty()) {
            for (CheckoutPreviewRequest.DirectCheckoutItem item : req.getItems()) {
                itemsToCheckout.add(new CheckoutItemHolder(null, item.getSkuId(), item.getQuantity()));
            }
        } else {
            List<CartItemPO> selectedCartItems = cartItemMapper.selectList(
                    new LambdaQueryWrapper<CartItemPO>()
                            .eq(CartItemPO::getCompanyId, companyId)
                            .eq(userId != null, CartItemPO::getUserId, userId)
                            .eq(CartItemPO::getSelected, true)
            );

            if (selectedCartItems.isEmpty()) {
                throw new BizException(ResultCode.CART_EMPTY, "当前购物车没有勾选任何商品，无法结算");
            }

            for (CartItemPO po : selectedCartItems) {
                itemsToCheckout.add(new CheckoutItemHolder(po.getId(), po.getSkuId(), po.getQuantity()));
            }
        }

        // 批量查询 SKU 档案
        List<String> skuIds = itemsToCheckout.stream().map(CheckoutItemHolder::getSkuId).distinct().collect(Collectors.toList());
        Map<String, SkuDTO> skuMap = catalogFacade.getSkusByIds(skuIds);

        // 调用计价流水线
        List<PricingRequestDTO.PricingItem> pricingItems = itemsToCheckout.stream()
                .map(it -> PricingRequestDTO.PricingItem.builder()
                        .skuId(it.getSkuId())
                        .quantity(it.getQuantity())
                        .build())
                .collect(Collectors.toList());

        PricingSnapshotDTO pricingSnapshot = pricingFacade.calculatePricing(
                PricingRequestDTO.builder()
                        .companyId(companyId)
                        .items(pricingItems)
                        .build()
        );

        // 组装 CartItemDTO 列表与汇总
        List<CartItemDTO> itemDTOs = new ArrayList<>();
        int totalItemCount = 0;

        Map<String, PricingSnapshotDTO.LinePricingSnapshot> snapshotMap = pricingSnapshot.getLineSnapshots().stream()
                .collect(Collectors.toMap(PricingSnapshotDTO.LinePricingSnapshot::getSkuId, s -> s, (k1, k2) -> k1));

        for (CheckoutItemHolder item : itemsToCheckout) {
            SkuDTO sku = skuMap.get(item.getSkuId());
            if (sku == null) {
                throw new BizException(ResultCode.NOT_FOUND, "商品 SKU 不存在或已下架: " + item.getSkuId());
            }

            PricingSnapshotDTO.LinePricingSnapshot lineSnap = snapshotMap.get(item.getSkuId());
            BigDecimal unitPrice = lineSnap != null ? lineSnap.getFinalUnitPrice() : sku.getPrice();
            BigDecimal listUnitPrice = sku.getListPrice() != null ? sku.getListPrice() : sku.getPrice();

            totalItemCount += item.getQuantity();

            CartItemDTO dto = CartItemDTO.builder()
                    .id(item.getCartItemId() != null ? item.getCartItemId() : "direct_" + item.getSkuId())
                    .productId(sku.getProductId())
                    .skuId(sku.getId())
                    .skuCode(sku.getCode())
                    .productTitle(sku.getName())
                    .skuName(sku.getName())
                    .unit(sku.getUnit() != null ? sku.getUnit() : "件")
                    .unitPrice(unitPrice)
                    .listUnitPrice(listUnitPrice)
                    .quantity(item.getQuantity())
                    .selected(true)
                    .promotionIds(List.of("promo-september-agreement"))
                    .addedAt(Instant.now())
                    .build();

            itemDTOs.add(dto);
        }

        CartTotalsDTO totals = CartTotalsDTO.builder()
                .currency(pricingSnapshot.getCurrency())
                .itemCount(totalItemCount)
                .subtotal(pricingSnapshot.getSubtotal())
                .listSubtotal(pricingSnapshot.getListSubtotal())
                .promotionDiscount(pricingSnapshot.getPromotionDiscount())
                .shippingFee(pricingSnapshot.getShippingFee())
                .tax(pricingSnapshot.getTax())
                .total(pricingSnapshot.getTotal())
                .build();

        // 匹配收货地址
        List<AddressDTO> addresses = financeFacade.listAddresses(companyId);
        AddressDTO selectedAddress = null;
        if (req.getAddressId() != null) {
            selectedAddress = addresses.stream()
                    .filter(a -> a.getId().equals(req.getAddressId()))
                    .findFirst()
                    .orElse(null);
        }
        if (selectedAddress == null && !addresses.isEmpty()) {
            selectedAddress = addresses.stream().filter(AddressDTO::getIsDefault).findFirst().orElse(addresses.get(0));
        }

        // 匹配开票资质
        List<InvoiceDTO> invoices = financeFacade.listInvoices(companyId);
        InvoiceDTO selectedInvoice = null;
        if (req.getInvoiceId() != null) {
            selectedInvoice = invoices.stream()
                    .filter(inv -> inv.getId().equals(req.getInvoiceId()))
                    .findFirst()
                    .orElse(null);
        }
        if (selectedInvoice == null && !invoices.isEmpty()) {
            selectedInvoice = invoices.stream().filter(InvoiceDTO::getIsDefault).findFirst().orElse(invoices.get(0));
        }

        // 预检企业可用授信
        CreditCheckDTO creditCheck = financeFacade.checkCredit(companyId, totals.getTotal());

        // 发放防重结算 Token
        String checkoutToken = idempotencyTokenService.generateCheckoutToken(companyId, userId);

        return CheckoutPreviewDTO.builder()
                .checkoutToken(checkoutToken)
                .tokenExpiresInSeconds(1800L)
                .items(itemDTOs)
                .totals(totals)
                .selectedAddress(selectedAddress)
                .selectedInvoice(selectedInvoice)
                .creditCheck(creditCheck)
                .build();
    }

    /**
     * 内部结算行项包装
     */
    public static class CheckoutItemHolder {
        private final String cartItemId;
        private final String skuId;
        private final Integer quantity;

        public CheckoutItemHolder(String cartItemId, String skuId, Integer quantity) {
            this.cartItemId = cartItemId;
            this.skuId = skuId;
            this.quantity = quantity;
        }

        public String getCartItemId() { return cartItemId; }
        public String getSkuId() { return skuId; }
        public Integer getQuantity() { return quantity; }
    }
}
