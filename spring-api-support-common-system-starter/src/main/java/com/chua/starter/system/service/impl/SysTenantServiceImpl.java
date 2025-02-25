package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.constant.Action;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysMenu;
import com.chua.starter.system.entity.SysTenant;
import com.chua.starter.system.mapper.SysMenuMapper;
import com.chua.starter.system.mapper.SysServiceItemMapper;
import com.chua.starter.system.mapper.SysTenantMapper;
import com.chua.starter.system.pojo.tenant.SyncTenantData;
import com.chua.starter.system.pojo.tenant.SyncTenantServiceData;
import com.chua.starter.system.pojo.tenant.SysTenantServiceBindV1Request;
import com.chua.starter.system.pojo.tenant.SysTenantSyncV1Request;
import com.chua.starter.system.service.SysServiceModuleService;
import com.chua.starter.system.service.SysServiceService;
import com.chua.starter.system.service.SysTenantService;
import com.chua.starter.system.service.SysTenantServiceService;
import com.chua.starter.tenant.service.TenantMessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements SysTenantService {

    final TransactionTemplate transactionTemplate;
    final SysTenantServiceService sysTenantServiceService;
    final SysServiceService sysServiceService;
    final SysServiceModuleService sysServiceModuleService;
    final SysServiceItemMapper sysServiceItemMapper;
    final SysMenuMapper sysMenuMapper;
    final TenantMessagePublisher tenantMessagePublisher;

    @Override
    public IPage<SysTenant> pageFor(Query<SysTenant> query, SysTenant sysTenant) {
        Page<SysTenant> sysServicePage = page(query.createPage(),
                Wrappers.<SysTenant>lambdaQuery()
                        .likeRight(StringUtils.isNotBlank(sysTenant.getSysTenantName()), SysTenant::getSysTenantName, sysTenant.getSysTenantName())
                        .likeRight(StringUtils.isNotBlank(sysTenant.getSysTenantCode()), SysTenant::getSysTenantCode, sysTenant.getSysTenantCode())
                        .orderByDesc(SysTenant::getCreateTime)
        );

        List<SysTenant> records = sysServicePage.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            List<com.chua.starter.system.entity.SysTenantService> list = sysTenantServiceService
                    .list(Wrappers.<com.chua.starter.system.entity.SysTenantService>lambdaQuery()
                            .in(com.chua.starter.system.entity.SysTenantService::getSysTenantId, records.stream().map(SysTenant::getSysTenantId).toList()));
            Map<Integer, List<com.chua.starter.system.entity.SysTenantService>> collect = list.stream().collect(Collectors.groupingBy(com.chua.starter.system.entity.SysTenantService::getSysTenantId));
            for (SysTenant record : records) {
                record.setSysTenantService(collect.getOrDefault(record.getSysTenantId(), List.of()));
                record.setServiceIds(collect.getOrDefault(record.getSysTenantId(), List.of()).stream().map(com.chua.starter.system.entity.SysTenantService::getSysServiceId).toList());
            }
        }
        return sysServicePage;
    }

    @Override
    public ReturnResult<SysTenant> saveFor(SysTenant sysTenant) {
        if (exists(Wrappers.<SysTenant>lambdaQuery()
                .eq(SysTenant::getSysTenantCode, sysTenant.getSysTenantCode()))) {
            return ReturnResult.error("服务编码已存在");
        }
        sysTenant.setSysTenantDelete(0);
        sysTenant.setSysTenantStatus(0);
        return transactionTemplate.execute(it -> {
            bind(new SysTenantServiceBindV1Request(sysTenant.getSysTenantId(), sysTenant.getSysTenantService()));
            boolean save = save(sysTenant);
            if (save) {
                tenantMessagePublisher.publish("tenant", new SyncTenantData(Action.CREATE, sysTenant));
            }
            return ReturnResult.preconditioning(sysTenant).toResult();
        });
    }

    @Override
    public ReturnResult<Boolean> updateFor(SysTenant sysTenant) {
        if (exists(Wrappers.<SysTenant>lambdaQuery().eq(SysTenant::getSysTenantCode, sysTenant.getSysTenantCode())
                .ne(SysTenant::getSysTenantId, sysTenant.getSysTenantId()))) {
            return ReturnResult.error("服务编码已存在");
        }
        sysTenant.setSysTenantDelete(null);
        return transactionTemplate.execute(it -> {
            bind(new SysTenantServiceBindV1Request(sysTenant.getSysTenantId(), sysTenant.getSysTenantService()));
            boolean b = updateById(sysTenant);
            if (b) {
                tenantMessagePublisher.publish("tenant", new SyncTenantData(Action.UPDATE, sysTenant));
            }
            return ReturnResult.preconditioning(b).toResult();
        });
    }

    @Override
    public ReturnResult<Boolean> deleteFor(Long sysServiceId) {
        SysTenant sysTenant = getById(sysServiceId);
        if (null == sysTenant) {
            return ReturnResult.illegal("数据不存在");
        }
        boolean b = removeById(sysServiceId);
        if (b) {
            tenantMessagePublisher.publish("tenant", new SyncTenantData(Action.DELETE, sysTenant));
        }
        return ReturnResult.preconditioning(b).toResult();
    }

    @Override
    public ReturnResult<Boolean> bind(SysTenantServiceBindV1Request request) {
        return transactionTemplate.execute(status -> {
            if (CollectionUtils.isNotEmpty(request.getSysServiceIds())) {
                SysTenant sysTenant = baseMapper.selectById(request.getSysTenantId());
                sysTenantServiceService.remove(Wrappers.<com.chua.starter.system.entity.SysTenantService>lambdaQuery()
                        .eq(com.chua.starter.system.entity.SysTenantService::getSysTenantId, request.getSysTenantId()));
                List<com.chua.starter.system.entity.SysTenantService> list = request.getSysServiceIds().stream().map(sysServiceModuleId -> {
                    com.chua.starter.system.entity.SysTenantService sysTenantService = new com.chua.starter.system.entity.SysTenantService();
                    sysTenantService.setSysTenantId(request.getSysTenantId());
                    sysTenantService.setSysServiceId(sysServiceModuleId.getSysServiceId());
                    sysTenantService.setSysTenantServiceValidTime(sysServiceModuleId.getSysTenantServiceValidTime());
                    return sysTenantService;
                }).toList();
                sysTenantServiceService.saveBatch(list);
                List<SysMenu> menus = sysTenantServiceService.getMenuIds(request.getSysTenantId());
                tenantMessagePublisher.publish("tenant", new SyncTenantData(Action.UPDATE, sysTenant));
                tenantMessagePublisher.publish("service", new SyncTenantServiceData(
                        Action.UPDATE,
                        request.getSysTenantId(),
                        menus
                ));
            }
            return ReturnResult.ok();
        });
    }

    @Override
    public ReturnResult<Boolean> sync(SysTenantSyncV1Request request) {
        return transactionTemplate.execute(status -> {
            Integer sysTenantId = request.getSysTenantId();
            SysTenant sysTenant = baseMapper.selectById(sysTenantId);
            List<SysMenu> list = sysTenant.getSysTenantStatus() == 0 ? sysTenantServiceService.getMenuIds(sysTenantId) : Collections.emptyList();

            tenantMessagePublisher.publish("tenant", new SyncTenantData(Action.UPDATE, sysTenant));
            tenantMessagePublisher.publish("service", new SyncTenantServiceData(
                    Action.UPDATE,
                    request.getSysTenantId(),
                    list
            ));
            return ReturnResult.ok();
        });
    }
}
