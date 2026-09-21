package com.chua.starter.system.pojo.feedback;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 反馈问题请求
 *
 * @param sysFeedbackId sysfeedbackid
 * @param sysFeedbackRecoverContent sysfeedbackrecover内容
 * @param sysFeedbackRecoverImages sysfeedbackrecoverimages
 *
 * @since 2024/8/20
 * @author CH
 */
@Schema(description = "反馈问题请求")
public record FeedbackIssueV1Request(
        @Schema(description = "反馈ID") Integer sysFeedbackId,
        @Schema(description = "回复内容") String sysFeedbackRecoverContent,
        @Schema(description = "回复图片") String sysFeedbackRecoverImages
) {
}