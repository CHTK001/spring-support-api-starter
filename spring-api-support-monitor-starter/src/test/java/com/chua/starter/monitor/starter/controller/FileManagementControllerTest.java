package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.enums.FileManagementMode;
import com.chua.starter.monitor.starter.pojo.FileOperationResponse;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.starter.service.impl.ApiFileManagementServiceImpl;
import com.chua.starter.monitor.starter.service.impl.SshFileManagementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 文件管理控制器测试
 * 
 * @author CH
 * @since 2024/12/13
 */
@ExtendWith(MockitoExtension.class)
class FileManagementControllerTest {

    @Mock
    private MonitorSysGenServerSettingService serverSettingService;

    @Mock
    private SshFileManagementServiceImpl sshFileManagementService;

    @Mock
    private ApiFileManagementServiceImpl apiFileManagementService;

    @InjectMocks
    private FileManagementController fileManagementController;

    private MonitorSysGenServerSetting sshSetting;
    private MonitorSysGenServerSetting apiSetting;
    private MonitorSysGenServerSetting noneSetting;

    @BeforeEach
    void setUp() {
        // SSH模式设置
        sshSetting = new MonitorSysGenServerSetting();
        sshSetting.setMonitorSysGenServerSettingServerId(1);
        sshSetting.setMonitorSysGenServerSettingFileManagementMode(FileManagementMode.SSH);

        // API模式设置
        apiSetting = new MonitorSysGenServerSetting();
        apiSetting.setMonitorSysGenServerSettingServerId(2);
        apiSetting.setMonitorSysGenServerSettingFileManagementMode(FileManagementMode.API);

        // NONE模式设置
        noneSetting = new MonitorSysGenServerSetting();
        noneSetting.setMonitorSysGenServerSettingServerId(3);
        noneSetting.setMonitorSysGenServerSettingFileManagementMode(FileManagementMode.NONE);
    }

    @Test
    void testListFiles_SshMode_Success() {
        // 准备数据
        when(serverSettingService.getByServerId(1)).thenReturn(sshSetting);
        when(sshFileManagementService.listFiles(eq(1), eq("/home"), eq(false), eq("name"), eq("asc")))
                .thenReturn(ReturnResult.ok(FileOperationResponse.success("LIST", "文件列表获取成功")));

        // 执行测试
        ReturnResult<FileOperationResponse> result = fileManagementController.listFiles(1, "/home", false, "name", "asc");

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("文件列表获取成功", result.getData().getMessage());
        verify(sshFileManagementService).listFiles(1, "/home", false, "name", "asc");
        verify(apiFileManagementService, never()).listFiles(anyInt(), anyString(), anyBoolean(), anyString(), anyString());
    }

    @Test
    void testListFiles_ApiMode_Success() {
        // 准备数据
        when(serverSettingService.getByServerId(2)).thenReturn(apiSetting);
        when(apiFileManagementService.listFiles(eq(2), eq("/var"), eq(true), eq("size"), eq("desc")))
                .thenReturn(ReturnResult.ok(FileOperationResponse.success("LIST", "API文件列表获取成功")));

        // 执行测试
        ReturnResult<FileOperationResponse> result = fileManagementController.listFiles(2, "/var", true, "size", "desc");

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("API文件列表获取成功", result.getData().getMessage());
        verify(apiFileManagementService).listFiles(2, "/var", true, "size", "desc");
        verify(sshFileManagementService, never()).listFiles(anyInt(), anyString(), anyBoolean(), anyString(), anyString());
    }

    @Test
    void testListFiles_NoneMode_Error() {
        // 准备数据
        when(serverSettingService.getByServerId(3)).thenReturn(noneSetting);

        // 执行测试
        ReturnResult<FileOperationResponse> result = fileManagementController.listFiles(3, "/tmp", false, "name", "asc");

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("文件管理功能未启用", result.getMessage());
        verify(sshFileManagementService, never()).listFiles(anyInt(), anyString(), anyBoolean(), anyString(), anyString());
        verify(apiFileManagementService, never()).listFiles(anyInt(), anyString(), anyBoolean(), anyString(), anyString());
    }

    @Test
    void testListFiles_NoConfiguration_Error() {
        // 准备数据
        when(serverSettingService.getByServerId(4)).thenReturn(null);

        // 执行测试
        ReturnResult<FileOperationResponse> result = fileManagementController.listFiles(4, "/opt", false, "name", "asc");

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("服务器文件管理模式未配置", result.getMessage());
    }

