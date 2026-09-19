package com.b2b.finance.controller;

import com.b2b.common.api.ApiResponse;
import com.b2b.common.tenant.TenantContextHolder;
import com.b2b.finance.api.dto.CompanyCreditDTO;
import com.b2b.finance.service.FinanceApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 企业授信额度控制器 (CreditController)
 *
 * <p>提供当前企业可用额度、总额度与已用授信查询。</p>
 *
 * @author b2b-commerce-backend
 */
@Tag(name = "03. 资金与授信管理", description = "企业授信额度与可用余额查询接口")
@RestController
@RequestMapping("/api/v1/finance/credit")
@RequiredArgsConstructor
public class CreditController {

    private final FinanceApplicationService financeApplicationService;

    @Operation(summary = "查询当前企业授信概况", description = "获取当前企业的总授信额度、已用额度及可用余额")
    @GetMapping
    public ApiResponse<CompanyCreditDTO> getCreditOverview() {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        return ApiResponse.success(financeApplicationService.getCreditOverview(companyId));
    }
}
