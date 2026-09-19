package com.b2b.pricing.infrastructure.persistence.mapper;

import com.b2b.pricing.infrastructure.persistence.entity.PromotionPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 营销与促销数据访问接口 (PromotionMapper)
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface PromotionMapper extends BaseMapper<PromotionPO> {
}
