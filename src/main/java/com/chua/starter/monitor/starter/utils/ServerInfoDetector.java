package com.chua.starter.monitor.starter.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 服务器信息自动检测工具类
 *
 * @author CH
 * @since 2024/12/24
 */
@Slf4j
public class ServerInfoDetector {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 检测是否为本地服务器
     *
     * @param host 主机地址
     * @return 是否为本地服务器
     */
    public static boolean isLocalServer(String host) {
        if (!StringUtils.hasText(host)) {
            return false;
        }

        try {
            // 检查是否为本地地址
            if (isLocalAddress(host)) {
                return true;
            }

            // 检查是否为本机IP
            Set<String> localIps = getLocalIpAddresses();
            return localIps.contains(host);
        } catch (Exception e) {
            log.warn("检测本地服务器失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查是否为本地地址
     *
     * @param host 主机地址
     * @return 是否为本地地址
     */
    private static boolean isLocalAddress(String host) {
        return "localhost".equalsIgnoreCase(host) ||
               "127.0.0.1".equals(host) ||
               "0.0.0.0".equals(host) ||
               "::1".equals(host);
    }

    /**
     * 获取本机所有IP地址
     *
     * @return IP地址集合
     */
    public static Set<String> getLocalIpAddresses() {
        Set<String> ipAddresses = new HashSet<>();
        
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                
                // 跳过回环接口和未启用的接口
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    
                    // 跳过回环地址和链路本地地址
                    if (inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress()) {
                        continue;
                    }
                    
                    // 只获取IPv4地址
                    if (inetAddress instanceof Inet4Address) {
                        ipAddresses.add(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            log.error("获取本机IP地址失败", e);
        }
        
        return ipAddresses;
    }

    /**
     * 将IP地址集合转换为JSON字符串
     *
     * @param ipAddresses IP地址集合
     * @return JSON字符串
     */
    public static String ipAddressesToJson(Set<String> ipAddresses) {
        if (ipAddresses == null || ipAddresses.isEmpty()) {
            return "[]";
        }
        
        try {
            List<String> sortedIps = ipAddresses.stream()
                    .sorted()
                    .collect(Collectors.toList());
            return objectMapper.writeValueAsString(sortedIps);
        } catch (JsonProcessingException e) {
            log.error("IP地址转换JSON失败", e);
            return "[]";
        }
    }

    /**
     * 从JSON字符串解析IP地址集合
     *
     * @param json JSON字符串
     * @return IP地址集合
     */
    public static Set<String> jsonToIpAddresses(String json) {
        if (!StringUtils.hasText(json)) {
            return new HashSet<>();
        }
        
        try {
            List<String> ipList = objectMapper.readValue(json, List.class);
            return new HashSet<>(ipList);
        } catch (JsonProcessingException e) {
            log.error("JSON解析IP地址失败", e);
            return new HashSet<>();
        }
    }

    /**
     * 获取操作系统类型
     *
     * @return 操作系统类型
     */
    public static String getOsType() {
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("windows")) {
            return "WINDOWS";
        } else if (osName.contains("linux")) {
            return "LINUX";
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            return "MACOS";
        } else if (osName.contains("unix")) {
            return "UNIX";
        } else if (osName.contains("solaris") || osName.contains("sunos")) {
            return "SOLARIS";
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * 获取操作系统版本
     *
     * @return 操作系统版本
     */
    public static String getOsVersion() {
        return System.getProperty("os.version");
    }

    /**
     * 获取操作系统架构
     *
     * @return 操作系统架构
     */
    public static String getOsArch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        
        if (arch.contains("amd64") || arch.contains("x86_64")) {
            return "X86_64";
        } else if (arch.contains("x86") || arch.contains("i386") || arch.contains("i686")) {
            return "X86";
        } else if (arch.contains("arm64") || arch.contains("aarch64")) {
            return "ARM64";
        } else if (arch.contains("arm")) {
            return "ARM";
        } else {
            return arch.toUpperCase();
        }
    }

    /**
     * 检测Docker是否可用
     *
     * @return 是否支持Docker
     */
    public static boolean isDockerAvailable() {
        try {
            Process process = Runtime.getRuntime().exec("docker --version");
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            log.debug("Docker检测失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取推荐的Docker连接方式
     *
     * @return Docker连接方式
     */
    public static String getRecommendedDockerConnectionType() {
        String osType = getOsType();
        
        // Windows推荐TCP连接，Linux/macOS推荐Shell连接
        if ("WINDOWS".equals(osType)) {
            return "TCP";
        } else {
            return "SHELL";
        }
    }

    /**
     * 获取默认的Docker TCP端口
     *
     * @return Docker TCP端口
     */
    public static int getDefaultDockerTcpPort() {
        return 2375; // Docker默认TCP端口
    }

    /**
     * 检测服务器的完整信息
     *
     * @param host 主机地址
     * @return 服务器信息Map
     */
    public static Map<String, Object> detectServerInfo(String host) {
        Map<String, Object> serverInfo = new HashMap<>();
        
        // 检测是否为本地服务器
        boolean isLocal = isLocalServer(host);
        serverInfo.put("isLocal", isLocal ? 1 : 0);
        
        // 如果是本地服务器，获取本机信息
        if (isLocal) {
            Set<String> localIps = getLocalIpAddresses();
            serverInfo.put("ipAddresses", ipAddressesToJson(localIps));
            serverInfo.put("osType", getOsType());
            serverInfo.put("osVersion", getOsVersion());
            serverInfo.put("osArch", getOsArch());
            serverInfo.put("dockerEnabled", isDockerAvailable() ? 1 : 0);
            serverInfo.put("dockerConnectionType", getRecommendedDockerConnectionType());
            serverInfo.put("dockerPort", getDefaultDockerTcpPort());
        } else {
            // 非本地服务器，只设置基本信息
            serverInfo.put("ipAddresses", "[]");
            serverInfo.put("osType", null);
            serverInfo.put("osVersion", null);
            serverInfo.put("osArch", null);
            serverInfo.put("dockerEnabled", 0);
            serverInfo.put("dockerConnectionType", null);
            serverInfo.put("dockerPort", null);
        }
        
        return serverInfo;
    }
}
