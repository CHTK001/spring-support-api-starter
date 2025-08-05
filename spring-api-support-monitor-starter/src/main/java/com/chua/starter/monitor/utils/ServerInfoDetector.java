package com.chua.starter.monitor.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 服务器信息检测工具类
 * 用于自动检测服务器的基本信息，包括本地/远程判断、IP地址、操作系统信息、Docker支持等
 *
 * @author CH
 * @since 2024/12/24
 */
@Slf4j
public class ServerInfoDetector {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 检测服务器信息
     *
     * @param host 服务器主机地址
     * @return 服务器信息Map
     */
    public static Map<String, Object> detectServerInfo(String host) {
        Map<String, Object> serverInfo = new HashMap<>();
        
        try {
            // 检测是否为本地服务器
            boolean isLocal = isLocalServer(host);
            serverInfo.put("isLocal", isLocal ? 1 : 0);
            
            if (isLocal) {
                // 本地服务器，获取详细信息
                detectLocalServerInfo(serverInfo);
            } else {
                // 远程服务器，设置默认值
                setRemoteServerDefaults(serverInfo);
            }
            
            log.debug("服务器信息检测完成: host={}, isLocal={}", host, isLocal);
            
        } catch (Exception e) {
            log.error("检测服务器信息失败: host={}, error={}", host, e.getMessage(), e);
            // 设置默认值
            setDefaultServerInfo(serverInfo);
        }
        
        return serverInfo;
    }

    /**
     * 判断是否为本地服务器（通过获取本机所有网卡进行判断）
     *
     * @param host 主机地址
     * @return true-本地服务器，false-远程服务器
     */
    private static boolean isLocalServer(String host) {
        if (host == null || host.trim().isEmpty()) {
            return false;
        }

        host = host.trim().toLowerCase();

        // 检查常见的本地地址
        if ("localhost".equals(host) || "127.0.0.1".equals(host) || "::1".equals(host) || "0.0.0.0".equals(host)) {
            log.debug("检测到标准本地地址: {}", host);
            return true;
        }

        try {
            // 解析目标地址
            InetAddress targetAddress = InetAddress.getByName(host);
            String targetIp = targetAddress.getHostAddress();

            log.debug("解析目标地址: {} -> {}", host, targetIp);

            // 检查是否为回环地址
            if (targetAddress.isLoopbackAddress()) {
                log.debug("检测到回环地址: {}", targetIp);
                return true;
            }

            // 检查是否为站点本地地址
            if (targetAddress.isSiteLocalAddress()) {
                log.debug("检测到站点本地地址: {}", targetIp);
            }

            // 获取本机所有网络接口的IP地址
            Set<String> localIps = getLocalIpAddresses();

            // 检查目标IP是否在本机IP列表中
            boolean isLocal = localIps.contains(targetIp);

            // 如果IPv6地址包含作用域标识符，也尝试匹配不带作用域的版本
            if (!isLocal && targetIp.contains("%")) {
                String ipWithoutScope = targetIp.substring(0, targetIp.indexOf("%"));
                isLocal = localIps.contains(ipWithoutScope);
                log.debug("IPv6地址去除作用域后匹配: {} -> {}", targetIp, ipWithoutScope);
            }

            // 如果原始host是域名，也尝试解析所有可能的IP地址
            if (!isLocal && !host.equals(targetIp)) {
                try {
                    InetAddress[] allAddresses = InetAddress.getAllByName(host);
                    for (InetAddress addr : allAddresses) {
                        if (localIps.contains(addr.getHostAddress())) {
                            isLocal = true;
                            log.debug("通过域名解析匹配到本地IP: {} -> {}", host, addr.getHostAddress());
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.debug("解析域名所有地址失败: {}", host);
                }
            }

            log.debug("本地服务器判断结果: {} -> {}", host, isLocal);
            return isLocal;

        } catch (Exception e) {
            log.warn("判断本地服务器失败: host={}, error={}", host, e.getMessage());
            return false;
        }
    }

    /**
     * 获取本机所有IP地址（包括所有网卡）
     *
     * @return IP地址集合
     */
    private static Set<String> getLocalIpAddresses() {
        Set<String> ipAddresses = new HashSet<>();

        try {
            // 添加常见的本地地址
            ipAddresses.add("127.0.0.1");
            ipAddresses.add("::1");
            ipAddresses.add("0.0.0.0");

            // 获取所有网络接口
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // 记录网络接口信息
                log.debug("检查网络接口: {} - 活动状态: {}, 回环: {}, 虚拟: {}",
                        networkInterface.getName(),
                        networkInterface.isUp(),
                        networkInterface.isLoopback(),
                        networkInterface.isVirtual());

                // 获取该接口的所有IP地址（包括非活动接口，因为某些情况下可能需要）
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    String hostAddress = inetAddress.getHostAddress();

                    // 添加所有类型的IP地址，包括IPv4和IPv6
                    if (inetAddress instanceof Inet4Address) {
                        // IPv4地址
                        ipAddresses.add(hostAddress);
                        log.debug("添加IPv4地址: {} (接口: {})", hostAddress, networkInterface.getName());
                    } else if (inetAddress instanceof Inet6Address) {
                        // IPv6地址，去掉作用域标识符
                        String ipv6Address = hostAddress;
                        if (ipv6Address.contains("%")) {
                            ipv6Address = ipv6Address.substring(0, ipv6Address.indexOf("%"));
                        }
                        ipAddresses.add(ipv6Address);
                        ipAddresses.add(hostAddress); // 也保留原始格式
                        log.debug("添加IPv6地址: {} (接口: {})", ipv6Address, networkInterface.getName());
                    }
                }
            }

            log.info("检测到本机IP地址: {}", ipAddresses);

        } catch (Exception e) {
            log.error("获取本机IP地址失败", e);
        }

        return ipAddresses;
    }

