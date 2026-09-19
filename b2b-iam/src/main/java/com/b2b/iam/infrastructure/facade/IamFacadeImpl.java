package com.b2b.iam.infrastructure.facade;

import com.b2b.common.api.ResultCode;
import com.b2b.common.exception.BizException;
import com.b2b.iam.api.IamFacade;
import com.b2b.iam.api.dto.OrganizationDTO;
import com.b2b.iam.api.dto.UserDTO;
import com.b2b.iam.infrastructure.persistence.entity.CompanyPO;
import com.b2b.iam.infrastructure.persistence.entity.MembershipPO;
import com.b2b.iam.infrastructure.persistence.entity.UserPO;
import com.b2b.iam.infrastructure.persistence.mapper.CompanyMapper;
import com.b2b.iam.infrastructure.persistence.mapper.MembershipMapper;
import com.b2b.iam.infrastructure.persistence.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 组织与身份域对外门面实现类 (IamFacadeImpl)
 *
 * <p>为交易域、计价域与审批域提供跨模块组织档案与买方权限查询服务，
 * 封装内部实体与 Mapper，防止外部模块直接访问 IAM 数据库表。</p>
 *
 * @author b2b-commerce-backend
 */
@Service
@RequiredArgsConstructor
public class IamFacadeImpl implements IamFacade {

    private final CompanyMapper companyMapper;
    private final UserMapper userMapper;
    private final MembershipMapper membershipMapper;

    @Override
    public OrganizationDTO getOrganization(String companyId) throws BizException {
        CompanyPO company = companyMapper.selectById(companyId);
        if (company == null || Boolean.TRUE.equals(company.getDeleted())) {
            throw new BizException(ResultCode.TENANT_NOT_FOUND);
        }
        return OrganizationDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .shortName(company.getShortName())
                .taxId(company.getTaxId())
                .customerTier(company.getCustomerTier())
                .isVerified(company.getIsVerified())
                .creditLimit(company.getCreditLimit())
                .creditUsed(company.getCreditUsed())
                .paymentTermDays(company.getPaymentTermDays())
                .currency(company.getCurrency())
                .build();
    }

    @Override
    public boolean validateMembership(String userId, String companyId) {
        Long count = membershipMapper.selectCount(
                new LambdaQueryWrapper<MembershipPO>()
                        .eq(MembershipPO::getUserId, userId)
                        .eq(MembershipPO::getCompanyId, companyId)
        );
        return count != null && count > 0;
    }

    @Override
    public UserDTO getUserProfile(String userId, String companyId) throws BizException {
        UserPO user = userMapper.selectById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }

        MembershipPO membership = membershipMapper.selectOne(
                new LambdaQueryWrapper<MembershipPO>()
                        .eq(MembershipPO::getUserId, userId)
                        .eq(MembershipPO::getCompanyId, companyId)
        );
        if (membership == null) {
            throw new BizException(ResultCode.MEMBERSHIP_INVALID);
        }

        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .role(membership.getRole())
                .department(user.getDepartment())
                .avatarText(user.getAvatarText())
                .companyId(companyId)
                .build();
    }
}
