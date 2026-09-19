package com.b2b.catalog.infrastructure.persistence.mapper;

import com.b2b.catalog.infrastructure.persistence.entity.SkuPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存单位 (SKU) Mapper 接口
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface SkuMapper extends BaseMapper<SkuPO> {
}
