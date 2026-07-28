package com.chua.starter.device.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备在线时间线请求。
 */
@Data
@Schema(description = "设备在线时间线请求")
public class DeviceTimelineV1Request {

    @Schema(description = "设备ID")
    private Integer sysDeviceCameraId;
}
