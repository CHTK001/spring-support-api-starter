package com.chua.starter.entity.device;

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
 * 门禁事件
 *
 * @since 2026/08/03
 * @author CH
 */
@Schema(description = "门禁事件")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_device_card_event")
public class SysDeviceCardEvent extends SysBase {
    /**
     * 门禁事件主键ID
     */
    @TableId(value = "sys_device_card_event_id", type = IdType.AUTO)
    @Schema(description = "")
    /** SYSdevicecard事件ID */
    private Integer sysDeviceCardEventId;

    /**
     * 时间
     */
    @TableField(value = "sys_device_card_event_time")
    @Schema(description = "时间")
    /** SYSdevicecard事件时间 */
    private LocalDateTime sysDeviceCardEventTime;

    /**
     * 序列号
     */
    @TableField(value = "sys_device_serial_number")
    @Schema(description = "序列号")
    /** SYSdeviceserial数字 */
    private String sysDeviceSerialNumber;

    /**
     * 涉事人ID
     */
    @TableField(value = "sys_device_card_event_person_involved")
    @Schema(description = "涉事人ID")
    /** SYSdevicecard事件personinvolved */
    private String sysDeviceCardEventPersonInvolved;

    /**
     * 涉事人姓名
     */
    @TableField(value = "sys_device_card_event_person_involved_name")
    @Schema(description = "涉事人姓名")
    /** SYSdevicecard事件personinvolved名称 */
    private String sysDeviceCardEventPersonInvolvedName;

    /**
     * 主事件类型
     */
    @TableField(value = "sys_device_card_event_major_name")
    @Schema(description = "主事件类型")
    /** SYSdevicecard事件major名称 */
    private String sysDeviceCardEventMajorName;

    /**
     * 次事件类型
     */
    @TableField(value = "sys_device_card_event_minor_name")
    @Schema(description = "次事件类型")
    /** SYSdevicecard事件minor名称 */
    private String sysDeviceCardEventMinorName;

    /**
     * 消息ID
     */
    @TableField(value = "sys_device_card_event_message_id")
    @Schema(description = "消息ID")
    /** SYSdevicecard事件消息ID */
    private String sysDeviceCardEventMessageId;
}
