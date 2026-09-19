package com.b2b.trade.infrastructure.persistence.mapper;

import com.b2b.trade.infrastructure.persistence.entity.OrderLinePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购订单行项数据访问 Mapper (OrderLineMapper)
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface OrderLineMapper extends BaseMapper<OrderLinePO> {
}
