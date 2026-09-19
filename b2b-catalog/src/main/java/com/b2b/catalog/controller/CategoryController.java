package com.b2b.catalog.controller;

import com.b2b.catalog.api.dto.CategoryDTO;
import com.b2b.catalog.application.CatalogApplicationService;
import com.b2b.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品品类控制器 (CategoryController)
 *
 * <p>提供商品品类树与各品类商品数量查询接口。</p>
 *
 * @author b2b-commerce-backend
 */
@Tag(name = "01. 商品品类中心", description = "商品品类树与分类统计查询")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CatalogApplicationService catalogApplicationService;

    /**
     * 获取所有商品品类列表及有效商品数
     *
     * @return 包含品类信息的统一响应
     */
    @Operation(summary = "获取商品品类列表", description = "查询商城所有有效品类列表及下属商品数量")
    @GetMapping
    public ApiResponse<List<CategoryDTO>> listCategories() {
        List<CategoryDTO> categories = catalogApplicationService.listCategories();
        return ApiResponse.success(categories);
    }
}
