package com.chua.starter.collect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Webhook 通知的请求体。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "数据采集-通知回调出参")
public class CollectNotifyPayloadVO {

    /**
     * 通知标题。
     */
    @Schema(description = "通知标题")
    private String subject;

    /**
     * 通知正文。
     */
    @Schema(description = "通知正文")
    private String content;

    /**
     * 正文格式。
     */
    @Schema(description = "正文格式")
    private String contentType;
}
