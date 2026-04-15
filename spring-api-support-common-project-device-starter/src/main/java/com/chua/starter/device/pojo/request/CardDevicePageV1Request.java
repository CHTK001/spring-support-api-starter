package com.chua.starter.device.pojo.request;

import com.chua.starter.mybatis.entity.Query;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 设备事件分页请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "设备事件分页请求")
public class CardDevicePageV1Request extends Query<CardDevicePageV1Request> {

    @Schema(description = "设备名称(支持模糊查询)")
    private String sysDeviceName;

    @Schema(description = "项目ID")
    private Integer sysProjectId;

    @Schema(description = "设备序列号(支持模糊查询)")
    private String sysDeviceSerialNumber;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
