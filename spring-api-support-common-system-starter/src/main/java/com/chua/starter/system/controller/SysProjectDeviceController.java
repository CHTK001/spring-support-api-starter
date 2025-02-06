package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.service.SysDeviceService;
import com.chua.starter.service.support.hikvision.query.SysDeviceCameraOnOffline;
import com.chua.starter.service.support.hikvision.result.OnOffline;
import com.chua.starter.system.entity.SysDevice;
import com.chua.starter.system.entity.SysDeviceCameraRecord;
import com.chua.starter.system.entity.SysDeviceOrg;
import com.chua.starter.system.pojo.device.CheckDeviceOnlineV1Request;
import com.chua.starter.system.pojo.device.DevicePreviewUrlV1Request;
import com.chua.starter.system.pojo.device.DeviceTimelineV1Request;
import com.chua.starter.system.pojo.project.SyncV1Request;
import com.chua.starter.system.pojo.project.TemplatePageV1Request;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@RequestMapping("/v2/project/template/device")
@RequiredArgsConstructor
public class SysProjectDeviceController {

    final SysDeviceService sysDeviceService;


    /**
     * 预览地址
     *
     * @param request 请求
     */
    @Operation(summary = "预览地址")
    @PutMapping("/previewUrl")
    public ReturnResult<String> previewUrl(@RequestBody @Validated(SelectGroup.class) @ParameterObject DevicePreviewUrlV1Request request) {
        return sysDeviceService.previewUrl(request);
    }

    /**
     * 设备日志分页
     *
     * @param request 请求
     */
    @ApiOperation("设备日志分页")
    @Operation(summary = "设备日志分页")
    @GetMapping("/log/page")
    public ReturnPageResult<SysDeviceCameraRecord> pageForLog(@ParameterObject TemplatePageV1Request request) {
        return ReturnPageResultUtils.ok(sysDeviceService.pageForLog(request));
    }

    /**
     * 分页
     *
     * @param request 请求
     */
    @ApiOperation("分页")
    @Operation(summary = "分页")
    @GetMapping("/page")
    public ReturnPageResult<SysDevice> page(@ParameterObject TemplatePageV1Request request) {
        return ReturnPageResultUtils.ok(sysDeviceService.pageFor(request));
    }


    /**
     * 获取项目组织
     *
     * @param request 密钥
     */
    @Operation(summary = "获取项目组织")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    @GetMapping("/listOrg")
    public ReturnResult<List<SysDeviceOrg>> listOrg(@Validated(SelectGroup.class) @ParameterObject SyncV1Request request,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysDeviceService.listOrg(request);
    }

    /**
     * 检测设备在线情况
     *
     * @param request 密钥
     */
    @Operation(summary = "检测设备在线情况")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    @PutMapping("/check")
    public ReturnResult<List<OnOffline>> check(@Validated(SelectGroup.class) @ParameterObject @RequestBody CheckDeviceOnlineV1Request request,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysDeviceService.check(request);
    }

    /**
     * 获取设备在线情况
     *
     * @param request 密钥
     */
    @Operation(summary = "获取设备在线情况")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    @GetMapping("/timeline")
    public ReturnResult<List<SysDeviceCameraOnOffline>> timeline(@Validated(SelectGroup.class) @ParameterObject DeviceTimelineV1Request request,
                                                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysDeviceService.timeline(request);
    }

    /**
     * 同步设备/组织
     *
     * @param request 密钥
     */
    @Operation(summary = "同步设备/组织")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    @PutMapping("/syncTemplate")
    @ApiOperation("同步设备/组织")
    public ReturnResult<Boolean> syncTemplate(@Validated(SelectGroup.class) @ParameterObject @RequestBody SyncV1Request request,
                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysDeviceService.syncTemplate(request);
    }

    /**
     * 同步设备/组织
     *
     * @param request 密钥
     */
    @Operation(summary = "同步组织")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    @PutMapping("/syncOrg")
    @ApiOperation("同步组织")
    public ReturnResult<Boolean> syncOrg(@Validated(SelectGroup.class) @ParameterObject @RequestBody SyncV1Request request,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysDeviceService.syncOrg(request);
    }

    /**
     * 删除模板
     *
     * @param sysDevice 密钥
     */
    @Operation(summary = "删除模板")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    @PutMapping("/delete")
    @ApiOperation("删除模板")
    public ReturnResult<Boolean> delete(@Validated(SelectGroup.class) @ParameterObject @RequestBody SysDevice sysDevice,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysDeviceService.deleteFor(sysDevice);
    }

    /**
     * 更新模板
     *
     * @param sysDevice 密钥
     */
    @Operation(summary = "更新模板")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    @PutMapping("/update")
    @ApiOperation("更新模板")
    public ReturnResult<Boolean> update(@Validated(UpdateGroup.class) @ParameterObject @RequestBody SysDevice sysDevice,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysDeviceService.updateFor(sysDevice);
    }

    /**
     * 新增模板
     *
     * @param sysDevice 密钥
     */
    @Operation(summary = "新增模板")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    @PostMapping("/save")
    @ApiOperation("新增模板")
    public ReturnResult<SysDevice> save(@Validated(AddGroup.class) @ParameterObject @RequestBody SysDevice sysDevice,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysDeviceService.saveFor(sysDevice);
    }
}
