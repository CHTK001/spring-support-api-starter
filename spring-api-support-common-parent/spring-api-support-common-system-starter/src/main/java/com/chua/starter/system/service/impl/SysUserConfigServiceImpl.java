package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.entity.system.SysUserConfig;
import com.chua.starter.system.mapper.SysUserConfigMapper;
import com.chua.starter.system.service.SysUserConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户个性化配置服务实现
 *
 * @since 4.0.0.42
 * @author CH
 */
@Service
public class SysUserConfigServiceImpl extends ServiceImpl<SysUserConfigMapper, SysUserConfig> implements SysUserConfigService {

    @Override
    /**
     * 获取by配置id
    */
    public SysUserConfig getByConfigId(Integer configId) {
        if (configId == null) {
            return null;
        }
        return getById(configId);
    }

    @Override
    /**
     * 获取bywxunionid
    */
    public SysUserConfig getByWxUnionid(String unionid) {
        if (unionid == null || unionid.isEmpty()) {
            return null;
        }
        return getOne(Wrappers.<SysUserConfig>lambdaQuery()
                .eq(SysUserConfig::getSysUserConfigWxUnionid, unionid)
                .last("LIMIT 1"));
    }

    @Override
    /**
     * 获取bywxopenid
    */
    public SysUserConfig getByWxOpenid(String openid) {
        if (openid == null || openid.isEmpty()) {
            return null;
        }
        return getOne(Wrappers.<SysUserConfig>lambdaQuery()
                .eq(SysUserConfig::getSysUserConfigWxOpenid, openid)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 保存或更新配置
     *
     * @param config 配置
     * @return 保存或更新配置的结果
     */
    public SysUserConfig saveOrUpdateConfig(SysUserConfig config) {
        if (config == null) {
            return null;
        }
        if (config.getSysUserConfigId() == null) {
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            save(config);
            return config;
        }
        config.setUpdateTime(LocalDateTime.now());
        boolean ok = updateById(config);
        return ok ? config : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 解绑Wx
     *
     * @param configId 配置id
     * @return unbindWx的结果
     */
    public boolean unbindWx(Integer configId) {
        if (configId == null) {
            return false;
        }
        SysUserConfig update = new SysUserConfig();
        update.setSysUserConfigId(configId);
        update.setSysUserConfigWxOpenid(null);
        update.setSysUserConfigWxUnionid(null);
        update.setUpdateTime(LocalDateTime.now());
        return updateById(update);
    }
}
