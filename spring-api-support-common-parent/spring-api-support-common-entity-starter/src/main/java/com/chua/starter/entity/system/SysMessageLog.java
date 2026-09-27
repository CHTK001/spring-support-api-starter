package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息推送日志表
 *
 * <p>一次发送的每个接收人落一行：成功失败都记，便于按渠道/接收人回溯。
 * 同一次请求的多个接收人共享 {@code sysMessageBatchId}。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Schema(description = "消息推送日志表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_message_log")
public class SysMessageLog extends SysBase implements Serializable {
    /**
     * 串行版本UID
    */
    private static final long serialVersionUID = 1L;

    /**
     * 日志标识
     */
    @TableId(value = "sys_message_log_id", type = IdType.AUTO)
    @Schema(description = "日志ID")
    /**
     * sysmessagelogid
    */
    private Integer sysMessageLogId;

    /**
     * 接收人
     */
    @TableField(value = "sys_message_log_target")
    @Schema(description = "接收人")
    /**
     * sysmessagelog目标
    */
    private String sysMessageLogTarget;

    /**
     * 模板标识
     */
    @TableField(value = "sys_message_template_id")
    @Schema(description = "模板标识")
    /**
     * sysmessagetemplateid
    */
    private Long sysMessageTemplateId;

    /**
     * 提供商标识
     */
    @TableField(value = "sys_message_provider_id")
    @Schema(description = "提供商标识")
    /**
     * sysmessageproviderid
    */
    private Integer sysMessageProviderId;

    /**
     * 渠道标识
     */
    @TableField(value = "sys_message_log_channel")
    @Schema(description = "渠道标识")
    /**
     * sysmessagelog通道
    */
    private String sysMessageLogChannel;

    /**
     * 发送内容
     */
    @TableField(value = "sys_message_log_content")
    @Schema(description = "发送内容")
    /**
     * sysmessagelog内容
    */
    private String sysMessageLogContent;

    /**
     * 模板参数(JSON)
     */
    @TableField(value = "sys_message_log_param")
    @Schema(description = "模板参数(JSON)")
    /**
     * sysmessagelog参数
    */
    private String sysMessageLogParam;

    /**
     * 发送耗时(毫秒)
     */
    @TableField(value = "sys_message_cost")
    @Schema(description = "发送耗时(毫秒)")
    /**
     * sys消息耗时
    */
    private Long sysMessageCost;

    /**
     * 返回码
     */
    @TableField(value = "sys_message_code")
    @Schema(description = "返回码")
    /**
     * sys消息代码
    */
    private String sysMessageCode;

    /**
     * 返回消息
     */
    @TableField(value = "sys_message_message")
    @Schema(description = "返回消息")
    /**
     * sys消息消息
    */
    private String sysMessageMessage;

    /**
     * 消息主题
     */
    @TableField(value = "sys_message_log_subject")
    @Schema(description = "消息主题")
    /**
     * sysmessagelog主题
    */
    private String sysMessageLogSubject;

    /**
     * 服务商消息标识
     */
    @TableField(value = "sys_message_log_message_id")
    @Schema(description = "服务商消息标识")
    /**
     * sysmessagelog消息标识
    */
    private String sysMessageLogMessageId;

    /**
     * 批次号
     */
    @TableField(value = "sys_message_batch_id")
    @Schema(description = "批次号")
    /**
     * sys消息批次标识
    */
    private String sysMessageBatchId;

    /**
     * 阅读追踪标识
     */
    @TableField(value = "sys_message_track_token")
    @Schema(description = "阅读追踪标识")
    /**
     * sys消息追踪口令
    */
    private String sysMessageTrackToken;

    /**
     * 已读状态: 0-未读 1-已读
     */
    @TableField(value = "sys_message_read_status")
    @Schema(description = "已读状态: 0-未读 1-已读")
    /**
     * sys消息读取状态
    */
    private Integer sysMessageReadStatus;

    /**
     * 已读时间
     */
    @TableField(value = "sys_message_read_time")
    @Schema(description = "已读时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * sys消息读取时间
    */
    private LocalDateTime sysMessageReadTime;
}
