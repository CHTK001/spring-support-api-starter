package com.chua.starter.monitor.service.spider.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MonitorSpiderTask;
import com.chua.starter.monitor.mapper.MonitorSpiderTaskMapper;
import com.chua.starter.monitor.service.spider.MonitorSpiderTaskService;
import org.springframework.stereotype.Service;

/**
 * 爬虫任务Service实现
 *
 * @author CH
 * @since 2024/12/28
 */
@Service
public class MonitorSpiderTaskServiceImpl extends ServiceImpl<MonitorSpiderTaskMapper, MonitorSpiderTask>
        implements MonitorSpiderTaskService {
}
