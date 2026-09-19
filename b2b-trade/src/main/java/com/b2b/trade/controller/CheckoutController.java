package com.b2b.trade.controller;

import com.b2b.common.api.ApiResponse;
import com.b2b.trade.api.dto.CheckoutPreviewDTO;
import com.b2b.trade.api.dto.CheckoutPreviewRequest;
import com.b2b.trade.service.CheckoutApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 结算试算会话控制器 (CheckoutController)
 *
 * <p>进入结算确认页时调用，提供结算金额试算、企业授信预检与防重 Token 下发。</p>
 *
 * @author b2b-commerce-backend
 */
@Tag(name = "01. 结算试算会话", description = "结算金额试算、收货开票档案匹配与防重提交 Token 预领")
@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutApplicationService checkoutApplicationService;

    @Operation(summary = "结算金额试算与防重 Token 预领", description = "试算采购总额、优惠减免与运费，预检企业授信额度，并下发单次有效 checkout_token")
    @PostMapping("/preview")
    public ApiResponse<CheckoutPreviewDTO> previewCheckout(@RequestBody(required = false) CheckoutPreviewRequest req) {
        if (req == null) {
            req = new CheckoutPreviewRequest();
        }
        return ApiResponse.success(checkoutApplicationService.previewCheckout(req));
    }
}
