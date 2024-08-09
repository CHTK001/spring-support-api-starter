package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.date.DateUtils;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.common.support.logger.SysLoggerInfo;
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
public class SysUserLogServiceImpl extends ServiceImpl<SysUserLogMapper, SysUserLog> implements SysUserLogService, ApplicationListener<SysLoggerInfo> {

    @Override
    public void onApplicationEvent(SysLoggerInfo sysLoggerInfo) {
        SysUserLog sysUserLog = createSysUserLog(sysLoggerInfo);
        ThreadUtils.newStaticThreadPool()
                .execute(() -> {baseMapper.insert(sysUserLog);});
    }

    /**
     * 创建日志
     * @param sysLoggerInfo 日志
     * @return 日志
     */
    private SysUserLog createSysUserLog(SysLoggerInfo sysLoggerInfo) {
        SysUserLog sysUserLog = new SysUserLog();

        sysUserLog.setSysLogAddress(sysLoggerInfo.getClientIp());
        sysUserLog.setSysLogUrl(sysLoggerInfo.getLogMapping());
        sysUserLog.setSysLogFrom(sysLoggerInfo.getLogModule());
        sysUserLog.setSysLogParam(sysLoggerInfo.getLogParam());
        sysUserLog.setSysLogStatus(sysLoggerInfo.getLogStatus());
        sysUserLog.setSysLogUsername(sysLoggerInfo.getCreateBy());
        sysUserLog.setCreateTime(DateUtils.toLocalDateTime(sysLoggerInfo.getCreateTime()));
        sysUserLog.setUpdateTime(DateUtils.toLocalDateTime(sysLoggerInfo.getCreateTime()));

        return sysUserLog;
    }
}
