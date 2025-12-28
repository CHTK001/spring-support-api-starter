package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.api.annotations.ApiPlatform;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.entity.system.SysMenu;
import com.chua.starter.system.pojo.menu.SysMenuSettingVO;
import com.chua.starter.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 菜单接口
 *
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "菜单接口")
@RestController
@RequestMapping("/v2/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;
    /**
     * 菜单列表
     *
     * @param sysMenu 条件
     * @return 菜单分页列表
     */
    @Operation(summary = "菜单列表")
    @GetMapping("/all")
    public ReturnResult<List<SysMenu>> all(@ParameterObject SysMenu sysMenu, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return sysMenuService.all(sysMenu, roles);
    }

    /**
     * 菜单列表
     *
     * @param sysMenu 条件
     * @return 菜单分页列表
     */
    @Operation(summary = "菜单列表")
    @GetMapping("/list")
    public ReturnResult<List<SysMenuSettingVO>> list(@ParameterObject SysMenu sysMenu, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return sysMenuService.listMenu(sysMenu, roles);
    }

    /**
     * 菜单分页列表
     *
     * @param sysMenu 条件
     * @return 菜单分页列表
     */
    @Operation(summary = "菜单分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysMenu> pageForRole(@ParameterObject Query<SysMenu> query, @ParameterObject SysMenu sysMenu, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysMenuService.pageForMenu(query, sysMenu, roles));
    }

    /**
     * 删除菜单
     *
     * @return 更新菜单
     */
    @Operation(summary = "删除菜单")
    @DeleteMapping("/delete")
    @ApiPlatform("system")
    @Transactional(rollbackFor = Exception.class)
    @Permission(value = "sys:menu:delete", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<Boolean> delete(Long sysMenuId) {
        if (null == sysMenuId) {
            return ReturnResult.illegal("菜单信息不存在");
        }

        return sysMenuService.delete(sysMenuId);
    }


    /**
     * 更新菜单
     *
     * @return 更新菜单
     */
    @Operation(summary = "更新菜单")
    @PutMapping("/update")
    @ApiPlatform("system")
    @Permission(value = "sys:menu:update", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysMenu sysMenu,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysMenuService.update(roles, sysMenu);
    }

    /**
     * 新增菜单
     *
     * @return 新增菜单
     */
    @Operation(summary = "新增菜单")
    @PostMapping("/save")
    @ApiPlatform("system")
    @Permission(value = "sys:menu:add", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<SysMenu> save(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(AddGroup.class) @RequestBody SysMenu sysMenu,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysMenuService.save(sysMenu, roles);
    }
}
