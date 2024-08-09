package com.chua.starter.system.enums;

import lombok.AllArgsConstructor;

/**
 * 通知
 * @author CH
 * @since 2024/8/7
 */
@AllArgsConstructor
public enum MessagePushEnum {

    /**
     * 邮件
     */
    EMAIL(1, "邮件"),

    ;

    private final int code;
    private final String desc;
}
