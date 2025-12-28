package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.geo.GeoCity;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.date.DateUtils;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.logger.UserLoggerInfo;
import com.chua.starter.common.support.service.IptablesService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysDept;
import com.chua.starter.entity.system.SysUserLog;
import com.chua.starter.system.mapper.SysUserLogMapper;
import com.chua.starter.system.pojo.user.log.SysUserLogQuery;
import com.chua.starter.system.service.SysUserLogService;
import com.github.yulichang.toolkit.MPJWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

/**
 * 用户操作日志服务实现类
 * <p>提供用户操作日志的记录、查询和删除功能</p>
 *
 * @author CH
 * @since 2024/8/8
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserLogServiceImpl extends ServiceImpl<SysUserLogMapper, SysUserLog> implements SysUserLogService, ApplicationListener<UserLoggerInfo> {

    final IptablesService iptablesService;

    @Override
    public void onApplicationEvent(UserLoggerInfo userLoggerInfo) {
//        SysUserLog sysUserLog = createSysUserLog(userLoggerInfo);
//        ThreadUtils.newStaticThreadPool()
//                .execute(() -> {
//                    baseMapper.insert(sysUserLog);
//                });
    }

    /**
     * 鍒涘缓日志
     *
     * @param userLoggerInfo 日志
     * @return 日志
     */
    private SysUserLog createSysUserLog(UserLoggerInfo userLoggerInfo) {
        SysUserLog sysUserLog = new SysUserLog();

        sysUserLog.setSysLogIp(userLoggerInfo.getClientIp());
        sysUserLog.setSysLogUrl(userLoggerInfo.getLogMapping());
        sysUserLog.setSysLogFrom(userLoggerInfo.getLogModule());
        sysUserLog.setSysLogParam(userLoggerInfo.getLogParam());
        sysUserLog.setSysLogStatus(userLoggerInfo.getLogStatus());
        sysUserLog.setSysLogUsername(userLoggerInfo.getCreateName());
        sysUserLog.setSysLogCost(BigDecimal.valueOf(userLoggerInfo.getLogCost()));
        sysUserLog.setSysLogBrowser(userLoggerInfo.getBrowser());
        sysUserLog.setSysLogSystem(userLoggerInfo.getSystem());
        sysUserLog.setSysLogLoginType(userLoggerInfo.getLoginType());
        sysUserLog.setSysLogUa(userLoggerInfo.getUa());
        sysUserLog.setSysLogFingerprint(userLoggerInfo.getFingerprint());
        sysUserLog.setSysDeptId(null);
        try {
            sysUserLog.setCreateBy(Integer.valueOf(userLoggerInfo.getCreateBy()));
        } catch (NumberFormatException e) {
            log.debug("创建者ID转换失败: {}", userLoggerInfo.getCreateBy());
        }
        ReturnResult<GeoCity> geoCityReturnResult = null;
        try {
            geoCityReturnResult = iptablesService.transferAddress(sysUserLog.getSysLogIp());
        } catch (Exception e) {
            log.debug("IP地址转换失败: {}", sysUserLog.getSysLogIp());
        }
        if (null != geoCityReturnResult && geoCityReturnResult.isOk()) {
            sysUserLog.setSysLogAddress(geoCityReturnResult.getData().toString());
            sysUserLog.setSysLogIsp(geoCityReturnResult.getData().getIsp());
        }
        sysUserLog.setCreateTime(DateUtils.toLocalDateTime(userLoggerInfo.getCreateTime()));
        sysUserLog.setUpdateTime(DateUtils.toLocalDateTime(userLoggerInfo.getCreateTime()));

        return sysUserLog;
    }

    @Override
    public IPage<SysUserLog> pageForUserLog(Query<SysUserLog> query, SysUserLogQuery sysUserLogQuery, Set<String> roles) {
        return baseMapper.selectPage(query.createPage(), MPJWrappers.<SysUserLog>lambdaJoin()
                .selectAll(SysUserLog.class)
                .selectAs(SysDept::getSysDeptName, SysUserLog::getSysDeptName)
                .leftJoin(SysDept.class, SysDept::getSysDeptId, SysUserLog::getSysDeptId)
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

    @Override
    public void asyncSaveUserLog(SysUserLog sysUserLog) {
        if (null == sysUserLog) {
            return;
        }
        Thread.ofVirtual().start(() -> {
            try {
                baseMapper.insert(sysUserLog);
            } catch (Exception e) {
                log.error("异步保存用户日志失败: {}", e.getMessage());
            }
        });
    }
}
