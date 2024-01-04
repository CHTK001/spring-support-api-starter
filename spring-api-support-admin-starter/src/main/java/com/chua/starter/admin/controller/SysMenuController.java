package com.chua.starter.admin.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.logger.Logger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.admin.entity.SysMenu;
import com.chua.starter.admin.entity.SysUserGrid;
import com.chua.starter.admin.result.SysMenuTreeResult;
import com.chua.starter.admin.result.SysUserGridResult;
import com.chua.starter.admin.result.UserMenuResult;
import com.chua.starter.admin.service.SysMenuService;
import com.chua.starter.admin.service.SysUserGridService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * 菜单控制器
 *
 * @author haoxr
 * @since 2020/11/06
 */
@Api(tags = "菜单接口")
@RestController
@RequestMapping("/v2/menus")
@RequiredArgsConstructor
@Slf4j
public class SysMenuController implements Constant, InitializingBean {

    private final SysMenuService menuService;

    private final SysUserGridService sysUserGridService;

    private static final ExecutorService POOL = ThreadUtils.newStaticThreadPool();

    @Operation(summary = "获取我的菜单")
    @GetMapping("/my/**")
    public Result<UserMenuResult> myMenu(@ApiParam(hidden = true) @UserValue("userId") String userId,
                                         @ApiParam(hidden = true) @UserValue("userDashboardGrid") String userGrid,
                                         @ApiParam(hidden = true) @UserValue("userDashboard") String userDashboard,
                                         @ApiParam(hidden = true) @UserValue("username") String username,
                                         @ApiParam(hidden = true) @UserValue("roles") Set<String> roles) {
        if(roles.isEmpty() && !ADMIN.equalsIgnoreCase(username)) {
            return Result.failed("联系管理员分配权限");
        }
        UserMenuResult userMenuResult = new UserMenuResult();
        userMenuResult.setPermissions(AuthClientExecute.getInstance().getUserResult().getPermission());
        List<SysMenu> sysMenus = menuService.roleMenu();
        if (ADMIN.equalsIgnoreCase(username)) {
            roles = new HashSet<>(roles);
            roles.add(ADMIN);
        }

        userMenuResult.setMenu(menuService.myMenu(sysMenus, roles));
        userMenuResult.setDashboardGrid(Splitter.on(',').trimResults().omitEmptyStrings().splitToSet(userGrid));
        userMenuResult.setDashboard(userDashboard);
        SysUserGridResult result = new SysUserGridResult();
        SysUserGrid gridByUserId = sysUserGridService.getGridByUserId(userId);
        try {
            result.setLayout(JSON.parseArray(gridByUserId.getLayout()));
        } catch (Exception e) {
            result.setLayout(new JSONArray());
        }
        try {
            result.setCopmsList(JSON.parseArray(gridByUserId.getCopmsList()));
        } catch (Exception ignored) {
        }
        userMenuResult.setGrid(result);
        return Result.success(userMenuResult);
    }
    /**
     * 删除菜单
     *
     * @return 删除菜单
     */
    @Logger
    @Operation(summary = "删除菜单")
    @DeleteMapping("delete")
    public ReturnResult<Boolean> delete(String menuId) {
        if(StringUtils.isEmpty(menuId)) {
            return Result.illegal("菜单不存在");
        }
        return Result.success(menuService.deleteMenu(menuId));
    }
    /**
     * 更新菜单
     *
     * @return 更新菜单
     */
    @Operation(summary = "更新菜单")
    @PutMapping("update")
    @Logger
    public Result<Boolean> update(@RequestBody SysMenuTreeResult sysMenu) {
        return Result.success(menuService.updateMenu(sysMenu.toSysMenu()));
    }
    /**
     * 新增菜单
     *
     * @return 新增菜单
     */
    @Operation(summary = "新增菜单")
    @PostMapping("save")
    public Result<SysMenu> save(@RequestBody SysMenuTreeResult sysMenu) {
        return Result.success(menuService.saveMenu(sysMenu.toSysMenu()));
    }

    /**
     * 获取菜单
     *
     * @return 获取菜单
     */
    @Operation(summary = "获取菜单")
    @GetMapping("list")
    public Result<List<SysMenu>> list() {
        return Result.success(menuService.roleMenu());
    }

    /**
     * 获取菜单
     *
     * @return 获取菜单
     */
    @Operation(summary = "获取菜单")
    @GetMapping("tree")
    public Result<List<SysMenuTreeResult>> tree() {
        return Result.success(menuService.roleMenuTree());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        POOL.execute(() -> {
            try {
                menuService.roleMenu();
            } catch (Exception ignored) {
            }
        });
    }
}

