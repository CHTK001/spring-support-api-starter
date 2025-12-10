package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.service.server.ServerTaskManagementService;
import com.chua.starter.monitor.task.manager.ServerTaskManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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


}
