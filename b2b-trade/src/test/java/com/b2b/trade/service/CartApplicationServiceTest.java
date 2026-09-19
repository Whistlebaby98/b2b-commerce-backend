package com.b2b.trade.service;

import com.b2b.catalog.api.CatalogFacade;
import com.b2b.catalog.api.dto.SkuDTO;
import com.b2b.common.tenant.TenantContext;
import com.b2b.common.tenant.TenantContextHolder;
import com.b2b.pricing.api.PricingFacade;
import com.b2b.trade.api.dto.AddToCartRequest;
import com.b2b.trade.api.dto.CartDTO;
import com.b2b.trade.infrastructure.persistence.entity.CartItemPO;
import com.b2b.trade.infrastructure.persistence.mapper.CartItemMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 采购车应用服务单元测试 (CartApplicationServiceTest)
 *
 * <p>验证多租户加购、同 SKU 增量合并、阶梯价联动重算与采购车汇总。</p>
 *
 * @author b2b-commerce-backend
 */
@ExtendWith(MockitoExtension.class)
class CartApplicationServiceTest {

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private CatalogFacade catalogFacade;

    @Mock
    private PricingFacade pricingFacade;

    @InjectMocks
    private CartApplicationService cartApplicationService;

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
    @DisplayName("验证获取采购车：查询行项并正确调用阶梯价重算与金额汇总")
    void testGetCart() {
        CartItemPO item1 = CartItemPO.builder()
                .id("cart-item-1")
                .companyId(companyId)
                .userId(userId)
                .skuId("sku-nsk-6205")
                .quantity(12)
                .selected(true)
                .createdAt(Instant.now())
                .build();

        when(cartItemMapper.selectList(any())).thenReturn(List.of(item1));

        SkuDTO sku = SkuDTO.builder()
                .id("sku-nsk-6205")
                .productId("prod-nsk-6205")
                .code("SKU-NSK-6205")
                .name("NSK 6205ZZ")
                .unit("套")
                .price(new BigDecimal("26.50"))
                .listPrice(new BigDecimal("32.00"))
                .build();

        when(catalogFacade.getSkusByIds(List.of("sku-nsk-6205"))).thenReturn(Map.of("sku-nsk-6205", sku));
        when(pricingFacade.getTierUnitPrice(eq(companyId), eq("sku-nsk-6205"), eq(12)))
                .thenReturn(new BigDecimal("24.00")); // 10+ 阶梯价

        CartDTO cart = cartApplicationService.getCart();

        assertThat(cart).isNotNull();
        assertThat(cart.getCompanyId()).isEqualTo(companyId);
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getUnitPrice()).isEqualByComparingTo("24.00");
        assertThat(cart.getTotals().getItemCount()).isEqualTo(12);
        assertThat(cart.getTotals().getSubtotal()).isEqualByComparingTo("288.00");
        assertThat(cart.getTotals().getListSubtotal()).isEqualByComparingTo("384.00");
        assertThat(cart.getTotals().getPromotionDiscount()).isEqualByComparingTo("96.00");
        assertThat(cart.getTotals().getTotal()).isEqualByComparingTo("288.00");
    }

    @Test
    @DisplayName("验证添加新商品至采购车：同 SKU 不存在时插入新行项")
    void testAddNewItemToCart() {
        AddToCartRequest req = AddToCartRequest.builder()
                .skuId("sku-sick-wl12g3")
                .quantity(5)
                .build();

        SkuDTO sku = SkuDTO.builder()
                .id("sku-sick-wl12g3")
                .productId("prod-sick-wl12g3")
                .code("SKU-SICK-WL12G3")
                .name("SICK WL12G-3")
                .unit("件")
                .price(new BigDecimal("156.00"))
                .build();

        when(catalogFacade.getSkuById("sku-sick-wl12g3")).thenReturn(sku);
        when(cartItemMapper.selectOne(any())).thenReturn(null); // 不存在已有行
        when(cartItemMapper.selectList(any())).thenReturn(List.of()); // getCart 结果

        cartApplicationService.addItem(req);

        // 验证执行了 insert
        verify(cartItemMapper).insert(any(CartItemPO.class));
    }

    @Test
    @DisplayName("验证添加已有商品至采购车：同 SKU 存在时数量增量合并")
    void testAddExistingItemToCart() {
        AddToCartRequest req = AddToCartRequest.builder()
                .skuId("sku-sick-wl12g3")
                .quantity(3)
                .build();

        SkuDTO sku = SkuDTO.builder()
                .id("sku-sick-wl12g3")
                .productId("prod-sick-wl12g3")
                .code("SKU-SICK-WL12G3")
                .name("SICK WL12G-3")
                .unit("件")
                .price(new BigDecimal("156.00"))
                .listPrice(new BigDecimal("179.00"))
                .build();

        CartItemPO existing = CartItemPO.builder()
                .id("cart-item-existing")
                .companyId(companyId)
                .userId(userId)
                .skuId("sku-sick-wl12g3")
                .quantity(5)
                .selected(false)
                .build();

        when(catalogFacade.getSkuById("sku-sick-wl12g3")).thenReturn(sku);
        when(cartItemMapper.selectOne(any())).thenReturn(existing);
        when(cartItemMapper.selectList(any())).thenReturn(List.of(existing));
        when(catalogFacade.getSkusByIds(any())).thenReturn(Map.of("sku-sick-wl12g3", sku));
        when(pricingFacade.getTierUnitPrice(any(), any(), any())).thenReturn(new BigDecimal("156.00"));

        cartApplicationService.addItem(req);

        // 数量应合并为 5 + 3 = 8，且重置为 selected = true
        assertThat(existing.getQuantity()).isEqualTo(8);
        assertThat(existing.getSelected()).isTrue();
        verify(cartItemMapper).updateById(existing);
    }
}
