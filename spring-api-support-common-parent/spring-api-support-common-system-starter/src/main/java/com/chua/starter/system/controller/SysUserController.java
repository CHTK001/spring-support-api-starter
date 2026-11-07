package com.chua.starter.system.controller;

import com.chua.common.support.utils.StringUtils;
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
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.enums.UpgradeType;
import com.chua.starter.system.model.user.SysUserQuery;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.system.pojo.user.SysUserBatchUpdate;
import com.chua.starter.system.pojo.user.SysUserPasswordUpdate;
import com.chua.starter.system.pojo.user.SysUserUpdate;
import com.chua.starter.system.pojo.user.SysUserVO;
import com.chua.starter.system.pojo.user.UserLayout;
import com.chua.starter.system.pojo.user.SysUserImportVO;
import com.chua.starter.system.pojo.verify.VerifyCodeSendRequest;
import com.chua.starter.system.pojo.verify.VerifyCodeSentVO;
import com.chua.starter.system.pojo.verify.VerifyCodeVerifyRequest;
import com.chua.starter.system.service.SysUserService;
import com.chua.starter.system.service.UserVerifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.transaction.annotation.Transactional;
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

    /**
     * 日志
     */
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SysUserController.class);

    /**
     * SYS用户服务
    */
    private final SysUserService sysUserService;

    /**
     * 账号资料验证服务
    */
    private final UserVerifyService userVerifyService;

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
    public ReturnPageResult<SysUserVO> pageForUser(@ParameterObject SysUserQuery sysUserQuery,
                                                    @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
                                                    @Parameter(hidden = true) @UserValue("sysUserId") Integer currentUserId,
                                                    @Parameter(hidden = true) @UserValue("sysDeptId") Integer currentDeptId) {
        return PageResultUtils.ok(sysUserService.pageForUser(sysUserQuery, roles, currentUserId, currentDeptId));
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
     * 删除批次。
     *
     * @param sysUserBatchUpdate 批量操作入参，不允许为 null
     * @return Return结果 对象
     */
    @Operation(summary = "批量删除用户")
    @DeleteMapping("/delete/batch")
    @SysLog
    @UserLog
    @Transactional(rollbackFor = Exception.class)
    @Permission("sys:login:delete")
    public ReturnResult<Boolean> deleteBatch(@RequestBody SysUserBatchUpdate sysUserBatchUpdate) {
        return sysUserService.deleteBatch(null == sysUserBatchUpdate ? null : sysUserBatchUpdate.getSysUserIds());
    }

    /**
     * 更新状态批次。
     *
     * @param sysUserBatchUpdate 批量操作入参，不允许为 null
     * @return Return结果 对象
     */
    @Operation(summary = "批量更新用户状态")
    @PutMapping("/status/batch")
    @SysLog
    @UserLog
    @Permission("sys:login:update")
    public ReturnResult<Boolean> updateStatusBatch(@RequestBody SysUserBatchUpdate sysUserBatchUpdate) {
        return null == sysUserBatchUpdate
                ? ReturnResult.illegal("请选择要操作的用户")
                : sysUserService.updateStatusBatch(sysUserBatchUpdate.getSysUserIds(), sysUserBatchUpdate.getSysUserStatus());
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
    @PostMapping("/reset")
    @SysLog
    @Permission(value = "sys:login:reset", role = {ADMIN, AuthConstant.SUPER_ADMIN})
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<String> reset(@RequestParam Long userId,
                                      @RequestParam(required = false) String password) {
        if (null == userId) {
            return ReturnResult.illegal("用户信息不存在");
        }

        // 如果传入了自定义密码，就用自定义密码；否则自动生成
        if (StringUtils.isNotBlank(password)) {
            return sysUserService.resetWithPassword(userId, password);
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
            @Parameter(hidden = true) @UserValue("token") String token,
            @Validated(UpdateGroup.class) @RequestBody SysUserPasswordUpdate sysUserPasswordUpdate,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        ReturnResult<SysUser> result = sysUserService.updateOwner(roles, userId, sysUserPasswordUpdate);
        // 更新成功后，通知 oauth 刷新用户信息缓存
        if (result.isSuccess() && StringUtils.isNotBlank(token)) {
            try {
                AuthClientExecute.getInstance().upgrade(token, UpgradeType.VERSION);
            } catch (Exception e) {
                log.debug("[user] 操作失败，已忽略: {}", e.getMessage());
            }
        }
        return result;
    }
    /**
     * 管理员更新指定用户信息。
     *
     * @param roles       当前用户角色集合
     * @param callerUsername 当前操作人账号
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
            @Parameter(hidden = true) @UserValue("username") String callerUsername,
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @Parameter(hidden = true) @UserValue("token") String token,
            @Validated(UpdateGroup.class) @RequestBody SysUserUpdate sysUser,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        ReturnResult<SysUser> result = sysUserService.updateUser(roles, callerUsername, userId, sysUser);
        // 更新成功后，通知 oauth 刷新用户信息缓存
        if (result.isSuccess() && StringUtils.isNotBlank(token)) {
            try {
                AuthClientExecute.getInstance().upgrade(token, UpgradeType.VERSION);
            } catch (Exception e) {
                log.debug("[user] 操作失败，已忽略: {}", e.getMessage());
            }
        }
        return result;
    }

    /**
     * 个人中心更新自己的基本资料
     *
     * @param userId      当前登录用户标识
     * @param token       当前token
     * @param sysUser     用户更新参数（不需要传sysUserId）
     * @return 更新后的用户信息
     */
    @Operation(summary = "个人中心更新自己的基本资料")
    @PutMapping("/updateProfile")
    @SysLog
    public ReturnResult<SysUser> updateProfile(
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @Parameter(hidden = true) @UserValue("token") String token,
            @RequestBody SysUserUpdate sysUser) {
        // 目标行由令牌中的 userId 决定：清空请求体携带的 sysUserId，使其走"更新自己"分支，
        // 同时杜绝借 body 指定他人 id；roles 与操作人账号传 null，自助改资料这条路径不开放角色变更
        sysUser.setSysUserId(null);
        ReturnResult<SysUser> result = sysUserService.updateUser(null, null, userId, sysUser);
        // 更新成功后，通知 oauth 刷新用户信息缓存
        if (result.isSuccess() && StringUtils.isNotBlank(token)) {
            try {
                AuthClientExecute.getInstance().upgrade(token, UpgradeType.VERSION);
            } catch (Exception e) {
                log.debug("[user] 操作失败，已忽略: {}", e.getMessage());
            }
        }
        return result;
    }

    /**
     * 向当前用户档案中的邮箱下发验证码
     *
     * <p>投递目标由服务端从自己的档案行取出，请求体不接受地址；服务器未配置邮箱发信通道时直接拒绝。</p>
     *
     * @param userId  当前登录用户标识
     * @param request 验证渠道
     * @return 有效期与脱敏目标
     */
    @Operation(summary = "发送本人资料验证的验证码")
    @PostMapping("/verify-code/send")
    @SysLog(module = "发送")
    public ReturnResult<VerifyCodeSentVO> sendVerifyCode(
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @Valid @RequestBody VerifyCodeSendRequest request) {
        if (null == userId) {
            return ReturnResult.illegal("请重新登录或者联系管理员");
        }
        return userVerifyService.sendCode(userId, request.channel());
    }

    /**
     * 校验验证码并标记本人邮箱已验证
     *
     * @param userId  当前登录用户标识
     * @param token   当前token
     * @param request 验证渠道与验证码
     * @return 验证结果
     */
    @Operation(summary = "校验本人资料验证码")
    @PostMapping("/verify-code/verify")
    @SysLog(module = "校验")
    public ReturnResult<Boolean> verifyVerifyCode(
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @Parameter(hidden = true) @UserValue("token") String token,
            @Valid @RequestBody VerifyCodeVerifyRequest request) {
        if (null == userId) {
            return ReturnResult.illegal("请重新登录或者联系管理员");
        }
        ReturnResult<Boolean> result = userVerifyService.verifyCode(userId, request.channel(), request.code());
        // 验证状态变更后刷新 oauth 用户信息，个人中心立即看到已验证标记
        if (result.isSuccess() && StringUtils.isNotBlank(token)) {
            try {
                AuthClientExecute.getInstance().upgrade(token, UpgradeType.VERSION);
            } catch (Exception e) {
                log.debug("[user] 操作失败，已忽略: {}", e.getMessage());
            }
        }
        return result;
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
    public ReturnResult<SysUser> save(@Validated(AddGroup.class) @RequestBody SysUserUpdate sysUserUpdate, BindingResult bindingResult,
        @Parameter(hidden = true) HttpServletRequest request) {
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
    @SysLog(module = "导入")
    @Permission(value = "sys:login:import", role = {ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<String> importUsers(@RequestParam("file") MultipartFile file) {
        return sysUserService.importUsers(file);
    }
}
