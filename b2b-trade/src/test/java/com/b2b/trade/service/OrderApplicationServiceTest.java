package com.b2b.trade.service;

import com.b2b.approval.api.ApprovalFacade;
import com.b2b.approval.api.dto.ApprovalRequestDTO;
import com.b2b.catalog.api.CatalogFacade;
import com.b2b.catalog.api.dto.SkuDTO;
import com.b2b.common.api.ResultCode;
import com.b2b.common.exception.BizException;
import com.b2b.common.outbox.OutboxMapper;
import com.b2b.common.outbox.OutboxMessage;
import com.b2b.common.tenant.TenantContext;
import com.b2b.common.tenant.TenantContextHolder;
import com.b2b.finance.api.FinanceFacade;
import com.b2b.finance.api.dto.AddressSnapshotDTO;
import com.b2b.finance.api.dto.InvoiceSnapshotDTO;
import com.b2b.pricing.api.PricingFacade;
import com.b2b.pricing.api.dto.PricingSnapshotDTO;
import com.b2b.trade.api.dto.CreateOrderRequest;
import com.b2b.trade.api.dto.OrderDTO;
import com.b2b.trade.domain.enums.ApprovalStatus;
import com.b2b.trade.domain.enums.OrderStatus;
import com.b2b.trade.infrastructure.persistence.entity.CartItemPO;
import com.b2b.trade.infrastructure.persistence.entity.OrderLinePO;
import com.b2b.trade.infrastructure.persistence.entity.OrderPO;
import com.b2b.trade.infrastructure.persistence.mapper.CartItemMapper;
import com.b2b.trade.infrastructure.persistence.mapper.OrderLineMapper;
import com.b2b.trade.infrastructure.persistence.mapper.OrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订单提交应用服务核心业务测试 (OrderApplicationServiceTest)
 *
 * <p>验证防重 Token 核销、CAS 授信预占、不可变快照固化、Outbox 消息持久化及待审批流转。</p>
 *
 * @author b2b-commerce-backend
 */
