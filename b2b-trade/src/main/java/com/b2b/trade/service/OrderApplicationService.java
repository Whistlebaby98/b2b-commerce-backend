package com.b2b.trade.service;

import com.b2b.approval.api.ApprovalFacade;
import com.b2b.approval.api.dto.ApprovalRequestDTO;
import com.b2b.catalog.api.CatalogFacade;
import com.b2b.catalog.api.dto.SkuDTO;
import com.b2b.common.api.ResultCode;
import com.b2b.common.tenant.TenantContextHolder;
import com.b2b.common.exception.BizException;
import com.b2b.common.outbox.OutboxMapper;
import com.b2b.common.outbox.OutboxMessage;
import com.b2b.finance.api.FinanceFacade;
import com.b2b.finance.api.dto.AddressSnapshotDTO;
import com.b2b.finance.api.dto.InvoiceSnapshotDTO;
import com.b2b.pricing.api.PricingFacade;
import com.b2b.pricing.api.dto.PricingRequestDTO;
import com.b2b.pricing.api.dto.PricingSnapshotDTO;
import com.b2b.trade.api.dto.CheckoutPreviewRequest;
import com.b2b.trade.api.dto.CreateOrderRequest;
import com.b2b.trade.api.dto.OrderDTO;
import com.b2b.trade.api.dto.OrderLineDTO;
import com.b2b.trade.api.dto.OrderTotalsDTO;
import com.b2b.trade.domain.enums.ApprovalStatus;
import com.b2b.trade.domain.enums.OrderStatus;
import com.b2b.trade.domain.enums.PaymentMethod;
import com.b2b.trade.infrastructure.persistence.entity.CartItemPO;
import com.b2b.trade.infrastructure.persistence.entity.OrderLinePO;
import com.b2b.trade.infrastructure.persistence.entity.OrderPO;
import com.b2b.trade.infrastructure.persistence.mapper.CartItemMapper;
import com.b2b.trade.infrastructure.persistence.mapper.OrderLineMapper;
import com.b2b.trade.infrastructure.persistence.mapper.OrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 采购订单应用服务 (OrderApplicationService)
 *
 * <p>核心交易创建编排：防重 Token 原子核销、CAS 授信预占、不可变快照固化、
 * 本地事务落库订单主子表、本地消息表发布领域事件以及触发两阶段审批。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderMapper orderMapper;
    private final OrderLineMapper orderLineMapper;
    private final CartItemMapper cartItemMapper;
    private final OutboxMapper outboxMapper;
    private final CatalogFacade catalogFacade;
    private final PricingFacade pricingFacade;
    private final FinanceFacade financeFacade;
    private final ApprovalFacade approvalFacade;
    private final IdempotencyTokenService idempotencyTokenService;
    private final ObjectMapper objectMapper;

    /**
     * 提交生成采购订单 (具备金融级幂等与强一致保障)
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO createOrder(CreateOrderRequest req) {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        String userId = TenantContextHolder.getUserId() != null ? TenantContextHolder.getUserId() : "user-anonymous";

        log.info("[Order] 收到订单提交请求: companyId={}, userId={}, paymentMethod={}",
                companyId, userId, req.getPaymentMethod());

        // 1. 原子核销防重 Token
        boolean tokenValid = idempotencyTokenService.consumeCheckoutToken(req.getCheckoutToken());
        if (!tokenValid) {
            throw new BizException(ResultCode.CHECKOUT_TOKEN_INVALID, "结算 Token 无效或已核销，请勿重复提交");
        }

        // 2. 获取待结算行项
        List<CheckoutApplicationService.CheckoutItemHolder> itemsToOrder = new ArrayList<>();
        List<String> cartItemIdsToDelete = new ArrayList<>();

        if (req.getItems() != null && !req.getItems().isEmpty()) {
            for (CheckoutPreviewRequest.DirectCheckoutItem item : req.getItems()) {
                itemsToOrder.add(new CheckoutApplicationService.CheckoutItemHolder(null, item.getSkuId(), item.getQuantity()));
            }
        } else {
            List<CartItemPO> selectedCartItems = cartItemMapper.selectList(
                    new LambdaQueryWrapper<CartItemPO>()
                            .eq(CartItemPO::getCompanyId, companyId)
                            .eq(userId != null, CartItemPO::getUserId, userId)
                            .eq(CartItemPO::getSelected, true)
            );

            if (selectedCartItems.isEmpty()) {
                throw new BizException(ResultCode.CART_EMPTY, "当前购物车没有勾选任何商品，无法下单");
            }

            for (CartItemPO po : selectedCartItems) {
                itemsToOrder.add(new CheckoutApplicationService.CheckoutItemHolder(po.getId(), po.getSkuId(), po.getQuantity()));
                cartItemIdsToDelete.add(po.getId());
            }
        }

        // 3. 批量查询 SKU 信息
        List<String> skuIds = itemsToOrder.stream().map(CheckoutApplicationService.CheckoutItemHolder::getSkuId).distinct().collect(Collectors.toList());
        Map<String, SkuDTO> skuMap = catalogFacade.getSkusByIds(skuIds);

        // 4. 调用无副作用纯内存算价流水线
        List<PricingRequestDTO.PricingItem> pricingItems = itemsToOrder.stream()
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

        // 5. 获取地址与开票资质快照 (不可变快照原则)
        AddressSnapshotDTO addressSnapshot = financeFacade.getAddressSnapshot(companyId, req.getAddressId());
        InvoiceSnapshotDTO invoiceSnapshot = req.getInvoiceId() != null
                ? financeFacade.getInvoiceSnapshot(companyId, req.getInvoiceId())
                : null;

        // 6. 生成唯一订单标识与业务订单号
        String orderId = "ord_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String orderNo = "PO" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now())
                + String.format("%04d", new Random().nextInt(10000));

        // 7. 若选择对公授信账期支付，执行 CAS 乐观预占授信额度
        if (PaymentMethod.CREDIT_ACCOUNT.getCode().equalsIgnoreCase(req.getPaymentMethod())) {
            boolean creditDeducted = financeFacade.preFreezeCredit(companyId, orderId, pricingSnapshot.getTotal());
            if (!creditDeducted) {
                throw new BizException(ResultCode.CREDIT_LIMIT_EXCEEDED, "企业可用授信额度不足，订单提交失败");
            }
        }

        // 8. 组装 OrderPO 并入库 (初始状态必须且只能为 pending_approval)
        OrderPO orderPO = OrderPO.builder()
                .id(orderId)
                .orderNo(orderNo)
                .companyId(companyId)
                .createdBy(userId)
                .status(OrderStatus.PENDING_APPROVAL.getCode())
                .approvalStatus(ApprovalStatus.PENDING.getCode())
                .paymentMethod(req.getPaymentMethod())
                .subtotal(pricingSnapshot.getSubtotal())
                .listSubtotal(pricingSnapshot.getListSubtotal())
                .promotionDiscount(pricingSnapshot.getPromotionDiscount())
                .shippingFee(pricingSnapshot.getShippingFee())
                .tax(pricingSnapshot.getTax())
                .total(pricingSnapshot.getTotal())
                .currency(pricingSnapshot.getCurrency())
                .shippingAddressSnapshot(addressSnapshot)
                .invoiceSnapshot(invoiceSnapshot)
                .note(req.getNote())
                .requestedDeliveryDate(req.getRequestedDeliveryDate())
                .submittedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();

        orderMapper.insert(orderPO);

        // 9. 组装 OrderLinePO 列表并入库
        Map<String, PricingSnapshotDTO.LinePricingSnapshot> snapshotMap = pricingSnapshot.getLineSnapshots().stream()
                .collect(Collectors.toMap(PricingSnapshotDTO.LinePricingSnapshot::getSkuId, s -> s, (k1, k2) -> k1));

        List<OrderLinePO> linePOs = new ArrayList<>();
        List<OrderLineDTO> lineDTOs = new ArrayList<>();

        for (CheckoutApplicationService.CheckoutItemHolder item : itemsToOrder) {
            SkuDTO sku = skuMap.get(item.getSkuId());
            PricingSnapshotDTO.LinePricingSnapshot lineSnap = snapshotMap.get(item.getSkuId());

            BigDecimal unitPrice = lineSnap != null ? lineSnap.getFinalUnitPrice() : sku.getPrice();
            BigDecimal listPrice = sku.getListPrice() != null ? sku.getListPrice() : sku.getPrice();
            BigDecimal discount = lineSnap != null && lineSnap.getPromotionDiscountPerUnit() != null
                    ? lineSnap.getPromotionDiscountPerUnit().multiply(BigDecimal.valueOf(item.getQuantity()))
                    : BigDecimal.ZERO;
            BigDecimal lineTotal = lineSnap != null ? lineSnap.getLineTotal() : unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            String lineId = "line_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

            OrderLinePO linePO = OrderLinePO.builder()
                    .id(lineId)
                    .orderId(orderId)
                    .productId(sku.getProductId())
                    .skuId(sku.getId())
                    .skuCode(sku.getCode())
                    .productTitle(sku.getName())
                    .skuName(sku.getName())
                    .attributes(sku.getAttributes())
                    .unit(sku.getUnit() != null ? sku.getUnit() : "件")
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .listUnitPrice(listPrice)
                    .promotionDiscount(discount)
                    .subtotal(lineTotal)
                    .pricingSnapshot(lineSnap)
                    .createdAt(Instant.now())
                    .build();

            orderLineMapper.insert(linePO);
            linePOs.add(linePO);

            lineDTOs.add(OrderLineDTO.builder()
                    .id(lineId)
                    .productId(sku.getProductId())
                    .skuId(sku.getId())
                    .skuCode(sku.getCode())
                    .productTitle(sku.getName())
                    .skuName(sku.getName())
                    .attributes(sku.getAttributes())
                    .unit(sku.getUnit() != null ? sku.getUnit() : "件")
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .listUnitPrice(listPrice)
                    .promotionDiscount(discount)
                    .subtotal(lineTotal)
                    .pricingSnapshot(lineSnap)
                    .build());
        }

        // 10. 本地消息表落库 (Outbox Pattern)
        try {
            Map<String, Object> eventPayload = Map.of(
                    "orderId", orderId,
                    "orderNo", orderNo,
                    "companyId", companyId,
                    "userId", userId,
                    "totalAmount", pricingSnapshot.getTotal(),
                    "currency", pricingSnapshot.getCurrency(),
                    "submittedAt", Instant.now().toString()
            );

            OutboxMessage outbox = OutboxMessage.builder()
                    .companyId(companyId)
                    .eventId(UUID.randomUUID().toString())
                    .eventType("OrderSubmittedEvent")
                    .payload(objectMapper.writeValueAsString(eventPayload))
                    .status("PENDING")
                    .retryCount(0)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            outboxMapper.insert(outbox);
        } catch (Exception e) {
            log.error("[Order] 本地消息表序列化异常: {}", e.getMessage(), e);
        }

        // 11. 清理已生成订单的采购车行项
        if (!cartItemIdsToDelete.isEmpty()) {
            cartItemMapper.deleteBatchIds(cartItemIdsToDelete);
        }

        // 12. 触发审批工作项创建 (两阶段审批状态机)
        ApprovalRequestDTO approval = approvalFacade.createApprovalRequest(orderId, orderNo, companyId, userId);

        log.info("[Order] 订单创建完成: orderId={}, orderNo={}, status={}",
                orderId, orderNo, orderPO.getStatus());

        return OrderDTO.builder()
                .id(orderId)
                .orderNo(orderNo)
                .companyId(companyId)
                .createdBy(userId)
                .status(orderPO.getStatus())
                .approvalStatus(orderPO.getApprovalStatus())
                .paymentMethod(orderPO.getPaymentMethod())
                .lines(lineDTOs)
                .totals(OrderTotalsDTO.builder()
                        .currency(orderPO.getCurrency())
                        .subtotal(orderPO.getSubtotal())
                        .listSubtotal(orderPO.getListSubtotal())
                        .promotionDiscount(orderPO.getPromotionDiscount())
                        .shippingFee(orderPO.getShippingFee())
                        .tax(orderPO.getTax())
                        .total(orderPO.getTotal())
                        .build())
                .shippingAddress(addressSnapshot)
                .invoice(invoiceSnapshot)
                .note(orderPO.getNote())
                .requestedDeliveryDate(orderPO.getRequestedDeliveryDate())
                .createdAt(orderPO.getCreatedAt())
                .updatedAt(orderPO.getUpdatedAt())
                .submittedAt(orderPO.getSubmittedAt())
                .approval(approval)
                .build();
    }
}
