package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 门禁事件
 */
@ApiModel(description = "门禁事件")
@Schema(description = "门禁事件")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_device_card_event")
public class SysDeviceCardEvent extends SysBase {
    @TableId(value = "sys_device_card_event_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysDeviceCardEventId;

    /**
     * 时间
     */
    @TableField(value = "sys_device_card_event_time")
    @ApiModelProperty(value = "时间")
    @Schema(description = "时间")
    private LocalDateTime sysDeviceCardEventTime;

    /**
     * 序列号
     */
    @TableField(value = "sys_device_serial_number")
    @ApiModelProperty(value = "序列号")
    @Schema(description = "序列号")
    private String sysDeviceSerialNumber;

    /**
     * 涉事人ID
     */
    @TableField(value = "sys_device_card_event_person_involved")
    @ApiModelProperty(value = "涉事人ID")
    @Schema(description = "涉事人ID")
    private String sysDeviceCardEventPersonInvolved;

    /**
     * 涉事人姓名
     */
    @TableField(value = "sys_device_card_event_person_involved_name")
    @ApiModelProperty(value = "涉事人姓名")
    @Schema(description = "涉事人姓名")
    private String sysDeviceCardEventPersonInvolvedName;

    /**
     * 主事件类型
     */
    @TableField(value = "sys_device_card_event_major_name")
    @ApiModelProperty(value = "主事件类型")
    @Schema(description = "主事件类型")
    private String sysDeviceCardEventMajorName;

    /**
     * 次事件类型
     */
    @TableField(value = "sys_device_card_event_minor_name")
    @ApiModelProperty(value = "次事件类型")
    @Schema(description = "次事件类型")
    private String sysDeviceCardEventMinorName;

    /**
     * 消息ID
     */
    @TableField(value = "sys_device_card_event_message_id")
    @ApiModelProperty(value = "消息ID")
    @Schema(description = "消息ID")
    private String sysDeviceCardEventMessageId;
}