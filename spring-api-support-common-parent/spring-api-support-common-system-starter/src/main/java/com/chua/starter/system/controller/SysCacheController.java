package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.model.cache.SysCacheInfo;
import com.chua.starter.system.service.SysCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 缓存管理接口
 *
 * <p>提供系统各类缓存的列举与手动刷新能力，供运维人员在配置变更后主动刷新缓存。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Tag(name = "缓存管理接口")
@RestController
@RequestMapping("/v2/cache")
@RequiredArgsConstructor
public class SysCacheController {

    /**
     * sys缓存服务
    */
    private final SysCacheService sysCacheService;

    /**
     * 缓存列表
     *
     * @return 缓存信息列表
     */
    @Operation(summary = "缓存列表")
    @GetMapping("/list")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<SysCacheInfo>> list() {
        return ReturnResult.ok(sysCacheService.list());
    }

    /**
     * 刷新指定缓存
     *
     * @param cacheName 缓存名称
     * @return 刷新结果
     */
    @Operation(summary = "刷新指定缓存")
    @DeleteMapping("/refresh")
    @SysLog
    @Permission(value = "sys:cache:refresh", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> refresh(@RequestParam String cacheName) {
        return sysCacheService.refresh(cacheName);
    }

    /**
     * 刷新全部缓存
     *
     * @return 刷新结果
     */
    @Operation(summary = "刷新全部缓存")
    @DeleteMapping("/refresh/all")
    @SysLog
    @Permission(value = "sys:cache:refresh", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> refreshAll() {
        return sysCacheService.refreshAll();
    }
}
