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
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.service.constant.SystemConstant;
import com.chua.starter.service.model.SysRole;
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

/**
 * 瑙掕壊鎺ュ彛
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "瑙掕壊鎺ュ彛")
@RestController
@RequestMapping("/v2/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;


    /**
     * 瑙掕壊鍒嗛〉鍒楄〃
     *
     * @param sysRole 鏉′欢
     * @return 瑙掕壊鍒嗛〉鍒楄〃
     */
    @Operation(summary = "瑙掕壊鍒嗛〉鍒楄〃")
    @GetMapping("/page")
    public ReturnPageResult<SysRole> pageForRole(@ParameterObject Query<SysRole> query, @ParameterObject SysRole sysRole, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysRoleService.pageForRole(query, sysRole, roles));
    }
    /**
     * 瑙掕壊鍒楄〃
     * @return 瑙掕壊鍒楄〃
     */
    @Operation(summary = "瑙掕壊鍒楄〃")
    @GetMapping("list")
    @Cacheable(cacheManager = SystemConstant.CACHE_WITH_REDIS, cacheNames = SystemConstant.CACHE_WITH_REDIS, key = "'cache:role'")
    public ReturnResult<List<SysRole>> list() {
        return Result.ok(sysRoleService.list(
                Wrappers.<SysRole>lambdaQuery().ne(SysRole::getSysRoleCode, AuthConstant.SUPER_ADMIN)
                .eq(SysRole::getSysRoleStatus, 1)
                .orderByAsc(SysRole::getSysRoleSort)
        ));
    }
    /**
     * 鍒犻櫎瑙掕壊
     *
     * @return 鏇存柊瑙掕壊
     */
    @Operation(summary = "鍒犻櫎瑙掕壊")
    @DeleteMapping("/delete")
    @CacheEvict(cacheManager = SystemConstant.CACHE_WITH_REDIS, cacheNames = SystemConstant.CACHE_WITH_REDIS, key = "'cache:role'")
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:role:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> delete(Long roleId) {
        if (null == roleId) {
            return Result.illegal("瑙掕壊淇℃伅涓嶅瓨鍦");
        }

        return sysRoleService.delete(roleId);
    }


    /**
     * 缁欒鑹叉坊鍔犺彍鍗"
     *
     * @return 鏇存柊瑙掕壊
     */
    @Operation(summary = "缁欒鑹叉坊鍔犺彍鍗")
    @PutMapping("/update_menu")
    @Permission(value = "sys:role:menu:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> updateMenu(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody RoleMenuUpdate roleMenuUpdate,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysRoleService.updateMenu(roles, roleMenuUpdate);
    }

    @Operation(summary = "鑾峰彇瑙掕壊鑿滃崟")
    @GetMapping("/get_menu")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Set<Integer>> getMenu(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(SelectGroup.class)  RoleMenuUpdate roleMenuUpdate,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysRoleService.getMenu(roleMenuUpdate);
    }
    /**
     * 鏇存柊瑙掕壊
     *
     * @return 鏇存柊瑙掕壊
     */
    @Operation(summary = "鏇存柊瑙掕壊")
    @PutMapping("/update")
    @CacheEvict(cacheManager = SystemConstant.CACHE_WITH_REDIS, cacheNames = SystemConstant.CACHE_WITH_REDIS, key = "'cache:role'")
    @Permission(value = "sys:role:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysRole sysRole,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysRoleService.updateRole(roles, sysRole);
    }

    /**
     * 鏂板瑙掕壊
     *
     * @return 鏂板瑙掕壊
     */
    @Operation(summary = "鏂板瑙掕壊")
    @PostMapping("/save")
    @CacheEvict(cacheManager = SystemConstant.CACHE_WITH_REDIS, cacheNames = SystemConstant.CACHE_WITH_REDIS, key = "'cache:role'")
    @Permission(value = "sys:role:add", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysRole> save(@Validated(AddGroup.class) @RequestBody SysRole sysRole, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysRoleService.saveRole(sysRole);
    }
}
