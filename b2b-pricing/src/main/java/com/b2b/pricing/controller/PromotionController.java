package com.b2b.pricing.controller;

import com.b2b.common.api.ApiResponse;
import com.b2b.pricing.api.dto.PromotionDTO;
import com.b2b.pricing.application.PromotionApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 营销与促销活动控制器 (PromotionController)
 *
 * <p>提供前台商城促销活动列表浏览与活动详情查询接口。</p>
 *
 * @author b2b-commerce-backend
 */
@Tag(name = "02. 营销与促销中心", description = "促销活动、品类批量折扣、满减券与免运费权益查询接口")
@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionApplicationService promotionApplicationService;

    @Operation(summary = "查询正在进行的促销活动", description = "获取当前处于生效窗口期内且对公开放的所有促销活动列表")
    @GetMapping
    public ApiResponse<List<PromotionDTO>> listActivePromotions() {
        return ApiResponse.success(promotionApplicationService.listActivePromotions());
    }

    @Operation(summary = "查询促销活动详情", description = "根据活动 ID 获取该促销规则的适用范围、满减档位及优惠力度")
    @GetMapping("/{id}")
    public ApiResponse<PromotionDTO> getPromotionById(
            @Parameter(description = "促销活动 ID", required = true)
            @PathVariable("id") String id
    ) {
        return ApiResponse.success(promotionApplicationService.getPromotionById(id));
    }
}
