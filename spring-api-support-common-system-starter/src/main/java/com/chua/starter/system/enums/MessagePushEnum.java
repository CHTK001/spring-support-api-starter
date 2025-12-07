package com.chua.starter.system.enums;

import lombok.AllArgsConstructor;

/**
 * 閫氱煡
 * @author CH
 * @since 2024/8/7
 */
@AllArgsConstructor
public enum MessagePushEnum {

    /**
     * 閭欢
     */
    EMAIL(1, "閭欢"),

    ;

    private final int code;
    private final String desc;
}
