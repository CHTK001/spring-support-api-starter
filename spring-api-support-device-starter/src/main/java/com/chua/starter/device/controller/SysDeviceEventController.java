package com.chua.starter.device.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.device.service.DeviceService;
import com.chua.starter.project.pojo.device.CardDevicePageV1Request;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 独立设备事件接口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "独立设备事件接口")
@Tag(name = "独立设备事件接口")
@RequestMapping("/v2/device/message")
public class SysDeviceEventController {

    private final DeviceService deviceService;

    @GetMapping("/sync")
    @Operation(summary = "同步设备事件消息")
    public ReturnResult<Boolean> syncEventMessage(@ParameterObject CardDevicePageV1Request request) {
        return deviceService.syncEventMessage(request);
    }
}
