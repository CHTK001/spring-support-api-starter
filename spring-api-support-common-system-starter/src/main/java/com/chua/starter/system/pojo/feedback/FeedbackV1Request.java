package com.chua.starter.system.pojo.feedback;

import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.entity.SysFeedback;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 反馈请求
 *
 * @author CH
 * @since 2025/4/9 10:05
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "反馈请求")
public class FeedbackV1Request extends Query<SysFeedback> {


    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /**
     * 反馈类型
     */
    @Schema(description = "反馈类型")
    private String sysFeedbackType;

    /**
     * 反馈状态
     */
    @Schema(description = "反馈类型;0:未处理, 1:已处理")
    private Integer sysFeedbackStatus;
}
