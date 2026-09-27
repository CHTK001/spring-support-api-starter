package com.chua.starter.system.controller;

import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.validator.group.AddGroup;
import com.chua.starter.system.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLog;
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
 * @since 2024/8/7
 * @author CH
 */
@Tag(name = "菜单接口")
@RestController
@RequestMapping("/v2/menu")
@RequiredArgsConstructor
public class SysMenuController {

    /**
     * 菜单位置：标准（侧边栏）
     */
    private static final String POSITION_STANDARD = "standard";

    /**
     * 菜单位置：顶部（顶栏按钮，强制 remind=1）
     */
    private static final String POSITION_TOP = "top";

    /**
     * sysmenu服务
    */
    private final SysMenuService sysMenuService;
    /**
     * 菜单列表
     *
     * @param sysMenu 条件
     * @return 菜单分页列表
     * @param true true
     * @param roles 方法入参 roles
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
     * @param true true
     * @param roles 方法入参 roles
     */
    @Operation(summary = "菜单列表")
    @GetMapping("/list")
    public ReturnResult<List<SysMenuSettingVO>> list(@ParameterObject SysMenu sysMenu, @Parameter(hidden = true) @UserValue("roles") Set<String> roles, @Parameter(hidden = true) @UserValue("username") String username) {
        return sysMenuService.listMenu(sysMenu, roles, username);
    }

    /**
     * 菜单分页列表
     *
     * @param sysMenu 条件
     * @return 菜单分页列表
     * @param query 查询
     * @param true true
     * @param roles 方法入参 roles
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
     * @param sysMenuId sysMenuId
     */
    @Operation(summary = "删除菜单")
    @DeleteMapping("/delete")
    @SysLog
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
    @SysLog
    @ApiPlatform("system")
    @Permission(value = "sys:menu:update", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysMenu sysMenu,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        normalizeMenuPosition(sysMenu);

        return sysMenuService.update(roles, sysMenu);
    }

    /**
     * 新增菜单
     *
     * @return 新增菜单
     */
    @Operation(summary = "新增菜单")
    @PostMapping("/save")
    @SysLog
    @ApiPlatform("system")
    @Permission(value = "sys:menu:add", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<SysMenu> save(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(AddGroup.class) @RequestBody SysMenu sysMenu,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        normalizeMenuPosition(sysMenu);

        return sysMenuService.save(sysMenu, roles);
    }

    /**
     * 归一化菜单位置：默认 standard；top(顶部) 强制脱离菜单控制（remind=1 且不可修改）
     *
     * @param sysMenu 菜单
     */
    private void normalizeMenuPosition(SysMenu sysMenu) {
        if (null == sysMenu) {
            return;
        }
        String position = sysMenu.getSysMenuPosition();
        if (null == position || position.isBlank()) {
            sysMenu.setSysMenuPosition(POSITION_STANDARD);
            return;
        }
        if (POSITION_TOP.equalsIgnoreCase(position.trim())) {
            sysMenu.setSysMenuPosition(POSITION_TOP);
            sysMenu.setSysMenuRemind(1);
            return;
        }
        sysMenu.setSysMenuPosition(POSITION_STANDARD);
    }

    /**
     * refresh缓存。
     *
     * @return Return结果 对象
     */
    @Operation(summary = "刷新菜单缓存")
    @DeleteMapping("/cache/refresh")
    @SysLog(module = "清缓存")
    @Permission(value = "sys:menu:edit", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<Boolean> refreshCache() {
        return sysMenuService.refreshCache();
    }

    /**
     * 批量保存按钮(权限)
     *
     * @param menus 按钮列表
     * @return 操作结果
     */
    @Operation(summary = "批量保存按钮")
    @PostMapping("/button/batch-save")
    @SysLog
    @Permission(value = "sys:menu:button:add", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<Boolean> batchSaveButtons(@RequestBody List<SysMenu> menus) {
        return sysMenuService.batchSaveButtons(menus);
    }

    /**
     * 批量删除按钮
     *
     * @param menuIds 按钮ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除按钮")
    @DeleteMapping("/button/batch-delete")
    @SysLog
    @Permission(value = "sys:menu:button:delete", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<Boolean> batchDeleteButtons(@RequestBody List<Integer> menuIds) {
        return sysMenuService.batchDeleteButtons(menuIds);
    }
}
