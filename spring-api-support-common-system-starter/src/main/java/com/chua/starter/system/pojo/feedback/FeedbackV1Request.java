package com.chua.starter.system.pojo.feedback;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 鍙嶉璇锋眰
 *
 * @author CH
 * @since 2024/8/20
 */
@Data
@Schema(description = "鍙嶉璇锋眰")
public class FeedbackV1Request {

    /**
     * 鍙嶉绫诲瀷
     */
    @Schema(description = "鍙嶉绫诲瀷")
    private String sysFeedbackType;

    /**
     * 鍙嶉鍐呭
     */
    @Schema(description = "鍙嶉鍐呭")
    private String sysFeedbackContent;

    /**
     * 鍙嶉鍥剧墖
     */
    @Schema(description = "鍙嶉鍥剧墖")
    private String sysFeedbackImages;
}