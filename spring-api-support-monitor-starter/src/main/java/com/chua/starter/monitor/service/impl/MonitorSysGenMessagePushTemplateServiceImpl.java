package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenMessagePushTemplate;
import com.chua.starter.monitor.mapper.MonitorSysGenMessagePushTemplateMapper;
import com.chua.starter.monitor.service.message.MonitorSysGenMessagePushTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MonitorSysGenMessagePushTemplateServiceImpl extends ServiceImpl<MonitorSysGenMessagePushTemplateMapper, MonitorSysGenMessagePushTemplate>
        implements MonitorSysGenMessagePushTemplateService {
    @Override
    public ReturnResult<MonitorSysGenMessagePushTemplate> getEnabledById(Integer templateId) {
        try {
            MonitorSysGenMessagePushTemplate t = getOne(Wrappers.<MonitorSysGenMessagePushTemplate>lambdaQuery()
                    .eq(MonitorSysGenMessagePushTemplate::getMonitorSysGenMessagePushTemplateId, templateId)
                    .eq(MonitorSysGenMessagePushTemplate::getMonitorSysGenMessagePushTemplateEnabled, true)
            , false);
            return ReturnResult.ok(t);
        } catch (Exception e) {
            log.error("查询模板失败 id={}", templateId, e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }
}

