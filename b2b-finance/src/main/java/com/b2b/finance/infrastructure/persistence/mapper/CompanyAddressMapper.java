package com.b2b.finance.infrastructure.persistence.mapper;

import com.b2b.finance.infrastructure.persistence.entity.CompanyAddressPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业地址数据访问 Mapper (CompanyAddressMapper)
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface CompanyAddressMapper extends BaseMapper<CompanyAddressPO> {
}
