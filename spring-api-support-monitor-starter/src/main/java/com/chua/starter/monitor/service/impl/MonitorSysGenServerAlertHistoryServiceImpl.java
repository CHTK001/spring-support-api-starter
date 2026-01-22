package com.chua.starter.monitor.service.impl;

import com.chua.starter.mybatis.utils.ReturnPageResultUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.CollectionUtils;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.monitor.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.enums.AlertLevel;
import com.chua.starter.monitor.enums.AlertStatus;
import com.chua.starter.monitor.enums.AlertType;
import com.chua.starter.monitor.mapper.MonitorSysGenServerAlertHistoryMapper;
import com.chua.starter.monitor.service.server.MonitorSysGenServerAlertHistoryService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 告警历史服务实现类
 *
 * @author CH
 * @since 2024/12/27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenServerAlertHistoryServiceImpl 
        extends ServiceImpl<MonitorSysGenServerAlertHistoryMapper, MonitorSysGenServerAlertHistory> 
        implements MonitorSysGenServerAlertHistoryService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> saveAlert(MonitorSysGenServerAlertHistory alert) {
        try {
            // 设置默认值
            if (alert.getMonitorSysGenServerAlertHistoryTriggerTime() == null) {
                alert.setMonitorSysGenServerAlertHistoryTriggerTime(LocalDateTime.now());
            }
            if (alert.getMonitorSysGenServerAlertHistoryStatus() == null) {
                alert.setMonitorSysGenServerAlertHistoryStatus(AlertStatus.TRIGGERED);
            }
            if (alert.getMonitorSysGenServerAlertHistoryNotificationSent() == null) {
                alert.setMonitorSysGenServerAlertHistoryNotificationSent(false);
            }

            boolean result = this.save(alert);
            if (result) {
                log.info("告警记录保存成功: alertId={}, serverId={}, type={}", 
                    alert.getMonitorSysGenServerAlertHistoryId(),
                    alert.getMonitorSysGenServerAlertHistoryServerId(),
                    alert.getMonitorSysGenServerAlertHistoryType());
            }
            return ReturnResult.of(result);

        } catch (Exception e) {
            log.error("保存告警记录失败", e);
            return ReturnResult.error("保存告警记录失败: " + e.getMessage());
        }
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Integer> batchUpdateAlertStatus(List<Long> alertIds, AlertStatus status) {
        try {
            if (CollectionUtils.isEmpty(alertIds)) {
                return ReturnResult.success(0);
            }

            LambdaUpdateWrapper<MonitorSysGenServerAlertHistory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryId, alertIds)
                        .set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryStatus, status);

            if (status == AlertStatus.RECOVERED) {
                updateWrapper.set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryRecoveryTime, LocalDateTime.now());
            }

            boolean result = this.update(updateWrapper);
            int updatedCount = result ? alertIds.size() : 0;

            if (result && status == AlertStatus.RECOVERED) {
                // 批量计算持续时间
                List<MonitorSysGenServerAlertHistory> alerts = this.listByIds(alertIds);
                for (MonitorSysGenServerAlertHistory alert : alerts) {
                    alert.calculateDuration();
                }
                this.updateBatchById(alerts);
            }

            log.info("批量更新告警状态成功: count={}, status={}", updatedCount, status);
            return ReturnResult.success(updatedCount);

        } catch (Exception e) {
            log.error("批量更新告警状态失败: alertIds={}, status={}", alertIds, status, e);
            return ReturnResult.error("批量更新告警状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> recoverAlert(Integer alertId) {
        return updateAlertStatus(alertId.longValue(), AlertStatus.RECOVERED);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> silenceAlert(Integer alertId, Integer silenceMinutes) {
        try {
            LocalDateTime silenceUntil = LocalDateTime.now().plusMinutes(silenceMinutes);
            
            LambdaUpdateWrapper<MonitorSysGenServerAlertHistory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryId, alertId)
                        .set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryStatus, AlertStatus.SILENCED)
                        .set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistorySilenceUntil, silenceUntil);

            boolean result = this.update(updateWrapper);
            if (result) {
                log.info("告警静默设置成功: alertId={}, silenceMinutes={}", alertId, silenceMinutes);
            }
            return ReturnResult.of(result);

        } catch (Exception e) {
            log.error("设置告警静默失败: alertId={}", alertId, e);
            return ReturnResult.error("设置告警静默失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerAlertHistory>> getActiveAlertsByServerId(Integer serverId) {
        try {
            LambdaQueryWrapper<MonitorSysGenServerAlertHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryServerId, serverId)
                        .in(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryStatus,
                            AlertStatus.TRIGGERED, AlertStatus.ACTIVE)
                        .orderByDesc(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryTriggerTime);

            List<MonitorSysGenServerAlertHistory> alerts = this.list(queryWrapper);
            return ReturnResult.success(alerts);

        } catch (Exception e) {
            log.error("查询活跃告警失败: serverId={}", serverId, e);
            return ReturnResult.error("查询活跃告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerAlertHistory> getLatestAlertByFingerprint(String fingerprint) {
        try {
            LambdaQueryWrapper<MonitorSysGenServerAlertHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryFingerprint, fingerprint)
                        .orderByDesc(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryTriggerTime)
                        .last("LIMIT 1");

            MonitorSysGenServerAlertHistory alert = this.getOne(queryWrapper);
            return ReturnResult.success(alert);

        } catch (Exception e) {
            log.error("查询最新告警失败: fingerprint={}", fingerprint, e);
            return ReturnResult.error("查询最新告警失败: " + e.getMessage());
        }
    }


    @Override
    public ReturnResult<Long> countAlerts(Integer serverId, AlertType alertType, AlertLevel alertLevel,
                                         AlertStatus status, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            LambdaQueryWrapper<MonitorSysGenServerAlertHistory> queryWrapper = new LambdaQueryWrapper<>();

            if (serverId != null) {
                queryWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryServerId, serverId);
            }
            if (alertType != null) {
                queryWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryType, alertType);
            }
            if (alertLevel != null) {
                queryWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryLevel, alertLevel);
            }
            if (status != null) {
                queryWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryStatus, status);
            }
            if (startTime != null) {
                queryWrapper.ge(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryTriggerTime, startTime);
            }
            if (endTime != null) {
                queryWrapper.le(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryTriggerTime, endTime);
            }



            long count = this.count(queryWrapper);
            return ReturnResult.success(count);

        } catch (Exception e) {
            log.error("统计告警数量失败", e);
            return ReturnResult.error("统计告警数量失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getAlertStatistics(Integer serverId, Integer days) {
        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            LocalDateTime endTime = LocalDateTime.now();

            Map<String, Object> statistics = new HashMap<>();

            // 总告警数
            ReturnResult<Long> totalResult = countAlerts(serverId, null, null, null, startTime, endTime);
            statistics.put("totalAlerts", totalResult.isOk() ? totalResult.getData() : 0L);

            // 按级别统计
            Map<String, Long> levelStats = new HashMap<>();
            for (AlertLevel level : AlertLevel.values()) {
                ReturnResult<Long> levelResult = countAlerts(serverId, null, level, null, startTime, endTime);
                levelStats.put(level.getCode(), levelResult.isOk() ? levelResult.getData() : 0L);
            }
            statistics.put("levelStatistics", levelStats);

            // 按类型统计
            Map<String, Long> typeStats = new HashMap<>();
            for (AlertType type : AlertType.values()) {
                ReturnResult<Long> typeResult = countAlerts(serverId, type, null, null, startTime, endTime);
                typeStats.put(type.getCode(), typeResult.isOk() ? typeResult.getData() : 0L);
            }
            statistics.put("typeStatistics", typeStats);

            // 按状态统计
            Map<String, Long> statusStats = new HashMap<>();
            for (AlertStatus status : AlertStatus.values()) {
                ReturnResult<Long> statusResult = countAlerts(serverId, null, null, status, startTime, endTime);
                statusStats.put(status.getCode(), statusResult.isOk() ? statusResult.getData() : 0L);
            }
            statistics.put("statusStatistics", statusStats);

            return ReturnResult.success(statistics);

        } catch (Exception e) {
            log.error("获取告警统计信息失败: serverId={}, days={}", serverId, days, e);
            return ReturnResult.error("获取告警统计信息失败: " + e.getMessage());
        }
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Integer> cleanupExpiredAlerts(Integer expireDays) {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(expireDays);
            int cleanedCount = baseMapper.cleanupExpiredAlerts(expireTime);

            log.info("清理过期告警记录完成: cleanedCount={}, expireDays={}", cleanedCount, expireDays);
            return ReturnResult.success(cleanedCount);

        } catch (Exception e) {
            log.error("清理过期告警记录失败: expireDays={}", expireDays, e);
            return ReturnResult.error("清理过期告警记录失败: " + e.getMessage());
        }
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateNotificationStatus(Integer alertId, Boolean sent, String method, String address) {
        try {
            LambdaUpdateWrapper<MonitorSysGenServerAlertHistory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryId, alertId)
                        .set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryNotificationSent, sent);

            if (StringUtils.isNotBlank(method)) {
                updateWrapper.set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryNotificationMethod, method);
            }
            if (StringUtils.isNotBlank(address)) {
                updateWrapper.set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryNotificationAddress, address);
            }

            boolean result = this.update(updateWrapper);
            if (result) {
                log.debug("更新告警通知状态成功: alertId={}, sent={}", alertId, sent);
            }
            return ReturnResult.of(result);

        } catch (Exception e) {
            log.error("更新告警通知状态失败: alertId={}", alertId, e);
            return ReturnResult.error("更新告警通知状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerAlertHistory>> getUnsentNotificationAlerts(Integer limit) {
        try {
            List<MonitorSysGenServerAlertHistory> alerts = baseMapper.getUnsentNotificationAlerts(limit);
            return ReturnResult.success(alerts);

        } catch (Exception e) {
            log.error("获取未发送通知的告警失败: limit={}", limit, e);
            return ReturnResult.error("获取未发送通知的告警失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Integer> autoRecoverExpiredAlerts(Integer expireMinutes) {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusMinutes(expireMinutes);
            int recoveredCount = baseMapper.autoRecoverExpiredAlerts(expireTime);

            log.info("自动恢复过期活跃告警完成: recoveredCount={}, expireMinutes={}", recoveredCount, expireMinutes);
            return ReturnResult.success(recoveredCount);

        } catch (Exception e) {
            log.error("自动恢复过期活跃告警失败: expireMinutes={}", expireMinutes, e);
            return ReturnResult.error("自动恢复过期活跃告警失败: " + e.getMessage());
        }
    }



    @Override
    public ReturnResult<MonitorSysGenServerAlertHistory> getAlertDetail(Long alertId) {
        try {
            MonitorSysGenServerAlertHistory alert = this.getById(alertId);
            if (alert == null) {
                return ReturnResult.error("告警记录不存在");
            }
            return ReturnResult.success(alert);
        } catch (Exception e) {
            log.error("获取告警详情失败: alertId={}", alertId, e);
            return ReturnResult.error("获取告警详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateAlertStatus(Long alertId, AlertStatus status) {
        try {
            LambdaUpdateWrapper<MonitorSysGenServerAlertHistory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryId, alertId)
                        .set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryStatus, status);

            if (status == AlertStatus.RECOVERED) {
                updateWrapper.set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryRecoveryTime, LocalDateTime.now());
            }

            boolean result = this.update(updateWrapper);
            if (result) {
                log.info("更新告警状态成功: alertId={}, status={}", alertId, status);
            }
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("更新告警状态失败: alertId={}, status={}", alertId, status, e);
            return ReturnResult.error("更新告警状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> ignoreAlert(Long alertId, String reason) {
        try {
            LambdaUpdateWrapper<MonitorSysGenServerAlertHistory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryId, alertId)
                        .set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryStatus, AlertStatus.IGNORED)
                        .set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryRemark, reason);

            boolean result = this.update(updateWrapper);
            if (result) {
                log.info("忽略告警成功: alertId={}, reason={}", alertId, reason);
            }
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("忽略告警失败: alertId={}, reason={}", alertId, reason, e);
            return ReturnResult.error("忽略告警失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchRecoverAlerts(List<Integer> alertIds) {
        try {
            if (CollectionUtils.isEmpty(alertIds)) {
                return ReturnResult.success(true);
            }

            LambdaUpdateWrapper<MonitorSysGenServerAlertHistory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryId, alertIds)
                        .set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryStatus, AlertStatus.RECOVERED)
                        .set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryRecoveryTime, LocalDateTime.now());

            boolean result = this.update(updateWrapper);
            log.info("批量恢复告警成功: alertIds={}, result={}", alertIds, result);
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("批量恢复告警失败: alertIds={}", alertIds, e);
            return ReturnResult.error("批量恢复告警失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> unsilenceAlert(Integer alertId) {
        try {
            LambdaUpdateWrapper<MonitorSysGenServerAlertHistory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryId, alertId)
                        .set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryStatus, AlertStatus.ACTIVE)
                        .set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistorySilenceUntil, null);

            boolean result = this.update(updateWrapper);
            log.info("取消告警静默成功: alertId={}, result={}", alertId, result);
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("取消告警静默失败: alertId={}", alertId, e);
            return ReturnResult.error("取消告警静默失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchIgnoreAlerts(List<Integer> alertIds) {
        try {
            if (CollectionUtils.isEmpty(alertIds)) {
                return ReturnResult.success(true);
            }

            LambdaUpdateWrapper<MonitorSysGenServerAlertHistory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryId, alertIds)
                        .set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryStatus, AlertStatus.IGNORED);

            boolean result = this.update(updateWrapper);
            log.info("批量忽略告警成功: alertIds={}, result={}", alertIds, result);
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("批量忽略告警失败: alertIds={}", alertIds, e);
            return ReturnResult.error("批量忽略告警失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> ignoreAlert(Integer alertId) {
        try {
            LambdaUpdateWrapper<MonitorSysGenServerAlertHistory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryId, alertId)
                        .set(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryStatus, AlertStatus.IGNORED);

            boolean result = this.update(updateWrapper);
            log.info("忽略告警成功: alertId={}, result={}", alertId, result);
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("忽略告警失败: alertId={}", alertId, e);
            return ReturnResult.error("忽略告警失败: " + e.getMessage());
        }
    }



    @Override
    public ReturnResult<List<MonitorSysGenServerAlertHistory>> getAlertsByFingerprintInTimeRange(
            String fingerprint, Integer minutes) {
        try {
            LocalDateTime startTime = LocalDateTime.now().minusMinutes(minutes);

            LambdaQueryWrapper<MonitorSysGenServerAlertHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryFingerprint, fingerprint)
                        .ge(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryTriggerTime, startTime)
                        .orderByDesc(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryTriggerTime);

            List<MonitorSysGenServerAlertHistory> alerts = this.list(queryWrapper);
            return ReturnResult.success(alerts);
        } catch (Exception e) {
            log.error("根据指纹获取时间范围内告警失败: fingerprint={}, minutes={}", fingerprint, minutes, e);
            return ReturnResult.error("根据指纹获取时间范围内告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnPageResult<MonitorSysGenServerAlertHistory> pageAlerts(Query<MonitorSysGenServerAlertHistory> query) {
        try {
            // 由于Query类型问题，这里使用简单的分页实现
            Page<MonitorSysGenServerAlertHistory> page = new Page<>(1, 10); // 默认分页参数
            LambdaQueryWrapper<MonitorSysGenServerAlertHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryTriggerTime);

            IPage<MonitorSysGenServerAlertHistory> result = this.page(page, queryWrapper);
            return PageResultUtils.ok(result);
        } catch (Exception e) {
            log.error("分页查询告警历史失败", e);
            return ReturnPageResultUtils.error("分页查询告警历史失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerAlertHistory>> getAlertsByServerId(Integer serverId, Integer limit) {
        try {
            LambdaQueryWrapper<MonitorSysGenServerAlertHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryServerId, serverId)
                        .orderByDesc(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryTriggerTime);

            if (limit != null && limit > 0) {
                queryWrapper.last("LIMIT " + limit);
            }

            List<MonitorSysGenServerAlertHistory> alerts = this.list(queryWrapper);
            return ReturnResult.success(alerts);
        } catch (Exception e) {
            log.error("根据服务器ID获取告警失败: serverId={}, limit={}", serverId, limit, e);
            return ReturnResult.error("根据服务器ID获取告警失败: " + e.getMessage());
        }
    }




}
