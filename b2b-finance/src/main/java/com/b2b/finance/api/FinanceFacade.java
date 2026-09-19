package com.b2b.finance.api;

import com.b2b.common.exception.BizException;
import com.b2b.finance.api.dto.AddressDTO;
import com.b2b.finance.api.dto.AddressSnapshotDTO;
import com.b2b.finance.api.dto.CreditCheckDTO;
import com.b2b.finance.api.dto.InvoiceDTO;
import com.b2b.finance.api.dto.InvoiceSnapshotDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资金与结算域对外门面接口 (FinanceFacade)
 *
 * <p>遵循 ADR 0007 规范，提供企业授信额度核验、预占与释放服务，
 * 以及收货地址快照、开票资质快照的防腐获取，严禁交易模块直接操作授信与资质数据表。</p>
 *
 * @author b2b-commerce-backend
 */
public interface FinanceFacade {

    /**
     * 核验企业当前可用授信额度是否充足
     *
     * @param companyId 客户企业 ID，不可为空
     * @param amount    需核验的订单总额，必须大于 0
     * @return 授信额度核验结果 DTO
     * @throws BizException 授信账户不存在时抛出
     */
    CreditCheckDTO checkCredit(String companyId, BigDecimal amount) throws BizException;

    /**
     * 执行授信额度预占（CAS 乐观扣减）
     *
     * <p>在订单提交成功但尚未通过审批阶段，预冻结对应金额，防并发超额下单。</p>
     *
     * @param companyId 客户企业 ID，不可为空
     * @param orderId   关联的订单 ID，用于流水溯源
     * @param amount    预占金额，必须大于 0
     * @return 预占成功返回 true，额度不足返回 false
     * @throws BizException 账户状态异常时抛出
     */
    boolean preFreezeCredit(String companyId, String orderId, BigDecimal amount) throws BizException;

    /**
     * 释放已预占的授信额度
     *
     * <p>当订单被审批驳回、买方主动取消或超时失效时，将预冻结额度回滚还回可用额度。</p>
     *
     * @param companyId 客户企业 ID，不可为空
     * @param orderId   关联的订单 ID
     * @param amount    释放金额，必须大于 0
     */
    void releaseCredit(String companyId, String orderId, BigDecimal amount);

    /**
     * 获取指定收货地址不可变快照
     *
     * @param companyId 企业 ID，不可为空
     * @param addressId 地址 ID，不可为空
     * @return 地址快照 DTO
     * @throws BizException 当地址不存在或不属于该企业时抛出
     */
    AddressSnapshotDTO getAddressSnapshot(String companyId, String addressId) throws BizException;

    /**
     * 获取指定开票资质不可变快照
     *
     * @param companyId 企业 ID，不可为空
     * @param invoiceId 开票资质 ID，不可为空
     * @return 开票快照 DTO
     * @throws BizException 当资质不存在或不属于该企业时抛出
     */
    InvoiceSnapshotDTO getInvoiceSnapshot(String companyId, String invoiceId) throws BizException;

    /**
     * 查询指定企业的所有有效地址档案
     *
     * @param companyId 企业 ID，不可为空
     * @return 地址列表
     */
    List<AddressDTO> listAddresses(String companyId);

    /**
     * 查询指定企业的所有有效开票资质档案
     *
     * @param companyId 企业 ID，不可为空
     * @return 开票资质列表
     */
    List<InvoiceDTO> listInvoices(String companyId);
}
