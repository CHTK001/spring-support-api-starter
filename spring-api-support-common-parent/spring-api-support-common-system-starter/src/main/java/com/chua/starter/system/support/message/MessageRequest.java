package com.chua.starter.system.support.message;

import lombok.Builder;
import lombok.Data;

/**
 * 消息推送请求
 * <p>替代 utils-support-task-starter 中的 MessageRequest</p>
 *
 * @author system
 * @since 2026/09/13
 */
@Data
@Builder
public class MessageRequest {

    /**
     * 接收方（手机号/邮箱等）
    */
    private String to;

    /**
     * 消息内容
    */
    private String content;

    /**
     * 消息模板ID（可选）
    */
    private String templateId;

    /**
     * 模板参数（JSON 字符串，可选）
    */
    private String templateParams;

    /**
     * 签名（可选）
    */
    private String sign;

    /**
     * 扩展字段
    */
    private String ext;
}
