package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.file.FileInfo;
import com.chua.starter.monitor.pojo.file.FileOperationRequest;
import com.chua.starter.monitor.pojo.file.FileOperationResponse;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.file.FileManagementService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerSettingService;
import com.chua.sync.support.pojo.ClientInfo;
import com.chua.sync.support.server.SyncServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;

/**
 * NODE模式文件管理服务实现
 * <p>
 * 通过 SyncProtocol 与在线客户端通信进行文件管理
 * </p>
 *
 * @author CH
 * @since 2024/12/13
 */
@Slf4j
@Service("nodeFileManagementServiceImpl")
public class NodeFileManagementServiceImpl implements FileManagementService {

    private static final String TOPIC_FILE_REQUEST = "monitor/file/request";
    private static final long REQUEST_TIMEOUT = 30000;

    @Autowired
    private MonitorSysGenServerSettingService serverSettingService;

    @Autowired(required = false)
    private SyncServer syncServer;

    private final Map<String, CompletableFuture<Map<String, Object>>> pendingRequests = new ConcurrentHashMap<>();
    private volatile boolean responseHandlerRegistered = false;

    @PostConstruct
    public void init() {
        registerResponseHandler();
    }

    // ==================== 核心方法 ====================

    private Map<String, Object> sendRequest(String clientId, Map<String, Object> request) {
        if (syncServer == null) {
            return null;
        }

        String requestId = UUID.randomUUID().toString();
        request.put("requestId", requestId);

        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);

