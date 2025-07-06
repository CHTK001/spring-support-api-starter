package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.enums.FileManagementMode;
import com.chua.starter.monitor.starter.pojo.ClientHealthStatus;
import com.chua.starter.monitor.starter.pojo.FileInfo;
import com.chua.starter.monitor.starter.pojo.FileOperationRequest;
import com.chua.starter.monitor.starter.pojo.FileOperationResponse;
import com.chua.starter.monitor.starter.service.ClientHealthService;
import com.chua.starter.monitor.starter.service.FileManagementService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * NODE模式文件管理服务实现
 * 通过在线客户端代理进行文件管理，自动检测客户端地址
 * 
 * @author CH
 * @since 2024/12/13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeFileManagementServiceImpl implements FileManagementService {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerSettingService serverSettingService;
    private final ClientHealthService clientHealthService;
    private final ApiFileManagementServiceImpl apiFileManagementService;

    @Override
    public ReturnResult<FileOperationResponse> listFiles(Integer serverId, String path, Boolean includeHidden, String sortBy, String sortOrder) {
        try {
            // 检查客户端健康状态并获取地址
            ReturnResult<String> clientAddressResult = getClientAddress(serverId);
            if (!clientAddressResult.isSuccess()) {
                return ReturnResult.error(clientAddressResult.getMsg());
            }
            
            // 使用API服务实现，但使用自动检测的客户端地址
            return apiFileManagementService.listFiles(serverId, path, includeHidden, sortBy, sortOrder);
            
        } catch (Exception e) {
            log.error("NODE模式列出文件失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("NODE模式列出文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth, Boolean includeHidden) {
        try {
            // 检查客户端健康状态并获取地址
            ReturnResult<String> clientAddressResult = getClientAddress(serverId);
            if (!clientAddressResult.isSuccess()) {
                return ReturnResult.error(clientAddressResult.getMsg());
            }

            return apiFileManagementService.getFileTree(serverId, path, maxDepth, includeHidden);

        } catch (Exception e) {
            log.error("NODE模式获取文件树失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("NODE模式获取文件树失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, MultipartFile file, Boolean overwrite) {
        try {
            // 检查客户端健康状态并获取地址
            ReturnResult<String> clientAddressResult = getClientAddress(serverId);
            if (!clientAddressResult.isSuccess()) {
                return ReturnResult.error(clientAddressResult.getMsg());
            }

            return apiFileManagementService.uploadFile(serverId, targetPath, file, overwrite);

        } catch (Exception e) {
            log.error("NODE模式上传文件失败: serverId={}, targetPath={}", serverId, targetPath, e);
            return ReturnResult.error("NODE模式上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, String fileName, InputStream inputStream, Boolean overwrite) {
        try {
            // 检查客户端健康状态并获取地址
            ReturnResult<String> clientAddressResult = getClientAddress(serverId);
            if (!clientAddressResult.isSuccess()) {
                return ReturnResult.error(clientAddressResult.getMsg());
            }
            
            return apiFileManagementService.uploadFile(serverId, targetPath, fileName, inputStream, overwrite);

        } catch (Exception e) {
            log.error("NODE模式上传文件失败: serverId={}, targetPath={}, fileName={}", serverId, targetPath, fileName, e);
            return ReturnResult.error("NODE模式上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> downloadFile(Integer serverId, String filePath) {
        try {
            // 检查客户端健康状态并获取地址
            ReturnResult<String> clientAddressResult = getClientAddress(serverId);
            if (!clientAddressResult.isSuccess()) {
                return ReturnResult.error(clientAddressResult.getMsg());
            }

            return apiFileManagementService.downloadFile(serverId, filePath);

        } catch (Exception e) {
            log.error("NODE模式下载文件失败: serverId={}, filePath={}", serverId, filePath, e);
            return ReturnResult.error("NODE模式下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> deleteFile(Integer serverId, String path, Boolean recursive) {
        try {
            // 检查客户端健康状态并获取地址
            ReturnResult<String> clientAddressResult = getClientAddress(serverId);
            if (!clientAddressResult.isSuccess()) {
                return ReturnResult.error(clientAddressResult.getMsg());
            }

            return apiFileManagementService.deleteFile(serverId, path, recursive);

        } catch (Exception e) {
            log.error("NODE模式删除文件失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("NODE模式删除文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> renameFile(Integer serverId, String oldPath, String newName) {
        try {
            // 检查客户端健康状态并获取地址
            ReturnResult<String> clientAddressResult = getClientAddress(serverId);
            if (!clientAddressResult.isSuccess()) {
                return ReturnResult.error(clientAddressResult.getMsg());
            }
            
            return apiFileManagementService.renameFile(serverId, oldPath, newName);

        } catch (Exception e) {
            log.error("NODE模式重命名文件失败: serverId={}, oldPath={}, newName={}", serverId, oldPath, newName, e);
            return ReturnResult.error("NODE模式重命名文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> createDirectory(Integer serverId, String path, Boolean recursive) {
        try {
            // 检查客户端健康状态并获取地址
            ReturnResult<String> clientAddressResult = getClientAddress(serverId);
            if (!clientAddressResult.isSuccess()) {
                return ReturnResult.error(clientAddressResult.getMsg());
            }

            return apiFileManagementService.createDirectory(serverId, path, recursive);

        } catch (Exception e) {
            log.error("NODE模式创建目录失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("NODE模式创建目录失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> testConnection(Integer serverId) {
        try {
            // 检查客户端健康状态并获取地址
            ReturnResult<String> clientAddressResult = getClientAddress(serverId);
            if (!clientAddressResult.isSuccess()) {
                return ReturnResult.error(clientAddressResult.getMsg());
            }

            return apiFileManagementService.testConnection(serverId);

        } catch (Exception e) {
            log.error("NODE模式测试连接失败: serverId={}", serverId, e);
            return ReturnResult.error("NODE模式测试连接失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户端地址
     * NODE模式自动检测在线客户端地址
     */
    private ReturnResult<String> getClientAddress(Integer serverId) {
        try {
            // 获取服务器信息
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在: " + serverId);
            }
            
            // 检查文件管理模式
            ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return ReturnResult.error("获取服务器配置失败: " + (settingResult.isOk() ? "配置为空" : settingResult.getMsg()));
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            if (setting.getMonitorSysGenServerSettingFileManagementMode() != FileManagementMode.NODE) {
                return ReturnResult.error("服务器文件管理模式不是NODE模式");
            }
            
            // 检查客户端健康状态
            ReturnResult<ClientHealthStatus> healthResult = clientHealthService.getClientHealth(server.getMonitorSysGenServerHost(), server.getMonitorSysGenServerPort());
            if (!healthResult.isSuccess()) {
                return ReturnResult.error("客户端不在线或健康状态检查失败: " + healthResult.getMsg());
            }
            
            ClientHealthStatus healthStatus = healthResult.getData();
            if (healthStatus == null) {
                return ReturnResult.error("客户端健康状态为空");
            }
            
            // 返回客户端地址
            String clientAddress = healthStatus.getClientAddress();
            log.debug("NODE模式自动检测到客户端地址: {}", clientAddress);
            return ReturnResult.ok(clientAddress);
            
        } catch (Exception e) {
            log.error("获取客户端地址失败: serverId={}", serverId, e);
            return ReturnResult.error("获取客户端地址失败: " + e.getMessage());
        }
    }

    // 以下方法委托给 ApiFileManagementServiceImpl，但需要先检查客户端健康状态
    
    @Override
    public ReturnResult<FileOperationResponse> copyFile(Integer serverId, String sourcePath, String targetPath, Boolean overwrite) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.copyFile(serverId, sourcePath, targetPath, overwrite);
    }

    @Override
    public ReturnResult<FileOperationResponse> moveFile(Integer serverId, String sourcePath, String targetPath, Boolean overwrite) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.moveFile(serverId, sourcePath, targetPath, overwrite);
    }

    @Override
    public ReturnResult<FileOperationResponse> changePermissions(Integer serverId, String path, String permissions, Boolean recursive) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.changePermissions(serverId, path, permissions, recursive);
    }

    @Override
    public ReturnResult<FileOperationResponse> changeOwner(Integer serverId, String path, String owner, String group, Boolean recursive) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.changeOwner(serverId, path, owner, group, recursive);
    }

    @Override
    public ReturnResult<FileOperationResponse> searchFiles(Integer serverId, String path, String pattern, Boolean includeContent, Integer maxResults) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.searchFiles(serverId, path, pattern, includeContent, maxResults);
    }

    @Override
    public ReturnResult<FileOperationResponse> compressFiles(Integer serverId, List<String> filePaths, String archivePath, String format) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.compressFiles(serverId, filePaths, archivePath, format);
    }

    @Override
    public ReturnResult<FileOperationResponse> extractArchive(Integer serverId, String archivePath, String extractPath, Boolean overwrite) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.extractArchive(serverId, archivePath, extractPath, overwrite);
    }

    @Override
    public ReturnResult<FileOperationResponse> previewFile(Integer serverId, String filePath, String encoding, Long maxSize) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.previewFile(serverId, filePath, encoding, maxSize);
    }

    @Override
    public ReturnResult<FileInfo> getFileInfo(Integer serverId, String path) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.getFileInfo(serverId, path);
    }

    @Override
    public ReturnResult<FileOperationResponse> executeOperation(Integer serverId, FileOperationRequest request) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.executeOperation(serverId, request);
    }

    @Override
    public ReturnResult<FileOperationResponse> batchDeleteFiles(Integer serverId, List<String> paths, Boolean recursive) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.batchDeleteFiles(serverId, paths, recursive);
    }

    @Override
    public ReturnResult<List<String>> getSupportedOperations(Integer serverId) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.getSupportedOperations(serverId);
    }

    @Override
    public ReturnResult<FileOperationResponse> getDiskUsage(Integer serverId, String path) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.getDiskUsage(serverId, path);
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileStatistics(Integer serverId, String path, Boolean includeSubdirectories) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.getFileStatistics(serverId, path, includeSubdirectories);
    }

    @Override
    public ReturnResult<String> watchFileChanges(Integer serverId, String path, Boolean recursive) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.watchFileChanges(serverId, path, recursive);
    }

    @Override
    public ReturnResult<Boolean> stopWatching(Integer serverId, String watchId) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.stopWatching(serverId, watchId);
    }

    @Override
    public ReturnResult<String> getFileChecksum(Integer serverId, String filePath, String algorithm) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.getFileChecksum(serverId, filePath, algorithm);
    }

    @Override
    public ReturnResult<FileOperationResponse> syncFiles(Integer serverId, String sourcePath, String targetPath, Boolean deleteExtra) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.syncFiles(serverId, sourcePath, targetPath, deleteExtra);
    }

    @Override
    public ReturnResult<FileOperationResponse> createSymbolicLink(Integer serverId, String targetPath, String linkPath) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.createSymbolicLink(serverId, targetPath, linkPath);
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileSystemInfo(Integer serverId) {
        ReturnResult<String> clientAddressResult = getClientAddress(serverId);
        if (!clientAddressResult.isSuccess()) {
            return ReturnResult.error(clientAddressResult.getMsg());
        }
        return apiFileManagementService.getFileSystemInfo(serverId);
    }
}
