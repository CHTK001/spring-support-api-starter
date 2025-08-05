package com.chua.starter.monitor.service;

import com.chua.starter.monitor.pojo.NodeLoggerConfigDTO;
import com.chua.starter.monitor.service.impl.NodeLoggerConfigServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 节点日志器配置服务测试
 *
 * @author CH
 * @since 2025/01/17
 */
@ExtendWith(MockitoExtension.class)
class NodeLoggerConfigServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NodeLoggerConfigServiceImpl nodeLoggerConfigService;

    @Test
    void testDecodeNodeUrl() {
        // 测试节点URL解码
        String ipPort = "192.168.1.100:8080";
        String encoded = Base64.getEncoder().encodeToString(ipPort.getBytes());
        
        String decoded = nodeLoggerConfigService.decodeNodeUrl(encoded);
        assertEquals(ipPort, decoded);
    }

    @Test
    void testEncodeNodeUrl() {
        // 测试节点URL编码
        String ipAddress = "192.168.1.100";
        Integer port = 8080;
        
        String encoded = nodeLoggerConfigService.encodeNodeUrl(ipAddress, port);
        String decoded = new String(Base64.getDecoder().decode(encoded));
        
        assertEquals(ipAddress + ":" + port, decoded);
    }

    @Test
    void testNodeLoggerConfigDTO() {
        // 测试 DTO 类
        NodeLoggerConfigDTO dto = new NodeLoggerConfigDTO();
        dto.setNodeUrl("test-url");
        dto.setLoggerName("com.example.Test");
        dto.setConfiguredLevel("DEBUG");
        dto.setEffectiveLevel("DEBUG");
        dto.setAdditive(true);
        
        assertTrue(dto.hasConfiguredLevel());
        assertFalse(dto.isInherited());
        assertEquals("DEBUG", dto.getDisplayLevel());
        
        // 测试继承状态
        dto.setConfiguredLevel(null);
        assertFalse(dto.hasConfiguredLevel());
        assertTrue(dto.isInherited());
        assertEquals("DEBUG (继承)", dto.getDisplayLevel());
    }

    @Test
    void testRegisterNodeToApplication() {
        // 测试节点注册到应用缓存
        String nodeUrl = "test-node-url";
        String applicationName = "test-app";
        
        nodeLoggerConfigService.registerNodeToApplication(nodeUrl, applicationName);
        
        // 验证注册成功（通过获取应用的节点列表）
        var result = nodeLoggerConfigService.getNodesByApplicationName(applicationName);
        assertTrue(result.isSuccess());
        assertTrue(result.getData().contains(nodeUrl));
    }

    @Test
    void testUnregisterNodeFromApplication() {
        // 测试从应用缓存中移除节点
        String nodeUrl = "test-node-url";
        String applicationName = "test-app";
        
        // 先注册
        nodeLoggerConfigService.registerNodeToApplication(nodeUrl, applicationName);
        
        // 再移除
        nodeLoggerConfigService.unregisterNodeFromApplication(nodeUrl);
        
        // 验证移除成功
        var result = nodeLoggerConfigService.getNodesByApplicationName(applicationName);
        assertTrue(result.isSuccess());
        assertFalse(result.getData().contains(nodeUrl));
    }
}
