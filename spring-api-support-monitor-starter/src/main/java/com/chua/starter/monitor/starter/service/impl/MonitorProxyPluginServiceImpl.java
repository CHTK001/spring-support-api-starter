package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.starter.entity.MonitorProxyPlugin;
import com.chua.starter.monitor.starter.mapper.MonitorProxyPluginMapper;
import com.chua.starter.monitor.starter.service.MonitorProxyPluginService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author CH
 * @since 2024/9/16
 */
@Service
@RequiredArgsConstructor
public class MonitorProxyPluginServiceImpl extends ServiceImpl<MonitorProxyPluginMapper, MonitorProxyPlugin> implements MonitorProxyPluginService {

}