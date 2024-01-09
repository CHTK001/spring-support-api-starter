package com.chua.starter.admin.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.exception.NotSupportedException;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.*;
import com.chua.starter.admin.entity.SysUser;
import com.chua.starter.admin.entity.SysUserGrid;
import com.chua.starter.admin.entity.SysUserRole;
import com.chua.starter.admin.pojo.LoginResult;
import com.chua.starter.admin.pojo.UserInfoVO;
import com.chua.starter.admin.query.LoginData;
import com.chua.starter.admin.query.SysUserUpdate;
import com.chua.starter.admin.query.UserPageQuery;
import com.chua.starter.admin.service.SysUserGridService;
import com.chua.starter.admin.service.SysUserRoleService;
import com.chua.starter.admin.service.SysUserService;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.logger.SysLogger;
import com.chua.starter.common.support.notice.NoticeMessage;
import com.chua.starter.common.support.notice.NoticeService;
import com.chua.starter.common.support.notice.NoticeType;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.AuthIgnore;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.*;

import static com.chua.common.support.discovery.Constants.CAPTCHA_SESSION_KEY;
import static com.chua.common.support.lang.code.ReturnCode.*;
import static com.chua.starter.common.support.utils.RequestUtils.getIpAddress;

/**
 * 用户控制器
 *
 * @author haoxr
 * @since 2022/10/16
 */
@Api(tags = "用户接口")
@RestController
@RequestMapping("/v2/users")
@RequiredArgsConstructor
public class SysUserController implements Constant {

    private final SysUserService userService;
    private final SysUserRoleService sysUserRoleService;

    private final TransactionTemplate transactionTemplate;

