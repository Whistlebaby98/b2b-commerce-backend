package com.b2b.catalog.application;

import com.b2b.catalog.api.dto.BulkResolveRequest;
import com.b2b.catalog.api.dto.BulkResolveResponse;
import com.b2b.catalog.api.dto.CatalogQueryRequest;
import com.b2b.catalog.api.dto.CategoryDTO;
import com.b2b.catalog.api.dto.PageResult;
import com.b2b.catalog.api.dto.ProductDTO;
import com.b2b.catalog.api.dto.SkuDTO;
import com.b2b.catalog.infrastructure.persistence.entity.CategoryPO;
import com.b2b.catalog.infrastructure.persistence.entity.ProductPO;
import com.b2b.catalog.infrastructure.persistence.entity.SkuPO;
import com.b2b.catalog.infrastructure.persistence.mapper.CategoryMapper;
import com.b2b.catalog.infrastructure.persistence.mapper.ProductMapper;
import com.b2b.catalog.infrastructure.persistence.mapper.SkuMapper;
import com.b2b.common.api.ResultCode;
import com.b2b.common.exception.BizException;
import com.b2b.common.tenant.TenantContextHolder;
import com.b2b.pricing.api.PricingFacade;
import com.b2b.pricing.api.dto.PriceTierDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品与目录应用服务 (CatalogApplicationService)
 *
 * <p>编排品类树查询、商品多维筛选、SKU 阶梯价装配与批量采购清单逐行解析。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogApplicationService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final PricingFacade pricingFacade;

    /**
     * 查询所有商品品类列表及各品类下有效商品数
     *
     * @return 品类列表 (按 sort_order 升序)
     */
    public List<CategoryDTO> listCategories() {
        List<CategoryPO> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<CategoryPO>()
                        .orderByAsc(CategoryPO::getSortOrder)
        );

        return categories.stream().map(cat -> {
            Long count = productMapper.selectCount(
                    new LambdaQueryWrapper<ProductPO>()
                            .eq(ProductPO::getCategoryId, cat.getId())
                            .eq(ProductPO::getStatus, "active")
            );
            return CategoryDTO.builder()
                    .id(cat.getId())
                    .name(cat.getName())
                    .description(cat.getDescription())
                    .productCount(count != null ? count.intValue() : 0)
                    .build();
        }).toList();
    }

    /**
     * 多条件分页检索商品目录
     *
     * @param request 筛选入参 (关键词、品类、品牌、现货状态、排序、分页)
     * @return 分页商品列表 (包含下属完整 SKU 与阶梯价)
     */
    public PageResult<ProductDTO> searchProducts(CatalogQueryRequest request) {
        Page<ProductPO> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<ProductPO> wrapper = new LambdaQueryWrapper<>();

        // 1. 状态过滤
        wrapper.eq(ProductPO::getStatus, "active");

        // 2. 类目筛选
        if (StringUtils.hasText(request.getCategoryId())) {
            wrapper.eq(ProductPO::getCategoryId, request.getCategoryId());
        }

        // 3. 品牌筛选
        if (StringUtils.hasText(request.getBrand())) {
            wrapper.eq(ProductPO::getBrand, request.getBrand());
        }

        // 4. 综合关键词模糊搜索
        if (StringUtils.hasText(request.getQuery())) {
            String q = request.getQuery().trim();
            wrapper.and(w -> w.like(ProductPO::getTitle, q)
                    .or().like(ProductPO::getBrand, q)
                    .or().like(ProductPO::getSubtitle, q));
        }

        // 5. 排序规则
        if ("newest".equalsIgnoreCase(request.getSortBy())) {
            wrapper.orderByDesc(ProductPO::getCreatedAt);
        } else {
            wrapper.orderByDesc(ProductPO::getIsFeatured)
                    .orderByDesc(ProductPO::getCreatedAt);
        }

        Page<ProductPO> productPage = productMapper.selectPage(page, wrapper);
        List<ProductPO> records = productPage.getRecords();

        if (records.isEmpty()) {
            return PageResult.<ProductDTO>builder()
                    .items(Collections.emptyList())
                    .total(productPage.getTotal())
                    .page((int) productPage.getCurrent())
                    .pageSize((int) productPage.getSize())
                    .totalPages((int) productPage.getPages())
                    .build();
        }

        // 6. 批量加载下属 SKU 与阶梯价
        List<String> productIds = records.stream().map(ProductPO::getId).toList();
        List<SkuPO> skuPOs = skuMapper.selectList(
                new LambdaQueryWrapper<SkuPO>()
                        .in(SkuPO::getProductId, productIds)
                        .eq(SkuPO::getStatus, "available")
        );

        List<String> skuIds = skuPOs.stream().map(SkuPO::getId).toList();
        Map<String, List<PriceTierDTO>> tierMap = pricingFacade.getPriceTiersMap(skuIds);

        Map<String, List<SkuDTO>> productSkuMap = skuPOs.stream()
                .map(sku -> toSkuDTO(sku, tierMap.getOrDefault(sku.getId(), Collections.emptyList())))
                .collect(Collectors.groupingBy(SkuDTO::getProductId));

        List<ProductDTO> items = records.stream().map(p -> {
            List<SkuDTO> skus = productSkuMap.getOrDefault(p.getId(), Collections.emptyList());
            return toProductDTO(p, skus);
        }).toList();

        return PageResult.<ProductDTO>builder()
                .items(items)
                .total(productPage.getTotal())
                .page((int) productPage.getCurrent())
                .pageSize((int) productPage.getSize())
                .totalPages((int) productPage.getPages())
                .build();
    }

    /**
     * 查询指定商品 (SPU) 详情及关联的完整 SKU 规格与阶梯价
     *
     * @param productId 商品 ID，不可为空
     * @return 完整商品详情 DTO
     * @throws BizException 当商品不存在时抛出
     */
    public ProductDTO getProductDetail(String productId) {
        ProductPO product = productMapper.selectById(productId);
        if (product == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "商品档案不存在");
        }

        List<SkuPO> skuPOs = skuMapper.selectList(
                new LambdaQueryWrapper<SkuPO>()
                        .eq(SkuPO::getProductId, productId)
                        .eq(SkuPO::getStatus, "available")
        );

        List<String> skuIds = skuPOs.stream().map(SkuPO::getId).toList();
        Map<String, List<PriceTierDTO>> tierMap = pricingFacade.getPriceTiersMap(skuIds);

        List<SkuDTO> skus = skuPOs.stream()
                .map(sku -> toSkuDTO(sku, tierMap.getOrDefault(sku.getId(), Collections.emptyList())))
                .toList();

        return toProductDTO(product, skus);
    }

    /**
     * 批量采购清单解析 (Bulk Resolve)
     *
     * <p>对齐前端 /bulk-buying 契约，支持逐行匹配 SKU 编码、校验数量并返回阶梯价格与精准行级报错。</p>
     *
     * @param request 批量文本清单入参
     * @return 逐行解析结果载荷
     */
    public BulkResolveResponse resolveBulkSkus(BulkResolveRequest request) {
        String rawText = request.getRawText();
        String[] lines = rawText.split("\\r?\\n");

        List<BulkResolveResponse.BulkRowItem> rowItems = new ArrayList<>();
        List<String> validCodes = new ArrayList<>();

        // 1. 语法逐行解析提取编码与数量
        for (int i = 0; i < lines.length; i++) {
            int lineNumber = i + 1;
            String rawLine = lines[i].trim();
            if (rawLine.isEmpty()) {
                continue;
            }

            String[] columns = rawLine.split("[\\t,; ]+");
            String skuCode = columns.length > 0 ? columns[0].trim() : "";
            int quantity = 1;
            String error = null;

            if (!StringUtils.hasText(skuCode)) {
                error = "缺少 SKU 编码";
            } else if (columns.length > 1) {
                try {
                    quantity = Integer.parseInt(columns[1].trim());
                    if (quantity <= 0) {
                        error = "数量需为大于 0 的数字";
                    }
                } catch (NumberFormatException e) {
                    error = "数量格式不正确";
                }
            }

            if (error == null) {
                validCodes.add(skuCode);
            }

            rowItems.add(BulkResolveResponse.BulkRowItem.builder()
                    .lineNumber(lineNumber)
                    .raw(rawLine)
                    .skuCode(skuCode)
                    .quantity(quantity)
                    .error(error)
                    .build());
        }

        // 2. 批量从数据库中检索所有有效的 SKU
        Map<String, SkuPO> skuCodeMap = Collections.emptyMap();
        Map<String, List<PriceTierDTO>> tierMap = Collections.emptyMap();

        if (!validCodes.isEmpty()) {
            List<SkuPO> skuPOs = skuMapper.selectList(
                    new LambdaQueryWrapper<SkuPO>()
                            .in(SkuPO::getCode, validCodes)
                            .eq(SkuPO::getStatus, "available")
            );
            skuCodeMap = skuPOs.stream().collect(Collectors.toMap(SkuPO::getCode, s -> s, (k1, k2) -> k1));
            List<String> skuIds = skuPOs.stream().map(SkuPO::getId).toList();
            tierMap = pricingFacade.getPriceTiersMap(skuIds);
        }

        // 3. 逐行匹配并计算阶梯成交单价
        String companyId = TenantContextHolder.getCompanyId();
        int successCount = 0;
        int errorCount = 0;

        for (BulkResolveResponse.BulkRowItem row : rowItems) {
            if (row.getError() != null) {
                errorCount++;
                continue;
            }

            SkuPO skuPO = skuCodeMap.get(row.getSkuCode());
            if (skuPO == null) {
                row.setError("SKU 编码不存在或已下架停售");
                errorCount++;
            } else {
                List<PriceTierDTO> tiers = tierMap.getOrDefault(skuPO.getId(), Collections.emptyList());
                SkuDTO skuDTO = toSkuDTO(skuPO, tiers);
                row.setSku(skuDTO);

                // 计算当前数量命中的阶梯单价
                BigDecimal unitPrice = pricingFacade.getTierUnitPrice(companyId, skuPO.getId(), row.getQuantity());
                if (unitPrice.compareTo(BigDecimal.ZERO) == 0) {
                    unitPrice = skuPO.getPrice();
                }
                row.setUnitPrice(unitPrice);
                successCount++;
            }
        }

        return BulkResolveResponse.builder()
                .successCount(successCount)
                .errorCount(errorCount)
                .rows(rowItems)
                .build();
    }

    private ProductDTO toProductDTO(ProductPO po, List<SkuDTO> skus) {
        return ProductDTO.builder()
                .id(po.getId())
                .slug(po.getSlug())
                .title(po.getTitle())
                .subtitle(po.getSubtitle())
                .brand(po.getBrand())
                .categoryId(po.getCategoryId())
                .description(po.getDescription())
                .unit(po.getUnit())
                .defaultSkuId(po.getDefaultSkuId())
                .status(po.getStatus())
                .skus(skus)
                .build();
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
