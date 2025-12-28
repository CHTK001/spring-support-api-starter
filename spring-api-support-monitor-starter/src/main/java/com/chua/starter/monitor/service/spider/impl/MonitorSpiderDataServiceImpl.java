package com.chua.starter.monitor.service.spider.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MonitorSpiderData;
import com.chua.starter.monitor.mapper.MonitorSpiderDataMapper;
import com.chua.starter.monitor.service.spider.MonitorSpiderDataService;
import org.springframework.stereotype.Service;

/**
 * 爬虫数据Service实现
 *
 * @author CH
 * @since 2024/12/28
 */
@Service
public class MonitorSpiderDataServiceImpl extends ServiceImpl<MonitorSpiderDataMapper, MonitorSpiderData>
        implements MonitorSpiderDataService {
}
