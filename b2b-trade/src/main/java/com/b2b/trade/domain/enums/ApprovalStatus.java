package com.b2b.trade.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单审批流转状态枚举 (ApprovalStatus)
 *
 * <p>对齐前端 ApprovalStatus 契约。</p>
 *
 * @author b2b-commerce-backend
 */
@Getter
@AllArgsConstructor
public enum ApprovalStatus {

    NOT_REQUIRED("not_required", "无需审批"),
    PENDING("pending", "待审批"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已驳回");

    private final String code;
    private final String description;

    public static ApprovalStatus fromCode(String code) {
        for (ApprovalStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}
