package com.chua.starter.project.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.project.service.SysDeviceService;
import com.chua.starter.entity.project.SysDevice;
import com.chua.starter.entity.project.SysDeviceCardEvent;
import com.chua.starter.project.pojo.device.CardDevicePageV1Request;
import com.chua.starter.project.pojo.device.DevicePageV1Request;
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
     * 门禁设备历史记录
     *
     * @param request 请求
     */
    @ApiOperation("门禁设备历史记录")
    @Operation(summary = "门禁设备历史记录")
    @GetMapping("card/history/page")
    public ReturnPageResult<SysDeviceCardEvent> cardHistoryPage(@ParameterObject CardDevicePageV1Request request) {
        return ReturnPageResultUtils.ok(sysDeviceService.cardHistoryPage(request));
    }
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
