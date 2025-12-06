package com.chua.starter.project.pojo.device;

import com.chua.common.support.validator.group.SelectGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设备在离线情�?
 *
 * @author CH
 */
@Data
@Schema(description = "设备在线情况")
public class CheckDeviceOnlineV1Request {

    /**
     * 设备ID
     */
    @Schema(description = "设备IDS")
    @NotNull(message = "设备ID不能为空", groups = SelectGroup.class)
    private String[] sysDeviceCameraIds;
}
