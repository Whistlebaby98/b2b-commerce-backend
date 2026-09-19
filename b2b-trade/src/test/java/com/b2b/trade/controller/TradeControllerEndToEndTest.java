package com.b2b.trade.controller;

import com.b2b.common.api.PageResult;
import com.b2b.common.exception.GlobalExceptionHandler;
import com.b2b.trade.api.dto.AddToCartRequest;
import com.b2b.trade.api.dto.CartDTO;
import com.b2b.trade.api.dto.CartItemDTO;
import com.b2b.trade.api.dto.CartTotalsDTO;
import com.b2b.trade.api.dto.CheckoutPreviewDTO;
import com.b2b.trade.api.dto.CreateOrderRequest;
import com.b2b.trade.api.dto.OrderDTO;
import com.b2b.trade.api.dto.OrderTotalsDTO;
import com.b2b.trade.domain.enums.OrderStatus;
import com.b2b.trade.service.CartApplicationService;
import com.b2b.trade.service.CheckoutApplicationService;
import com.b2b.trade.service.OrderApplicationService;
import com.b2b.trade.service.OrderQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 交易端到端控制器集成测试 (TradeControllerEndToEndTest)
 *
 * <p>遵循 ADR 0009 规范，通过 MockMvc 验证完整采购流程：
 * 选品加购 -> 结算试算 (预领 Token) -> 防重提交订单 -> 订单查询。</p>
 *
 * @author b2b-commerce-backend
 */
@ExtendWith(MockitoExtension.class)
class TradeControllerEndToEndTest {

    private MockMvc mockMvc;

    @Mock
    private CartApplicationService cartApplicationService;

    @Mock
    private CheckoutApplicationService checkoutApplicationService;

    @Mock
    private OrderApplicationService orderApplicationService;

    @Mock
    private OrderQueryService orderQueryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        CartController cartController = new CartController(cartApplicationService);
        CheckoutController checkoutController = new CheckoutController(checkoutApplicationService);
        OrderController orderController = new OrderController(orderApplicationService, orderQueryService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(cartController, checkoutController, orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("端到端链路测试：加购 -> 结算试算 -> 防重下单 -> 订单检索")
    void testEndToEndTradeFlow() throws Exception {
        // -------------------------------------------------------------
        // 1. 获取购物车
        // -------------------------------------------------------------
        CartDTO mockCart = CartDTO.builder()
                .id("cart_1")
                .companyId("company-lantu")
                .currency("CNY")
                .items(List.of(CartItemDTO.builder()
                        .id("item-1")
                        .skuId("sku-nsk-6205")
                        .skuCode("SKU-NSK-6205")
                        .productTitle("NSK 6205ZZ")
                        .quantity(12)
                        .unitPrice(new BigDecimal("24.00"))
                        .selected(true)
                        .build()))
                .totals(CartTotalsDTO.builder()
                        .itemCount(12)
                        .subtotal(new BigDecimal("288.00"))
                        .total(new BigDecimal("288.00"))
                        .build())
                .build();

        when(cartApplicationService.getCart()).thenReturn(mockCart);

        mockMvc.perform(get("/api/v1/cart")
                        .header("X-Organization-Id", "company-lantu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items[0].skuCode").value("SKU-NSK-6205"))
                .andExpect(jsonPath("$.data.totals.total").value(288.00));

        // -------------------------------------------------------------
        // 2. 添加商品至购物车
        // -------------------------------------------------------------
        AddToCartRequest addReq = AddToCartRequest.builder()
                .skuId("sku-sick-wl12g3")
                .quantity(2)
                .build();

        when(cartApplicationService.addItem(any())).thenReturn(mockCart);

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-Organization-Id", "company-lantu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // -------------------------------------------------------------
        // 3. 结算试算与防重 Token 预领
        // -------------------------------------------------------------
        CheckoutPreviewDTO previewDTO = CheckoutPreviewDTO.builder()
                .checkoutToken("mock_checkout_token_abc")
                .tokenExpiresInSeconds(1800L)
                .totals(CartTotalsDTO.builder()
                        .itemCount(14)
                        .subtotal(new BigDecimal("600.00"))
                        .total(new BigDecimal("600.00"))
                        .currency("CNY")
                        .build())
                .build();

        when(checkoutApplicationService.previewCheckout(any())).thenReturn(previewDTO);

        mockMvc.perform(post("/api/v1/checkout/preview")
                        .header("X-Organization-Id", "company-lantu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.checkoutToken").value("mock_checkout_token_abc"))
                .andExpect(jsonPath("$.data.totals.total").value(600.00));

        // -------------------------------------------------------------
        // 4. 提交生成采购订单 (初始状态必须为 pending_approval)
        // -------------------------------------------------------------
        CreateOrderRequest createOrderReq = CreateOrderRequest.builder()
                .checkoutToken("mock_checkout_token_abc")
                .addressId("addr-shenzhen-factory")
                .paymentMethod("credit_account")
                .build();

        OrderDTO mockOrder = OrderDTO.builder()
                .id("ord_test_1001")
                .orderNo("PO202609190001")
                .companyId("company-lantu")
                .status(OrderStatus.PENDING_APPROVAL.getCode())
                .totals(OrderTotalsDTO.builder()
                        .total(new BigDecimal("600.00"))
                        .currency("CNY")
                        .build())
                .build();

        when(orderApplicationService.createOrder(any())).thenReturn(mockOrder);

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-Organization-Id", "company-lantu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrderReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").value("PO202609190001"))
                .andExpect(jsonPath("$.data.status").value("pending_approval"))
                .andExpect(jsonPath("$.data.totals.total").value(600.00));

        // -------------------------------------------------------------
        // 5. 分页查询订单列表与详情
        // -------------------------------------------------------------
        PageResult<OrderDTO> pageResult = PageResult.<OrderDTO>builder()
                .items(List.of(mockOrder))
                .total(1L)
                .page(1)
                .pageSize(10)
                .totalPages(1)
                .build();

        when(orderQueryService.listOrders(any())).thenReturn(pageResult);
        when(orderQueryService.getOrderDetail("ord_test_1001")).thenReturn(mockOrder);

        mockMvc.perform(get("/api/v1/orders")
                        .header("X-Organization-Id", "company-lantu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items[0].orderNo").value("PO202609190001"));

        mockMvc.perform(get("/api/v1/orders/ord_test_1001")
                        .header("X-Organization-Id", "company-lantu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("ord_test_1001"));
    }

    @Test
    @DisplayName("验证参数校验拦截：加购数量为 0 时返回 400 业务错误")
    void testValidationFailure() throws Exception {
        AddToCartRequest invalidReq = AddToCartRequest.builder()
                .skuId("sku-1")
                .quantity(0) // 违反 @Min(1)
                .build();

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-Organization-Id", "company-lantu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
