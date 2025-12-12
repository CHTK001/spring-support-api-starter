package com.chua.starter.tenant.configuration;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.chua.starter.tenant.properties.TenantProperties;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

/**
 * 租户行处理器实现
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
public class TenantLineHandlerImpl implements TenantLineHandler {

    private final TenantProperties tenantProperties;
    
    /**
     * 是否为客户端模式
     */
    private final boolean clientMode;

    /**
     * 当前租户ID（线程本地变量）
     */
    private static final ThreadLocal<String> CURRENT_TENANT_ID = new ThreadLocal<>();

    /**
     * 构造函数
     *
     * @param tenantProperties 租户配置
     * @param clientMode       是否为客户端模式
     */
    public TenantLineHandlerImpl(TenantProperties tenantProperties, boolean clientMode) {
        this.tenantProperties = tenantProperties;
        this.clientMode = clientMode;
    }

    /**
     * 设置当前租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(String tenantId) {
        CURRENT_TENANT_ID.set(tenantId);
    }

    /**
     * 获取当前租户ID
     *
     * @return 租户ID
     */
    public static String getCurrentTenantIdValue() {
        return CURRENT_TENANT_ID.get();
    }

    /**
     * 清除当前租户ID
     */
    public static void clear() {
        CURRENT_TENANT_ID.remove();
    }

    @Override
    public Expression getTenantId() {
        String tenantId = CURRENT_TENANT_ID.get();
        if (tenantId == null && clientMode) {
            // 客户端模式从配置获取默认租户ID（租户编码，字符串）
            String defaultTenantId = tenantProperties.getClient().getTenantId();
            if (defaultTenantId != null && !defaultTenantId.isEmpty()) {
                tenantId = defaultTenantId;
            }
        }
        return new StringValue(tenantId != null ? tenantId : "");
    }

    @Override
    public String getTenantIdColumn() {
        return tenantProperties.getEffectiveTenantIdColumn();
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return tenantProperties.getEffectiveIgnoreTable().contains(tableName);
    }
}
