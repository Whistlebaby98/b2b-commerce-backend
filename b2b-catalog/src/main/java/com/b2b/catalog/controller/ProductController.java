package com.b2b.catalog.controller;

import com.b2b.catalog.api.dto.BulkResolveRequest;
import com.b2b.catalog.api.dto.BulkResolveResponse;
import com.b2b.catalog.api.dto.CatalogQueryRequest;
import com.b2b.catalog.api.dto.PageResult;
import com.b2b.catalog.api.dto.ProductDTO;
import com.b2b.catalog.application.CatalogApplicationService;
import com.b2b.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品目录与检索控制器 (ProductController)
 *
 * <p>提供商品多维筛选搜索、详情查询以及批量采购清单解析接口。</p>
 *
 * @author b2b-commerce-backend
 */
@Tag(name = "02. 商品与选品中心", description = "商品目录检索、SKU 规格详情与批量采购清单解析")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final CatalogApplicationService catalogApplicationService;

    /**
     * 多条件分页检索商品目录
     *
     * @param request 筛选入参 (query, categoryId, brand, availability, minPrice, maxPrice, sortBy, page, pageSize)
     * @return 分页商品列表
     */
    @Operation(summary = "商品目录搜索与筛选", description = "支持按关键词、品类、品牌、现货状态、价格区间与排序分页浏览商品")
    @GetMapping
    public ApiResponse<PageResult<ProductDTO>> searchProducts(CatalogQueryRequest request) {
        PageResult<ProductDTO> result = catalogApplicationService.searchProducts(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取指定商品详情及关联的完整 SKU 与阶梯价
     *
     * @param id 商品唯一标识
     * @return 完整商品详情
     */
    @Operation(summary = "获取商品详情", description = "根据商品 ID 查询商品详细档案，包含下属所有可售 SKU、规格参数与阶梯优惠价")
    @GetMapping("/{id}")
    public ApiResponse<ProductDTO> getProductDetail(@PathVariable("id") String id) {
        ProductDTO product = catalogApplicationService.getProductDetail(id);
        return ApiResponse.success(product);
    }

    /**
     * 批量采购清单解析 (Bulk Resolve)
     *
     * @param request 包含多行文本清单的请求入参
     * @return 逐行匹配与错误行标记结果
     */
    @Operation(summary = "批量采购清单解析", description = "输入 SKU 编码与数量的多行文本清单，逐行匹配商品有效性、库存并计算阶梯单价")
    @PostMapping("/bulk-resolve")
    public ApiResponse<BulkResolveResponse> resolveBulkSkus(@Valid @RequestBody BulkResolveRequest request) {
        BulkResolveResponse response = catalogApplicationService.resolveBulkSkus(request);
        return ApiResponse.success(response);
    }
}
