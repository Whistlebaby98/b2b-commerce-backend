package com.b2b.pricing.infrastructure.persistence.mapper;

import com.b2b.pricing.infrastructure.persistence.entity.PriceTierPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 阶梯价格区间 Mapper 接口
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface PriceTierMapper extends BaseMapper<PriceTierPO> {
}
