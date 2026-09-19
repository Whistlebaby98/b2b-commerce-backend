package com.b2b.iam.infrastructure.persistence.mapper;

import com.b2b.iam.infrastructure.persistence.entity.MembershipPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户与企业组织成员关系 Mapper 接口
 *
 * @author b2b-commerce-backend
 */
@Mapper
public interface MembershipMapper extends BaseMapper<MembershipPO> {
}
