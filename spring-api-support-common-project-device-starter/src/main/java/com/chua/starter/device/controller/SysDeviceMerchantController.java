package com.chua.starter.device.controller;

import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.SelectGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.device.pojo.request.SyncV1Request;
import com.chua.starter.device.service.DeviceMerchantService;
import com.chua.starter.entity.project.SysProject;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 独立设备商户接口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "设备商户接口")
@Tag(name = "设备商户接口")
@RequestMapping("/v2/device/merchant")
public class SysDeviceMerchantController {

    private final DeviceMerchantService deviceMerchantService;

    @GetMapping("/page")
    @Operation(summary = "设备商户分页")
    public ReturnPageResult<SysProject> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long pageSize,
                                             @ParameterObject SysProject request) {
        return ReturnPageResultUtils.ok(deviceMerchantService.page(page, pageSize, request));
    }

    @GetMapping("/list")
    @Operation(summary = "设备商户列表")
    public ReturnResult<List<SysProject>> list(@ParameterObject SysProject request) {
        return ReturnResult.ok(deviceMerchantService.list(request));
    }

    @PostMapping("/save")
    @Operation(summary = "新增设备商户")
    public ReturnResult<SysProject> save(@RequestBody @Validated(AddGroup.class) SysProject request) {
        return deviceMerchantService.save(request);
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备商户")
    public ReturnResult<Boolean> update(@RequestBody @Validated(UpdateGroup.class) SysProject request) {
        return deviceMerchantService.update(request);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备商户")
    public ReturnResult<Boolean> delete(@RequestParam Integer sysProjectId) {
        return deviceMerchantService.delete(sysProjectId);
    }

    @GetMapping("/testConnection")
    @Operation(summary = "测试设备商户连接")
    public ReturnResult<Boolean> testConnection(@Valid @Validated(SelectGroup.class) @ParameterObject SyncV1Request request) {
        return deviceMerchantService.testConnection(request.getSysProjectId());
    }
}
