package com.chua.starter.system.pojo.device;

import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysDevice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 同步请求
 *
 * @author CH
 */
@Data
@Schema(description = "模板查询请求")
public class DevicePageV1Request extends Query<SysDevice> {

    /**
     * 设备名称
     */
    @Schema(description = "设备名称(支持模糊查询)")
    private String sysDeviceName;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Integer sysProjectId;
    /**
     * 设备序列号
     */
    @Schema(description = "设备序列号(支持模糊查询)")
    private String sysDeviceSerialNumber;

    /**
     * 设备状态
     */
    @Schema(description = "设备是否禁用;0:启用")
    private Integer sysDeviceStatus;


    /**
     * 设备是否在线;0:离线
     */
    @Schema(description = "设备是否在线;0:离线")
    private Integer sysDeviceOnline;


}
