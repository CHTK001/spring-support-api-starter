package com.chua.starter.device.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.device.service.DeviceService;
import com.chua.starter.project.pojo.device.SysDeviceChannelV1Request;
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
@RestController
@RequiredArgsConstructor
@Api(tags = "独立设备通道接口")
@Tag(name = "独立设备通道接口")
@RequestMapping("/v2/device/channel")
public class SysDeviceChannelController {

    private final DeviceService deviceService;

    @PutMapping("/update")
    @Operation(summary = "更新设备通道")
    public ReturnResult<Boolean> update(@RequestBody SysDeviceChannelV1Request request) {
        return deviceService.syncChannels(request.getSysDeviceSerialNumber(), request.getChannels());
    }
}
