package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.MonitorServer;

import java.util.List;
import java.util.Map;

/**
 * 被监控服务器主机服务接口，提供服务器基础 CRUD 与下拉选项能力。
 *
 * @since 2026-07-28
 * @author CH
 */
public interface MonitorServerService extends IService<MonitorServer> {

    /**
      * 获取服务器下拉选项（标识 与名称），用于布局复制等场景的目标服务器选择。
     *
     * @return 服务器选项列表，每项包含 服务端id 与 服务端名称
     */
    List<Map<String, Object>> listOptions();
}
