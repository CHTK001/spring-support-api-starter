package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统反馈
 */
@Schema(description = "系统反馈")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_feedback")
public class SysFeedback extends SysBase {
    @TableId(value = "sys_feedback_id", type = IdType.AUTO)
    @Schema(description = "")
    private Integer sysFeedbackId;

    /**
     * 反馈类型
     */
    @TableField(value = "sys_feedback_type")
    @Schema(description = "反馈类型")
    private String sysFeedbackType;

    /**
     * 反馈图片
     */
    @TableField(value = "sys_feedback_images")
    @Schema(description = "反馈图片")
    private String sysFeedbackImages;

    /**
     * 反馈内容
     */
    @TableField(value = "sys_feedback_content")
    @Schema(description = "反馈内容")
    private String sysFeedbackContent;

    /**
     * 反馈类型;0:未处�? 1:已处�?
     */
    @TableField(value = "sys_feedback_status")
    @Schema(description = "反馈类型;0:未处�? 1:已处�?)
    private Integer sysFeedbackStatus;

    /**
     * 处理人ID
     */
    @TableField(value = "sys_feedback_deal_by")
    @Schema(description = "处理人ID")
    private Integer sysFeedbackDealBy;

    /**
     * 处理人名�?
     */
    @TableField(value = "sys_feedback_deal_name")
    @Schema(description = "处理人名�?)
    private String sysFeedbackDealName;

    /**
     * 回复内容
     */
    @TableField(value = "sys_feedback_recover_content")
    @Schema(description = "回复内容")
    private String sysFeedbackRecoverContent;

    /**
     * 回复时间
     */
    @TableField(value = "sys_feedback_recover_time")
    @Schema(description = "回复时间")
    private LocalDateTime sysFeedbackRecoverTime;

    /**
     * 回复的图�?
     */
    @TableField(value = "sys_feedback_recover_images")
    @Schema(description = "回复的图�?)
    private String sysFeedbackRecoverImages;
}