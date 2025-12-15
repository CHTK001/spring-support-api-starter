package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.protocol.Protocol;
import com.chua.starter.system.pojo.online.OnlineUserVO;
import com.chua.starter.system.service.SysOnlineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线用户管理接口
 *
 * @author CH
 * @since 2024/12/15
 */
@Tag(name = "在线用户管理接口")
@RestController
@RequestMapping("/v2/online")
@RequiredArgsConstructor
public class SysOnlineController {

    private final SysOnlineService sysOnlineService;

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    @Operation(summary = "获取在线用户列表")
    @GetMapping("/list")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<OnlineUserVO>> getOnlineUsers(
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "IP地址") @RequestParam(required = false) String ip) {
        return ReturnResult.ok(sysOnlineService.getOnlineUsers(username, ip));
    }

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数量
     */
    @Operation(summary = "获取在线用户数量")
    @GetMapping("/count")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Integer> getOnlineCount() {
        return ReturnResult.ok(sysOnlineService.getOnlineCount());
    }

    /**
     * 获取用户在线状态
     *
     * @param userId 用户ID
     * @return 在线状态
     */
    @Operation(summary = "获取用户在线状态")
    @GetMapping("/status/{userId}")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Protocol.OnlineStatus> getOnlineStatus(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        return ReturnResult.ok(AuthClientExecute.getInstance().getOnlineStatus(userId));
    }

    /**
     * 强制下线指定用户
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @Operation(summary = "强制下线用户")
    @DeleteMapping("/kick/{userId}")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> kickUser(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        return sysOnlineService.kickUser(userId);
    }

    /**
     * 批量强制下线用户
     *
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量强制下线用户")
    @DeleteMapping("/kick/batch")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Integer> kickUsers(@RequestBody List<String> userIds) {
        return sysOnlineService.kickUsers(userIds);
    }
}
