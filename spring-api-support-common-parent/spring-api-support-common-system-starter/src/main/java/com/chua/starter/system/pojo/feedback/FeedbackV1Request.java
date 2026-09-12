package com.chua.starter.system.pojo.feedback;

import com.chua.starter.entity.system.SysFeedback;
import com.chua.starter.mybatis.entity.Query;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 反馈请求
 *
 * @since 2024/8/20
 * @author CH
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "反馈请求")
public class FeedbackV1Request extends Query<SysFeedback> {

    /**
     * 反馈类型
     */
    @Schema(description = "反馈类型")
    /** sysfeedback类型 */
    private String sysFeedbackType;

    /**
    * 反馈内容
     */
    @Schema(description = "反馈内容")
    /** sysfeedback内容 */
    private String sysFeedbackContent;

    /**
    * 反馈图片
     */
    @Schema(description = "反馈图片")
    /** sysfeedbackimages */
    private String sysFeedbackImages;
}
