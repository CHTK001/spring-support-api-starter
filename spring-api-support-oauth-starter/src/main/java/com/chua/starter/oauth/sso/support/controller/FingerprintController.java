package com.chua.starter.oauth.sso.support.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.sso.support.service.FingerprintStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 指纹查询接口
 * <p>
 * 支持通过浏览器指纹或 IP 反查对应用户，用于安全审计和异常登录排查。
 * </p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
@RestController
@RequestMapping("/oauth/admin/fingerprint")
@RequiredArgsConstructor
@Tag(name = "指纹查询", description = "通过指纹/IP反查用户身份")
public class FingerprintController {

    private final FingerprintStorageService fingerprintStorageService; // fingerprintstorage服务

    /**
     * 根据指纹查询对应用户信息，用于安全审计和异常登录排查。
     *
     * @param fingerprint 浏览器指纹，不能为空
     * @return 用户信息（含 用户id、用户名、ip、browser、os 等），未找到返回 found=false
     */
    @GetMapping("/by-fingerprint")
    @Operation(summary = "根据指纹查询用户")
    @Parameter(name = "fingerprint", description = "浏览器指纹", required = true)
    public ReturnResult<Map<String, Object>> findByFingerprint(
            @RequestParam("fingerprint") String fingerprint) {
        if (fingerprint == null || fingerprint.isBlank()) {
            return ReturnResult.illegal("fingerprint 不能为空");
        }
        Map<String, Object> result = fingerprintStorageService.findByFingerprint(fingerprint);
        if (result == null) {
            return ReturnResult.ok(Map.of("found", false));
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("found", true);
        resp.putAll(result);
        return ReturnResult.ok(resp);
    }

    /**
     * 根据 IP 查询登录过的用户列表，用于排查同一 IP 的多个登录账户。
     *
     * @param ip 客户端 IP 地址，不能为空
     * @return 用户列表及总数
     */
    @GetMapping("/by-ip")
    @Operation(summary = "根据IP查询登录用户列表")
    @Parameter(name = "ip", description = "客户端IP地址", required = true)
    public ReturnResult<Map<String, Object>> findByIp(
            @RequestParam("ip") String ip) {
        if (ip == null || ip.isBlank()) {
            return ReturnResult.illegal("ip 不能为空");
        }
        List<Map<String, Object>> result = fingerprintStorageService.findByIp(ip);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("count", result.size());
        resp.put("users", result);
        return ReturnResult.ok(resp);
    }

    /**
     * 清除指纹缓存，通常在数据修正后手动调用。
     *
     * @param fingerprint 指定指纹（可选）
     * @param ip          指定 IP（可选）
     * @return 操作结果
     */
    @PostMapping("/cache/evict")
    @Operation(summary = "清除指纹缓存")
    @Parameter(name = "fingerprint", description = "指纹")
    public ReturnResult<String> evictCache(
            @RequestParam(required = false) String fingerprint,
            @RequestParam(required = false) String ip) {
        if (fingerprint != null && !fingerprint.isBlank()) {
            fingerprintStorageService.evictFingerprintCache(fingerprint);
        }
        if (ip != null && !ip.isBlank()) {
            fingerprintStorageService.evictIpCache(ip);
        }
        return ReturnResult.ok("已清除缓存");
    }
}
