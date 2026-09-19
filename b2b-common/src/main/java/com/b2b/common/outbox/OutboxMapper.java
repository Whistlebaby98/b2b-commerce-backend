package com.b2b.common.outbox;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 本地消息表数据访问 Mapper (OutboxMapper)
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface OutboxMapper extends BaseMapper<OutboxMessage> {
}
