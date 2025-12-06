package com.chua.starter.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统消息历史记录表
 * 用于存储已读消息的历史记录
 *
 * @author CH
 * @since 2024/12/04
 */
@ApiModel(description = "系统消息历史记录表")
@Schema(description = "系统消息历史记录表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_message_history")
public class SysMessageHistory extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 历史记录ID
     */
    @TableId(value = "sys_message_history_id", type = IdType.AUTO)
    @ApiModelProperty(value = "历史记录ID")
    @Schema(description = "历史记录ID")
    private Integer sysMessageHistoryId;

    /**
     * 原消息ID
     */
    @TableField(value = "sys_message_history_message_id")
    @ApiModelProperty(value = "原消息ID")
    @Schema(description = "原消息ID")
    private Integer sysMessageHistoryMessageId;

    /**
     * 消息标题
     */
    @TableField(value = "sys_message_history_title")
    @ApiModelProperty(value = "消息标题")
    @Schema(description = "消息标题")
    @Size(max = 255, message = "消息标题最大长度要小于 255")
    private String sysMessageHistoryTitle;

    /**
     * 消息内容
     */
    @TableField(value = "sys_message_history_content")
    @ApiModelProperty(value = "消息内容")
    @Schema(description = "消息内容")
    private String sysMessageHistoryContent;

    /**
     * 消息类型
     */
    @TableField(value = "sys_message_history_type")
    @ApiModelProperty(value = "消息类型")
    @Schema(description = "消息类型")
    @Size(max = 50, message = "消息类型最大长度要小于 50")
    private String sysMessageHistoryType;

    /**
     * 消息级别
     */
    @TableField(value = "sys_message_history_level")
    @ApiModelProperty(value = "消息级别")
    @Schema(description = "消息级别")
    @Size(max = 20, message = "消息级别最大长度要小于 20")
    private String sysMessageHistoryLevel;

    /**
     * 发送者ID
     */
    @TableField(value = "sys_message_history_sender_id")
    @ApiModelProperty(value = "发送者ID")
    @Schema(description = "发送者ID")
    private Integer sysMessageHistorySenderId;

    /**
     * 发送者名称
     */
    @TableField(value = "sys_message_history_sender_name")
    @ApiModelProperty(value = "发送者名称")
    @Schema(description = "发送者名称")
    @Size(max = 100, message = "发送者名称最大长度要小于 100")
    private String sysMessageHistorySenderName;

    /**
     * 接收者ID
     */
    @TableField(value = "sys_message_history_receiver_id")
    @ApiModelProperty(value = "接收者ID")
    @Schema(description = "接收者ID")
    private Integer sysMessageHistoryReceiverId;

    /**
     * 接收者名称
     */
    @TableField(value = "sys_message_history_receiver_name")
    @ApiModelProperty(value = "接收者名称")
    @Schema(description = "接收者名称")
    @Size(max = 100, message = "接收者名称最大长度要小于 100")
    private String sysMessageHistoryReceiverName;

    /**
     * 发送时间
     */
    @TableField(value = "sys_message_history_send_time")
    @ApiModelProperty(value = "发送时间")
    @Schema(description = "发送时间")
    private LocalDateTime sysMessageHistorySendTime;

    /**
     * 阅读时间
     */
    @TableField(value = "sys_message_history_read_time")
    @ApiModelProperty(value = "阅读时间")
    @Schema(description = "阅读时间")
    private LocalDateTime sysMessageHistoryReadTime;

    /**
     * 业务类型
     */
    @TableField(value = "sys_message_history_biz_type")
    @ApiModelProperty(value = "业务类型")
    @Schema(description = "业务类型")
    @Size(max = 50, message = "业务类型最大长度要小于 50")
    private String sysMessageHistoryBizType;

    /**
     * 业务ID
     */
    @TableField(value = "sys_message_history_biz_id")
    @ApiModelProperty(value = "业务ID")
    @Schema(description = "业务ID")
    @Size(max = 100, message = "业务ID最大长度要小于 100")
    private String sysMessageHistoryBizId;

    /**
     * 跳转链接
     */
    @TableField(value = "sys_message_history_url")
    @ApiModelProperty(value = "跳转链接")
    @Schema(description = "跳转链接")
    @Size(max = 500, message = "跳转链接最大长度要小于 500")
    private String sysMessageHistoryUrl;

    /**
     * 扩展数据
     */
    @TableField(value = "sys_message_history_extra")
    @ApiModelProperty(value = "扩展数据")
    @Schema(description = "扩展数据")
    private String sysMessageHistoryExtra;

    /**
     * 从SysMessage转换
     *
     * @param message 消息对象
     * @return 历史记录对象
     */
    public static SysMessageHistory fromMessage(SysMessage message) {
        SysMessageHistory history = new SysMessageHistory();
        history.setSysMessageHistoryMessageId(message.getSysMessageId());
        history.setSysMessageHistoryTitle(message.getSysMessageTitle());
        history.setSysMessageHistoryContent(message.getSysMessageContent());
        history.setSysMessageHistoryType(message.getSysMessageType());
        history.setSysMessageHistoryLevel(message.getSysMessageLevel());
        history.setSysMessageHistorySenderId(message.getSysMessageSenderId());
        history.setSysMessageHistorySenderName(message.getSysMessageSenderName());
        history.setSysMessageHistoryReceiverId(message.getSysMessageReceiverId());
        history.setSysMessageHistoryReceiverName(message.getSysMessageReceiverName());
        history.setSysMessageHistorySendTime(message.getSysMessageSendTime());
        history.setSysMessageHistoryBizType(message.getSysMessageBizType());
        history.setSysMessageHistoryBizId(message.getSysMessageBizId());
        history.setSysMessageHistoryUrl(message.getSysMessageUrl());
        history.setSysMessageHistoryExtra(message.getSysMessageExtra());
        return history;
    }
}
