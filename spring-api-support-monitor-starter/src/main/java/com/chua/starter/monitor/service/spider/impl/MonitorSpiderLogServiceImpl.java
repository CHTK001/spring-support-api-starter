package com.chua.starter.monitor.service.spider.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MonitorSpiderLog;
import com.chua.starter.monitor.mapper.MonitorSpiderLogMapper;
import com.chua.starter.monitor.service.spider.MonitorSpiderLogService;
import org.springframework.stereotype.Service;

/**
 * 爬虫日志Service实现
 *
 * @author CH
 * @since 2024/12/28
 */
@Service
public class MonitorSpiderLogServiceImpl extends ServiceImpl<MonitorSpiderLogMapper, MonitorSpiderLog>
        implements MonitorSpiderLogService {
}
