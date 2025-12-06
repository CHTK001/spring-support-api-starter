package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 摄像头记录
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "摄像头记录")
@Schema(description = "摄像头记录")
@Data
@TableName(value = "sys_device_record")
public class SysDeviceCameraRecord extends SysBase {
    @TableId(value = "sys_camera_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer sysDeviceRecordId;
}
