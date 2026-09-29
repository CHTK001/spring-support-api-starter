package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.starter.monitor.entity.MsMonitorGateway;

import java.util.List;

/**
 * 监控网关服务接口，提供网关基础 CRUD 与按监控资产标识查询能力。
 *
 * @since 2026-08-11
 * @author CH
 */
public interface MsMonitorGatewayService extends IService<MsMonitorGateway> {

    /**
     * 根据监控资产标识查询关联的网关列表。
     *
     * @param monitorId 监控资产标识
     * @return 网关列表
     */
    List<MsMonitorGateway> listByMonitorId(Long monitorId);
}
