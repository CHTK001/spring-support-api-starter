package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.common.support.network.ip.GeoCity;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.DateUtils;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
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
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
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

    final IptablesService iptablesService; // iptables服务

    @Override
    public void onApplicationEvent(UserLoggerInfo userLoggerInfo) {
//        SysUserLog sysUserLog = createSysUserLog(userLoggerInfo);
//        ThreadUtils.newStaticThreadPool()
//                .execute(() -> {
//                    baseMapper.insert(sysUserLog);
//                });
    }

    /**
    /**
     * 创建用户登录日志
     *
     * @param userLoggerInfo 日志信息
     * @return 日志实体
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

    /**
     * normalize用户Log查询。
     *
     * @param source 来源，不允许为 null
     * @return Sys用户Log查询 对象
     */
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

    /**
     * normalizeNullable字符串。
     *
     * @param value 值，不允许为 null
     * @return 结果字符串
     */
    private String normalizeNullableString(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    @Override
    public ReturnResult<Boolean> delete(Long userLogId) {
        return baseMapper.deleteById(userLogId) > 0 ? ReturnResult.ok() : ReturnResult.error("删除失败");
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
    public IPage<SysUserLog> mineLogs(Query<SysUserLog> query, SysUserLogQuery sysUserLogQuery,
                                      Integer sysUserId, String username, Set<String> roles) {
        boolean hasUserId = null != sysUserId;
        boolean hasUsername = StringUtils.isNotBlank(username);
        if (!hasUserId && !hasUsername) {
            Page<SysUserLog> empty = query.createPage();
            empty.setRecords(List.of());
            return empty;
        }
        LambdaQueryWrapper<SysUserLog> wrapper = Wrappers.<SysUserLog>lambdaQuery();
        // 失败登录无令牌归属，只能按登录名归到本人
        if (hasUserId && hasUsername) {
            wrapper.and(w -> w.eq(SysUserLog::getCreateBy, sysUserId).or().eq(SysUserLog::getSysLogUsername, username));
        } else if (hasUserId) {
            wrapper.eq(SysUserLog::getCreateBy, sysUserId);
        } else {
            wrapper.eq(SysUserLog::getSysLogUsername, username);
        }
        return baseMapper.selectPage(query.createPage(), wrapper
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

    /**
     * 单次导出最大行数，避免超大结果集拖垮服务
    */
    private static final long EXPORT_MAX_ROWS = 50000L;
    /**
     * CSV 导出时间格式化
    */
    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public byte[] exportUserLogs(SysUserLogQuery sysUserLogQuery,
                                 Integer sysUserId,
                                 Set<String> roles,
                                 List<String> managedDeptTreeIds) {
        SysUserLogQuery normalizedQuery = normalizeUserLogQuery(sysUserLogQuery);
        boolean isSuperAdmin = roles != null && roles.contains(AuthConstant.SUPER_ADMIN);
        boolean isAdmin = !isSuperAdmin && roles != null && roles.contains(AuthConstant.ADMIN);
        boolean isDeptLeader = !isSuperAdmin && !isAdmin && roles != null && roles.contains("DEPT_LEADER");

        // 复用分页查询的权限与筛选逻辑，拉取全量（上限 EXPORT_MAX_ROWS）
        List<SysUserLog> records = baseMapper.listForUserLog(
                normalizedQuery,
                sysUserId,
                isSuperAdmin,
                isAdmin,
                isDeptLeader,
                managedDeptTreeIds,
                0L,
                EXPORT_MAX_ROWS
        );

        StringBuilder sb = new StringBuilder();
        sb.append("日志ID,操作账号,登录方式,请求模块,请求IP,地理位置,运营商,操作系统,浏览器,请求URL,状态,耗时(毫秒),操作时间\r\n");
        for (SysUserLog log : records) {
            sb.append(csv(log.getSysLogId())).append(',')
                    .append(csv(log.getSysLogUsername())).append(',')
                    .append(csv(log.getSysLogLoginType())).append(',')
                    .append(csv(log.getSysLogFrom())).append(',')
                    .append(csv(log.getSysLogIp())).append(',')
                    .append(csv(log.getSysLogAddress())).append(',')
                    .append(csv(log.getSysLogIsp())).append(',')
                    .append(csv(log.getSysLogSystem())).append(',')
                    .append(csv(log.getSysLogBrowser())).append(',')
                    .append(csv(log.getSysLogUrl())).append(',')
                    .append(log.getSysLogStatus() == null ? "" : (log.getSysLogStatus() == 1 ? "成功" : "失败")).append(',')
                    .append(null == log.getSysLogCost() ? "" : log.getSysLogCost()).append(',')
                    .append(null == log.getCreateTime() ? "" : log.getCreateTime().format(EXPORT_TIME_FORMATTER)).append("\r\n");
        }
        return withUtf8Bom(sb.toString());
    }

    /**
     * CSV字段转义：含逗号/引号/换行时用双引号包裹并转义内部双引号
     *
     * @param value 原始值
     * @return 转义后的CSV字段
     */
    private String csv(Object value) {
        if (null == value) {
            return "";
        }
        String text = String.valueOf(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    /**
     * 给CSV内容前置UTF-8 BOM，便于Excel正确识别中文
     *
     * @param content CSV文本
     * @return 带BOM的字节数组
     */
    private byte[] withUtf8Bom(String content) {
        byte[] body = content.getBytes(StandardCharsets.UTF_8);
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] result = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(body, 0, result, bom.length, body.length);
        return result;
    }
}