    @Test
    void testGetFileTree_SshMode_Success() {
        // 准备数据
        when(serverSettingService.getByServerId(1)).thenReturn(sshSetting);
        when(sshFileManagementService.getFileTree(eq(1), eq("/home"), eq(3), eq(false)))
                .thenReturn(ReturnResult.ok(FileOperationResponse.successWithTree("TREE", "文件树获取成功", null)));

        // 执行测试
        ReturnResult<FileOperationResponse> result = fileManagementController.getFileTree(1, "/home", 3, false);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("文件树获取成功", result.getData().getMessage());
        verify(sshFileManagementService).getFileTree(1, "/home", 3, false);
    }

    @Test
    void testUploadFile_SshMode_Success() {
        // 准备数据
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
        when(serverSettingService.getByServerId(1)).thenReturn(sshSetting);
        when(sshFileManagementService.uploadFile(eq(1), eq("/upload"), eq(file), eq(false)))
                .thenReturn(ReturnResult.ok(FileOperationResponse.success("UPLOAD", "文件上传成功")));

        // 执行测试
        ReturnResult<FileOperationResponse> result = fileManagementController.uploadFile(1, "/upload", file, false);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("文件上传成功", result.getData().getMessage());
        verify(sshFileManagementService).uploadFile(1, "/upload", file, false);
    }

    @Test
    void testDeleteFile_ApiMode_Success() {
        // 准备数据
        when(serverSettingService.getByServerId(2)).thenReturn(apiSetting);
        when(apiFileManagementService.deleteFile(eq(2), eq("/tmp/test.txt"), eq(false)))
                .thenReturn(ReturnResult.ok(FileOperationResponse.success("DELETE", "文件删除成功")));

        // 执行测试
        ReturnResult<FileOperationResponse> result = fileManagementController.deleteFile(2, "/tmp/test.txt", false);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("文件删除成功", result.getData().getMessage());
        verify(apiFileManagementService).deleteFile(2, "/tmp/test.txt", false);
    }

    @Test
    void testRenameFile_SshMode_Success() {
        // 准备数据
        when(serverSettingService.getByServerId(1)).thenReturn(sshSetting);
        when(sshFileManagementService.renameFile(eq(1), eq("/home/old.txt"), eq("new.txt")))
                .thenReturn(ReturnResult.ok(FileOperationResponse.success("RENAME", "文件重命名成功")));

        // 执行测试
        ReturnResult<FileOperationResponse> result = fileManagementController.renameFile(1, "/home/old.txt", "new.txt");

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("文件重命名成功", result.getData().getMessage());
        verify(sshFileManagementService).renameFile(1, "/home/old.txt", "new.txt");
    }

    @Test
    void testCreateDirectory_ApiMode_Success() {
        // 准备数据
        when(serverSettingService.getByServerId(2)).thenReturn(apiSetting);
        when(apiFileManagementService.createDirectory(eq(2), eq("/tmp/newdir"), eq(true)))
                .thenReturn(ReturnResult.ok(FileOperationResponse.success("MKDIR", "目录创建成功")));

        // 执行测试
        ReturnResult<FileOperationResponse> result = fileManagementController.createDirectory(2, "/tmp/newdir", true);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("目录创建成功", result.getData().getMessage());
        verify(apiFileManagementService).createDirectory(2, "/tmp/newdir", true);
    }

    @Test
    void testTestConnection_SshMode_Success() {
        // 准备数据
        when(serverSettingService.getByServerId(1)).thenReturn(sshSetting);
        when(sshFileManagementService.testConnection(1)).thenReturn(ReturnResult.ok(true));

        // 执行测试
        ReturnResult<Boolean> result = fileManagementController.testConnection(1);

        // 验证结果
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(sshFileManagementService).testConnection(1);
    }

    @Test
    void testTestConnection_ApiMode_Success() {
        // 准备数据
        when(serverSettingService.getByServerId(2)).thenReturn(apiSetting);
        when(apiFileManagementService.testConnection(2))
                .thenReturn(ReturnResult.ok(FileOperationResponse.success("TEST", "连接测试成功")));

        // 执行测试
        ReturnResult<Boolean> result = fileManagementController.testConnection(2);

        // 验证结果
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(apiFileManagementService).testConnection(2);
    }

    @Test
    void testException_Handling() {
        // 准备数据
        when(serverSettingService.getByServerId(1)).thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试
        ReturnResult<FileOperationResponse> result = fileManagementController.listFiles(1, "/home", false, "name", "asc");

        // 验证结果
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("列出文件失败"));
    }
}
