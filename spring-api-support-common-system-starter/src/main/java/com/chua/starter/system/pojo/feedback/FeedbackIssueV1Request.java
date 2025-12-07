package com.chua.starter.system.pojo.feedback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 鍙嶉闂璇锋眰
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "鍙嶉闂璇锋眰")
public class FeedbackIssueV1Request {

    /**
     * 鍙嶉ID
     */
    @Schema(description = "鍙嶉ID")
    private Integer sysFeedbackId;

    /**
     * 鍥炲鍐呭
     */
    @Schema(description = "鍥炲鍐呭")
    private String sysFeedbackRecoverContent;

    /**
     * 鍥炲鍥剧墖
     */
    @Schema(description = "鍥炲鍥剧墖")
    private String sysFeedbackRecoverImages;
}