package com.chua.starter.admin.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.admin.entity.SysMenu;
import com.chua.starter.admin.result.SysMenuTreeResult;
import com.chua.starter.admin.result.UserMenuResult;
import com.chua.starter.admin.service.SysMenuService;
import com.chua.starter.admin.service.SysUserSettingService;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.annotations.UserLogger;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * 菜单控制器
 *
 * @author haoxr
 * @since 2020/11/06
 */
@Tag(name = "菜单接口")
@RestController
@RequestMapping("/v2/menus")
@RequiredArgsConstructor
@Slf4j
public class SysMenuController implements Constant, InitializingBean {

    private final SysMenuService menuService;

    private final SysUserSettingService sysUserSettingService;

    private static final ExecutorService POOL = ThreadUtils.newStaticThreadPool();

    @Operation(summary = "获取我的菜单")
    @GetMapping("/my/**")
    public Result<UserMenuResult> myMenu(@Parameter(hidden = true) @UserValue("userId") String userId,
                                         @Parameter(hidden = true) @UserValue("userDashboardGrid") String userGrid,
                                         @Parameter(hidden = true) @UserValue("userDashboard") String userDashboard,
                                         @Parameter(hidden = true) @UserValue("username") String username,
                                         @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {

        return menuService.myMenu(userId, userGrid, userDashboard, username, roles);
    }
    /**
     * 删除菜单
     *
     * @return 删除菜单
     */
    @UserLogger
    @Operation(summary = "删除菜单")
    @DeleteMapping("delete")
    @OperateLog
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
    @UserLogger
    @OperateLog
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

