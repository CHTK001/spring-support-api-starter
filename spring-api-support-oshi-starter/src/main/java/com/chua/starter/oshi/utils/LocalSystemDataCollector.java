package com.chua.starter.oshi.utils;

import com.chua.starter.oshi.pojo.DiskPartitionInfo;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.VirtualMemory;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地系统数据收集器，基于 OSHI 直接读取主机指标。
 *
 * @author CH
 * @since 2024/12/24
 */
@Slf4j
public class LocalSystemDataCollector {

    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardware;
    private final OperatingSystem operatingSystem;

    public LocalSystemDataCollector() {
        this.systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.operatingSystem = systemInfo.getOperatingSystem();
    }

    /**
     * 收集完整系统数据。
     *
     * @return 指标数据
     */
    public Map<String, Object> collectSystemData() {
        Map<String, Object> data = new HashMap<>();
        try {
            collectBasicSystemInfo(data);
            collectCpuInfo(data);
            collectMemoryInfo(data);
            collectDiskInfo(data);
            collectNetworkInfo(data);
            collectProcessInfo(data);
            collectJvmInfo(data);
            log.debug("系统数据收集完成，共收集 {} 项数据", data.size());
        } catch (Exception e) {
            log.error("收集系统数据失败", e);
        }
        return data;
    }

    private void collectBasicSystemInfo(Map<String, Object> data) {
        try {
            data.put("osName", operatingSystem.toString());
            data.put("osVersion", operatingSystem.getVersionInfo().toString());
            data.put("osArch", System.getProperty("os.arch"));
            data.put("osManufacturer", operatingSystem.getManufacturer());
            data.put("osFamily", operatingSystem.getFamily());
            data.put("systemUptime", operatingSystem.getSystemUptime());
            data.put("systemBootTime", operatingSystem.getSystemBootTime());

            ComputerSystem computerSystem = hardware.getComputerSystem();
            data.put("computerManufacturer", computerSystem.getManufacturer());
            data.put("computerModel", computerSystem.getModel());
            data.put("computerSerialNumber", computerSystem.getSerialNumber());
        } catch (Exception e) {
            log.warn("收集基本系统信息失败", e);
        }
    }

    private void collectCpuInfo(Map<String, Object> data) {
        try {
            CentralProcessor processor = hardware.getProcessor();
            data.put("cpuName", processor.getProcessorIdentifier().getName());
            data.put("cpuFamily", processor.getProcessorIdentifier().getFamily());
            data.put("cpuModel", processor.getProcessorIdentifier().getModel());
            data.put("cpuVendor", processor.getProcessorIdentifier().getVendor());
            data.put("cpuLogicalProcessors", processor.getLogicalProcessorCount());
            data.put("cpuPhysicalProcessors", processor.getPhysicalProcessorCount());
            data.put("cpuMaxFreq", processor.getMaxFreq());

            double systemCpuLoad = processor.getSystemCpuLoad(1000);
            data.put("cpuUsage", systemCpuLoad * 100);

            double[] loadAverage = processor.getSystemLoadAverage(3);
            data.put("systemLoad1min", loadAverage[0]);
            data.put("systemLoad5min", loadAverage[1]);
            data.put("systemLoad15min", loadAverage[2]);
        } catch (Exception e) {
            log.warn("收集CPU信息失败", e);
        }
    }

    private void collectMemoryInfo(Map<String, Object> data) {
        try {
            GlobalMemory memory = hardware.getMemory();
            data.put("totalMemory", memory.getTotal());
            data.put("availableMemory", memory.getAvailable());
            data.put("usedMemory", memory.getTotal() - memory.getAvailable());
            data.put("memoryUsagePercent", (double) (memory.getTotal() - memory.getAvailable()) / memory.getTotal() * 100);

            VirtualMemory virtualMemory = memory.getVirtualMemory();
            data.put("swapTotal", virtualMemory.getSwapTotal());
            data.put("swapUsed", virtualMemory.getSwapUsed());
            data.put("swapUsagePercent", virtualMemory.getSwapTotal() > 0
                    ? (double) virtualMemory.getSwapUsed() / virtualMemory.getSwapTotal() * 100
                    : 0);
        } catch (Exception e) {
            log.warn("收集内存信息失败", e);
        }
    }

