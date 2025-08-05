package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.MonitorJobLog;
import com.chua.starter.monitor.job.pojo.JobStatisticResult;

/**
 * 监控日志
 * @author Administrator
 */
public interface MonitorJobLogService extends IService<MonitorJobLog>{


    /**
     * 时间
     *
     * @param entity 实体
     * @return {@link MonitorJobLog}
     */
    JobStatisticResult time(MonitorJobLog entity);

    /**
     * 清除
     *
     * @param entity 实体
     * @return {@link JobStatisticResult}
     */
    Boolean clear(MonitorJobLog entity);

    /**
     * 日志
     *
     * @param jobLogId 日志id
     * @return {@link String}
     */
    String logCat(int jobLogId);
}
