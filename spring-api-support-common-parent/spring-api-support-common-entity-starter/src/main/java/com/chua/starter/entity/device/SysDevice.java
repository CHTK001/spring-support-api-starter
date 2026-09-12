package com.chua.starter.entity.device;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 设备管理
 *
 * @since 2026/08/03
 * @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "设备管理")
@Data
@TableName(value = "sys_device")
public class SysDevice extends SysBase {
    /**
      * 设备主键标识
     */
    @TableId(value = "sys_device_id", type = IdType.AUTO)
    @Schema(description = "")
    /** sysdeviceid */
    private Integer sysDeviceId;

    /**
    * 项目标识
     */
    @TableField(value = "sys_project_id")
    @Schema(description = "项目id")
    /** sysprojectid */
    private Integer sysProjectId;

    /**
    * 部门标识
     */
    @TableField(value = "sys_dept_id")
    @Schema(description = "部门id")
    /** sysdeptid */
    private Integer sysDeptId;

    /**
    * 名称
     */
    @TableField(value = "sys_device_name")
    @Schema(description = "名称")
    /** sysdevice名称 */
    private String sysDeviceName;

    /**
    * 序列号
     */
    @TableField(value = "sys_device_serial_number")
    @Schema(description = "序列号")
    /** sysdeviceserial数字 */
    private String sysDeviceSerialNumber;

    /**
    * 组织编码
     */
    @TableField(value = "sys_device_org_code")
    @Schema(description = "组织编码")
    /** sysdeviceorg代码 */
    private String sysDeviceOrgCode;

    /**
    * 组织名称
     */
    @TableField(value = "sys_device_org_name")
    @Schema(description = "组织名称")
    /** sysdeviceorg名称 */
    private String sysDeviceOrgName;

    /**
    * 管道数量
     */
    @TableField(exist = false)
    @Schema(description = "管道数量")
    /** sysdevice通道数量 */
    private Integer sysDeviceChannelCount;

    /**
    * 管道信息
     */
    @TableField(exist = false)
    /** 通道列表 */
    private List<SysDeviceChannel> channelList;

    /**
    * 管道信息, 多个以逗号分隔
     */
    @TableField(value = "sys_device_channels")
    @Schema(description = "管道信息, 多个以逗号分隔")
    /** sysdevicechannels */
    private String sysDeviceChannels;

    /**
    * 主码流编码
     */
    @TableField(value = "sys_device_main_subtype")
    @Schema(description = "主码流编码")
    /** sysdevicemainsubtype */
    private String sysDeviceMainSubtype;

    /**
    * 子码流编码
     */
    @TableField(value = "sys_device_sub_subtype")
    @Schema(description = "子码流编码")
    /** sysdevicesubsubtype */
    private String sysDeviceSubSubtype;

    /**
    * 是否在线; 0: 离线； 1:在线
     */
    @TableField(value = "sys_device_status")
    @Schema(description = "是否禁用; 0: 禁用")
    /** sysdevice状态 */
    private Integer sysDeviceStatus;

    /**
    * 是否在线; 0: 离线； 1:在线
     */
    @TableField(value = "sys_device_online")
    @Schema(description = "是否在线; 0: 离线； 1:在线")
    /** sysdeviceonline */
    private Integer sysDeviceOnline;

    /**
    * 位置
     */
    @TableField(value = "sys_device_position")
    @Schema(description = "位置")
    /** sysdevice位置 */
    private String sysDevicePosition;

    /**
    * 资源类型
     */
    @TableField(value = "sys_device_resource_type")
    @Schema(description = "资源类型")
    /** sysdeviceresource类型 */
    private String sysDeviceResourceType;

    /**
    * 描述
     */
    @TableField(value = "sys_device_description")
    @Schema(description = "描述")
    /** sysdevice描述 */
    private String sysDeviceDescription;

    /**
    * 版本
     */
    @TableField(value = "sys_device_version")
    @Schema(description = "版本")
    /** sysdevice版本 */
    private String sysDeviceVersion;

    /**
    * 验证码
     */
    @TableField(value = "sys_device_validate_code")
    @Schema(description = "验证码")
    /** sysdevicevalidate代码 */
    private String sysDeviceValidateCode;

    /**
    * 设备类型
     */
    @TableField(value = "sys_device_type")
    @Schema(description = "设备类型")
    /** sysdevice类型 */
    private String sysDeviceType;

    /**
    * 账号
     */
    @TableField(value = "sys_device_account")
    @Schema(description = "账号")
    /** sysdeviceaccount */
    private String sysDeviceAccount;

    /**
    * 密码
     */
    @TableField(value = "sys_device_password")
    @Schema(description = "密码")
    /** sysdevice密码 */
    private String sysDevicePassword;

    /**
    * 推流地址; 存在则使用该地址播放视像头
     */
    @TableField(value = "sys_device_rtsp")
    @Schema(description = "推流地址; 存在则使用该地址播放视像头")
    /** sysdevicertsp */
    private String sysDeviceRtsp;

    /**
    * 网络地址
     */
    @TableField(value = "sys_device_rtsp_webrtc")
    @Schema(description = "rtsp推理webrtc地址; 采用webrtc解析rtsp")
    /** sysdevicertspwebrtc */
    private String sysDeviceRtspWebrtc;

    /**
    * 网络地址
     */
    @TableField(value = "sys_device_net_address")
    @Schema(description = "网络地址")
    /** sysdevicenet地址 */
    private String sysDeviceNetAddress;

    /**
    * 网络地址物理地址
     */
    @TableField(value = "sys_device_net_physical_address")
    @Schema(description = "网络地址物理地址")
    /** sysdevicenetphysical地址 */
    private String sysDeviceNetPhysicalAddress;

    /**
    * 纬度
     */
    @TableField(value = "sys_device_latitude")
    @Schema(description = "纬度")
    /** sysdevicelatitude */
    private BigDecimal sysDeviceLatitude;

    /**
    * 经度
     */
    @TableField(value = "sys_device_longitude")
    @Schema(description = "经度")
    /** sysdevicelongitude */
    private BigDecimal sysDeviceLongitude;

    /**
    * 所属者唯一编码
     */
    @TableField(value = "sys_device_owner")
    @Schema(description = "所属者唯一编码")
    /** sysdeviceowner */
    private String sysDeviceOwner;
}
