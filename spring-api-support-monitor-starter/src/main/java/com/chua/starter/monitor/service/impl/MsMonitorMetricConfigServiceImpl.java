package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MsMonitorMetricConfig;
import com.chua.starter.monitor.mapper.MsMonitorMetricConfigMapper;
import com.chua.starter.monitor.service.MsMonitorMetricConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 指标解析规则服务实现，提供解析规则基础 CRUD 与按任务ID查询能力。
 *
 * @author CH
 * @since 2026-08-11
 */
@Slf4j
@Service
public class MsMonitorMetricConfigServiceImpl
        extends ServiceImpl<MsMonitorMetricConfigMapper, MsMonitorMetricConfig>
        implements MsMonitorMetricConfigService {

    /**
     * 根据采集任务ID查询关联的解析规则列表。
     *
     * @param taskId 任务ID
     * @return 解析规则列表
     */
    @Override
    public List<MsMonitorMetricConfig> listByTaskId(Long taskId) {
        return list(Wrappers.<MsMonitorMetricConfig>lambdaQuery()
                .eq(MsMonitorMetricConfig::getTaskId, taskId));
    }
}
