package com.b2b.trade.infrastructure.persistence.mapper;

import com.b2b.trade.infrastructure.persistence.entity.OrderPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购订单数据访问 Mapper (OrderMapper)
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderPO> {
}