    private void collectDiskInfo(Map<String, Object> data) {
        try {
            List<HWDiskStore> diskStores = hardware.getDiskStores();
            List<Map<String, Object>> disks = new ArrayList<>();
            for (HWDiskStore disk : diskStores) {
                Map<String, Object> diskInfo = new HashMap<>();
                diskInfo.put("name", disk.getName());
                diskInfo.put("model", disk.getModel());
                diskInfo.put("serial", disk.getSerial());
                diskInfo.put("size", disk.getSize());
                diskInfo.put("reads", disk.getReads());
                diskInfo.put("writes", disk.getWrites());
                diskInfo.put("readBytes", disk.getReadBytes());
                diskInfo.put("writeBytes", disk.getWriteBytes());
                disks.add(diskInfo);
            }
            data.put("disks", disks);

            List<oshi.software.os.OSFileStore> fileStores = operatingSystem.getFileSystem().getFileStores();
            List<Map<String, Object>> fileSystems = new ArrayList<>();
            List<DiskPartitionInfo> diskPartitions = new ArrayList<>();
            for (oshi.software.os.OSFileStore fileStore : fileStores) {
                Map<String, Object> fileStoreInfo = new HashMap<>();
                fileStoreInfo.put("name", fileStore.getName());
                fileStoreInfo.put("type", fileStore.getType());
                fileStoreInfo.put("mount", fileStore.getMount());
                fileStoreInfo.put("totalSpace", fileStore.getTotalSpace());
                fileStoreInfo.put("usableSpace", fileStore.getUsableSpace());
                fileStoreInfo.put("freeSpace", fileStore.getFreeSpace());

                double usagePercent = fileStore.getTotalSpace() > 0
                        ? (double) (fileStore.getTotalSpace() - fileStore.getFreeSpace()) / fileStore.getTotalSpace() * 100
                        : 0;
                fileStoreInfo.put("usagePercent", usagePercent);
                fileSystems.add(fileStoreInfo);

                diskPartitions.add(DiskPartitionInfo.builder()
                        .name(fileStore.getName())
                        .mount(fileStore.getMount())
                        .type(fileStore.getType())
                        .totalSpace(fileStore.getTotalSpace())
                        .freeSpace(fileStore.getFreeSpace())
                        .usedSpace(fileStore.getTotalSpace() - fileStore.getFreeSpace())
                        .usagePercent(usagePercent)
                        .isPrimary("/".equals(fileStore.getMount()) || "C:\\".equals(fileStore.getMount()))
                        .deviceName(fileStore.getName())
                        .build());
            }

            data.put("fileSystems", fileSystems);
            data.put("diskPartitions", diskPartitions);
        } catch (Exception e) {
            log.warn("收集磁盘信息失败", e);
        }
    }

    private void collectNetworkInfo(Map<String, Object> data) {
        try {
            List<NetworkIF> networkIfs = hardware.getNetworkIFs();
            List<Map<String, Object>> networks = new ArrayList<>();
            for (NetworkIF networkIf : networkIfs) {
                if (networkIf.getBytesRecv() <= 0 && networkIf.getBytesSent() <= 0) {
                    continue;
                }
                Map<String, Object> networkInfo = new HashMap<>();
                networkInfo.put("name", networkIf.getName());
                networkInfo.put("displayName", networkIf.getDisplayName());
                networkInfo.put("macAddress", networkIf.getMacaddr());
                networkInfo.put("ipv4", Arrays.toString(networkIf.getIPv4addr()));
                networkInfo.put("ipv6", Arrays.toString(networkIf.getIPv6addr()));
                networkInfo.put("bytesReceived", networkIf.getBytesRecv());
                networkInfo.put("bytesSent", networkIf.getBytesSent());
                networkInfo.put("packetsReceived", networkIf.getPacketsRecv());
                networkInfo.put("packetsSent", networkIf.getPacketsSent());
                networkInfo.put("speed", networkIf.getSpeed());
                networkInfo.put("mtu", networkIf.getMTU());
                networks.add(networkInfo);
            }
            data.put("networks", networks);
        } catch (Exception e) {
            log.warn("收集网络信息失败", e);
        }
    }

