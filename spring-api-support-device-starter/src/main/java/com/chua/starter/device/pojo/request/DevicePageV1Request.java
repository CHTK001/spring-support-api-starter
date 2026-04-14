package com.chua.starter.device.pojo.request;

import com.chua.starter.entity.device.SysDevice;
import com.chua.starter.mybatis.entity.Query;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备分页请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "设备分页请求")
public class DevicePageV1Request extends Query<SysDevice> {

    @Schema(description = "设备名称(支持模糊查询)")
    private String sysDeviceName;

    @Schema(description = "项目ID")
    private Integer sysProjectId;

    @Schema(description = "设备序列号(支持模糊查询)")
    private String sysDeviceSerialNumber;

    @Schema(description = "设备是否禁用;0:启用")
    private Integer sysDeviceStatus;

    @Schema(description = "设备是否在线;0:离线")
    private Integer sysDeviceOnline;
}
