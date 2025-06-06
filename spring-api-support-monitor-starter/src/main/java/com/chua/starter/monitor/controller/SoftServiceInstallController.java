package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.monitor.entity.SoftServiceInstall;
import com.chua.starter.monitor.service.SoftServiceInstallService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 软件服务安装记录控制器
 *
 * @author CH
 * @since 2024/7/18
 */
@RestController
@RequestMapping("v1/soft/service/install")
@Tag(name = "软件服务安装管理")
@Api(tags = "软件服务安装管理")
@RequiredArgsConstructor
public class SoftServiceInstallController {

    private final SoftServiceInstallService softServiceInstallService;

    /**
     * 根据ID获取安装记录
     *
     * @param installId 安装记录ID
     * @return 安装记录
     */
    @GetMapping("/{installId}")
    @ApiOperation(value = "根据ID获取安装记录")
    public ReturnResult<SoftServiceInstall> getInstallById(@PathVariable Integer installId) {
        return ReturnResult.success(softServiceInstallService.getInstallById(installId));
    }

    /**
     * 根据SSH ID获取安装记录列表
     *
     * @param sshId SSH ID
     * @return 安装记录列表
     */
    @GetMapping("/ssh/{sshId}")
    @ApiOperation(value = "根据SSH ID获取安装记录列表")
    public ReturnResult<List<SoftServiceInstall>> listInstallBySshId(@PathVariable String sshId) {
        return ReturnResult.success(softServiceInstallService.listInstallBySshId(sshId));
    }

    /**
     * 根据软件服务ID获取安装记录列表
     *
     * @param softServiceId 软件服务ID
     * @return 安装记录列表
     */
    @GetMapping("/service/{softServiceId}")
    @ApiOperation(value = "根据软件服务ID获取安装记录列表")
    public ReturnResult<List<SoftServiceInstall>> listInstallBySoftServiceId(@PathVariable Integer softServiceId) {
        return ReturnResult.success(softServiceInstallService.listInstallBySoftServiceId(softServiceId));
    }

    /**
     * 添加安装记录
     *
     * @param softServiceInstall 安装记录
     * @param result             校验结果
     * @return 操作结果
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加安装记录")
    public ReturnResult<Boolean> addInstall(@Validated(AddGroup.class) @RequestBody SoftServiceInstall softServiceInstall, BindingResult result) {
        if (result.hasErrors()) {
            return ReturnResult.illegal(result.getAllErrors().getFirst().getDefaultMessage());
        }
        return softServiceInstallService.addInstall(softServiceInstall);
    }

    /**
     * 更新安装记录
     *
     * @param softServiceInstall 安装记录
     * @param result             校验结果
     * @return 操作结果
     */
    @PutMapping("/update")
    @ApiOperation(value = "更新安装记录")
    public ReturnResult<Boolean> updateInstall(@Validated(UpdateGroup.class) @RequestBody SoftServiceInstall softServiceInstall, BindingResult result) {
        if (result.hasErrors()) {
            return ReturnResult.illegal(result.getAllErrors().getFirst().getDefaultMessage());
        }
        return softServiceInstallService.updateInstall(softServiceInstall);
    }

    /**
     * 删除安装记录
     *
     * @param installId 安装记录ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{installId}")
    @ApiOperation(value = "删除安装记录")
    public ReturnResult<Boolean> removeInstallById(@PathVariable Integer installId) {
        return softServiceInstallService.removeInstallById(installId);
    }

    /**
     * 开始安装软件
     *
     * @param softServiceInstall 安装记录
     * @param result             校验结果
     * @return 操作结果
     */
    @PostMapping("/start")
    @ApiOperation(value = "开始安装软件")
    public ReturnResult<Boolean> startInstall(@Validated(AddGroup.class) @RequestBody SoftServiceInstall softServiceInstall, BindingResult result) {
        if (result.hasErrors()) {
            return ReturnResult.illegal(result.getAllErrors().getFirst().getDefaultMessage());
        }
        return softServiceInstallService.startInstall(softServiceInstall);
    }

    /**
     * 停止安装软件
     *
     * @param installId 安装记录ID
     * @return 操作结果
     */
    @PostMapping("/stop/{installId}")
    @ApiOperation(value = "停止安装软件")
    public ReturnResult<Boolean> stopInstall(@PathVariable Integer installId) {
        return softServiceInstallService.stopInstall(installId);
    }
} 