package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.geo.support.geo.GeoCity;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.time.date.DateUtils;
import com.chua.common.support.core.utils.ObjectUtils;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.common.support.logger.UserLoggerInfo;
import com.chua.starter.common.support.service.IptablesService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.entity.system.SysUserLog;
import com.chua.starter.system.mapper.SysUserLogMapper;
import com.chua.starter.system.pojo.user.log.SysUserLogQuery;
import com.chua.starter.system.service.SysUserLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
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
    public IPage<SysUserLog> pageForUserLog(Query<SysUserLog> query,
                                            SysUserLogQuery sysUserLogQuery,
                                            Integer sysUserId,
                                            Set<String> roles,
                                            List<String> managedDeptTreeIds) {
        SysUserLogQuery normalizedQuery = normalizeUserLogQuery(sysUserLogQuery);
        boolean isSuperAdmin = roles != null && roles.contains(AuthConstant.SUPER_ADMIN);
        boolean isAdmin = !isSuperAdmin && roles != null && roles.contains(AuthConstant.ADMIN);
        boolean isDeptLeader = !isSuperAdmin && !isAdmin && roles != null && roles.contains("DEPT_LEADER");
        Page<SysUserLog> page = query.createPage();
        Long total = baseMapper.countForUserLog(
                normalizedQuery,
                sysUserId,
                isSuperAdmin,
                isAdmin,
                isDeptLeader,
                managedDeptTreeIds
        );
        long safeTotal = null == total ? 0L : total;
        page.setTotal(safeTotal);
        if (safeTotal <= 0) {
            page.setRecords(List.of());
            return page;
        }
        long offset = Math.max(page.getCurrent() - 1, 0) * page.getSize();
        page.setRecords(baseMapper.listForUserLog(
                normalizedQuery,
                sysUserId,
                isSuperAdmin,
                isAdmin,
                isDeptLeader,
                managedDeptTreeIds,
                offset,
                page.getSize()
        ));
        return page;
    }

    private SysUserLogQuery normalizeUserLogQuery(SysUserLogQuery source) {
        SysUserLogQuery target = null == source ? new SysUserLogQuery() : source;
        target.setSysLogUsername(normalizeNullableString(target.getSysLogUsername()));
        target.setSysLogFrom(normalizeNullableString(target.getSysLogFrom()));
        target.setSysLogAddress(normalizeNullableString(target.getSysLogAddress()));
        target.setSysLogIp(normalizeNullableString(target.getSysLogIp()));
        target.setStartDate(normalizeNullableString(target.getStartDate()));
        target.setEndDate(normalizeNullableString(target.getEndDate()));
        return target;
    }

    private String normalizeNullableString(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    @Override
    public ReturnResult<Boolean> delete(Long userLogId) {
        return baseMapper.deleteById(userLogId) > 0 ? ReturnResult.ok() : ReturnResult.error();
    }

    @Override
    public ReturnResult<Integer> cleanup(SysUserLogQuery sysUserLogQuery,
                                         Integer sysUserId,
                                         Set<String> roles,
                                         List<String> managedDeptTreeIds) {
        SysUserLogQuery normalizedQuery = normalizeUserLogQuery(sysUserLogQuery);
        boolean isSuperAdmin = roles != null && roles.contains(AuthConstant.SUPER_ADMIN);
        boolean isAdmin = !isSuperAdmin && roles != null && roles.contains(AuthConstant.ADMIN);
        boolean isDeptLeader = !isSuperAdmin && !isAdmin && roles != null && roles.contains("DEPT_LEADER");
        int deletedCount = baseMapper.deleteForUserLog(
                normalizedQuery,
                sysUserId,
                isSuperAdmin,
                isAdmin,
                isDeptLeader,
                managedDeptTreeIds
        );
        return ReturnResult.ok(deletedCount);
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
                // 如果有 IP 且地址为空，尝试解析 IP 归属地
                if (StringUtils.isNotBlank(sysUserLog.getSysLogIp())
                        && StringUtils.isBlank(sysUserLog.getSysLogAddress())) {
                    try {
                        ReturnResult<GeoCity> geo = iptablesService.transferAddress(sysUserLog.getSysLogIp());
                        if (geo != null && geo.isOk() && geo.getData() != null) {
                            sysUserLog.setSysLogAddress(geo.getData().toString());
                            sysUserLog.setSysLogIsp(geo.getData().getIsp());
                        }
                    } catch (Exception e) {
                        log.debug("[UserLog] IP 解析失败: ip={}", sysUserLog.getSysLogIp());
                    }
                }
                baseMapper.insert(sysUserLog);
                log.info("[UserLog] 保存成功: from={}, user={}, status={}, ip={}, address={}",
                        sysUserLog.getSysLogFrom(),
                        sysUserLog.getSysLogUsername(),
                        sysUserLog.getSysLogStatus(),
                        sysUserLog.getSysLogIp(),
                        sysUserLog.getSysLogAddress());
            } catch (Exception e) {
                log.error("[UserLog] 保存失败: from={}, user={}, error={}",
                        sysUserLog.getSysLogFrom(), sysUserLog.getSysLogUsername(), e.getMessage());
            }
        });
    }
}