    private final SysUserGridService sysUserGridService;

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
     * @param sysUserGrid 保存用户首页布局
     * @return 保存用户首页布局
     */
    @Operation(summary = "保存用户首页布局")
    @PostMapping("/grid")
    public ReturnResult<Boolean> grid(@RequestBody SysUserGrid sysUserGrid,
                                      @ApiParam(hidden = true) @UserValue("userDashboardGrid") String userDashboardGrid,
                                      @ApiParam(hidden = true) @UserValue("userId") Long userId,
                                      @ApiParam(hidden = true) @UserValue("username") String username,
                                      @ApiParam(hidden = true) @UserValue("roles") Set<String> roles
    ) {
        sysUserGrid.setUserId(userId);
        if (!ADMIN.equalsIgnoreCase(username) && !roles.contains(ADMIN)) {
            if (StringUtils.isEmpty(userDashboardGrid)) {
                return Result.illegal("当前账号没有权限配置");
            }
            List<String> strings = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(userDashboardGrid);
            String copmsList = sysUserGrid.getCopmsList();
            JSONArray jsonArray = JSON.parseArray(copmsList);
            for (int i = 0; i < jsonArray.size(); i++) {
                if (CollectionUtils.notContains(strings, (Collection<String>) jsonArray.get(i))) {
                    return Result.illegal("当前账号没有权限添加其它组件");
                }
            }

        }
        return Result.success(sysUserGridService.saveGrid(sysUserGrid));
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
    public Result<String> loginCode(String loginCodeType) {
        String loginCode = AuthClientExecute.getInstance().getLoginCode(loginCodeType);
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
    @SysLogger(value = "用户登录", action = "登录", content = "#{#now} #{#args[0].username} 登录系统, 登录返回[状态:#{#result.code}(#{#result.msg})}]}")
    public Result<LoginResult> login(@Valid @RequestBody LoginData loginData,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.failed(REQUEST_PARAM_ERROR, bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        String code = loginData.getVerifyCode();
        String sessionKey = Optional.ofNullable(request.getSession().getAttribute(CAPTCHA_SESSION_KEY)).orElse("").toString();
        if (Strings.isNullOrEmpty(code) || !code.equalsIgnoreCase(sessionKey)) {
            return Result.failed(RESOURCE_CAPTCHA_ERROR, "校验码错误");
        }

        String address = getIpAddress(request);
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        LoginAuthResult accessToken = clientExecute.getAccessToken(loginData.getUsername(), loginData.getPassword(), AuthType.WEB,
                ImmutableBuilder.<String, Object>builderOfMap().put("address", address).build()
        );
        if (null == accessToken) {
            return Result.failed(USERNAME_OR_PASSWORD_ERROR, "账号或者密码不正确");
        }

        if (accessToken.getCode() != 200) {
            return Result.failed(REQUEST_PARAM_ERROR, accessToken.getMessage());
        }

        LoginResult loginResult = LoginResult.builder()
                .userInfo(userService.getUserLoginInfo(accessToken.getToken()))
                .tokenType("Bearer")
                .accessToken(accessToken.getToken())
                .build();
        return Result.success(loginResult);
    }

    @Operation(summary = "注销")
    @SysLogger(value = "用户登出", action = "登出", content = "#{#now} #{#args[0].username} 退出系统, 登录返回[状态:#{#result.code}(#{#result.msg})}]}")
    @DeleteMapping("/logout")
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
    @Transactional(rollbackOn = Exception.class)
    public ReturnResult<SysUser> resetOwner(@ApiParam(hidden = true) @UserValue("userId") Long userId, String notice) {
        if (null == userId) {
            return Result.illegal("请重新登录或者联系管理员");
        }

        SysUser sysUser1 = userService.getById(userId);
        if (null == sysUser1) {
            return Result.illegal("用户信息不存在, 请重新登录或者联系管理员");
        }

        sysUser1.setUserPassword(RandomUtils.randomString(8));
        sysUser1.setUserSalt(RandomUtils.randomString(16));
        sysUser1.setUserPassword(DigestUtils.md5Hex(sysUser1.getUserPassword() + sysUser1.getUserSalt()));

        userService.updateById(sysUser1);
        NoticeService noticeService = ServiceProvider.of(NoticeService.class).createProvider(notice);
        noticeService.notice(NoticeMessage.builder()
                .userId(sysUser1.getUserId() + "").type(NoticeType.RESET).build());
        return Result.ok();
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
    @Permission("sys:user:reset")
    @Transactional(rollbackOn = Exception.class)
    public ReturnResult<SysUser> reset(Long userId, String notice) {
        if (null == userId) {
            return Result.illegal("用户信息不存在");
        }

        SysUser sysUser1 = userService.getById(userId);
        if (null == sysUser1) {
            return Result.illegal("用户信息不存在");
        }

        String password = RandomUtils.randomString(8);
        sysUser1.setUserPassword(password);
        sysUser1.setUserSalt(RandomUtils.randomString(14));
        sysUser1.setUserPassword(DigestUtils.md5Hex(sysUser1.getUserPassword() + sysUser1.getUserSalt()));

        userService.updateById(sysUser1);
        NoticeService noticeService = ServiceProvider.of(NoticeService.class).createProvider(notice);
        noticeService.notice(NoticeMessage.builder()
                .userId(sysUser1.getUserId() + "").type(NoticeType.RESET).build());
        return Result.ok();
    }

    /**
     * 更新用户
     *
     * @return 更新用户
     */
    @Operation(summary = "更新用户")
    @PutMapping("/update")
    public ReturnResult<SysUser> update(
            @ApiParam(hidden = true) @UserValue("roles") Set<String> roles1,
            @ApiParam(hidden = true) @UserValue("username") String username,
            @Valid @RequestBody SysUserUpdate sysUser,
            BindingResult bindingResult,
            @ApiParam(hidden = true) HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        SysUser sysUser1 = userService.getById(sysUser.getUserId());
        if (null == sysUser1) {
            return Result.illegal("用户信息不存在");
        }

        if (ADMIN.equalsIgnoreCase(sysUser1.getUsername()) && !ADMIN.equalsIgnoreCase(username)) {
            return Result.illegal("您无权限修改管理员账号信息");
        }

        List<String> strings = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(sysUser.getRoleId());

        if (!roles1.contains(ADMIN) && !userService.isAdmin()) {
            if (!sysUser1.getUsername().equalsIgnoreCase(username)) {
                return Result.illegal("您无权限修改管理员账号信息");
            }
        }

        if (!roles1.contains(ADMIN) && !sysUser1.getUsername().equalsIgnoreCase(username)) {
            return Result.illegal("您只能修改自己的账号信息");
        }

        if (StringUtils.isNotBlank(sysUser.getUserPassword()) && StringUtils.isBlank(sysUser.getUserSeRan())) {
            return Result.illegal("系统异常请联系管理员");
        }
        SysUser newSysUser = new SysUser();
        newSysUser.setUserSalt(null);
        BeanUtils.copyProperties(sysUser, newSysUser);
        newSysUser.setUsername(null);

        if (StringUtils.isNotBlank(sysUser.getUserPassword())) {
            if (StringUtils.isBlank(sysUser.getUserOldPassword())) {
                return Result.illegal("旧密码不能为空");
            }

            if (sysUser.getUserOldPassword().equals(sysUser.getUserPassword())) {
                return Result.illegal("旧密码与新密码不能相同");
            }

            Base64.Decoder decoder = Base64.getDecoder();
            String key = new String(decoder.decode(decoder.decode(sysUser.getUserSeRan())));
            String userOldPassword = DigestUtils.aesDecrypt(sysUser.getUserOldPassword(), key);

            if (!sysUser1.getUserPassword().equals(DigestUtils.md5Hex(DigestUtils.md5Hex(userOldPassword) + sysUser1.getUserSalt()))) {
                return Result.illegal("旧密码不正确");
            }

            String newPassword = DigestUtils.aesDecrypt(sysUser.getUserPassword(), key);
            newSysUser.setUserSalt(RandomUtils.randomString(16));
            newSysUser.setUserPassword(DigestUtils.md5Hex(DigestUtils.md5Hex(newPassword) + newSysUser.getUserSalt()));
        }

        if (1 == sysUser1.getUserSys()) {
            newSysUser.setUserStatus(1);
            newSysUser.setUserDeleted(0);
            newSysUser.setUserSys(1);
        }

        transactionTemplate.execute(status -> {
            userService.updateById(newSysUser);
            sysUserRoleService.remove(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, sysUser.getUserId()));
            List<SysUserRole> roles = new ArrayList<>();
            for (String s : strings) {
                try {
                    roles.add(new SysUserRole(newSysUser.getUserId(), Long.parseLong(s)));
                } catch (NumberFormatException ignored) {
                }
            }
            sysUserRoleService.saveBatch(roles);
            return true;
        });
        refresh();
        return Result.ok();
    }

    private void refresh() {
        AuthClientExecute.getInstance().refreshToken();
    }

    /**
     * 新增用户
     *
     * @return 新增用户
     */
    @Operation(summary = "新增用户")
    @PostMapping("/save")
    @Permission("sys:user:add")
    @Transactional(rollbackOn = Exception.class)
    public ReturnResult<SysUser> save(@Valid @RequestBody SysUser sysUser, BindingResult bindingResult, @ApiParam(hidden = true) HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        String username = sysUser.getUsername();
        if (userService.count(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username)) > 0L) {
            return Result.illegal("用户账号已存在");
        }

        Base64.Decoder decoder = Base64.getDecoder();
        String key = new String(decoder.decode(decoder.decode(sysUser.getUserSeRan())));
        String newPassword = DigestUtils.aesDecrypt(sysUser.getUserPassword(), key);

        sysUser.setUserSys(0);
        sysUser.setUserSalt(RandomUtils.randomString(16));
        sysUser.setUserPassword(DigestUtils.md5Hex(DigestUtils.md5Hex(newPassword) + sysUser.getUserSalt()));
        sysUser.setUserRegisterIp(RequestUtils.getIpAddress(request));
        sysUser.setUserCode(IdUtils.snowFlake());

        userService.save(sysUser);
        Long userId = sysUser.getUserId();

        sysUserRoleService.remove(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        List<SysUserRole> roles = new ArrayList<>();
        for (String s : Splitter.on(",").omitEmptyStrings().trimResults().splitToList(sysUser.getRoleId())) {
            try {
                roles.add(new SysUserRole(userId, Long.parseLong(s)));
            } catch (NumberFormatException ignored) {
            }
        }
        sysUserRoleService.saveBatch(roles);
        return Result.ok(sysUser);
    }
}
