package com.chua.starter.monitor.service.spider.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MonitorSpiderNode;
import com.chua.starter.monitor.mapper.MonitorSpiderNodeMapper;
import com.chua.starter.monitor.service.spider.MonitorSpiderNodeService;
import org.springframework.stereotype.Service;

/**
 * 爬虫节点Service实现
 *
 * @author CH
 * @since 2024/12/28
 */
@Service
public class MonitorSpiderNodeServiceImpl extends ServiceImpl<MonitorSpiderNodeMapper, MonitorSpiderNode>
        implements MonitorSpiderNodeService {
}
