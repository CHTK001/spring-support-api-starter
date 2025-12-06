package com.chua.starter.system.pojo.feedback;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysFeedback;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 反馈请求
 *
 * @author CH
 * @since 2025/4/9 10:05
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "反馈请求")
public class FeedbackIssueV1Request extends Query<SysFeedback> {

    /**
     * 反馈编码
     */
    @Schema(description = "问题编码")
    private Integer sysFeedbackId;

    /**
     * 回复内容
     */
    @TableField(value = "sys_feedback_recover_content")
    @Schema(description = "回复内容")
    @NotBlank(message = "回复内容不能为空", groups = {UpdateGroup.class})
    private String sysFeedbackRecoverContent;


    /**
     * 回复的图片
     */
    @TableField(value = "sys_feedback_recover_images")
    @Schema(description = "回复的图片")
    private String sysFeedbackRecoverImages;
}
