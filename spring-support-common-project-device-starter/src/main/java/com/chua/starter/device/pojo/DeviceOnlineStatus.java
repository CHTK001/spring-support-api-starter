package com.chua.starter.device.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备在线状态。
 */
@Data
@Schema(description = "设备在线状态")
public class DeviceOnlineStatus {

    /**
     * 设备序列号
     */
    @Schema(description = "设备序列号")
    private String sysCameraTemplateSerialNumber;

    /**
     * 在线状态
     */
    @Schema(description = "在线状态;0: 离线; 1:在线")
    private Integer online;
}
