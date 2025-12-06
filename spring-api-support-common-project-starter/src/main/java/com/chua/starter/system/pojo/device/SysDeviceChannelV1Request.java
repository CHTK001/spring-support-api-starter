package com.chua.starter.system.pojo.device;

import com.chua.starter.system.entity.SysDeviceChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 设备管道请求
 *
 * @author CH
 */
@Data
@Schema(description = "设备管道请求")
public class SysDeviceChannelV1Request {

    /**
     * 设备编号
     */
    @Schema(description = "设备编号")
    private String sysDeviceSerialNumber;

    /**
     * 设备管道
     */
    @Schema(description = "设备管道")
    private List<SysDeviceChannel> channels;
}
