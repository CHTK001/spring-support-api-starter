package com.chua.starter.admin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/30
 */
@AllArgsConstructor
@Getter
public enum MessageType {

    /**
     * 系统消息
     */
    SYSTEM(1, "系统消息"),
    /**
     * 系统通知
     */
    NOTICE(2, "系统通知"),
    /**
     * 系统公告
     */
    ANNOUNCE(3, "系统公告"),
    /**
     * 系统通知
     */
    OTHER(4, "其它通知");

    private final int code;

    private final String message;
}
