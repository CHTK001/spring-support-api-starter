package com.chua.starter.device.pojo.request;

import com.chua.common.support.utils.group.SelectGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设备在线状态检测请求。
 */
@Data
@Schema(description = "设备在线状态检测请求")
public class CheckDeviceOnlineV1Request {

    @Schema(description = "设备IDS")
    @NotNull(message = "设备ID不能为空", groups = SelectGroup.class)
    private String[] sysDeviceCameraIds;
}
