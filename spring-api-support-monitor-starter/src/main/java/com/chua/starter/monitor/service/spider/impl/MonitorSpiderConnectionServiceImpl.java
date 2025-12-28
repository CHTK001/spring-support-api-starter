package com.chua.starter.monitor.service.spider.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MonitorSpiderConnection;
import com.chua.starter.monitor.mapper.MonitorSpiderConnectionMapper;
import com.chua.starter.monitor.service.spider.MonitorSpiderConnectionService;
import org.springframework.stereotype.Service;

/**
 * 爬虫连接Service实现
 *
 * @author CH
 * @since 2024/12/28
 */
@Service
public class MonitorSpiderConnectionServiceImpl extends ServiceImpl<MonitorSpiderConnectionMapper, MonitorSpiderConnection>
        implements MonitorSpiderConnectionService {
}
