package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysTenant;
import com.chua.starter.system.mapper.SysTenantMapper;
import com.chua.starter.system.pojo.tenant.SysTenantServiceBindV1Request;
import com.chua.starter.system.service.SysTenantService;
import com.chua.starter.system.service.SysTenantServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements SysTenantService {

    final TransactionTemplate transactionTemplate;
    final SysTenantServiceService sysTenantServiceService;

    final

    @Override
    public IPage<SysTenant> pageFor(Query<SysTenant> query, SysTenant sysTenant) {
        Page<SysTenant> sysServicePage = baseMapper.selectPage(query.createPage(),
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
            save(sysTenant);
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
            return ReturnResult.preconditioning(updateById(sysTenant)).toResult();
        });
    }

    @Override
    public ReturnResult<Boolean> deleteFor(Long sysServiceId) {
        return ReturnResult.preconditioning(removeById(sysServiceId)).toResult();
    }

    @Override
    public ReturnResult<Boolean> bind(SysTenantServiceBindV1Request request) {
        return transactionTemplate.execute(status -> {
            if (CollectionUtils.isNotEmpty(request.getSysServiceIds())) {
                sysTenantServiceService.remove(Wrappers.<com.chua.starter.system.entity.SysTenantService>lambdaQuery()
                        .eq(com.chua.starter.system.entity.SysTenantService::getSysTenantId, request.getSysTenantId()));
                sysTenantServiceService.saveBatch(request.getSysServiceIds().stream().map(sysServiceModuleId -> {
                    com.chua.starter.system.entity.SysTenantService sysTenantService = new com.chua.starter.system.entity.SysTenantService();
                    sysTenantService.setSysTenantId(request.getSysTenantId());
                    sysTenantService.setSysServiceId(sysServiceModuleId.getSysServiceId());
                    sysTenantService.setSysTenantServiceValidTime(sysServiceModuleId.getSysTenantServiceValidTime());
                    return sysTenantService;
                }).toList());
            }
            return ReturnResult.ok();
        });
    }
}
