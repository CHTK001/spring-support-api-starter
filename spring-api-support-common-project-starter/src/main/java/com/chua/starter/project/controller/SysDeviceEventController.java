package com.chua.starter.project.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.project.service.SysDeviceMessageEventService;
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
 * 设备消息接口
 *
 * @author CH
 * @since 2024/9/4
 */
@RestController
@Slf4j
@Api(tags = "设备消息接口")
@Tag(name = "设备消息接口")
@RequestMapping("/v2/project/device/message")
@RequiredArgsConstructor
public class SysDeviceEventController {

    final SysDeviceMessageEventService sysDeviceMessageEventService;


    /**
     * 同步消息
     *
     * @param request
     * @return
     */
    @GetMapping("/sync")
    @Operation(summary = "同步消息")
    public ReturnResult<Boolean> syncEventMessage(@ParameterObject CardDevicePageV1Request request) {
        return sysDeviceMessageEventService.syncEventMessage(request);
    }

}
