package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.UserLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.service.query.SysUserQuery;
import com.chua.starter.service.system.service.SysUserService;
import com.chua.starter.system.entity.SysUser;
import com.chua.starter.system.pojo.menu.SysMenuVO;
import com.chua.starter.system.pojo.user.SysUserPasswordUpdate;
import com.chua.starter.system.pojo.user.SysUserUpdate;
import com.chua.starter.system.pojo.user.SysUserVO;
import com.chua.starter.system.pojo.user.UserLayout;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * 用户接口
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "用户接口")
@RestController
@RequestMapping("/v2/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 用户分页列表
     *
     * @param sysUserQuery 条件
     * @return 用户分页列表
     */
    @Operation(summary = "用户分页列表")
    @GetMapping("/page")
    @Permission("sys:user:page")
    public ReturnPageResult<SysUserVO> pageForUser(@ParameterObject SysUserQuery sysUserQuery, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysUserService.pageForUser(sysUserQuery, roles));
    }


    /**
     * 获取当前用户布局
     *
     * @return 获取当前用户布局
     */
    @Operation(summary = "获取当前用户布局")
    @GetMapping("/layout")
    public ReturnResult<UserLayout> getUserLayout(
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return ReturnResult.ok(sysUserService.getUserLayout(userId, roles));
    }
    /**
     * 保存当前用户布局
     *
     * @return 更新用户
     */
    @Operation(summary = "保存当前用户布局")
    @PutMapping("/layout")
    public ReturnResult<UserLayout> saveUserLayout(
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId, @RequestBody UserLayout userLayout) {
        return ReturnResult.ok(sysUserService.saveUserLayout(userId, userLayout));
    }
    /**
     * 获取当前登录用户菜单
     *
     * @return 获取当前登录用户菜单
     */
    @Operation(summary = "获取当前登录用户菜单")
    @GetMapping("/menu")
    public ReturnResult<List<SysMenuVO>> getUserMenu(@Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return sysUserService.getUserMenu(roles);
    }

    /**
     * 删除用户
     *
     * @return 更新用户
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/delete")
    @OperateLog
    @UserLogger
    @Transactional(rollbackOn = Exception.class)
    @Permission("sys:user:delete")
    public ReturnResult<Boolean> delete(Long sysUserId) {
        if (null == sysUserId) {
            return Result.illegal("用户信息不存在");
        }

        return sysUserService.delete(sysUserId);
    }

    /**
     * 重置自身密碼
     *
     * @param userId 用戶ID
     * @return 重置用户密碼
     */
    @Operation(summary = "重置当前账号密碼")
    @GetMapping("/resetOwner")
    @OperateLog
    @Transactional(rollbackOn = Exception.class)
    public ReturnResult<SysUser> resetOwner(@Parameter(hidden = true) @UserValue("sysUserId") Long userId) {
        if (null == userId) {
            return Result.illegal("请重新登录或者联系管理员");
        }

        return sysUserService.resetOwner(userId);
    }

    /**
     * 重置用户密碼
     *
     * @param userId 用戶ID
     * @return 重置用户密碼
     */
    @Operation(summary = "重置用户密碼")
    @GetMapping("/reset")
    @OperateLog
    @Permission(value = "sys:user:reset", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    @Transactional(rollbackOn = Exception.class)
    public ReturnResult<String> reset(Long userId) {
        if (null == userId) {
            return Result.illegal("用户信息不存在");
        }

        return sysUserService.reset(userId);
    }

    /**
     * 更新当前账号密码
     *
     * @return 更新用户
     */
    @Operation(summary = "更新用户自身密码")
    @PutMapping("/updateOwner")
    @OperateLog
    public ReturnResult<SysUser> updateOwner(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @Validated(UpdateGroup.class) @RequestBody SysUserPasswordUpdate sysUserPasswordUpdate,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysUserService.updateOwner(roles, userId, sysUserPasswordUpdate);
    }
    /**
     * 更新用户
     *
     * @return 更新用户
     */
    @Operation(summary = "更新用户")
    @PutMapping("/update")
    @OperateLog
    public ReturnResult<SysUser> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @Validated(UpdateGroup.class) @RequestBody SysUserUpdate sysUser,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysUserService.updateUser(roles, userId, sysUser);
    }

    /**
     * 新增用户
     *
     * @return 新增用户
     */
    @Operation(summary = "新增用户")
    @PostMapping("/save")
    @OperateLog
    @Permission(value = "sys:user:add", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysUser> save(@Validated(AddGroup.class) @RequestBody SysUserUpdate sysUserUpdate, BindingResult bindingResult, @Parameter(hidden = true) HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysUserService.saveUser(sysUserUpdate, request);
    }
}
