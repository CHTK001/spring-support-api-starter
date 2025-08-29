package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 邮件历史记录
 *
 * 记录邮件的详细信息，包括邮件内容、状态、标签等。
 *
 * @author CH
 * @since 2025-01-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("邮件历史记录")
@Schema(name = "SystemDataEmailHistory")
@TableName("system_data_email_history")
public class SystemDataEmailHistory extends SysBase {

    @TableId(value = "system_data_email_history_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    @NotNull(message = "ID不能为空", groups = { })
    private Integer systemDataEmailHistoryId;

    /** 关联的数据源配置ID */
    @Schema(description = "关联的数据源配置ID")
    @TableField("system_data_email_history_setting_id")
    @NotNull(message = "数据源配置ID不能为空")
    private Integer systemDataEmailHistorySettingId;

    /** 邮件唯一标识符 */
    @Schema(description = "邮件唯一标识符")
    @TableField("system_data_email_history_message_id")
    @Size(max = 255)
    private String systemDataEmailHistoryMessageId;

    /** 邮件主题 */
    @Schema(description = "邮件主题")
    @TableField("system_data_email_history_subject")
    @Size(max = 500)
    private String systemDataEmailHistorySubject;

    /** 发件人 */
    @Schema(description = "发件人")
    @TableField("system_data_email_history_from")
    @Size(max = 255)
    private String systemDataEmailHistoryFrom;

    /** 收件人 */
    @Schema(description = "收件人")
    @TableField("system_data_email_history_to")
    @Size(max = 1000)
    private String systemDataEmailHistoryTo;

    /** 抄送人 */
    @Schema(description = "抄送人")
    @TableField("system_data_email_history_cc")
    @Size(max = 1000)
    private String systemDataEmailHistoryCc;

    /** 密送人 */
    @Schema(description = "密送人")
    @TableField("system_data_email_history_bcc")
    @Size(max = 1000)
    private String systemDataEmailHistoryBcc;

    /** 邮件内容（纯文本） */
    @Schema(description = "邮件内容（纯文本）")
    @TableField("system_data_email_history_content")
    private String systemDataEmailHistoryContent;
    /** 邮件内容类型 */
    @Schema(description = "邮件内容类型")
    @TableField("system_data_email_history_content_type")
    private String systemDataEmailHistoryContentType;

    /** 邮件内容（HTML） */
    @Schema(description = "邮件内容（HTML）")
    @TableField("system_data_email_history_html_content")
    private String systemDataEmailHistoryHtmlContent;

    /** 邮件发送时间 */
    @Schema(description = "邮件发送时间")
    @TableField("system_data_email_history_sent_date")
    private LocalDateTime systemDataEmailHistorySentDate;

    /** 邮件接收时间 */
    @Schema(description = "邮件接收时间")
    @TableField("system_data_email_history_received_date")
    private LocalDateTime systemDataEmailHistoryReceivedDate;

    /** 邮件大小（字节） */
    @Schema(description = "邮件大小（字节）")
    @TableField("system_data_email_history_size")
    private Long systemDataEmailHistorySize;

    /** 是否已读 */
    @Schema(description = "是否已读")
    @TableField("system_data_email_history_is_read")
    private Boolean systemDataEmailHistoryIsRead;

    /** 是否收藏 */
    @Schema(description = "是否收藏")
    @TableField("system_data_email_history_is_starred")
    private Boolean systemDataEmailHistoryIsStarred;

    /** 是否重要 */
    @Schema(description = "是否重要")
    @TableField("system_data_email_history_is_important")
    private Boolean systemDataEmailHistoryIsImportant;

    /** 是否已删除 */
    @Schema(description = "是否已删除")
    @TableField("system_data_email_history_is_deleted")
    private Boolean systemDataEmailHistoryIsDeleted;

    /** 邮件标签（多个标签用逗号分隔） */
    @Schema(description = "邮件标签（多个标签用逗号分隔）")
    @TableField("system_data_email_history_labels")
    @Size(max = 500)
    private String systemDataEmailHistoryLabels;

    /** 邮件文件夹 */
    @Schema(description = "邮件文件夹")
    @TableField("system_data_email_history_folder")
    @Size(max = 255)
    private String systemDataEmailHistoryFolder;

    /** 邮件优先级 */
    @Schema(description = "邮件优先级")
    @TableField("system_data_email_history_priority")
    @Size(max = 50)
    private String systemDataEmailHistoryPriority;

    /** 附件数量 */
    @Schema(description = "附件数量")
    @TableField("system_data_email_history_attachment_count")
    private Integer systemDataEmailHistoryAttachmentCount;

    /** 附件信息（JSON格式） */
    @Schema(description = "附件信息（JSON格式）")
    @TableField("system_data_email_history_attachments")
    private String systemDataEmailHistoryAttachments;

    /** 邮件头信息（JSON格式） */
    @Schema(description = "邮件头信息（JSON格式）")
    @TableField("system_data_email_history_headers")
    private String systemDataEmailHistoryHeaders;

    /** 邮件类型 */
    @Schema(description = "邮件类型")
    @TableField("system_data_email_history_type")
    @Size(max = 50)
    private String systemDataEmailHistoryType;

    /** 邮件状态 */
    @Schema(description = "邮件状态")
    @TableField("system_data_email_history_status")
    @Size(max = 50)
    private String systemDataEmailHistoryStatus;

    /** 备注 */
    @Schema(description = "备注")
    @TableField("system_data_email_history_remark")
    @Size(max = 1000)
    private String systemDataEmailHistoryRemark;

    /** 同步状态 */
    @Schema(description = "同步状态：0-未同步，1-已同步，2-同步失败")
    @TableField("system_data_email_history_sync_status")
    private Integer systemDataEmailHistorySyncStatus;

    /** 同步时间 */
    @Schema(description = "同步时间")
    @TableField("system_data_email_history_sync_time")
    private LocalDateTime systemDataEmailHistorySyncTime;

    /** 错误信息 */
    @Schema(description = "错误信息")
    @TableField("system_data_email_history_error_message")
    @Size(max = 1000)
    private String systemDataEmailHistoryErrorMessage;
}