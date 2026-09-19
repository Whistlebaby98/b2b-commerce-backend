package com.b2b.trade.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举 (PaymentMethod)
 *
 * <p>对齐前端 PaymentMethod 契约。</p>
 *
 * @author b2b-commerce-backend
 */
@Getter
@AllArgsConstructor
public enum PaymentMethod {

    CREDIT_ACCOUNT("credit_account", "企业授信账期"),
    BANK_TRANSFER("bank_transfer", "对公银行转账");

    private final String code;
    private final String description;

    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod method : values()) {
            if (method.getCode().equalsIgnoreCase(code)) {
                return method;
            }
        }
        return null;
    }
}
