package com.chua.starter.monitor.enums;

/**
 * 告警通知方式枚举
 *
 * @author CH
 * @since 2024/12/26
 */
public enum AlertNotificationMethod {
    EMAIL("EMAIL", "邮件"),
    SMS("SMS", "短信"),
    WEBHOOK("WEBHOOK", "Webhook"),
    DINGTALK("DINGTALK", "钉钉"),
    WECHAT("WECHAT", "微信");

    private final String code;
    private final String desc;

    AlertNotificationMethod(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AlertNotificationMethod fromCode(String code) {
        for (AlertNotificationMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        return EMAIL;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
