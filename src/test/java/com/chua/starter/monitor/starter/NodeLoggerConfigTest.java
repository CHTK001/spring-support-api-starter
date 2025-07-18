package com.chua.starter.monitor.starter;

import com.chua.starter.monitor.starter.pojo.NodeLoggerConfigDTO;
import com.chua.starter.monitor.starter.service.impl.NodeLoggerConfigServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 节点日志器配置功能测试
 *
 * @author CH
 * @since 2025/01/17
 */
@SpringBootTest
class NodeLoggerConfigTest {

    @Test
    void testNodeLoggerConfigDTO() {
        // 测试 DTO 基本功能
        NodeLoggerConfigDTO dto = new NodeLoggerConfigDTO();
        dto.setNodeUrl("test-url");
        dto.setLoggerName("com.example.Test");
        dto.setConfiguredLevel("DEBUG");
        dto.setEffectiveLevel("DEBUG");
        dto.setAdditive(true);
        dto.setIpAddress("192.168.1.100");
        dto.setPort(8080);
        
        // 测试基本属性
        assertEquals("test-url", dto.getNodeUrl());
        assertEquals("com.example.Test", dto.getLoggerName());
        assertEquals("DEBUG", dto.getConfiguredLevel());
        assertEquals("DEBUG", dto.getEffectiveLevel());
        assertTrue(dto.getAdditive());
        
        // 测试工具方法
        assertTrue(dto.hasConfiguredLevel());
        assertFalse(dto.isInherited());
        assertEquals("DEBUG", dto.getDisplayLevel());
        assertEquals("192.168.1.100:8080", dto.getNodeAddress());
        
        // 测试继承状态
        dto.setConfiguredLevel(null);
        assertFalse(dto.hasConfiguredLevel());
        assertTrue(dto.isInherited());
        assertEquals("DEBUG (继承)", dto.getDisplayLevel());
        
        // 测试空配置等级
        dto.setConfiguredLevel("");
        assertFalse(dto.hasConfiguredLevel());
        assertTrue(dto.isInherited());
    }

    @Test
    void testNodeUrlEncoding() {
        // 测试节点URL编码解码
        String ipAddress = "192.168.1.100";
        Integer port = 8080;
        String expected = ipAddress + ":" + port;
        
        // 手动编码
        String encoded = Base64.getEncoder().encodeToString(expected.getBytes());
        
        // 手动解码
        String decoded = new String(Base64.getDecoder().decode(encoded));
        
        assertEquals(expected, decoded);
        
        // 测试特殊字符
        String specialIp = "127.0.0.1";
        Integer specialPort = 9090;
        String specialExpected = specialIp + ":" + specialPort;
        String specialEncoded = Base64.getEncoder().encodeToString(specialExpected.getBytes());
        String specialDecoded = new String(Base64.getDecoder().decode(specialEncoded));
        
        assertEquals(specialExpected, specialDecoded);
    }

    @Test
    void testNodeLoggerConfigDTOConstructors() {
        // 测试默认构造方法
        NodeLoggerConfigDTO dto1 = new NodeLoggerConfigDTO();
        assertTrue(dto1.getEnabled());
        assertEquals("UNKNOWN", dto1.getNodeStatus());
        
        // 测试简单构造方法
        NodeLoggerConfigDTO dto2 = new NodeLoggerConfigDTO("test-url", "com.example.Logger", "INFO", "INFO");
        assertEquals("test-url", dto2.getNodeUrl());
        assertEquals("com.example.Logger", dto2.getLoggerName());
        assertEquals("INFO", dto2.getConfiguredLevel());
        assertEquals("INFO", dto2.getEffectiveLevel());
        assertTrue(dto2.getEnabled());
        assertEquals("UNKNOWN", dto2.getNodeStatus());
        
        // 测试完整构造方法
        NodeLoggerConfigDTO dto3 = new NodeLoggerConfigDTO(
                "test-url", "192.168.1.100", 8080, "test-app",
                "com.example.Logger", "DEBUG", "DEBUG", true);
        assertEquals("test-url", dto3.getNodeUrl());
        assertEquals("192.168.1.100", dto3.getIpAddress());
        assertEquals(Integer.valueOf(8080), dto3.getPort());
        assertEquals("test-app", dto3.getApplicationName());
        assertEquals("com.example.Logger", dto3.getLoggerName());
        assertEquals("DEBUG", dto3.getConfiguredLevel());
        assertEquals("DEBUG", dto3.getEffectiveLevel());
        assertTrue(dto3.getAdditive());
    }

    @Test
    void testNodeStatusMethods() {
        NodeLoggerConfigDTO dto = new NodeLoggerConfigDTO();
        
        // 测试默认状态
        assertFalse(dto.isNodeOnline());
        
        // 测试在线状态
        dto.setNodeStatus("ONLINE");
        assertTrue(dto.isNodeOnline());
        
        // 测试离线状态
        dto.setNodeStatus("OFFLINE");
        assertFalse(dto.isNodeOnline());
        
        // 测试未知状态
        dto.setNodeStatus("UNKNOWN");
        assertFalse(dto.isNodeOnline());
    }

    @Test
    void testToString() {
        NodeLoggerConfigDTO dto = new NodeLoggerConfigDTO();
        dto.setNodeUrl("test-url");
        dto.setApplicationName("test-app");
        dto.setLoggerName("com.example.Test");
        dto.setConfiguredLevel("INFO");
        dto.setEffectiveLevel("INFO");
        dto.setAdditive(false);
        dto.setNodeStatus("ONLINE");
        
        String toString = dto.toString();
        
        // 验证 toString 包含关键信息
        assertTrue(toString.contains("test-url"));
        assertTrue(toString.contains("test-app"));
        assertTrue(toString.contains("com.example.Test"));
        assertTrue(toString.contains("INFO"));
        assertTrue(toString.contains("ONLINE"));
        assertTrue(toString.contains("false"));
    }
}
