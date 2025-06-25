package com.chua.starter.monitor.starter.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OSProcess;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.*;

/**
 * 本地系统数据收集器
 * 使用OSHI库直接获取本地系统的详细信息
 *
 * @author CH
 * @since 2024/12/24
 */
@Slf4j
@Component
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
     * 收集完整的系统数据
     *
     * @return 系统数据Map
     */
    public Map<String, Object> collectSystemData() {
        Map<String, Object> data = new HashMap<>();
        
        try {
            // 收集基本系统信息
            collectBasicSystemInfo(data);
            
            // 收集CPU信息
            collectCpuInfo(data);
            
            // 收集内存信息
            collectMemoryInfo(data);
            
            // 收集磁盘信息
            collectDiskInfo(data);
            
            // 收集网络信息
            collectNetworkInfo(data);
            
            // 收集进程信息
            collectProcessInfo(data);
            
            // 收集JVM信息
            collectJvmInfo(data);
            
            log.debug("系统数据收集完成，共收集 {} 项数据", data.size());
            
        } catch (Exception e) {
            log.error("收集系统数据失败", e);
        }
        
        return data;
    }

    /**
     * 收集基本系统信息
     */
    private void collectBasicSystemInfo(Map<String, Object> data) {
        try {
            data.put("osName", operatingSystem.toString());
            data.put("osVersion", operatingSystem.getVersionInfo().toString());
            data.put("osArch", System.getProperty("os.arch"));
            data.put("osManufacturer", operatingSystem.getManufacturer());
            data.put("osFamily", operatingSystem.getFamily());
            data.put("systemUptime", operatingSystem.getSystemUptime());
            data.put("systemBootTime", operatingSystem.getSystemBootTime());
            
            // 计算机信息
            ComputerSystem computerSystem = hardware.getComputerSystem();
            data.put("computerManufacturer", computerSystem.getManufacturer());
            data.put("computerModel", computerSystem.getModel());
            data.put("computerSerialNumber", computerSystem.getSerialNumber());
            
        } catch (Exception e) {
            log.warn("收集基本系统信息失败", e);
        }
    }

    /**
     * 收集CPU信息
     */
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

            // CPU使用率 - 使用便捷方法获取系统CPU使用率
            double systemCpuLoad = processor.getSystemCpuLoad(1000); // 等待1秒获取准确的CPU使用率
            data.put("cpuUsage", systemCpuLoad * 100);

            // 系统负载
            double[] loadAverage = processor.getSystemLoadAverage(3);
            data.put("systemLoad1min", loadAverage[0]);
            data.put("systemLoad5min", loadAverage[1]);
            data.put("systemLoad15min", loadAverage[2]);

        } catch (Exception e) {
            log.warn("收集CPU信息失败", e);
        }
    }

    /**
     * 收集内存信息
     */
    private void collectMemoryInfo(Map<String, Object> data) {
        try {
            GlobalMemory memory = hardware.getMemory();
            
            data.put("totalMemory", memory.getTotal());
            data.put("availableMemory", memory.getAvailable());
            data.put("usedMemory", memory.getTotal() - memory.getAvailable());
            data.put("memoryUsagePercent", (double)(memory.getTotal() - memory.getAvailable()) / memory.getTotal() * 100);
            
            // 虚拟内存
            VirtualMemory virtualMemory = memory.getVirtualMemory();
            data.put("swapTotal", virtualMemory.getSwapTotal());
            data.put("swapUsed", virtualMemory.getSwapUsed());
            data.put("swapUsagePercent", virtualMemory.getSwapTotal() > 0 ? 
                (double)virtualMemory.getSwapUsed() / virtualMemory.getSwapTotal() * 100 : 0);
            
        } catch (Exception e) {
            log.warn("收集内存信息失败", e);
        }
    }

    /**
     * 收集磁盘信息
     */
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
            
            // 文件系统信息
            List<oshi.software.os.OSFileStore> fileStores = operatingSystem.getFileSystem().getFileStores();
            List<Map<String, Object>> fileSystems = new ArrayList<>();
            
            for (oshi.software.os.OSFileStore fs : fileStores) {
                Map<String, Object> fsInfo = new HashMap<>();
                fsInfo.put("name", fs.getName());
                fsInfo.put("type", fs.getType());
                fsInfo.put("mount", fs.getMount());
                fsInfo.put("totalSpace", fs.getTotalSpace());
                fsInfo.put("usableSpace", fs.getUsableSpace());
                fsInfo.put("freeSpace", fs.getFreeSpace());
                fsInfo.put("usagePercent", fs.getTotalSpace() > 0 ? 
                    (double)(fs.getTotalSpace() - fs.getFreeSpace()) / fs.getTotalSpace() * 100 : 0);
                fileSystems.add(fsInfo);
            }
            
            data.put("fileSystems", fileSystems);
            
        } catch (Exception e) {
            log.warn("收集磁盘信息失败", e);
        }
    }

    /**
     * 收集网络信息
     */
    private void collectNetworkInfo(Map<String, Object> data) {
        try {
            List<NetworkIF> networkIFs = hardware.getNetworkIFs();
            List<Map<String, Object>> networks = new ArrayList<>();
            
            for (NetworkIF net : networkIFs) {
                if (net.getBytesRecv() > 0 || net.getBytesSent() > 0) {
                    Map<String, Object> netInfo = new HashMap<>();
                    netInfo.put("name", net.getName());
                    netInfo.put("displayName", net.getDisplayName());
                    netInfo.put("macAddress", net.getMacaddr());
                    netInfo.put("ipv4", Arrays.toString(net.getIPv4addr()));
                    netInfo.put("ipv6", Arrays.toString(net.getIPv6addr()));
                    netInfo.put("bytesReceived", net.getBytesRecv());
                    netInfo.put("bytesSent", net.getBytesSent());
                    netInfo.put("packetsReceived", net.getPacketsRecv());
                    netInfo.put("packetsSent", net.getPacketsSent());
                    netInfo.put("speed", net.getSpeed());
                    netInfo.put("mtu", net.getMTU());
                    networks.add(netInfo);
                }
            }
            
            data.put("networks", networks);
            
        } catch (Exception e) {
            log.warn("收集网络信息失败", e);
        }
    }

    /**
     * 收集进程信息
     */
    private void collectProcessInfo(Map<String, Object> data) {
        try {
            data.put("processCount", operatingSystem.getProcessCount());
            data.put("threadCount", operatingSystem.getThreadCount());
            
            // 获取前10个CPU使用率最高的进程
            List<OSProcess> processes = operatingSystem.getProcesses(
                oshi.software.os.OperatingSystem.ProcessFiltering.ALL_PROCESSES,
                oshi.software.os.OperatingSystem.ProcessSorting.CPU_DESC, 10);
            
            List<Map<String, Object>> topProcesses = new ArrayList<>();
            for (OSProcess process : processes) {
                Map<String, Object> processInfo = new HashMap<>();
                processInfo.put("pid", process.getProcessID());
                processInfo.put("name", process.getName());
                processInfo.put("cpuPercent", process.getProcessCpuLoadCumulative() * 100);
                processInfo.put("memoryPercent", (double)process.getResidentSetSize() / hardware.getMemory().getTotal() * 100);
                processInfo.put("memorySize", process.getResidentSetSize());
                topProcesses.add(processInfo);
            }
            
            data.put("topProcesses", topProcesses);
            
        } catch (Exception e) {
            log.warn("收集进程信息失败", e);
        }
    }

    /**
     * 收集JVM信息
     */
    private void collectJvmInfo(Map<String, Object> data) {
        try {
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            data.put("jvmName", runtimeBean.getVmName());
            data.put("jvmVersion", runtimeBean.getVmVersion());
            data.put("jvmVendor", runtimeBean.getVmVendor());
            data.put("jvmUptime", runtimeBean.getUptime());
            data.put("jvmStartTime", runtimeBean.getStartTime());
            
            // JVM内存信息
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
     * 检查系统健康状态
     *
     * @return 是否健康
     */
    public boolean checkSystemHealth() {
        try {
            // 检查CPU使用率
            CentralProcessor processor = hardware.getProcessor();
            double cpuUsage = processor.getSystemCpuLoad(1000) * 100;
            if (cpuUsage > 90) {
                log.warn("CPU使用率过高: {}%", cpuUsage);
                return false;
            }
            
            // 检查内存使用率
            GlobalMemory memory = hardware.getMemory();
            double memoryUsage = (double)(memory.getTotal() - memory.getAvailable()) / memory.getTotal() * 100;
            if (memoryUsage > 90) {
                log.warn("内存使用率过高: {}%", memoryUsage);
                return false;
            }
            
            // 检查磁盘使用率
            List<oshi.software.os.OSFileStore> fileStores = operatingSystem.getFileSystem().getFileStores();
            for (oshi.software.os.OSFileStore fs : fileStores) {
                if (fs.getTotalSpace() > 0) {
                    double diskUsage = (double)(fs.getTotalSpace() - fs.getFreeSpace()) / fs.getTotalSpace() * 100;
                    if (diskUsage > 95) {
                        log.warn("磁盘 {} 使用率过高: {}%", fs.getMount(), diskUsage);
                        return false;
                    }
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("检查系统健康状态失败", e);
            return false;
        }
    }
}
