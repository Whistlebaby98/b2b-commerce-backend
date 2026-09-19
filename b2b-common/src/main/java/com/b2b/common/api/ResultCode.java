package com.b2b.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局业务状态码与错误码枚举
 *
 * <p>定义系统级与 B2B 业务领域（多租户、计价、授信、库存、防重提交）专属错误码。</p>
 *
 * @author b2b-commerce-backend
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // ----------------------------------------------------
    // 1. 系统级状态码 (100 ~ 999)
    // ----------------------------------------------------
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数校验失败"),
    UNAUTHORIZED(401, "尚未登录或凭据已失效"),
    FORBIDDEN(403, "无权访问当前企业或资源"),
    NOT_FOUND(404, "请求的资源不存在"),
    CONFLICT(409, "数据冲突或请勿重复提交"),
    INTERNAL_SERVER_ERROR(500, "系统内部繁忙，请稍后重试"),

    // ----------------------------------------------------
    // 2. 组织与多租户领域错误码 (1000 ~ 1999)
    // ----------------------------------------------------
    TENANT_NOT_FOUND(1001, "当前企业组织不存在"),
    TENANT_DISABLED(1002, "当前企业组织已停用"),
    MEMBERSHIP_INVALID(1003, "用户不属于该企业或无采购权限"),
    MISSING_TENANT_HEADER(1004, "缺少 X-Organization-Id 请求头"),

    // ----------------------------------------------------
    // 3. 交易与结算领域错误码 (2000 ~ 2999)
    // ----------------------------------------------------
    CHECKOUT_TOKEN_INVALID(2001, "结算 Token 无效或已核销，请勿重复提交"),
    CART_EMPTY(2002, "购物车为空，无法进入结算"),
    PRICE_CHANGED(2003, "商品价格或阶梯区间发生变化，请重新确认"),
    STOCK_INSUFFICIENT(2004, "部分商品可售库存不足"),
    CREDIT_LIMIT_EXCEEDED(2005, "企业授信可用额度不足"),
    ORDER_NOT_FOUND(2006, "采购订单不存在"),
    ORDER_STATUS_INVALID(2007, "订单状态不允许执行该操作"),

    // ----------------------------------------------------
    // 4. 审批领域错误码 (3000 ~ 3999)
    // ----------------------------------------------------
    APPROVAL_REQUEST_NOT_FOUND(3001, "审批请求不存在"),
    APPROVAL_ACTION_FORBIDDEN(3002, "买方端严禁执行审批或驳回操作");

    private final Integer code;
    private final String message;
}
