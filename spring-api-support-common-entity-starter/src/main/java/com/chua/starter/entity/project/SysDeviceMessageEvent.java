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
 * 事件
 */
@Schema(description = "事件")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_device_message_event")
public class SysDeviceMessageEvent extends SysBase {
    @TableId(value = "sys_device_message_event_id", type = IdType.AUTO)
    @Schema(description = "")
    private Integer sysDeviceMessageEventId;

    /**
     * 事件类型
     */
    @TableField(value = "sys_device_message_event_type")
    @Schema(description = "事件类型")
    private String sysDeviceMessageEventType;

    /**
     * 事件ID
     */
    @TableField(value = "sys_device_message_event_event_id")
    @Schema(description = "事件ID")
    private String sysDeviceMessageEventEventId;

    /**
     * 事件时间
     */
    @TableField(value = "sys_device_message_event_data_time")
    @Schema(description = "事件时间")
    private LocalDateTime sysDeviceMessageEventDataTime;

    /**
     * 人员ID
     */
    @TableField(value = "sys_device_message_event_employee_no")
    @Schema(description = "人员ID")
    private String sysDeviceMessageEventEmployeeNo;
    /**
     * 姓名
     */
    @TableField(value = "sys_device_message_event_employee_name")
    @Schema(description = "姓名")
    private String sysDeviceMessageEventEmployeeName;

    /**
     * 卡号
     */
    @TableField(value = "sys_device_message_event_card_no")
    @Schema(description = "卡号")
    private String sysDeviceMessageEventCardNo;

    /**
     * 设备序列号
     */
    @TableField(value = "sys_device_serial_number")
    @Schema(description = "设备序列号")
    private String sysDeviceSerialNumber;

    /**
     * 设备名称
     */
    @TableField(value = "sys_device_name")
    @Schema(description = "设备名称")
    private String sysDeviceName;

    /**
     * 触发报警的设备通道号
     */
    @TableField(value = "sys_device_message_event_channel_no")
    @Schema(description = "触发报警的设备通道号")
    private String sysDeviceMessageEventChannelNo;
    /**
     * 触发设备通道位置
     */
    @TableField(value = "sys_device_message_event_location")
    @Schema(description = "触发设备通道位置")
    private String sysDeviceMessageEventLocation;

    /**
     * 触发报警的设备通道名称
     */
    @TableField(value = "sys_device_message_event_channel_name")
    @Schema(description = "触发报警的设备通道名称")
    private String sysDeviceMessageEventChannelName;

    /**
     * 读卡器类型：1-IC读卡器，2-身份证读卡器，3-二维码读卡器
     */
    @TableField(value = "sys_device_message_event_card_reader_kind")
    @Schema(description = "读卡器类型：1-IC读卡器，2-身份证读卡器，3-二维码读卡器	")
    private String sysDeviceMessageEventCardReaderKind;

    /**
     * 读卡器编号
     */
    @TableField(value = "sys_device_message_event_card_reader_no")
    @Schema(description = "读卡器编号")
    private Integer sysDeviceMessageEventCardReaderNo;

    /**
     * 门编号
     */
    @TableField(value = "sys_device_message_event_door_no")
    @Schema(description = "门编号")
    private String sysDeviceMessageEventDoorNo;

    /**
     * 事件类型
     */
    @TableField(value = "sys_device_message_event_event_type")
    @Schema(description = "事件消息类型")
    private String sysDeviceMessageEventEventType;
    /**
     * 事件类型名称
     */
    @TableField(value = "sys_device_message_event_event_type_name")
    @Schema(description = "事件类型名称")
    private String sysDeviceMessageEventEventTypeName;

    /**
     * 事件码
     */
    @TableField(value = "sys_device_message_event_event_code")
    @Schema(description = "事件码")
    private String sysDeviceMessageEventEventCode;

    /**
     * 事件描述
     */
    @TableField(value = "sys_device_message_event_event_description")
    @Schema(description = "事件描述")
    private String sysDeviceMessageEventEventDescription;

    /**
     * 事件来源(目前暂未使用)
     */
    @TableField(value = "sys_device_message_event_event_source")
    @Schema(description = "事件来源(目前暂未使用)")
    private String sysDeviceMessageEventEventSource;

    /**
     * 图片URL
     */
    @TableField(value = "sys_device_message_event_event_picture_url")
    @Schema(description = "图片URL")
    private String sysDeviceMessageEventEventPictureUrl;
}
