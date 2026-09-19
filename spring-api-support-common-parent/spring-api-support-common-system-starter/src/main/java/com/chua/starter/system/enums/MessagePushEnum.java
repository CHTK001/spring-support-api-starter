package com.chua.starter.system.enums;

import lombok.AllArgsConstructor;

/**
 * 通知
 * @since 2024/8/7
 * @author CH
 */
@AllArgsConstructor
public enum MessagePushEnum {

    /**
     * 邮箱欢
     */
    EMAIL(1, "邮箱欢"),

    ;

    /**
     * 代码
    */
    private final int code;
    /**
     * Desc
    */
    private final String desc;
}