    private void collectProcessInfo(Map<String, Object> data) {
        try {
            data.put("processCount", operatingSystem.getProcessCount());
            data.put("threadCount", operatingSystem.getThreadCount());

            List<OSProcess> processes = operatingSystem.getProcesses(
                    OperatingSystem.ProcessFiltering.ALL_PROCESSES,
                    OperatingSystem.ProcessSorting.CPU_DESC,
                    10
            );

            List<Map<String, Object>> topProcesses = new ArrayList<>();
            for (OSProcess process : processes) {
                Map<String, Object> processInfo = new HashMap<>();
                processInfo.put("pid", process.getProcessID());
                processInfo.put("name", process.getName());
                processInfo.put("cpuPercent", process.getProcessCpuLoadCumulative() * 100);
                processInfo.put("memoryPercent", (double) process.getResidentSetSize() / hardware.getMemory().getTotal() * 100);
                processInfo.put("memorySize", process.getResidentSetSize());
                topProcesses.add(processInfo);
            }
            data.put("topProcesses", topProcesses);
        } catch (Exception e) {
            log.warn("收集进程信息失败", e);
        }
    }

    private void collectJvmInfo(Map<String, Object> data) {
        try {
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

            data.put("jvmName", runtimeBean.getVmName());
            data.put("jvmVersion", runtimeBean.getVmVersion());
            data.put("jvmVendor", runtimeBean.getVmVendor());
            data.put("jvmUptime", runtimeBean.getUptime());
            data.put("jvmStartTime", runtimeBean.getStartTime());
            data.put("jvmHeapUsed", memoryBean.getHeapMemoryUsage().getUsed());
            data.put("jvmHeapMax", memoryBean.getHeapMemoryUsage().getMax());
            data.put("jvmHeapCommitted", memoryBean.getHeapMemoryUsage().getCommitted());
            data.put("jvmNonHeapUsed", memoryBean.getNonHeapMemoryUsage().getUsed());
            data.put("jvmNonHeapMax", memoryBean.getNonHeapMemoryUsage().getMax());
        } catch (Exception e) {
            log.warn("收集JVM信息失败", e);
        }
    }

    /**
     * 检查系统健康状态。
     *
     * @return 是否健康
     */
    public boolean checkSystemHealth() {
        try {
            CentralProcessor processor = hardware.getProcessor();
            double cpuUsage = processor.getSystemCpuLoad(1000) * 100;
            if (cpuUsage > 90) {
                log.warn("CPU使用率过高: {}%", cpuUsage);
                return false;
            }

            GlobalMemory memory = hardware.getMemory();
            double memoryUsage = (double) (memory.getTotal() - memory.getAvailable()) / memory.getTotal() * 100;
            if (memoryUsage > 90) {
                log.warn("内存使用率过高: {}%", memoryUsage);
                return false;
            }

            List<oshi.software.os.OSFileStore> fileStores = operatingSystem.getFileSystem().getFileStores();
            for (oshi.software.os.OSFileStore fileStore : fileStores) {
                if (fileStore.getTotalSpace() <= 0) {
                    continue;
                }
                double diskUsage = (double) (fileStore.getTotalSpace() - fileStore.getFreeSpace()) / fileStore.getTotalSpace() * 100;
                if (diskUsage > 95) {
                    log.warn("磁盘 {} 使用率过高: {}%", fileStore.getMount(), diskUsage);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.error("检查系统健康状态失败", e);
            return false;
        }
    }
}
