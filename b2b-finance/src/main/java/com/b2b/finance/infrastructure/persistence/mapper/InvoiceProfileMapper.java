package com.b2b.finance.infrastructure.persistence.mapper;

import com.b2b.finance.infrastructure.persistence.entity.InvoiceProfilePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业开票资质数据访问 Mapper (InvoiceProfileMapper)
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface InvoiceProfileMapper extends BaseMapper<InvoiceProfilePO> {
}
