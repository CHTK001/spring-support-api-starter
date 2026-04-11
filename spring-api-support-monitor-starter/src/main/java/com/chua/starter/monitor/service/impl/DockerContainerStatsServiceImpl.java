package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.DockerContainerStats;
import com.chua.starter.monitor.mapper.DockerContainerStatsMapper;
import com.chua.starter.monitor.service.docker.DockerContainerStatsService;
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
public class DockerContainerStatsServiceImpl extends ServiceImpl<DockerContainerStatsMapper, DockerContainerStats> implements DockerContainerStatsService {

}