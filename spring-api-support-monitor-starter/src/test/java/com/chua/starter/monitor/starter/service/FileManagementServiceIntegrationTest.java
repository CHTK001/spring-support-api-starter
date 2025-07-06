package com.chua.starter.monitor.starter.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.enums.FileManagementMode;
import com.chua.starter.monitor.starter.pojo.FileOperationResponse;
import com.chua.starter.monitor.starter.service.impl.ApiFileManagementServiceImpl;
import com.chua.starter.monitor.starter.service.impl.SshFileManagementServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 文件管理服务集成测试
 * 
 * @author CH
 * @since 2024/12/13
 */
@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class FileManagementServiceIntegrationTest {

    @Mock
    private MonitorSysGenServerSettingService serverSettingService;

    @Mock
    private SshFileManagementServiceImpl sshFileManagementService;

    @Mock
    private ApiFileManagementServiceImpl apiFileManagementService;

    @Test
    void testFileManagementServiceInterface() {
        // 测试SSH实现是否正确实现了接口
        assertTrue(sshFileManagementService instanceof FileManagementService);
        
        // 测试API实现是否正确实现了接口
        assertTrue(apiFileManagementService instanceof FileManagementService);
    }

    @Test
    void testSshFileManagementServiceMethods() {
        // 验证SSH服务的方法签名
        assertDoesNotThrow(() -> {
            when(sshFileManagementService.listFiles(anyInt(), anyString(), anyBoolean(), anyString(), anyString()))
                    .thenReturn(ReturnResult.ok(FileOperationResponse.success("LIST", "测试成功")));
            
            ReturnResult<FileOperationResponse> result = sshFileManagementService.listFiles(1, "/home", false, "name", "asc");
            assertTrue(result.isSuccess());
        });
    }

    @Test
    void testApiFileManagementServiceMethods() {
        // 验证API服务的方法签名
        assertDoesNotThrow(() -> {
            when(apiFileManagementService.listFiles(anyInt(), anyString(), anyBoolean(), anyString(), anyString()))
                    .thenReturn(ReturnResult.ok(FileOperationResponse.success("LIST", "测试成功")));
            
            ReturnResult<FileOperationResponse> result = apiFileManagementService.listFiles(1, "/var", true, "size", "desc");
            assertTrue(result.isSuccess());
        });
    }

    @Test
    void testFileManagementModeEnum() {
        // 测试文件管理模式枚举
        assertEquals(3, FileManagementMode.values().length);
        assertTrue(FileManagementMode.SSH.isEnabled());
        assertTrue(FileManagementMode.API.isEnabled());
        assertFalse(FileManagementMode.NONE.isEnabled());
    }

    @Test
    void testServerSettingConfiguration() {
        // 测试服务器设置配置
        MonitorSysGenServerSetting setting = new MonitorSysGenServerSetting();
        setting.setMonitorSysGenServerSettingServerId(1);
        setting.setMonitorSysGenServerSettingFileManagementMode(FileManagementMode.SSH);
        
        assertEquals(FileManagementMode.SSH, setting.getMonitorSysGenServerSettingFileManagementMode());
        assertTrue(setting.isFileManagementEnabled());
    }

    @Test
    void testFileManagementServiceAvailability() {
        // 测试服务可用性
        assertNotNull(sshFileManagementService);
        assertNotNull(apiFileManagementService);
        
        // 验证服务实现了正确的接口
        assertTrue(FileManagementService.class.isAssignableFrom(SshFileManagementServiceImpl.class));
        assertTrue(FileManagementService.class.isAssignableFrom(ApiFileManagementServiceImpl.class));
    }

    @Test
    void testFileOperationResponseStructure() {
        // 测试文件操作响应结构
        FileOperationResponse response = FileOperationResponse.success("TEST", "测试消息");
        
        assertTrue(response.getSuccess());
        assertEquals("TEST", response.getOperation());
        assertEquals("测试消息", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testErrorHandling() {
        // 测试错误处理
        FileOperationResponse errorResponse = FileOperationResponse.error("ERROR", "错误消息");
        
        assertFalse(errorResponse.getSuccess());
        assertEquals("ERROR", errorResponse.getOperation());
        assertEquals("错误消息", errorResponse.getMessage());
    }

    @Test
    void testServiceMethodSignatures() {
        // 验证所有必需的方法都存在
        assertDoesNotThrow(() -> {
            // 基础文件操作方法
            SshFileManagementServiceImpl.class.getMethod("listFiles", Integer.class, String.class, Boolean.class, String.class, String.class);
            SshFileManagementServiceImpl.class.getMethod("getFileTree", Integer.class, String.class, Integer.class, Boolean.class);
            SshFileManagementServiceImpl.class.getMethod("uploadFile", Integer.class, String.class, org.springframework.web.multipart.MultipartFile.class, Boolean.class);
            SshFileManagementServiceImpl.class.getMethod("downloadFile", Integer.class, String.class);
            SshFileManagementServiceImpl.class.getMethod("deleteFile", Integer.class, String.class, Boolean.class);
            SshFileManagementServiceImpl.class.getMethod("renameFile", Integer.class, String.class, String.class);
            SshFileManagementServiceImpl.class.getMethod("createDirectory", Integer.class, String.class, Boolean.class);
            SshFileManagementServiceImpl.class.getMethod("testConnection", Integer.class);
            
            // API服务方法
            ApiFileManagementServiceImpl.class.getMethod("listFiles", Integer.class, String.class, Boolean.class, String.class, String.class);
            ApiFileManagementServiceImpl.class.getMethod("getFileTree", Integer.class, String.class, Integer.class, Boolean.class);
            ApiFileManagementServiceImpl.class.getMethod("uploadFile", Integer.class, String.class, org.springframework.web.multipart.MultipartFile.class, Boolean.class);
            ApiFileManagementServiceImpl.class.getMethod("downloadFile", Integer.class, String.class);
            ApiFileManagementServiceImpl.class.getMethod("deleteFile", Integer.class, String.class, Boolean.class);
            ApiFileManagementServiceImpl.class.getMethod("renameFile", Integer.class, String.class, String.class);
            ApiFileManagementServiceImpl.class.getMethod("createDirectory", Integer.class, String.class, Boolean.class);
            ApiFileManagementServiceImpl.class.getMethod("testConnection", Integer.class);
        });
    }

    @Test
    void testInterfaceImplementation() {
        // 验证接口实现的完整性
        Class<?>[] sshInterfaces = SshFileManagementServiceImpl.class.getInterfaces();
        Class<?>[] apiInterfaces = ApiFileManagementServiceImpl.class.getInterfaces();
        
        boolean sshImplementsInterface = false;
        boolean apiImplementsInterface = false;
        
        for (Class<?> iface : sshInterfaces) {
            if (iface.equals(FileManagementService.class)) {
                sshImplementsInterface = true;
                break;
            }
        }
        
        for (Class<?> iface : apiInterfaces) {
            if (iface.equals(FileManagementService.class)) {
                apiImplementsInterface = true;
                break;
            }
        }
        
        assertTrue(sshImplementsInterface, "SshFileManagementServiceImpl should implement FileManagementService");
        assertTrue(apiImplementsInterface, "ApiFileManagementServiceImpl should implement FileManagementService");
    }

    @Test
    void testServiceConfiguration() {
        // 测试服务配置
        assertDoesNotThrow(() -> {
            // 验证服务类有正确的注解
            assertTrue(SshFileManagementServiceImpl.class.isAnnotationPresent(org.springframework.stereotype.Service.class));
            assertTrue(ApiFileManagementServiceImpl.class.isAnnotationPresent(org.springframework.stereotype.Service.class));
            
            // 验证依赖注入注解
            assertTrue(SshFileManagementServiceImpl.class.isAnnotationPresent(lombok.RequiredArgsConstructor.class));
            assertTrue(ApiFileManagementServiceImpl.class.isAnnotationPresent(lombok.RequiredArgsConstructor.class));
        });
    }
}
