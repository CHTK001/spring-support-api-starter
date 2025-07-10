package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
// 移除 FileManagementMode 导入，改为使用字符串比较
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

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import com.chua.common.support.discovery.Discovery;
import com.chua.starter.discovery.support.service.DiscoveryService;

/**
 * NODE模式文件管理服务实现 通过在线客户端代理进行文件管理，自动检测客户端地址
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
    private final DiscoveryService discoveryService;

    @Override
    public ReturnResult<FileOperationResponse> listFiles(Integer serverId, String path, Boolean includeHidden,
            String sortBy, String sortOrder) {
        try {
            // 检查客户端健康状态并获取地址
            ReturnResult<String> clientAddressResult = getClientAddress(serverId);
            if (!clientAddressResult.isSuccess()) {
                return ReturnResult.error(clientAddressResult.getMsg());
            }

            return apiFileManagementService.listFiles(serverId, path, includeHidden, sortBy, sortOrder);

        } catch (Exception e) {
            log.error("NODE模式文件列表获取异常: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("NODE模式文件列表获取异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth,
            Boolean includeHidden) {
        try {
            // 检查客户端健康状态并获取地址
            ReturnResult<String> clientAddressResult = getClientAddress(serverId);
            if (!clientAddressResult.isSuccess()) {
                return ReturnResult.error(clientAddressResult.getMsg());
            }

            return apiFileManagementService.getFileTree(serverId, path, maxDepth, includeHidden);

        } catch (Exception e) {
            log.error("NODE模式文件树获取异常: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("NODE模式文件树获取异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, MultipartFile file,
            Boolean overwrite) {
        try {
            // 检查客户端健康状态
            ReturnResult<Boolean> healthCheck = clientHealthService.isServerClientHealthy(String.valueOf(serverId));
            if (!healthCheck.isSuccess() || !healthCheck.getData()) {
                return ReturnResult.error("客户端不在线或健康检查失败");
            }

            // 获取客户端地址
            String clientUrl = getClientUrl(String.valueOf(serverId));
            if (clientUrl == null) {
                return ReturnResult.error("无法获取客户端地址");
            }

            String url = clientUrl + "/api/file-management/upload";

            // 构建multipart请求
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("file", file.getResource());
            parts.add("targetPath", targetPath);
            parts.add("overwrite", overwrite != null ? overwrite : false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(parts, headers);

            ResponseEntity<FileOperationResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                    FileOperationResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                FileOperationResponse result = response.getBody();
                if (result.isSuccess()) {
                    return ReturnResult.ok(result);
                } else {
                    return ReturnResult.error(result.getMessage());
                }
            } else {
                return ReturnResult.error("文件上传失败");
            }

        } catch (Exception e) {
            log.error("NODE模式文件上传异常: {}", e.getMessage(), e);
            return ReturnResult.error("文件上传异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, String fileName,
            InputStream inputStream, Boolean overwrite) {
        try {
            // 检查客户端健康状态
            ReturnResult<Boolean> healthCheck = clientHealthService.isServerClientHealthy(String.valueOf(serverId));
            if (!healthCheck.isSuccess() || !healthCheck.getData()) {
                return ReturnResult.error("客户端不在线或健康检查失败");
            }

            // 获取客户端地址
            String clientUrl = getClientUrl(String.valueOf(serverId));
            if (clientUrl == null) {
                return ReturnResult.error("无法获取客户端地址");
            }

            String url = clientUrl + "/api/file-management/upload-stream";

            // 构建multipart请求
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("inputStream", inputStream);
            parts.add("targetPath", targetPath);
            parts.add("fileName", fileName);
            parts.add("overwrite", overwrite != null ? overwrite : false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(parts, headers);

            ResponseEntity<FileOperationResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                    FileOperationResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                FileOperationResponse result = response.getBody();
                if (result.isSuccess()) {
                    return ReturnResult.ok(result);
                } else {
                    return ReturnResult.error(result.getMessage());
                }
            } else {
                return ReturnResult.error("文件流上传失败");
            }

        } catch (Exception e) {
            log.error("NODE模式文件流上传异常: {}", e.getMessage(), e);
            return ReturnResult.error("文件流上传异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> downloadFile(Integer serverId, String filePath) {
        try {
            // 检查客户端健康状态
            ReturnResult<Boolean> healthCheck = clientHealthService.isServerClientHealthy(String.valueOf(serverId));
            if (!healthCheck.isSuccess() || !healthCheck.getData()) {
                return ReturnResult.error("客户端不在线或健康检查失败");
            }

            // 获取客户端地址
            String clientUrl = getClientUrl(String.valueOf(serverId));
            if (clientUrl == null) {
                return ReturnResult.error("无法获取客户端地址");
            }

            String url = clientUrl + "/api/file-management/download";

            Map<String, Object> params = new HashMap<>();
            params.put("filePath", filePath);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<FileOperationResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                    FileOperationResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                FileOperationResponse result = response.getBody();
                if (result.isSuccess()) {
                    return ReturnResult.ok(result);
                } else {
                    return ReturnResult.error(result.getMessage());
                }
            } else {
                return ReturnResult.error("文件下载失败");
            }

        } catch (Exception e) {
            log.error("NODE模式文件下载异常: {}", e.getMessage(), e);
            return ReturnResult.error("文件下载异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> deleteFile(Integer serverId, String filePath, Boolean recursive) {
        try {
            // 检查客户端健康状态
            ReturnResult<Boolean> healthCheck = clientHealthService.isServerClientHealthy(String.valueOf(serverId));
            if (!healthCheck.isSuccess() || !healthCheck.getData()) {
                return ReturnResult.error("客户端不在线或健康检查失败");
            }

            // 获取客户端地址
            String clientUrl = getClientUrl(String.valueOf(serverId));
            if (clientUrl == null) {
                return ReturnResult.error("无法获取客户端地址");
            }

            String url = clientUrl + "/api/file-management/delete";

            Map<String, Object> params = new HashMap<>();
            params.put("filePath", filePath);
            params.put("recursive", recursive != null ? recursive : false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<FileOperationResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                    FileOperationResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                FileOperationResponse result = response.getBody();
                if (result.isSuccess()) {
                    return ReturnResult.ok(result);
                } else {
                    return ReturnResult.error(result.getMessage());
                }
            } else {
                return ReturnResult.error("文件删除失败");
            }

        } catch (Exception e) {
            log.error("NODE模式文件删除异常: {}", e.getMessage(), e);
            return ReturnResult.error("文件删除异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> renameFile(Integer serverId, String oldPath, String newName) {
        try {
            // 检查客户端健康状态
            ReturnResult<Boolean> healthCheck = clientHealthService.isServerClientHealthy(String.valueOf(serverId));
            if (!healthCheck.isSuccess() || !healthCheck.getData()) {
                return ReturnResult.error("客户端不在线或健康检查失败");
            }

            // 获取客户端地址
            String clientUrl = getClientUrl(String.valueOf(serverId));
            if (clientUrl == null) {
                return ReturnResult.error("无法获取客户端地址");
            }

            String url = clientUrl + "/api/file-management/rename";

            Map<String, Object> params = new HashMap<>();
            params.put("oldPath", oldPath);
            params.put("newName", newName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<FileOperationResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                    FileOperationResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                FileOperationResponse result = response.getBody();
                if (result.isSuccess()) {
                    return ReturnResult.ok(result);
                } else {
                    return ReturnResult.error(result.getMessage());
                }
            } else {
                return ReturnResult.error("文件重命名失败");
            }

        } catch (Exception e) {
            log.error("NODE模式文件重命名异常: {}", e.getMessage(), e);
            return ReturnResult.error("文件重命名异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> createDirectory(Integer serverId, String path, Boolean recursive) {
        try {
            // 检查客户端健康状态
            ReturnResult<Boolean> healthCheck = clientHealthService.isServerClientHealthy(String.valueOf(serverId));
            if (!healthCheck.isSuccess() || !healthCheck.getData()) {
                return ReturnResult.error("客户端不在线或健康检查失败");
            }

            // 获取客户端地址
            String clientUrl = getClientUrl(String.valueOf(serverId));
            if (clientUrl == null) {
                return ReturnResult.error("无法获取客户端地址");
            }

            String url = clientUrl + "/api/file-management/mkdir";

            Map<String, Object> params = new HashMap<>();
            params.put("path", path);
            params.put("recursive", recursive != null ? recursive : false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<FileOperationResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                    FileOperationResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                FileOperationResponse result = response.getBody();
                if (result.isSuccess()) {
                    return ReturnResult.ok(result);
                } else {
                    return ReturnResult.error(result.getMessage());
                }
            } else {
                return ReturnResult.error("目录创建失败");
            }

        } catch (Exception e) {
            log.error("NODE模式目录创建异常: {}", e.getMessage(), e);
            return ReturnResult.error("目录创建异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> executeOperation(Integer serverId, FileOperationRequest request) {
        try {
            // 检查客户端健康状态
            ReturnResult<Boolean> healthCheck = clientHealthService.isServerClientHealthy(String.valueOf(serverId));
            if (!healthCheck.isSuccess() || !healthCheck.getData()) {
                return ReturnResult.error("客户端不在线或健康检查失败");
            }

            // 获取客户端地址
            String clientUrl = getClientUrl(String.valueOf(serverId));
            if (clientUrl == null) {
                return ReturnResult.error("无法获取客户端地址");
            }

            String url = clientUrl + "/api/file-management/execute";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<FileOperationRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<FileOperationResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                    FileOperationResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                FileOperationResponse result = response.getBody();
                if (result.isSuccess()) {
                    return ReturnResult.ok(result);
                } else {
                    return ReturnResult.error(result.getMessage());
                }
            } else {
                return ReturnResult.error("操作执行失败");
            }

        } catch (Exception e) {
            log.error("NODE模式操作执行异常: {}", e.getMessage(), e);
            return ReturnResult.error("操作执行异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> testConnection(Integer serverId) {
        try {
            // 检查客户端健康状态
            ReturnResult<Boolean> healthCheck = clientHealthService.isServerClientHealthy(String.valueOf(serverId));
            if (!healthCheck.isSuccess() || !healthCheck.getData()) {
                return ReturnResult.error("客户端不在线或健康检查失败");
            }

            // 获取客户端地址
            String clientUrl = getClientUrl(String.valueOf(serverId));
            if (clientUrl == null) {
                return ReturnResult.error("无法获取客户端地址");
            }

            String url = clientUrl + "/api/file-management/test";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<FileOperationResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity,
                    FileOperationResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                FileOperationResponse result = response.getBody();
                return ReturnResult.ok(result.isSuccess());
            } else {
                return ReturnResult.ok(false);
            }

        } catch (Exception e) {
            log.error("NODE模式连接测试异常: {}", e.getMessage(), e);
            return ReturnResult.ok(false);
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> copyFile(Integer serverId, String sourcePath, String targetPath,
            Boolean overwrite) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> moveFile(Integer serverId, String sourcePath, String targetPath,
            Boolean overwrite) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> changePermissions(Integer serverId, String path, String permissions,
            Boolean recursive) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> changeOwner(Integer serverId, String path, String owner, String group,
            Boolean recursive) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> searchFiles(Integer serverId, String searchPath, String pattern,
            Boolean includeContent, Integer maxResults) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> compressFiles(Integer serverId, List<String> filePaths,
            String archivePath, String format) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> extractArchive(Integer serverId, String archivePath, String extractPath,
            Boolean overwrite) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> previewFile(Integer serverId, String filePath, String encoding,
            Long maxSize) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<FileInfo> getFileInfo(Integer serverId, String path) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> batchDeleteFiles(Integer serverId, List<String> paths,
            Boolean recursive) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<List<String>> getSupportedOperations(Integer serverId) {
        return ReturnResult.ok(List.of("listFiles", "getFileTree", "uploadFile", "downloadFile", "deleteFile",
                "renameFile", "createDirectory", "testConnection"));
    }

    @Override
    public ReturnResult<FileOperationResponse> getDiskUsage(Integer serverId, String path) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileStatistics(Integer serverId, String path,
            Boolean includeSubdirectories) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<String> watchFileChanges(Integer serverId, String path, Boolean recursive) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<Boolean> stopWatching(Integer serverId, String watchId) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<String> getFileChecksum(Integer serverId, String filePath, String algorithm) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> syncFiles(Integer serverId, String sourcePath, String targetPath,
            Boolean deleteExtra) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> createSymbolicLink(Integer serverId, String targetPath,
            String linkPath) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileSystemInfo(Integer serverId) {
        return ReturnResult.error("NODE模式暂不支持此操作");
    }

    /**
     * 获取客户端地址并验证健康状态
     *
     * @param serverId 服务器ID
     * @return 客户端地址结果
     */
    private ReturnResult<String> getClientAddress(Integer serverId) {
        try {
            // 1. 获取服务器信息
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在: " + serverId);
            }

            // 2. 获取服务器配置
            ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return ReturnResult.error("服务器配置不存在");
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            String fileManagementModeStr = setting.getMonitorSysGenServerSettingFileManagementMode();

            if (!"NODE".equals(fileManagementModeStr)) {
                return ReturnResult.error("服务器文件管理模式不是NODE模式，当前模式: " + fileManagementModeStr);
            }

            // 3. 获取节点名称（存储在配置中）
            String nodeName = setting.getMonitorSysGenServerSettingFileManagementNodeClient();
            if (nodeName == null || nodeName.trim().isEmpty()) {
                return ReturnResult.error("节点名称未配置");
            }

            // 4. 根据节点名称随机获取一个可用的地址
            ReturnResult<String> addressResult = getRandomNodeAddress(nodeName);
            if (!addressResult.isOk()) {
                return addressResult;
            }

            return ReturnResult.ok(addressResult.getData());

        } catch (Exception e) {
            log.error("获取客户端地址失败: serverId={}", serverId, e);
            return ReturnResult.error("获取客户端地址失败: " + e.getMessage());
        }
    }

    /**
     * 根据节点名称随机获取一个可用的地址 NODE 模式的核心逻辑：一个节点名称对应多个地址，随机选择一个
     *
     * @param nodeName 节点名称
     * @return 随机选择的地址
     */
    private ReturnResult<String> getRandomNodeAddress(String nodeName) {
        try {
            // 从 discoveryService 获取指定节点名称的所有在线实例
            Set<Discovery> discoveries = discoveryService.getDiscoveryAll("monitor");
            if (discoveries == null || discoveries.isEmpty()) {
                return ReturnResult.error("没有找到在线的监控节点");
            }

            // 过滤出匹配节点名称的实例
            List<Discovery> matchingNodes = discoveries.stream().filter(discovery -> {
                Map<String, String> metadata = discovery.getMetadata();
                if (metadata == null) {
                    return false;
                }
                String applicationName = metadata.get("applicationName");
                return nodeName.equals(applicationName);
            }).collect(Collectors.toList());

            if (matchingNodes.isEmpty()) {
                return ReturnResult.error("没有找到节点名称为 '" + nodeName + "' 的在线实例");
            }

            // 随机选择一个实例
            Random random = new Random();
            Discovery selectedNode = matchingNodes.get(random.nextInt(matchingNodes.size()));

            // 构建地址
            String address = "http://" + selectedNode.getHost() + ":" + selectedNode.getPort();

            log.debug("为节点 '{}' 随机选择了地址: {} (共有 {} 个可用实例)", nodeName, address, matchingNodes.size());

            return ReturnResult.ok(address);

        } catch (Exception e) {
            log.error("获取节点地址失败: nodeName={}", nodeName, e);
            return ReturnResult.error("获取节点地址失败: " + e.getMessage());
        }
    }
}
