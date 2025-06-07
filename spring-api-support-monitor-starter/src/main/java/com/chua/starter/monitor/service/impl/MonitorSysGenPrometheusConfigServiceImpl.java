package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenPrometheusConfig;
import com.chua.starter.monitor.mapper.MonitorSysGenPrometheusConfigMapper;
import com.chua.starter.monitor.service.MonitorSysGenPrometheusConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author CH
 * @since 2025/6/6 16:50
 */
@Service
public class MonitorSysGenPrometheusConfigServiceImpl extends ServiceImpl<MonitorSysGenPrometheusConfigMapper, MonitorSysGenPrometheusConfig> implements MonitorSysGenPrometheusConfigService {

    @Override
    public List<MonitorSysGenPrometheusConfig> listPrometheusConfig(Integer genId, String monitorSysGenPrometheusConfigType) {
        return this.list(
                Wrappers.lambdaQuery(MonitorSysGenPrometheusConfig.class)
                        .eq(MonitorSysGenPrometheusConfig::getMonitorSysGenId, genId)
                        .eq(MonitorSysGenPrometheusConfig::getMonitorSysGenPrometheusConfigEnable, true)
                        .eq(StringUtils.isNotBlank(monitorSysGenPrometheusConfigType), MonitorSysGenPrometheusConfig::getMonitorSysGenPrometheusConfigType, monitorSysGenPrometheusConfigType)
                        .orderByDesc(MonitorSysGenPrometheusConfig::getMonitorSysGenPrometheusConfigId)
        );
    }

    @Override
    public ReturnResult<Boolean> savePrometheusConfig(MonitorSysGenPrometheusConfig monitorSysGenPrometheusConfig) {
        return ReturnResult.success(this.save(monitorSysGenPrometheusConfig));
    }

    @Override
    public ReturnResult<Boolean> updatePrometheusConfig(MonitorSysGenPrometheusConfig monitorSysGenPrometheusConfig) {
        return ReturnResult.success(this.updateById(monitorSysGenPrometheusConfig));
    }

    @Override
    public ReturnResult<Boolean> deletePrometheusConfig(Integer id) {
        return ReturnResult.success(this.removeById(id));
    }

    @Override
    public ReturnResult<Boolean> sharePrometheusConfig(Integer id) {
        return ReturnResult.success(this.update(Wrappers.lambdaUpdate(MonitorSysGenPrometheusConfig.class)
                .set(MonitorSysGenPrometheusConfig::getMonitorSysGenPrometheusConfigShare, true)
                .eq(MonitorSysGenPrometheusConfig::getMonitorSysGenPrometheusConfigId, id)
        ));
    }

    @Override
    public ReturnResult<List<MonitorSysGenPrometheusConfig>> sharePrometheusConfig() {
        return ReturnResult.success(this.list(Wrappers.lambdaQuery(MonitorSysGenPrometheusConfig.class)
                .eq(MonitorSysGenPrometheusConfig::getMonitorSysGenPrometheusConfigShare, true)
        ));
    }
}