        try {
            syncServer.send(clientId, TOPIC_FILE_REQUEST, request);
            return future.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("[NodeFile] 请求超时: requestId={}", requestId);
            return null;
        } catch (Exception e) {
            log.error("[NodeFile] 发送请求失败: requestId={}", requestId, e);
            return null;
        } finally {
            pendingRequests.remove(requestId);
        }
    }

    private synchronized void registerResponseHandler() {
        if (responseHandlerRegistered || syncServer == null) {
            return;
        }
        syncServer.registerHandler("monitor/file/response", new com.chua.sync.support.spi.SyncMessageHandler() {
            @Override
            public String getName() {
                return "fileResponseHandler";
            }

            @Override
            public Object handle(String topic, String sessionId, Map<String, Object> data) {
                String requestId = getString(data, "requestId");
                if (requestId != null) {
                    CompletableFuture<Map<String, Object>> future = pendingRequests.get(requestId);
                    if (future != null) {
                        future.complete(data);
                    }
                }
                return null;
            }
        });
        responseHandlerRegistered = true;
    }

    private String getClientId(Integer serverId) {
        if (syncServer == null) {
            return null;
        }
        try {
            ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return null;
            }

            String nodeClientFunction = settingResult.getData().getMonitorSysGenServerSettingFileManagementNodeClient();
            if (nodeClientFunction == null || nodeClientFunction.trim().isEmpty()) {
                return null;
            }

            for (Map.Entry<String, ClientInfo> entry : syncServer.getAllClients().entrySet()) {
                if (nodeClientFunction.equals(entry.getValue().getClientApplicationName())) {
                    return entry.getKey();
                }
            }
            return null;
        } catch (Exception e) {
            log.error("[NodeFile] 获取客户端ID失败: serverId={}", serverId, e);
            return null;
        }
    }

    // ==================== FileManagementService 实现 ====================

    @Override
    public ReturnResult<FileOperationResponse> listFiles(Integer serverId, String path, Boolean includeHidden,
                                                         String sortBy, String sortOrder) {
        String clientId = getClientId(serverId);
        if (clientId == null) {
            return ReturnResult.error("未找到在线的客户端");
        }

        Map<String, Object> request = new HashMap<>();
        request.put("operation", "list");
        request.put("path", path != null ? path : "/");
        request.put("includeHidden", includeHidden != null ? includeHidden : false);

        Map<String, Object> response = sendRequest(clientId, request);
        if (response == null) {
            return ReturnResult.error("请求超时");
        }

        if (getBoolean(response, "success", false)) {
            List<FileInfo> files = parseFileList(response);
            return ReturnResult.ok(FileOperationResponse.success("LIST", files, path));
        }
        return ReturnResult.error(getString(response, "message"));
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth,
                                                            Boolean includeHidden) {
        return getFileTree(serverId, path, maxDepth, includeHidden, false, 1000, 0);
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth,
                                                            Boolean includeHidden, Boolean lazyLoad, Integer pageSize, Integer pageIndex) {
        String clientId = getClientId(serverId);
        if (clientId == null) {
            return ReturnResult.error("未找到在线的客户端");
        }

        Map<String, Object> request = new HashMap<>();
        request.put("operation", "tree");
        request.put("path", path != null ? path : "/");
        request.put("maxDepth", maxDepth != null ? maxDepth : 3);
        request.put("includeHidden", includeHidden != null ? includeHidden : false);

        Map<String, Object> response = sendRequest(clientId, request);
        if (response == null) {
            return ReturnResult.error("请求超时");
        }

        if (getBoolean(response, "success", false)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> tree = (Map<String, Object>) response.get("tree");
            List<FileInfo> files = tree != null ? List.of(mapToFileInfo(tree)) : Collections.emptyList();
            return ReturnResult.ok(FileOperationResponse.success("TREE", files, path));
        }
        return ReturnResult.error(getString(response, "message"));
    }

    @Override
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, MultipartFile file,
                                                           Boolean overwrite) {
        try {
            return uploadFile(serverId, targetPath, file.getOriginalFilename(), file.getInputStream(), overwrite);
        } catch (Exception e) {
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, String fileName,
                                                           InputStream inputStream, Boolean overwrite) {
        String clientId = getClientId(serverId);
        if (clientId == null) {
            return ReturnResult.error("未找到在线的客户端");
        }

        try {
            byte[] content = inputStream.readAllBytes();
            String fullPath = targetPath.endsWith("/") ? targetPath + fileName : targetPath + "/" + fileName;

            Map<String, Object> request = new HashMap<>();
            request.put("operation", "write");
            request.put("path", fullPath);
            request.put("content", Base64.getEncoder().encodeToString(content));
            request.put("overwrite", overwrite != null ? overwrite : false);

            Map<String, Object> response = sendRequest(clientId, request);
            if (response == null) {
                return ReturnResult.error("请求超时");
            }

            if (getBoolean(response, "success", false)) {
                FileOperationResponse opResponse = FileOperationResponse.success("UPLOAD", "文件上传成功");
                opResponse.setPath(fullPath);
                return ReturnResult.ok(opResponse);
            }
            return ReturnResult.error(getString(response, "message"));
        } catch (Exception e) {
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> downloadFile(Integer serverId, String filePath) {
        String clientId = getClientId(serverId);
        if (clientId == null) {
            return ReturnResult.error("未找到在线的客户端");
        }

        Map<String, Object> request = new HashMap<>();
        request.put("operation", "read");
        request.put("path", filePath);

        Map<String, Object> response = sendRequest(clientId, request);
        if (response == null) {
            return ReturnResult.error("请求超时");
        }

        if (getBoolean(response, "success", false)) {
            String contentBase64 = getString(response, "content");
            byte[] content = Base64.getDecoder().decode(contentBase64);
            FileOperationResponse resp = FileOperationResponse.success("DOWNLOAD", "文件下载成功");
            resp.setPath(filePath);
            resp.setContentBytes(content);
            return ReturnResult.ok(resp);
        }
        return ReturnResult.error(getString(response, "message"));
    }

    @Override
    public ReturnResult<FileOperationResponse> deleteFile(Integer serverId, String path, Boolean recursive) {
        return executeSimpleOperation(serverId, "delete", Map.of("path", path, "recursive", recursive != null ? recursive : false));
    }

    @Override
    public ReturnResult<FileOperationResponse> renameFile(Integer serverId, String oldPath, String newName) {
        return executeSimpleOperation(serverId, "rename", Map.of("oldPath", oldPath, "newName", newName));
    }

    @Override
    public ReturnResult<FileOperationResponse> createDirectory(Integer serverId, String path, Boolean recursive) {
        return executeSimpleOperation(serverId, "mkdir", Map.of("path", path, "recursive", recursive != null ? recursive : true));
    }

    @Override
    public ReturnResult<Boolean> testConnection(Integer serverId) {
        String clientId = getClientId(serverId);
        if (clientId == null) {
            return ReturnResult.error("未找到在线的客户端");
        }

        Map<String, Object> request = new HashMap<>();
        request.put("operation", "exists");
        request.put("path", "/");

        Map<String, Object> response = sendRequest(clientId, request);
        return ReturnResult.ok(response != null && getBoolean(response, "success", false));
    }

    @Override
    public ReturnResult<FileOperationResponse> copyFile(Integer serverId, String sourcePath, String targetPath, Boolean overwrite) {
        return executeSimpleOperation(serverId, "copy", Map.of("sourcePath", sourcePath, "targetPath", targetPath, "overwrite", overwrite != null ? overwrite : false));
    }

    @Override
    public ReturnResult<FileOperationResponse> moveFile(Integer serverId, String sourcePath, String targetPath, Boolean overwrite) {
        return executeSimpleOperation(serverId, "move", Map.of("sourcePath", sourcePath, "targetPath", targetPath, "overwrite", overwrite != null ? overwrite : false));
    }

    @Override
    public ReturnResult<FileOperationResponse> getDiskUsage(Integer serverId, String path) {
        return executeSimpleOperation(serverId, "diskUsage", Map.of("path", path));
    }

    @Override
    public ReturnResult<FileInfo> getFileInfo(Integer serverId, String path) {
        String clientId = getClientId(serverId);
        if (clientId == null) {
            return ReturnResult.error("未找到在线的客户端");
        }

        Map<String, Object> request = new HashMap<>();
        request.put("operation", "info");
        request.put("path", path);

        Map<String, Object> response = sendRequest(clientId, request);
        if (response != null && getBoolean(response, "success", false)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fileMap = (Map<String, Object>) response.get("file");
            return ReturnResult.ok(mapToFileInfo(fileMap));
        }
        return ReturnResult.error(response != null ? getString(response, "message") : "请求超时");
    }

    @Override
    public ReturnResult<FileOperationResponse> batchDeleteFiles(Integer serverId, List<String> paths, Boolean recursive) {
        for (String path : paths) {
            ReturnResult<FileOperationResponse> result = deleteFile(serverId, path, recursive);
            if (!result.isOk()) {
                return result;
            }
        }
        return ReturnResult.ok(FileOperationResponse.success("BATCH_DELETE", null));
    }

    @Override
    public ReturnResult<List<String>> getSupportedOperations(Integer serverId) {
        return ReturnResult.ok(List.of("list", "tree", "read", "write", "delete", "mkdir", "rename", "copy", "move", "exists", "info", "diskUsage"));
    }

    // ==================== 暂不支持的操作 ====================

    @Override
    public ReturnResult<FileOperationResponse> changePermissions(Integer serverId, String path, String permissions, Boolean recursive) {
        return ReturnResult.error("暂不支持");
    }

    @Override
    public ReturnResult<FileOperationResponse> changeOwner(Integer serverId, String path, String owner, String group, Boolean recursive) {
        return ReturnResult.error("暂不支持");
    }

    @Override
    public ReturnResult<FileOperationResponse> searchFiles(Integer serverId, String path, String pattern, Boolean includeContent, Integer maxResults) {
        return ReturnResult.error("暂不支持");
    }

    @Override
    public ReturnResult<FileOperationResponse> compressFiles(Integer serverId, List<String> filePaths, String archivePath, String format) {
        return ReturnResult.error("暂不支持");
    }

    @Override
    public ReturnResult<FileOperationResponse> extractArchive(Integer serverId, String archivePath, String extractPath, Boolean overwrite) {
        return ReturnResult.error("暂不支持");
    }

    @Override
    public ReturnResult<FileOperationResponse> previewFile(Integer serverId, String filePath, String previewType, Integer maxSizeMB) {
        return downloadFile(serverId, filePath);
    }

    @Override
    public ReturnResult<FileOperationResponse> executeOperation(Integer serverId, FileOperationRequest request) {
        return ReturnResult.error("暂不支持");
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileStatistics(Integer serverId, String path, Boolean includeSubdirectories) {
        return ReturnResult.error("暂不支持");
    }

    @Override
    public ReturnResult<String> watchFileChanges(Integer serverId, String path, Boolean recursive) {
        return ReturnResult.error("暂不支持");
    }

    @Override
    public ReturnResult<Boolean> stopWatching(Integer serverId, String watchId) {
        return ReturnResult.error("暂不支持");
    }

    @Override
    public ReturnResult<String> getFileChecksum(Integer serverId, String filePath, String algorithm) {
        return ReturnResult.error("暂不支持");
    }

    @Override
    public ReturnResult<FileOperationResponse> syncFiles(Integer serverId, String sourcePath, String targetPath, Boolean deleteExtra) {
        return ReturnResult.error("暂不支持");
    }

    @Override
    public ReturnResult<FileOperationResponse> createSymbolicLink(Integer serverId, String targetPath, String linkPath) {
        return ReturnResult.error("暂不支持");
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileSystemInfo(Integer serverId) {
        return getDiskUsage(serverId, "/");
    }

    // ==================== 辅助方法 ====================

    private ReturnResult<FileOperationResponse> executeSimpleOperation(Integer serverId, String operation, Map<String, Object> params) {
        String clientId = getClientId(serverId);
        if (clientId == null) {
            return ReturnResult.error("未找到在线的客户端");
        }

        Map<String, Object> request = new HashMap<>(params);
        request.put("operation", operation);

        Map<String, Object> response = sendRequest(clientId, request);
        if (response == null) {
            return ReturnResult.error("请求超时");
        }

        if (getBoolean(response, "success", false)) {
            return ReturnResult.ok(FileOperationResponse.success(operation.toUpperCase(), null));
        }
        return ReturnResult.error(getString(response, "message"));
    }

    @SuppressWarnings("unchecked")
    private List<FileInfo> parseFileList(Map<String, Object> response) {
        Object filesObj = response.get("files");
        if (filesObj instanceof List) {
            return ((List<Map<String, Object>>) filesObj).stream()
                    .map(this::mapToFileInfo)
                    .toList();
        }
        return Collections.emptyList();
    }

    private FileInfo mapToFileInfo(Map<String, Object> map) {
        FileInfo info = new FileInfo();
        info.setName(getString(map, "name"));
        info.setPath(getString(map, "path"));
        info.setDirectory(getBoolean(map, "isDirectory", false));
        info.setFile(!getBoolean(map, "isDirectory", false));
        info.setSize(getLong(map, "size", 0L));
        // 将时间戳转换为 LocalDateTime
        long lastModified = getLong(map, "lastModified", 0L);
        if (lastModified > 0) {
            info.setModifiedTime(java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(lastModified),
                    java.time.ZoneId.systemDefault()));
        }
        info.setHidden(getBoolean(map, "hidden", false));
        info.setReadable(getBoolean(map, "readable", true));
        info.setWritable(getBoolean(map, "writable", true));
        return info;
    }

    private String getString(Map<String, Object> data, String key) {
        Object v = data.get(key);
        return v != null ? v.toString() : null;
    }

    private boolean getBoolean(Map<String, Object> data, String key, boolean defaultValue) {
        Object v = data.get(key);
        if (v instanceof Boolean) return (Boolean) v;
        if (v != null) return Boolean.parseBoolean(v.toString());
        return defaultValue;
    }

    private long getLong(Map<String, Object> data, String key, long defaultValue) {
        Object v = data.get(key);
        if (v instanceof Number) return ((Number) v).longValue();
        if (v != null) {
            try {
                return Long.parseLong(v.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
}
