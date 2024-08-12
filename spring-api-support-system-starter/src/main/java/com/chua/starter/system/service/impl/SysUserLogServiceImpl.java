package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.date.DateUtils;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.common.support.logger.OperateLoggerInfo;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.entity.SysUserLog;
import com.chua.starter.system.mapper.SysUserLogMapper;
import com.chua.starter.system.service.SysUserLogService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.Set;

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

    @Override
    public IPage<SysUserLog> pageForUserLog(Query<SysUserLog> query, SysUserLog sysUserLog, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysUserLog>lambdaQuery()
                .like(StringUtils.isNotBlank(sysUserLog.getSysLogUsername()), SysUserLog::getSysLogUsername, sysUserLog.getSysLogUsername())
                .like(StringUtils.isNotBlank(sysUserLog.getSysLogFrom()), SysUserLog::getSysLogFrom, sysUserLog.getSysLogFrom())
                .like(StringUtils.isNotBlank(sysUserLog.getSysLogAddress()), SysUserLog::getSysLogAddress, sysUserLog.getSysLogAddress())
                .eq(ObjectUtils.isNotEmpty(sysUserLog.getSysLogStatus()), SysUserLog::getSysLogStatus, sysUserLog.getSysLogStatus())
                .orderByDesc(SysUserLog::getCreateTime)
        );
    }

    @Override
    public ReturnResult<Boolean> delete(Long userLogId) {
        return baseMapper.deleteById(userLogId) > 0 ? ReturnResult.ok() : ReturnResult.error();
    }
}
