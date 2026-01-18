package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统消息表
 *
 * @author CH
 * @since 2024/12/04
 */
@Schema(description = "系统消息表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_message")
public class SysMessage extends SysBase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(value = "sys_message_id", type = IdType.AUTO)
    @Schema(description = "消息ID")
    Integer sysMessageId;

    /**
     * 消息标题
     */
    @TableField(value = "sys_message_title")
    @Schema(description = "消息标题")
    @NotBlank(message = "消息标题不能为空")
    @Size(max = 255, message = "消息标题最大长度需小于255")
    String sysMessageTitle;

    /**
     * 消息内容
     */
    @TableField(value = "sys_message_content")
    @Schema(description = "消息内容")
    String sysMessageContent;

    /**
     * 消息类型: system-系统通知 notice-公告 message-私信 warning-警告 info-信息
     */
    @TableField(value = "sys_message_type")
    @Schema(description = "消息类型: system-系统通知 notice-公告 message-私信 warning-警告 info-信息")
    @Size(max = 50, message = "消息类型最大长度需小于50")
    String sysMessageType;

    /**
     * 消息级别: low-低 normal-普通 high-高 urgent-紧急
     */
    @TableField(value = "sys_message_level")
    @Schema(description = "消息级别: low-低 normal-普通 high-高 urgent-紧急")
    @Size(max = 20, message = "消息级别最大长度需小于20")
    String sysMessageLevel;

    /**
     * 发送者ID(0表示系统发送)
     */
    @TableField(value = "sys_message_sender_id")
    @Schema(description = "发送者ID(0表示系统发送)")
    Integer sysMessageSenderId;

    /**
     * 发送者名称
     */
    @TableField(value = "sys_message_sender_name")
    @Schema(description = "发送者名称")
    @Size(max = 100, message = "发送者名称最大长度需小于100")
    String sysMessageSenderName;

    /**
     * 接收者ID(0表示广播消息)
     */
    @TableField(value = "sys_message_receiver_id")
    @Schema(description = "接收者ID(0表示广播消息)")
    Integer sysMessageReceiverId;

    /**
     * 接收者名称
     */
    @TableField(value = "sys_message_receiver_name")
    @Schema(description = "接收者名称")
    @Size(max = 100, message = "接收者名称最大长度需小于100")
    String sysMessageReceiverName;

    /**
     * 是否已读: 0-未读 1-已读
     */
    @TableField(value = "sys_message_read")
    @Schema(description = "是否已读: 0-未读 1-已读")
    private Integer sysMessageRead;

    /**
     * 阅读时间
     */
    @TableField(value = "sys_message_read_time")
    @Schema(description = "阅读时间")
    private LocalDateTime sysMessageReadTime;

    /**
     * 发送时间
     */
    @TableField(value = "sys_message_send_time")
    @Schema(description = "发送时间")
    LocalDateTime sysMessageSendTime;

    /**
     * 过期时间(为空表示永不过期)
     */
    @TableField(value = "sys_message_expire_time")
    @Schema(description = "过期时间(为空表示永不过期)")
    private LocalDateTime sysMessageExpireTime;

    /**
     * 业务类型
     */
    @TableField(value = "sys_message_biz_type")
    @Schema(description = "业务类型")
    @Size(max = 50, message = "业务类型最大长度需小于50")
    String sysMessageBizType;

    /**
     * 业务ID
     */
    @TableField(value = "sys_message_biz_id")
    @Schema(description = "业务ID")
    @Size(max = 100, message = "业务ID最大长度需小于100")
    String sysMessageBizId;

    /**
     * 跳转链接
     */
    @TableField(value = "sys_message_url")
    @Schema(description = "跳转链接")
    @Size(max = 500, message = "跳转链接最大长度需小于500")
    String sysMessageUrl;

    /**
     * 扩展数据(JSON格式)
     */
    @TableField(value = "sys_message_extra")
    @Schema(description = "扩展数据(JSON格式)")
    String sysMessageExtra;

    /**
     * 状态: 0-草稿 1-已发送 2-已撤回
     */
    @TableField(value = "sys_message_status")
    @Schema(description = "状态: 0-草稿 1-已发送 2-已撤回")
    private Integer sysMessageStatus;

    /**
     * 消息类型常量
     */
    public static class Type {
        public static final String SYSTEM = "system";
        public static final String NOTICE = "notice";
        public static final String MESSAGE = "message";
        public static final String WARNING = "warning";
        public static final String INFO = "info";
    }

    /**
     * 消息级别常量
     */
    public static class Level {
        public static final String LOW = "low";
        public static final String NORMAL = "normal";
        public static final String HIGH = "high";
        public static final String URGENT = "urgent";
    }

    /**
     * 状态常量
     */
    public static class Status {
        public static final int DRAFT = 0;
        public static final int SENT = 1;
        public static final int RECALLED = 2;
    }
}
