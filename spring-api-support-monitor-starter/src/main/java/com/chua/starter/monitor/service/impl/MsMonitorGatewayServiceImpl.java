package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MsMonitorGateway;
import com.chua.starter.monitor.mapper.MsMonitorGatewayMapper;
import com.chua.starter.monitor.service.MsMonitorGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 监控网关服务实现，提供网关基础 CRUD 与按监控资产标识查询能力。
 *
 * @since 2026-08-11
 * @author CH
 */
@Slf4j
@Service
public class MsMonitorGatewayServiceImpl extends ServiceImpl<MsMonitorGatewayMapper, MsMonitorGateway>
        implements MsMonitorGatewayService {

    /**
     * 根据监控资产标识查询关联的网关列表。
     *
     * @param monitorId 监控资产标识
     * @return 网关列表
     */
    @Override
    public List<MsMonitorGateway> listByMonitorId(Long monitorId) {
        return list(Wrappers.<MsMonitorGateway>lambdaQuery()
                .eq(MsMonitorGateway::getMonitorId, monitorId));
    }
}
