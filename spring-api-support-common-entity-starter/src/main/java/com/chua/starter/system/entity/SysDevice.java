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

import java.math.BigDecimal;

/**
 * 设备管理
 */
@ApiModel(description = "设备管理")
@Schema(description = "设备管理")
@Data
@TableName(value = "sys_device")
public class SysDevice extends SysBase {
    @TableId(value = "sys_device_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysDeviceId;
    /**
     * 项目id
     */
    @TableField(value = "sys_project_id")
    @ApiModelProperty(value = "项目id")
    @Schema(description = "项目id")
    private Integer sysProjectId;
    /**
     * 部门id
     */
    @TableField(value = "sys_dept_id")
    @ApiModelProperty(value = "部门id")
    @Schema(description = "部门id")
    private Integer sysDeptId;

    /**
     * 名称
     */
    @TableField(value = "sys_device_name")
    @ApiModelProperty(value = "名称")
    @Schema(description = "名称")
    private String sysDeviceName;

    /**
     * 序列号
     */
    @TableField(value = "sys_device_serial_number")
    @ApiModelProperty(value = "序列号")
    @Schema(description = "序列号")
    private String sysDeviceSerialNumber;

    /**
     * 组织编码
     */
    @TableField(value = "sys_device_org_code")
    @ApiModelProperty(value = "组织编码")
    @Schema(description = "组织编码")
    private String sysDeviceOrgCode;

    /**
     * 组织名称
     */
    @TableField(value = "sys_device_org_name")
    @ApiModelProperty(value = "组织名称")
    @Schema(description = "组织名称")
    private String sysDeviceOrgName;

    /**
     * 管道名称
     */
    @TableField(value = "sys_device_channel_name")
    @ApiModelProperty(value = "管道名称")
    @Schema(description = "管道名称")
    private String sysDeviceChannelName;

    /**
     * 管道号
     */
    @TableField(value = "sys_device_channel_number")
    @ApiModelProperty(value = "管道号")
    @Schema(description = "管道号")
    private String sysDeviceChannelNumber;
    /**
     * 管道数量
     */
    @TableField(value = "sys_device_channel_count")
    @ApiModelProperty(value = "管道数量")
    @Schema(description = "管道数量")
    private Integer sysDeviceChannelCount;

    /**
     * 管道信息, 多个以逗号分隔
     */
    @TableField(value = "sys_device_channels")
    @ApiModelProperty(value = "管道信息, 多个以逗号分隔")
    @Schema(description = "管道信息, 多个以逗号分隔")
    private String sysDeviceChannels;

    /**
     * 主码流编码
     */
    @TableField(value = "sys_device_main_subtype")
    @ApiModelProperty(value = "主码流编码")
    @Schema(description = "主码流编码")
    private String sysDeviceMainSubtype;

    /**
     * 子码流编码
     */
    @TableField(value = "sys_device_sub_subtype")
    @ApiModelProperty(value = "子码流编码")
    @Schema(description = "子码流编码")
    private String sysDeviceSubSubtype;
    /**
     * 是否在线; 0: 离线； 1:在线
     */
    @TableField(value = "sys_device_status")
    @ApiModelProperty(value = "是否禁用; 0: 禁用")
    @Schema(description = "是否禁用; 0: 禁用")
    private Integer sysDeviceStatus;
    /**
     * 是否在线; 0: 离线； 1:在线
     */
    @TableField(value = "sys_device_online")
    @ApiModelProperty(value = "是否在线; 0: 离线； 1:在线")
    @Schema(description = "是否在线; 0: 离线； 1:在线")
    private Integer sysDeviceOnline;

    /**
     * 位置
     */
    @TableField(value = "sys_device_position")
    @ApiModelProperty(value = "位置")
    @Schema(description = "位置")
    private String sysDevicePosition;
    /**
     * 资源类型
     */
    @TableField(value = "sys_device_resource_type")
    @ApiModelProperty(value = "资源类型")
    @Schema(description = "资源类型")
    private String sysDeviceResourceType;
    /**
     * 描述
     */
    @TableField(value = "sys_device_description")
    @ApiModelProperty(value = "描述")
    @Schema(description = "描述")
    private String sysDeviceDescription;

    /**
     * 版本
     */
    @TableField(value = "sys_device_version")
    @ApiModelProperty(value = "版本")
    @Schema(description = "版本")
    private String sysDeviceVersion;

    /**
     * 验证码
     */
    @TableField(value = "sys_device_validate_code")
    @ApiModelProperty(value = "验证码")
    @Schema(description = "验证码")
    private String sysDeviceValidateCode;
    /**
     * 设备类型
     */
    @TableField(value = "sys_device_type")
    @ApiModelProperty(value = "设备类型")
    @Schema(description = "设备类型")
    private String sysDeviceType;

    /**
     * 账号
     */
    @TableField(value = "sys_device_account")
    @ApiModelProperty(value = "账号")
    @Schema(description = "账号")
    private String sysDeviceAccount;

    /**
     * 密码
     */
    @TableField(value = "sys_device_password")
    @ApiModelProperty(value = "密码")
    @Schema(description = "密码")
    private String sysDevicePassword;

    /**
     * 推流地址; 存在则使用该地址播放视像头
     */
    @TableField(value = "sys_device_rtsp")
    @ApiModelProperty(value = "推流地址; 存在则使用该地址播放视像头")
    @Schema(description = "推流地址; 存在则使用该地址播放视像头")
    private String sysDeviceRtsp;
    /**
     * 网络地址
     */
    @TableField(value = "sys_device_rtsp_webrtc")
    @ApiModelProperty(value = "rtsp推理webrtc地址; 采用webrtc解析rtsp")
    @Schema(description = "rtsp推理webrtc地址; 采用webrtc解析rtsp")
    private String sysDeviceRtspWebrtc;

    /**
     * 网络地址
     */
    @TableField(value = "sys_device_net_address")
    @ApiModelProperty(value = "网络地址")
    @Schema(description = "网络地址")
    private String sysDeviceNetAddress;
    /**
     * 纬度
     */
    @TableField(value = "sys_device_latitude")
    @ApiModelProperty(value = "纬度")
    @Schema(description = "纬度")
    private BigDecimal sysDeviceLatitude;

    /**
     * 经度
     */
    @TableField(value = "sys_device_longitude")
    @ApiModelProperty(value = "经度")
    @Schema(description = "经度")
    private BigDecimal sysDeviceLongitude;

    /**
     * 所属者唯一编码
     */
    @TableField(value = "sys_device_owner")
    @ApiModelProperty(value = "所属者唯一编码")
    @Schema(description = "所属者唯一编码")
    private String sysDeviceOwner;
}