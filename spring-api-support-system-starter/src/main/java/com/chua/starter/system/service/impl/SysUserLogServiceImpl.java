package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.date.DateUtils;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.common.support.logger.OperateLoggerInfo;
import com.chua.starter.system.entity.SysUserLog;
import com.chua.starter.system.mapper.SysUserLogMapper;
import com.chua.starter.system.service.SysUserLogService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
/**
 *
 * @since 2024/8/8
 * @author CH    
 */
@Service
public class SysUserLogServiceImpl extends ServiceImpl<SysUserLogMapper, SysUserLog> implements SysUserLogService, ApplicationListener<OperateLoggerInfo> {

    @Override
    public void onApplicationEvent(OperateLoggerInfo operateLoggerInfo) {
        SysUserLog sysUserLog = createSysUserLog(operateLoggerInfo);
        ThreadUtils.newStaticThreadPool()
                .execute(() -> {baseMapper.insert(sysUserLog);});
    }

    /**
     * 创建日志
     * @param operateLoggerInfo 日志
     * @return 日志
     */
    private SysUserLog createSysUserLog(OperateLoggerInfo operateLoggerInfo) {
        SysUserLog sysUserLog = new SysUserLog();

        sysUserLog.setSysLogAddress(operateLoggerInfo.getClientIp());
        sysUserLog.setSysLogUrl(operateLoggerInfo.getLogMapping());
        sysUserLog.setSysLogFrom(operateLoggerInfo.getLogModule());
        sysUserLog.setSysLogParam(operateLoggerInfo.getLogParam());
        sysUserLog.setSysLogStatus(operateLoggerInfo.getLogStatus());
        sysUserLog.setSysLogUsername(operateLoggerInfo.getCreateBy());
        sysUserLog.setCreateTime(DateUtils.toLocalDateTime(operateLoggerInfo.getCreateTime()));
        sysUserLog.setUpdateTime(DateUtils.toLocalDateTime(operateLoggerInfo.getCreateTime()));

        return sysUserLog;
    }
}
