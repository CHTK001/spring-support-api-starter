package com.chua.starter.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.exception.NotSupportedException;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.admin.entity.SysUser;
import com.chua.starter.admin.entity.SysUserSetting;
import com.chua.starter.admin.pojo.LoginResult;
import com.chua.starter.admin.pojo.UserInfoVO;
import com.chua.starter.admin.query.LoginData;
import com.chua.starter.admin.query.SysUserUpdate;
import com.chua.starter.admin.query.UserPageQuery;
import com.chua.starter.admin.service.SysUserRoleService;
import com.chua.starter.admin.service.SysUserService;
import com.chua.starter.admin.service.SysUserSettingService;
import com.chua.starter.common.support.annotations.OperateLog;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.logger.SysLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.AuthIgnore;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Set;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;

/**
 * 用户控制器
 *
 * @author haoxr
 * @since 2022/10/16
 */
@Tag(name = "用户接口")
@RestController
@RequestMapping("/v2/users")
@RequiredArgsConstructor
public class SysUserController implements Constant {

    private final SysUserService userService;
    private final SysUserRoleService sysUserRoleService;

    private final SysUserSettingService sysUserSettingService;

    /**
     * 用户分页列表
     *
     * @param queryParams 条件
     * @return 用户分页列表
     */
    @Operation(summary = "用户分页列表")
    @GetMapping("/page")
    public ReturnPageResult<SysUser> getUserPage(UserPageQuery queryParams, @UserValue("username") String username) {
        IPage<SysUser> result = userService.getUserPage(queryParams, username);
        return PageResultUtils.ok(result);
    }

    /**
     * 保存用户首页布局
     *
     * @param sysUserSetting 保存用户首页布局
     * @return 保存用户首页布局
     */
    @Operation(summary = "保存用户首页布局")
    @PostMapping("/grid")
    public ReturnResult<Boolean> grid(@RequestBody SysUserSetting sysUserSetting,
                                      @Parameter(hidden = true) @UserValue("userDashboardGrid") String userDashboardGrid,
                                      @Parameter(hidden = true) @UserValue("userId") Long userId,
                                      @Parameter(hidden = true) @UserValue("username") String username,
                                      @Parameter(hidden = true) @UserValue("roles") Set<String> roles
    ) {
       return sysUserSettingService.saveUserSetting(sysUserSetting, userDashboardGrid, userId, username, roles);
    }

    /**
     * 登录码
     *
     * @param loginCodeType 登录码类型
     * @return 结果
     */
    @Operation(summary = "登录码")
    @GetMapping("/loginCode")
    @AuthIgnore
    public Result<String> loginCode(String loginCodeType, String type) {
        String loginCode = AuthClientExecute.getInstance().getLoginCode(loginCodeType, type);
        if (null == loginCode) {
            throw new NotSupportedException("当前不支持");
        }
        return Result.success(loginCode);
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
    @SysLogger(value = "用户登录", action = "登录", content = "#{#now} #{#args[0].username} 登录系统, 登录返回[状态:#{#result.code}(#{#result.msg})}]}")
    public Result<LoginResult> login(@Valid @RequestBody LoginData loginData,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.failed(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return userService.login(loginData, request, response);
    }

    @Operation(summary = "注销")
    @SysLogger(value = "用户登出", action = "登出", content = "#{#now} #{#args[0].username} 退出系统, 登录返回[状态:#{#result.code}(#{#result.msg})}]}")
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
        return Result.success(userService.getUserLoginInfo());
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
    @Permission("sys:user:del")
    public ReturnResult<SysUser> delete(Long userId) {
        if (null == userId) {
            return Result.illegal("用户信息不存在");
        }
        SysUser sysUser1 = userService.getById(userId);
        if (null == sysUser1) {
            return Result.illegal("用户信息不存在");
        }

        if (1 == sysUser1.getUserSys()) {
            return Result.illegal("系统账号不能删除");
        }
        userService.removeById(userId);
        return Result.ok();
    }

    /**
     * 重置自身密碼
     *
     * @param userId 用戶ID
     * @param notice 通知(实现方式多个,分割)
     * @return 重置用户密碼
     */
    @Operation(summary = "重置用户密碼")
    @GetMapping("/resetOwner")
    @OperateLog
    @Transactional(rollbackOn = Exception.class)
    public ReturnResult<SysUser> resetOwner(@Parameter(hidden = true) @UserValue("userId") Long userId, String notice) {
        if (null == userId) {
            return Result.illegal("请重新登录或者联系管理员");
        }

        return userService.resetOwner(userId, notice);
    }
    /**
     * 重置用户密碼
     *
     * @param userId 用戶ID
     * @param notice 通知(实现方式多个,分割)
     * @return 重置用户密碼
     */
    @Operation(summary = "重置用户密碼")
    @GetMapping("/reset")
    @OperateLog
    @Permission("sys:user:reset")
    @Transactional(rollbackOn = Exception.class)
    public ReturnResult<SysUser> reset(Long userId, String notice) {
        if (null == userId) {
            return Result.illegal("用户信息不存在");
        }

        return userService.reset(userId, notice);
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
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles1,
            @Parameter(hidden = true) @UserValue("username") String username,
            @Valid @RequestBody SysUserUpdate sysUser,
            BindingResult bindingResult,
            @Parameter(hidden = true) HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

       return userService.updateUser(roles1, username, sysUser);
    }

    /**
     * 新增用户
     *
     * @return 新增用户
     */
    @Operation(summary = "新增用户")
    @PostMapping("/save")
    @OperateLog
    @Permission("sys:user:add")
    @Transactional(rollbackOn = Exception.class)
    public ReturnResult<SysUser> save(@Valid @RequestBody SysUser sysUser, BindingResult bindingResult, @Parameter(hidden = true) HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return userService.saveUser(sysUser, request);
    }
}
