package com.chua.starter.system.pojo.feedback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* 反馈问题请求
*
* @since 2024/8/20
* @author CH
 */
@Data
@Schema(description = "反馈问题请求")
public class FeedbackIssueV1Request {

    /**
    * 反馈标识
     */
    @Schema(description = "反馈ID")
    /** sysfeedbackid */
    private Integer sysFeedbackId;

    /**
    * 回复内容
     */
    @Schema(description = "回复内容")
    /** sysfeedbackrecover内容 */
    private String sysFeedbackRecoverContent;

    /**
    * 回复图片
     */
    @Schema(description = "回复图片")
    /** sysfeedbackrecoverimages */
    private String sysFeedbackRecoverImages;
}