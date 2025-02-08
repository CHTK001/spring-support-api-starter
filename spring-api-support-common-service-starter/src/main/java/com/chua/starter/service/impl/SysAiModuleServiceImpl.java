package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.starter.common.support.annotations.ApiCacheKey;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.common.support.constant.CacheConstant;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.chua.starter.service.SysAiModuleService;
import com.chua.starter.service.configuration.CustomTenantedKeyGenerator;
import com.chua.starter.service.mapper.SysAiModuleMapper;
import com.chua.starter.system.entity.SysAiModule;
import com.chua.starter.system.entity.SysDictItem;
import com.github.yulichang.toolkit.MPJWrappers;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.chua.starter.common.support.constant.CacheConstant.REDIS_CACHE_ALWAYS;

@Service
public class SysAiModuleServiceImpl extends ServiceImpl<SysAiModuleMapper, SysAiModule> implements SysAiModuleService {

    @Override
    public List<SysAiModule> list(Integer sysProjectId) {
        if (null == sysProjectId) {
            return Collections.emptyList();
        }
        return baseMapper.selectList(
                Wrappers.<SysAiModule>lambdaQuery()
                        .eq(SysAiModule::getSysProjectId, sysProjectId)
                        .eq(SysAiModule::getSysAiModuleStatus, 1)
                        .orderByAsc(SysAiModule::getSysAiModuleSort)
                        .orderByDesc(SysAiModule::getCreateTime)
        );
    }

    @Override
    public IPage<SysAiModule> pageFor(Query<?> query, Integer sysProjectId) {
        if (null == sysProjectId) {
            return ReturnPageResultUtils.empty(SysAiModule.class);
        }
        return baseMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()),
                MPJWrappers.<SysAiModule>lambdaJoin()
                        .selectAll(SysAiModule.class)
                        .selectAs(SysDictItem::getSysDictItemName, SysAiModule::getSysApiModuleManufacturersLabel)
                        .leftJoin(SysDictItem.class, SysDictItem::getSysDictItemId, SysAiModule::getSysApiModuleManufacturers)
                        .eq(SysAiModule::getSysProjectId, sysProjectId)
                        .eq(SysAiModule::getSysAiModuleStatus, 1)
                        .orderByAsc(SysAiModule::getSysAiModuleSort)
                        .orderByDesc(SysAiModule::getCreateTime)
        );
    }

    @Override
    public ReturnResult<Boolean> updateForModule(SysAiModule sysAiModule) {
        SysAiModule sysAiModule1 = getById(sysAiModule.getSysAiModuleId());
        if (null == sysAiModule1) {
            return ReturnResult.error("模型不存在");
        }
        clearDefaultCache(sysAiModule1.getSysAiModuleCode(), sysAiModule1.getSysProjectId());
        return ReturnResult.of(updateById(sysAiModule));
    }

    @Override
    public ReturnResult<SysAiModule> saveForModule(SysAiModule sysAiModule) {
        sysAiModule.setSysAiModuleStatus(1);
        sysAiModule.setSysAiModuleSort(ObjectUtils.defaultIfNull(sysAiModule.getSysAiModuleSort(), 0));
        sysAiModule.setSysAiModuleRoleSetting(ObjectUtils.defaultIfNull(sysAiModule.getSysAiModuleRoleSetting(), 0));
        baseMapper.insert(sysAiModule);
        return ReturnResult.of(sysAiModule);
    }

    @Override
    public ReturnResult<Boolean> deleteForModule(Integer sysAiModuleId) {
        SysAiModule sysAiModule1 = getById(sysAiModuleId);
        if (null == sysAiModule1) {
            return ReturnResult.error("模型不存在");
        }

        SysAiModule sysAiModule = getById(sysAiModuleId);
        if (null == sysAiModule) {
            return ReturnResult.error("模型不存在");
        }

        clearDefaultCache(sysAiModule.getSysAiModuleCode(), sysAiModule.getSysProjectId());
        return ReturnResult.of(removeById(sysAiModuleId));
    }

    @Override
    @ApiCacheKey("'sys:ai:module:' + #model + ':'+#sysProjectId")
    @Cacheable(cacheManager = REDIS_CACHE_ALWAYS, cacheNames = REDIS_CACHE_ALWAYS, keyGenerator = "customTenantedKeyGenerator")
    public SysAiModule getOneByCode(Integer sysProjectId, String model) {
        return baseMapper.selectOne(
                Wrappers.<SysAiModule>lambdaQuery()
                        .eq(SysAiModule::getSysProjectId, sysProjectId)
                        .eq(SysAiModule::getSysAiModuleCode, model)
                        .eq(SysAiModule::getSysAiModuleStatus, 1)
                , true);
    }

    private void clearDefaultCache(String model, Integer sysProjectId) {
        StringBuilder keyPref = SpringBeanUtils.getBean(CustomTenantedKeyGenerator.class).getKeyPref();
        SpringBeanUtils.getBean(CacheConstant.REDIS_CACHE_ALWAYS, CacheManager.class)
                .getCache(CacheConstant.REDIS_CACHE_ALWAYS)
                .evict(keyPref.toString() + "sys:ai:module:" + model + ":" + sysProjectId);
    }
}
