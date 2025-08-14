package com.chua.starter.monitor.task.manager;

import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;

/**
 * 通用任务步骤接口
 * 每个具体功能实现一个步骤类，便于拆分维护
 */
@FunctionalInterface
public interface TaskStep {

    /**
     * 执行任务步骤
     *
     * @param server  服务器信息
     * @param setting 服务器配置
     */
    void run(MonitorSysGenServer server, MonitorSysGenServerSetting setting);
}

