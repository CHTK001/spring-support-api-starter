package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.ApiCacheKey;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.entity.SysTemplateCategory;
import com.chua.starter.system.pojo.category.SysTemplateCategoryTree;
import com.chua.starter.system.service.SysTemplateCategoryService;
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

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 类别接口(缓存)
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "模板类别接口")
@RestController
@RequestMapping("/v2/template/category")
@RequiredArgsConstructor
public class SysTemplateCategoryController {

    private final SysTemplateCategoryService sysTemplateCategoryService;
    /**
     * 类别树
     *
     * @param sysTemplateCategory 条件
     * @return 类别分页列表
     */
    @Operation(summary = "类别树")
    @GetMapping("/tree")
    @ApiCacheKey("'sys:category:all'")
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public ReturnResult<List<SysTemplateCategoryTree>> tree(@ParameterObject Query<SysTemplateCategory> query, @ParameterObject SysTemplateCategory sysTemplateCategory, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return sysTemplateCategoryService.tree(query, sysTemplateCategory, roles);
    }

    /**
     * 类别分页列表
     *
     * @param sysTemplateCategory 条件
     * @return 类别分页列表
     */
    @Operation(summary = "类别分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysTemplateCategory> pageForRole(@ParameterObject Query<SysTemplateCategory> query, @ParameterObject SysTemplateCategory sysTemplateCategory, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysTemplateCategoryService.page(query, sysTemplateCategory, roles));
    }

    /**
     * 删除类别
     *
     * @return 更新类别
     */
    @Operation(summary = "删除类别")
    @DeleteMapping("/delete")
    @Transactional(rollbackOn = Exception.class)
    @ApiCacheKey("'sys:category:all'")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    @Permission(value = "sys:template:category:delete", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long sysTemplateCategoryId) {
        if (null == sysTemplateCategoryId) {
            return Result.illegal("类别信息不存在");
        }

        return sysTemplateCategoryService.delete(sysTemplateCategoryId);
    }


    /**
     * 更新类别
     *
     * @return 更新类别
     */
    @Operation(summary = "更新类别")
    @PutMapping("/update")
    @ApiCacheKey("'sys:category:all'")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    @Permission(value = "sys:template:category:update", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysTemplateCategory sysTemplateCategory,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysTemplateCategoryService.update(roles, sysTemplateCategory);
    }

    /**
     * 还原类别
     *
     * @return 更新类别
     */
    @Operation(summary = "还原类别")
    @PutMapping("/rollback")
    @Permission(value = "sys:template:category:update", role = {ADMIN, SUPER_ADMIN})
    @ApiCacheKey("'sys:category:all'")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public ReturnResult<Boolean> rollback(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysTemplateCategory sysTemplateCategory,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysTemplateCategoryService.rollback(roles, sysTemplateCategory);
    }

    /**
     * 新增类别
     *
     * @return 新增类别
     */
    @Operation(summary = "新增类别")
    @PostMapping("/save")
    @ApiCacheKey("'sys:category:all'")
    @CacheEvict(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    @Permission(value = "sys:template:category:add", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SysTemplateCategory> save(
                @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                @Validated(AddGroup.class) @RequestBody SysTemplateCategory sysTemplateCategory,
                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysTemplateCategoryService.save(sysTemplateCategory, roles);
    }
}
