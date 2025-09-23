package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.SystemSoftContainerStats;
import com.chua.starter.monitor.mapper.SystemSoftContainerStatsMapper;
import com.chua.starter.monitor.service.SystemSoftContainerStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 容器统计信息服务实现类
 *
 * @author CH
 * @version 1.0
 * @since 2025/01/15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSoftContainerStatsServiceImpl extends ServiceImpl<SystemSoftContainerStatsMapper, SystemSoftContainerStats> implements SystemSoftContainerStatsService {

}