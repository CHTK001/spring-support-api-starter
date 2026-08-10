package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.starter.monitor.entity.MonitorServer;

import java.util.List;
import java.util.Map;

/**
 * 被监控服务器主机服务接口，提供服务器基础 CRUD 与下拉选项能力。
 *
 * @author CH
 * @since 2026-07-28
 */
public interface MonitorServerService extends IService<MonitorServer> {

    /**
     * 获取服务器下拉选项（ID 与名称），用于布局复制等场景的目标服务器选择。
     *
     * @return 服务器选项列表，每项包含 serverId 与 serverName
     */
    List<Map<String, Object>> listOptions();
}
