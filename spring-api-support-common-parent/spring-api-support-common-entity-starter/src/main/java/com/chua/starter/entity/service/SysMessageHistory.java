package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统消息历史记录表
 *
 * @since 2024/12/04
 * @author CH
 */
@Schema(description = "系统消息历史记录表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_message_history")
public class SysMessageHistory extends SysBase implements Serializable {

static final long serialVersionUID = 1L; // 串行版本uid

    /**
      * 历史记录标识
     */
    @TableId(value = "sys_message_history_id", type = IdType.AUTO)
    @Schema(description = "历史记录ID")
Integer sysMessageHistoryId; // sys消息历史id

    /**
      * 原消息标识
     */
    @TableField(value = "sys_message_history_message_id")
    @Schema(description = "原消息ID")
Integer sysMessageHistoryMessageId; // sys消息历史消息id

    /**
     * 消息标题
     */
    @TableField(value = "sys_message_history_title")
    @Schema(description = "消息标题")
    @Size(max = 255, message = "消息标题最大长度需小于255")
String sysMessageHistoryTitle; // sys消息历史title

    /**
     * 消息内容
     */
    @TableField(value = "sys_message_history_content")
    @Schema(description = "消息内容")
String sysMessageHistoryContent; // sys消息历史内容

    /**
     * 消息类型
     */
    @TableField(value = "sys_message_history_type")
    @Schema(description = "消息类型")
    @Size(max = 50, message = "消息类型最大长度需小于50")
String sysMessageHistoryType; // sys消息历史类型

    /**
     * 消息级别
     */
    @TableField(value = "sys_message_history_level")
    @Schema(description = "消息级别")
    @Size(max = 20, message = "消息级别最大长度需小于20")
String sysMessageHistoryLevel; // sys消息历史级别

    /**
      * 发送者标识
     */
    @TableField(value = "sys_message_history_sender_id")
    @Schema(description = "发送者ID")
Integer sysMessageHistorySenderId; // sys消息历史发送id

    /**
     * 发送者名称
     */
    @TableField(value = "sys_message_history_sender_name")
    @Schema(description = "发送者名称")
    @Size(max = 100, message = "发送者名称最大长度需小于100")
String sysMessageHistorySenderName; // sys消息历史发送名称

    /**
      * 接收者标识
     */
    @TableField(value = "sys_message_history_receiver_id")
    @Schema(description = "接收者ID")
Integer sysMessageHistoryReceiverId; // sys消息历史receiverid

    /**
     * 接收者名称
     */
    @TableField(value = "sys_message_history_receiver_name")
    @Schema(description = "接收者名称")
    @Size(max = 100, message = "接收者名称最大长度需小于100")
String sysMessageHistoryReceiverName; // sys消息历史receiver名称

    /**
     * 发送时间
     */
    @TableField(value = "sys_message_history_send_time")
    @Schema(description = "发送时间")
LocalDateTime sysMessageHistorySendTime; // sys消息历史发送时间

    /**
     * 阅读时间
     */
    @TableField(value = "sys_message_history_read_time")
    @Schema(description = "阅读时间")
LocalDateTime sysMessageHistoryReadTime; // sys消息历史读取时间

    /**
     * 业务类型
     */
    @TableField(value = "sys_message_history_biz_type")
    @Schema(description = "业务类型")
    @Size(max = 50, message = "业务类型最大长度需小于50")
String sysMessageHistoryBizType; // sys消息历史biz类型

    /**
      * 业务标识
     */
    @TableField(value = "sys_message_history_biz_id")
    @Schema(description = "业务ID")
    @Size(max = 100, message = "业务ID最大长度需小于100")
String sysMessageHistoryBizId; // sys消息历史bizid

    /**
     * 跳转链接
     */
    @TableField(value = "sys_message_history_url")
    @Schema(description = "跳转链接")
    @Size(max = 500, message = "跳转链接最大长度需小于500")
String sysMessageHistoryUrl; // sys消息历史url

    /**
     * 扩展数据(JSON格式)
     */
    @TableField(value = "sys_message_history_extra")
    @Schema(description = "扩展数据(JSON格式)")
String sysMessageHistoryExtra; // sys消息历史extra

    /**
      * 从sys消息转换为历史记录
     *
     * @param message 消息对象
     * @return 历史记录对象
     */
    public static SysMessageHistory fromMessage(SysMessage message) {
        var history = new SysMessageHistory();
        history.sysMessageHistoryMessageId = message.sysMessageId;
        history.sysMessageHistoryTitle = message.sysMessageTitle;
        history.sysMessageHistoryContent = message.sysMessageContent;
        history.sysMessageHistoryType = message.sysMessageType;
        history.sysMessageHistoryLevel = message.sysMessageLevel;
        history.sysMessageHistorySenderId = message.sysMessageSenderId;
        history.sysMessageHistorySenderName = message.sysMessageSenderName;
        history.sysMessageHistoryReceiverId = message.sysMessageReceiverId;
        history.sysMessageHistoryReceiverName = message.sysMessageReceiverName;
        history.sysMessageHistorySendTime = message.sysMessageSendTime;
        history.sysMessageHistoryBizType = message.sysMessageBizType;
        history.sysMessageHistoryBizId = message.sysMessageBizId;
        history.sysMessageHistoryUrl = message.sysMessageUrl;
        history.sysMessageHistoryExtra = message.sysMessageExtra;
        return history;
    }
}