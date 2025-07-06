package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.pojo.FileOperationResponse;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * SshFileManagementServiceImpl 测试类
 * 
 * @author CH
 * @since 2024/12/13
 */
@ExtendWith(MockitoExtension.class)
class SshFileManagementServiceImplTest {

    @Mock
    private MonitorSysGenServerService serverService;
    
    @Mock
    private MonitorSysGenServerSettingService serverSettingService;

    private SshFileManagementServiceImpl sshFileManagementService;

    @BeforeEach
    void setUp() {
        sshFileManagementService = new SshFileManagementServiceImpl(
            serverService,
            serverSettingService
        );
    }

    @Test
    void testGetSupportedOperations() {
        // When
        ReturnResult<java.util.List<String>> result = sshFileManagementService.getSupportedOperations(1);

        // Then
        assertTrue(result.isOk());
        assertNotNull(result.getData());
        assertTrue(result.getData().contains("LIST"));
        assertTrue(result.getData().contains("UPLOAD"));
        assertTrue(result.getData().contains("DOWNLOAD"));
        assertTrue(result.getData().contains("DELETE"));
        assertTrue(result.getData().contains("RENAME"));
        assertTrue(result.getData().contains("MKDIR"));
        assertTrue(result.getData().contains("MOVE"));
        assertTrue(result.getData().contains("CHMOD"));
        assertTrue(result.getData().contains("PREVIEW"));
    }

    @Test
    void testFileOperationResponseWithMap() {
        // Given
        Map<String, Object> data = new HashMap<>();
        data.put("content", "test content");
        data.put("size", 1024L);
        data.put("encoding", "UTF-8");

        // When
        FileOperationResponse response = FileOperationResponse.success("PREVIEW", "文件预览成功", data);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("PREVIEW", response.getOperation());
        assertEquals("文件预览成功", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("test content", response.getData().get("content"));
        assertEquals(1024L, response.getData().get("size"));
        assertEquals("UTF-8", response.getData().get("encoding"));
    }

    @Test
    void testListFiles_NoServerConfig() {
        // Given
        Integer serverId = 1;
        when(serverService.getById(serverId))
            .thenReturn(ReturnResult.error("服务器不存在"));

        // When
        ReturnResult<FileOperationResponse> result = sshFileManagementService.listFiles(
            serverId, "/", false, "name", "asc"
        );

        // Then
        assertFalse(result.isOk());
        assertTrue(result.getMsg().contains("列出文件失败"));
    }

    @Test
    void testUploadFile_NoServerConfig() {
        // Given
        Integer serverId = 1;
        when(serverService.getById(serverId))
            .thenReturn(ReturnResult.error("服务器不存在"));

        // When
        ReturnResult<FileOperationResponse> result = sshFileManagementService.uploadFile(
            serverId, null, "/test", false
        );

        // Then
        assertFalse(result.isOk());
        assertTrue(result.getMsg().contains("上传文件失败"));
    }

    @Test
    void testDownloadFile_NoServerConfig() {
        // Given
        Integer serverId = 1;
        when(serverService.getById(serverId))
            .thenReturn(ReturnResult.error("服务器不存在"));

        // When
        ReturnResult<FileOperationResponse> result = sshFileManagementService.downloadFile(
            serverId, "/test/file.txt"
        );

        // Then
        assertFalse(result.isOk());
        assertTrue(result.getMsg().contains("下载文件失败"));
    }

    @Test
    void testDeleteFile_NoServerConfig() {
        // Given
        Integer serverId = 1;
        when(serverService.getById(serverId))
            .thenReturn(ReturnResult.error("服务器不存在"));

        // When
        ReturnResult<FileOperationResponse> result = sshFileManagementService.deleteFile(
            serverId, "/test/file.txt", false
        );

        // Then
        assertFalse(result.isOk());
        assertTrue(result.getMsg().contains("删除文件失败"));
    }

    @Test
    void testWatchFileChanges_NotSupported() {
        // When
        ReturnResult<String> result = sshFileManagementService.watchFileChanges(1, "/test", false);

        // Then
        assertFalse(result.isOk());
        assertEquals("SSH模式暂不支持文件监控", result.getMsg());
    }

    @Test
    void testStopWatching_NotSupported() {
        // When
        ReturnResult<Boolean> result = sshFileManagementService.stopWatching(1, "watchId");

        // Then
        assertFalse(result.isOk());
        assertEquals("SSH模式暂不支持文件监控", result.getMsg());
    }
}
