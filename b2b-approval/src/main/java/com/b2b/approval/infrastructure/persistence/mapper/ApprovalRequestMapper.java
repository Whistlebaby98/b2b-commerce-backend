package com.b2b.approval.infrastructure.persistence.mapper;

import com.b2b.approval.infrastructure.persistence.entity.ApprovalRequestPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单审批数据访问 Mapper (ApprovalRequestMapper)
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface ApprovalRequestMapper extends BaseMapper<ApprovalRequestPO> {
}
