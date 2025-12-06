package com.chua.starter.project.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.DeleteGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.project.service.SysAiVincentSettingService;
import com.chua.starter.project.entity.SysAiModule;
import com.chua.starter.project.entity.SysAiVincentSetting;
import io.swagger.annotations.Api;
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
 * 文生图模型配置接口
 *
 * @author CH
 * @since 2022/7/29 16:01
 */
@RestController
@Slf4j
@Api(tags = "文生图模型配置接口")
@Tag(name = "文生图模型配置接口")
@RequestMapping("/v2/ai/vincent/setting")
@RequiredArgsConstructor
public class VincentDiagramSettingController {

    final SysAiVincentSettingService sysAiVincentSettingService;

    /**
     * 模型列表
     *
     * @param sysAiModuleId 模型ID
     * @return {@link ReturnResult}<{@link List}<{@link SysAiModule}>>
     */
    @Operation(summary = "模型配置")
    @GetMapping("config")
    public ReturnResult<SysAiVincentSetting> config(String sysAiModuleId) {
        return ReturnResult.success(sysAiVincentSettingService.config(sysAiModuleId));
    }

    /**
     * 更新ai模型
     *
     * @param sysAiVincentSetting ai模块
     * @param bindingResult       绑定结果
     * @return {@link ReturnResult}<{@link Boolean}>
     */
    @Operation(summary = "更新ai模型配置")
    @PutMapping("update")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> updateForModule(@Validated(UpdateGroup.class) @ParameterObject @RequestBody SysAiVincentSetting sysAiVincentSetting,
                                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysAiVincentSettingService.updateForModuleSetting(sysAiVincentSetting);
    }

    @Operation(summary = "添加ai模型配置")
    @PostMapping("save")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysAiModule> saveForModule(@Validated(AddGroup.class) @ParameterObject @RequestBody SysAiVincentSetting sysAiVincentSetting, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysAiVincentSettingService.saveForModuleSetting(sysAiVincentSetting);
    }

    /**
     * 删除ai模型
     *
     * @param sysAiModuleId ai模块ID
     * @param bindingResult 绑定结果
     * @return {@link ReturnResult}<{@link Boolean}>
     */
    @Operation(summary = "删除ai模型配置")
    @DeleteMapping("delete")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> deleteForModule(@Validated(DeleteGroup.class) Integer sysAiModuleId, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysAiVincentSettingService.deleteForModuleSetting(sysAiModuleId);
    }

}
