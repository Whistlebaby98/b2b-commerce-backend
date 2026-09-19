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

    @Operation(summary = "新增企业地址", description = "为当前企业新增收货或开票地址档案")
    @org.springframework.web.bind.annotation.PostMapping
    public ApiResponse<AddressDTO> createAddress(@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.b2b.finance.api.dto.CreateAddressRequest req) {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        return ApiResponse.success(financeApplicationService.createAddress(companyId, req));
    }

    @Operation(summary = "设置默认企业地址", description = "将指定地址设为当前企业的默认收货/开票地址")
    @org.springframework.web.bind.annotation.PutMapping("/{id}/default")
    public ApiResponse<Void> setDefaultAddress(
            @io.swagger.v3.oas.annotations.Parameter(description = "地址 ID", required = true)
            @org.springframework.web.bind.annotation.PathVariable("id") String id
    ) {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        financeApplicationService.setDefaultAddress(companyId, id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "删除企业地址", description = "从当前企业档案中删除指定地址")
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAddress(
            @io.swagger.v3.oas.annotations.Parameter(description = "地址 ID", required = true)
            @org.springframework.web.bind.annotation.PathVariable("id") String id
    ) {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        financeApplicationService.deleteAddress(companyId, id);
        return ApiResponse.success(null);
    }
}
