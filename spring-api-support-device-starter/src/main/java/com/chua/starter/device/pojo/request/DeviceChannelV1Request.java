package com.chua.starter.device.pojo.request;

import com.chua.starter.entity.device.SysDeviceChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 设备通道更新请求。
 */
@Data
@Schema(description = "设备通道更新请求")
public class DeviceChannelV1Request {

    @Schema(description = "设备编号")
    private String sysDeviceSerialNumber;

    @Schema(description = "设备通道")
    private List<SysDeviceChannel> channels;
}
