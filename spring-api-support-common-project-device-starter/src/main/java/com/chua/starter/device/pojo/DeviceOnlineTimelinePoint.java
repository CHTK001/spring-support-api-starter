package com.chua.starter.device.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备上下线时间点。
 */
@Data
@Schema(description = "设备上下线时间点")
public class DeviceOnlineTimelinePoint {

    /**
     * 采集时间
     */
    @Schema(description = "采集时间")
    private LocalDateTime collectTime;

    /**
     * 0:下线 1:上线
     */
    @Schema(description = "0:下线 1:上线")
    private Integer online;
}
