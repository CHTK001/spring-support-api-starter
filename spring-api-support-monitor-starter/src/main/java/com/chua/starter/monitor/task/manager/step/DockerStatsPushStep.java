package com.chua.starter.monitor.task.manager.step;

import com.chua.docker.support.client.DockerProtocolClient;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.entity.SystemSoftContainer;
import com.chua.starter.monitor.entity.SystemSoftContainerStats;
import com.chua.starter.monitor.factory.DockerProtocolClientFactory;
import com.chua.starter.monitor.mapper.SystemSoftContainerMapper;
import com.chua.starter.monitor.service.SystemSoftContainerStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.github.dockerjava.api.model.Statistics;

import java.util.List;
import java.util.Map;

/**
 * Docker状态推送步骤
 * <p>
 * 负责收集和推送Docker容器的实时状态信息
 *
 * @author CH
 * @since 2025/09/22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DockerStatsPushStep {

    private final DockerProtocolClientFactory dockerProtocolClientFactory;
    private final SystemSoftContainerMapper systemSoftContainerMapper;
    private final SystemSoftContainerStatsService systemSoftContainerStatsService;

    /**
     * 执行Docker状态推送任务
     *
     * @param server  服务器信息
     * @param setting 服务器配置
     */
    public void run(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            // 检查是否启用了Docker监控
            if (setting.getMonitorSysGenServerSettingDockerEnabled() == null ||
                    setting.getMonitorSysGenServerSettingDockerEnabled() != 1) {
                return;
            }

            // 检查是否启用了Docker状态推送
            if (setting.getMonitorSysGenServerSettingDockerMonitorEnabled() == null ||
                    setting.getMonitorSysGenServerSettingDockerMonitorEnabled() != 1) {
                return;
            }

            // 获取Docker客户端
            var dockerClient = dockerProtocolClientFactory.createClient(server.getMonitorSysGenServerId());
            if (dockerClient == null) {
                log.warn("无法创建Docker客户端: serverId={}", server.getMonitorSysGenServerId());
                return;
            }

            // 检查客户端是否连接
            try {
                boolean isConnected = dockerClient.isConnected();
                if (!isConnected) {
                    log.warn("Docker客户端未连接: serverId={}", server.getMonitorSysGenServerId());
                    return;
                }
            } catch (Exception e) {
                log.warn("检查Docker客户端连接状态失败: serverId={}, error={}", server.getMonitorSysGenServerId(), e.getMessage());
                return;
            }

            // 获取该服务器下的所有容器
            List<SystemSoftContainer> containers = systemSoftContainerMapper.selectByServerId(server.getMonitorSysGenServerId());

            if (containers.isEmpty()) {
                return;
            }

            // 收集并推送每个容器的状态
            for (SystemSoftContainer container : containers) {
                try {
                    collectAndPushContainerStats(dockerClient, container, server);
                } catch (Exception e) {
                    log.warn("收集容器状态失败: containerId={}, containerName={}",
                            container.getSystemSoftContainerId(), container.getSystemSoftContainerName(), e);
                }
            }
        } catch (Exception e) {
            log.error("执行Docker状态推送任务失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }

    /**
     * 收集并推送单个容器的状态信息
     *
     * @param dockerClient Docker客户端
     * @param container    容器信息
     * @param server       服务器信息
     */
    private void collectAndPushContainerStats(DockerProtocolClient dockerClient, SystemSoftContainer container, MonitorSysGenServer server) {
        try {
            // 检查容器是否有Docker ID
            if (container.getSystemSoftContainerDockerId() == null ||
                    container.getSystemSoftContainerDockerId().isEmpty()) {
                return;
            }

            // 获取容器统计信息
            Statistics stats = dockerClient.getContainerStats(container.getSystemSoftContainerDockerId());
            if (stats == null) {
                return;
            }

            // 转换为SystemSoftContainerStats实体
            SystemSoftContainerStats containerStats = new SystemSoftContainerStats();
            containerStats.setSystemSoftContainerStatsContainerId(container.getSystemSoftContainerDockerId());
            containerStats.setSystemSoftContainerStatsContainerName(container.getSystemSoftContainerName());

            // 设置容器和镜像关联ID
            containerStats.setSystemSoftContainerId(container.getSystemSoftContainerId());
            containerStats.setSystemSoftImageId(container.getSystemSoftImageId());

            // 从统计信息中提取CPU和内存数据
            Map<String, Object> statsMap = stats.getRawValues();
            if (statsMap != null) {
                // CPU使用率
                extractCpuStats(statsMap, containerStats);

                // 内存使用情况
                extractMemoryStats(statsMap, containerStats);

                // 网络IO
                extractNetworkStats(statsMap, containerStats);

                // 磁盘IO
                extractDiskStats(statsMap, containerStats);
            }

            // 获取容器详细信息
            com.github.dockerjava.api.command.InspectContainerResponse containerInfo = dockerClient.inspectContainerById(
                    container.getSystemSoftContainerDockerId()
            );

            if (containerInfo != null) {
                // 设置基本信息
                containerStats.setSystemSoftContainerStatsImage(
                        containerInfo.getConfig() != null ? containerInfo.getConfig().getImage() : "");
                containerStats.setSystemSoftContainerStatsCommand(
                        containerInfo.getConfig() != null ?
                                String.join(" ", containerInfo.getConfig().getCmd() != null ?
                                        containerInfo.getConfig().getCmd() : new String[0]) : "");
                containerStats.setSystemSoftContainerStatsCreated(
                        containerInfo.getCreated() != null ? containerInfo.getCreated().toString() : "");

                // 设置状态信息
                if (containerInfo.getState() != null) {
                    containerStats.setSystemSoftContainerStatsStatus(containerInfo.getState().getStatus());
                    containerStats.setSystemSoftContainerStatsStartedAt(
                            containerInfo.getState().getStartedAt() != null ?
                                    containerInfo.getState().getStartedAt().toString() : "");
                }

                // 设置网络信息
                if (containerInfo.getNetworkSettings() != null &&
                        containerInfo.getNetworkSettings().getPorts() != null) {
                    containerStats.setSystemSoftContainerStatsPorts(
                            containerInfo.getNetworkSettings().getPorts().toString());
                }

                // 设置标签信息
                if (containerInfo.getConfig() != null && containerInfo.getConfig().getLabels() != null) {
                    containerStats.setSystemSoftContainerStatsLabels(
                            containerInfo.getConfig().getLabels().toString());
                }
            }

            // 设置记录时间
            containerStats.setSystemSoftContainerStatsRecordTime(java.time.LocalDateTime.now());

            // 保存到数据库
            systemSoftContainerStatsService.save(containerStats);

            log.debug("收集并保存容器状态: containerId={}, status={}",
                    container.getSystemSoftContainerDockerId(),
                    containerStats.getSystemSoftContainerStatsStatus());

        } catch (Exception e) {
            log.warn("收集容器状态失败: containerId={}, containerName={}",
                    container.getSystemSoftContainerDockerId(), container.getSystemSoftContainerName(), e);
        }
    }

    /**
     * 提取CPU统计信息
     */
    private void extractCpuStats(Map<String, Object> statsMap, SystemSoftContainerStats containerStats) {
        try {
            Object cpuStats = statsMap.get("cpu_stats");
            Object preCpuStats = statsMap.get("precpu_stats");

            if (cpuStats instanceof Map && preCpuStats instanceof Map) {
                Map<String, Object> cpuMap = (Map<String, Object>) cpuStats;
                Map<String, Object> preCpuMap = (Map<String, Object>) preCpuStats;

                Object cpuUsage = cpuMap.get("cpu_usage");
                Object preCpuUsage = preCpuMap.get("cpu_usage");
                Object systemCpuUsage = cpuMap.get("system_cpu_usage");
                Object preSystemCpuUsage = preCpuMap.get("system_cpu_usage");

                if (cpuUsage instanceof Map && preCpuUsage instanceof Map &&
                        systemCpuUsage instanceof Number && preSystemCpuUsage instanceof Number) {

                    Map<String, Object> cpuUsageMap = (Map<String, Object>) cpuUsage;
                    Map<String, Object> preCpuUsageMap = (Map<String, Object>) preCpuUsage;

                    Object totalUsage = cpuUsageMap.get("total_usage");
                    Object preTotalUsage = preCpuUsageMap.get("total_usage");
                    Object percpuUsage = cpuUsageMap.get("percpu_usage");

                    if (totalUsage instanceof Number && preTotalUsage instanceof Number) {
                        long cpuDelta = ((Number) totalUsage).longValue() - ((Number) preTotalUsage).longValue();
                        long systemDelta = ((Number) systemCpuUsage).longValue() - ((Number) preSystemCpuUsage).longValue();

                        if (systemDelta > 0 && cpuDelta > 0) {
                            int cpuCount = 0;
                            if (percpuUsage instanceof List) {
                                cpuCount = ((List<?>) percpuUsage).size();
                            }

                            double cpuPercent = (double) cpuDelta / systemDelta * cpuCount * 100.0;
                            containerStats.setSystemSoftContainerStatsCpuPercent(cpuPercent);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取CPU统计信息失败", e);
        }
    }

    /**
     * 提取内存统计信息
     */
    private void extractMemoryStats(Map<String, Object> statsMap, SystemSoftContainerStats containerStats) {
        try {
            Object memoryStats = statsMap.get("memory_stats");
            if (memoryStats instanceof Map) {
                Map<String, Object> memoryMap = (Map<String, Object>) memoryStats;
                Object usage = memoryMap.get("usage");
                Object limit = memoryMap.get("limit");

                if (usage instanceof Number) {
                    long memUsage = ((Number) usage).longValue();
                    containerStats.setSystemSoftContainerStatsMemoryUsage(memUsage);
                }

                if (limit instanceof Number) {
                    long memLimit = ((Number) limit).longValue();
                    containerStats.setSystemSoftContainerStatsMemoryLimit(memLimit);

                    // 计算内存使用率
                    if (usage instanceof Number) {
                        long memUsage = ((Number) usage).longValue();
                        if (memLimit > 0) {
                            double memPercent = (double) memUsage / memLimit * 100.0;
                            containerStats.setSystemSoftContainerStatsMemoryPercent(memPercent);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取内存统计信息失败", e);
        }
    }

    /**
     * 提取网络统计信息
     */
    private void extractNetworkStats(Map<String, Object> statsMap, SystemSoftContainerStats containerStats) {
        try {
            Object networkStats = statsMap.get("networks");
            if (networkStats instanceof Map) {
                Map<String, Object> networkMap = (Map<String, Object>) networkStats;
                long rxBytes = 0, txBytes = 0;

                for (Object value : networkMap.values()) {
                    if (value instanceof Map) {
                        Map<String, Object> ifaceMap = (Map<String, Object>) value;
                        Object rx = ifaceMap.get("rx_bytes");
                        Object tx = ifaceMap.get("tx_bytes");

                        if (rx instanceof Number) {
                            rxBytes += ((Number) rx).longValue();
                        }
                        if (tx instanceof Number) {
                            txBytes += ((Number) tx).longValue();
                        }
                    }
                }

                containerStats.setSystemSoftContainerStatsNetworkRxBytes(rxBytes);
                containerStats.setSystemSoftContainerStatsNetworkTxBytes(txBytes);
            }
        } catch (Exception e) {
            log.warn("提取网络统计信息失败", e);
        }
    }

    /**
     * 提取磁盘统计信息
     */
    private void extractDiskStats(Map<String, Object> statsMap, SystemSoftContainerStats containerStats) {
        try {
            Object blkioStats = statsMap.get("blkio_stats");
            if (blkioStats instanceof Map) {
                Map<String, Object> blkioMap = (Map<String, Object>) blkioStats;
                Object ioServiceBytesRecursive = blkioMap.get("io_service_bytes_recursive");

                if (ioServiceBytesRecursive instanceof List) {
                    List<?> ioList = (List<?>) ioServiceBytesRecursive;
                    long readBytes = 0, writeBytes = 0;

                    for (Object item : ioList) {
                        if (item instanceof Map) {
                            Map<String, Object> ioItem = (Map<String, Object>) item;
                            Object op = ioItem.get("op");
                            Object value = ioItem.get("value");

                            if (op instanceof String && value instanceof Number) {
                                String operation = (String) op;
                                long bytes = ((Number) value).longValue();

                                if ("read".equalsIgnoreCase(operation)) {
                                    readBytes += bytes;
                                } else if ("write".equalsIgnoreCase(operation)) {
                                    writeBytes += bytes;
                                }
                            }
                        }
                    }

                    containerStats.setSystemSoftContainerStatsDiskRead(readBytes);
                    containerStats.setSystemSoftContainerStatsDiskWrite(writeBytes);
                }
            }
        } catch (Exception e) {
            log.warn("提取磁盘统计信息失败", e);
        }
    }
}