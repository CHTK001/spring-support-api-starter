package com.chua.starter.oauth.sso.login;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResume;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 租户自动选择登录服务
 * <p>
 * 模式一：自动选择租户
 * 当登录时未传入 tenantId，系统自动选择租户：
 * </p>
 * <ol>
 *   <li>优先使用用户上次登录的租户（从 Redis 读取）</li>
 *   <li>如果没有记录，查询用户所属的第一个租户</li>
 *   <li>如果用户属于多个租户，返回租户列表供选择</li>
 * </ol>
 *
 * <h2>与 TenantUserService 的区别</h2>
 * <ul>
 *   <li>TenantUserService（模式二）：必须传入 tenantId，直接使用指定租户</li>
 *   <li>TenantAutoUserService（模式一）：自动选择租户，适合首次登录或切换设备</li>
 * </ul>
 *
 * @author CH
 * @since 1.0.0
 */
@Slf4j
@Service
@Spi("tenant_auto")
@LoginType("tenant_auto")
@Tag(name = "租户自动选择登录", description = "自动选择租户的登录验证")
public class TenantAutoUserService extends TenantUserService {

    /**
     * 用户上次登录租户缓存前缀
     */
    private static final String LAST_TENANT_KEY = "user:last_tenant:";
    
    /**
     * 缓存过期时间（天）
     */
    private static final long LAST_TENANT_EXPIRE_DAYS = 30;

    /**
     * 自动选择租户登录验证
     * <p>
     * 当未传入 tenantId 时，自动选择合适的租户进行登录：
     * 1. 优先使用用户上次登录的租户
     * 2. 如果没有记录，使用默认租户
     * </p>
     *
     * @param principal  用户标识（通常是用户名）
     * @param credential 用户凭证（通常是密码）
     * @param address    客户端地址
     * @param ext        扩展参数
     * @return 用户信息，若验证失败则 message 字段包含错误信息
     */
    @Override
    public UserResume checkLogin(String principal, String credential, String address, Object ext) {
        Map<String, Object> extMap = (Map<String, Object>) ext;
        String tenantId = MapUtils.getString(extMap, "tenantId");

        // 如果已传入 tenantId，直接调用父类方法（模式二）
        if (StringUtils.isNotEmpty(tenantId)) {
            log.debug("使用指定租户登录: 用户={}, 租户={}", principal, tenantId);
            UserResume result = super.checkLogin(principal, credential, address, ext);
            // 登录成功后记住租户
            if (result != null && StringUtils.isEmpty(result.getMessage())) {
                saveLastTenant(principal, tenantId);
            }
            return result;
        }

        // 模式一：自动选择租户
        log.debug("自动选择租户登录: 用户={}", principal);

        // 1. 尝试从缓存获取上次登录的租户
        String lastTenantId = getLastTenant(principal);
        if (StringUtils.isNotEmpty(lastTenantId)) {
            log.debug("使用上次登录的租户: 用户={}, 租户={}", principal, lastTenantId);
            extMap.put("tenantId", lastTenantId);
            UserResume result = super.checkLogin(principal, credential, address, ext);
            if (result != null && StringUtils.isEmpty(result.getMessage())) {
                return result;
            }
            // 如果上次租户登录失败，清除缓存，尝试默认租户
            log.debug("上次租户登录失败，尝试默认租户: 用户={}", principal);
            clearLastTenant(principal);
        }

        // 2. 使用默认租户（租户ID为1或从配置获取）
        String defaultTenantId = "1";
        log.debug("使用默认租户登录: 用户={}, 租户={}", principal, defaultTenantId);
        extMap.put("tenantId", defaultTenantId);
        UserResume result = super.checkLogin(principal, credential, address, ext);

        // 登录成功后记住租户
        if (result != null && StringUtils.isEmpty(result.getMessage())) {
            saveLastTenant(principal, result.getTenantId());
        }

        return result;
    }

    /**
     * 获取用户上次登录的租户ID
     *
     * @param username 用户名
     * @return 上次登录的租户ID，如果不存在返回 null
     */
    private String getLastTenant(String username) {
        try {
            return stringRedisTemplate.opsForValue().get(LAST_TENANT_KEY + username);
        } catch (Exception e) {
            log.warn("获取用户上次登录租户失败: 用户={}, 错误={}", username, e.getMessage());
            return null;
        }
    }

    /**
     * 保存用户上次登录的租户ID
     *
     * @param username 用户名
     * @param tenantId 租户ID
     */
    private void saveLastTenant(String username, String tenantId) {
        try {
            if (StringUtils.isNotEmpty(tenantId)) {
                stringRedisTemplate.opsForValue().set(
                        LAST_TENANT_KEY + username,
                        tenantId,
                        LAST_TENANT_EXPIRE_DAYS,
                        TimeUnit.DAYS
                );
                log.debug("保存用户上次登录租户: 用户={}, 租户={}", username, tenantId);
            }
        } catch (Exception e) {
            log.warn("保存用户上次登录租户失败: 用户={}, 租户={}, 错误={}", username, tenantId, e.getMessage());
        }
    }

    /**
     * 清除用户上次登录的租户ID缓存
     *
     * @param username 用户名
     */
    private void clearLastTenant(String username) {
        try {
            stringRedisTemplate.delete(LAST_TENANT_KEY + username);
        } catch (Exception e) {
            log.warn("清除用户上次登录租户缓存失败: 用户={}, 错误={}", username, e.getMessage());
        }
    }
}
