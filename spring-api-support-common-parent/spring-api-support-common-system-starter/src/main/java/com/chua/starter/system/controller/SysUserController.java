package com.chua.starter.system.controller;

import com.chua.starter.common.support.lang.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.validator.group.AddGroup;
import com.chua.starter.system.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.common.support.annotations.UserLog;
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

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;

/**
 * 用户管理接口
 *
 * <p>提供用户 CRUD、密码管理、用户布局/偏好配置、导入导出等运维管理能力。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Tag(name = "用户接口")
@RestController
@RequestMapping("/v2/user")
@RequiredArgsConstructor
public class SysUserController {

    /** SYS用户服务 */
    private final SysUserService sysUserService;

    /**
     * 用户分页列表查询。
     *
     * @param sysUserQuery 查询条件
     * @param roles        当前用户角色集合
     * @return 用户分页结果
     * @param true true
     */
    @Operation(summary = "用户分页列表")
    @GetMapping("/page")
    @Permission(role = {ADMIN})
    public ReturnPageResult<SysUserVO> pageForUser(@ParameterObject SysUserQuery sysUserQuery, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysUserService.pageForUser(sysUserQuery, roles));
    }


    /**
     * 获取当前用户的布局配置。
     *
     * @param userId 当前用户标识
     * @param roles  当前用户角色集合
     * @return 用户布局信息
     */
    @Operation(summary = "获取当前用户布局")
    @GetMapping("/layout")
    public ReturnResult<UserLayout> getUserLayout(
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return ReturnResult.ok(sysUserService.getUserLayout(userId, roles));
    }
    /**
     * 保存当前用户的布局配置。
     *
     * @param userId      当前用户标识
     * @param userLayout  布局信息
     * @return 更新后的用户布局
     */
    @Operation(summary = "保存当前用户布局")
    @PutMapping("/layout")
    public ReturnResult<UserLayout> saveUserLayout(
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId, @RequestBody UserLayout userLayout) {
        return ReturnResult.ok(sysUserService.saveUserLayout(userId, userLayout));
    }

    /**
     * 获取当前用户的偏好配置。
     *
     * @param userId 当前用户标识
     * @param roles  当前用户角色集合
     * @return 偏好配置 JSON 字符串
     */
    @Operation(summary = "获取当前用户偏好配置")
    @GetMapping("/preference")
    public ReturnResult<String> getUserPreference(
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        var userLayout = sysUserService.getUserLayout(userId, roles);
        return ReturnResult.ok(userLayout != null ? userLayout.getPreference() : null);
    }

    /**
     * 保存当前用户的偏好配置。
     *
     * @param userId     当前用户标识
     * @param preference 偏好配置 JSON 字符串
     * @return 保存结果
     */
    @Operation(summary = "保存当前用户偏好配置")
    @PutMapping("/preference")
    public ReturnResult<Boolean> saveUserPreference(
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @RequestBody(required = false) String preference) {
        var userLayout = sysUserService.getUserLayout(userId, null);
        if (userLayout == null) {
            userLayout = new UserLayout();
        }
 // 如果 preference 为 空，设置为空字符串
        userLayout.setPreference(preference != null ? preference : "{}");
        sysUserService.saveUserLayout(userId, userLayout);
        return ReturnResult.ok(true);
    }
    /**
     * 删除用户（逻辑删除）。
     *
     * @param sysUserId 用户标识
     * @return 删除结果
     * @throws Exception 删除失败时抛出异常
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/delete")
    @SysLog
    @UserLog
    @Transactional(rollbackFor = Exception.class)
    @Permission("sys:login:delete")
    public ReturnResult<Boolean> delete(Long sysUserId) {
        if (null == sysUserId) {
            return ReturnResult.illegal("用户信息不存在");
        }

        return sysUserService.delete(sysUserId);
    }

    /**
     * 重置当前用户自身密码。
     *
     * @param userId 当前用户标识
     * @return 重置后的用户信息
     * @param true true
     */
    @Operation(summary = "重置当前账号密码")
    @GetMapping("/resetOwner")
    @SysLog
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<SysUser> resetOwner(@Parameter(hidden = true) @UserValue("sysUserId") Long userId) {
        if (null == userId) {
            return ReturnResult.illegal("请重新登录或者联系管理员");
        }

        return sysUserService.resetOwner(userId);
    }

    /**
     * 管理员重置指定用户密码。
     *
     * @param userId 目标用户标识
     * @return 重置结果提示
     */
    @Operation(summary = "重置用户密码")
    @GetMapping("/reset")
    @SysLog
    @Permission(value = "sys:login:reset", role = {ADMIN, AuthConstant.SUPER_ADMIN})
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<String> reset(@RequestParam Long userId) {
        if (null == userId) {
            return ReturnResult.illegal("用户信息不存在");
        }

        return sysUserService.reset(userId);
    }

    /**
     * 更新当前用户自身密码。
     *
     * @param roles                 当前用户角色集合
     * @param userId                当前用户标识
     * @param sysUserPasswordUpdate 密码更新参数（含原密码和新密码）
     * @param bindingResult         校验结果
     * @return 更新后的用户信息
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
     * 管理员更新指定用户信息。
     *
     * @param roles       当前用户角色集合
     * @param userId      当前管理员用户标识
     * @param sysUser     用户更新参数
     * @param bindingResult 校验结果
     * @return 更新后的用户信息
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
     * 新增用户。
     *
     * @param sysUserUpdate 用户新增参数
     * @param bindingResult 校验结果
     * @param request       HTTP请求对象
     * @return 新增后的用户信息
     * @param @Validated(AddGroup.class @Validated(AddGroup.class
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
     * 下载用户导入模板。
     *
     * @param response HTTP响应对象，用于写入Excel文件流
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
     * @param @RequestParam("file" @RequestParam("file"
     */
    @Operation(summary = "导入用户")
    @PostMapping("/import")
    @SysLog
    @Permission(value = "sys:login:import", role = {ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<String> importUsers(@RequestParam("file") MultipartFile file) {
        return sysUserService.importUsers(file);
    }
}
