package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MsMonitor;
import com.chua.starter.monitor.mapper.MsMonitorMapper;
import com.chua.starter.monitor.service.MsMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
* 监控资产服务实现，提供监控资产基础 CRUD 能力。
*
* @since 2026-08-11
* @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MsMonitorServiceImpl extends ServiceImpl<MsMonitorMapper, MsMonitor>
        implements MsMonitorService {
}
