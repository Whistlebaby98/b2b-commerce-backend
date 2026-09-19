package com.b2b.iam.api;

import com.b2b.common.exception.BizException;
import com.b2b.iam.api.dto.OrganizationDTO;
import com.b2b.iam.api.dto.UserDTO;

/**
 * 组织与身份域对外门面接口 (IamFacade)
 *
 * <p>供其他业务模块（如交易、计价、审批）查询企业组织资质、
 * 买方成员权限，严禁外部模块直接注入 IAM 内部的 Mapper 或访问私有表。</p>
 *
 * @author b2b-commerce-backend
 */
public interface IamFacade {

    /**
     * 获取指定客户企业组织详情
     *
     * @param companyId 企业组织唯一标识，不可为空
     * @return 客户企业组织详情 DTO
     * @throws BizException 当企业不存在或已被禁用时抛出
     */
    OrganizationDTO getOrganization(String companyId) throws BizException;

    /**
     * 校验买方用户是否具有目标企业组织的有效采购资格
     *
     * @param userId    买方用户 ID，不可为空
     * @param companyId 目标客户企业 ID，不可为空
     * @return 若具备有效隶属关系返回 true，否则返回 false
     */
    boolean validateMembership(String userId, String companyId);

    /**
     * 获取买方用户在指定企业下的档案信息
     *
     * @param userId    用户 ID，不可为空
     * @param companyId 企业 ID，不可为空
     * @return 买方用户 DTO（包含其在当前企业下的角色与部门）
     * @throws BizException 用户不存在或与企业无成员关系时抛出
     */
    UserDTO getUserProfile(String userId, String companyId) throws BizException;
}
