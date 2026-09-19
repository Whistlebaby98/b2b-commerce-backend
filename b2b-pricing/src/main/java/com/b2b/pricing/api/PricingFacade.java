package com.b2b.pricing.api;

import com.b2b.common.exception.BizException;
import com.b2b.pricing.api.dto.PricingRequestDTO;
import com.b2b.pricing.api.dto.PricingSnapshotDTO;

import java.math.BigDecimal;

/**
 * 计价与营销域对外门面接口 (PricingFacade)
 *
 * <p>遵循 ADR 0006 规范，为交易域提供无 I/O 副作用的批量算价服务与单品阶梯单价查询，
 * 严禁外部模块自行拼接价格与活动计算公式。</p>
 *
 * @author b2b-commerce-backend
 */
public interface PricingFacade {

    /**
     * 执行批量算价流水线，生成订单级不可变算价快照
     *
     * @param request 算价请求参数（包含企业上下文、SKU 集合与购买数量）
     * @return 包含最终总额、阶梯命中轨迹与优惠分摊的完整算价快照
     * @throws BizException 当 SKU 不存在、采购数量不合法或企业上下文缺失时抛出
     */
    PricingSnapshotDTO calculatePricing(PricingRequestDTO request) throws BizException;

    /**
     * 查询指定客户企业下某个 SKU 在特定采购数量下的阶梯成交单价
     *
     * @param companyId 客户企业 ID，不可为空
     * @param skuId     SKU 唯一标识，不可为空
     * @param quantity  采购数量，必须为大于 0 的正整数
     * @return 命中阶梯后的单件成交单价
     * @throws BizException SKU 不存在时抛出
     */
    BigDecimal getTierUnitPrice(String companyId, String skuId, Integer quantity) throws BizException;

    /**
     * 获取指定 SKU 的全部阶梯价格区间
     *
     * @param skuId SKU 唯一标识，不可为空
     * @return 阶梯价格列表 (按起订量升序排列)
     */
    java.util.List<com.b2b.pricing.api.dto.PriceTierDTO> getPriceTiers(String skuId);

    /**
     * 批量获取多个 SKU 的阶梯价格映射
     *
     * @param skuIds SKU 标识集合
     * @return Key 为 skuId，Value 为阶梯价格列表的映射
     */
    java.util.Map<String, java.util.List<com.b2b.pricing.api.dto.PriceTierDTO>> getPriceTiersMap(java.util.List<String> skuIds);
}
