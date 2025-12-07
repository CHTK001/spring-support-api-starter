package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.ApiPlatform;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.entity.SysMenu;
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

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

/**
 * 鑿滃崟鎺ュ彛
 *
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "鑿滃崟鎺ュ彛")
@RestController
@RequestMapping("/v2/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;
    /**
     * 鑿滃崟鍒楄〃
     *
     * @param sysMenu 鏉′欢
     * @return 鑿滃崟鍒嗛〉鍒楄〃
     */
    @Operation(summary = "鑿滃崟鍒楄〃")
    @GetMapping("/all")
    public ReturnResult<List<SysMenu>> all(@ParameterObject SysMenu sysMenu, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return sysMenuService.all(sysMenu, roles);
    }

    /**
     * 鑿滃崟鍒楄〃
     *
     * @param sysMenu 鏉′欢
     * @return 鑿滃崟鍒嗛〉鍒楄〃
     */
    @Operation(summary = "鑿滃崟鍒楄〃")
    @GetMapping("/list")
    public ReturnResult<List<SysMenuSettingVO>> list(@ParameterObject SysMenu sysMenu, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return sysMenuService.listMenu(sysMenu, roles);
    }

    /**
     * 鑿滃崟鍒嗛〉鍒楄〃
     *
     * @param sysMenu 鏉′欢
     * @return 鑿滃崟鍒嗛〉鍒楄〃
     */
    @Operation(summary = "鑿滃崟鍒嗛〉鍒楄〃")
    @GetMapping("/page")
    public ReturnPageResult<SysMenu> pageForRole(@ParameterObject Query<SysMenu> query, @ParameterObject SysMenu sysMenu, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysMenuService.pageForMenu(query, sysMenu, roles));
    }

    /**
     * 鍒犻櫎鑿滃崟
     *
     * @return 鏇存柊鑿滃崟
     */
    @Operation(summary = "鍒犻櫎鑿滃崟")
    @DeleteMapping("/delete")
    @ApiPlatform("system")
    @Transactional(rollbackOn = Exception.class)
    @Permission(value = "sys:menu:delete", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<Boolean> delete(Long sysMenuId) {
        if (null == sysMenuId) {
            return Result.illegal("鑿滃崟淇℃伅涓嶅瓨鍦?);
        }

        return sysMenuService.delete(sysMenuId);
    }


    /**
     * 鏇存柊鑿滃崟
     *
     * @return 鏇存柊鑿滃崟
     */
    @Operation(summary = "鏇存柊鑿滃崟")
    @PutMapping("/update")
    @ApiPlatform("system")
    @Permission(value = "sys:menu:update", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<Boolean> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(UpdateGroup.class) @RequestBody SysMenu sysMenu,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysMenuService.update(roles, sysMenu);
    }

    /**
     * 鏂板鑿滃崟
     *
     * @return 鏂板鑿滃崟
     */
    @Operation(summary = "鏂板鑿滃崟")
    @PostMapping("/save")
    @ApiPlatform("system")
    @Permission(value = "sys:menu:add", role = {SUPER_ADMIN, ADMIN})
    public ReturnResult<SysMenu> save(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Validated(AddGroup.class) @RequestBody SysMenu sysMenu,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysMenuService.save(sysMenu, roles);
    }
}
