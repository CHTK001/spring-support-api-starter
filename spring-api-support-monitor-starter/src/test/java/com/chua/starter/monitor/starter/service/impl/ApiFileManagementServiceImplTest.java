package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.enums.FileManagementMode;
import com.chua.starter.monitor.starter.pojo.FileManagementApiConfig;
import com.chua.starter.monitor.starter.pojo.FileOperationResponse;
import com.chua.starter.monitor.starter.service.ClientHealthService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * ApiFileManagementServiceImpl 测试类
 * 
 * @author CH
 * @since 2024/12/13
 */
@ExtendWith(MockitoExtension.class)
class ApiFileManagementServiceImplTest {

    @Mock
    private MonitorSysGenServerService serverService;
    
    @Mock
    private MonitorSysGenServerSettingService serverSettingService;
    
    @Mock
    private ClientHealthService clientHealthService;
    
    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private ObjectMapper objectMapper;

    private ApiFileManagementServiceImpl apiFileManagementService;

    @BeforeEach
    void setUp() {
        apiFileManagementService = new ApiFileManagementServiceImpl(
            serverService,
            serverSettingService,
            clientHealthService,
            restTemplate,
            objectMapper
        );
    }

    @Test
    void testListFiles_ClientNotHealthy() {
        // Given
        Integer serverId = 1;
        when(clientHealthService.isServerClientHealthy(serverId))
            .thenReturn(ReturnResult.error("客户端不健康"));

        // When
        ReturnResult<FileOperationResponse> result = apiFileManagementService.listFiles(
            serverId, "/", false, "name", "asc"
        );

        // Then
        assertFalse(result.isOk());
        assertEquals("客户端不健康", result.getMsg());
    }

    @Test
    void testListFiles_NoApiConfig() {
        // Given
        Integer serverId = 1;
        when(clientHealthService.isServerClientHealthy(serverId))
            .thenReturn(ReturnResult.ok(true));
        when(serverSettingService.getByServerId(serverId))
            .thenReturn(ReturnResult.error("配置不存在"));

        // When
        ReturnResult<FileOperationResponse> result = apiFileManagementService.listFiles(
            serverId, "/", false, "name", "asc"
        );

        // Then
        assertFalse(result.isOk());
        assertEquals("无法获取API配置", result.getMsg());
    }

    @Test
    void testGetSupportedOperations() {
        // When
        ReturnResult<java.util.List<String>> result = apiFileManagementService.getSupportedOperations(1);

        // Then
        assertTrue(result.isOk());
        assertNotNull(result.getData());
        assertTrue(result.getData().contains("LIST"));
        assertTrue(result.getData().contains("UPLOAD"));
        assertTrue(result.getData().contains("DOWNLOAD"));
    }

    @Test
    void testChangePermissions_NotSupported() {
        // When
        ReturnResult<FileOperationResponse> result = apiFileManagementService.changePermissions(
            1, "/test", "755", false
        );

        // Then
        assertFalse(result.isOk());
        assertEquals("API模式暂不支持权限修改", result.getMsg());
    }

    @Test
    void testChangeOwner_NotSupported() {
        // When
        ReturnResult<FileOperationResponse> result = apiFileManagementService.changeOwner(
            1, "/test", "user", "group", false
        );

        // Then
        assertFalse(result.isOk());
        assertEquals("API模式暂不支持所有者修改", result.getMsg());
    }

    @Test
    void testWatchFileChanges_NotSupported() {
        // When
        ReturnResult<String> result = apiFileManagementService.watchFileChanges(1, "/test", false);

        // Then
        assertFalse(result.isOk());
        assertEquals("API模式暂不支持文件监控", result.getMsg());
    }

    @Test
    void testStopWatching_NotSupported() {
        // When
        ReturnResult<Boolean> result = apiFileManagementService.stopWatching(1, "watchId");

        // Then
        assertFalse(result.isOk());
        assertEquals("API模式暂不支持文件监控", result.getMsg());
    }

    @Test
    void testFileInfoBuilderFields() {
        // 测试 FileInfo builder 是否正确工作
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        com.chua.starter.monitor.starter.pojo.FileInfo fileInfo =
            com.chua.starter.monitor.starter.pojo.FileInfo.builder()
                .name("test.txt")
                .path("/test/test.txt")
                .size(1024L)
                .isDirectory(false)
                .permissions("rw-r--r--")
                .modifyTime(now)  // 验证 modifyTime 字段
                .children(new java.util.ArrayList<>())
                .build();

        // Then
        assertNotNull(fileInfo);
        assertEquals("test.txt", fileInfo.getName());
        assertEquals("/test/test.txt", fileInfo.getPath());
        assertEquals(1024L, fileInfo.getSize());
        assertEquals(false, fileInfo.getIsDirectory());
        assertEquals("rw-r--r--", fileInfo.getPermissions());
        assertEquals(now, fileInfo.getModifyTime());
        assertNotNull(fileInfo.getChildren());
    }

    private MonitorSysGenServerSetting createTestSetting() {
        MonitorSysGenServerSetting setting = new MonitorSysGenServerSetting();
        setting.setMonitorSysGenServerSettingFileManagementMode(FileManagementMode.API);
        
        FileManagementApiConfig config = FileManagementApiConfig.createDefault();
        try {
            setting.setMonitorSysGenServerSettingFileManagementApiConfig(
                new ObjectMapper().writeValueAsString(config)
            );
        } catch (Exception e) {
            // 忽略测试中的JSON序列化错误
        }
        
        return setting;
    }
}