    /**
     * 检测本地服务器详细信息
     *
     * @param serverInfo 服务器信息Map
     */
    private static void detectLocalServerInfo(Map<String, Object> serverInfo) {
        // 获取IP地址列表（通过所有网卡）
        Set<String> localIps = getLocalIpAddresses();

        // 过滤和排序IP地址，优先显示IPv4地址
        List<String> sortedIps = new ArrayList<>();
        List<String> ipv4Addresses = new ArrayList<>();
        List<String> ipv6Addresses = new ArrayList<>();

        for (String ip : localIps) {
            try {
                InetAddress addr = InetAddress.getByName(ip);
                if (addr instanceof Inet4Address) {
                    // IPv4地址，排除回环地址
                    if (!addr.isLoopbackAddress()) {
                        ipv4Addresses.add(ip);
                    }
                }
            } catch (Exception e) {
                log.debug("解析IP地址失败: {}", ip);
            }
        }

        // 按优先级排序：IPv4地址在前，IPv6地址在后
        sortedIps.addAll(ipv4Addresses);
        sortedIps.addAll(ipv6Addresses);

        // 如果没有找到有效IP，至少包含回环地址
        if (sortedIps.isEmpty()) {
            sortedIps.add("127.0.0.1");
        }

        try {
            String ipAddressesJson = objectMapper.writeValueAsString(sortedIps);
            serverInfo.put("ipAddresses", ipAddressesJson);
            log.info("检测到本地IP地址列表: {}", sortedIps);
        } catch (JsonProcessingException e) {
            log.warn("序列化IP地址列表失败", e);
            serverInfo.put("ipAddresses", "[\"127.0.0.1\"]");
        }

        // 获取详细的操作系统信息
        detectDetailedOSInfo(serverInfo);
        
        // 获取操作系统信息
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        
        serverInfo.put("osType", osName);
        serverInfo.put("osVersion", osVersion);
        serverInfo.put("osArch", osArch);
        
        // 检测Docker支持
       //detectDockerSupport(serverInfo);
        
        log.info("本地服务器信息检测完成: OS={} {}, IPs={}",
                osName, osVersion, localIps.size());
    }

