package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.annotation.UserValue;
import com.chua.starter.system.pojo.user.UserLayout;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * @author CH
 * @since 2025/6/5 8:45
 */
@Tag(name = "用户接口")
@RestController
@RequestMapping("/v2/login")
@RequiredArgsConstructor
public class EmptyController {

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
        return ReturnResult.ok(new UserLayout());
    }
}
