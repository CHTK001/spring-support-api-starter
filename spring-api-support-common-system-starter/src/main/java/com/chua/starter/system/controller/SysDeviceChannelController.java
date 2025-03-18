package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.pojo.device.SysDeviceChannelV1Request;
import com.chua.starter.system.service.SysDeviceChannelService;
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
 * 设备管道接口
 *
 * @author CH
 * @since 2024/9/4
 */
@RestController
@Slf4j
@Api(tags = "设备管道接口")
@Tag(name = "设备管道接口")
@RequestMapping("/v2/project/device/channel")
@RequiredArgsConstructor
public class SysDeviceChannelController {

    final SysDeviceChannelService sysDeviceChannelService;


    /**
     * 更新管道信息
     *
     * @param v1Request
     * @return
     */
    @PutMapping("/update")
    @Operation(summary = "更新管道信息")
    public ReturnResult<Boolean> syncEventMessage(@RequestBody SysDeviceChannelV1Request v1Request) {
        return sysDeviceChannelService.syncEventMessage(v1Request);
    }

}
