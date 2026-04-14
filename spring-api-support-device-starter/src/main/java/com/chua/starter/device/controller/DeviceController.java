package com.chua.starter.device.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.device.pojo.request.CardDevicePageV1Request;
import com.chua.starter.device.pojo.request.DevicePageV1Request;
import com.chua.starter.device.service.DeviceService;
import com.chua.starter.entity.device.SysDevice;
import com.chua.starter.entity.device.SysDeviceCardEvent;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
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
 * 独立设备接口。
 */
@Slf4j
@RestController("deviceDeviceController")
@RequiredArgsConstructor
@Api(tags = "独立设备接口")
@Tag(name = "独立设备接口")
@RequestMapping("/v2/device")
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping("/card/history/page")
    @Operation(summary = "门禁设备历史记录分页")
    public ReturnPageResult<SysDeviceCardEvent> cardHistoryPage(@ParameterObject CardDevicePageV1Request request) {
        return ReturnPageResultUtils.ok(deviceService.cardHistoryPage(request));
    }

    @GetMapping("/page")
    @Operation(summary = "设备分页")
    public ReturnPageResult<SysDevice> page(@ParameterObject DevicePageV1Request request) {
        return ReturnPageResultUtils.ok(deviceService.pageFor(request));
    }
}
