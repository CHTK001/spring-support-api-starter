package com.chua.starter.admin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配置类型
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/30
 */
@AllArgsConstructor
@Getter
public enum PushType {

    /**
     * 邮政
     */
    MAIL(0, "邮件"),

    /**
     * 钉钉
     */
    DINGTALK(1, "钉钉"),

    /**
     * 微信
     */
    WECHAT(2, "微信"),

    /**
     * 短讯服务
     */
    SMS(3, "短信"),
    ;

    private final int code;

    private final String message;
}
