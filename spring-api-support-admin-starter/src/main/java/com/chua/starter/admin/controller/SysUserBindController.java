package com.chua.starter.admin.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.admin.entity.SysUser;
import com.chua.starter.admin.entity.SysUserTripartiteBind;
import com.chua.starter.admin.service.SysUserService;
import com.chua.starter.admin.service.SysUserTripartiteBindService;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户控制器
 *
 * @author haoxr
 * @since 2022/10/16
 */
@Tag(name = "用户三方接口")
@RestController
@RequestMapping("/v2/users/bind")
@RequiredArgsConstructor
public class SysUserBindController implements Constant {

    private final SysUserService userService;
    private final SysUserTripartiteBindService sysUserTripartiteBindService;

    /**
     * 列表
     * 绑定信息
     *
     * @param userId  用户id
     * @param request 要求
     * @return {@link ReturnResult}<{@link SysUser}>
     */
    @Operation(summary = "解绑")
    @DeleteMapping("/unBinder")
    public ReturnResult<Boolean> unBinder(@Parameter(hidden = true) @UserValue("userId") Long userId,
                                       Long tripartiteBindId,
                                       @Parameter(hidden = true) HttpServletRequest request) {
        return ReturnResult.ok(sysUserTripartiteBindService.unBinder(userId, tripartiteBindId));
    }

    /**
     * 列表
     * 绑定信息
     *
     * @param userId  用户id
     * @param request 要求
     * @return {@link ReturnResult}<{@link SysUser}>
     */
    @Operation(summary = "绑定信息")
    @GetMapping("/list")
    public ReturnResult<List<SysUserTripartiteBind>> list(@Parameter(hidden = true) @UserValue("userId") Long userId, @Parameter(hidden = true) HttpServletRequest request) {
        return ReturnResult.ok(sysUserTripartiteBindService.listBind(userId));
    }


}
