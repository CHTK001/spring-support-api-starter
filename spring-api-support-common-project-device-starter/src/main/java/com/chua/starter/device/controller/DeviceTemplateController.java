package com.chua.starter.device.controller;

import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.SelectGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.device.pojo.DeviceOnlineStatus;
import com.chua.starter.device.pojo.DeviceOnlineTimelinePoint;
import com.chua.starter.device.pojo.request.CheckDeviceOnlineV1Request;
import com.chua.starter.device.pojo.request.DevicePreviewUrlV1Request;
import com.chua.starter.device.pojo.request.DeviceTimelineV1Request;
import com.chua.starter.device.pojo.request.SyncV1Request;
import com.chua.starter.device.pojo.request.TemplatePageV1Request;
import com.chua.starter.device.service.DeviceService;
import com.chua.starter.entity.device.SysDevice;
import com.chua.starter.entity.device.SysDeviceCameraRecord;
import com.chua.starter.entity.device.SysDeviceOrg;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 独立设备模板接口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "独立设备模板接口")
@Tag(name = "独立设备模板接口")
@RequestMapping("/v2/device/template")
public class DeviceTemplateController {

    private final DeviceService deviceService;

    @PutMapping("/previewUrl")
    @Operation(summary = "获取设备预览地址")
    public ReturnResult<String> previewUrl(@RequestBody @Validated(SelectGroup.class) DevicePreviewUrlV1Request request) {
        return deviceService.previewUrl(request);
    }

    @GetMapping("/log/page")
    @Operation(summary = "设备日志分页")
    public ReturnPageResult<SysDeviceCameraRecord> pageForLog(@ParameterObject TemplatePageV1Request request) {
        return ReturnPageResultUtils.ok(deviceService.pageForLog(request));
    }

    @GetMapping("/page")
    @Operation(summary = "设备模板分页")
    public ReturnPageResult<SysDevice> page(@ParameterObject TemplatePageV1Request request) {
        return ReturnPageResultUtils.ok(deviceService.pageFor(request));
    }

    @GetMapping("/listOrg")
    @Operation(summary = "获取设备组织")
    public ReturnResult<List<SysDeviceOrg>> listOrg(@Valid @Validated(SelectGroup.class) @ParameterObject SyncV1Request request) {
        return deviceService.listOrg(request);
    }

    @GetMapping("/timeline")
    @Operation(summary = "获取设备在线时间线")
    public ReturnResult<List<DeviceOnlineTimelinePoint>> timeline(@ParameterObject DeviceTimelineV1Request request) {
        return deviceService.timeline(request);
    }

    @PutMapping("/check")
    @Operation(summary = "检测设备在线状态")
    public ReturnResult<List<DeviceOnlineStatus>> check(@RequestBody @Validated(SelectGroup.class) CheckDeviceOnlineV1Request request) {
        return deviceService.check(request);
    }

    @PutMapping("/syncTemplate")
    @Operation(summary = "同步设备模板")
    public ReturnResult<Boolean> syncTemplate(@RequestBody @Validated(SelectGroup.class) SyncV1Request request) {
        return deviceService.syncTemplate(request);
    }

    @PutMapping("/syncOrg")
    @Operation(summary = "同步设备组织")
    public ReturnResult<Boolean> syncOrg(@RequestBody @Validated(SelectGroup.class) SyncV1Request request) {
        return deviceService.syncOrg(request);
    }

    @PutMapping("/delete")
    @Operation(summary = "删除设备模板")
    public ReturnResult<Boolean> delete(@RequestBody @Validated(SelectGroup.class) SysDevice request) {
        return deviceService.deleteFor(request);
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备模板")
    public ReturnResult<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysDevice request) {
        return deviceService.updateFor(request);
    }

    @PostMapping("/save")
    @Operation(summary = "新增设备模板")
    public ReturnResult<SysDevice> save(@RequestBody @Validated(AddGroup.class) SysDevice request) {
        return deviceService.saveFor(request);
    }
}
