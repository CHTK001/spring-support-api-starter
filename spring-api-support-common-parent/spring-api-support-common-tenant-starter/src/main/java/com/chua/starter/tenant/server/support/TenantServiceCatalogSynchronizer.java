package com.chua.starter.tenant.server.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.tenant.entity.SysService;
import com.chua.starter.tenant.entity.SysServiceModule;
import com.chua.starter.tenant.entity.SysServiceModuleItem;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.entity.SysTenantService;
import com.chua.starter.tenant.server.mapper.SysServiceMapper;
import com.chua.starter.tenant.server.mapper.SysServiceModuleItemMapper;
import com.chua.starter.tenant.server.mapper.SysServiceModuleMapper;
import com.chua.starter.tenant.server.mapper.SysTenantServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 把平台分配给租户的服务目录同步到租户自己的库。
 *
 * <p>租户使用独立库时，{@code sys_service} / {@code sys_service_module} /
 * {@code sys_service_module_item} 三张表在租户库里是空的：菜单同步只推菜单，
 * 服务目录一直停留在平台库，租户端看不到"自己买了哪些服务"，服务到期也没有可删的记录。</p>
 *
 * <p>本类按「当前有效绑定」增量同步租户库里的这三张表：</p>
 * <ul>
 *   <li>绑定新增 → 服务行写入租户库（按主键 upsert，重复调用不会重复插入）；</li>
 *   <li>绑定取消 / 服务到期 → 只删除平台维护且已不再绑定的服务行及其模块关联；</li>
 *   <li>租户自建的服务行不在平台服务 ID 集合内，不会被同步清理。</li>
 * </ul>
 *
 * <p><b>共享库租户</b>（未登记库地址）：服务目录本来就与平台同一份数据，
 * 直接跳过，不做任何写操作。</p>
 *
 * <p><b>失败语义</b>：同步失败只记告警，不影响绑定接口的主流程 ——
 * 绑定关系已经落平台库，后续任意一次同步或过期任务都会重试。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantServiceCatalogSynchronizer {

    /**
     * 租户数据源注册器
     */
    private final TenantDataSourceRegistry tenantDataSourceRegistry;

    /**
     * 租户 - 服务绑定
     */
    private final SysTenantServiceMapper sysTenantServiceMapper;

    /**
     * 服务
     */
    private final SysServiceMapper sysServiceMapper;

    /**
     * 服务模块
     */
    private final SysServiceModuleMapper sysServiceModuleMapper;

    /**
     * 服务模块关联
     */
    private final SysServiceModuleItemMapper sysServiceModuleItemMapper;

    /**
     * 把当前有效绑定同步到租户库。
     *
     * @param tenant 租户记录
     * @return 写入的服务条数；共享库或库不可用时返回 -1
     */
    public int sync(SysTenant tenant) {
        if (tenant == null || tenant.getSysTenantId() == null) {
            return -1;
        }
        DataSource dataSource = tenantDataSourceRegistry.getOrCreate(tenant);
        if (dataSource == null) {
            log.debug("[租户服务目录] 租户 {} 未使用独立库，服务目录随平台库，无需同步", tenant.getSysTenantId());
            return -1;
        }

        List<Integer> serviceIds = loadValidServiceIds(tenant.getSysTenantId());
        List<SysService> services = serviceIds.isEmpty()
                ? List.of()
                : sysServiceMapper.selectBatchIds(serviceIds);
        List<SysServiceModuleItem> items = serviceIds.isEmpty()
                ? List.of()
                : sysServiceModuleItemMapper.selectList(Wrappers.<SysServiceModuleItem>lambdaQuery()
                        .in(SysServiceModuleItem::getSysServiceId, serviceIds));
        List<Integer> moduleIds = items.stream()
                .map(SysServiceModuleItem::getSysServiceModuleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<SysServiceModule> modules = moduleIds.isEmpty()
                ? List.of()
                : sysServiceModuleMapper.selectBatchIds(moduleIds);

        // 平台维护的全部服务（含逻辑删除），用于判定哪些行可以由同步任务清理
        List<Integer> managedServiceIds = sysServiceMapper.selectAllServiceIdsIncludingDeleted();

        try {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            List<Integer> desiredServiceIds = services.stream()
                    .map(SysService::getSysServiceId)
                    .toList();
            List<Integer> removedServiceIds = managedServiceIds.stream()
                    .filter(id -> !desiredServiceIds.contains(id))
                    .toList();
            // 先记下被移除服务占用的模块，删完关联后再清理没人引用的模块
            List<Integer> removedModuleIds = loadModuleIds(jdbc, removedServiceIds);
            deleteItems(jdbc, removedServiceIds);
            deleteServices(jdbc, removedServiceIds);
            deleteOrphanModules(jdbc, removedModuleIds);
            // 本次要同步的服务先清掉旧关联，服务行本身用 upsert，避免删除重建
            deleteItems(jdbc, desiredServiceIds);

            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            List<Object[]> serviceRows = new ArrayList<>(services.size());
            for (SysService service : services) {
                serviceRows.add(new Object[]{
                        service.getSysServiceId(),
                        service.getSysServiceName(),
                        service.getSysServiceCode(),
                        service.getSysServiceDesc(),
                        defaultInt(service.getSysServiceSort()),
                        defaultInt(service.getSysServiceDelete()),
                        defaultInt(service.getSysServiceStatus()),
                        now
                });
            }
            if (!serviceRows.isEmpty()) {
                jdbc.batchUpdate("INSERT INTO sys_service "
                        + "(sys_service_id, sys_service_name, sys_service_code, sys_service_desc, "
                        + "sys_service_sort, sys_service_delete, sys_service_status, create_time) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE "
                        + "sys_service_name = VALUES(sys_service_name), "
                        + "sys_service_code = VALUES(sys_service_code), "
                        + "sys_service_desc = VALUES(sys_service_desc), "
                        + "sys_service_sort = VALUES(sys_service_sort), "
                        + "sys_service_delete = VALUES(sys_service_delete), "
                        + "sys_service_status = VALUES(sys_service_status)", serviceRows);
            }

            List<Object[]> moduleRows = new ArrayList<>(modules.size());
            for (SysServiceModule module : modules) {
                moduleRows.add(new Object[]{
                        module.getSysServiceModuleId(),
                        module.getSysServiceModuleName(),
                        module.getSysServiceModuleCode(),
                        module.getSysServiceModuleDesc(),
                        defaultInt(module.getSysServiceModuleSort()),
                        defaultInt(module.getSysServiceModuleDelete()),
                        defaultInt(module.getSysServiceModuleStatus()),
                        module.getSysServiceModuleType(),
                        module.getSysMenuId(),
                        now
                });
            }
            if (!moduleRows.isEmpty()) {
                jdbc.batchUpdate("INSERT INTO sys_service_module "
                        + "(sys_service_module_id, sys_service_module_name, sys_service_module_code, "
                        + "sys_service_module_desc, sys_service_module_sort, sys_service_module_delete, "
                        + "sys_service_module_status, sys_service_module_type, sys_menu_id, create_time) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE "
                        + "sys_service_module_name = VALUES(sys_service_module_name), "
                        + "sys_service_module_code = VALUES(sys_service_module_code), "
                        + "sys_service_module_desc = VALUES(sys_service_module_desc), "
                        + "sys_service_module_sort = VALUES(sys_service_module_sort), "
                        + "sys_service_module_delete = VALUES(sys_service_module_delete), "
                        + "sys_service_module_status = VALUES(sys_service_module_status), "
                        + "sys_service_module_type = VALUES(sys_service_module_type), "
                        + "sys_menu_id = VALUES(sys_menu_id)", moduleRows);
            }

            // 只保留「服务与模块都在本次同步集合内」的关联，避免孤立行
            List<Integer> serviceIdSet = services.stream().map(SysService::getSysServiceId).toList();
            List<Integer> moduleIdSet = modules.stream().map(SysServiceModule::getSysServiceModuleId).toList();
            List<Object[]> itemRows = new ArrayList<>(items.size());
            for (SysServiceModuleItem item : items) {
                if (!serviceIdSet.contains(item.getSysServiceId())
                        || !moduleIdSet.contains(item.getSysServiceModuleId())) {
                    continue;
                }
                itemRows.add(new Object[]{
                        item.getSysServiceId(),
                        item.getSysServiceModuleId(),
                        item.getSysMenuId(),
                        now
                });
            }
            if (!itemRows.isEmpty()) {
                jdbc.batchUpdate("INSERT INTO sys_service_module_item "
                        + "(sys_service_id, sys_service_module_id, sys_menu_id, create_time) "
                        + "VALUES (?, ?, ?, ?)", itemRows);
            }

            log.info("[租户服务目录] 已同步到租户库: tenantId={}, services={}, modules={}, items={}",
                    tenant.getSysTenantId(), serviceRows.size(), moduleRows.size(), itemRows.size());
            return serviceRows.size();
        } catch (Exception e) {
            // 租户库还没建表、连不上、表结构不一致都不该让绑定接口失败
            log.warn("[租户服务目录] 同步租户库失败，已跳过: tenantId={}, error={}",
                    tenant.getSysTenantId(), e.getMessage());
            return -1;
        }
    }

    /**
     * 读取租户当前有效的服务绑定。
     *
     * <p>到期时间按「当天仍有效」判定：{@code valid_time} 为 null 表示长期有效，
     * 等于今天也仍可继续使用，次日才算过期。</p>
     *
     * @param tenantId 租户标识
     * @return 服务标识列表
     */
    private List<Integer> loadValidServiceIds(String tenantId) {
        LocalDate today = LocalDate.now();
        List<SysTenantService> binds = sysTenantServiceMapper.selectList(
                Wrappers.<SysTenantService>lambdaQuery()
                        .eq(SysTenantService::getSysTenantId, tenantId));
        if (binds == null || binds.isEmpty()) {
            return List.of();
        }
        return binds.stream()
                .filter(bind -> bind.getSysTenantServiceValidTime() == null
                        || !bind.getSysTenantServiceValidTime().isBefore(today))
                .map(SysTenantService::getSysServiceId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 空值按 0 处理，与表默认值保持一致。
     *
     * @param value 原值
     * @return 非空返回原值，否则返回 0
     */
    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 按服务 ID 删除模块关联。
     *
     * @param jdbc JDBC 操作器
     * @param serviceIds 服务 ID 列表
     */
    private void deleteItems(JdbcTemplate jdbc, List<Integer> serviceIds) {
        if (serviceIds.isEmpty()) {
            return;
        }
        jdbc.update("DELETE FROM sys_service_module_item WHERE sys_service_id IN ("
                + placeholders(serviceIds.size()) + ")", serviceIds.toArray());
    }

    /**
     * 按服务 ID 删除服务行。
     *
     * @param jdbc JDBC 操作器
     * @param serviceIds 服务 ID 列表
     */
    private void deleteServices(JdbcTemplate jdbc, List<Integer> serviceIds) {
        if (serviceIds.isEmpty()) {
            return;
        }
        jdbc.update("DELETE FROM sys_service WHERE sys_service_id IN ("
                + placeholders(serviceIds.size()) + ")", serviceIds.toArray());
    }

    /**
     * 生成 IN 子句占位符。
     *
     * @param size 参数个数
     * @return 以逗号分隔的 {@code ?} 串
     */
    private String placeholders(int size) {
        return String.join(",", Collections.nCopies(size, "?"));
    }

    /**
     * 查询一批服务在租户库里关联的模块 ID。
     *
     * @param jdbc JDBC 操作器
     * @param serviceIds 服务 ID 列表
     * @return 模块 ID 列表
     */
    private List<Integer> loadModuleIds(JdbcTemplate jdbc, List<Integer> serviceIds) {
        if (serviceIds.isEmpty()) {
            return List.of();
        }
        List<Integer> moduleIds = jdbc.queryForList(
                "SELECT DISTINCT sys_service_module_id FROM sys_service_module_item "
                        + "WHERE sys_service_id IN (" + placeholders(serviceIds.size()) + ")",
                Integer.class, serviceIds.toArray());
        return moduleIds == null ? List.of() : moduleIds;
    }

    /**
     * 删除已无任何服务引用的模块。
     *
     * @param jdbc JDBC 操作器
     * @param moduleIds 候选模块 ID 列表
     */
    private void deleteOrphanModules(JdbcTemplate jdbc, List<Integer> moduleIds) {
        if (moduleIds.isEmpty()) {
            return;
        }
        jdbc.update("DELETE FROM sys_service_module WHERE sys_service_module_id IN ("
                        + placeholders(moduleIds.size()) + ")"
                        + " AND sys_service_module_id NOT IN "
                        + "(SELECT sys_service_module_id FROM sys_service_module_item)",
                moduleIds.toArray());
    }
}
