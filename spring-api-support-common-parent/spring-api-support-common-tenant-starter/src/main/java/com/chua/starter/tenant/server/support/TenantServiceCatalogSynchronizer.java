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
import java.util.List;
import java.util.Objects;

/**
 * 把平台分配给租户的服务目录同步到租户自己的库。
 *
 * <p>租户使用独立库时，{@code sys_service} / {@code sys_service_module} /
 * {@code sys_service_module_item} 三张表在租户库里是空的：菜单同步只推菜单，
 * 服务目录一直停留在平台库，租户端看不到"自己买了哪些服务"，服务到期也没有可删的记录。</p>
 *
 * <p>本类按「当前有效绑定」全量重写租户库里的这三张表：</p>
 * <ul>
 *   <li>绑定新增 → 服务行出现在租户库；</li>
 *   <li>绑定取消 / 服务到期 → 对应服务行从租户库消失；</li>
 *   <li>重复调用安全：先清空再写入，结果只取决于当前绑定。</li>
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

        try {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            jdbc.execute("DELETE FROM sys_service_module_item");
            jdbc.execute("DELETE FROM sys_service_module");
            jdbc.execute("DELETE FROM sys_service");

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
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", serviceRows);
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
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", moduleRows);
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
}
