package com.chua.starter.tenant.server.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.datasource.support.DataSourceContextSupport;
import com.chua.starter.system.login.TenantLoginSupport;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import com.chua.starter.tenant.server.support.TenantDataSourceResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * {@link TenantLoginSupport} 的租户模块实现。
 *
 * <p>负责三件事：在平台主库按登录账号找到租户、把数据源切到该租户库、用完恢复主库。
 * 系统模块只负责在「平台主库查不到账号」时调用它，因此这里的每个方法都必须容忍
 * 租户不存在、未建独立库、租户库连不上等情形，并给出明确的返回而不是抛异常——
 * 登录链路抛异常会变成 500，用户只会看到「认证异常」。</p>
 *
 * <p><b>为什么只认「登记了独立库」的租户</b>：仍在平台共享库的租户，其 {@code sys_user}
 * 本来就在主库，主库查询已经覆盖；再按租户切一次库反而会查空。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantLoginDatasourceSupport implements TenantLoginSupport {

    /**
     * 租户状态：正常
     */
    private static final int TENANT_STATUS_ENABLED = 0;

    /**
     * 租户查询
     */
    private final SysTenantMapper sysTenantMapper;

    /**
     * 租户数据源注册器
     */
    private final TenantDataSourceRegistry tenantDataSourceRegistry;

    /**
     * 租户库连接信息解析器：判定「是否独立库」与切库共用同一口径，
     * 避免登录说「独立库」而过滤器说「未登记」两边不一致。
     */
    private final TenantDataSourceResolver tenantDataSourceResolver;

    @Override
    public TenantLoginTarget resolveByCode(String tenantCode) {
        if (tenantCode == null || tenantCode.isBlank()) {
            return null;
        }
        String code = tenantCode.trim().toLowerCase(Locale.ROOT);
        try {
            SysTenant tenant = sysTenantMapper.selectOne(Wrappers.<SysTenant>lambdaQuery()
                    .eq(SysTenant::getSysTenantCode, code)
                    .last("limit 1"));
            if (tenant == null) {
                log.debug("[租户登录] 租户编码 {} 不存在", code);
                return null;
            }
            boolean enabled = tenant.getSysTenantStatus() == null
                    || tenant.getSysTenantStatus() == TENANT_STATUS_ENABLED;
            boolean dedicated = tenantDataSourceResolver.isDedicated(tenant);
            log.debug("[租户登录] 编码 {} 命中租户 {}（独立库={}, 启用={}）",
                    code, tenant.getSysTenantId(), dedicated, enabled);
            return new TenantLoginTarget(tenant.getSysTenantId(), code, dedicated, enabled);
        } catch (Exception e) {
            log.warn("[租户登录] 按编码解析租户失败: code={}, {}", code, e.getMessage());
            return null;
        }
    }

    @Override
    public String resolveTenantIdByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        try {
            SysTenant tenant = sysTenantMapper.selectOne(Wrappers.<SysTenant>lambdaQuery()
                    .eq(SysTenant::getSysTenantUsername, username.trim())
                    .last("limit 1"));
            if (tenant == null) {
                log.debug("[租户登录] 账号 {} 不属于任何租户", username);
                return null;
            }
            if (tenant.getSysTenantStatus() != null
                    && tenant.getSysTenantStatus() != TENANT_STATUS_ENABLED) {
                log.warn("[租户登录] 租户已停用，拒绝登录: tenantId={}, username={}",
                        tenant.getSysTenantId(), username);
                return null;
            }
            if (!tenantDataSourceResolver.isDedicated(tenant)) {
                log.debug("[租户登录] 租户 {} 使用共享库，按主库登录处理", tenant.getSysTenantId());
                return null;
            }
            return tenant.getSysTenantId();
        } catch (Exception e) {
            log.warn("[租户登录] 按账号解析租户失败: username={}, {}", username, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean switchTo(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return false;
        }
        try {
            SysTenant tenant = sysTenantMapper.selectById(tenantId);
            if (tenant == null) {
                log.warn("[租户登录] 租户不存在: {}", tenantId);
                return false;
            }
            if (tenantDataSourceRegistry.getOrCreate(tenant) == null) {
                log.warn("[租户登录] 租户库不可用: tenantId={}", tenantId);
                return false;
            }
            DataSourceContextSupport.setDbType(tenantDataSourceRegistry.dataSourceName(tenantId));
            log.debug("[租户登录] 已切到租户库: tenantId={}", tenantId);
            return true;
        } catch (Exception e) {
            log.warn("[租户登录] 切库失败: tenantId={}, {}", tenantId, e.getMessage());
            return false;
        }
    }

    @Override
    public void clear() {
        DataSourceContextSupport.clearDbType();
    }
}
