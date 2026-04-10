package com.chua.starter.system.controller;

import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
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

@Tag(name = "小程序菜单接口")
@RestController
@RequestMapping("/v2/mini-menu")
@RequiredArgsConstructor
public class SysMiniMenuController {

    private final SysMiniMenuService sysMiniMenuService;

    @Operation(summary = "小程序菜单列表")
    @GetMapping("/list")
    public ReturnResult<List<SysMiniMenu>> list() {
        return sysMiniMenuService.listManageMenus();
    }

    @Operation(summary = "删除小程序菜单")
    @DeleteMapping("/delete")
    @ApiPlatform("system")
    @Transactional(rollbackFor = Exception.class)
    @Permission(value = "sys:mini-menu:delete", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<Boolean> delete(Long sysMiniMenuId) {
        return sysMiniMenuService.deleteMenu(sysMiniMenuId);
    }

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
