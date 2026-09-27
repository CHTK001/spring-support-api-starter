package com.chua.starter.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息接收人送达/已读记录表
 *
 * <p>发送邮件/短信时按接收人逐条记录送达结果；站内消息被查看时回写已读。</p>
 *
 * @since 4.0.0.52
 * @author CH
 */
@Schema(description = "消息接收人送达记录表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_message_recipient")
public class SysMessageRecipient extends SysBase implements Serializable {

    /**
     * 接收人类型: USER/EMAIL/PHONE
     */
    public static final String TYPE_EMAIL = "EMAIL";
    /**
     * 接收人类型: 手机号
     */
    public static final String TYPE_PHONE = "PHONE";
    /**
     * 接收人类型: 站内用户
     */
    public static final String TYPE_USER = "USER";

    /**
     * 送达状态: 已送达
     */
    public static final String DELIVERY_SENT = "SENT";
    /**
     * 送达状态: 失败
     */
    public static final String DELIVERY_FAILED = "FAILED";

    /**
     * 已读状态: 已读
     */
    public static final String READ_READ = "READ";
    /**
     * 已读状态: 未读
     */
    public static final String READ_UNREAD = "UNREAD";

    /**
     * 串行版本UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 主键标识
     */
    @TableId(value = "sys_message_recipient_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long sysMessageRecipientId;

    /**
     * 关联站内消息ID(sys_message.sys_message_id)
     */
    @TableField(value = "sys_message_id")
    @Schema(description = "关联站内消息ID")
    private Long sysMessageId;

    /**
     * 接收人类型: USER/EMAIL/PHONE
     */
    @TableField(value = "recipient_type")
    @Schema(description = "接收人类型: USER/EMAIL/PHONE")
    private String recipientType;

    /**
     * 接收人地址(邮箱/手机号/用户名)
     */
    @TableField(value = "recipient_address")
    @Schema(description = "接收人地址(邮箱/手机号/用户名)")
    private String recipientAddress;

    /**
     * 送达状态: SENT/FAILED
     */
    @TableField(value = "delivery_status")
    @Schema(description = "送达状态: SENT/FAILED")
    private String deliveryStatus;

    /**
     * 已读状态: READ/UNREAD
     */
    @TableField(value = "read_status")
    @Schema(description = "已读状态: READ/UNREAD")
    private String readStatus;

    /**
     * 阅读时间
     */
    @TableField(value = "read_time")
    @Schema(description = "阅读时间")
    private LocalDateTime readTime;
}
