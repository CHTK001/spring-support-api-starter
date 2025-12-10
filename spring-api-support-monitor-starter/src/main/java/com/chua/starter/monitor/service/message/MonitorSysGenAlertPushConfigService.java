package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenAlertPushConfig;
import com.chua.starter.monitor.enums.AlertType;

import java.util.List;

/**
 * 告警推送配置服务
 */
public interface MonitorSysGenAlertPushConfigService extends IService<MonitorSysGenAlertPushConfig> {

    ReturnResult<List<MonitorSysGenAlertPushConfig>> listEnabledByServerAndType(Integer serverId, AlertType type);
}

