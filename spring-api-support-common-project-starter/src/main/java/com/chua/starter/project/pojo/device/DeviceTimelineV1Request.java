package com.chua.starter.project.pojo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备在离线情况查询请求
 *
 * @author CH
 */
@Data
@Schema(description = "设备在离线情况")
public class DeviceTimelineV1Request {

    /**
     * 设备ID
     */
    @Schema(description = "设备ID")
    private Integer sysDeviceCameraId;
}
