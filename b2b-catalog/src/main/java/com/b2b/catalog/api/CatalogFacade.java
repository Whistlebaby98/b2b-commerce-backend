package com.b2b.catalog.api;

import com.b2b.catalog.api.dto.ProductDTO;
import com.b2b.catalog.api.dto.SkuDTO;
import com.b2b.common.exception.BizException;

import java.util.List;
import java.util.Map;

/**
 * 商品与目录域对外门面接口 (CatalogFacade)
 *
 * <p>供交易域、计价域跨模块读取商品规格、校验库存与批量解析 SKU 编码，
 * 严禁外部模块直接依赖或查询商品库表。</p>
 *
 * @author b2b-commerce-backend
 */
public interface CatalogFacade {

    /**
     * 根据 SKU 唯一标识获取 SKU 详情
     *
     * @param skuId SKU 唯一标识，不可为空
     * @return SKU 详情 DTO
     * @throws BizException SKU 不存在或已下架时抛出
     */
    SkuDTO getSkuById(String skuId) throws BizException;

    /**
     * 批量根据 SKU ID 列表获取 SKU 映射
     *
     * @param skuIds SKU 标识集合
     * @return Key 为 skuId，Value 为 SkuDTO 的映射表
     */
    Map<String, SkuDTO> getSkusByIds(List<String> skuIds);

    /**
     * 批量根据业务编码（如 OEM 码或型号）检索 SKU
     *
     * @param codes SKU 业务编码集合（用于批量采购解析）
     * @return 匹配到的 SKU 列表
     */
    List<SkuDTO> getSkusByCodes(List<String> codes);

    /**
     * 获取指定商品 (SPU) 及其包含的所有 SKU 列表
     *
     * @param productId 商品唯一标识，不可为空
     * @return 商品详情 DTO
     * @throws BizException 商品不存在时抛出
     */
    ProductDTO getProductById(String productId) throws BizException;
}
