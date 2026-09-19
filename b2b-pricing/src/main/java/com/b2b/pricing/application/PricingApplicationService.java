package com.b2b.pricing.application;

import com.b2b.pricing.api.dto.PriceTierDTO;
import com.b2b.pricing.api.dto.PricingRequestDTO;
import com.b2b.pricing.api.dto.PricingSnapshotDTO;
import com.b2b.pricing.domain.service.PricingPipeline;
import com.b2b.pricing.infrastructure.persistence.entity.PriceTierPO;
import com.b2b.pricing.infrastructure.persistence.mapper.PriceTierMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 计价与营销应用服务 (PricingApplicationService)
 *
 * <p>编排阶梯价格数据读取与纯内存算价流水线，提供高并发单品阶梯查询与批量订单算价。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PricingApplicationService {

    private final PriceTierMapper priceTierMapper;
    private final PricingPipeline pricingPipeline;

    /**
     * 查询指定 SKU 的全部阶梯价格区间
     *
     * @param skuId SKU 唯一标识，不可为空
     * @return 按起订量升序排列的阶梯价格列表
     */
    public List<PriceTierDTO> getPriceTiers(String skuId) {
        List<PriceTierPO> pos = priceTierMapper.selectList(
                new LambdaQueryWrapper<PriceTierPO>()
                        .eq(PriceTierPO::getSkuId, skuId)
                        .orderByAsc(PriceTierPO::getMinQuantity)
        );

        return pos.stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 批量查询多个 SKU 的阶梯价格映射
     *
     * @param skuIds SKU 标识集合
     * @return Key 为 skuId，Value 为阶梯价格列表的映射
     */
    public Map<String, List<PriceTierDTO>> getPriceTiersMap(List<String> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<PriceTierPO> pos = priceTierMapper.selectList(
                new LambdaQueryWrapper<PriceTierPO>()
                        .in(PriceTierPO::getSkuId, skuIds)
                        .orderByAsc(PriceTierPO::getMinQuantity)
        );

        return pos.stream()
                .collect(Collectors.groupingBy(
                        PriceTierPO::getSkuId,
                        Collectors.mapping(this::toDTO, Collectors.toList())
                ));
    }

    /**
     * 查询指定采购数量下的阶梯成交单价
     *
     * @param skuId     SKU 唯一标识
     * @param quantity  采购数量
     * @param basePrice 基准协议单价
     * @return 命中阶梯后的单价
     */
    public BigDecimal getTierUnitPrice(String skuId, Integer quantity, BigDecimal basePrice) {
        List<PriceTierDTO> tiers = getPriceTiers(skuId);
        return pricingPipeline.calculateTierUnitPrice(basePrice, tiers, quantity);
    }

    /**
     * 执行批量算价流水线
     *
     * @param request 算价请求（包含 SKU 列表与数量）
     * @return 可解释算价快照
     */
    public PricingSnapshotDTO calculatePricing(PricingRequestDTO request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return PricingSnapshotDTO.builder()
                    .subtotal(BigDecimal.ZERO)
                    .listSubtotal(BigDecimal.ZERO)
                    .promotionDiscount(BigDecimal.ZERO)
                    .shippingFee(BigDecimal.ZERO)
                    .tax(BigDecimal.ZERO)
                    .total(BigDecimal.ZERO)
                    .currency("CNY")
                    .lineSnapshots(Collections.emptyList())
                    .build();
        }

        List<String> skuIds = request.getItems().stream()
                .map(PricingRequestDTO.PricingItem::getSkuId)
                .toList();

        // 1. 一次性批量查出涉及的所有阶梯
        List<PriceTierPO> pos = priceTierMapper.selectList(
                new LambdaQueryWrapper<PriceTierPO>()
                        .in(PriceTierPO::getSkuId, skuIds)
                        .orderByAsc(PriceTierPO::getMinQuantity)
        );
        Map<String, List<PriceTierDTO>> tierMap = pos.stream()
                .collect(Collectors.groupingBy(
                        PriceTierPO::getSkuId,
                        Collectors.mapping(this::toDTO, Collectors.toList())
                ));

        // 2. 装配流水线上下文
        List<PricingPipeline.PricingItemContext> contexts = new ArrayList<>();
        for (PricingRequestDTO.PricingItem item : request.getItems()) {
            List<PriceTierDTO> tiers = tierMap.getOrDefault(item.getSkuId(), Collections.emptyList());
            // 若未单独提供 basePrice，以阶梯中第一档单价作为基准协议价
            BigDecimal basePrice = !tiers.isEmpty() ? tiers.get(0).getUnitPrice() : BigDecimal.ZERO;

            contexts.add(PricingPipeline.PricingItemContext.builder()
                    .skuId(item.getSkuId())
                    .quantity(item.getQuantity())
                    .basePrice(basePrice)
                    .listPrice(basePrice)
                    .tiers(tiers)
                    .build());
        }

        // 3. 执行纯内存无副作用算价流水线
        return pricingPipeline.buildSnapshot(contexts);
    }

    private PriceTierDTO toDTO(PriceTierPO po) {
        return PriceTierDTO.builder()
                .minQuantity(po.getMinQuantity())
                .unitPrice(po.getUnitPrice())
                .label(po.getLabel())
                .build();
    }
}
