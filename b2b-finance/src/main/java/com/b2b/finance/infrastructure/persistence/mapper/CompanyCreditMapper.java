package com.b2b.finance.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 企业授信额度原子操作 Mapper (CompanyCreditMapper)
 *
 * <p>遵循 ADR 0007 规范，通过数据库原子 CAS 乐观更新实现授信额度预占与释放，杜绝超额下单。</p>
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface CompanyCreditMapper {

    /**
     * 原子 CAS 预占授信额度
     *
     * <p>仅当 (credit_limit - credit_used) >= amount 时才执行更新，原子扣减防并发击穿。</p>
     *
     * @param companyId 目标客户企业 ID
     * @param amount    需预占的金额
     * @return 影响行数，1 表示预占成功，0 表示可用额度不足或并发失败
     */
    @Update("UPDATE org_company SET credit_used = credit_used + #{amount}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{companyId} AND (credit_limit - credit_used) >= #{amount} AND deleted = false")
    int casPreFreezeCredit(@Param("companyId") String companyId, @Param("amount") BigDecimal amount);

    /**
     * 原子释放授信额度
     *
     * <p>当订单驳回、取消或失效时回滚已占用的授信。</p>
     *
     * @param companyId 目标客户企业 ID
     * @param amount    需释放的金额
     * @return 影响行数
     */
    @Update("UPDATE org_company SET credit_used = credit_used - #{amount}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{companyId} AND deleted = false")
    int releaseCredit(@Param("companyId") String companyId, @Param("amount") BigDecimal amount);

    /**
     * 查询企业当前授信数据
     *
     * @param companyId 目标客户企业 ID
     * @return 企业授信数据对象
     */
    @Select("SELECT id, name, credit_limit AS creditLimit, credit_used AS creditUsed, currency " +
            "FROM org_company WHERE id = #{companyId} AND deleted = false")
    CompanyCreditRecord selectCreditRecord(@Param("companyId") String companyId);

    /**
     * 内部记录载体
     */
    class CompanyCreditRecord {
        private String id;
        private String name;
        private BigDecimal creditLimit;
        private BigDecimal creditUsed;
        private String currency;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getCreditLimit() { return creditLimit; }
        public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
        public BigDecimal getCreditUsed() { return creditUsed; }
        public void setCreditUsed(BigDecimal creditUsed) { this.creditUsed = creditUsed; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}
