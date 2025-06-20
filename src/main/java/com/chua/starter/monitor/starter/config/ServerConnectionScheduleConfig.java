package com.chua.starter.monitor.starter.config;

import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 服务器连接状态定时检查配置
 *
 * @author CH
 * @since 2024/12/20
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "monitor.server.connection.schedule.enabled", havingValue = "true", matchIfMissing = true)
public class ServerConnectionScheduleConfig {

    private final MonitorSysGenServerService monitorSysGenServerService;

    /**
     * 定时检查服务器连接状态
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300000毫秒
    public void checkServerConnectionStatus() {
        try {
            log.debug("开始定时检查服务器连接状态");
            monitorSysGenServerService.scheduleConnectionCheck();
            log.debug("定时检查服务器连接状态完成");
        } catch (Exception e) {
            log.error("定时检查服务器连接状态异常", e);
        }
    }

    /**
     * 定时清理过期的连接错误信息
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredConnectionErrors() {
        try {
            log.info("开始清理过期的连接错误信息");
            // 这里可以添加清理逻辑，比如清理7天前的错误信息
            log.info("清理过期的连接错误信息完成");
        } catch (Exception e) {
            log.error("清理过期连接错误信息异常", e);
        }
    }
}
