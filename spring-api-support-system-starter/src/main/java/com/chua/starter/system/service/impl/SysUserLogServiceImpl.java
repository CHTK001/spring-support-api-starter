package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.geo.GeoCity;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.date.DateUtils;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.starter.common.support.logger.OperateLoggerInfo;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.IptablesService;
import com.chua.starter.system.entity.SysUserLog;
import com.chua.starter.system.mapper.SysUserLogMapper;
import com.chua.starter.system.pojo.user.log.SysUserLogQuery;
import com.chua.starter.system.service.SysUserLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

/**
 * @author CH
 * @since 2024/8/8
 */
@Service
@RequiredArgsConstructor
public class SysUserLogServiceImpl extends ServiceImpl<SysUserLogMapper, SysUserLog> implements SysUserLogService, ApplicationListener<OperateLoggerInfo> {

    final IptablesService iptablesService;

    @Override
    public void onApplicationEvent(OperateLoggerInfo operateLoggerInfo) {
        SysUserLog sysUserLog = createSysUserLog(operateLoggerInfo);
        ThreadUtils.newStaticThreadPool()
                .execute(() -> {
                    baseMapper.insert(sysUserLog);
                });
    }

    /**
     * 创建日志
     *
     * @param operateLoggerInfo 日志
     * @return 日志
     */
    private SysUserLog createSysUserLog(OperateLoggerInfo operateLoggerInfo) {
        SysUserLog sysUserLog = new SysUserLog();

        sysUserLog.setSysLogIp(operateLoggerInfo.getClientIp());
        sysUserLog.setSysLogUrl(operateLoggerInfo.getLogMapping());
        sysUserLog.setSysLogFrom(operateLoggerInfo.getLogModule());
        sysUserLog.setSysLogParam(operateLoggerInfo.getLogParam());
        sysUserLog.setSysLogStatus(operateLoggerInfo.getLogStatus());
        sysUserLog.setSysLogUsername(operateLoggerInfo.getCreateBy());
        sysUserLog.setSysLogCost(BigDecimal.valueOf(operateLoggerInfo.getLogCost()));
        ReturnResult<GeoCity> geoCityReturnResult = null;
        try {
            geoCityReturnResult = iptablesService.transferAddress(sysUserLog.getSysLogIp());
        } catch (Exception ignored) {
        }
        if (null != geoCityReturnResult && geoCityReturnResult.isOk()) {
            sysUserLog.setSysLogAddress(geoCityReturnResult.getData().toString());
            sysUserLog.setSysLogIsp(geoCityReturnResult.getData().getIsp());
        }
        sysUserLog.setCreateTime(DateUtils.toLocalDateTime(operateLoggerInfo.getCreateTime()));
        sysUserLog.setUpdateTime(DateUtils.toLocalDateTime(operateLoggerInfo.getCreateTime()));

        return sysUserLog;
    }

    @Override
    public IPage<SysUserLog> pageForUserLog(Query<SysUserLog> query, SysUserLogQuery sysUserLogQuery, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysUserLog>lambdaQuery()
                .in(SysUserLog::getSysLogFrom, "LOGIN", "LOGOUT")
                .like(StringUtils.isNotBlank(sysUserLogQuery.getSysLogUsername()), SysUserLog::getSysLogUsername, sysUserLogQuery.getSysLogUsername())
                .like(StringUtils.isNotBlank(sysUserLogQuery.getSysLogFrom()), SysUserLog::getSysLogFrom, sysUserLogQuery.getSysLogFrom())
                .like(StringUtils.isNotBlank(sysUserLogQuery.getSysLogAddress()), SysUserLog::getSysLogAddress, sysUserLogQuery.getSysLogAddress())
                .like(StringUtils.isNotBlank(sysUserLogQuery.getSysLogIp()), SysUserLog::getSysLogIp, sysUserLogQuery.getSysLogIp())
                .eq(ObjectUtils.isNotEmpty(sysUserLogQuery.getSysLogStatus()), SysUserLog::getSysLogStatus, sysUserLogQuery.getSysLogStatus())
                .ge(null != sysUserLogQuery.getStartDate(), SysUserLog::getCreateTime, sysUserLogQuery.getStartDate())
                .le(null != sysUserLogQuery.getEndDate(), SysUserLog::getCreateTime, sysUserLogQuery.getEndDate())
                .orderByDesc(SysUserLog::getCreateTime)
        );
    }

    @Override
    public ReturnResult<Boolean> delete(Long userLogId) {
        return baseMapper.deleteById(userLogId) > 0 ? ReturnResult.ok() : ReturnResult.error();
    }

    @Override
    public IPage<SysUserLog> mineLogs(Query<SysUserLog> query, SysUserLogQuery sysUserLogQuery, Integer sysUserId, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), Wrappers.<SysUserLog>lambdaQuery()
                .eq(SysUserLog::getCreateBy, sysUserId)
                .like(StringUtils.isNotBlank(sysUserLogQuery.getSysLogUsername()), SysUserLog::getSysLogUsername, sysUserLogQuery.getSysLogUsername())
                .like(StringUtils.isNotBlank(sysUserLogQuery.getSysLogFrom()), SysUserLog::getSysLogFrom, sysUserLogQuery.getSysLogFrom())
                .like(StringUtils.isNotBlank(sysUserLogQuery.getSysLogAddress()), SysUserLog::getSysLogAddress, sysUserLogQuery.getSysLogAddress())
                .eq(ObjectUtils.isNotEmpty(sysUserLogQuery.getSysLogStatus()), SysUserLog::getSysLogStatus, sysUserLogQuery.getSysLogStatus())
                .orderByDesc(SysUserLog::getCreateTime)
        );
    }
}
