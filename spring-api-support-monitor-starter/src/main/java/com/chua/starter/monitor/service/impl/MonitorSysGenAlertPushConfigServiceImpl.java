package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenAlertPushConfig;
import com.chua.starter.monitor.enums.AlertType;
import com.chua.starter.monitor.mapper.MonitorSysGenAlertPushConfigMapper;
import com.chua.starter.monitor.service.MonitorSysGenAlertPushConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenAlertPushConfigServiceImpl extends ServiceImpl<MonitorSysGenAlertPushConfigMapper, MonitorSysGenAlertPushConfig>
        implements MonitorSysGenAlertPushConfigService {

    @Override
    public ReturnResult<List<MonitorSysGenAlertPushConfig>> listEnabledByServerAndType(Integer serverId, AlertType type) {
        try {
            List<MonitorSysGenAlertPushConfig> list = this.baseMapper.selectEnabledByServerAndType(serverId, type == null ? null : type.getCode());
            return ReturnResult.ok(list);
        } catch (Exception e) {
            log.error("查询告警推送配置失败 serverId={} type={}", serverId, type, e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }
}