@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderLineMapper orderLineMapper;

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private OutboxMapper outboxMapper;

    @Mock
    private CatalogFacade catalogFacade;

    @Mock
    private PricingFacade pricingFacade;

    @Mock
    private FinanceFacade financeFacade;

    @Mock
    private ApprovalFacade approvalFacade;

    @Mock
    private IdempotencyTokenService idempotencyTokenService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OrderApplicationService orderApplicationService;

    private final String companyId = "company-lantu";
    private final String userId = "user-lin-yue";

    @BeforeEach
    void setUp() {
        TenantContextHolder.setContext(TenantContext.builder()
                .companyId(companyId)
                .userId(userId)
                .build());
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    @DisplayName("验证订单提交成功全链路：Token 核销、CAS 预占、快照持久化、初始待审批")
    void testCreateOrderSuccess() {
        CreateOrderRequest req = CreateOrderRequest.builder()
                .checkoutToken("valid_token_123")
                .addressId("addr-shenzhen-factory")
                .invoiceId("invoice-special-default")
                .paymentMethod("credit_account")
                .note("产线紧急替换")
                .requestedDeliveryDate(LocalDate.of(2026, 9, 25))
                .build();

        // 1. Token 校验成功
        when(idempotencyTokenService.consumeCheckoutToken("valid_token_123")).thenReturn(true);

        // 2. 购物车行项
        CartItemPO cartItem = CartItemPO.builder()
                .id("cart-item-1")
                .companyId(companyId)
                .userId(userId)
                .skuId("sku-sick-wl12g3")
                .quantity(18)
                .selected(true)
                .build();
        when(cartItemMapper.selectList(any())).thenReturn(List.of(cartItem));

        // 3. SKU 与计价快照
        SkuDTO sku = SkuDTO.builder()
                .id("sku-sick-wl12g3")
                .productId("prod-sick-wl12g3")
                .code("SKU-SICK-WL12G3")
                .name("SICK WL12G-3")
                .unit("件")
                .price(new BigDecimal("156.00"))
                .listPrice(new BigDecimal("179.00"))
                .build();
        when(catalogFacade.getSkusByIds(List.of("sku-sick-wl12g3"))).thenReturn(Map.of("sku-sick-wl12g3", sku));

        PricingSnapshotDTO.LinePricingSnapshot lineSnap = PricingSnapshotDTO.LinePricingSnapshot.builder()
                .skuId("sku-sick-wl12g3")
                .quantity(18)
                .listUnitPrice(new BigDecimal("179.00"))
                .finalUnitPrice(new BigDecimal("156.00"))
                .lineTotal(new BigDecimal("2808.00"))
                .promotionDiscountPerUnit(BigDecimal.ZERO)
                .build();

        PricingSnapshotDTO pricingSnapshot = PricingSnapshotDTO.builder()
                .subtotal(new BigDecimal("2808.00"))
                .listSubtotal(new BigDecimal("3222.00"))
                .promotionDiscount(new BigDecimal("414.00"))
                .shippingFee(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .total(new BigDecimal("2808.00"))
                .currency("CNY")
                .lineSnapshots(List.of(lineSnap))
                .build();
        when(pricingFacade.calculatePricing(any())).thenReturn(pricingSnapshot);

        // 4. 地址与发票快照
        AddressSnapshotDTO addressSnapshot = AddressSnapshotDTO.builder()
                .id("addr-shenzhen-factory")
                .recipient("陈志远")
                .build();
        InvoiceSnapshotDTO invoiceSnapshot = InvoiceSnapshotDTO.builder()
                .id("invoice-special-default")
                .title("深圳市蓝图精密制造有限公司")
                .build();
        when(financeFacade.getAddressSnapshot(companyId, "addr-shenzhen-factory")).thenReturn(addressSnapshot);
        when(financeFacade.getInvoiceSnapshot(companyId, "invoice-special-default")).thenReturn(invoiceSnapshot);

        // 5. CAS 预占授信成功
        when(financeFacade.preFreezeCredit(eq(companyId), anyString(), eq(new BigDecimal("2808.00"))))
                .thenReturn(true);

        // 6. 审批工作项生成
        ApprovalRequestDTO approvalDTO = ApprovalRequestDTO.builder()
                .id("appr_001")
                .status("pending")
                .currentNode("待审批")
                .build();
        when(approvalFacade.createApprovalRequest(anyString(), anyString(), eq(companyId), eq(userId)))
                .thenReturn(approvalDTO);

        // 执行创建
        OrderDTO order = orderApplicationService.createOrder(req);

        // 断言验证
        assertThat(order).isNotNull();
        assertThat(order.getOrderNo()).startsWith("PO");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_APPROVAL.getCode());
        assertThat(order.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING.getCode());
        assertThat(order.getTotals().getTotal()).isEqualByComparingTo("2808.00");
        assertThat(order.getShippingAddress().getRecipient()).isEqualTo("陈志远");
        assertThat(order.getInvoice().getTitle()).isEqualTo("深圳市蓝图精密制造有限公司");

        // 验证持久化调用
        verify(orderMapper).insert(any(OrderPO.class));
        verify(orderLineMapper).insert(any(OrderLinePO.class));
        verify(outboxMapper).insert(any(OutboxMessage.class));
        verify(cartItemMapper).deleteBatchIds(List.of("cart-item-1"));
        verify(approvalFacade).createApprovalRequest(anyString(), anyString(), eq(companyId), eq(userId));
    }

    @Test
    @DisplayName("验证防重提交：重复或无效 Token 时直接阻断并抛出异常")
    void testCreateOrderInvalidToken() {
        CreateOrderRequest req = CreateOrderRequest.builder()
                .checkoutToken("already_used_token")
                .addressId("addr-1")
                .paymentMethod("credit_account")
                .build();

        // 模拟 Token 核销失败 (已核销或伪造)
        when(idempotencyTokenService.consumeCheckoutToken("already_used_token")).thenReturn(false);

        assertThatThrownBy(() -> orderApplicationService.createOrder(req))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", ResultCode.CHECKOUT_TOKEN_INVALID.getCode());
    }

    @Test
    @DisplayName("验证企业授信不足时：CAS 预占失败即时回滚并抛出异常")
    void testCreateOrderInsufficientCredit() {
        CreateOrderRequest req = CreateOrderRequest.builder()
                .checkoutToken("valid_token")
                .addressId("addr-1")
                .paymentMethod("credit_account")
                .build();

        when(idempotencyTokenService.consumeCheckoutToken("valid_token")).thenReturn(true);

        CartItemPO cartItem = CartItemPO.builder()
                .id("cart-1")
                .companyId(companyId)
                .userId(userId)
                .skuId("sku-expensive")
                .quantity(100)
                .selected(true)
                .build();
        when(cartItemMapper.selectList(any())).thenReturn(List.of(cartItem));

        SkuDTO sku = SkuDTO.builder().id("sku-expensive").productId("p-1").code("SKU-EXP").name("Expensive").price(new BigDecimal("10000.00")).build();
        when(catalogFacade.getSkusByIds(any())).thenReturn(Map.of("sku-expensive", sku));

        PricingSnapshotDTO pricingSnapshot = PricingSnapshotDTO.builder()
                .subtotal(new BigDecimal("1000000.00"))
                .listSubtotal(new BigDecimal("1000000.00"))
                .promotionDiscount(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .total(new BigDecimal("1000000.00"))
                .currency("CNY")
                .lineSnapshots(List.of())
                .build();
        when(pricingFacade.calculatePricing(any())).thenReturn(pricingSnapshot);

        when(financeFacade.getAddressSnapshot(any(), any())).thenReturn(AddressSnapshotDTO.builder().build());

        // 模拟 CAS 授信不足，预占返回 false
        when(financeFacade.preFreezeCredit(eq(companyId), anyString(), eq(new BigDecimal("1000000.00"))))
                .thenReturn(false);

        assertThatThrownBy(() -> orderApplicationService.createOrder(req))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", ResultCode.CREDIT_LIMIT_EXCEEDED.getCode());
    }
}
