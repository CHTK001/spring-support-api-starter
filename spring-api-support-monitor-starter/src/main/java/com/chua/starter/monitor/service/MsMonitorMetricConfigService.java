package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.MsMonitorMetricConfig;

import java.util.List;

/**
   * 指标解析规则服务接口，提供解析规则基础 CRUD 与按任务标识查询能力。
 *
 * @since 2026-08-11
 * @author CH
 */
public interface MsMonitorMetricConfigService extends IService<MsMonitorMetricConfig> {

    /**
      * 根据采集任务标识查询关联的解析规则列表。
     *
     * @param taskId 任务标识
     * @return 解析规则列表
     */
    List<MsMonitorMetricConfig> listByTaskId(Long taskId);
}
