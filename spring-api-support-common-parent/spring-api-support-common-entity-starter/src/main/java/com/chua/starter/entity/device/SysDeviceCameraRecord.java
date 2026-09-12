package com.chua.starter.entity.device;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* 摄像头记录
*
* @since 2026/08/03
* @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "摄像头记录")
@Data
@TableName(value = "sys_device_camera_record")
public class SysDeviceCameraRecord extends SysBase {
    /**
    * 摄像头记录主键标识
     */
    @TableId(value = "sys_device_camera_record_id", type = IdType.AUTO)
    @Schema(description = "")
    /** sysdevicerecordid */
    private Integer sysDeviceRecordId;
}
