package com.chua.starter.system.pojo.feedback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 反馈闂请求
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "反馈闂请求")
public class FeedbackIssueV1Request {

    /**
     * 反馈ID
     */
    @Schema(description = "反馈ID")
    private Integer sysFeedbackId;

    /**
     * 鍥炲内容
     */
    @Schema(description = "鍥炲内容")
    private String sysFeedbackRecoverContent;

    /**
     * 鍥炲图片
     */
    @Schema(description = "鍥炲图片")
    private String sysFeedbackRecoverImages;
}