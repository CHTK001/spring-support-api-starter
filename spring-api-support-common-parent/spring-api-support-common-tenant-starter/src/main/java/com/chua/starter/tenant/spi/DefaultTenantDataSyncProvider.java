package com.chua.starter.tenant.spi;

import com.chua.common.support.utils.MapUtils;
import com.chua.starter.entity.system.SysMenu;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.sync.TenantSyncMessageSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 默认租户数据同步提供者。
 * <p>
 * 默认实现优先保证租户端独立库可用：
 * <ul>
 *     <li>租户用户同步采用幂等 upsert/delete</li>
 *     <li>菜单全量同步采用 replace 语义</li>
 *     <li>仅收到 menuIds 时只做租户菜单裁剪，不再依赖本地存在平台主菜单模板</li>
 * </ul>
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

    private static final String OP_CREATE = TenantSyncMessageSupport.OP_CREATE;
    private static final String OP_UPDATE = TenantSyncMessageSupport.OP_UPDATE;
    private static final String OP_DELETE = TenantSyncMessageSupport.OP_DELETE;

    private final JdbcTemplate jdbcTemplate;
    private final TenantProperties tenantProperties;

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser syncTenantUser(SysTenant tenant, String operation) {
        if (tenant == null || tenant.getSysTenantId() == null) {
            log.warn("[租户同步] 租户信息不完整，无法同步用户");
            return null;
        }

        String tenantId = tenant.getSysTenantId();
        String tenantIdColumn = tenantProperties.getEffectiveTenantIdColumn();

        try {
            return switch (normalizeOperation(operation)) {
                case OP_CREATE -> createTenantUser(tenant, tenantIdColumn);
                case OP_UPDATE -> updateTenantUser(tenant, tenantIdColumn);
                case OP_DELETE -> {
                    deleteTenantUser(tenantId, tenantIdColumn);
                    yield null;
                }
                default -> {
                    log.warn("[租户同步] 未知租户用户操作类型: {}", operation);
                    yield null;
                }
            };
        } catch (Exception e) {
            log.error("[租户同步] 同步租户用户失败: tenantId={}, operation={}", tenantId, operation, e);
            return null;
        }
    }

    private SysUser createTenantUser(SysTenant tenant, String tenantIdColumn) {
        String checkSql = String.format("SELECT COUNT(*) FROM sys_user WHERE %s = ?", tenantIdColumn);
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tenant.getSysTenantId());

        if (count != null && count > 0) {
            log.info("[租户同步] 租户用户已存在，执行更新: tenantId={}", tenant.getSysTenantId());
            return updateTenantUser(tenant, tenantIdColumn);
        }

        String insertSql = String.format("""
                INSERT INTO sys_user (
                    sys_user_nickname, sys_user_username, sys_user_password,
                    sys_user_phone, sys_user_email, sys_user_status,
                    %s, create_time, update_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """, tenantIdColumn);

        jdbcTemplate.update(insertSql,
                tenant.getSysTenantName(),
                tenant.getSysTenantUsername(),
                tenant.getSysTenantPassword(),
                tenant.getSysTenantPhone(),
                tenant.getSysTenantEmail(),
                tenant.getSysTenantStatus() == null ? 0 : tenant.getSysTenantStatus(),
                tenant.getSysTenantId()
        );

        log.info("[租户同步] 创建租户用户成功: tenantId={}, username={}",
                tenant.getSysTenantId(), tenant.getSysTenantUsername());
        return toSysUser(tenant);
    }

    private SysUser updateTenantUser(SysTenant tenant, String tenantIdColumn) {
        StringBuilder updateSql = new StringBuilder("""
                UPDATE sys_user SET
                    sys_user_nickname = ?,
                    sys_user_username = ?,
                    sys_user_phone = ?,
                    sys_user_email = ?,
                    sys_user_status = ?
                """);
        List<Object> params = new ArrayList<>();
        params.add(tenant.getSysTenantName());
        params.add(tenant.getSysTenantUsername());
        params.add(tenant.getSysTenantPhone());
        params.add(tenant.getSysTenantEmail());
        params.add(tenant.getSysTenantStatus());

        if (TenantSyncMessageSupport.hasText(tenant.getSysTenantPassword())) {
            updateSql.append(", sys_user_password = ?");
            params.add(tenant.getSysTenantPassword());
        }

        updateSql.append(", update_time = NOW() WHERE ").append(tenantIdColumn).append(" = ?");
        params.add(tenant.getSysTenantId());

        int rows = jdbcTemplate.update(updateSql.toString(), params.toArray());
        if (rows == 0) {
            log.info("[租户同步] 租户用户不存在，转为创建: tenantId={}", tenant.getSysTenantId());
            return createTenantUser(tenant, tenantIdColumn);
        }

        log.info("[租户同步] 更新租户用户: tenantId={}, rows={}", tenant.getSysTenantId(), rows);
        return toSysUser(tenant);
    }

    private void deleteTenantUser(String tenantId, String tenantIdColumn) {
        String deleteSql = String.format("DELETE FROM sys_user WHERE %s = ?", tenantIdColumn);
        int rows = jdbcTemplate.update(deleteSql, tenantId);
        log.info("[租户同步] 删除租户用户: tenantId={}, rows={}", tenantId, rows);
    }

    private SysUser toSysUser(SysTenant tenant) {
        SysUser user = new SysUser();
        user.setSysUserNickname(tenant.getSysTenantName());
        user.setSysUserUsername(tenant.getSysTenantUsername());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncTenantMenu(String tenantId, List<Integer> menuIds) {
        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            log.warn("[租户同步] 租户ID为空，无法同步菜单");
            return false;
        }

        String tenantIdColumn = tenantProperties.getEffectiveTenantIdColumn();

        try {
            if (menuIds == null || menuIds.isEmpty()) {
                String deleteSql = String.format("DELETE FROM sys_menu WHERE %s = ?", tenantIdColumn);
                jdbcTemplate.update(deleteSql, tenantId);
                log.info("[租户同步] 清空租户菜单: tenantId={}", tenantId);
                return true;
            }

            String placeholders = menuIds.stream().map(menuId -> "?").collect(Collectors.joining(","));
            String deleteSql = String.format(
                    "DELETE FROM sys_menu WHERE %s = ? AND sys_menu_id NOT IN (%s)",
                    tenantIdColumn, placeholders);

            Object[] params = new Object[menuIds.size() + 1];
            params[0] = tenantId;
            for (int i = 0; i < menuIds.size(); i++) {
                params[i + 1] = menuIds.get(i);
            }
            int rows = jdbcTemplate.update(deleteSql, params);

            log.info("[租户同步] 裁剪租户菜单完成: tenantId={}, 保留菜单数={}, 删除数量={}",
                    tenantId, menuIds.size(), rows);
            return true;
        } catch (Exception e) {
            log.error("[租户同步] 同步租户菜单失败: tenantId={}", tenantId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncMenuData(String tenantId, List<Map<String, Object>> menus, String operation) {
        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            log.warn("[租户同步] 租户ID为空，无法同步菜单数据");
            return false;
        }

        String tenantIdColumn = tenantProperties.getEffectiveTenantIdColumn();
        String normalizedOperation = normalizeOperation(operation);
        List<Map<String, Object>> safeMenus = menus == null ? List.of() : menus;

        try {
            switch (normalizedOperation) {
                case OP_CREATE, OP_UPDATE -> {
                    for (Map<String, Object> menu : safeMenus) {
                        upsertMenu(tenantId, menu, tenantIdColumn);
                    }
                }
                case OP_DELETE -> {
                    for (Map<String, Object> menu : safeMenus) {
                        deleteMenu(tenantId, menu, tenantIdColumn);
                    }
                }
                case TenantSyncMessageSupport.OP_REPLACE, TenantSyncMessageSupport.MODE_FULL -> replaceMenus(tenantId, safeMenus, tenantIdColumn);
                default -> {
                    log.warn("[租户同步] 未知菜单操作类型: {}", operation);
                    return false;
                }
            }

            log.info("[租户同步] 同步菜单数据完成: tenantId={}, count={}, operation={}",
                    tenantId, safeMenus.size(), normalizedOperation);
            return true;
        } catch (Exception e) {
            log.error("[租户同步] 同步菜单数据失败: tenantId={}", tenantId, e);
            return false;
        }
    }

    /**
     * 同步租户服务数据
     * <p>
     * 将平台下发的服务、模块、模块项写入租户库, 同时记录租户自身与租户-服务绑定关系
     * </p>
     *
     * @param tenant    租户信息
     * @param services  服务列表
     * @param modules   服务模块列表
     * @param items     服务模块项列表
     * @param operation 操作类型
     * @return 同步结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncServiceData(SysTenant tenant,
                                   List<Map<String, Object>> services,
                                   List<Map<String, Object>> modules,
                                   List<Map<String, Object>> items,
                                   String operation) {
        if (tenant == null || tenant.getSysTenantId() == null) {
            log.warn("[租户同步] 租户信息不完整，无法同步服务");
            return false;
        }
        String tenantId = tenant.getSysTenantId();
        String tenantIdColumn = tenantProperties.getEffectiveTenantIdColumn();
        String op = normalizeOperation(operation);
        try {
            if (OP_DELETE.equals(op)) {
                jdbcTemplate.update("DELETE FROM sys_service WHERE " + tenantIdColumn + " = ?", tenantId);
                jdbcTemplate.update("DELETE FROM sys_service_module WHERE " + tenantIdColumn + " = ?", tenantId);
                jdbcTemplate.update("DELETE FROM sys_service_module_item WHERE " + tenantIdColumn + " = ?", tenantId);
                jdbcTemplate.update("DELETE FROM sys_tenant_service WHERE sys_tenant_id = ?", tenantId);
                return true;
            }

            upsertTenantRow(tenant);
            jdbcTemplate.update("DELETE FROM sys_tenant_service WHERE sys_tenant_id = ?", tenantId);
            if (services != null) {
                for (Map<String, Object> service : services) {
                    Integer serviceId = MapUtils.getInteger(service, "sysServiceId", null);
                    if (serviceId != null) {
                        upsertTenantServiceRow(tenantId, serviceId);
                    }
                    upsertServiceRow(tenantId, service, tenantIdColumn);
                }
            }
            if (modules != null) {
                for (Map<String, Object> module : modules) {
                    upsertModuleRow(tenantId, module, tenantIdColumn);
                }
            }
            if (items != null) {
                for (Map<String, Object> item : items) {
                    upsertItemRow(tenantId, item, tenantIdColumn);
                }
            }
            log.info("[租户同步] 同步租户服务完成: tenantId={}, serviceCount={}, moduleCount={}, itemCount={}",
                    tenantId,
                    services != null ? services.size() : 0,
                    modules != null ? modules.size() : 0,
                    items != null ? items.size() : 0);
            return true;
        } catch (Exception e) {
            log.error("[租户同步] 同步租户服务失败: tenantId={}", tenantId, e);
            return false;
        }
    }

    /**
     * 写入租户自身记录(不存在时插入)
     */
    private void upsertTenantRow(SysTenant tenant) {
        String insertSql = """
                INSERT INTO sys_tenant (
                    sys_tenant_id, sys_tenant_name, sys_tenant_username,
                    sys_tenant_delete, sys_tenant_status, create_time, update_time
                ) VALUES (?, ?, ?, 0, 0, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    sys_tenant_name = VALUES(sys_tenant_name),
                    sys_tenant_username = VALUES(sys_tenant_username),
                    update_time = NOW()
                """;
        jdbcTemplate.update(insertSql,
                tenant.getSysTenantId(), tenant.getSysTenantName(), tenant.getSysTenantUsername());
    }

    /**
     * 写入租户-服务绑定关系
     */
    private void upsertTenantServiceRow(String tenantId, Integer serviceId) {
        String insertSql = """
                INSERT INTO sys_tenant_service (sys_tenant_id, sys_service_id, create_time, update_time)
                VALUES (?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE sys_service_id = VALUES(sys_service_id), update_time = NOW()
                """;
        jdbcTemplate.update(insertSql, tenantId, serviceId);
    }

    /**
     * 写入服务记录
     */
    private void upsertServiceRow(String tenantId, Map<String, Object> service, String tenantIdColumn) {
        String insertSql = String.format("""
                INSERT INTO sys_service (
                    sys_service_id, sys_service_name, sys_service_code, sys_service_desc,
                    sys_service_sort, sys_service_delete, sys_service_status,
                    %s, create_time, update_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    sys_service_name = VALUES(sys_service_name),
                    sys_service_code = VALUES(sys_service_code),
                    sys_service_desc = VALUES(sys_service_desc),
                    sys_service_sort = VALUES(sys_service_sort),
                    sys_service_delete = VALUES(sys_service_delete),
                    sys_service_status = VALUES(sys_service_status),
                    %s = VALUES(%s),
                    update_time = NOW()
                """, tenantIdColumn, tenantIdColumn, tenantIdColumn);
        jdbcTemplate.update(insertSql,
                MapUtils.getInteger(service, "sysServiceId", null),
                MapUtils.getString(service, "sysServiceName"),
                MapUtils.getString(service, "sysServiceCode"),
                MapUtils.getString(service, "sysServiceDesc"),
                MapUtils.getInteger(service, "sysServiceSort", 0),
                MapUtils.getInteger(service, "sysServiceDelete", 0),
                MapUtils.getInteger(service, "sysServiceStatus", 0),
                tenantId);
    }

    /**
     * 写入服务模块记录
     */
    private void upsertModuleRow(String tenantId, Map<String, Object> module, String tenantIdColumn) {
        String insertSql = String.format("""
                INSERT INTO sys_service_module (
                    sys_service_module_id, sys_service_module_name, sys_service_module_code,
                    sys_service_module_desc, sys_service_module_sort, sys_service_module_delete,
                    sys_service_module_status, sys_service_module_type, sys_menu_id,
                    %s, create_time, update_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    sys_service_module_name = VALUES(sys_service_module_name),
                    sys_service_module_code = VALUES(sys_service_module_code),
                    sys_service_module_desc = VALUES(sys_service_module_desc),
                    sys_service_module_sort = VALUES(sys_service_module_sort),
                    sys_service_module_delete = VALUES(sys_service_module_delete),
                    sys_service_module_status = VALUES(sys_service_module_status),
                    sys_service_module_type = VALUES(sys_service_module_type),
                    sys_menu_id = VALUES(sys_menu_id),
                    %s = VALUES(%s),
                    update_time = NOW()
                """, tenantIdColumn, tenantIdColumn, tenantIdColumn);
        jdbcTemplate.update(insertSql,
                MapUtils.getInteger(module, "sysServiceModuleId", null),
                MapUtils.getString(module, "sysServiceModuleName"),
                MapUtils.getString(module, "sysServiceModuleCode"),
                MapUtils.getString(module, "sysServiceModuleDesc"),
                MapUtils.getInteger(module, "sysServiceModuleSort", 0),
                MapUtils.getInteger(module, "sysServiceModuleDelete", 0),
                MapUtils.getInteger(module, "sysServiceModuleStatus", 0),
                MapUtils.getString(module, "sysServiceModuleType"),
                MapUtils.getInteger(module, "sysMenuId", null),
                tenantId);
    }

    /**
     * 写入服务模块项记录
     */
    private void upsertItemRow(String tenantId, Map<String, Object> item, String tenantIdColumn) {
        String insertSql = String.format("""
                INSERT INTO sys_service_module_item (
                    sys_service_module_item_id, sys_service_id, sys_service_module_id, sys_menu_id,
                    %s, create_time, update_time
                ) VALUES (?, ?, ?, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    sys_service_id = VALUES(sys_service_id),
                    sys_service_module_id = VALUES(sys_service_module_id),
                    sys_menu_id = VALUES(sys_menu_id),
                    %s = VALUES(%s),
                    update_time = NOW()
                """, tenantIdColumn, tenantIdColumn, tenantIdColumn);
        jdbcTemplate.update(insertSql,
                MapUtils.getInteger(item, "sysServiceModuleItemId", null),
                MapUtils.getInteger(item, "sysServiceId", null),
                MapUtils.getInteger(item, "sysServiceModuleId", null),
                MapUtils.getInteger(item, "sysMenuId", null),
                tenantId);
    }

    private void replaceMenus(String tenantId, List<Map<String, Object>> menus, String tenantIdColumn) {
        List<Integer> keepIds = new ArrayList<>();
        for (Map<String, Object> menu : menus) {
            SysMenu sysMenu = toSysMenu(menu);
            if (sysMenu.getSysMenuId() == null) {
                continue;
            }
            keepIds.add(sysMenu.getSysMenuId());
            // upsert: 新增的插入，存在的更新（含 hidden/状态等字段），避免清空重录
            upsertMenu(tenantId, menu, tenantIdColumn);
        }

        // 清理平台已删除的菜单：删除当前租户下不在推送列表中的菜单记录
        if (keepIds.isEmpty()) {
            String deleteSql = String.format("DELETE FROM sys_menu WHERE %s = ?", tenantIdColumn);
            jdbcTemplate.update(deleteSql, tenantId);
            return;
        }
        String placeholders = keepIds.stream().map(menuId -> "?").collect(Collectors.joining(","));
        String deleteSql = String.format(
                "DELETE FROM sys_menu WHERE %s = ? AND sys_menu_id NOT IN (%s)",
                tenantIdColumn, placeholders);
        Object[] params = new Object[keepIds.size() + 1];
        params[0] = tenantId;
        for (int i = 0; i < keepIds.size(); i++) {
            params[i + 1] = keepIds.get(i);
        }
        jdbcTemplate.update(deleteSql, params);
    }

    private void upsertMenu(String tenantId, Map<String, Object> menu, String tenantIdColumn) {
        SysMenu sysMenu = toSysMenu(menu);
        if (sysMenu.getSysMenuId() == null) {
            log.warn("[租户同步] 菜单ID为空，忽略数据: {}", menu);
            return;
        }

        String checkSql = "SELECT COUNT(*) FROM sys_menu WHERE sys_menu_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, sysMenu.getSysMenuId());

        if (count != null && count > 0) {
            updateMenu(tenantId, sysMenu, tenantIdColumn);
            return;
        }
        insertMenu(tenantId, sysMenu, tenantIdColumn);
    }

    private void updateMenu(String tenantId, SysMenu menu, String tenantIdColumn) {
        String updateSql = String.format("""
                UPDATE sys_menu SET
                    %s = ?,
                    sys_menu_name = ?, sys_menu_path = ?, sys_menu_i18n = ?, sys_menu_title = ?,
                    sys_menu_perm = ?, sys_menu_pid = ?, sys_menu_tree_id = ?, sys_menu_component = ?,
                    sys_menu_icon = ?, sys_menu_extra_icon = ?, sys_menu_type = ?, sys_menu_sort = ?,
                    sys_menu_hidden = ?, sys_menu_show_parent = ?, sys_menu_hidden_tag = ?, sys_menu_fixed_tag = ?,
                    sys_menu_redirect = ?, sys_menu_enter_transition = ?, sys_menu_leave_transition = ?,
                    sys_menu_active_path = ?, sys_menu_frame_src = ?, sys_menu_keep_alive = ?,
                    sys_menu_role = ?, sys_menu_home = ?, sys_menu_engine = ?, update_time = NOW()
                WHERE sys_menu_id = ?
                """, tenantIdColumn);

        jdbcTemplate.update(updateSql,
                tenantId,
                menu.getSysMenuName(), menu.getSysMenuPath(), menu.getSysMenuI18n(), menu.getSysMenuTitle(),
                menu.getSysMenuPerm(), menu.getSysMenuPid(), menu.getSysMenuTreeId(), menu.getSysMenuComponent(),
                menu.getSysMenuIcon(), menu.getSysMenuExtraIcon(), menu.getSysMenuType(), menu.getSysMenuSort(),
                menu.getSysMenuHidden(), menu.getSysMenuShowParent(), menu.getSysMenuHiddenTag(), menu.getSysMenuFixedTag(),
                menu.getSysMenuRedirect(), menu.getSysMenuEnterTransition(), menu.getSysMenuLeaveTransition(),
                menu.getSysMenuActivePath(), menu.getSysMenuFrameSrc(), menu.getSysMenuKeepAlive(),
                menu.getSysMenuRole(), menu.getSysMenuHome(), menu.getSysMenuEngine(),
                menu.getSysMenuId());
    }

    private void insertMenu(String tenantId, SysMenu menu, String tenantIdColumn) {
        String insertSql = String.format("""
                INSERT INTO sys_menu (
                    sys_menu_id, sys_menu_name, sys_menu_path, sys_menu_i18n, sys_menu_title,
                    sys_menu_perm, sys_menu_pid, sys_menu_tree_id, sys_menu_component,
                    sys_menu_icon, sys_menu_extra_icon, sys_menu_type, sys_menu_sort,
                    sys_menu_hidden, sys_menu_show_parent, sys_menu_hidden_tag, sys_menu_fixed_tag,
                    sys_menu_redirect, sys_menu_enter_transition, sys_menu_leave_transition, sys_menu_active_path,
                    sys_menu_frame_src, sys_menu_keep_alive, sys_menu_role, sys_menu_home,
                    sys_menu_engine, %s, create_time, update_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """, tenantIdColumn);

        jdbcTemplate.update(insertSql,
                menu.getSysMenuId(), menu.getSysMenuName(), menu.getSysMenuPath(), menu.getSysMenuI18n(), menu.getSysMenuTitle(),
                menu.getSysMenuPerm(), menu.getSysMenuPid(), menu.getSysMenuTreeId(), menu.getSysMenuComponent(),
                menu.getSysMenuIcon(), menu.getSysMenuExtraIcon(), menu.getSysMenuType(), menu.getSysMenuSort(),
                menu.getSysMenuHidden(), menu.getSysMenuShowParent(), menu.getSysMenuHiddenTag(), menu.getSysMenuFixedTag(),
                menu.getSysMenuRedirect(), menu.getSysMenuEnterTransition(), menu.getSysMenuLeaveTransition(), menu.getSysMenuActivePath(),
                menu.getSysMenuFrameSrc(), menu.getSysMenuKeepAlive(), menu.getSysMenuRole(), menu.getSysMenuHome(),
                menu.getSysMenuEngine(), tenantId);
    }

    private void deleteMenu(String tenantId, Map<String, Object> menu, String tenantIdColumn) {
        Integer menuId = MapUtils.getInteger(menu, "sysMenuId", 0);
        if (menuId == 0) {
            return;
        }
        String deleteSql = String.format("DELETE FROM sys_menu WHERE sys_menu_id = ? AND %s = ?", tenantIdColumn);
        jdbcTemplate.update(deleteSql, menuId, tenantId);
    }

    private SysMenu toSysMenu(Map<String, Object> menu) {
        SysMenu sysMenu = new SysMenu();
        if (menu == null) {
            return sysMenu;
        }

        sysMenu.setSysMenuId(MapUtils.getInteger(menu, "sysMenuId", null));
        sysMenu.setSysMenuName(MapUtils.getString(menu, "sysMenuName"));
        sysMenu.setSysMenuPath(MapUtils.getString(menu, "sysMenuPath"));
        sysMenu.setSysMenuI18n(MapUtils.getString(menu, "sysMenuI18n"));
        sysMenu.setSysMenuTitle(MapUtils.getString(menu, "sysMenuTitle"));
        sysMenu.setSysMenuPerm(MapUtils.getString(menu, "sysMenuPerm"));
        sysMenu.setSysMenuPid(MapUtils.getInteger(menu, "sysMenuPid", 0));
        sysMenu.setSysMenuTreeId(MapUtils.getString(menu, "sysMenuTreeId"));
        sysMenu.setSysMenuComponent(MapUtils.getString(menu, "sysMenuComponent"));
        sysMenu.setSysMenuIcon(MapUtils.getString(menu, "sysMenuIcon"));
        sysMenu.setSysMenuExtraIcon(MapUtils.getString(menu, "sysMenuExtraIcon"));
        sysMenu.setSysMenuType(MapUtils.getInteger(menu, "sysMenuType", 0));
        sysMenu.setSysMenuSort(MapUtils.getInteger(menu, "sysMenuSort", 0));
        sysMenu.setSysMenuHidden(MapUtils.getInteger(menu, "sysMenuHidden", 0));
        sysMenu.setSysMenuShowParent(MapUtils.getInteger(menu, "sysMenuShowParent", 0));
        sysMenu.setSysMenuHiddenTag(MapUtils.getInteger(menu, "sysMenuHiddenTag", 0));
        sysMenu.setSysMenuFixedTag(MapUtils.getInteger(menu, "sysMenuFixedTag", 0));
        sysMenu.setSysMenuRedirect(MapUtils.getString(menu, "sysMenuRedirect"));
        sysMenu.setSysMenuEnterTransition(MapUtils.getString(menu, "sysMenuEnterTransition"));
        sysMenu.setSysMenuLeaveTransition(MapUtils.getString(menu, "sysMenuLeaveTransition"));
        sysMenu.setSysMenuActivePath(MapUtils.getString(menu, "sysMenuActivePath"));
        sysMenu.setSysMenuFrameSrc(MapUtils.getString(menu, "sysMenuFrameSrc"));
        sysMenu.setSysMenuKeepAlive(MapUtils.getInteger(menu, "sysMenuKeepAlive", 0));
        sysMenu.setSysMenuRole(MapUtils.getString(menu, "sysMenuRole"));
        sysMenu.setSysMenuHome(MapUtils.getInteger(menu, "sysMenuHome", 0));
        sysMenu.setSysMenuEngine(MapUtils.getInteger(menu, "sysMenuEngine", 0));
        return sysMenu;
    }

    private String normalizeOperation(String operation) {
        if (!TenantSyncMessageSupport.hasText(operation)) {
            return OP_UPDATE;
        }
        return operation.trim().toLowerCase();
    }
}
