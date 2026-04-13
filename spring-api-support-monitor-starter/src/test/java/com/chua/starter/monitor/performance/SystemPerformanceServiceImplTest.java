package com.chua.starter.monitor.performance;

import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.monitor.utils.LocalSystemDataCollector;
import com.chua.starter.service.SysSettingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemPerformanceServiceImplTest {

    @Mock
    private SysSettingService sysSettingService;

    @Mock
    private LocalSystemDataCollector localSystemDataCollector;

    @Mock
    private SocketSessionTemplate socketSessionTemplate;

    @Test
    void shouldFilterDisabledMetricSections() {
        when(sysSettingService.list(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(Set.class)))
                .thenReturn(List.of(
                        setting(SystemPerformanceSettings.MONITOR_OPEN, "true"),
                        setting(SystemPerformanceSettings.SOCKET_PUSH_OPEN, "false"),
                        setting(SystemPerformanceSettings.REFRESH_INTERVAL_SECONDS, "5"),
                        setting("PerformanceMonitorSystemOpen", "true"),
                        setting("PerformanceMonitorCpuOpen", "false"),
                        setting("PerformanceMonitorMemoryOpen", "true"),
                        setting("PerformanceMonitorDiskOpen", "true"),
                        setting("PerformanceMonitorNetworkOpen", "false"),
                        setting("PerformanceMonitorJvmOpen", "false"),
                        setting("PerformanceMonitorProcessOpen", "false")
                ));

        when(localSystemDataCollector.collectSystemData()).thenReturn(mockMetrics());

        SystemPerformanceService service = new SystemPerformanceServiceImpl(sysSettingService, localSystemDataCollector, emptyProvider());
        SystemPerformanceSnapshot snapshot = service.getSnapshot(false);

        assertThat(snapshot.getEnabled()).isTrue();
        assertThat(snapshot.getSections()).containsKeys("overview", "system", "memory", "disk");
        assertThat(snapshot.getSections()).doesNotContainKeys("cpu", "network", "jvm", "process");
        assertThat(snapshot.getHealthStatus()).isEqualTo("warning");
        assertThat(snapshot.getActiveIndicators()).containsExactly("system", "memory", "disk");
    }

    @Test
    void shouldBroadcastSnapshotWhenSocketPushEnabled() {
        when(sysSettingService.list(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(Set.class)))
                .thenReturn(List.of(
                        setting(SystemPerformanceSettings.MONITOR_OPEN, "true"),
                        setting(SystemPerformanceSettings.SOCKET_PUSH_OPEN, "true"),
                        setting(SystemPerformanceSettings.REFRESH_INTERVAL_SECONDS, "5"),
                        setting("PerformanceMonitorSystemOpen", "true"),
                        setting("PerformanceMonitorCpuOpen", "true"),
                        setting("PerformanceMonitorMemoryOpen", "true"),
                        setting("PerformanceMonitorDiskOpen", "true"),
                        setting("PerformanceMonitorNetworkOpen", "true"),
                        setting("PerformanceMonitorJvmOpen", "true"),
                        setting("PerformanceMonitorProcessOpen", "true")
                ));

        when(localSystemDataCollector.collectSystemData()).thenReturn(mockMetrics());

        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        beanFactory.addBean("socketSessionTemplate", socketSessionTemplate);
        ObjectProvider<SocketSessionTemplate> provider = beanFactory.getBeanProvider(SocketSessionTemplate.class);

        SystemPerformanceService service = new SystemPerformanceServiceImpl(sysSettingService, localSystemDataCollector, provider);
        SystemPerformanceSnapshot snapshot = service.getSnapshot(true);

        assertThat(snapshot.getSections()).containsKey("overview");
        verify(socketSessionTemplate).send(eq(SystemPerformanceSettings.SOCKET_TOPIC), anyString());
    }

    private ObjectProvider<SocketSessionTemplate> emptyProvider() {
        return new StaticListableBeanFactory().getBeanProvider(SocketSessionTemplate.class);
    }

    private SysSetting setting(String name, String value) {
        SysSetting setting = new SysSetting();
        setting.setSysSettingGroup(SystemPerformanceSettings.SETTING_GROUP);
        setting.setSysSettingName(name);
        setting.setSysSettingValue(value);
        setting.setSysSettingStatus(1);
        return setting;
    }

    private Map<String, Object> mockMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("cpuUsage", 78.5D);
        metrics.put("memoryUsagePercent", 63.2D);
        metrics.put("systemUptime", 3600L);
        metrics.put("systemBootTime", 1711600000L);
        metrics.put("processCount", 128);
        metrics.put("threadCount", 412);
        metrics.put("osName", "Windows 11");
        metrics.put("osVersion", "23H2");
        metrics.put("osArch", "amd64");
        metrics.put("osManufacturer", "Microsoft");
        metrics.put("osFamily", "Windows");
        metrics.put("computerManufacturer", "LENOVO");
        metrics.put("computerModel", "ThinkCentre");
        metrics.put("computerSerialNumber", "ABC123");
        metrics.put("totalMemory", 16L * 1024 * 1024 * 1024);
        metrics.put("usedMemory", 10L * 1024 * 1024 * 1024);
        metrics.put("availableMemory", 6L * 1024 * 1024 * 1024);
        metrics.put("swapTotal", 8L * 1024 * 1024 * 1024);
        metrics.put("swapUsed", 1L * 1024 * 1024 * 1024);
        metrics.put("swapUsagePercent", 12.5D);
        metrics.put("cpuName", "Intel(R) Core(TM)");
        metrics.put("cpuFamily", "6");
        metrics.put("cpuModel", "165");
        metrics.put("cpuVendor", "Intel");
        metrics.put("cpuLogicalProcessors", 16);
        metrics.put("cpuPhysicalProcessors", 8);
        metrics.put("cpuMaxFreq", 4200000000L);
        metrics.put("systemLoad1min", 0.8D);
        metrics.put("systemLoad5min", 0.6D);
        metrics.put("systemLoad15min", 0.4D);
        metrics.put("jvmName", "OpenJDK 64-Bit Server VM");
        metrics.put("jvmVersion", "25");
        metrics.put("jvmVendor", "OpenJDK");
        metrics.put("jvmUptime", 60000L);
        metrics.put("jvmStartTime", 1711600100L);
        metrics.put("jvmHeapUsed", 256L * 1024 * 1024);
        metrics.put("jvmHeapMax", 1024L * 1024 * 1024);
        metrics.put("jvmHeapCommitted", 512L * 1024 * 1024);
        metrics.put("jvmNonHeapUsed", 64L * 1024 * 1024);
        metrics.put("jvmNonHeapMax", 256L * 1024 * 1024);
        metrics.put("fileSystems", List.of(
                Map.of(
                        "name", "C:",
                        "mount", "C:\\",
                        "type", "NTFS",
                        "totalSpace", 512L * 1024 * 1024 * 1024,
                        "freeSpace", 128L * 1024 * 1024 * 1024,
                        "usableSpace", 128L * 1024 * 1024 * 1024,
                        "usagePercent", 75.0D
                )
        ));
        metrics.put("diskPartitions", List.of(
                Map.of(
                        "name", "C:",
                        "mount", "C:\\",
                        "usagePercent", 75.0D
                )
        ));
        metrics.put("disks", List.of(
                Map.of(
                        "name", "Disk0",
                        "model", "NVMe",
                        "size", 512L * 1024 * 1024 * 1024
                )
        ));
        metrics.put("networks", List.of(
                Map.of(
                        "name", "eth0",
                        "displayName", "Ethernet",
                        "bytesReceived", 1024L,
                        "bytesSent", 2048L
                )
        ));
        metrics.put("topProcesses", List.of(
                Map.of(
                        "pid", 1001,
                        "name", "java",
                        "cpuPercent", 12.3D,
                        "memoryPercent", 8.1D,
                        "memorySize", 512L * 1024 * 1024
                )
        ));
        return metrics;
    }
}
