package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.SysServiceItemService;
import com.chua.starter.service.SysServiceService;
import com.chua.starter.service.mapper.SysServiceMapper;
import com.chua.starter.system.entity.SysService;
import com.chua.starter.system.entity.SysServiceModuleItem;
import com.chua.starter.system.pojo.service.SysServiceBindV1Request;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务
 *
 * @author CH
 */
@Service
@RequiredArgsConstructor
public class SysServiceServiceImpl extends ServiceImpl<SysServiceMapper, SysService> implements SysServiceService {

    final TransactionTemplate transactionTemplate;
    final SysServiceItemService sysServiceItemService;

    @Override
    public IPage<SysService> pageFor(Query<SysService> query, SysService sysService) {
        Page<SysService> sysServicePage = baseMapper.selectPage(query.createPage(),
                Wrappers.<SysService>lambdaQuery()
                        .likeRight(StringUtils.isNotBlank(sysService.getSysServiceName()), SysService::getSysServiceName, sysService.getSysServiceName())
                        .likeRight(StringUtils.isNotBlank(sysService.getSysServiceCode()), SysService::getSysServiceCode, sysService.getSysServiceCode())
                        .orderByAsc(SysService::getSysServiceSort)
        );

        List<SysService> records = sysServicePage.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            List<SysServiceModuleItem> list = sysServiceItemService.list(Wrappers.<SysServiceModuleItem>lambdaQuery()
                    .in(SysServiceModuleItem::getSysServiceId, records.stream().map(SysService::getSysServiceId).toList()));
            Map<Integer, List<SysServiceModuleItem>> collect = list.stream().collect(Collectors.groupingBy(SysServiceModuleItem::getSysServiceId));
            for (SysService record : records) {
                record.setSysServiceTags(collect.getOrDefault(record.getSysServiceId(), List.of()).stream().map(SysServiceModuleItem::getSysServiceModuleId).toList());
            }
        }
        return sysServicePage;
    }

    @Override
    public ReturnResult<SysService> saveFor(SysService sysService) {
        if (exists(Wrappers.<SysService>lambdaQuery()
                .eq(SysService::getSysServiceCode, sysService.getSysServiceCode()))) {
            return ReturnResult.error("服务编码已存在");
        }
        sysService.setSysServiceDelete(0);
        sysService.setSysServiceStatus(0);
        save(sysService);
        return ReturnResult.success(sysService);
    }

    @Override
    public ReturnResult<Boolean> updateFor(SysService sysService) {
        if (exists(Wrappers.<SysService>lambdaQuery().eq(SysService::getSysServiceCode, sysService.getSysServiceCode())
                .ne(SysService::getSysServiceId, sysService.getSysServiceId()))) {
            return ReturnResult.error("服务编码已存在");
        }
        sysService.setSysServiceDelete(null);
        return ReturnResult.preconditioning(updateById(sysService)).toResult();
    }

    @Override
    public ReturnResult<Boolean> deleteFor(Long sysServiceId) {
        return ReturnResult.preconditioning(removeById(sysServiceId)).toResult();
    }

    @Override
    public ReturnResult<Boolean> bind(SysServiceBindV1Request request) {
        return transactionTemplate.execute(status -> {
            if (CollectionUtils.isNotEmpty(request.getSysServiceModuleIds())) {
                sysServiceItemService.remove(Wrappers.<SysServiceModuleItem>lambdaQuery()
                        .eq(SysServiceModuleItem::getSysServiceId, request.getSysServiceId()));
                sysServiceItemService.saveBatch(request.getSysServiceModuleIds().stream().map(sysServiceModuleId -> {
                    SysServiceModuleItem sysServiceModuleItem = new SysServiceModuleItem();
                    sysServiceModuleItem.setSysServiceId(request.getSysServiceId());
                    sysServiceModuleItem.setSysServiceModuleId(sysServiceModuleId);
                    return sysServiceModuleItem;
                }).toList());
            }
            return ReturnResult.ok();
        });
    }
}
