package com.chua.starter.system.support.message;

import lombok.Builder;
import lombok.Data;

/**
 * 消息推送响应
 * <p>替代 utils-support-task-starter 中的 MessageResponse</p>
 *
 * @author system
 * @since 2026/09/13
 */
@Data
@Builder
public class MessageResponse {

    /** 是否成功 */
    private boolean success;

    /** 错误消息（失败时） */
    private String errorMessage;

    /** 服务商返回的原始消息ID */
    private String messageId;

    /** 服务商返回的原始响应（JSON） */
    private String rawResponse;

    /**
     * 构造成功响应
     * @param messageId 消息ID，不允许为 null
     * @return 消息响应 对象
     */
    public static MessageResponse ok(String messageId) {
        return MessageResponse.builder()
                .success(true)
                .messageId(messageId)
                .build();
    }

    /**
     * 构造失败响应
     * @param errorMessage error消息，不允许为 null
     * @return 消息响应 对象
     */
    public static MessageResponse fail(String errorMessage) {
        return MessageResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
