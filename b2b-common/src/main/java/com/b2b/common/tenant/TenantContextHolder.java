package com.b2b.common.tenant;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.b2b.common.exception.BizException;

/**
 * 租户与企业上下文持有者 (TenantContextHolder)
 *
 * <p>基于阿里 {@link TransmittableThreadLocal} 实现，保证在父子线程、
 * 异步线程池以及 Java 21 虚拟线程之间安全穿透当前组织上下文，杜绝多租户数据穿透。</p>
 *
 * @author b2b-commerce-backend
 */
public final class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CONTEXT_HOLDER = new TransmittableThreadLocal<>();

    private TenantContextHolder() {
        // 工具类私有构造
    }

    /**
     * 设置当前线程的租户上下文
     *
     * @param context 租户上下文
     */
    public static void setContext(TenantContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取当前线程绑定的租户上下文
     *
     * @return 租户上下文，若未绑定返回 null
     */
    public static TenantContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 快捷获取当前企业组织 ID (company_id)
     *
     * @return 企业组织 ID，若未绑定返回 null
     */
    public static String getCompanyId() {
        TenantContext context = getContext();
        return context != null ? context.getCompanyId() : null;
    }

    /**
     * 获取当前企业组织 ID，若不存在则抛出 MISSING_TENANT_HEADER 异常
     *
     * @return 企业组织 ID
     */
    public static String getRequiredCompanyId() {
        String companyId = getCompanyId();
        if (companyId == null || companyId.isBlank()) {
            throw new BizException(com.b2b.common.api.ResultCode.MISSING_TENANT_HEADER);
        }
        return companyId;
    }

    /**
     * 快捷获取当前登录用户 ID (user_id)
     *
     * @return 用户 ID，若未绑定返回 null
     */
    public static String getUserId() {
        TenantContext context = getContext();
        return context != null ? context.getUserId() : null;
    }

    /**
     * 清理当前线程绑定的租户上下文，防止内存泄露
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
