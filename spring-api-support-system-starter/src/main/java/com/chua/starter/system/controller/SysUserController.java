package com.chua.starter.system.controller;

import com.chua.common.support.constant.Action;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.system.entity.SysUser;
import com.chua.starter.system.pojo.menu.SysMenuVO;
import com.chua.starter.system.pojo.user.SysUserUpdate;
import com.chua.starter.system.query.SysUserQuery;
import com.chua.starter.system.result.LoginResult;
import com.chua.starter.system.result.UserInfoVO;
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

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.SUPER_ADMIN;

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
    public ReturnPageResult<SysUser> pageForUser(@ParameterObject SysUserQuery sysUserQuery, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysUserService.pageForUser(sysUserQuery, roles));
    }

    /**
     * 登录
     *
     * @param loginData     登录信息
     * @param request       请求
     * @param response      响应
     * @param bindingResult 参数处理
     * @return 结果
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    @OperateLog
    @SysLogger(module = "LOGIN", name = "用户登录", action = Action.LOGIN, content = "#{#now} #{#args[0].username} 登录系统, 登录返回[状态:#{#result.code}(#{#result.msg})]")
    public Result<LoginResult> login(@Valid @RequestBody LoginData loginData,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.failed(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysUserService.login(loginData, request, response);
    }

    @Operation(summary = "注销")
    @SysLogger(module = "LOGIN", name = "用户登出", action = Action.LOGOUT, content = "#{#now} #{#args[0].username} 退出系统, 登录返回[状态:#{#result.code}(#{#result.msg})]")
    @DeleteMapping("/logout")
    @OperateLog
    public Result logout(@UserValue("token") String uid, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(uid)) {
            return Result.success("注销成功");
        }
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        LoginAuthResult accessToken = clientExecute.logout(uid, LogoutType.LOGOUT);
        if (null == accessToken) {
            return Result.failed("退出失败请稍后重试");
        }

        if (accessToken.getCode() != 200) {
            return Result.failed(accessToken.getMessage());
        }

        CookieUtil.remove(request, response, "x-oauth-cookie");
        return Result.success("注销成功");
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 获取当前登录用户信息
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<UserInfoVO> getUserLoginInfo() {
        return Result.success(sysUserService.getUserLoginInfo());
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
    @SysLogger
    @Transactional(rollbackOn = Exception.class)
    @Permission("sys:user:delete")
    public ReturnResult<Boolean> delete(Long userId) {
        if (null == userId) {
            return Result.illegal("用户信息不存在");
        }

        return sysUserService.delete(userId);
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
    public ReturnResult<SysUser> resetOwner(@Parameter(hidden = true) @UserValue("userId") Long userId) {
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
    @Permission(value = "sys:user:reset", role = {ADMIN, SUPER_ADMIN})
    @Transactional(rollbackOn = Exception.class)
    public ReturnResult<String> reset(Long userId) {
        if (null == userId) {
            return Result.illegal("用户信息不存在");
        }

        return sysUserService.reset(userId);
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
            @Parameter(hidden = true) @UserValue("userId") Long userId,
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
    @Permission(value = "sys:user:add", role = {ADMIN, SUPER_ADMIN})
    public ReturnResult<SysUser> save(@Validated(AddGroup.class) @RequestBody SysUserUpdate sysUserUpdate, BindingResult bindingResult, @Parameter(hidden = true) HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return sysUserService.saveUser(sysUserUpdate, request);
    }
}
