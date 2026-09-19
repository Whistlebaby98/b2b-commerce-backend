package com.b2b.finance.controller;

import com.b2b.common.api.ApiResponse;
import com.b2b.common.tenant.TenantContextHolder;
import com.b2b.finance.api.dto.InvoiceDTO;
import com.b2b.finance.service.FinanceApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 企业开票资质管理控制器 (InvoiceController)
 *
 * <p>提供当前企业上下文下的开票资质档案查询。</p>
 *
 * @author b2b-commerce-backend
 */
@Tag(name = "03. 结算与开票资质", description = "企业增值税专用发票、普通发票及电子发票资质管理接口")
@RestController
@RequestMapping("/api/v1/finance/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final FinanceApplicationService financeApplicationService;

    @Operation(summary = "查询当前企业开票资质列表", description = "获取当前企业有效开票资质档案")
    @GetMapping
    public ApiResponse<List<InvoiceDTO>> listInvoices() {
        String companyId = TenantContextHolder.getRequiredCompanyId();
        return ApiResponse.success(financeApplicationService.listInvoices(companyId));
    }
}
