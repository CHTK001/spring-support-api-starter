package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.DeleteGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.entity.SysAiModule;
import com.chua.starter.system.service.SysAiModuleService;
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
 * ai模型接口
 *
 * @author CH
 * @since 2024/9/4
 */
@RestController
@Slf4j
@Api(tags = "ai模型接口")
@Tag(name = "ai模型接口")
@RequestMapping("/v2/project/ai/module")
@RequiredArgsConstructor
public class SysProjectAiModuleController {

    final SysAiModuleService sysAiModuleService;


    /**
     * 模型列表
     *
     * @param sysProjectId 项目ID
     * @return {@link ReturnResult}<{@link List}<{@link SysAiModule}>>
     */
    @Operation(summary = "模型列表")
    @GetMapping("list")
    public ReturnResult<List<SysAiModule>> list(Integer sysProjectId) {
        return ReturnResult.success(sysAiModuleService.list(sysProjectId));
    }

    /**
     * 模型分页列表
     *
     * @param sysProjectId 项目ID
     * @return {@link ReturnResult}<{@link List}<{@link SysAiModule}>>
     */
    @Operation(summary = "模型分页列表")
    @GetMapping("page")
    public ReturnPageResult<SysAiModule> page(@ParameterObject Query<?> query, Integer sysProjectId) {
        return ReturnPageResultUtils.ok(sysAiModuleService.pageFor(query, sysProjectId));
    }

    /**
     * 更新ai模型
     *
     * @param sysAiModule   ai模块
     * @param bindingResult 绑定结果
     * @return {@link ReturnResult}<{@link Boolean}>
     */
    @Operation(summary = "更新ai模型")
    @PutMapping("update")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> updateForModule(@Validated(UpdateGroup.class) @ParameterObject @RequestBody SysAiModule sysAiModule, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysAiModuleService.updateForModule(sysAiModule);
    }

    @Operation(summary = "添加ai模型")
    @PostMapping("save")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysAiModule> saveForModule(@Validated(AddGroup.class) @ParameterObject @RequestBody SysAiModule sysAiModule, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysAiModuleService.saveForModule(sysAiModule);
    }

    /**
     * 删除ai模型
     *
     * @param sysAiModuleId ai模块ID
     * @param bindingResult 绑定结果
     * @return {@link ReturnResult}<{@link Boolean}>
     */
    @Operation(summary = "删除ai模型")
    @DeleteMapping("delete")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> deleteForModule(@Validated(DeleteGroup.class) Integer sysAiModuleId, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysAiModuleService.deleteForModule(sysAiModuleId);
    }
}
