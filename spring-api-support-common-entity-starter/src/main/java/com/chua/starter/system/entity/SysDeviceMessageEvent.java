package com.chua.starter.system.entity;

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
 * 事件
 */
@ApiModel(description = "事件")
@Schema(description = "事件")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_device_message_event")
public class SysDeviceMessageEvent extends SysBase {
    @TableId(value = "sys_device_message_event_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysDeviceMessageEventId;

    /**
     * 事件类型
     */
    @TableField(value = "sys_device_message_event_type")
    @ApiModelProperty(value = "事件类型")
    @Schema(description = "事件类型")
    private String sysDeviceMessageEventType;

    /**
     * 事件ID
     */
    @TableField(value = "sys_device_message_event_event_id")
    @ApiModelProperty(value = "事件ID")
    @Schema(description = "事件ID")
    private String sysDeviceMessageEventEventId;

    /**
     * 事件时间
     */
    @TableField(value = "sys_device_message_event_data_time")
    @ApiModelProperty(value = "事件时间")
    @Schema(description = "事件时间")
    private LocalDateTime sysDeviceMessageEventDataTime;

    /**
     * 人员ID
     */
    @TableField(value = "sys_device_message_event_employee_no")
    @ApiModelProperty(value = "人员ID")
    @Schema(description = "人员ID")
    private String sysDeviceMessageEventEmployeeNo;

    /**
     * 卡号
     */
    @TableField(value = "sys_device_message_event_card_no")
    @ApiModelProperty(value = "卡号")
    @Schema(description = "卡号")
    private String sysDeviceMessageEventCardNo;

    /**
     * 设备序列号
     */
    @TableField(value = "sys_device_serial_number")
    @ApiModelProperty(value = "设备序列号")
    @Schema(description = "设备序列号")
    private String sysDeviceSerialNumber;

    /**
     * 设备名称
     */
    @TableField(value = "sys_device_name")
    @ApiModelProperty(value = "设备名称")
    @Schema(description = "设备名称")
    private String sysDeviceName;

    /**
     * 触发报警的设备通道号
     */
    @TableField(value = "sys_device_message_event_channel_no")
    @ApiModelProperty(value = "触发报警的设备通道号")
    @Schema(description = "触发报警的设备通道号")
    private String sysDeviceMessageEventChannelNo;

    /**
     * 触发报警的设备通道名称
     */
    @TableField(value = "sys_device_message_event_channel_name")
    @ApiModelProperty(value = "触发报警的设备通道名称")
    @Schema(description = "触发报警的设备通道名称")
    private String sysDeviceMessageEventChannelName;

    /**
     * 读卡器类型：1-IC读卡器，2-身份证读卡器，3-二维码读卡器
     */
    @TableField(value = "sys_device_message_event_card_reader_kind")
    @ApiModelProperty(value = "读卡器类型：1-IC读卡器，2-身份证读卡器，3-二维码读卡器	")
    @Schema(description = "读卡器类型：1-IC读卡器，2-身份证读卡器，3-二维码读卡器	")
    private String sysDeviceMessageEventCardReaderKind;

    /**
     * 读卡器编号
     */
    @TableField(value = "sys_device_message_event_card_reader_no")
    @ApiModelProperty(value = "读卡器编号")
    @Schema(description = "读卡器编号")
    private Integer sysDeviceMessageEventCardReaderNo;

    /**
     * 门编号
     */
    @TableField(value = "sys_device_message_event_door_no")
    @ApiModelProperty(value = "门编号")
    @Schema(description = "门编号")
    private String sysDeviceMessageEventDoorNo;

    /**
     * 事件类型
     */
    @TableField(value = "sys_device_message_event_event_type")
    @ApiModelProperty(value = "事件类型")
    @Schema(description = "事件类型")
    private String sysDeviceMessageEventEventType;

    /**
     * 事件码
     */
    @TableField(value = "sys_device_message_event_event_code")
    @ApiModelProperty(value = "事件码")
    @Schema(description = "事件码")
    private String sysDeviceMessageEventEventCode;

    /**
     * 事件描述
     */
    @TableField(value = "sys_device_message_event_event_description")
    @ApiModelProperty(value = "事件描述")
    @Schema(description = "事件描述")
    private String sysDeviceMessageEventEventDescription;

    /**
     * 事件来源(目前暂未使用)
     */
    @TableField(value = "sys_device_message_event_event_source")
    @ApiModelProperty(value = "事件来源(目前暂未使用)")
    @Schema(description = "事件来源(目前暂未使用)")
    private String sysDeviceMessageEventEventSource;

    /**
     * 图片URL
     */
    @TableField(value = "sys_device_message_event_event_picture_url")
    @ApiModelProperty(value = "图片URL")
    @Schema(description = "图片URL")
    private String sysDeviceMessageEventEventPictureUrl;
}