package com.chua.starter.entity.system;

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
 * 系统反馈表
 *
 * @since 2024/8/7
 * @author CH
 */
@Schema(description = "系统反馈表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_feedback")
public class SysFeedback extends SysBase {

    /**
     * 反馈ID
     */
    @TableId(value = "sys_feedback_id", type = IdType.AUTO)
    @Schema(description = "反馈ID")
    /** SYSfeedbackID */
    private Integer sysFeedbackId;

    /**
     * 反馈类型
     */
    @TableField(value = "sys_feedback_type")
    @Schema(description = "反馈类型")
    /** SYSfeedback类型 */
    private String sysFeedbackType;

    /**
     * 反馈图片(多个逗号分隔)
     */
    @TableField(value = "sys_feedback_images")
    @Schema(description = "反馈图片(多个逗号分隔)")
    /** SYSfeedbackimages */
    private String sysFeedbackImages;

    /**
     * 反馈内容
     */
    @TableField(value = "sys_feedback_content")
    @Schema(description = "反馈内容")
    /** SYSfeedback内容 */
    private String sysFeedbackContent;

    /**
     * 反馈状态: 0:未处理 1:已处理
     */
    @TableField(value = "sys_feedback_status")
    @Schema(description = "反馈状态: 0:未处理 1:已处理")
    /** SYSfeedback状态 */
    private Integer sysFeedbackStatus;

    /**
     * 处理人ID
     */
    @TableField(value = "sys_feedback_deal_by")
    @Schema(description = "处理人ID")
    /** SYSfeedbackdealBY */
    private Integer sysFeedbackDealBy;

    /**
     * 处理人名称
     */
    @TableField(value = "sys_feedback_deal_name")
    @Schema(description = "处理人名称")
    /** SYSfeedbackdeal名称 */
    private String sysFeedbackDealName;

    /**
     * 回复内容
     */
    @TableField(value = "sys_feedback_recover_content")
    @Schema(description = "回复内容")
    /** SYSfeedbackrecover内容 */
    private String sysFeedbackRecoverContent;

    /**
     * 回复时间
     */
    @TableField(value = "sys_feedback_recover_time")
    @Schema(description = "回复时间")
    /** SYSfeedbackrecover时间 */
    private LocalDateTime sysFeedbackRecoverTime;

    /**
     * 回复图片(多个逗号分隔)
     */
    @TableField(value = "sys_feedback_recover_images")
    @Schema(description = "回复图片(多个逗号分隔)")
    /** SYSfeedbackrecoverimages */
    private String sysFeedbackRecoverImages;

    /**
     * 客户端类型(UserAgent)
     */
    @TableField(value = "sys_feedback_user_agent")
    @Schema(description = "客户端类型(UserAgent)")
    /** SYSfeedback用户agent */
    private String sysFeedbackUserAgent;

    /**
     * 平台类型(预留，多平台支持)
     */
    @TableField(value = "sys_feedback_platform")
    @Schema(description = "平台类型(预留，多平台支持)")
    /** SYSfeedbackplatform */
    private String sysFeedbackPlatform;
}
