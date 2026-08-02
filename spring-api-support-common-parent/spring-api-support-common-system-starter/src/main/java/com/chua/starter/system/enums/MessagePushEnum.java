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
     * 邮箱欢
     */
    EMAIL(1, "邮箱欢"),

    ;

    private final int code;
    private final String desc;
}
