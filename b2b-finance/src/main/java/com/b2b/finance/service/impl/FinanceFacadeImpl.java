package com.b2b.finance.service.impl;

import com.b2b.common.exception.BizException;
import com.b2b.finance.api.FinanceFacade;
import com.b2b.finance.api.dto.AddressDTO;
import com.b2b.finance.api.dto.AddressSnapshotDTO;
import com.b2b.finance.api.dto.CreditCheckDTO;
import com.b2b.finance.api.dto.InvoiceDTO;
import com.b2b.finance.api.dto.InvoiceSnapshotDTO;
import com.b2b.finance.service.FinanceApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资金与结算门面实现类 (FinanceFacadeImpl)
 *
 * <p>为交易域等外部模块提供统一的资金、授信与地址资质防腐调用支持。</p>
 *
 * @author b2b-commerce-backend
 */
@Component
@RequiredArgsConstructor
public class FinanceFacadeImpl implements FinanceFacade {

    private final FinanceApplicationService financeApplicationService;

    @Override
    public CreditCheckDTO checkCredit(String companyId, BigDecimal amount) throws BizException {
        return financeApplicationService.checkCredit(companyId, amount);
    }

    @Override
    public boolean preFreezeCredit(String companyId, String orderId, BigDecimal amount) throws BizException {
        return financeApplicationService.preFreezeCredit(companyId, orderId, amount);
    }

    @Override
    public void releaseCredit(String companyId, String orderId, BigDecimal amount) {
        financeApplicationService.releaseCredit(companyId, orderId, amount);
    }

    @Override
    public AddressSnapshotDTO getAddressSnapshot(String companyId, String addressId) throws BizException {
        return financeApplicationService.getAddressSnapshot(companyId, addressId);
    }

    @Override
    public InvoiceSnapshotDTO getInvoiceSnapshot(String companyId, String invoiceId) throws BizException {
        return financeApplicationService.getInvoiceSnapshot(companyId, invoiceId);
    }

    @Override
    public List<AddressDTO> listAddresses(String companyId) {
        return financeApplicationService.listAddresses(companyId);
    }

    @Override
    public List<InvoiceDTO> listInvoices(String companyId) {
        return financeApplicationService.listInvoices(companyId);
    }
}
