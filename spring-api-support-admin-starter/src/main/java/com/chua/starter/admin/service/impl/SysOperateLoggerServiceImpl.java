package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.lang.date.DateUtils;
import com.chua.common.support.lang.date.constant.DateFormatConstant;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.admin.entity.SysOperateLogger;
import com.chua.starter.admin.mapper.SysOperateLoggerMapper;
import com.chua.starter.admin.pojo.LogStaticVO;
import com.chua.starter.admin.result.LogStaticResult;
import com.chua.starter.admin.service.SysOperateLoggerService;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.logger.SysLoggerInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * 日志接口
 *
 * @author CH
 */
@Service
public class SysOperateLoggerServiceImpl extends ServiceImpl<SysOperateLoggerMapper, SysOperateLogger>
        implements SysOperateLoggerService, Constant , ApplicationListener<SysLoggerInfo> {


    private final Executor executorService;

    public SysOperateLoggerServiceImpl(@Qualifier(Constant.DEFAULT_EXECUTOR2) Executor executorService) {
        this.executorService = executorService;
    }


    @Override
    public LogStaticResult near(LocalDateTime startTime, LocalDateTime endTime) {
        LogStaticResult logStaticResult = new LogStaticResult();
        Map<String, Integer> success = new LinkedHashMap<>();
        Map<String, Integer> failure = new LinkedHashMap<>();
        LocalDateTime tpl = startTime;
        while (!tpl.isAfter(endTime)) {
            String format = DateUtils.format(tpl, DateFormatConstant.YYYY_MM_DD);
            success.put(format, 0);
            failure.put(format, 0);
            tpl = tpl.plusDays(1);
        }
        List<LogStaticVO> near = baseMapper.near(startTime, endTime);
        for (LogStaticVO logStaticVO : near) {
            if ("0".equals(logStaticVO.getLogStatus())) {
                failure.put(logStaticVO.getLogDay(), logStaticVO.getCnt());
                continue;
            }
            success.put(logStaticVO.getLogDay(), logStaticVO.getCnt());
        }
        logStaticResult.setSuccess(new LinkedList<>(success.values()));
        logStaticResult.setFailure(new LinkedList<>(failure.values()));

        return logStaticResult;
    }

    @Override
    public void onApplicationEvent(SysLoggerInfo sysLog) {
        if (StringUtils.isEmpty(sysLog.getLogName()) || StringUtils.isEmpty(sysLog.getLogCode())) {
            return;
        }

        executorService.execute(() -> {
            try {
                doSave(sysLog);
            } catch (Throwable ignored) {
            }
        });
    }


    private void doSave(SysLoggerInfo sysLog) {
        SysOperateLogger sysLogger1 = BeanUtils.copyProperties(sysLog, SysOperateLogger.class);
        if(null != sysLogger1) {
            sysLogger1.setLogDay(new Date());
            saveOrUpdate(sysLogger1, Wrappers.<SysOperateLogger>lambdaQuery().eq(SysOperateLogger::getLogCode, sysLogger1.getLogCode()));
        }
    }
}
