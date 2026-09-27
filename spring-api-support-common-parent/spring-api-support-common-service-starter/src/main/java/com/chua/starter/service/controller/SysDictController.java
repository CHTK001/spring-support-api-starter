package com.chua.starter.service.controller;
import com.chua.starter.common.support.annotations.SysLog;

import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.entity.service.SysDict;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

/**
 * 字典接口
 * @since 2024/8/7
 * @author CH
 */
@Tag(name = "字典接口")
@RestController
@RequestMapping("/v2/dict")
@RequiredArgsConstructor
public class SysDictController {

    /**
     * sysdict服务
    */
    private final SysDictService sysDictService;


    /**
     * 字典分页列表
     *
     * @param sysDict 条件
     * @return 字典分页列表
     */
    @Operation(summary = "字典分页列表")
    @GetMapping("/page")
    @Cacheable(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS,
            key = "'sys:dict:page:' + #userId + ':' + T(com.chua.common.support.utils.ObjectUtils).defaultIfNull(#sysDict.sysDictCode,'') + ':' + T(com.chua.common.support.utils.ObjectUtils).defaultIfNull(#sysDict.sysDictName,'') + ':' + T(com.chua.common.support.utils.ObjectUtils).defaultIfNull(#query.page,1) + ':' + T(com.chua.common.support.utils.ObjectUtils).defaultIfNull(#query.pageSize,10)",
            unless = "#result == null || #result.data == null || #result.data.data == null || #result.data.data.isEmpty()")
    /**
     * pagefor角色
     *
     * @param query 查询
     * @param sysDict sysDict
     * @param true true
     * @return pagefor角色的结果
     * @param roles 方法入参 roles
     * @param userId 用户ID，不允许为 null
     */
    public ReturnPageResult<SysDict> pageForRole(@ParameterObject Query<SysDict> query, @ParameterObject SysDict sysDict, @Parameter(hidden = true) @UserValue("roles") Set<String> roles, @Parameter(hidden = true) @UserValue("sysUserId") Long userId) {
        return PageResultUtils.ok(sysDictService.pageForDict(query, sysDict, roles));
    }

    /**
     * 字典列表（不分页，带子级）
     *
     * @param sysDict 条件
     * @return 字典列表的结果
     */
    @Operation(summary = "字典列表")
    @GetMapping("/list")
    public ReturnResult<List<SysDict>> list(@ParameterObject SysDict sysDict) {
        return ReturnResult.ok(sysDictService.listForDict(sysDict));
    }

    /**
     * 删除字典
     *
     * @return 更新字典
     * @param sysDictId sysDictId
     */
    @Operation(summary = "删除字典")
    @DeleteMapping("/delete")
    @SysLog
    @Transactional(rollbackFor = Exception.class)
    @Permission(value = "sys:dict:delete", role = {"ADMIN", "SUPER_ADMIN"})
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, allEntries = true, beforeInvocation = false)
    public ReturnResult<Boolean> delete(Long sysDictId) {
        if (null == sysDictId) {
            return ReturnResult.illegal("字典信息不存在");
        }

        return sysDictService.delete(sysDictId);
    }


    /**
     * 更新字典
     *
     * @return 更新字典
     */
    @Operation(summary = "更新字典")
    @PutMapping("/update")
    @SysLog
    @Permission(value = "sys:dict:update", role = {"ADMIN", "SUPER_ADMIN"})
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, allEntries = true)
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysDict sysDict,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysDictService.update(roles, sysDict);
    }

    /**
     * 新增字典
     *
     * @return 新增字典
     */
    @Operation(summary = "新增字典")
    @PostMapping("/save")
    @SysLog
    @Permission(value = "sys:dict:add", role = {"ADMIN", "SUPER_ADMIN"})
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, allEntries = true)
    public ReturnResult<SysDict> save(
                @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                @Validated(AddGroup.class) @RequestBody SysDict sysDict,
                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysDictService.save(sysDict, roles);
    }

    /**
     * 刷新字典缓存
     * 清空Redis中字典相关缓存，下次查询重新加载
     *
     * @return 刷新结果
     */
    @Operation(summary = "刷新字典缓存")
    @DeleteMapping("/cache/refresh")
    @Permission(value = "sys:dict:update", role = {"ADMIN", "SUPER_ADMIN"})
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, allEntries = true)
    public ReturnResult<Boolean> refreshCache() {
        return ReturnResult.ok(Boolean.TRUE);
    }

    /**
     * 批量更新字典分类排序
     */
    @Operation(summary = "批量更新字典分类排序")
    @PutMapping("/update-sort")
    @SysLog
    @Permission(value = "sys:dict:update", role = {"ADMIN", "SUPER_ADMIN"})
    @CacheEvict(cacheManager = CacheConstant.REDIS_CACHE_ALWAYS, cacheNames = CacheConstant.REDIS_CACHE_ALWAYS, allEntries = true)
    public ReturnResult<Boolean> updateSort(@RequestBody List<SysDict> list) {
        if (null == list || list.isEmpty()) {
            return ReturnResult.illegal("排序数据不能为空");
        }
        sysDictService.updateBatchById(list);
        return ReturnResult.ok(true);
    }
}
