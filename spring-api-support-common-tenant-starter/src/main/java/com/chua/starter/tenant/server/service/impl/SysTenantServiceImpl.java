package com.chua.starter.tenant.server.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.IdUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.entity.SysTenantService;
import com.chua.starter.tenant.entity.SysTenantStats;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import com.chua.starter.tenant.server.mapper.SysTenantServiceMapper;
import com.chua.starter.tenant.server.pojo.SysTenantBindServiceV1Request;
import com.chua.starter.tenant.server.sync.TenantServerSyncPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租户服务实现类
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant>
        implements com.chua.starter.tenant.server.service.SysTenantService {

    private final TransactionTemplate transactionTemplate;
    private final SysTenantServiceMapper sysTenantServiceMapper;

    @Autowired(required = false)
    private TenantServerSyncPublisher syncPublisher;

    @Override
    public IPage<SysTenant> pageForSysTenant(Query<SysTenant> query, SysTenant sysTenant) {
        log.debug("分页查询租户, 条件: {}", sysTenant);
        IPage<SysTenant> page = baseMapper.selectPage(query.createPage(),
                Wrappers.<SysTenant>lambdaQuery()
                        .likeRight(StringUtils.isNotBlank(sysTenant.getSysTenantName()),
                                SysTenant::getSysTenantName, sysTenant.getSysTenantName())
                        .likeRight(StringUtils.isNotBlank(sysTenant.getSysTenantCode()),
                                SysTenant::getSysTenantCode, sysTenant.getSysTenantCode())
                        .likeRight(StringUtils.isNotBlank(sysTenant.getSysTenantPhone()),
                                SysTenant::getSysTenantPhone, sysTenant.getSysTenantPhone())
                        .eq(sysTenant.getSysTenantStatus() != null,
                                SysTenant::getSysTenantStatus, sysTenant.getSysTenantStatus())
                        .orderByDesc(SysTenant::getCreateTime)
        );

        List<SysTenant> records = page.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            fillTenantServices(records);
        }
        return page;
    }

    @Override
    public ReturnResult<List<SysTenant>> listForSysTenant(SysTenant sysTenant) {
        log.debug("查询租户列表, 条件: {}", sysTenant);
        List<SysTenant> records = baseMapper.selectList(
                Wrappers.<SysTenant>lambdaQuery()
                        .likeRight(StringUtils.isNotBlank(sysTenant.getSysTenantName()),
                                SysTenant::getSysTenantName, sysTenant.getSysTenantName())
                        .likeRight(StringUtils.isNotBlank(sysTenant.getSysTenantCode()),
                                SysTenant::getSysTenantCode, sysTenant.getSysTenantCode())
                        .eq(sysTenant.getSysTenantStatus() != null,
                                SysTenant::getSysTenantStatus, sysTenant.getSysTenantStatus())
                        .orderByDesc(SysTenant::getCreateTime)
        );

        if (CollectionUtils.isNotEmpty(records)) {
            fillTenantServices(records);
        }
        return ReturnResult.ok(records);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ReturnResult<SysTenant> saveForSysTenant(SysTenant sysTenant) {
        log.debug("保存租户: {}", sysTenant);

        // sysTenantId 使用 ASSIGN_ID 策略自动生成，同时兼容旧的 sysTenantCode 字段
        // 保存后将 sysTenantId 同步到废弃的 sysTenantCode 字段以保持兼容

        // 检查账号是否已存在
        if (StringUtils.isNotBlank(sysTenant.getSysTenantUsername())) {
            if (exists(Wrappers.<SysTenant>lambdaQuery()
                    .eq(SysTenant::getSysTenantUsername, sysTenant.getSysTenantUsername()))) {
                log.warn("租户账号已存在: {}", sysTenant.getSysTenantUsername());
                return ReturnResult.error("租户账号已存在");
            }
        }

        // 设置默认值
        sysTenant.setSysTenantDelete(0);
        if (sysTenant.getSysTenantStatus() == null) {
            sysTenant.setSysTenantStatus(0);
        }

        if (StringUtils.isBlank(sysTenant.getSysTenantGid())) {
            sysTenant.setSysTenantGid(IdUtils.createUlid());
        }

        save(sysTenant);
        
        // 将 sysTenantId 同步到废弃的 sysTenantCode 字段以保持兼容
        sysTenant.setSysTenantCode(sysTenant.getSysTenantId());
        updateById(sysTenant);
        
        log.info("租户保存成功, ID: {}", sysTenant.getSysTenantId());

        // 发布同步消息
        if (syncPublisher != null) {
            syncPublisher.publishTenantUpdate(sysTenant);
        }

        return ReturnResult.success(sysTenant);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ReturnResult<Boolean> updateForSysTenant(SysTenant sysTenant) {
        log.debug("更新租户: {}", sysTenant);

        // 租户ID不可修改，sysTenantCode 废弃字段也置空
        sysTenant.setSysTenantCode(null);

        if (StringUtils.isNotBlank(sysTenant.getSysTenantUsername())) {
            if (exists(Wrappers.<SysTenant>lambdaQuery()
                    .eq(SysTenant::getSysTenantUsername, sysTenant.getSysTenantUsername())
                    .ne(SysTenant::getSysTenantId, sysTenant.getSysTenantId()))) {
                log.warn("租户账号已存在: {}", sysTenant.getSysTenantUsername());
                return ReturnResult.error("租户账号已存在");
            }
        }

        sysTenant.setSysTenantDelete(null);
        boolean result = updateById(sysTenant);
        log.info("租户更新结果: {}", result);

        // 发布同步消息
        if (result && syncPublisher != null) {
            SysTenant updated = getById(sysTenant.getSysTenantId());
            syncPublisher.publishTenantUpdate(updated);
        }

        return ReturnResult.preconditioning(result).toResult();
    }

    @Override
    public ReturnResult<Boolean> deleteForSysTenant(Integer sysTenantId) {
        log.debug("删除租户, ID: {}", sysTenantId);

        return transactionTemplate.execute(status -> {
            sysTenantServiceMapper.delete(Wrappers.<SysTenantService>lambdaQuery()
                    .eq(SysTenantService::getSysTenantId, sysTenantId));

            boolean result = removeById(sysTenantId);
            log.info("租户删除结果: {}", result);

            // 发布同步消息
            if (result && syncPublisher != null) {
                syncPublisher.publishTenantDelete(sysTenantId);
            }

            return ReturnResult.preconditioning(result).toResult();
        });
    }

    @Override
    public ReturnResult<SysTenant> getForSysTenant(Integer sysTenantId) {
        log.debug("获取租户详情, ID: {}", sysTenantId);
        SysTenant sysTenant = getById(sysTenantId);
        if (sysTenant == null) {
            return ReturnResult.error("租户不存在");
        }

        fillTenantServices(List.of(sysTenant));
        return ReturnResult.ok(sysTenant);
    }

    @Override
    public ReturnResult<Boolean> bindServiceForSysTenant(SysTenantBindServiceV1Request request) {
        log.debug("绑定租户服务, 租户ID: {}, 服务数量: {}",
                request.getSysTenantId(),
                request.getServiceBindItems() != null ? request.getServiceBindItems().size() : 0);

        return transactionTemplate.execute(status -> {
            sysTenantServiceMapper.delete(Wrappers.<SysTenantService>lambdaQuery()
                    .eq(SysTenantService::getSysTenantId, request.getSysTenantId()));

            if (CollectionUtils.isNotEmpty(request.getServiceBindItems())) {
                List<SysTenantService> tenantServices = request.getServiceBindItems().stream()
                        .map(item -> {
                            SysTenantService tenantService = new SysTenantService();
                            tenantService.setSysTenantId(request.getSysTenantId());
                            tenantService.setSysServiceId(item.getSysServiceId());
                            tenantService.setSysTenantServiceValidTime(item.getValidTime());
                            return tenantService;
                        })
                        .toList();

                for (SysTenantService tenantService : tenantServices) {
                    sysTenantServiceMapper.insert(tenantService);
                }
                log.info("租户服务绑定成功, 绑定数量: {}", tenantServices.size());

                // 发布服务更新
                if (syncPublisher != null) {
                    List<Integer> menuIds = sysTenantServiceMapper.getMenuByTenantId(request.getSysTenantId());
                    syncPublisher.publishServiceUpdate(request.getSysTenantId(), menuIds);
                }
            }
            return ReturnResult.ok();
        });
    }

    @Override
    public ReturnResult<Boolean> updateStatusForSysTenant(Integer sysTenantId, Integer status) {
        log.debug("更新租户状态, ID: {}, 状态: {}", sysTenantId, status);

        boolean result = update(Wrappers.<SysTenant>lambdaUpdate()
                .set(SysTenant::getSysTenantStatus, status)
                .eq(SysTenant::getSysTenantId, sysTenantId));

        log.info("租户状态更新结果: {}", result);

        // 发布同步消息
        if (result && syncPublisher != null) {
            SysTenant tenant = getById(sysTenantId);
            syncPublisher.publishTenantUpdate(tenant);
        }

        return ReturnResult.preconditioning(result).toResult();
    }

    @Override
    public ReturnResult<SysTenantStats> statsForSysTenant() {
        log.debug("获取租户统计数据");
        SysTenantStats stats = new SysTenantStats();

        // 总数
        stats.setTotal(count());
        // 启用数 (status = 0 表示启用)
        stats.setEnabled(count(Wrappers.<SysTenant>lambdaQuery().eq(SysTenant::getSysTenantStatus, 0)));
        // 禁用数 (status = 1 表示禁用)
        stats.setDisabled(count(Wrappers.<SysTenant>lambdaQuery().eq(SysTenant::getSysTenantStatus, 1)));
        // 订阅服务数（有订阅服务的租户数）
        stats.setSubscribed(sysTenantServiceMapper.selectCount(
                Wrappers.query().select("DISTINCT sys_tenant_id")
        ));

        return ReturnResult.ok(stats);
    }

    /**
     * 填充租户的关联服务信息
     *
     * @param records 租户列表
     */
    private void fillTenantServices(List<SysTenant> records) {
        List<String> tenantIds = records.stream()
                .map(SysTenant::getSysTenantId)
                .toList();

        List<SysTenantService> tenantServices = sysTenantServiceMapper.selectList(
                Wrappers.<SysTenantService>lambdaQuery()
                        .in(SysTenantService::getSysTenantId, tenantIds));

        Map<String, List<SysTenantService>> serviceMap = tenantServices.stream()
                .collect(Collectors.groupingBy(SysTenantService::getSysTenantId));

        for (SysTenant record : records) {
            List<SysTenantService> services = serviceMap.getOrDefault(record.getSysTenantId(), List.of());
            record.setSysTenantService(services);
            record.setServiceIds(services.stream()
                    .map(SysTenantService::getSysServiceId)
                    .toList());
        }
    }
}
