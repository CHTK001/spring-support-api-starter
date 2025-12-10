package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.converter.Converter;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.entity.MonitorSysGenServerSettingHistory;
import com.chua.starter.monitor.mapper.MonitorSysGenServerSettingHistoryMapper;
import com.chua.starter.monitor.mapper.MonitorSysGenServerSettingMapper;
import com.chua.starter.monitor.service.server.MonitorSysGenServerSettingHistoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 服务器配置变更历史Service实现类
 *
 * @author CH
 * @since 2024/12/27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenServerSettingHistoryServiceImpl 
        extends ServiceImpl<MonitorSysGenServerSettingHistoryMapper, MonitorSysGenServerSettingHistory>
        implements MonitorSysGenServerSettingHistoryService {

    private final MonitorSysGenServerSettingMapper settingMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ReturnResult<List<MonitorSysGenServerSettingHistory>> getHistoryByServerId(Integer serverId, Integer limit) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            List<MonitorSysGenServerSettingHistory> historyList = baseMapper.selectByServerId(serverId, limit);
            return ReturnResult.ok(historyList);
        } catch (Exception e) {
            log.error("查询服务器配置历史失败", e);
            return ReturnResult.error("查询配置历史失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerSettingHistory>> getHistoryByServerIdAndTimeRange(
            Integer serverId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            List<MonitorSysGenServerSettingHistory> historyList = 
                    baseMapper.selectByServerIdAndTimeRange(serverId, startTime, endTime);
            return ReturnResult.ok(historyList);
        } catch (Exception e) {
            log.error("查询服务器配置历史失败", e);
            return ReturnResult.error("查询配置历史失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerSettingHistory>> getHistoryByChangeType(String changeType, Integer limit) {
        try {
            if (StringUtils.isBlank(changeType)) {
                return ReturnResult.error("变更类型不能为空");
            }

            List<MonitorSysGenServerSettingHistory> historyList = 
                    baseMapper.selectByChangeType(changeType, limit);
            return ReturnResult.ok(historyList);
        } catch (Exception e) {
            log.error("查询配置变更历史失败", e);
            return ReturnResult.error("查询配置变更历史失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getHistoryStatistics(Integer serverId) {
        try {
            Map<String, Object> statistics = baseMapper.getHistoryStatistics(serverId);
            return ReturnResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取历史统计信息失败", e);
            return ReturnResult.error("获取历史统计信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> recordHistory(Integer serverId, Long settingId,
                                               MonitorSysGenServerSettingHistory.ChangeType changeType,
                                               String description, String user,
                                               String settingSnapshot, String changedFields) {
        try {
            MonitorSysGenServerSettingHistory history = MonitorSysGenServerSettingHistory.createHistory(
                    serverId, settingId, changeType, description, user);
            
            if (StringUtils.isNotBlank(settingSnapshot)) {
                history.setSettingSnapshot(settingSnapshot);
            }
            
            if (StringUtils.isNotBlank(changedFields)) {
                history.setChangedFields(changedFields);
            }

            boolean result = this.save(history);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("记录配置变更历史失败", e);
            return ReturnResult.error("记录配置变更历史失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<MonitorSysGenServerSetting> restoreFromHistory(Long historyId, String user) {
        try {
            if (historyId == null) {
                return ReturnResult.error("历史记录ID不能为空");
            }

            // 获取历史记录
            MonitorSysGenServerSettingHistory history = this.getById(historyId);
            if (history == null) {
                return ReturnResult.error("历史记录不存在");
            }

            if (StringUtils.isBlank(history.getSettingSnapshot())) {
                return ReturnResult.error("历史记录中没有配置快照，无法恢复");
            }

            // 解析配置快照
            MonitorSysGenServerSetting historySetting = objectMapper.readValue(
                    history.getSettingSnapshot(), MonitorSysGenServerSetting.class);

            // 获取当前配置
            MonitorSysGenServerSetting currentSetting = settingMapper.selectByServerId(history.getMonitorSysGenServerId());
            if (currentSetting == null) {
                return ReturnResult.error("当前配置不存在");
            }

            // 更新配置
            historySetting.setMonitorSysGenServerSettingId(currentSetting.getMonitorSysGenServerSettingId());

            settingMapper.updateById(historySetting);

            // 记录恢复操作
            this.recordHistory(
                    history.getMonitorSysGenServerId(),
                    Converter.convertIfNecessary(historySetting.getMonitorSysGenServerSettingId(), Long.class),
                    MonitorSysGenServerSettingHistory.ChangeType.RESTORE,
                    "从历史记录恢复配置 (历史ID: " + historyId + ")",
                    user,
                    objectMapper.writeValueAsString(historySetting),
                    null
            );

            return ReturnResult.ok(historySetting);
        } catch (Exception e) {
            log.error("恢复历史配置失败", e);
            return ReturnResult.error("恢复历史配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> compareHistory(Long historyId1, Long historyId2) {
        try {
            if (historyId1 == null || historyId2 == null) {
                return ReturnResult.error("历史记录ID不能为空");
            }

            MonitorSysGenServerSettingHistory history1 = this.getById(historyId1);
            MonitorSysGenServerSettingHistory history2 = this.getById(historyId2);

            if (history1 == null || history2 == null) {
                return ReturnResult.error("历史记录不存在");
            }

            Map<String, Object> comparison = new HashMap<>();
            comparison.put("history1", history1);
            comparison.put("history2", history2);

            // 比较配置快照
            if (StringUtils.isNotBlank(history1.getSettingSnapshot()) && 
                StringUtils.isNotBlank(history2.getSettingSnapshot())) {
                
                JsonNode config1 = objectMapper.readTree(history1.getSettingSnapshot());
                JsonNode config2 = objectMapper.readTree(history2.getSettingSnapshot());
                
                Map<String, Object> differences = compareJsonNodes(config1, config2);
                comparison.put("differences", differences);
            }

            return ReturnResult.ok(comparison);
        } catch (Exception e) {
            log.error("比较历史记录失败", e);
            return ReturnResult.error("比较历史记录失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getHistoryDetail(Long historyId) {
        try {
            if (historyId == null) {
                return ReturnResult.error("历史记录ID不能为空");
            }

            MonitorSysGenServerSettingHistory history = this.getById(historyId);
            if (history == null) {
                return ReturnResult.error("历史记录不存在");
            }

            Map<String, Object> detail = new HashMap<>();
            detail.put("history", history);

            // 解析配置快照
            if (StringUtils.isNotBlank(history.getSettingSnapshot())) {
                try {
                    JsonNode snapshot = objectMapper.readTree(history.getSettingSnapshot());
                    detail.put("configSnapshot", snapshot);
                } catch (Exception e) {
                    log.warn("解析配置快照失败: {}", e.getMessage());
                }
            }

            // 解析变更字段
            if (StringUtils.isNotBlank(history.getChangedFields())) {
                try {
                    JsonNode changedFields = objectMapper.readTree(history.getChangedFields());
                    detail.put("changedFields", changedFields);
                } catch (Exception e) {
                    log.warn("解析变更字段失败: {}", e.getMessage());
                }
            }

            return ReturnResult.ok(detail);
        } catch (Exception e) {
            log.error("获取历史详情失败", e);
            return ReturnResult.error("获取历史详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Integer> deleteHistoryBeforeTime(LocalDateTime beforeTime) {
        try {
            if (beforeTime == null) {
                return ReturnResult.error("时间参数不能为空");
            }

            int deletedCount = baseMapper.deleteBeforeTime(beforeTime);
            return ReturnResult.ok(deletedCount);
        } catch (Exception e) {
            log.error("删除历史记录失败", e);
            return ReturnResult.error("删除历史记录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Integer> deleteHistoryByServerId(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            int deletedCount = baseMapper.deleteByServerId(serverId);
            return ReturnResult.ok(deletedCount);
        } catch (Exception e) {
            log.error("删除服务器历史记录失败", e);
            return ReturnResult.error("删除服务器历史记录失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> exportHistory(Integer serverId, LocalDateTime startTime, 
                                              LocalDateTime endTime, String format) {
        try {
            List<MonitorSysGenServerSettingHistory> historyList = 
                    baseMapper.selectByServerIdAndTimeRange(serverId, startTime, endTime);

            String exportData;
            switch (format.toLowerCase()) {
                case "json":
                    exportData = objectMapper.writeValueAsString(historyList);
                    break;
                case "csv":
                    exportData = convertToCSV(historyList);
                    break;
                default:
                    return ReturnResult.error("不支持的导出格式: " + format);
            }

            return ReturnResult.ok(exportData);
        } catch (Exception e) {
            log.error("导出历史记录失败", e);
            return ReturnResult.error("导出历史记录失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getChangeTrend(Integer serverId, Integer days) {
        // 实现变更趋势查询逻辑
        return ReturnResult.ok(new ArrayList<>());
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getRecentChanges(Integer serverId, Integer limit) {
        try {
            List<MonitorSysGenServerSettingHistory> historyList = baseMapper.selectByServerId(serverId, limit);
            List<Map<String, Object>> recentChanges = new ArrayList<>();

            for (MonitorSysGenServerSettingHistory history : historyList) {
                Map<String, Object> change = new HashMap<>();
                change.put("id", history.getMonitorSysGenServerSettingHistoryId());
                change.put("changeType", history.getChangeType());
                change.put("description", history.getChangeDescription());
                change.put("user", history.getChangeUser());
                change.put("time", history.getChangeTime());
                recentChanges.add(change);
            }

            return ReturnResult.ok(recentChanges);
        } catch (Exception e) {
            log.error("获取最近变更失败", e);
            return ReturnResult.error("获取最近变更失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> hasChanges(Integer serverId, MonitorSysGenServerSetting newSetting) {
        try {
            MonitorSysGenServerSetting currentSetting = settingMapper.selectByServerId(serverId);
            if (currentSetting == null) {
                return ReturnResult.ok(true); // 没有当前配置，认为有变更
            }

            // 比较配置是否有变更
            String currentJson = objectMapper.writeValueAsString(currentSetting);
            String newJson = objectMapper.writeValueAsString(newSetting);
            
            boolean hasChanges = !currentJson.equals(newJson);
            return ReturnResult.ok(hasChanges);
        } catch (Exception e) {
            log.error("检查配置变更失败", e);
            return ReturnResult.error("检查配置变更失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getChangeSummary(Integer serverId, Integer days) {
        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            List<MonitorSysGenServerSettingHistory> historyList = 
                    baseMapper.selectByServerIdAndTimeRange(serverId, startTime, LocalDateTime.now());

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalChanges", historyList.size());
            summary.put("period", days + " 天");
            
            // 按变更类型统计
            Map<String, Long> changeTypeCount = new HashMap<>();
            for (MonitorSysGenServerSettingHistory history : historyList) {
                changeTypeCount.merge(history.getChangeType(), 1L, Long::sum);
            }
            summary.put("changeTypeCount", changeTypeCount);

            return ReturnResult.ok(summary);
        } catch (Exception e) {
            log.error("获取变更摘要失败", e);
            return ReturnResult.error("获取变更摘要失败: " + e.getMessage());
        }
    }

    /**
     * 比较两个JSON节点的差异
     */
    private Map<String, Object> compareJsonNodes(JsonNode node1, JsonNode node2) {
        Map<String, Object> differences = new HashMap<>();
        // 实现JSON节点比较逻辑
        return differences;
    }

    /**
     * 转换为CSV格式
     */
    private String convertToCSV(List<MonitorSysGenServerSettingHistory> historyList) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,服务器ID,变更类型,变更描述,变更用户,变更时间\n");
        
        for (MonitorSysGenServerSettingHistory history : historyList) {
            csv.append(history.getMonitorSysGenServerSettingHistoryId()).append(",")
               .append(history.getMonitorSysGenServerId()).append(",")
               .append(history.getChangeType()).append(",")
               .append(history.getChangeDescription()).append(",")
               .append(history.getChangeUser()).append(",")
               .append(history.getChangeTime()).append("\n");
        }
        
        return csv.toString();
    }
}
