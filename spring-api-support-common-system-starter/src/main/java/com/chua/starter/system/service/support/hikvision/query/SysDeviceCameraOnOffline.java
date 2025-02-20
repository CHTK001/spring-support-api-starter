package com.chua.starter.system.service.support.hikvision.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 摄像头上下线
 *
 * @author CH
 */
@Data
@Schema(description = "摄像头上下线")
public class SysDeviceCameraOnOffline {

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
