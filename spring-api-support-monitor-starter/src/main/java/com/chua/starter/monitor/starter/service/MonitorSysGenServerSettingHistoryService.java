package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSettingHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务器配置变更历史Service接口
 *
 * @author CH
 * @since 2024/12/27
 */
public interface MonitorSysGenServerSettingHistoryService extends IService<MonitorSysGenServerSettingHistory> {

    /**
     * 根据服务器ID查询历史记录
     *
     * @param serverId 服务器ID
     * @param limit    限制数量
     * @return 历史记录列表
     */
    ReturnResult<List<MonitorSysGenServerSettingHistory>> getHistoryByServerId(Integer serverId, Integer limit);

    /**
     * 根据服务器ID和时间范围查询历史记录
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 历史记录列表
     */
    ReturnResult<List<MonitorSysGenServerSettingHistory>> getHistoryByServerIdAndTimeRange(
            Integer serverId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据变更类型查询历史记录
     *
     * @param changeType 变更类型
     * @param limit      限制数量
     * @return 历史记录列表
     */
    ReturnResult<List<MonitorSysGenServerSettingHistory>> getHistoryByChangeType(String changeType, Integer limit);

    /**
     * 获取历史记录统计信息
     *
     * @param serverId 服务器ID（可选）
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getHistoryStatistics(Integer serverId);

    /**
     * 记录配置变更历史
     *
     * @param serverId        服务器ID
     * @param settingId       配置ID
     * @param changeType      变更类型
     * @param description     变更描述
     * @param user            变更用户
     * @param settingSnapshot 配置快照
     * @param changedFields   变更字段
     * @return 操作结果
     */
    ReturnResult<Boolean> recordHistory(Integer serverId, Long settingId,
                                        MonitorSysGenServerSettingHistory.ChangeType changeType,
                                        String description, String user,
                                        String settingSnapshot, String changedFields);

    /**
     * 恢复历史配置
     *
     * @param historyId 历史记录ID
     * @param user      操作用户
     * @return 操作结果
     */
    ReturnResult<MonitorSysGenServerSetting> restoreFromHistory(Long historyId, String user);

    /**
     * 比较两个历史记录的差异
     *
     * @param historyId1 历史记录ID1
     * @param historyId2 历史记录ID2
     * @return 差异信息
     */
    ReturnResult<Map<String, Object>> compareHistory(Long historyId1, Long historyId2);

    /**
     * 获取配置变更详情
     *
     * @param historyId 历史记录ID
     * @return 变更详情
     */
    ReturnResult<Map<String, Object>> getHistoryDetail(Long historyId);

    /**
     * 删除指定时间之前的历史记录
     *
     * @param beforeTime 时间点
     * @return 删除数量
     */
    ReturnResult<Integer> deleteHistoryBeforeTime(LocalDateTime beforeTime);

    /**
     * 根据服务器ID删除历史记录
     *
     * @param serverId 服务器ID
     * @return 删除数量
     */
    ReturnResult<Integer> deleteHistoryByServerId(Integer serverId);

    /**
     * 导出历史记录
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param format    导出格式 (json/csv/excel)
     * @return 导出数据
     */
    ReturnResult<String> exportHistory(Integer serverId, LocalDateTime startTime, LocalDateTime endTime, String format);

    /**
     * 获取配置变更趋势
     *
     * @param serverId 服务器ID
     * @param days     天数
     * @return 变更趋势数据
     */
    ReturnResult<List<Map<String, Object>>> getChangeTrend(Integer serverId, Integer days);

    /**
     * 获取最近的配置变更
     *
     * @param serverId 服务器ID
     * @param limit    限制数量
     * @return 最近变更列表
     */
    ReturnResult<List<Map<String, Object>>> getRecentChanges(Integer serverId, Integer limit);

    /**
     * 检查配置是否有变更
     *
     * @param serverId    服务器ID
     * @param newSetting  新配置
     * @return 是否有变更
     */
    ReturnResult<Boolean> hasChanges(Integer serverId, MonitorSysGenServerSetting newSetting);

    /**
     * 获取配置变更摘要
     *
     * @param serverId 服务器ID
     * @param days     天数
     * @return 变更摘要
     */
    ReturnResult<Map<String, Object>> getChangeSummary(Integer serverId, Integer days);
}
