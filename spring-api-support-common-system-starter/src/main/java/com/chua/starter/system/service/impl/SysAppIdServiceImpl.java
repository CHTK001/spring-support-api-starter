package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.system.mapper.SysAppIdMapper;
import com.chua.starter.system.service.SysAppIdService;
import com.chua.starter.entity.system.SysAppId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * AppId应用标识 Service 实现
 *
 * @author CH
 * @since 2025/01/05
 */
@Slf4j
@Service
public class SysAppIdServiceImpl extends ServiceImpl<SysAppIdMapper, SysAppId> implements SysAppIdService {

    @Override
    @Cacheable(value = "appId:info", key = "#appIdValue", unless = "#result == null")
    public SysAppId getByAppIdValue(String appIdValue) {
        if (StringUtils.isBlank(appIdValue)) {
            return null;
        }
        return baseMapper.selectByAppIdValue(appIdValue);
    }

    @Override
    @Async("taskExecutor")
    public void updateUseInfo(String appIdValue) {
        try {
            long currentTime = System.currentTimeMillis();
            baseMapper.updateUseInfo(appIdValue, currentTime);
            log.debug("AppId使用信息已异步更新: appIdValue={}", appIdValue);
        } catch (Exception e) {
            log.error("更新AppId使用信息失败: appIdValue={}", appIdValue, e);
        }
    }
}

