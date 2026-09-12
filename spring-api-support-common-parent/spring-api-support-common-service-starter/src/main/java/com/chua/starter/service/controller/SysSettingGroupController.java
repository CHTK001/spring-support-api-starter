package com.chua.starter.service.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.api.annotations.ApiPlatform;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.service.SysSettingGroupService;
import com.chua.starter.entity.service.SysSettingGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 系统设置接口(缓存)
 *
 * @since 2024/8/7
 * @author CH
 */
@Tag(name = "系统全局设置接口")
@RestController
@RequestMapping("/v2/setting/group")
@RequiredArgsConstructor
public class SysSettingGroupController {

    /** SYS设置分组服务 */
    private final SysSettingGroupService sysSettingGroupService;


    /**
     * 设置分页列表
     *
     * @param sysSettingGroup 条件
     * @return 设置分页列表
     * @param true true
     */
    @Operation(summary = "设置分页列表")
    @GetMapping("/list")
    @ApiPlatform("system")
    @Permission(role = {ADMIN, SUPER_ADMIN})
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'sys:setting:group'")
    public ReturnResult<List<SysSettingGroup>> listForSettingGroup(@ParameterObject SysSettingGroup sysSettingGroup, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return ReturnResult.ok(sysSettingGroupService.listForSettingGroup(sysSettingGroup, roles));
    }

    /**
     * 删除设置
     *
     * @return 更新设置
     */
    @Operation(summary = "删除设置")
    @RequestMapping(value = "/delete", method = {RequestMethod.DELETE, RequestMethod.POST})
    @ApiPlatform("system")
    @Permission(value = "sys:setting:group:delete", role = {ADMIN, SUPER_ADMIN})
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'sys:setting:group'")
    public ReturnResult<Boolean> delete(@Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                                        @RequestParam(value = "sysSettingGroupId", required = false) Long sysSettingGroupId,
                                        @RequestParam(value = "sysSettingGroupMapperId", required = false) Long legacySysSettingGroupId) {
        Long targetGroupId = null != sysSettingGroupId ? sysSettingGroupId : legacySysSettingGroupId;
        if (null == targetGroupId) {
            return ReturnResult.illegal("设置信息不存在");
        }

        return sysSettingGroupService.deleteForGroup(targetGroupId);
    }


    /**
     * 新增设置
     *
     * @return 新增设置
     * @param true true
     */
    @Operation(summary = "更新设置")
    @PostMapping("/saveOrUpdate")
    @ApiPlatform("system")
    @Permission(value = "sys:setting:group:add", role = {ADMIN, SUPER_ADMIN})
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'sys:setting:group'")
    public ReturnResult<Boolean> saveOrUpdate(@Parameter(hidden = true) @UserValue("roles") Set<String> roles, @RequestBody SysSettingGroup sysSettingGroup, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysSettingGroupService.saveOrUpdateForGroup(sysSettingGroup, roles);
    }

    /**
     * 新增设置
     *
     * @return 新增设置
     * @param true true
     */
    @Operation(summary = "更新设置")
    @PostMapping("/batchUpdate")
    @ApiPlatform("system")
    @Permission(value = "sys:setting:group:batchUpdate", role = {ADMIN, SUPER_ADMIN})
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, key = "'sys:setting:group'")
    public ReturnResult<Boolean> batchUpdate(@Parameter(hidden = true) @UserValue("roles") Set<String> roles, @RequestBody List<SysSettingGroup> sysSettingGroups, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysSettingGroupService.batchUpdate(sysSettingGroups, roles);
    }
}
