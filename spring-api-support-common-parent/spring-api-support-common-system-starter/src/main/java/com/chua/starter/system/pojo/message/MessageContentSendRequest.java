package com.chua.starter.system.pojo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 按内容发送消息请求
 *
 * <p>不经过模板库，直接以给定内容向指定渠道发送，用于手工补发与测试连通性。
 * 邮件/短信已收紧为必须走模板，自由内容发送已停用。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Data
@Schema(description = "按内容发送消息请求")
public class MessageContentSendRequest {

    /**
     * 消息渠道: EMAIL/SMS
     */
    @Schema(description = "消息渠道: EMAIL/SMS")
    private String channel;

    /**
     * 接收人列表
     */
    @Schema(description = "接收人列表")
    private List<String> target;

    /**
     * 消息主题
     */
    @Schema(description = "消息主题")
    private String subject;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String content;

    /**
     * 消息变量(替换内容中的 {key} 占位符)
     */
    @Schema(description = "消息变量(替换内容中的 {key} 占位符)")
    private Map<String, Object> params;
}
