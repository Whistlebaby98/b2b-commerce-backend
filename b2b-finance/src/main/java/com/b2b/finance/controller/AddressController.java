package com.b2b.finance.controller;

import com.b2b.common.api.ApiResponse;
import com.b2b.common.tenant.TenantContextHolder;
import com.b2b.finance.api.dto.AddressDTO;
import com.b2b.finance.service.FinanceApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 企业地址管理控制器 (AddressController)
 *
 * <p>提供当前企业上下文下的收货地址与开票地址查询与选择。</p>
 *
 * @author b2b-commerce-backend
 */
@Tag(name = "03. 结算与地址管理", description = "企业收货地址与开票档案管理接口")
@RestController
@RequestMapping("/api/v1/finance/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final FinanceApplicationService financeApplicationService;

    @Operation(summary = "查询当前企业地址列表", description = "获取当前企业有效收货地址与开票地址档案")
    @GetMapping
    public ApiResponse<List<AddressDTO>> listAddresses() {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        return ApiResponse.success(financeApplicationService.listAddresses(companyId));
    }
}
