package com.b2b.finance.service;

import com.b2b.common.api.ResultCode;
import com.b2b.common.exception.BizException;
import com.b2b.finance.api.dto.AddressDTO;
import com.b2b.finance.api.dto.AddressSnapshotDTO;
import com.b2b.finance.api.dto.CompanyCreditDTO;
import com.b2b.finance.api.dto.CreditCheckDTO;
import com.b2b.finance.api.dto.InvoiceDTO;
import com.b2b.finance.api.dto.InvoiceSnapshotDTO;
import com.b2b.finance.infrastructure.persistence.entity.CompanyAddressPO;
import com.b2b.finance.infrastructure.persistence.entity.InvoiceProfilePO;
import com.b2b.finance.infrastructure.persistence.mapper.CompanyAddressMapper;
import com.b2b.finance.infrastructure.persistence.mapper.CompanyCreditMapper;
import com.b2b.finance.infrastructure.persistence.mapper.InvoiceProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.b2b.finance.api.dto.CreateAddressRequest;
import com.b2b.finance.api.dto.CreateInvoiceRequest;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 资金与结算应用服务 (FinanceApplicationService)
 *
 * <p>处理企业授信额度 CAS 预占/释放、地址管理及开票资质管理。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceApplicationService {

    private final CompanyCreditMapper companyCreditMapper;
    private final CompanyAddressMapper companyAddressMapper;
    private final InvoiceProfileMapper invoiceProfileMapper;

    /**
     * 核验企业当前可用授信额度是否充足
     */
    public CreditCheckDTO checkCredit(String companyId, BigDecimal amount) {
        CompanyCreditMapper.CompanyCreditRecord record = companyCreditMapper.selectCreditRecord(companyId);
        if (record == null) {
            throw new BizException(ResultCode.TENANT_NOT_FOUND, "客户企业授信账户不存在: " + companyId);
        }

        BigDecimal limit = record.getCreditLimit() != null ? record.getCreditLimit() : BigDecimal.ZERO;
        BigDecimal used = record.getCreditUsed() != null ? record.getCreditUsed() : BigDecimal.ZERO;
        BigDecimal available = limit.subtract(used);
        boolean isSufficient = available.compareTo(amount) >= 0;

        return CreditCheckDTO.builder()
                .companyId(companyId)
                .creditLimit(limit)
                .creditUsed(used)
                .availableCredit(available)
                .requestAmount(amount)
                .isSufficient(isSufficient)
                .build();
    }

    /**
     * 执行授信额度预占（CAS 乐观扣减）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean preFreezeCredit(String companyId, String orderId, BigDecimal amount) {
        log.info("[Finance] 开始执行企业授信 CAS 预占: companyId={}, orderId={}, amount={}", companyId, orderId, amount);
        int rows = companyCreditMapper.casPreFreezeCredit(companyId, amount);
        if (rows > 0) {
            log.info("[Finance] 授信 CAS 预占成功: companyId={}, orderId={}, amount={}", companyId, orderId, amount);
            return true;
        } else {
            log.warn("[Finance] 授信 CAS 预占失败 (额度不足或并发冲突): companyId={}, orderId={}, amount={}", companyId, orderId, amount);
            return false;
        }
    }

    /**
     * 释放已预占的授信额度
     */
    @Transactional(rollbackFor = Exception.class)
    public void releaseCredit(String companyId, String orderId, BigDecimal amount) {
        log.info("[Finance] 开始释放企业已占用授信: companyId={}, orderId={}, amount={}", companyId, orderId, amount);
        companyCreditMapper.releaseCredit(companyId, amount);
    }

    /**
     * 获取企业授信概览
     */
    public CompanyCreditDTO getCreditOverview(String companyId) {
        CompanyCreditMapper.CompanyCreditRecord record = companyCreditMapper.selectCreditRecord(companyId);
        if (record == null) {
            throw new BizException(ResultCode.TENANT_NOT_FOUND, "客户企业授信账户不存在: " + companyId);
        }
        BigDecimal limit = record.getCreditLimit() != null ? record.getCreditLimit() : BigDecimal.ZERO;
        BigDecimal used = record.getCreditUsed() != null ? record.getCreditUsed() : BigDecimal.ZERO;
        return CompanyCreditDTO.builder()
                .companyId(record.getId())
                .companyName(record.getName())
                .creditLimit(limit)
                .creditUsed(used)
                .availableCredit(limit.subtract(used))
                .currency(record.getCurrency())
                .build();
    }

    /**
     * 获取指定收货地址不可变快照
     */
    public AddressSnapshotDTO getAddressSnapshot(String companyId, String addressId) {
        CompanyAddressPO address = companyAddressMapper.selectOne(
                new LambdaQueryWrapper<CompanyAddressPO>()
                        .eq(CompanyAddressPO::getCompanyId, companyId)
                        .eq(CompanyAddressPO::getId, addressId)
        );
        if (address == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "收货地址不存在或不属于当前企业: " + addressId);
        }
        return AddressSnapshotDTO.builder()
                .id(address.getId())
                .label(address.getLabel())
                .kind(address.getKind())
                .recipient(address.getRecipient())
                .phone(address.getPhone())
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detail(address.getDetail())
                .postalCode(address.getPostalCode())
                .build();
    }

    /**
     * 获取指定开票资质不可变快照
     */
    public InvoiceSnapshotDTO getInvoiceSnapshot(String companyId, String invoiceId) {
        InvoiceProfilePO invoice = invoiceProfileMapper.selectOne(
                new LambdaQueryWrapper<InvoiceProfilePO>()
                        .eq(InvoiceProfilePO::getCompanyId, companyId)
                        .eq(InvoiceProfilePO::getId, invoiceId)
        );
        if (invoice == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "开票资质档案不存在或不属于当前企业: " + invoiceId);
        }
        return InvoiceSnapshotDTO.builder()
                .id(invoice.getId())
                .type(invoice.getType())
                .title(invoice.getTitle())
                .taxId(invoice.getTaxId())
                .bankName(invoice.getBankName())
                .bankAccount(invoice.getBankAccount())
                .registeredAddress(invoice.getRegisteredAddress())
                .registeredPhone(invoice.getRegisteredPhone())
                .receiveEmail(invoice.getReceiveEmail())
                .build();
    }

    /**
     * 查询指定企业的所有有效地址档案
     */
    public List<AddressDTO> listAddresses(String companyId) {
        List<CompanyAddressPO> list = companyAddressMapper.selectList(
                new LambdaQueryWrapper<CompanyAddressPO>()
                        .eq(CompanyAddressPO::getCompanyId, companyId)
                        .orderByDesc(CompanyAddressPO::getIsDefault)
                        .orderByDesc(CompanyAddressPO::getCreatedAt)
        );
        return list.stream().map(this::toAddressDTO).collect(Collectors.toList());
    }

    /**
     * 查询指定企业的所有有效开票资质档案
     */
    public List<InvoiceDTO> listInvoices(String companyId) {
        List<InvoiceProfilePO> list = invoiceProfileMapper.selectList(
                new LambdaQueryWrapper<InvoiceProfilePO>()
                        .eq(InvoiceProfilePO::getCompanyId, companyId)
                        .orderByDesc(InvoiceProfilePO::getIsDefault)
                        .orderByDesc(InvoiceProfilePO::getCreatedAt)
        );
        return list.stream().map(this::toInvoiceDTO).collect(Collectors.toList());
    }

    /**
     * 新增企业地址
     *
     * @param companyId 当前企业 ID
     * @param req       地址入参
     * @return 新建的地址 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public AddressDTO createAddress(String companyId, CreateAddressRequest req) {
        boolean isDefault = Boolean.TRUE.equals(req.getIsDefault());
        if (isDefault) {
            companyAddressMapper.update(null,
                    new LambdaUpdateWrapper<CompanyAddressPO>()
                            .eq(CompanyAddressPO::getCompanyId, companyId)
                            .eq(CompanyAddressPO::getKind, req.getKind())
                            .set(CompanyAddressPO::getIsDefault, false)
            );
        }

        String addressId = "addr_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        CompanyAddressPO po = CompanyAddressPO.builder()
                .id(addressId)
                .companyId(companyId)
                .label(req.getLabel())
                .kind(req.getKind())
                .recipient(req.getRecipient())
                .phone(req.getPhone())
                .province(req.getProvince())
                .city(req.getCity())
                .district(req.getDistrict())
                .detail(req.getDetail())
                .postalCode(req.getPostalCode())
                .isDefault(isDefault)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();

        companyAddressMapper.insert(po);
        return toAddressDTO(po);
    }

    /**
     * 设置企业默认地址
     *
     * @param companyId 当前企业 ID
     * @param addressId 目标地址 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(String companyId, String addressId) {
        CompanyAddressPO target = companyAddressMapper.selectOne(
                new LambdaQueryWrapper<CompanyAddressPO>()
                        .eq(CompanyAddressPO::getCompanyId, companyId)
                        .eq(CompanyAddressPO::getId, addressId)
        );
        if (target == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "地址不存在: " + addressId);
        }

        companyAddressMapper.update(null,
                new LambdaUpdateWrapper<CompanyAddressPO>()
                        .eq(CompanyAddressPO::getCompanyId, companyId)
                        .eq(CompanyAddressPO::getKind, target.getKind())
                        .set(CompanyAddressPO::getIsDefault, false)
        );

        target.setIsDefault(true);
        target.setUpdatedAt(Instant.now());
        companyAddressMapper.updateById(target);
    }

    /**
     * 删除企业地址
     *
     * @param companyId 当前企业 ID
     * @param addressId 目标地址 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(String companyId, String addressId) {
        companyAddressMapper.delete(
                new LambdaQueryWrapper<CompanyAddressPO>()
                        .eq(CompanyAddressPO::getCompanyId, companyId)
                        .eq(CompanyAddressPO::getId, addressId)
        );
    }

    /**
     * 新增企业开票资质
     *
     * @param companyId 当前企业 ID
     * @param req       开票资质入参
     * @return 新建的开票资质 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public InvoiceDTO createInvoice(String companyId, CreateInvoiceRequest req) {
        boolean isDefault = Boolean.TRUE.equals(req.getIsDefault());
        if (isDefault) {
            invoiceProfileMapper.update(null,
                    new LambdaUpdateWrapper<InvoiceProfilePO>()
                            .eq(InvoiceProfilePO::getCompanyId, companyId)
                            .set(InvoiceProfilePO::getIsDefault, false)
            );
        }

        String invoiceId = "inv_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        InvoiceProfilePO po = InvoiceProfilePO.builder()
                .id(invoiceId)
                .companyId(companyId)
                .type(req.getType())
                .title(req.getTitle())
                .taxId(req.getTaxId())
                .bankName(req.getBankName())
                .bankAccount(req.getBankAccount())
                .registeredAddress(req.getRegisteredAddress())
                .registeredPhone(req.getRegisteredPhone())
                .receiveEmail(req.getReceiveEmail())
                .status("active")
                .isDefault(isDefault)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .deleted(false)
                .build();

        invoiceProfileMapper.insert(po);
        return toInvoiceDTO(po);
    }

    /**
     * 设置企业默认开票资质
     *
     * @param companyId 当前企业 ID
     * @param invoiceId 目标开票资质 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultInvoice(String companyId, String invoiceId) {
        InvoiceProfilePO target = invoiceProfileMapper.selectOne(
                new LambdaQueryWrapper<InvoiceProfilePO>()
                        .eq(InvoiceProfilePO::getCompanyId, companyId)
                        .eq(InvoiceProfilePO::getId, invoiceId)
        );
        if (target == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "开票资质档案不存在: " + invoiceId);
        }

        invoiceProfileMapper.update(null,
                new LambdaUpdateWrapper<InvoiceProfilePO>()
                        .eq(InvoiceProfilePO::getCompanyId, companyId)
                        .set(InvoiceProfilePO::getIsDefault, false)
        );

        target.setIsDefault(true);
        target.setUpdatedAt(Instant.now());
        invoiceProfileMapper.updateById(target);
    }

    /**
     * 删除企业开票资质
     *
     * @param companyId 当前企业 ID
     * @param invoiceId 目标开票资质 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteInvoice(String companyId, String invoiceId) {
        invoiceProfileMapper.delete(
                new LambdaQueryWrapper<InvoiceProfilePO>()
                        .eq(InvoiceProfilePO::getCompanyId, companyId)
                        .eq(InvoiceProfilePO::getId, invoiceId)
        );
    }

    private AddressDTO toAddressDTO(CompanyAddressPO po) {
        return AddressDTO.builder()
                .id(po.getId())
                .companyId(po.getCompanyId())
                .label(po.getLabel())
                .kind(po.getKind())
                .recipient(po.getRecipient())
                .phone(po.getPhone())
                .province(po.getProvince())
                .city(po.getCity())
                .district(po.getDistrict())
                .detail(po.getDetail())
                .postalCode(po.getPostalCode())
                .isDefault(po.getIsDefault())
                .build();
    }

    private InvoiceDTO toInvoiceDTO(InvoiceProfilePO po) {
        return InvoiceDTO.builder()
                .id(po.getId())
                .companyId(po.getCompanyId())
                .type(po.getType())
                .title(po.getTitle())
                .taxId(po.getTaxId())
                .bankName(po.getBankName())
                .bankAccount(po.getBankAccount())
                .registeredAddress(po.getRegisteredAddress())
                .registeredPhone(po.getRegisteredPhone())
                .receiveEmail(po.getReceiveEmail())
                .status(po.getStatus())
                .isDefault(po.getIsDefault())
                .build();
    }
}
