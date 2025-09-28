package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.service.SysLimitConfigurationService;
import com.chua.starter.service.mapper.SysLimitConfigurationMapper;
import com.chua.starter.system.entity.SysLimitConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 限流配置服务实现类
 * 
 * @author CH
 * @since 2025/9/28
 */
@Slf4j
@Service
public class SysLimitConfigurationServiceImpl extends ServiceImpl<SysLimitConfigurationMapper, SysLimitConfiguration> implements SysLimitConfigurationService {

    private final ApplicationContext applicationContext;

    public SysLimitConfigurationServiceImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public List<SysLimitConfiguration> list(SysLimitConfiguration sysLimitConfiguration, Set<String> roles) {
        return baseMapper.selectList(Wrappers.<SysLimitConfiguration>lambdaQuery()
                .like(StringUtils.isNotBlank(sysLimitConfiguration.getSysLimitPath()), SysLimitConfiguration::getSysLimitPath, sysLimitConfiguration.getSysLimitPath())
                .like(StringUtils.isNotBlank(sysLimitConfiguration.getSysLimitName()), SysLimitConfiguration::getSysLimitName, sysLimitConfiguration.getSysLimitName())
                .eq(sysLimitConfiguration.getSysLimitStatus() != null, SysLimitConfiguration::getSysLimitStatus, sysLimitConfiguration.getSysLimitStatus())
                .orderByAsc(SysLimitConfiguration::getSysLimitSort)
                .orderByDesc(SysLimitConfiguration::getCreateTime)
        );
    }

    @Override
    public ReturnResult<Boolean> delete(Long limitConfigurationId) {
        if(null == limitConfigurationId) {
            return ReturnResult.fail("限流配置ID不能为空");
        }

        SysLimitConfiguration sysLimitConfiguration = baseMapper.selectById(limitConfigurationId);
        if(null == sysLimitConfiguration) {
            return ReturnResult.fail("限流配置不存在, 请刷新后重试!");
        }

        int b = baseMapper.deleteById(limitConfigurationId);
        return ReturnResult.of(b > 0);
    }

    @Override
    public ReturnResult<Boolean> update(Set<String> roles, SysLimitConfiguration sysLimitConfiguration) {
        SysLimitConfiguration existingConfig = baseMapper.selectById(sysLimitConfiguration.getSysLimitConfigurationId());
        if(!AuthConstant.isSuperAdmin(roles)) {
            // 非超级管理员不能修改某些系统字段
            sysLimitConfiguration.setSysLimitConfigurationId(null);
        }

        String sysLimitPath = sysLimitConfiguration.getSysLimitPath();
        if(StringUtils.isNotBlank(sysLimitPath)) {
            SysLimitConfiguration duplicateConfig = baseMapper.selectOne(Wrappers.<SysLimitConfiguration>lambdaQuery()
                    .eq(SysLimitConfiguration::getSysLimitPath, sysLimitPath)
                    .ne(SysLimitConfiguration::getSysLimitConfigurationId, sysLimitConfiguration.getSysLimitConfigurationId()));
            if(null != duplicateConfig) {
                return ReturnResult.fail("该接口路径的限流配置已存在");
            }
        }
        
        if(baseMapper.updateById(sysLimitConfiguration) > 0) {
            return ReturnResult.success(true);
        }
        return ReturnResult.fail("更新失败");
    }

    @Override
    public ReturnResult<SysLimitConfiguration> saveForEntity(SysLimitConfiguration sysLimitConfiguration, Set<String> roles) {
        if(null == sysLimitConfiguration.getSysLimitStatus()) {
            sysLimitConfiguration.setSysLimitStatus(1);
        }
        
        SysLimitConfiguration existingConfig = baseMapper.selectOne(Wrappers.<SysLimitConfiguration>lambdaQuery()
                .eq(SysLimitConfiguration::getSysLimitPath, sysLimitConfiguration.getSysLimitPath()));
        if(null != existingConfig) {
            return ReturnResult.fail("该接口路径的限流配置已存在");
        }

        if(baseMapper.insert(sysLimitConfiguration) > 0) {
            return ReturnResult.success(sysLimitConfiguration);
        }
        return ReturnResult.fail("新增失败");
    }

    @Override
    public IPage<SysLimitConfiguration> pageForLimitConfiguration(Query<SysLimitConfiguration> query, SysLimitConfiguration sysLimitConfiguration, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysLimitConfiguration>lambdaQuery()
                .like(StringUtils.isNotBlank(sysLimitConfiguration.getSysLimitPath()), SysLimitConfiguration::getSysLimitPath, sysLimitConfiguration.getSysLimitPath())
                .like(StringUtils.isNotBlank(sysLimitConfiguration.getSysLimitName()), SysLimitConfiguration::getSysLimitName, sysLimitConfiguration.getSysLimitName())
                .eq(sysLimitConfiguration.getSysLimitStatus() != null, SysLimitConfiguration::getSysLimitStatus, sysLimitConfiguration.getSysLimitStatus())
                .orderByDesc(SysLimitConfiguration::getCreateTime)
        );
    }

    @Override
    public ReturnResult<Boolean> updateBatch(Set<String> roles, List<SysLimitConfiguration> sysLimitConfigurations) {
        for (SysLimitConfiguration config : sysLimitConfigurations) {
            if(baseMapper.updateById(config) <= 0) {
                return ReturnResult.fail("批量更新失败");
            }
        }
        return ReturnResult.success(true);
    }

    @Override
    public List<SysLimitConfiguration> getLimitConfigurationsByPath(String path) {
        return baseMapper.selectList(Wrappers.<SysLimitConfiguration>lambdaQuery()
                .eq(SysLimitConfiguration::getSysLimitPath, path)
                .eq(SysLimitConfiguration::getSysLimitStatus, 1)
        );
    }
}