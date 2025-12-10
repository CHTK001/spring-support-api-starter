package com.chua.starter.monitor.service.message;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenMessagePushTemplate;

/**
 * 消息推送模板服务
 */
public interface MonitorSysGenMessagePushTemplateService extends IService<MonitorSysGenMessagePushTemplate> {

    ReturnResult<MonitorSysGenMessagePushTemplate> getEnabledById(Integer templateId);
}

