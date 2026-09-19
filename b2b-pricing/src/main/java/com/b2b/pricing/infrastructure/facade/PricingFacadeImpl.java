package com.b2b.pricing.infrastructure.facade;

import com.b2b.common.exception.BizException;
import com.b2b.pricing.api.PricingFacade;
import com.b2b.pricing.api.dto.PriceTierDTO;
import com.b2b.pricing.api.dto.PricingRequestDTO;
import com.b2b.pricing.api.dto.PricingSnapshotDTO;
import com.b2b.pricing.application.PricingApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 计价与营销域对外门面实现类 (PricingFacadeImpl)
 *
 * <p>为目录域与交易域提供纯领域阶梯算价与价格区间查询服务，
 * 封装内部实体与计算规则，防止跨模块业务耦合。</p>
 *
 * @author b2b-commerce-backend
 */
@Service
@RequiredArgsConstructor
public class PricingFacadeImpl implements PricingFacade {

    private final PricingApplicationService pricingApplicationService;

    @Override
    public PricingSnapshotDTO calculatePricing(PricingRequestDTO request) throws BizException {
        return pricingApplicationService.calculatePricing(request);
    }

    @Override
    public BigDecimal getTierUnitPrice(String companyId, String skuId, Integer quantity) throws BizException {
        // 默认协议基准价在无前置输入时使用 0，实际由阶梯规则首档或目录基价决定
        return pricingApplicationService.getTierUnitPrice(skuId, quantity, BigDecimal.ZERO);
    }

    @Override
    public List<PriceTierDTO> getPriceTiers(String skuId) {
        return pricingApplicationService.getPriceTiers(skuId);
    }

    @Override
    public Map<String, List<PriceTierDTO>> getPriceTiersMap(List<String> skuIds) {
        return pricingApplicationService.getPriceTiersMap(skuIds);
    }
}
