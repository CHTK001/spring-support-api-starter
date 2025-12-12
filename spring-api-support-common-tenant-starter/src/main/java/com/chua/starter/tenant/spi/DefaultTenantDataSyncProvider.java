package com.chua.starter.tenant.spi;

import com.chua.common.support.utils.MapUtils;
import com.chua.starter.entity.system.SysMenu;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 默认租户数据同步提供者
 * <p>
 * 使用 JDBC 直接操作数据库，同步租户用户和菜单数据
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultTenantDataSyncProvider implements TenantDataSyncProvider {

    private final JdbcTemplate jdbcTemplate;
    private final TenantProperties tenantProperties;

    /**
     * 操作类型常量
     */
    private static final String OP_CREATE = "create";
    private static final String OP_UPDATE = "update";
    private static final String OP_DELETE = "delete";

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public SysUser syncTenantUser(SysTenant tenant, String operation) {
        if (tenant == null || tenant.getSysTenantId() == null) {
            log.warn("[租户同步] 租户信息不完整，无法同步用户");
            return null;
        }

        String tenantId = tenant.getSysTenantId();
        String tenantIdColumn = tenantProperties.getEffectiveTenantIdColumn();

        try {
            return switch (operation) {
                case OP_CREATE -> createTenantUser(tenant, tenantIdColumn);
                case OP_UPDATE -> updateTenantUser(tenant, tenantIdColumn);
                case OP_DELETE -> {
                    deleteTenantUser(tenantId, tenantIdColumn);
                    yield null;
                }
                default -> {
                    log.warn("[租户同步] 未知操作类型: {}", operation);
                    yield null;
                }
            };
        } catch (Exception e) {
            log.error("[租户同步] 同步租户用户失败: tenantId={}, operation={}", tenantId, operation, e);
            return null;
        }
    }

    /**
     * 创建租户用户
     */
    private SysUser createTenantUser(SysTenant tenant, String tenantIdColumn) {
        String checkSql = String.format(
                "SELECT COUNT(*) FROM sys_user WHERE %s = ?", tenantIdColumn);
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tenant.getSysTenantId());

        if (count != null && count > 0) {
            log.info("[租户同步] 租户用户已存在，执行更新: tenantId={}", tenant.getSysTenantId());
            return updateTenantUser(tenant, tenantIdColumn);
        }

        String insertSql = String.format("""
                INSERT INTO sys_user (
                    sys_user_name, sys_user_login_name, sys_user_password, 
                    sys_user_phone, sys_user_email, sys_user_status, 
                    %s, create_time, update_time
                ) VALUES (?, ?, ?, ?, ?, 0, ?, NOW(), NOW())
                """, tenantIdColumn);

        jdbcTemplate.update(insertSql,
                tenant.getSysTenantName(),
                tenant.getSysTenantUsername(),
                tenant.getSysTenantPassword(),
                tenant.getSysTenantPhone(),
                tenant.getSysTenantEmail(),
                tenant.getSysTenantId()
        );

        log.info("[租户同步] 创建租户用户成功: tenantId={}, username={}",
                tenant.getSysTenantId(), tenant.getSysTenantUsername());

        SysUser user = new SysUser();
        user.setSysUserNickname(tenant.getSysTenantName());
        user.setSysUserUsername(tenant.getSysTenantUsername());
        return user;
    }

    /**
     * 更新租户用户
     */
    private SysUser updateTenantUser(SysTenant tenant, String tenantIdColumn) {
        String updateSql = String.format("""
                UPDATE sys_user SET 
                    sys_user_name = ?, 
                    sys_user_login_name = ?, 
                    sys_user_phone = ?, 
                    sys_user_email = ?,
                    sys_user_status = ?,
                    update_time = NOW()
                WHERE %s = ?
                """, tenantIdColumn);

        int rows = jdbcTemplate.update(updateSql,
                tenant.getSysTenantName(),
                tenant.getSysTenantUsername(),
                tenant.getSysTenantPhone(),
                tenant.getSysTenantEmail(),
                tenant.getSysTenantStatus(),
                tenant.getSysTenantId()
        );

        log.info("[租户同步] 更新租户用户: tenantId={}, rows={}", tenant.getSysTenantId(), rows);

        SysUser user = new SysUser();
        user.setSysUserNickname(tenant.getSysTenantName());
        user.setSysUserUsername(tenant.getSysTenantUsername());
        return user;
    }

    /**
     * 删除租户用户
     */
    private void deleteTenantUser(String tenantId, String tenantIdColumn) {
        String deleteSql = String.format(
                "DELETE FROM sys_user WHERE %s = ?", tenantIdColumn);
        int rows = jdbcTemplate.update(deleteSql, tenantId);
        log.info("[租户同步] 删除租户用户: tenantId={}, rows={}", tenantId, rows);
    }

    @Override
    public boolean syncTenantMenu(Integer tenantId, List<Integer> menuIds) {
        if (tenantId == null) {
            log.warn("[租户同步] 租户ID为空，无法同步菜单");
            return false;
        }

        String tenantIdColumn = tenantProperties.getEffectiveTenantIdColumn();

        try {
            // 先删除该租户的所有菜单关联
            String deleteSql = String.format(
                    "DELETE FROM sys_menu WHERE %s = ?", tenantIdColumn);
            jdbcTemplate.update(deleteSql, tenantId);

            if (menuIds == null || menuIds.isEmpty()) {
                log.info("[租户同步] 清空租户菜单: tenantId={}", tenantId);
                return true;
            }

            // 复制菜单数据到租户
            String copySql = String.format("""
                    INSERT INTO sys_menu (
                        sys_menu_parent_id, sys_menu_name, sys_menu_path, 
                        sys_menu_component, sys_menu_icon, sys_menu_sort, 
                        sys_menu_status, sys_menu_type, %s, 
                        create_time, update_time
                    )
                    SELECT 
                        sys_menu_parent_id, sys_menu_name, sys_menu_path, 
                        sys_menu_component, sys_menu_icon, sys_menu_sort, 
                        sys_menu_status, sys_menu_type, ?, 
                        NOW(), NOW()
                    FROM sys_menu 
                    WHERE sys_menu_id IN (%s) AND (%s IS NULL OR %s = 0)
                    """, tenantIdColumn, 
                    String.join(",", menuIds.stream().map(String::valueOf).toList()),
                    tenantIdColumn, tenantIdColumn);

            int rows = jdbcTemplate.update(copySql, tenantId);
            log.info("[租户同步] 同步租户菜单: tenantId={}, menuCount={}, rows={}",
                    tenantId, menuIds.size(), rows);
            return true;
        } catch (Exception e) {
            log.error("[租户同步] 同步租户菜单失败: tenantId={}", tenantId, e);
            return false;
        }
    }

    @Override
    public boolean syncMenuData(Integer tenantId, List<Map<String, Object>> menus, String operation) {
        if (tenantId == null) {
            log.warn("[租户同步] 租户ID为空，无法同步菜单数据");
            return false;
        }

        String tenantIdColumn = tenantProperties.getEffectiveTenantIdColumn();

        try {
            for (Map<String, Object> menu : menus) {
                switch (operation) {
                    case OP_CREATE, OP_UPDATE -> upsertMenu(tenantId, menu, tenantIdColumn);
                    case OP_DELETE -> deleteMenu(tenantId, menu, tenantIdColumn);
                    default -> log.warn("[租户同步] 未知菜单操作类型: {}", operation);
                }
            }
            log.info("[租户同步] 同步菜单数据完成: tenantId={}, count={}, operation={}",
                    tenantId, menus.size(), operation);
            return true;
        } catch (Exception e) {
            log.error("[租户同步] 同步菜单数据失败: tenantId={}", tenantId, e);
            return false;
        }
    }

    /**
     * 插入或更新菜单
     */
    private void upsertMenu(Integer tenantId, Map<String, Object> menu, String tenantIdColumn) {
        Integer menuId = MapUtils.getInteger(menu, "sysMenuId", 0);
        String menuName = MapUtils.getString(menu, "sysMenuName");
        String menuPath = MapUtils.getString(menu, "sysMenuPath");
        String menuComponent = MapUtils.getString(menu, "sysMenuComponent");
        String menuIcon = MapUtils.getString(menu, "sysMenuIcon");
        Integer menuSort = MapUtils.getInteger(menu, "sysMenuSort", 0);
        Integer menuStatus = MapUtils.getInteger(menu, "sysMenuStatus", 0);
        Integer menuType = MapUtils.getInteger(menu, "sysMenuType", 0);
        Integer parentId = MapUtils.getInteger(menu, "sysMenuParentId", 0);

        // 检查是否存在
        String checkSql = String.format(
                "SELECT COUNT(*) FROM sys_menu WHERE sys_menu_id = ? AND %s = ?", tenantIdColumn);
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, menuId, tenantId);

        if (count != null && count > 0) {
            // 更新
            String updateSql = String.format("""
                    UPDATE sys_menu SET 
                        sys_menu_name = ?, sys_menu_path = ?, sys_menu_component = ?,
                        sys_menu_icon = ?, sys_menu_sort = ?, sys_menu_status = ?,
                        sys_menu_type = ?, sys_menu_parent_id = ?, update_time = NOW()
                    WHERE sys_menu_id = ? AND %s = ?
                    """, tenantIdColumn);
            jdbcTemplate.update(updateSql, menuName, menuPath, menuComponent, menuIcon,
                    menuSort, menuStatus, menuType, parentId, menuId, tenantId);
        } else {
            // 插入
            String insertSql = String.format("""
                    INSERT INTO sys_menu (
                        sys_menu_id, sys_menu_name, sys_menu_path, sys_menu_component,
                        sys_menu_icon, sys_menu_sort, sys_menu_status, sys_menu_type,
                        sys_menu_parent_id, %s, create_time, update_time
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                    """, tenantIdColumn);
            jdbcTemplate.update(insertSql, menuId, menuName, menuPath, menuComponent,
                    menuIcon, menuSort, menuStatus, menuType, parentId, tenantId);
        }
    }

    /**
     * 删除菜单
     */
    private void deleteMenu(Integer tenantId, Map<String, Object> menu, String tenantIdColumn) {
        Integer menuId = MapUtils.getInteger(menu, "sysMenuId", 0);
        String deleteSql = String.format(
                "DELETE FROM sys_menu WHERE sys_menu_id = ? AND %s = ?", tenantIdColumn);
        jdbcTemplate.update(deleteSql, menuId, tenantId);
    }
}
