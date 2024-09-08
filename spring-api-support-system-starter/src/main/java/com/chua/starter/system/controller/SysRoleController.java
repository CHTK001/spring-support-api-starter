package com.chua.starter.system.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.SelectGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.entity.SysRole;
import com.chua.starter.system.pojo.role.RoleMenuUpdate;
import com.chua.starter.system.service.SysRoleService;
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

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;
import static com.chua.starter.service.constant.SystemConstant.CACHE_WITH_REDIS;

/**
 * 角色接口
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "角色接口")
@RestController
@RequestMapping("/v2/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;


    /**
     * 角色分页列表
     *
     * @param sysRole 条件
     * @return 角色分页列表
     */
    @Operation(summary = "角色分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysRole> pageForRole(@ParameterObject Query<SysRole> query, @ParameterObject SysRole sysRole, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysRoleService.pageForRole(query, sysRole, roles));
    }
    /**
     * 角色列表
     * @return 角色列表
     */
    @Operation(summary = "角色列表")
    @GetMapping("list")
    @Cacheable(cacheManager = CACHE_WITH_REDIS, cacheNames = CACHE_WITH_REDIS, key = "'cache:role'")
    public ReturnResult<List<SysRole>> list() {
        return Result.ok(sysRoleService.list(
                Wrappers.<SysRole>lambdaQuery().ne(SysRole::getSysRoleCode, SUPER_ADMIN)
                .eq(SysRole::getSysRoleStatus, 1)
                .orderByAsc(SysRole::getSysRoleSort)
        ));
    }
    /**
     * 删除角色
     *
     * @return 更新角色
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/delete")
    @CacheEvict(cacheManager = CACHE_WITH_REDIS, cacheNames = CACHE_WITH_REDIS, key = "'cache:role'")
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:role:delete", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long roleId) {
        if (null == roleId) {
            return Result.illegal("角色信息不存在");
        }

        return sysRoleService.delete(roleId);
    }


    /**
     * 给角色添加菜单
     *
     * @return 更新角色
     */
    @Operation(summary = "给角色添加菜单")
    @PutMapping("/update_menu")
    @Permission(value = "sys:role:menu:update", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> updateMenu(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody RoleMenuUpdate roleMenuUpdate,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysRoleService.updateMenu(roles, roleMenuUpdate);
    }

    @Operation(summary = "获取角色菜单")
    @GetMapping("/get_menu")
    @Permission(value = "sys:role:menu:get", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Set<Integer>> getMenu(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(SelectGroup.class)  RoleMenuUpdate roleMenuUpdate,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysRoleService.getMenu(roleMenuUpdate);
    }
    /**
     * 更新角色
     *
     * @return 更新角色
     */
    @Operation(summary = "更新角色")
    @PutMapping("/update")
    @CacheEvict(cacheManager = CACHE_WITH_REDIS, cacheNames = CACHE_WITH_REDIS, key = "'cache:role'")
    @Permission(value = "sys:role:update", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysRole sysRole,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysRoleService.updateRole(roles, sysRole);
    }

    /**
     * 新增角色
     *
     * @return 新增角色
     */
    @Operation(summary = "新增角色")
    @PostMapping("/save")
    @CacheEvict(cacheManager = CACHE_WITH_REDIS, cacheNames = CACHE_WITH_REDIS, key = "'cache:role'")
    @Permission(value = "sys:role:add", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SysRole> save(@Validated(AddGroup.class) @RequestBody SysRole sysRole, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysRoleService.saveRole(sysRole);
    }
}
