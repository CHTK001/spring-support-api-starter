package com.chua.starter.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.admin.entity.SysOperateLogger;
import com.chua.starter.admin.result.LogStaticResult;

import java.time.LocalDateTime;

/**
 * @author CH
 */
public interface SysOperateLoggerService extends IService<SysOperateLogger> {

    /**
     * 按时间统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    LogStaticResult near(LocalDateTime startTime, LocalDateTime endTime);
}
