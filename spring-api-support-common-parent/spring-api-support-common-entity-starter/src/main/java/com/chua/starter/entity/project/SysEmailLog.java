package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 邮件下发日志表
 */
@Schema(description = "邮件下发日志表")
@Data
@TableName(value = "sys_email_log")
public class SysEmailLog {
    @TableId(value = "sys_email_log_id", type = IdType.AUTO)
    @Schema(description = "")
    private Integer sysEmailLogId;

    /**
     * 接收人
     */
    @TableField(value = "sys_email_log_target")
    @Schema(description = "接收人")
    private String sysEmailLogTarget;

    /**
     * 模板ID
     */
    @TableField(value = "sys_email_log_template_id")
    @Schema(description = "模板ID")
    private String sysEmailLogTemplateId;

    /**
     * 参数
     */
    @TableField(value = "sys_email_log_param")
    @Schema(description = "参数")
    private String sysEmailLogParam;

    /**
     * 耗时时间
     */
    @TableField(value = "sys_email_cost")
    @Schema(description = "耗时时间")
    private BigDecimal sysEmailCost;

    /**
     * 错误码
     */
    @TableField(value = "sys_email_code")
    @Schema(description = "错误码")
    private String sysEmailCode;

    /**
     * 错误信息
     */
    @TableField(value = "sys_email_message")
    @Schema(description = "错误信息")
    private String sysEmailMessage;

    /**
     * 短信内容
     */
    @TableField(value = "sys_email_log_content")
    @Schema(description = "邮件内容")
    private String sysEmailLogContent;

    /**
     * 短信厂家
     */
    @TableField(value = "sys_email_app_type")
    @Schema(description = "邮件厂家")
    private String sysEmailAppType;

    /**
     * 短信密钥
     */
    @TableField(value = "sys_email_app_id")
    @Schema(description = "邮件密钥")
    private String sysEmailAppId;

    /**
     * 签名
     */
    @TableField(value = "sys_email_app_sign")
    @Schema(description = "签名")
    private String sysEmailAppSign;

    /**
     * 项目ID
     */
    @TableField(value = "sys_project_id")
    @Schema(description = "项目ID")
    private Integer sysProjectId;

    /**
     * 是否已读;0:未读
     */
    @TableField(value = "sys_email_read")
    @Schema(description = "是否已读;0:未读")
    private Integer sysEmailRead;

    /**
     * 创建人姓名
     */
    @TableField(value = "create_name")
    @Schema(description = "创建人姓名")
    private String createName;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    @Schema(description = "创建人")
    private Integer createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 更新人姓名
     */
    @TableField(value = "update_name")
    @Schema(description = "更新人姓名")
    private String updateName;

    /**
     * 更新人
     */
    @TableField(value = "update_by")
    @Schema(description = "更新人")
    private Integer updateBy;
}
