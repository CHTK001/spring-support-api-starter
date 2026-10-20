package com.chua.starter.system.support.message;

/**
 * 消息推送响应
 * <p>替代 utils-support-task-starter 中的 MessageResponse</p>
 *
 * @param success      是否成功
 * @param errorMessage 错误消息（失败时）
 * @param messageId    服务商返回的原始消息ID
 * @param rawResponse  服务商返回的原始响应（JSON）
 * @author system
 * @since 2026/09/13
 */
public record MessageResponse(
        boolean success,
        String errorMessage,
        String messageId,
        String rawResponse) {

    /**
     * 构造成功响应
     *
     * @param messageId 消息ID，不允许为 null
     * @return 消息响应 对象
     */
    public static MessageResponse ok(String messageId) {
        return new MessageResponse(true, null, messageId, null);
    }

    /**
     * 构造失败响应
     *
     * @param errorMessage error消息，不允许为 null
     * @return 消息响应 对象
     */
    public static MessageResponse fail(String errorMessage) {
        return new MessageResponse(false, errorMessage, null, null);
    }
}
