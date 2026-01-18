package com.chua.starter.entity.project;

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
 */
@Schema(description = "门禁事件")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_device_card_event")
public class SysDeviceCardEvent extends SysBase {
    @TableId(value = "sys_device_card_event_id", type = IdType.AUTO)
    @Schema(description = "")
    private Integer sysDeviceCardEventId;

    /**
     * 时间
     */
    @TableField(value = "sys_device_card_event_time")
    @Schema(description = "时间")
    private LocalDateTime sysDeviceCardEventTime;

    /**
     * 序列号
     */
    @TableField(value = "sys_device_serial_number")
    @Schema(description = "序列号")
    private String sysDeviceSerialNumber;

    /**
     * 涉事人ID
     */
    @TableField(value = "sys_device_card_event_person_involved")
    @Schema(description = "涉事人ID")
    private String sysDeviceCardEventPersonInvolved;

    /**
     * 涉事人姓名
     */
    @TableField(value = "sys_device_card_event_person_involved_name")
    @Schema(description = "涉事人姓名")
    private String sysDeviceCardEventPersonInvolvedName;

    /**
     * 主事件类型
     */
    @TableField(value = "sys_device_card_event_major_name")
    @Schema(description = "主事件类型")
    private String sysDeviceCardEventMajorName;

    /**
     * 次事件类型
     */
    @TableField(value = "sys_device_card_event_minor_name")
    @Schema(description = "次事件类型")
    private String sysDeviceCardEventMinorName;

    /**
     * 消息ID
     */
    @TableField(value = "sys_device_card_event_message_id")
    @Schema(description = "消息ID")
    private String sysDeviceCardEventMessageId;
}
