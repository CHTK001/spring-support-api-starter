package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSkywalkingConfig;
import com.chua.starter.monitor.mapper.MonitorSkywalkingConfigMapper;
import com.chua.starter.monitor.service.skywalking.MonitorSkywalkingConfigService;
import com.chua.starter.monitor.service.skywalking.SkywalkingGraphQLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SkyWalking 配置服务实现
 *
 * @author CH
 * @since 2024-12-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSkywalkingConfigServiceImpl extends ServiceImpl<MonitorSkywalkingConfigMapper, MonitorSkywalkingConfig>
        implements MonitorSkywalkingConfigService {

    private final SkywalkingGraphQLService skywalkingGraphQLService;

    @Override
    public ReturnResult<Boolean> testConnection(Integer configId) {
        MonitorSkywalkingConfig config = getById(configId);
        if (config == null) {
            return ReturnResult.illegal("配置不存在");
        }
        return testConnection(config);
    }

    @Override
    public ReturnResult<Boolean> testConnection(MonitorSkywalkingConfig config) {
        try {
            boolean result = skywalkingGraphQLService.testConnection(config);
            if (result) {
                return ReturnResult.ok(true);
            } else {
                return ReturnResult.illegal("连接失败");
            }
        } catch (Exception e) {
            log.error("测试 SkyWalking 连接失败: {}", e.getMessage(), e);
            return ReturnResult.illegal("连接失败: " + e.getMessage());
        }
    }

    @Override
    public List<MonitorSkywalkingConfig> getEnabledConfigs() {
        return list(new LambdaQueryWrapper<MonitorSkywalkingConfig>()
                .eq(MonitorSkywalkingConfig::getSkywalkingConfigStatus, 1));
    }
}