    /**
     * 检测Docker支持情况
     *
     * @param serverInfo 服务器信息Map
     */
    private static void detectDockerSupport(Map<String, Object> serverInfo) {
        try {
            // 尝试执行 docker version 命令
            Process process = Runtime.getRuntime().exec("docker version");
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                serverInfo.put("dockerEnabled", 1);
                
                // 检测Docker连接方式
                detectDockerConnectionType(serverInfo);
            } else {
                serverInfo.put("dockerEnabled", 0);
                setDockerDefaults(serverInfo);
            }
            
        } catch (Exception e) {
            log.debug("Docker检测失败: {}", e.getMessage());
            serverInfo.put("dockerEnabled", 0);
            setDockerDefaults(serverInfo);
        }
    }

    /**
     * 检测Docker连接方式
     *
     * @param serverInfo 服务器信息Map
     */
    private static void detectDockerConnectionType(Map<String, Object> serverInfo) {
        try {
            // 检查是否存在Docker socket文件
            String dockerHost = System.getenv("DOCKER_HOST");
            
            if (dockerHost != null && !dockerHost.isEmpty()) {
                // 使用环境变量中的Docker主机配置
                if (dockerHost.startsWith("tcp://")) {
                    serverInfo.put("dockerConnectionType", "tcp");
                    // 解析主机和端口
                    String[] parts = dockerHost.replace("tcp://", "").split(":");
                    if (parts.length >= 1) {
                        serverInfo.put("dockerHost", parts[0]);
                    }
                    if (parts.length >= 2) {
                        try {
                            serverInfo.put("dockerPort", Integer.parseInt(parts[1]));
                        } catch (NumberFormatException e) {
                            serverInfo.put("dockerPort", 2376); // 默认端口
                        }
                    }
                } else {
                    serverInfo.put("dockerConnectionType", "shell");
                    serverInfo.put("dockerHost", "localhost");
                    serverInfo.put("dockerPort", null);
                }
            } else {
                // 默认使用shell方式
                serverInfo.put("dockerConnectionType", "shell");
                serverInfo.put("dockerHost", "localhost");
                serverInfo.put("dockerPort", null);
            }
            
        } catch (Exception e) {
            log.debug("检测Docker连接方式失败: {}", e.getMessage());
            // 设置默认值
            serverInfo.put("dockerConnectionType", "shell");
            serverInfo.put("dockerHost", "localhost");
            serverInfo.put("dockerPort", null);
        }
    }

    /**
     * 设置Docker默认值
     *
     * @param serverInfo 服务器信息Map
     */
    private static void setDockerDefaults(Map<String, Object> serverInfo) {
        serverInfo.put("dockerConnectionType", null);
        serverInfo.put("dockerHost", null);
        serverInfo.put("dockerPort", null);
    }

    /**
     * 设置远程服务器默认值
     *
     * @param serverInfo 服务器信息Map
     */
    private static void setRemoteServerDefaults(Map<String, Object> serverInfo) {
        serverInfo.put("ipAddresses", "[]");
        serverInfo.put("osType", null);
        serverInfo.put("osVersion", null);
        serverInfo.put("osArch", null);
        serverInfo.put("dockerEnabled", 0);
        setDockerDefaults(serverInfo);
    }

    /**
     * 设置默认服务器信息（检测失败时使用）
     *
     * @param serverInfo 服务器信息Map
     */
    private static void setDefaultServerInfo(Map<String, Object> serverInfo) {
        serverInfo.put("isLocal", 0);
        setRemoteServerDefaults(serverInfo);
    }

    /**
     * 执行系统命令并获取输出
     *
     * @param command 命令
     * @return 命令输出
     */
    private static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.debug("命令执行失败: command={}, exitCode={}", command, exitCode);
                return null;
            }
            
        } catch (IOException | InterruptedException e) {
            log.debug("执行命令失败: command={}, error={}", command, e.getMessage());
            return null;
        }
        
        return output.toString().trim();
    }

    /**
     * 检测详细的操作系统信息
     *
     * @param serverInfo 服务器信息Map
     */
    private static void detectDetailedOSInfo(Map<String, Object> serverInfo) {
        try {
            // 获取基本系统属性
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            String osArch = System.getProperty("os.arch");

            // 标准化操作系统类型
            String osType = normalizeOSType(osName);

            serverInfo.put("osType", osType);
            serverInfo.put("osVersion", osVersion);
            serverInfo.put("osArch", osArch);
            serverInfo.put("osName", osName);

            // 获取更详细的系统信息
            detectAdvancedSystemInfo(serverInfo, osType);

            log.info("检测到操作系统信息: type={}, version={}, arch={}", osType, osVersion, osArch);

        } catch (Exception e) {
            log.warn("检测操作系统信息失败", e);
            setDefaultOSInfo(serverInfo);
        }
    }

    /**
     * 标准化操作系统类型
     *
     * @param osName 原始操作系统名称
     * @return 标准化的操作系统类型
     */
    private static String normalizeOSType(String osName) {
        if (osName == null) {
            return "Unknown";
        }

        String lowerOsName = osName.toLowerCase();

        if (lowerOsName.contains("windows")) {
            return "Windows";
        } else if (lowerOsName.contains("linux")) {
            return "Linux";
        } else if (lowerOsName.contains("mac") || lowerOsName.contains("darwin")) {
            return "macOS";
        } else if (lowerOsName.contains("unix")) {
            return "Unix";
        } else if (lowerOsName.contains("solaris") || lowerOsName.contains("sunos")) {
            return "Solaris";
        } else if (lowerOsName.contains("aix")) {
            return "AIX";
        } else {
            return "Other";
        }
    }

    /**
     * 检测高级系统信息
     *
     * @param serverInfo 服务器信息Map
     * @param osType 操作系统类型
     */
    private static void detectAdvancedSystemInfo(Map<String, Object> serverInfo, String osType) {
        try {
            // 获取Java运行时信息
            Runtime runtime = Runtime.getRuntime();
            serverInfo.put("availableProcessors", runtime.availableProcessors());
            serverInfo.put("maxMemory", runtime.maxMemory());
            serverInfo.put("totalMemory", runtime.totalMemory());
            serverInfo.put("freeMemory", runtime.freeMemory());

            // 获取JVM信息
            serverInfo.put("javaVersion", System.getProperty("java.version"));
            serverInfo.put("javaVendor", System.getProperty("java.vendor"));
            serverInfo.put("javaHome", System.getProperty("java.home"));

            // 根据操作系统类型获取特定信息
            switch (osType) {
                case "Windows":
                    detectWindowsInfo(serverInfo);
                    break;
                case "Linux":
                    detectLinuxInfo(serverInfo);
                    break;
                case "macOS":
                    detectMacOSInfo(serverInfo);
                    break;
                default:
                    log.debug("未实现 {} 操作系统的详细信息检测", osType);
                    break;
            }

        } catch (Exception e) {
            log.warn("检测高级系统信息失败", e);
        }
    }

    /**
     * 检测Windows系统信息
     *
     * @param serverInfo 服务器信息Map
     */
    private static void detectWindowsInfo(Map<String, Object> serverInfo) {
        try {
            // Windows特有的系统属性
            serverInfo.put("userDomain", System.getProperty("user.domain", "Unknown"));
            serverInfo.put("computerName", System.getenv("COMPUTERNAME"));
            serverInfo.put("userProfile", System.getenv("USERPROFILE"));

            // 尝试获取Windows版本信息
            String windir = System.getenv("WINDIR");
            if (windir != null) {
                serverInfo.put("windowsDir", windir);
            }

        } catch (Exception e) {
            log.debug("检测Windows信息失败", e);
        }
    }

    /**
     * 检测Linux系统信息
     *
     * @param serverInfo 服务器信息Map
     */
    private static void detectLinuxInfo(Map<String, Object> serverInfo) {
        try {
            // Linux特有的环境变量
            serverInfo.put("shell", System.getenv("SHELL"));
            serverInfo.put("path", System.getenv("PATH"));
            serverInfo.put("home", System.getenv("HOME"));

            // 尝试读取发行版信息
            detectLinuxDistribution(serverInfo);

        } catch (Exception e) {
            log.debug("检测Linux信息失败", e);
        }
    }

    /**
     * 检测macOS系统信息
     *
     * @param serverInfo 服务器信息Map
     */
    private static void detectMacOSInfo(Map<String, Object> serverInfo) {
        try {
            // macOS特有的环境变量
            serverInfo.put("shell", System.getenv("SHELL"));
            serverInfo.put("path", System.getenv("PATH"));
            serverInfo.put("home", System.getenv("HOME"));

        } catch (Exception e) {
            log.debug("检测macOS信息失败", e);
        }
    }

    /**
     * 检测Linux发行版信息
     *
     * @param serverInfo 服务器信息Map
     */
    private static void detectLinuxDistribution(Map<String, Object> serverInfo) {
        try {
            // 尝试读取 /etc/os-release 文件
            Path osReleasePath = Paths.get("/etc/os-release");
            if (Files.exists(osReleasePath)) {
                List<String> lines = Files.readAllLines(osReleasePath);
                for (String line : lines) {
                    if (line.startsWith("NAME=")) {
                        String distroName = line.substring(5).replaceAll("\"", "");
                        serverInfo.put("linuxDistribution", distroName);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("读取Linux发行版信息失败", e);
        }
    }

    /**
     * 设置默认操作系统信息
     *
     * @param serverInfo 服务器信息Map
     */
    private static void setDefaultOSInfo(Map<String, Object> serverInfo) {
        serverInfo.put("osType", "Unknown");
        serverInfo.put("osVersion", "Unknown");
        serverInfo.put("osArch", "Unknown");
        serverInfo.put("osName", "Unknown");
    }
}
