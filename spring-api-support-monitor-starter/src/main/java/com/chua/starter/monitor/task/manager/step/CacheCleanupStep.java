package com.chua.starter.monitor.task.manager.step;

import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.MonitorSysGenServerService;
import com.chua.starter.monitor.task.manager.TaskStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 缓存清理步骤
 * <p>
 * 负责清理服务器的临时文件和缓存数据
 *
 * @author CH
 * @since 2025/09/22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheCleanupStep implements TaskStep {

    private final MonitorSysGenServerService serverService;

    /**
     * 执行缓存清理任务
     *
     * @param server  服务器信息
     * @param setting 服务器配置
     */
    @Override
    public void run(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            // 使用配置的临时文件保留时间进行清理
            Integer tempFileRetentionHours = setting.getMonitorSysGenServerSettingTempFileRetentionHours();
            if (tempFileRetentionHours != null && tempFileRetentionHours > 0) {
                log.debug("使用配置的临时文件保留时间进行清理: {} 小时", tempFileRetentionHours);
                serverService.cleanupTempFiles(tempFileRetentionHours);
            } else {
                serverService.cleanupTempFiles();
            }
            
            serverService.cleanupCache();
        } catch (Exception e) {
            log.error("缓存清理失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }
}