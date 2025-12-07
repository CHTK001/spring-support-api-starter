package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.common.support.annotations.UserLogger;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.pojo.SysUserQuery;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.system.pojo.menu.SysMenuVO;
import com.chua.starter.system.pojo.user.SysUserPasswordUpdate;
import com.chua.starter.system.pojo.user.SysUserUpdate;
import com.chua.starter.system.pojo.user.SysUserVO;
import com.chua.starter.system.pojo.user.UserLayout;
import com.chua.starter.system.service.SysUserService;
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

import static com.chua.starter.oauth.client.support.contants.AuthConstant.ADMIN;

/**
 * 鐢ㄦ埛鎺ュ彛
 * @author CH
 * @since 2024/8/7
 */
@Tag(name = "鐢ㄦ埛鎺ュ彛")
@RestController
@RequestMapping("/v2/login")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 鐢ㄦ埛鍒嗛〉鍒楄〃
     *
     * @param sysUserQuery 鏉′欢
     * @return 鐢ㄦ埛鍒嗛〉鍒楄〃
     */
    @Operation(summary = "鐢ㄦ埛鍒嗛〉鍒楄〃")
    @GetMapping("/page")
    @Permission(role = {ADMIN})
    public ReturnPageResult<SysUserVO> pageForUser(@ParameterObject SysUserQuery sysUserQuery, @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return PageResultUtils.ok(sysUserService.pageForUser(sysUserQuery, roles));
    }


    /**
     * 鑾峰彇褰撳墠鐢ㄦ埛甯冨眬
     *
     * @return 鑾峰彇褰撳墠鐢ㄦ埛甯冨眬
     */
    @Operation(summary = "鑾峰彇褰撳墠鐢ㄦ埛甯冨眬")
    @GetMapping("/layout")
    public ReturnResult<UserLayout> getUserLayout(
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return ReturnResult.ok(sysUserService.getUserLayout(userId, roles));
    }
    /**
     * 淇濆瓨褰撳墠鐢ㄦ埛甯冨眬
     *
     * @return 鏇存柊鐢ㄦ埛
     */
    @Operation(summary = "淇濆瓨褰撳墠鐢ㄦ埛甯冨眬")
    @PutMapping("/layout")
    public ReturnResult<UserLayout> saveUserLayout(
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId, @RequestBody UserLayout userLayout) {
        return ReturnResult.ok(sysUserService.saveUserLayout(userId, userLayout));
    }
    /**
     * 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛鑿滃崟
     *
     * @return 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛鑿滃崟
     */
    @Operation(summary = "鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛鑿滃崟")
    @GetMapping("/menu")
    public ReturnResult<List<SysMenuVO>> getUserMenu(@Parameter(hidden = true) @UserValue("roles") Set<String> roles) {
        return sysUserService.getUserMenu(roles);
    }

    /**
     * 鍒犻櫎鐢ㄦ埛
     *
     * @return 鏇存柊鐢ㄦ埛
     */
    @Operation(summary = "鍒犻櫎鐢ㄦ埛")
    @DeleteMapping("/delete")
    @SysLog
    @UserLogger
    @Transactional(rollbackOn = Exception.class)
    @Permission("sys:login:delete")
    public ReturnResult<Boolean> delete(Long sysUserId) {
        if (null == sysUserId) {
            return Result.illegal("鐢ㄦ埛淇℃伅涓嶅瓨鍦");
        }

        return sysUserService.delete(sysUserId);
    }

    /**
     * 閲嶇疆鑷韩瀵嗙⒓
     *
     * @param userId 鐢ㄦ埗ID
     * @return 閲嶇疆鐢ㄦ埛瀵嗙⒓
     */
    @Operation(summary = "閲嶇疆褰撳墠璐﹀彿瀵嗙⒓")
    @GetMapping("/resetOwner")
    @SysLog
    @Transactional(rollbackOn = Exception.class)
    public ReturnResult<SysUser> resetOwner(@Parameter(hidden = true) @UserValue("sysUserId") Long userId) {
        if (null == userId) {
            return Result.illegal("璇烽噸鏂扮櫥褰曟垨鑰呰仈绯荤鐞嗗憳");
        }

        return sysUserService.resetOwner(userId);
    }

    /**
     * 閲嶇疆鐢ㄦ埛瀵嗙⒓
     *
     * @param userId 鐢ㄦ埗ID
     * @return 閲嶇疆鐢ㄦ埛瀵嗙⒓
     */
    @Operation(summary = "閲嶇疆鐢ㄦ埛瀵嗙⒓")
    @GetMapping("/reset")
    @SysLog
    @Permission(value = "sys:login:reset", role = {ADMIN, AuthConstant.SUPER_ADMIN})
    @Transactional(rollbackOn = Exception.class)
    public ReturnResult<String> reset(Long userId) {
        if (null == userId) {
            return Result.illegal("鐢ㄦ埛淇℃伅涓嶅瓨鍦");
        }

        return sysUserService.reset(userId);
    }

    /**
     * 鏇存柊褰撳墠璐﹀彿瀵嗙爜
     *
     * @return 鏇存柊鐢ㄦ埛
     */
    @Operation(summary = "鏇存柊鐢ㄦ埛鑷韩瀵嗙爜")
    @PutMapping("/updateOwner")
    @SysLog
    public ReturnResult<SysUser> updateOwner(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @Validated(UpdateGroup.class) @RequestBody SysUserPasswordUpdate sysUserPasswordUpdate,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysUserService.updateOwner(roles, userId, sysUserPasswordUpdate);
    }
    /**
     * 鏇存柊鐢ㄦ埛
     *
     * @return 鏇存柊鐢ㄦ埛
     */
    @Operation(summary = "鏇存柊鐢ㄦ埛")
    @PutMapping("/update")
    @SysLog
    public ReturnResult<SysUser> update(
            @Parameter(hidden = true) @UserValue("roles") Set<String> roles,
            @Parameter(hidden = true) @UserValue("sysUserId") Long userId,
            @Validated(UpdateGroup.class) @RequestBody SysUserUpdate sysUser,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysUserService.updateUser(roles, userId, sysUser);
    }

    /**
     * 鏂板鐢ㄦ埛
     *
     * @return 鏂板鐢ㄦ埛
     */
    @Operation(summary = "鏂板鐢ㄦ埛")
    @PostMapping("/save")
    @SysLog
    @Permission(value = "sys:login:add", role = {ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysUser> save(@Validated(AddGroup.class) @RequestBody SysUserUpdate sysUserUpdate, BindingResult bindingResult, @Parameter(hidden = true) HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return Result.illegal(bindingResult.getAllErrors().getFirst().getDefaultMessage());
        }

        return sysUserService.saveUser(sysUserUpdate, request);
    }
}
