package com.chua.starter.system.controller;

import com.chua.starter.system.validator.group.AddGroup;
import com.chua.starter.system.validator.group.UpdateGroup;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.api.annotations.ApiPlatform;
import com.chua.starter.entity.system.SysMiniMenu;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.service.SysMiniMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 小程序菜单接口
 *
 * @author CH
 * @since 2026/08/03
 */
@Tag(name = "小程序菜单接口")
@RestController
@RequestMapping("/v2/mini-menu")
@RequiredArgsConstructor
public class SysMiniMenuController {

    /**
     * 小程序菜单服务
     */
    private final SysMiniMenuService sysMiniMenuService;

    /**
     * 查询后端管理用的小程序菜单列表。
     *
     * @return 菜单列表结果
     */
    @Operation(summary = "小程序菜单列表")
    @GetMapping("/list")
    public ReturnResult<List<SysMiniMenu>> list() {
        return sysMiniMenuService.listManageMenus();
    }

    /**
     * 删除小程序菜单。
     *
     * @param sysMiniMenuId 菜单ID
     * @return 删除结果
     */
    @Operation(summary = "删除小程序菜单")
    @DeleteMapping("/delete")
    @ApiPlatform("system")
    @Transactional(rollbackFor = Exception.class)
    @Permission(value = "sys:mini-menu:delete", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<Boolean> delete(Long sysMiniMenuId) {
        return sysMiniMenuService.deleteMenu(sysMiniMenuId);
    }

    /**
     * 更新小程序菜单。
     *
     * @param roles         用户角色集合
     * @param sysMiniMenu   小程序菜单实体
     * @param bindingResult 参数校验结果
     * @return 更新结果
     */
    @Operation(summary = "更新小程序菜单")
    @PutMapping("/update")
    @ApiPlatform("system")
    @Permission(value = "sys:mini-menu:update", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysMiniMenu sysMiniMenu,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysMiniMenuService.updateMenu(roles, sysMiniMenu);
    }

    /**
     * 新增小程序菜单。
     *
     * @param roles         用户角色集合
     * @param sysMiniMenu   小程序菜单实体
     * @param bindingResult 参数校验结果
     * @return 保存结果
     */
    @Operation(summary = "新增小程序菜单")
    @PostMapping("/save")
    @ApiPlatform("system")
    @Permission(value = "sys:mini-menu:add", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<SysMiniMenu> save(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(AddGroup.class) @RequestBody SysMiniMenu sysMiniMenu,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }
        return sysMiniMenuService.saveMenu(sysMiniMenu, roles);
    }
}
