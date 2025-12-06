package com.chua.starter.tenant.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.constant.Action;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysMenu;
import com.chua.starter.system.entity.SysTenant;
import com.chua.starter.system.pojo.tenant.SyncTenantData;
import com.chua.starter.system.pojo.tenant.SyncTenantServiceData;
import com.chua.starter.system.pojo.tenant.SysTenantServiceBindV1Request;
import com.chua.starter.system.pojo.tenant.SysTenantSyncV1Request;
import com.chua.starter.tenant.mapper.SysTenantMapper;
import com.chua.starter.tenant.service.SysTenantService;
import com.chua.starter.tenant.service.SysTenantServiceService;
import com.chua.sync.support.pojo.ClientInfo;
import com.chua.sync.support.server.SyncServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租户服务实现类
 * <p>
 * 提供租户的增删改查、服务绑定、数据同步等功能
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements SysTenantService {

    private final TransactionTemplate transactionTemplate;
    private final SysTenantServiceService sysTenantServiceService;

    @Autowired(required = false)
    private SyncServer syncServer;

    @Override
    public IPage<SysTenant> pageForTenant(Query<SysTenant> query, SysTenant sysTenant) {
        Page<SysTenant> sysServicePage = page(query.createPage(),
                Wrappers.<SysTenant>lambdaQuery()
                        .likeRight(StringUtils.isNotBlank(sysTenant.getSysTenantName()),
                                SysTenant::getSysTenantName, sysTenant.getSysTenantName())
                        .likeRight(StringUtils.isNotBlank(sysTenant.getSysTenantCode()),
                                SysTenant::getSysTenantCode, sysTenant.getSysTenantCode())
                        .orderByDesc(SysTenant::getCreateTime)
        );

        List<SysTenant> records = sysServicePage.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            List<Integer> tenantIds = records.stream().map(SysTenant::getSysTenantId).toList();
            List<com.chua.starter.system.entity.SysTenantService> list = sysTenantServiceService
                    .list(Wrappers.<com.chua.starter.system.entity.SysTenantService>lambdaQuery()
                            .in(com.chua.starter.system.entity.SysTenantService::getSysTenantId, tenantIds));
            Map<Integer, List<com.chua.starter.system.entity.SysTenantService>> collect = list.stream()
                    .collect(Collectors.groupingBy(com.chua.starter.system.entity.SysTenantService::getSysTenantId));
            for (SysTenant record : records) {
                List<com.chua.starter.system.entity.SysTenantService> services =
                        collect.getOrDefault(record.getSysTenantId(), List.of());
                record.setSysTenantService(services);
                record.setServiceIds(services.stream()
                        .map(com.chua.starter.system.entity.SysTenantService::getSysServiceId)
                        .toList());
            }
        }
        return sysServicePage;
    }

    @Override
    public ReturnResult<SysTenant> saveForTenant(SysTenant sysTenant) {
        if (exists(Wrappers.<SysTenant>lambdaQuery()
                .eq(SysTenant::getSysTenantCode, sysTenant.getSysTenantCode()))) {
            return ReturnResult.error("租户编码已存在");
        }
        sysTenant.setSysTenantDelete(0);
        sysTenant.setSysTenantStatus(0);
        return transactionTemplate.execute(it -> {
            boolean save = save(sysTenant);
            if (save) {
                // 绑定服务
                bindTenantService(new SysTenantServiceBindV1Request(
                        sysTenant.getSysTenantId(), sysTenant.getSysTenantService()));
                // 广播租户数据
                broadcastTenantData("tenant", new SyncTenantData(Action.CREATE, sysTenant));
            }
            return ReturnResult.preconditioning(sysTenant).toResult();
        });
    }

    @Override
    public ReturnResult<Boolean> updateForTenant(SysTenant sysTenant) {
        if (exists(Wrappers.<SysTenant>lambdaQuery()
                .eq(SysTenant::getSysTenantCode, sysTenant.getSysTenantCode())
                .ne(SysTenant::getSysTenantId, sysTenant.getSysTenantId()))) {
            return ReturnResult.error("租户编码已存在");
        }
        sysTenant.setSysTenantDelete(null);
        return transactionTemplate.execute(it -> {
            // 绑定服务
            bindTenantService(new SysTenantServiceBindV1Request(
                    sysTenant.getSysTenantId(), sysTenant.getSysTenantService()));
            boolean b = updateById(sysTenant);
            if (b) {
                broadcastTenantData("tenant", new SyncTenantData(Action.UPDATE, sysTenant));
            }
            return ReturnResult.preconditioning(b).toResult();
        });
    }

    @Override
    public ReturnResult<Boolean> deleteForTenant(Long sysServiceId) {
        SysTenant sysTenant = getById(sysServiceId);
        if (null == sysTenant) {
            return ReturnResult.illegal("数据不存在");
        }
        boolean b = removeById(sysServiceId);
        if (b) {
            broadcastTenantData("tenant", new SyncTenantData(Action.DELETE, sysTenant));
        }
        return ReturnResult.preconditioning(b).toResult();
    }

    @Override
    public ReturnResult<Boolean> bindTenantService(SysTenantServiceBindV1Request request) {
        return transactionTemplate.execute(status -> {
            if (CollectionUtils.isNotEmpty(request.getSysServiceIds())) {
                SysTenant sysTenant = baseMapper.selectById(request.getSysTenantId());
                // 删除原有绑定
                sysTenantServiceService.remove(
                        Wrappers.<com.chua.starter.system.entity.SysTenantService>lambdaQuery()
                                .eq(com.chua.starter.system.entity.SysTenantService::getSysTenantId,
                                        request.getSysTenantId()));
                // 创建新绑定
                List<com.chua.starter.system.entity.SysTenantService> list = request.getSysServiceIds().stream()
                        .map(sysServiceModuleId -> {
                            com.chua.starter.system.entity.SysTenantService sysTenantService =
                                    new com.chua.starter.system.entity.SysTenantService();
                            sysTenantService.setSysTenantId(request.getSysTenantId());
                            sysTenantService.setSysServiceId(sysServiceModuleId.getSysServiceId());
                            sysTenantService.setSysTenantServiceValidTime(sysServiceModuleId.getSysTenantServiceValidTime());
                            return sysTenantService;
                        }).toList();
                sysTenantServiceService.saveBatch(list);

                // 获取菜单并广播
                List<SysMenu> menus = sysTenantServiceService.getMenuIds(request.getSysTenantId());
                broadcastTenantData("tenant", new SyncTenantData(Action.UPDATE, sysTenant));
                broadcastTenantData("service", new SyncTenantServiceData(
                        Action.UPDATE,
                        request.getSysTenantId(),
                        menus
                ));
            }
            return ReturnResult.ok();
        });
    }

    @Override
    public ReturnResult<Boolean> syncTenantData(SysTenantSyncV1Request request) {
        return transactionTemplate.execute(status -> {
            Integer sysTenantId = request.getSysTenantId();
            SysTenant sysTenant = baseMapper.selectById(sysTenantId);
            List<SysMenu> list = ObjectUtils.equal(sysTenant.getSysTenantStatus(), 0)
                    ? sysTenantServiceService.getMenuIds(sysTenantId)
                    : Collections.emptyList();

            broadcastTenantData("tenant", new SyncTenantData(Action.UPDATE, sysTenant));
            broadcastTenantData("service", new SyncTenantServiceData(
                    Action.UPDATE,
                    request.getSysTenantId(),
                    list
            ));
            return ReturnResult.ok();
        });
    }

    /**
     * 根据租户编码定向下发数据
     * <p>
     * 查找元数据中 tenantCode 匹配的客户端，并单独发送
     * </p>
     *
     * @param topic 主题
     * @param data  数据
     */
    private void broadcastTenantData(String topic, Object data) {
        if (syncServer == null) {
            log.debug("[Tenant] SyncServer 未配置，跳过数据同步");
            return;
        }

        // 提取租户编码
        String tenantCode = extractTenantCode(data);
        if (tenantCode == null) {
            // 无法提取租户编码，广播给所有客户端
            log.debug("[Tenant] 无法提取租户编码，广播给所有客户端");
            syncServer.broadcast(topic, data);
            return;
        }

        // 根据租户编码查找匹配的客户端并定向下发
        boolean sent = false;
        for (var entry : syncServer.getAllClients().entrySet()) {
            ClientInfo clientInfo = entry.getValue();
            if (clientInfo.getMetadata() != null) {
                Object clientTenantCode = clientInfo.getMetadata().get("tenantCode");
                if (tenantCode.equals(clientTenantCode)) {
                    syncServer.send(entry.getKey(), topic, data);
                    sent = true;
                    log.debug("[Tenant] 定向发送给客户端: clientId={}, tenantCode={}",
                            entry.getKey(), tenantCode);
                }
            }
        }

        // 如果没有找到匹配的客户端，广播给所有客户端
        if (!sent) {
            log.debug("[Tenant] 未找到匹配租户的客户端，广播给所有客户端: tenantCode={}", tenantCode);
            syncServer.broadcast(topic, data);
        }
    }

    /**
     * 从数据中提取租户编码
     *
     * @param data 数据
     * @return 租户编码，未找到返回 null
     */
    private String extractTenantCode(Object data) {
        if (data instanceof SyncTenantData syncTenantData) {
            SysTenant tenant = syncTenantData.getTenant();
            return tenant != null ? tenant.getSysTenantCode() : null;
        }
        if (data instanceof SyncTenantServiceData syncServiceData) {
            Integer tenantId = syncServiceData.getSysTenantId();
            if (tenantId != null) {
                SysTenant tenant = getById(tenantId);
                return tenant != null ? tenant.getSysTenantCode() : null;
            }
        }
        return null;
    }
}
