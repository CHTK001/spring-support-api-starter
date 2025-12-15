package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.common.support.annotations.UserLogger;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.model.user.SysUserQuery;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.system.pojo.user.SysUserPasswordUpdate;
import com.chua.starter.system.pojo.user.SysUserUpdate;
import com.chua.starter.system.pojo.user.SysUserVO;
import com.chua.starter.system.pojo.user.UserLayout;
import com.chua.starter.system.pojo.user.SysUserImportVO;
import com.chua.starter.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;

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
    @Permission(role = {ADMIN})
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
     * 删除用户
     *
     * @return 更新用户
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/delete")
    @SysLog
    @UserLogger
    @Transactional(rollbackOn = Exception.class)
    @Permission("sys:login:delete")
    public ReturnResult<Boolean> delete(Long sysUserId) {
        if (null == sysUserId) {
            return ReturnResult.illegal("用户信息不存在");
        }

        return sysUserService.delete(sysUserId);
    }

    /**
     * 重置自身密码
     *
     * @param userId 用户ID
     * @return 重置用户密码
     */
    @Operation(summary = "重置当前账号密码")
    @GetMapping("/resetOwner")
    @SysLog
    @Transactional(rollbackOn = Exception.class)
    public ReturnResult<SysUser> resetOwner(@Parameter(hidden = true) @UserValue("sysUserId") Long userId) {
        if (null == userId) {
            return ReturnResult.illegal("请重新登录或者联系管理员");
        }

        return sysUserService.resetOwner(userId);
    }

    /**
     * 重置用户密码
     *
     * @param userId 用户ID
     * @return 重置用户密码
     */
    @Operation(summary = "重置用户密码")
    @GetMapping("/reset")
    @SysLog
    @Permission(value = "sys:login:reset", role = {ADMIN, AuthConstant.SUPER_ADMIN})
    @Transactional(rollbackOn = Exception.class)
    public ReturnResult<String> reset(Long userId) {
        if (null == userId) {
            return ReturnResult.illegal("用户信息不存在");
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
    @SysLog
    public ReturnResult<SysUser> updateOwner(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @Validated(UpdateGroup.class) @RequestBody SysUserPasswordUpdate sysUserPasswordUpdate,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
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
    @SysLog
    public ReturnResult<SysUser> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @Validated(UpdateGroup.class) @RequestBody SysUserUpdate sysUser,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
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
    @SysLog
    @Permission(value = "sys:login:add", role = {ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysUser> save(@Validated(AddGroup.class) @RequestBody SysUserUpdate sysUserUpdate, BindingResult bindingResult, @Parameter(hidden = true) HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysUserService.saveUser(sysUserUpdate, request);
    }

    /**
     * 导出用户列表
     *
     * @param sysUserQuery 查询条件
     * @param roles        角色集合
     * @param response     响应对象
     */
    @Operation(summary = "导出用户列表")
    @GetMapping("/export")
    @SysLog
    @Permission(value = "sys:login:export", role = {ADMIN, AuthConstant.SUPER_ADMIN})
    public void exportUsers(@ParameterObject SysUserQuery sysUserQuery,
                            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                            HttpServletResponse response) {
        sysUserService.exportUsers(sysUserQuery, roles, response);
    }

    /**
     * 下载用户导入模板
     *
     * @param response 响应对象
     */
    @Operation(summary = "下载用户导入模板")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        sysUserService.downloadTemplate(response);
    }

    /**
     * 导入用户
     *
     * @param file CSV文件
     * @return 导入结果
     */
    @Operation(summary = "导入用户")
    @PostMapping("/import")
    @SysLog
    @Permission(value = "sys:login:import", role = {ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<String> importUsers(@RequestParam("file") MultipartFile file) {
        return sysUserService.importUsers(file);
    }
}
