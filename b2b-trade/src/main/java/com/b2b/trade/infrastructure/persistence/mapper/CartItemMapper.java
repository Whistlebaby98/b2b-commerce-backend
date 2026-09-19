package com.b2b.trade.infrastructure.persistence.mapper;

import com.b2b.trade.infrastructure.persistence.entity.CartItemPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购车数据访问 Mapper (CartItemMapper)
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItemPO> {
}
