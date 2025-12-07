package com.chua.starter.project.pojo.device;

import com.chua.starter.mybatis.entity.Query;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author CH
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CardDevicePageV1Request extends Query<CardDevicePageV1Request> {
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
     * 开始时间
     */
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
