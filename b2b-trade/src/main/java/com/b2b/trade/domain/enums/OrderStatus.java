package com.b2b.trade.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 采购订单状态枚举 (OrderStatus)
 *
 * <p>对齐前端 OrderStatus 契约，支持审批流转与履约状态跟踪。</p>
 *
 * @author b2b-commerce-backend
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {

    DRAFT("draft", "草稿"),
    PENDING_APPROVAL("pending_approval", "待审批"),
    APPROVED("approved", "已审批"),
    AWAITING_PAYMENT("awaiting_payment", "待支付"),
    PROCESSING("processing", "处理中/备货中"),
    SHIPPED("shipped", "已发货"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消"),
    REJECTED("rejected", "已驳回");

    private final String code;
    private final String description;

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}
