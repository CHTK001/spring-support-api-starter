package com.chua.starter.system.pojo.feedback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 反馈请求
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "反馈请求")
public class FeedbackV1Request {

    /**
     * 反馈类型
     */
    @Schema(description = "反馈类型")
    private String sysFeedbackType;

    /**
     * 反馈内容
     */
    @Schema(description = "反馈内容")
    private String sysFeedbackContent;

    /**
     * 反馈图片
     */
    @Schema(description = "反馈图片")
    private String sysFeedbackImages;
}