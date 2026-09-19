package com.b2b.catalog.infrastructure.persistence.mapper;

import com.b2b.catalog.infrastructure.persistence.entity.ProductPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品族档案 Mapper 接口
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductPO> {
}
