package com.chua.starter.monitor.starter.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 服务器信息检测工具类测试
 *
 * @author CH
 * @since 2024/12/24
 */
@SpringBootTest
public class ServerInfoDetectorTest {

    @Test
    public void testIsLocalServer() {
        // 测试本地地址
        assertTrue(ServerInfoDetector.isLocalServer("localhost"));
        assertTrue(ServerInfoDetector.isLocalServer("127.0.0.1"));
        assertTrue(ServerInfoDetector.isLocalServer("0.0.0.0"));
        assertTrue(ServerInfoDetector.isLocalServer("::1"));
        
        // 测试远程地址
        assertFalse(ServerInfoDetector.isLocalServer("192.168.1.100"));
        assertFalse(ServerInfoDetector.isLocalServer("8.8.8.8"));
        assertFalse(ServerInfoDetector.isLocalServer("google.com"));
        
        // 测试空值
        assertFalse(ServerInfoDetector.isLocalServer(null));
        assertFalse(ServerInfoDetector.isLocalServer(""));
    }

    @Test
    public void testGetLocalIpAddresses() {
        Set<String> localIps = ServerInfoDetector.getLocalIpAddresses();
        assertNotNull(localIps);
        // 本地应该至少有一个IP地址
        assertFalse(localIps.isEmpty());
        
        // 打印本地IP地址
        System.out.println("本地IP地址: " + localIps);
    }

    @Test
    public void testIpAddressesToJson() {
        Set<String> ips = Set.of("192.168.1.100", "10.0.0.1");
        String json = ServerInfoDetector.ipAddressesToJson(ips);
        assertNotNull(json);
        assertTrue(json.contains("192.168.1.100"));
        assertTrue(json.contains("10.0.0.1"));
        
        // 测试空集合
        String emptyJson = ServerInfoDetector.ipAddressesToJson(null);
        assertEquals("[]", emptyJson);
    }

    @Test
    public void testJsonToIpAddresses() {
        String json = "[\"192.168.1.100\",\"10.0.0.1\"]";
        Set<String> ips = ServerInfoDetector.jsonToIpAddresses(json);
        assertNotNull(ips);
        assertEquals(2, ips.size());
        assertTrue(ips.contains("192.168.1.100"));
        assertTrue(ips.contains("10.0.0.1"));
        
        // 测试空JSON
        Set<String> emptyIps = ServerInfoDetector.jsonToIpAddresses(null);
        assertTrue(emptyIps.isEmpty());
    }

    @Test
    public void testGetOsType() {
        String osType = ServerInfoDetector.getOsType();
        assertNotNull(osType);
        assertFalse(osType.isEmpty());
        
        // 应该是已知的操作系统类型之一
        assertTrue(osType.equals("WINDOWS") || 
                  osType.equals("LINUX") || 
                  osType.equals("MACOS") || 
                  osType.equals("UNIX") || 
                  osType.equals("SOLARIS") || 
                  osType.equals("UNKNOWN"));
        
        System.out.println("操作系统类型: " + osType);
    }

    @Test
    public void testGetOsVersion() {
        String osVersion = ServerInfoDetector.getOsVersion();
        assertNotNull(osVersion);
        assertFalse(osVersion.isEmpty());
        
        System.out.println("操作系统版本: " + osVersion);
    }

    @Test
    public void testGetOsArch() {
        String osArch = ServerInfoDetector.getOsArch();
        assertNotNull(osArch);
        assertFalse(osArch.isEmpty());
        
        System.out.println("操作系统架构: " + osArch);
    }

    @Test
    public void testIsDockerAvailable() {
        boolean dockerAvailable = ServerInfoDetector.isDockerAvailable();
        System.out.println("Docker可用: " + dockerAvailable);
        
        // 这个测试不做断言，因为Docker可能未安装
    }

    @Test
    public void testGetRecommendedDockerConnectionType() {
        String connectionType = ServerInfoDetector.getRecommendedDockerConnectionType();
        assertNotNull(connectionType);
        assertTrue(connectionType.equals("SHELL") || connectionType.equals("TCP"));
        
        System.out.println("推荐Docker连接方式: " + connectionType);
    }

    @Test
    public void testGetDefaultDockerTcpPort() {
        int port = ServerInfoDetector.getDefaultDockerTcpPort();
        assertEquals(2375, port);
    }

    @Test
    public void testDetectServerInfo() {
        // 测试本地服务器检测
        Map<String, Object> localInfo = ServerInfoDetector.detectServerInfo("localhost");
        assertNotNull(localInfo);
        assertEquals(1, localInfo.get("isLocal"));
        assertNotNull(localInfo.get("ipAddresses"));
        assertNotNull(localInfo.get("osType"));
        assertNotNull(localInfo.get("osVersion"));
        assertNotNull(localInfo.get("osArch"));
        
        System.out.println("本地服务器信息: " + localInfo);
        
        // 测试远程服务器检测
        Map<String, Object> remoteInfo = ServerInfoDetector.detectServerInfo("8.8.8.8");
        assertNotNull(remoteInfo);
        assertEquals(0, remoteInfo.get("isLocal"));
        assertEquals("[]", remoteInfo.get("ipAddresses"));
        assertNull(remoteInfo.get("osType"));
        
        System.out.println("远程服务器信息: " + remoteInfo);
    }
}
