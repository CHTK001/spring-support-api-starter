package com.chua.starter.system.pojo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 按模板发送消息请求
 *
 * @since 4.0.0.50
 * @author CH
 */
@Data
@Schema(description = "按模板发送消息请求")
public class MessageSendRequest {

    /**
     * 接收人列表
    */
    @Schema(description = "接收人列表")
    private List<String> target;

    /**
     * 模板标识
    */
    @Schema(description = "模板标识")
    private Long sysMessageTemplateId;

    /**
     * 消息主题
    */
    @Schema(description = "消息主题")
    private String subject;

    /**
     * 模板变量(替换模板中的 {key} 占位符)
    */
    @Schema(description = "模板变量(替换模板中的 {key} 占位符)")
    private Map<String, Object> params;
}
