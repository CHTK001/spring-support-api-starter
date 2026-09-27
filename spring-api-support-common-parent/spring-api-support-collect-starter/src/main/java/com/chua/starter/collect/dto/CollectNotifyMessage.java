package com.chua.starter.collect.dto;

import lombok.Data;

/**
 * 一次通知发送的入参。
 *
 * <p>字段按渠道各取所需：邮件用 {@code target}/{@code subject}/{@code content}，
 * 回调地址同样从 {@code target} 取。新增渠道不需要改动发送方。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
public class CollectNotifyMessage {

    /**
     * 接收目标：邮箱（逗号分隔）或 HTTP 回调地址。
     */
    private String target;

    /**
     * 标题。
     */
    private String subject;

    /**
     * 正文。
     */
    private String content;

    /**
     * 正文格式 - text/html/json。
     */
    private String contentType;

    /**
     * 发送超时毫秒。
     */
    private long timeoutMs = 10000L;
}
