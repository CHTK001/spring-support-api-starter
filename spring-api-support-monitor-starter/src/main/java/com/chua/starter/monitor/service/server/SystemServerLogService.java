package com.chua.starter.monitor.service.server;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.network.protocol.firewall.FirewallEvent;
import com.chua.common.support.network.protocol.firewall.FirewallMetrics;
import com.chua.starter.monitor.entity.SystemServerLog;

import java.util.List;
import java.time.LocalDateTime;

public interface SystemServerLogService extends IService<SystemServerLog> {

    ReturnResult<IPage<SystemServerLog>> pageLogs(Page<SystemServerLog> page,
                                                  Integer serverId,
                                                  String filterType,
                                                  String processStatus,
                                                  String clientIp,
                                                  LocalDateTime startTime,
                                                  LocalDateTime endTime);

    ReturnResult<byte[]> exportCsv(Integer serverId,
                                   String filterType,
                                   String processStatus,
                                   String clientIp,
                                   LocalDateTime startTime,
                                   LocalDateTime endTime);

    ReturnResult<Integer> cleanup(LocalDateTime beforeTime);

    ReturnResult<List<FirewallEvent>> getFirewallEvents(Integer serverId,
                                                        String clientIp,
                                                        String action,
                                                        LocalDateTime startTime,
                                                        LocalDateTime endTime,
                                                        Integer limit);

    ReturnResult<FirewallMetrics> getFirewallMetrics(Integer serverId,
                                                     LocalDateTime startTime,
                                                     LocalDateTime endTime);

    void asyncRecord(SystemServerLog log);
}

