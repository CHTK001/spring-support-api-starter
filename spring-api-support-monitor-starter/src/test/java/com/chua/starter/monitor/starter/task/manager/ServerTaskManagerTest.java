package com.chua.starter.monitor.starter.task.manager;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerMetricsService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.starter.service.PrometheusDataCollectorService;
import com.chua.starter.monitor.starter.service.ServerMetricsWebSocketService;
import com.chua.starter.monitor.starter.service.UnifiedAlertService;
import com.chua.starter.monitor.starter.utils.LocalSystemDataCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ServerTaskManager 测试类
 *
 * @author CH
 * @since 2024/12/28
 */
@ExtendWith(MockitoExtension.class)
class ServerTaskManagerTest {

    @Mock
    private MonitorSysGenServerService serverService;

    @Mock
    private MonitorSysGenServerSettingService serverSettingService;

    @Mock
    private ServerMetricsWebSocketService serverMetricsWebSocketService;

    @Mock
    private UnifiedAlertService unifiedAlertService;

    @Mock
    private MonitorSysGenServerMetricsService metricsService;

    @Mock
    private PrometheusDataCollectorService prometheusDataCollectorService;

    @Mock
    private LocalSystemDataCollector localSystemDataCollector;

    private ServerTaskManager serverTaskManager;

    @BeforeEach
    void setUp() {
        serverTaskManager = new ServerTaskManager(
            serverService,
            serverSettingService,
            serverMetricsWebSocketService,
            unifiedAlertService,
            metricsService,
            prometheusDataCollectorService,
            localSystemDataCollector
        );
    }

    @Test
    void testCreateServerTasks() {
        // 准备测试数据
        Integer serverId = 1;
        MonitorSysGenServer server = new MonitorSysGenServer();
        server.setMonitorSysGenServerId(serverId);
        server.setMonitorSysGenServerName("测试服务器");
        server.setMonitorSysGenServerStatus(1);

        MonitorSysGenServerSetting setting = new MonitorSysGenServerSetting();
        setting.setMonitorSysGenServerId(serverId);
        setting.setMonitorSysGenServerSettingMonitorEnabled(1);
        setting.setMonitorSysGenServerSettingAlertEnabled(1);
        setting.setMonitorSysGenServerSettingDataReportMethod("LOCAL");
        setting.setMonitorSysGenServerSettingDataCollectionFrequency(30);

        // 模拟服务调用
        when(serverService.getById(serverId)).thenReturn(server);
        when(serverSettingService.getOrCreateSetting(serverId))
            .thenReturn(ReturnResult.ok(setting));

        // 执行测试
        serverTaskManager.createServerTasks(serverId);

        // 验证结果
        Map<String, Object> status = serverTaskManager.getServerTaskStatus(serverId);
        assertTrue((Boolean) status.get("exists"));
        assertEquals(serverId, status.get("serverId"));
    }

    @Test
    void testConfigCacheManagement() {
        // 准备测试数据
        Integer serverId = 1;
        MonitorSysGenServer server = new MonitorSysGenServer();
        server.setMonitorSysGenServerId(serverId);
        server.setMonitorSysGenServerName("测试服务器");
        server.setMonitorSysGenServerStatus(1);

        MonitorSysGenServerSetting setting = new MonitorSysGenServerSetting();
        setting.setMonitorSysGenServerId(serverId);
        setting.setMonitorSysGenServerSettingMonitorEnabled(1);
        setting.setMonitorSysGenServerSettingDataCollectionFrequency(30);

        // 模拟服务调用
        when(serverService.getById(serverId)).thenReturn(server);
        when(serverSettingService.getOrCreateSetting(serverId))
            .thenReturn(ReturnResult.ok(setting));

        // 创建任务
        serverTaskManager.createServerTasks(serverId);

        // 测试配置缓存状态
        Map<String, Object> cacheStatus = serverTaskManager.getConfigCacheStatus();
        assertEquals(1, cacheStatus.get("totalCachedConfigs"));
        assertEquals(60, cacheStatus.get("refreshInterval"));

        // 测试手动刷新配置
        serverTaskManager.refreshServerConfigManually(serverId);

        // 验证服务调用
        verify(serverSettingService, atLeast(2)).getOrCreateSetting(serverId);
    }

    @Test
    void testTaskManagerStatus() {
        // 测试管理器状态
        Map<String, Object> status = serverTaskManager.getManagerStatus();
        
        assertNotNull(status);
        assertTrue(status.containsKey("running"));
        assertTrue(status.containsKey("totalServerTasks"));
        assertTrue(status.containsKey("totalGlobalTasks"));
        assertTrue(status.containsKey("configCacheSize"));
        assertTrue(status.containsKey("configRefreshInterval"));
        assertTrue(status.containsKey("activeServerTasks"));
        assertTrue(status.containsKey("activeGlobalTasks"));
    }

    @Test
    void testPauseAndResumeServerTasks() {
        // 准备测试数据
        Integer serverId = 1;
        MonitorSysGenServer server = new MonitorSysGenServer();
        server.setMonitorSysGenServerId(serverId);
        server.setMonitorSysGenServerName("测试服务器");
        server.setMonitorSysGenServerStatus(1);

        MonitorSysGenServerSetting setting = new MonitorSysGenServerSetting();
        setting.setMonitorSysGenServerId(serverId);
        setting.setMonitorSysGenServerSettingMonitorEnabled(1);

        // 模拟服务调用
        when(serverService.getById(serverId)).thenReturn(server);
        when(serverSettingService.getOrCreateSetting(serverId))
            .thenReturn(ReturnResult.ok(setting));

        // 创建任务
        serverTaskManager.createServerTasks(serverId);

        // 测试暂停任务
        serverTaskManager.pauseServerTasks(serverId);
        
        // 测试恢复任务
        serverTaskManager.resumeServerTasks(serverId);

        // 验证任务存在
        Map<String, Object> status = serverTaskManager.getServerTaskStatus(serverId);
        assertTrue((Boolean) status.get("exists"));
    }

    @Test
    void testRemoveServerTasks() {
        // 准备测试数据
        Integer serverId = 1;
        MonitorSysGenServer server = new MonitorSysGenServer();
        server.setMonitorSysGenServerId(serverId);
        server.setMonitorSysGenServerName("测试服务器");
        server.setMonitorSysGenServerStatus(1);

        MonitorSysGenServerSetting setting = new MonitorSysGenServerSetting();
        setting.setMonitorSysGenServerId(serverId);
        setting.setMonitorSysGenServerSettingMonitorEnabled(1);

        // 模拟服务调用
        when(serverService.getById(serverId)).thenReturn(server);
        when(serverSettingService.getOrCreateSetting(serverId))
            .thenReturn(ReturnResult.ok(setting));

        // 创建任务
        serverTaskManager.createServerTasks(serverId);

        // 验证任务存在
        Map<String, Object> statusBefore = serverTaskManager.getServerTaskStatus(serverId);
        assertTrue((Boolean) statusBefore.get("exists"));

        // 移除任务
        serverTaskManager.removeServerTasks(serverId);

        // 验证任务已移除
        Map<String, Object> statusAfter = serverTaskManager.getServerTaskStatus(serverId);
        assertFalse((Boolean) statusAfter.get("exists"));
    }

    @Test
    void testForceReloadAllConfigs() {
        // 准备测试数据
        when(serverService.list()).thenReturn(Arrays.asList());

        // 测试强制重新加载配置
        serverTaskManager.forceReloadAllConfigs();

        // 验证服务调用
        verify(serverService, atLeastOnce()).list();
    }
}
