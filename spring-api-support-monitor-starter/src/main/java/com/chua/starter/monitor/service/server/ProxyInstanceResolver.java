package com.chua.starter.monitor.service.server;

import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.pojo.server.ProxyConnectionInfo;

public interface ProxyInstanceResolver {

    ProxyConnectionInfo resolve(MonitorSysGenServer server);

    ProxyConnectionInfo resolve(MonitorSysGenServer server, MonitorSysGenServerSetting setting);
}
