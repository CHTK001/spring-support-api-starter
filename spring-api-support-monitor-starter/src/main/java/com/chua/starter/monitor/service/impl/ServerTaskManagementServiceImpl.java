package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.service.ServerTaskManagementService;
import com.chua.starter.monitor.starter.task.manager.ServerTaskManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务器任务管理服务实现
 *
 * @author CH
 * @since 2024/12/28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServerTaskManagementServiceImpl implements ServerTaskManagementService {

    private final ServerTaskManager serverTaskManager;

    @Override
    public ReturnResult<Boolean> createServerTasks(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            serverTaskManager.createServerTasks(serverId);
            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("创建服务器任务失败: serverId={}", serverId, e);
            return ReturnResult.error("创建服务器任务失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> updateServerTasks(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            serverTaskManager.updateServerTasks(serverId);
            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("更新服务器任务失败: serverId={}", serverId, e);
            return ReturnResult.error("更新服务器任务失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> removeServerTasks(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            serverTaskManager.removeServerTasks(serverId);
            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("移除服务器任务失败: serverId={}", serverId, e);
            return ReturnResult.error("移除服务器任务失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getTaskStatistics() {
        try {
            Map<String, Object> statistics = serverTaskManager.getTaskStatistics();
            return ReturnResult.success(statistics);

        } catch (Exception e) {
            log.error("获取任务统计信息失败", e);
            return ReturnResult.error("获取任务统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> reinitializeAllTasks() {
        try {
            // 重新初始化任务管理器
            serverTaskManager.destroy();
            serverTaskManager.init();
            
            return ReturnResult.success(true);

        } catch (Exception e) {
            log.error("重新初始化所有任务失败", e);
            return ReturnResult.error("重新初始化所有任务失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> checkTaskHealth() {
        try {
            Map<String, Object> healthInfo = new HashMap<>();
            
            // 获取任务统计信息
            Map<String, Object> statistics = serverTaskManager.getTaskStatistics();
            healthInfo.putAll(statistics);
            
            // 添加健康状态判断
            int totalServerTasks = (Integer) statistics.get("totalServerTasks");
            long activeServerTasks = (Long) statistics.get("activeServerTasks");
            long activeGlobalTasks = (Long) statistics.get("activeGlobalTasks");
            
            boolean isHealthy = activeGlobalTasks > 0; // 至少要有全局任务在运行
            healthInfo.put("isHealthy", isHealthy);
            healthInfo.put("healthStatus", isHealthy ? "HEALTHY" : "UNHEALTHY");
            
            // 计算任务活跃率
            double taskActiveRate = totalServerTasks > 0 ? 
                (double) activeServerTasks / (totalServerTasks * 3) * 100 : 0; // 每个服务器最多3个任务
            healthInfo.put("taskActiveRate", Math.round(taskActiveRate * 100.0) / 100.0);
            
            healthInfo.put("checkTime", System.currentTimeMillis());
            
            return ReturnResult.success(healthInfo);

        } catch (Exception e) {
            log.error("检查任务健康状态失败", e);
            return ReturnResult.error("检查任务健康状态失败: " + e.getMessage());
        }
    }
}
