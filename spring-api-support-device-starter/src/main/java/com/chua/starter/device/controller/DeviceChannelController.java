package com.chua.starter.device.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.device.pojo.request.DeviceChannelV1Request;
import com.chua.starter.device.service.DeviceService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 独立设备通道接口。
 */
@Slf4j
@RestController("deviceDeviceChannelController")
@RequiredArgsConstructor
@Api(tags = "独立设备通道接口")
@Tag(name = "独立设备通道接口")
@RequestMapping("/v2/device/channel")
public class DeviceChannelController {

    private final DeviceService deviceService;

    @PutMapping("/update")
    @Operation(summary = "更新设备通道")
    public ReturnResult<Boolean> update(@RequestBody DeviceChannelV1Request request) {
        return deviceService.syncChannels(request.getSysDeviceSerialNumber(), request.getChannels());
    }
}
