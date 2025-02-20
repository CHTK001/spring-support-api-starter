package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.system.entity.SysDevice;
import com.chua.starter.system.pojo.device.DevicePageV1Request;
import com.chua.starter.system.service.SysDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备接口
 *
 * @author CH
 * @since 2024/9/4
 */
@RestController
@Slf4j
@Api(tags = "设备接口")
@Tag(name = "设备接口")
@RequestMapping("/v2/project/device")
@RequiredArgsConstructor
public class SysDeviceController {

    final SysDeviceService sysDeviceService;


    /**
     * 分页
     *
     * @param request 请求
     */
    @ApiOperation("分页")
    @Operation(summary = "分页")
    @GetMapping("/page")
    public ReturnPageResult<SysDevice> page(@ParameterObject DevicePageV1Request request) {
        return ReturnPageResultUtils.ok(sysDeviceService.pageFor(request));
    }

}
