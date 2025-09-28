package com.chua.starter.service.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.ApiPlatform;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.service.SysLimitConfigurationService;
import com.chua.starter.system.entity.SysLimitConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 限流配置接口
 * 
 * @author CH
 * @since 2025/9/28
 */
@Tag(name = "限流配置接口")
@RestController
@RequestMapping("/v2/limit")
@RequiredArgsConstructor
public class SysLimitConfigurationController {

    private final SysLimitConfigurationService sysLimitConfigurationService;

    /**
     * 限流配置分页列表
     *
     * @param sysLimitConfiguration 条件
     * @return 限流配置分页列表
     */
    @Operation(summary = "限流配置分页列表")
    @GetMapping("/page")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnPageResult<SysLimitConfiguration> pageForLimitConfiguration(
            @ParameterObject Query<SysLimitConfiguration> query, 
            @ParameterObject SysLimitConfiguration sysLimitConfiguration, 
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysLimitConfigurationService.pageForLimitConfiguration(query, sysLimitConfiguration, roles));
    }

    /**
     * 限流配置列表
     *
     * @param sysLimitConfiguration 条件
     * @return 限流配置列表
     */
    @Operation(summary = "限流配置列表")
    @GetMapping("/list")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<List<SysLimitConfiguration>> list(
            @ParameterObject SysLimitConfiguration sysLimitConfiguration, 
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return ReturnResult.ok(sysLimitConfigurationService.list(sysLimitConfiguration, roles));
    }

    /**
     * 删除限流配置
     *
     * @param sysLimitConfigurationId 限流配置ID
     * @return 删除结果
     */
    @Operation(summary = "删除限流配置")
    @DeleteMapping("/delete")
    @ApiPlatform("system")
    @Permission(value = "sys:limit:delete", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long sysLimitConfigurationId) {
        if (null == sysLimitConfigurationId) {
            return Result.illegal("限流配置ID不能为空");
        }

        return sysLimitConfigurationService.delete(sysLimitConfigurationId);
    }

    /**
     * 批量更新限流配置
     *
     * @param sysLimitConfigurations 限流配置列表
     * @param bindingResult 验证结果
     * @return 更新结果
     */
    @Operation(summary = "批量更新限流配置")
    @PutMapping("/updateBatch")
    @ApiPlatform("system")
    @Permission(value = "sys:limit:update", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> updateBatch(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody List<SysLimitConfiguration> sysLimitConfigurations,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysLimitConfigurationService.updateBatch(roles, sysLimitConfigurations);
    }

    /**
     * 更新限流配置
     *
     * @param sysLimitConfiguration 限流配置
     * @param bindingResult 验证结果
     * @return 更新结果
     */
    @Operation(summary = "更新限流配置")
    @PutMapping("/update")
    @ApiPlatform("system")
    @Permission(value = "sys:limit:update", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysLimitConfiguration sysLimitConfiguration,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysLimitConfigurationService.update(roles, sysLimitConfiguration);
    }

    /**
     * 新增限流配置
     *
     * @param sysLimitConfiguration 限流配置
     * @param bindingResult 验证结果
     * @return 新增结果
     */
    @Operation(summary = "新增限流配置")
    @PostMapping("/save")
    @ApiPlatform("system")
    @Permission(value = "sys:limit:add", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SysLimitConfiguration> save(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(AddGroup.class) @RequestBody SysLimitConfiguration sysLimitConfiguration, 
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysLimitConfigurationService.saveForEntity(sysLimitConfiguration, roles);
    }
}