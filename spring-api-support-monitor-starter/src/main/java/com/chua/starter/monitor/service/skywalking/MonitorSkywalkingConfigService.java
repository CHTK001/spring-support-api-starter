package com.chua.starter.monitor.service.skywalking;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSkywalkingConfig;

import java.util.List;

/**
 * SkyWalking 配置服务接口
 *
 * @author CH
 * @since 2024-12-18
 */
public interface MonitorSkywalkingConfigService extends IService<MonitorSkywalkingConfig> {

    /**
     * 测试 SkyWalking 连接
     *
     * @param configId 配置ID
     * @return 测试结果
     */
    ReturnResult<Boolean> testConnection(Integer configId);

    /**
     * 测试 SkyWalking 连接
     *
     * @param config 配置信息
     * @return 测试结果
     */
    ReturnResult<Boolean> testConnection(MonitorSkywalkingConfig config);

    /**
     * 获取所有启用的配置
     *
     * @return 配置列表
     */
    List<MonitorSkywalkingConfig> getEnabledConfigs();
}
