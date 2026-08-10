package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MonitorServer;
import com.chua.starter.monitor.mapper.MonitorServerMapper;
import com.chua.starter.monitor.service.MonitorServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 被监控服务器主机服务实现，提供服务器基础 CRUD 与下拉选项能力。
 *
 * @author CH
 * @since 2026-07-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorServerServiceImpl extends ServiceImpl<MonitorServerMapper, MonitorServer>
        implements MonitorServerService {

    /**
     * 获取服务器下拉选项（ID 与名称），用于布局复制等场景的目标服务器选择。
     *
     * @return 服务器选项列表
     */
    @Override
    public List<Map<String, Object>> listOptions() {
        return list().stream()
                .map(server -> Map.<String, Object>of(
                        "serverId", server.getServerId(),
                        "serverName", server.getServerName()))
                .collect(Collectors.toList());
    }
}
