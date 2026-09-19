package com.b2b.common.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 当前请求绑定的企业组织与用户上下文 (ActiveOrganizationContext)
 *
 * <p>遵循 ADR 0005 规范，代表买方用户当前选定的企业组织，
 * 决定当前会话中可见的商品价格、库存、购物车与订单视图。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantContext implements Serializable {

    /**
     * 当前选定的客户企业组织 ID (Company ID)
     */
    private String companyId;

    /**
     * 当前登录的买方用户 ID (User ID)
     */
    private String userId;

    /**
     * 当前用户在当前企业下的角色 (buyer, approver, finance, owner)
     */
    private String role;
}
