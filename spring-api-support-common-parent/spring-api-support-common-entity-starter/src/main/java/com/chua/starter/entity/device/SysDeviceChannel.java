package com.chua.starter.entity.device;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备通道信息
 *
 * @since 2026/08/03
 * @author CH
 */
@Schema
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_device_channel")
public class SysDeviceChannel extends SysBase {
    /**
      * 通道主键标识
     */
    @TableId(value = "sys_device_channel_id", type = IdType.AUTO)
    @Schema(description = "")
    /** sysdevice通道标识 */
    private Integer sysDeviceChannelId;

    /**
    * 通道名称
     */
    @TableField(value = "sys_device_channel_name")
    @Schema(description = "通道名称")
    /** sysdevice通道名称 */
    private String sysDeviceChannelName;

    /**
    * 通道号
     */
    @TableField(value = "sys_device_channel_no")
    @Schema(description = "通道号")
    /** sysdevice通道NO */
    private Integer sysDeviceChannelNo;

    /**
    * 设备序列号
     */
    @TableField(value = "sys_device_serial_number")
    @Schema(description = "设备序列号")
    /** sysdeviceserial数字 */
    private String sysDeviceSerialNumber;

    /**
    * 管道类型
     */
    @TableField(value = "sys_device_channel_type")
    @Schema(description = "管道类型")
    /** sysdevice通道类型 */
    private String sysDeviceChannelType;

    /**
    * 通道状态，0：离线，1：在线，-1：设备未上报
     */
    @TableField(value = "sys_device_channel_status")
    @Schema(description = "通道状态，0：离线，1：在线，-1：设备未上报; 2:未知")
    /** sysdevice通道状态 */
    private Integer sysDeviceChannelStatus;

    /**
    * 通道启用状态：0-禁用；1-启用
     */
    @TableField(value = "sys_device_channel_use")
    @Schema(description = "通道启用状态：0-禁用；1-启用")
    /** sysdevice通道USE */
    private Integer sysDeviceChannelUse;
}
