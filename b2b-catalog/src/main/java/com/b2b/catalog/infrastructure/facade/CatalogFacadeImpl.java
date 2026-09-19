package com.b2b.catalog.infrastructure.facade;

import com.b2b.catalog.api.CatalogFacade;
import com.b2b.catalog.api.dto.ProductDTO;
import com.b2b.catalog.api.dto.SkuDTO;
import com.b2b.catalog.application.CatalogApplicationService;
import com.b2b.catalog.infrastructure.persistence.entity.SkuPO;
import com.b2b.catalog.infrastructure.persistence.mapper.SkuMapper;
import com.b2b.common.api.ResultCode;
import com.b2b.common.exception.BizException;
import com.b2b.pricing.api.PricingFacade;
import com.b2b.pricing.api.dto.PriceTierDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品与目录域对外门面实现类 (CatalogFacadeImpl)
 *
 * <p>为交易域与计价域提供跨模块商品规格、库存状态与批量 SKU 查询服务，
 * 封装内部实体与 Mapper，防止外部模块直接访问商品数据库表。</p>
 *
 * @author b2b-commerce-backend
 */
@Service
@RequiredArgsConstructor
public class CatalogFacadeImpl implements CatalogFacade {

    private final SkuMapper skuMapper;
    private final CatalogApplicationService catalogApplicationService;
    private final PricingFacade pricingFacade;

    @Override
    public SkuDTO getSkuById(String skuId) throws BizException {
        SkuPO skuPO = skuMapper.selectById(skuId);
        if (skuPO == null || !"available".equalsIgnoreCase(skuPO.getStatus())) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "SKU 不存在或已停售");
        }
        List<PriceTierDTO> tiers = pricingFacade.getPriceTiers(skuId);
        return toSkuDTO(skuPO, tiers);
    }

    @Override
    public Map<String, SkuDTO> getSkusByIds(List<String> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SkuPO> skuPOs = skuMapper.selectList(
                new LambdaQueryWrapper<SkuPO>()
                        .in(SkuPO::getId, skuIds)
                        .eq(SkuPO::getStatus, "available")
        );
        Map<String, List<PriceTierDTO>> tierMap = pricingFacade.getPriceTiersMap(skuIds);

        return skuPOs.stream()
                .map(sku -> toSkuDTO(sku, tierMap.getOrDefault(sku.getId(), Collections.emptyList())))
                .collect(Collectors.toMap(SkuDTO::getId, s -> s));
    }

    @Override
    public List<SkuDTO> getSkusByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }
        List<SkuPO> skuPOs = skuMapper.selectList(
                new LambdaQueryWrapper<SkuPO>()
                        .in(SkuPO::getCode, codes)
                        .eq(SkuPO::getStatus, "available")
        );
        List<String> skuIds = skuPOs.stream().map(SkuPO::getId).toList();
        Map<String, List<PriceTierDTO>> tierMap = pricingFacade.getPriceTiersMap(skuIds);

        return skuPOs.stream()
                .map(sku -> toSkuDTO(sku, tierMap.getOrDefault(sku.getId(), Collections.emptyList())))
                .toList();
    }

    @Override
    public ProductDTO getProductById(String productId) throws BizException {
        return catalogApplicationService.getProductDetail(productId);
    }

    private SkuDTO toSkuDTO(SkuPO po, List<PriceTierDTO> tiers) {
        return SkuDTO.builder()
                .id(po.getId())
                .productId(po.getProductId())
                .code(po.getCode())
                .name(po.getName())
                .attributes(po.getAttributes())
                .unit(po.getUnit())
                .price(po.getPrice())
                .listPrice(po.getListPrice())
                .currency(po.getCurrency())
                .stock(po.getStock())
                .availability(po.getAvailability())
                .leadTimeLabel(po.getLeadTimeLabel())
                .status(po.getStatus())
                .weightKg(po.getWeightKg())
                .priceTiers(tiers)
                .build();
    }
}
