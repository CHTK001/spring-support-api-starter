package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
* @since 2026-07-28
* @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorServerServiceImpl extends ServiceImpl<MonitorServerMapper, MonitorServer>
        implements MonitorServerService {

    /**
    * 下拉选项键：服务器标识
     */
    private static final String KEY_SERVER_ID = "serverId";

    /**
    * 下拉选项键：服务器名称
     */
    private static final String KEY_SERVER_NAME = "serverName";

    /**
    * 获取服务器下拉选项（标识 与名称），用于布局复制等场景的目标服务器选择。
    *
    * @return 服务器选项列表
     */
    @Override
    public List<Map<String, Object>> listOptions() {
        return list().stream()
                .map(server -> Map.<String, Object>of(
                        KEY_SERVER_ID, server.getServerId(),
                        KEY_SERVER_NAME, server.getServerName()))
                .collect(Collectors.toList());
    }
}
