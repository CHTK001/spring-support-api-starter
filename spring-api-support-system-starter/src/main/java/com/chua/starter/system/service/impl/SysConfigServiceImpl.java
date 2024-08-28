package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.system.entity.SysConfig;
import com.chua.starter.system.mapper.SysConfigMapper;
import com.chua.starter.system.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.chua.starter.system.constant.SystemConstant.CACHE_WITH_REDIS;

/**
 *
 * @since 2024/8/28
 * @author CH    
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService{

    @Autowired
    private SysConfigService sysConfigService;

    @Override
    public List<SysConfig> list(SysConfig sysConfig, Set<String> roles) {
        return baseMapper.selectList(Wrappers.<SysConfig>lambdaQuery()
                .in(StringUtils.isNotBlank(sysConfig.getSysConfigGroup()), SysConfig::getSysConfigGroup, Splitter.on(",").omitEmptyStrings().trimResults().splitToSet(sysConfig.getSysConfigGroup()))
        );
    }

    @Override
    public ReturnResult<Boolean> delete(Long configId, Set<String> roles) {
        if(null == configId) {
            return ReturnResult.fail("配置ID不能为空");
        }

        SysConfig sysConfig = baseMapper.selectById(configId);
        if(null == sysConfig) {
            return ReturnResult.fail("配置不存在, 请刷新后重试!");
        }

        Integer sysConfigInSystem = sysConfig.getSysConfigInSystem();
        if(null != sysConfigInSystem && sysConfigInSystem == 1 && !AuthConstant.hasAdmin(roles)) {
            return ReturnResult.fail("内置配置不允许删除");
        }

        int b = baseMapper.deleteById(configId);
        sysConfigService.clearCache(sysConfig);
        return ReturnResult.of(b);
    }

    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysConfig sysConfig) {
        SysConfig sysConfig1 = baseMapper.selectById(sysConfig.getSysConfigId());
        Integer sysConfigInSystem = sysConfig1.getSysConfigInSystem();
        if(null != sysConfigInSystem && sysConfigInSystem == 1 && !AuthConstant.isSuperAdmin(roles)) {
            return ReturnResult.fail("内置配置不允许修改");
        }

        String sysConfigName = sysConfig.getSysConfigName();
        sysConfig.setSysConfigInSystem(null);
        if(StringUtils.isNotBlank(sysConfigName)) {
            SysConfig sysConfig2 = baseMapper.selectOne(Wrappers.<SysConfig>lambdaQuery()
                    .eq(SysConfig::getSysConfigName, sysConfigName)
                    .eq(SysConfig::getSysConfigGroup, sysConfig.getSysConfigGroup())
                    .ne(SysConfig::getSysConfigId, sysConfig.getSysConfigId()))
                    ;
            if(null != sysConfig2) {
                return ReturnResult.fail("配置编码已存在");
            }
        }
        boolean b = baseMapper.updateById(sysConfig) > 0;
        sysConfigService.clearCache(sysConfig);
        return ReturnResult.of(b);
    }

    @Override
    public ReturnResult<SysConfig> saveForEntity(SysConfig sysConfig, Set<String> roles) {
        SysConfig sysConfig1 = baseMapper.selectById(sysConfig.getSysConfigId());
        Integer sysRoleInSystem = sysConfig1.getSysConfigInSystem();
        if(!AuthConstant.isSuperAdmin(roles)) {
            if(null != sysRoleInSystem && sysRoleInSystem == 1) {
                return ReturnResult.fail("内置配置不允许修改");
            }
        }

        String sysSettingName = sysConfig.getSysConfigName();
        sysConfig.setSysConfigInSystem(null);
        if(StringUtils.isNotBlank(sysSettingName)) {
            SysConfig sysConfig2 = baseMapper.selectOne(Wrappers.<SysConfig>lambdaQuery()
                    .eq(SysConfig::getSysConfigName, sysSettingName)
                    .eq(SysConfig::getSysConfigGroup, sysConfig.getSysConfigGroup())
                    .ne(SysConfig::getSysConfigId, sysConfig.getSysConfigId()))
                    ;
            if(null != sysConfig2) {
                return ReturnResult.fail("配置编码已存在");
            }
        }
        boolean b = baseMapper.updateById(sysConfig) > 0;
        sysConfigService.clearCache(sysConfig);
        return ReturnResult.of(sysConfig);
    }

    @Override
    @CacheEvict(cacheManager = CACHE_WITH_REDIS, cacheNames = CACHE_WITH_REDIS, key = "'cache:sys:config:id:' + #sysConfig.sysConfigGroup")
    public SysConfig clearCache(SysConfig sysConfig) {
        return sysConfig;
    }

    @Override
    public IPage<SysConfig> pageForSetting(Query<SysConfig> query, SysConfig sysSetting, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysConfig>lambdaQuery()
                .like(StringUtils.isNotBlank(sysSetting.getSysConfigGroup()), SysConfig::getSysConfigGroup, sysSetting.getSysConfigGroup())
                .like(StringUtils.isNotBlank(sysSetting.getSysConfigName()), SysConfig::getSysConfigName, sysSetting.getSysConfigName())
                .like(StringUtils.isNotBlank(sysSetting.getSysConfigRemark()), SysConfig::getSysConfigRemark, sysSetting.getSysConfigRemark())
                .orderByDesc(SysConfig::getCreateTime)
        );
    }
}
