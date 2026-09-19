package com.b2b.iam.infrastructure.persistence.mapper;

import com.b2b.iam.infrastructure.persistence.entity.CompanyPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户企业组织 Mapper 接口
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface CompanyMapper extends BaseMapper<CompanyPO> {
}
