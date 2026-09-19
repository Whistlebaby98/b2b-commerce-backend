package com.b2b.iam.infrastructure.persistence.mapper;

import com.b2b.iam.infrastructure.persistence.entity.UserPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 买方用户 Mapper 接口
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {
}
