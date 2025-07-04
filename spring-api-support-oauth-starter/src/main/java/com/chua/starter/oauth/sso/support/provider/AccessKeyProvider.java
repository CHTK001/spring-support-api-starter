package com.chua.starter.oauth.sso.support.provider;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.json.Json;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.result.ReturnResult;
import com.chua.starter.oauth.client.support.user.AccessSecret;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.service.AccessKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 访问密钥管理控制器
 * <p>
 * 提供ak,sk（访问密钥和秘密密钥）的管理功能，包括：
 * - 查询用户的ak,sk列表
 * - 删除指定的ak,sk
 * - 批量删除ak,sk
 * - 清空用户所有ak,sk
 *
 * @author CH
 * @version 4.0.0.32
 */
@Slf4j
@RestController
@RequestMapping("/oauth/access-key")
@RequiredArgsConstructor
@Tag(name = "访问密钥管理", description = "提供ak,sk访问密钥的管理功能")
@Spi("AccessKeyProvider")
public class AccessKeyProvider {

    private final AuthServerProperties authServerProperties;
    private final AccessKeyService accessKeyService;

    /**
     * 获取用户的ak,sk列表
     *
     * @param userId  用户ID
     * @param request HTTP请求
     * @return ak, sk列表
     */
    @GetMapping("/list")
    @Operation(
            summary = "获取用户ak,sk列表",
            description = "获取指定用户的所有访问密钥列表",
            responses = {
                    @ApiResponse(responseCode = "200", description = "获取成功"),
                    @ApiResponse(responseCode = "400", description = "参数错误"),
                    @ApiResponse(responseCode = "401", description = "未授权访问")
            }
    )
    public ReturnResult<List<AccessSecret>> getAccessKeyList(
            @Parameter(description = "用户ID", required = true)
            @RequestParam String userId,
            HttpServletRequest request) {

        log.info("获取用户ak,sk列表 - 用户ID: {}, IP: {}", userId, getClientIp(request));

        if (StringUtils.isEmpty(userId)) {
            return ReturnResult.illegal("用户ID不能为空");
        }

        try {
            List<AccessSecret> accessKeys = accessKeyService.getAccessKeysByUserId(userId);
            log.info("成功获取用户ak,sk列表 - 用户ID: {}, 数量: {}", userId, accessKeys.size());
            return ReturnResult.ok(accessKeys);
        } catch (Exception e) {
            log.error("获取用户ak,sk列表失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return ReturnResult.error("获取ak,sk列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定的ak,sk
     *
     * @param accessKey 访问密钥
     * @param userId    用户ID
     * @param request   HTTP请求
     * @param response  HTTP响应
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(
            summary = "删除指定ak,sk",
            description = "删除用户指定的访问密钥",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功"),
                    @ApiResponse(responseCode = "400", description = "参数错误"),
                    @ApiResponse(responseCode = "404", description = "ak,sk不存在"),
                    @ApiResponse(responseCode = "403", description = "无权限删除")
            }
    )
    public ReturnResult<Boolean> deleteAccessKey(
            @Parameter(description = "访问密钥", required = true)
            @RequestParam String accessKey,
            @Parameter(description = "用户ID", required = true)
            @RequestParam String userId,
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("删除ak,sk - 用户ID: {}, AccessKey: {}, IP: {}", userId, accessKey, getClientIp(request));

        if (StringUtils.isEmpty(accessKey) || StringUtils.isEmpty(userId)) {
            return ReturnResult.illegal("访问密钥和用户ID不能为空");
        }

        try {
            boolean deleted = accessKeyService.deleteAccessKey(userId, accessKey);
            if (deleted) {
                log.info("成功删除ak,sk - 用户ID: {}, AccessKey: {}", userId, accessKey);
                return ReturnResult.ok(true, "删除成功");
            } else {
                log.warn("ak,sk不存在或无权限删除 - 用户ID: {}, AccessKey: {}", userId, accessKey);
                return ReturnResult.error("ak,sk不存在或无权限删除");
            }
        } catch (Exception e) {
            log.error("删除ak,sk失败 - 用户ID: {}, AccessKey: {}, 错误: {}", userId, accessKey, e.getMessage(), e);
            return ReturnResult.error("删除ak,sk失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除ak,sk
     *
     * @param accessKeys 访问密钥列表（逗号分隔）
     * @param userId     用户ID
     * @param request    HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/batch-delete")
    @Operation(
            summary = "批量删除ak,sk",
            description = "批量删除用户指定的多个访问密钥",
            responses = {
                    @ApiResponse(responseCode = "200", description = "删除成功"),
                    @ApiResponse(responseCode = "400", description = "参数错误"),
                    @ApiResponse(responseCode = "403", description = "无权限删除")
            }
    )
    public ReturnResult<Map<String, Object>> batchDeleteAccessKeys(
            @Parameter(description = "访问密钥列表（逗号分隔）", required = true)
            @RequestParam String accessKeys,
            @Parameter(description = "用户ID", required = true)
            @RequestParam String userId,
            HttpServletRequest request) {

        log.info("批量删除ak,sk - 用户ID: {}, AccessKeys: {}, IP: {}", userId, accessKeys, getClientIp(request));

        if (StringUtils.isEmpty(accessKeys) || StringUtils.isEmpty(userId)) {
            return ReturnResult.illegal("访问密钥列表和用户ID不能为空");
        }

        try {
            Map<String, Object> result = accessKeyService.batchDeleteAccessKeys(userId, accessKeys);
            log.info("批量删除ak,sk完成 - 用户ID: {}, 结果: {}", userId, Json.toJson(result));
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("批量删除ak,sk失败 - 用户ID: {}, AccessKeys: {}, 错误: {}", userId, accessKeys, e.getMessage(), e);
            return ReturnResult.error("批量删除ak,sk失败: " + e.getMessage());
        }
    }

    /**
     * 清空用户所有ak,sk
     *
     * @param userId          用户ID
     * @param confirmPassword 确认密码
     * @param request         HTTP请求
     * @return 清空结果
     */
    @DeleteMapping("/clear-all")
    @Operation(
            summary = "清空用户所有ak,sk",
            description = "清空指定用户的所有访问密钥（需要确认密码）",
            responses = {
                    @ApiResponse(responseCode = "200", description = "清空成功"),
                    @ApiResponse(responseCode = "400", description = "参数错误"),
                    @ApiResponse(responseCode = "401", description = "密码验证失败"),
                    @ApiResponse(responseCode = "403", description = "无权限操作")
            }
    )
    public ReturnResult<Integer> clearAllAccessKeys(
            @Parameter(description = "用户ID", required = true)
            @RequestParam String userId,
            @Parameter(description = "确认密码", required = true)
            @RequestParam String confirmPassword,
            HttpServletRequest request) {

        log.info("清空用户所有ak,sk - 用户ID: {}, IP: {}", userId, getClientIp(request));

        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(confirmPassword)) {
            return ReturnResult.illegal("用户ID和确认密码不能为空");
        }

        try {
            int deletedCount = accessKeyService.clearAllAccessKeys(userId, confirmPassword);
            log.info("成功清空用户所有ak,sk - 用户ID: {}, 删除数量: {}", userId, deletedCount);
            return ReturnResult.ok(deletedCount, "成功清空" + deletedCount + "个访问密钥");
        } catch (Exception e) {
            log.error("清空用户所有ak,sk失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return ReturnResult.error("清空ak,sk失败: " + e.getMessage());
        }
    }

    /**
     * 创建新的访问密钥
     *
     * @param userId      用户ID
     * @param description 密钥描述
     * @param request     HTTP请求
     * @return 创建结果
     */
    @PostMapping("/create")
    @Operation(
            summary = "创建新的访问密钥",
            description = "为用户创建新的ak,sk访问密钥",
            responses = {
                    @ApiResponse(responseCode = "200", description = "创建成功"),
                    @ApiResponse(responseCode = "400", description = "参数错误"),
                    @ApiResponse(responseCode = "403", description = "无权限创建")
            }
    )
    public ReturnResult<AccessSecret> createAccessKey(
            @Parameter(description = "用户ID", required = true)
            @RequestParam String userId,
            @Parameter(description = "密钥描述")
            @RequestParam(required = false) String description,
            HttpServletRequest request) {

        log.info("创建访问密钥 - 用户ID: {}, 描述: {}, IP: {}", userId, description, getClientIp(request));

        if (StringUtils.isEmpty(userId)) {
            return ReturnResult.illegal("用户ID不能为空");
        }

        try {
            AccessSecret accessSecret = accessKeyService.createAccessKey(userId, description);
            if (accessSecret != null) {
                log.info("成功创建访问密钥 - 用户ID: {}, AccessKey: {}", userId, accessSecret.getAccessKey());
                return ReturnResult.ok(accessSecret, "创建成功");
            } else {
                log.warn("创建访问密钥失败 - 用户ID: {}", userId);
                return ReturnResult.error("创建访问密钥失败");
            }
        } catch (Exception e) {
            log.error("创建访问密钥失败 - 用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return ReturnResult.error("创建访问密钥失败: " + e.getMessage());
        }
    }

    /**
     * 切换访问密钥状态
     *
     * @param userId    用户ID
     * @param accessKey 访问密钥
     * @param enabled   是否启用
     * @param request   HTTP请求
     * @return 操作结果
     */
    @PutMapping("/toggle-status")
    @Operation(
            summary = "切换访问密钥状态",
            description = "启用或禁用指定的访问密钥",
            responses = {
                    @ApiResponse(responseCode = "200", description = "操作成功"),
                    @ApiResponse(responseCode = "400", description = "参数错误"),
                    @ApiResponse(responseCode = "404", description = "密钥不存在"),
                    @ApiResponse(responseCode = "403", description = "无权限操作")
            }
    )
    public ReturnResult<Boolean> toggleAccessKeyStatus(
            @Parameter(description = "用户ID", required = true)
            @RequestParam String userId,
            @Parameter(description = "访问密钥", required = true)
            @RequestParam String accessKey,
            @Parameter(description = "是否启用", required = true)
            @RequestParam boolean enabled,
            HttpServletRequest request) {

        log.info("切换访问密钥状态 - 用户ID: {}, AccessKey: {}, 启用: {}, IP: {}",
                userId, accessKey, enabled, getClientIp(request));

        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(accessKey)) {
            return ReturnResult.illegal("用户ID和访问密钥不能为空");
        }

        try {
            boolean success = accessKeyService.setAccessKeyEnabled(userId, accessKey, enabled);
            if (success) {
                log.info("成功切换访问密钥状态 - 用户ID: {}, AccessKey: {}, 启用: {}", userId, accessKey, enabled);
                return ReturnResult.ok(true, "状态更新成功");
            } else {
                log.warn("切换访问密钥状态失败 - 用户ID: {}, AccessKey: {}", userId, accessKey);
                return ReturnResult.error("状态更新失败，密钥不存在或无权限");
            }
        } catch (Exception e) {
            log.error("切换访问密钥状态失败 - 用户ID: {}, AccessKey: {}, 错误: {}", userId, accessKey, e.getMessage(), e);
            return ReturnResult.error("状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.isNotEmpty(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
