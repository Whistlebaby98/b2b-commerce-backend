package com.b2b.catalog.infrastructure.persistence.mapper;

import com.b2b.catalog.infrastructure.persistence.entity.CategoryPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品品类 Mapper 接口
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryPO> {
}
