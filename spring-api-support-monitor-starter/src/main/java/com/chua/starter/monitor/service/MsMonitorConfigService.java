package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.MsMonitorConfig;

import java.util.Map;

/**
 * 全局配置服务接口，提供配置的批量保存与键值对读取能力。
 *
 * @since 2026-08-11
 * @author CH
 */
public interface MsMonitorConfigService extends IService<MsMonitorConfig> {

    /**
     * 读取全部配置项，返回键值对映射。
     *
     * @return 配置键值对Map，key为configKey，value为configValue
     */
    Map<String, String> getAllConfigMap();
}
