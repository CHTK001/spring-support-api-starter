package com.chua.starter.monitor.starter.service.impl;

import com.chua.starter.monitor.starter.utils.MonitorSysGenServerSettingUtils;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.pojo.*;
import com.chua.starter.monitor.starter.service.FileManagementService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.starter.service.ClientHealthService;
import com.chua.common.support.lang.code.ReturnResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Base64;

/**
 * API模式文件管理服务实现 通过HTTP API调用客户端代理进行文件管理
 * 
 * @author CH
 * @since 2024/12/13
 */
@Slf4j
@Service("apiFileManagementService")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "monitor.file-management.api.enabled", havingValue = "true", matchIfMissing = true)
public class ApiFileManagementServiceImpl implements FileManagementService {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerSettingService serverSettingService;
    private final ClientHealthService clientHealthService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 缓存API配置以提高性能
    private final Map<Integer, FileManagementApiConfig> configCache = new ConcurrentHashMap<>();
    private final Map<Integer, Long> configCacheTime = new ConcurrentHashMap<>();
    private static final long CACHE_TIMEOUT = 5 * 60 * 1000; // 5分钟缓存

    @Override
    public ReturnResult<FileOperationResponse> listFiles(Integer serverId, String path, Boolean includeHidden,
                                                         String sortBy, String sortOrder) {
        long startTime = System.currentTimeMillis();

        try {
            // 验证客户端健康状态
            ReturnResult<Boolean> healthCheck = validateClientHealth(serverId);
            if (!healthCheck.isOk()) {
                return ReturnResult.error(healthCheck.getMsg());
            }

            FileManagementApiConfig config = getApiConfig(serverId);
            if (config == null) {
                return ReturnResult.error("无法获取API配置");
            }

            // 构建请求参数
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("path", path != null ? path : "/");
            requestParams.put("includeHidden", includeHidden != null ? includeHidden : false);
            requestParams.put("sortBy", sortBy != null ? sortBy : "name");
            requestParams.put("sortOrder", sortOrder != null ? sortOrder : "asc");

            // 发送API请求
            String url = config.buildFileOperationUrl("list");
            ResponseEntity<Map> response = sendApiRequest(config, url, HttpMethod.GET, requestParams, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<FileInfo> files = parseFileList(responseBody);

                FileOperationResponse operationResponse = FileOperationResponse.success("LIST", files, path)
                        .withDuration(startTime);

                log.info("API文件列表获取成功: serverId={}, path={}, count={}", serverId, path, files.size());
                return ReturnResult.ok(operationResponse);
            } else {
                return ReturnResult.error("API请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("API文件列表获取失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("API文件列表获取失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth,
                                                           Boolean includeHidden) {
        long startTime = System.currentTimeMillis();

        try {
            // 验证客户端健康状态
            ReturnResult<Boolean> healthCheck = validateClientHealth(serverId);
            if (!healthCheck.isOk()) {
                return ReturnResult.error(healthCheck.getMsg());
            }

            FileManagementApiConfig config = getApiConfig(serverId);
            if (config == null) {
                return ReturnResult.error("无法获取API配置");
            }

            // 构建请求参数
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("path", path != null ? path : "/");
            requestParams.put("maxDepth", maxDepth != null ? maxDepth : 3);
            requestParams.put("includeHidden", includeHidden != null ? includeHidden : false);

            // 发送API请求
            String url = config.buildFileOperationUrl("tree");
            ResponseEntity<Map> response = sendApiRequest(config, url, HttpMethod.GET, requestParams, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                FileInfo tree = parseFileTree(responseBody);

                FileOperationResponse operationResponse = FileOperationResponse.success("TREE", tree, path)
                        .withDuration(startTime);

                log.info("API文件树获取成功: serverId={}, path={}", serverId, path);
                return ReturnResult.ok(operationResponse);
            } else {
                return ReturnResult.error("API请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("API文件树获取失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("API文件树获取失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, MultipartFile file,
                                                          Boolean overwrite) {
        try {
            return uploadFile(serverId, targetPath, file.getOriginalFilename(), file.getInputStream(), overwrite);
        } catch (Exception e) {
            log.error("API文件上传失败: serverId={}, targetPath={}, fileName={}", serverId, targetPath,
                    file.getOriginalFilename(), e);
            return ReturnResult.error("API文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, String fileName,
                                                          InputStream inputStream, Boolean overwrite) {
        long startTime = System.currentTimeMillis();

        try {
            // 验证客户端健康状态
            ReturnResult<Boolean> healthCheck = validateClientHealth(serverId);
            if (!healthCheck.isOk()) {
                return ReturnResult.error(healthCheck.getMsg());
            }

            FileManagementApiConfig config = getApiConfig(serverId);
            if (config == null) {
                return ReturnResult.error("无法获取API配置");
            }

            // 构建文件上传请求
            String url = config.buildFileOperationUrl("upload");

            // 创建multipart请求
            HttpHeaders headers = createApiHeaders(config);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("targetPath", targetPath);
            body.add("fileName", fileName);
            body.add("overwrite", overwrite != null ? overwrite : false);

            // 添加文件流
            body.add("file", new InputStreamResource(inputStream, fileName));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                FileOperationResponse operationResponse = FileOperationResponse.success("UPLOAD", "文件上传成功")
                        .withDuration(startTime);

                log.info("API文件上传成功: serverId={}, targetPath={}, fileName={}", serverId, targetPath, fileName);
                return ReturnResult.ok(operationResponse);
            } else {
                return ReturnResult.error("文件上传失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("API文件上传失败: serverId={}, targetPath={}, fileName={}", serverId, targetPath, fileName, e);
            return ReturnResult.error("API文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> downloadFile(Integer serverId, String filePath) {
        try {
            // 验证客户端健康状态
            ReturnResult<Boolean> healthCheck = validateClientHealth(serverId);
            if (!healthCheck.isOk()) {
                return ReturnResult.error(healthCheck.getMsg());
            }

            FileManagementApiConfig config = getApiConfig(serverId);
            if (config == null) {
                return ReturnResult.error("无法获取API配置");
            }

            // 构建下载请求
            String url = config.buildFileOperationUrl("download");

            HttpHeaders headers = createApiHeaders(config);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));

            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("filePath", filePath);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestParams, headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 创建文件操作响应，包含下载URL或文件内容
                FileOperationResponse fileResponse = FileOperationResponse.builder().success(true).operation("DOWNLOAD")
                        .message("文件下载成功").downloadUrl("data:application/octet-stream;base64,"
                                + java.util.Base64.getEncoder().encodeToString(response.getBody()))
                        .build();

                log.info("API文件下载成功: serverId={}, filePath={}", serverId, filePath);
                return ReturnResult.ok(fileResponse);
            } else {
                return ReturnResult.error("文件下载失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("API文件下载失败: serverId={}, filePath={}", serverId, filePath, e);
            return ReturnResult.error("API文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> deleteFile(Integer serverId, String path, Boolean recursive) {
        return executeSimpleOperation(serverId, "delete", path,
                Map.of("recursive", recursive != null ? recursive : false));
    }

    @Override
    public ReturnResult<FileOperationResponse> renameFile(Integer serverId, String oldPath, String newName) {
        return executeSimpleOperation(serverId, "rename", oldPath, Map.of("newName", newName));
    }

    @Override
    public ReturnResult<FileOperationResponse> createDirectory(Integer serverId, String path, Boolean recursive) {
        return executeSimpleOperation(serverId, "mkdir", path,
                Map.of("recursive", recursive != null ? recursive : false));
    }

    @Override
    public ReturnResult<FileOperationResponse> copyFile(Integer serverId, String sourcePath, String targetPath,
                                                        Boolean overwrite) {
        Map<String, Object> params = new HashMap<>();
        params.put("targetPath", targetPath);
        params.put("overwrite", overwrite != null ? overwrite : false);
        return executeSimpleOperation(serverId, "copy", sourcePath, params);
    }

    @Override
    public ReturnResult<FileOperationResponse> moveFile(Integer serverId, String sourcePath, String targetPath,
                                                        Boolean overwrite) {
        Map<String, Object> params = new HashMap<>();
        params.put("targetPath", targetPath);
        params.put("overwrite", overwrite != null ? overwrite : false);
        return executeSimpleOperation(serverId, "move", sourcePath, params);
    }

    @Override
    public ReturnResult<FileOperationResponse> changePermissions(Integer serverId, String path, String permissions,
                                                                 Boolean recursive) {
        return ReturnResult.error("API模式暂不支持权限修改");
    }

    @Override
    public ReturnResult<FileOperationResponse> changeOwner(Integer serverId, String path, String owner, String group,
                                                           Boolean recursive) {
        return ReturnResult.error("API模式暂不支持所有者修改");
    }

    @Override
    public ReturnResult<FileOperationResponse> searchFiles(Integer serverId, String searchPath, String pattern,
                                                           Boolean includeContent, Integer maxResults) {
        Map<String, Object> params = new HashMap<>();
        params.put("pattern", pattern);
        params.put("includeContent", includeContent != null ? includeContent : false);
        params.put("maxResults", maxResults != null ? maxResults : 100);
        return executeSimpleOperation(serverId, "search", searchPath, params);
    }

    @Override
    public ReturnResult<FileOperationResponse> previewFile(Integer serverId, String filePath, String encoding,
                                                           Long maxSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("encoding", encoding != null ? encoding : "UTF-8");
        params.put("maxSize", maxSize != null ? maxSize : 1024 * 1024);
        return executeSimpleOperation(serverId, "preview", filePath, params);
    }

    @Override
    public ReturnResult<FileInfo> getFileInfo(Integer serverId, String path) {
        try {
            // 验证客户端健康状态
            ReturnResult<Boolean> healthCheck = validateClientHealth(serverId);
            if (!healthCheck.isOk()) {
                return ReturnResult.error(healthCheck.getMsg());
            }

            FileManagementApiConfig config = getApiConfig(serverId);
            if (config == null) {
                return ReturnResult.error("无法获取API配置");
            }

            // 构建请求参数
            Map<String, Object> requestParams = Map.of("path", path);

            // 发送API请求
            String url = config.buildFileOperationUrl("info");
            ResponseEntity<Map> response = sendApiRequest(config, url, HttpMethod.GET, requestParams, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                FileInfo fileInfo = parseFileInfo(response.getBody());
                return ReturnResult.ok(fileInfo);
            } else {
                return ReturnResult.error("获取文件信息失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("API获取文件信息失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("API获取文件信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> getDiskUsage(Integer serverId, String path) {
        return executeSimpleOperation(serverId, "diskUsage", path, Map.of());
    }

    @Override
    public ReturnResult<FileOperationResponse> compressFiles(Integer serverId, List<String> paths, String archivePath,
                                                             String format) {
        Map<String, Object> params = new HashMap<>();
        params.put("paths", paths);
        params.put("archivePath", archivePath);
        params.put("format", format != null ? format : "zip");
        return executeSimpleOperation(serverId, "compress", null, params);
    }

    @Override
    public ReturnResult<FileOperationResponse> extractArchive(Integer serverId, String archivePath, String extractPath,
                                                              Boolean overwrite) {
        Map<String, Object> params = new HashMap<>();
        params.put("extractPath", extractPath);
        params.put("overwrite", overwrite != null ? overwrite : false);
        return executeSimpleOperation(serverId, "extract", archivePath, params);
    }

    @Override
    public ReturnResult<FileOperationResponse> executeOperation(Integer serverId, FileOperationRequest request) {
        request.validate();

        switch (FileOperationRequest.OperationType.fromCode(request.getOperation())) {
            case LIST:
                return listFiles(serverId, request.getPath(), request.getIncludeHiddenOrDefault(),
                        request.getSortByOrDefault(), request.getSortOrderOrDefault());
            case TREE:
                return getFileTree(serverId, request.getPath(), request.getMaxDepthOrDefault(),
                        request.getIncludeHiddenOrDefault());
            case DELETE:
                return deleteFile(serverId, request.getPath(), request.getRecursiveOrDefault());
            case RENAME:
                return renameFile(serverId, request.getPath(), request.getNewName());
            case MKDIR:
                return createDirectory(serverId, request.getPath(), request.getRecursiveOrDefault());
            case COPY:
                return copyFile(serverId, request.getPath(), request.getTargetPath(), request.getOverwriteOrDefault());
            case MOVE:
                return moveFile(serverId, request.getPath(), request.getTargetPath(), request.getOverwriteOrDefault());
            case SEARCH:
                return searchFiles(serverId, request.getPath(), request.getPattern(), request.getIncludeContentOrDefault(),
                        request.getMaxResultsOrDefault());
            case PREVIEW:
                return previewFile(serverId, request.getPath(), request.getEncoding(), request.getMaxSize());
            case COMPRESS:
                return compressFiles(serverId, request.getPaths(), request.getArchivePath(), request.getFormat());
            case EXTRACT:
                return extractArchive(serverId, request.getPath(), request.getExtractPath(),
                        request.getOverwriteOrDefault());
            default:
                return ReturnResult.error("不支持的操作类型: " + request.getOperation());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> batchDeleteFiles(Integer serverId, List<String> paths,
                                                                Boolean recursive) {
        Map<String, Object> params = new HashMap<>();
        params.put("paths", paths);
        params.put("recursive", recursive != null ? recursive : false);
        return executeSimpleOperation(serverId, "batchDelete", null, params);
    }

    @Override
    public ReturnResult<List<String>> getSupportedOperations(Integer serverId) {
        List<String> operations = Arrays.asList("LIST", "TREE", "UPLOAD", "DOWNLOAD", "DELETE", "RENAME", "MKDIR",
                "COPY", "MOVE", "SEARCH", "PREVIEW", "COMPRESS", "EXTRACT", "INFO");
        return ReturnResult.ok(operations);
    }

    @Override
    public ReturnResult<Boolean> testConnection(Integer serverId) {
        try {
            // 验证客户端健康状态
            ReturnResult<Boolean> healthCheck = validateClientHealth(serverId);
            if (!healthCheck.isOk()) {
                return ReturnResult.error(healthCheck.getMsg());
            }

            FileManagementApiConfig config = getApiConfig(serverId);
            if (config == null) {
                return ReturnResult.error("无法获取API配置");
            }

            // 测试健康检查端点
            String healthUrl = config.getHealthCheckUrl();
            if (healthUrl != null) {
                HttpHeaders headers = createApiHeaders(config);
                HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(healthUrl, HttpMethod.GET, requestEntity,
                        String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    return ReturnResult.ok(true);
                }
            }

            return ReturnResult.ok(false);

        } catch (Exception e) {
            log.error("API连接测试失败: serverId={}", serverId, e);
            return ReturnResult.error("API连接测试失败: " + e.getMessage());
        }
    }

    // 辅助方法
    private ReturnResult<Boolean> validateClientHealth(Integer serverId) {
        try {
            return clientHealthService.isServerClientHealthy(serverId);
        } catch (Exception e) {
            log.error("客户端健康状态检查失败: serverId={}", serverId, e);
            return ReturnResult.error("客户端健康状态检查失败: " + e.getMessage());
        }
    }

    private FileManagementApiConfig getApiConfig(Integer serverId) {
        try {
            // 检查缓存
            Long cacheTime = configCacheTime.get(serverId);
            if (cacheTime != null && System.currentTimeMillis() - cacheTime < CACHE_TIMEOUT) {
                return configCache.get(serverId);
            }

            // 从数据库获取配置
            ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return null;
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            if (!"API".equals(MonitorSysGenServerSettingUtils.getFileManagementMode(setting))) {
                return null;
            }

            String configJson = setting.getMonitorSysGenServerSettingFileManagementApiConfig();
            if (configJson == null || configJson.trim().isEmpty()) {
                return null;
            }

            FileManagementApiConfig config = objectMapper.readValue(configJson, FileManagementApiConfig.class);

            // 缓存配置
            configCache.put(serverId, config);
            configCacheTime.put(serverId, System.currentTimeMillis());

            return config;

        } catch (Exception e) {
            log.error("获取API配置失败: serverId={}", serverId, e);
            return null;
        }
    }

    private HttpHeaders createApiHeaders(FileManagementApiConfig config) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        // 添加认证信息
        FileManagementApiConfig.AuthType authType = config.getAuthTypeEnum();
        switch (authType) {
            case BASIC:
                if (config.getUsername() != null && config.getPassword() != null) {
                    String auth = config.getUsername() + ":" + config.getPassword();
                    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                    headers.set("Authorization", "Basic " + encodedAuth);
                }
                break;
            case TOKEN:
                if (config.getToken() != null) {
                    headers.set("Authorization", "Bearer " + config.getToken());
                }
                break;
            case API_KEY:
                if (config.getApiKey() != null && config.getApiKeyHeader() != null) {
                    headers.set(config.getApiKeyHeader(), config.getApiKey());
                }
                break;
            case NONE:
            default:
                // 无需认证
                break;
        }

        // 添加自定义请求头
        Map<String, String> customHeaders = config.getCustomHeadersOrDefault();
        for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
            headers.set(entry.getKey(), entry.getValue());
        }

        return headers;
    }

    private <T> ResponseEntity<T> sendApiRequest(FileManagementApiConfig config, String url, HttpMethod method,
                                                 Map<String, Object> params, Class<T> responseType) {

        HttpHeaders headers = createApiHeaders(config);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(params, headers);

        int maxRetries = config.getMaxRetriesOrDefault();
        long retryInterval = config.getRetryIntervalOrDefault();

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return restTemplate.exchange(url, method, requestEntity, responseType);
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                if (attempt == maxRetries) {
                    throw e;
                }
                log.warn("API请求失败，正在重试 ({}/{}): {}", attempt + 1, maxRetries, e.getMessage());
            } catch (ResourceAccessException e) {
                if (attempt == maxRetries) {
                    throw e;
                }
                log.warn("API连接超时，正在重试 ({}/{}): {}", attempt + 1, maxRetries, e.getMessage());
            }

            // 等待重试间隔
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }

        throw new RuntimeException("API请求重试次数已用完");
    }

    private ReturnResult<FileOperationResponse> executeSimpleOperation(Integer serverId, String operation, String path,
                                                                       Map<String, Object> additionalParams) {
        long startTime = System.currentTimeMillis();

        try {
            // 验证客户端健康状态
            ReturnResult<Boolean> healthCheck = validateClientHealth(serverId);
            if (!healthCheck.isOk()) {
                return ReturnResult.error(healthCheck.getMsg());
            }

            FileManagementApiConfig config = getApiConfig(serverId);
            if (config == null) {
                return ReturnResult.error("无法获取API配置");
            }

            // 构建请求参数
            Map<String, Object> requestParams = new HashMap<>();
            if (path != null) {
                requestParams.put("path", path);
            }
            requestParams.putAll(additionalParams);

            // 发送API请求
            String url = config.buildFileOperationUrl(operation);
            ResponseEntity<Map> response = sendApiRequest(config, url, HttpMethod.POST, requestParams, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String message = operation + "操作成功";
                if (response.getBody() != null && response.getBody().containsKey("message")) {
                    message = (String) response.getBody().get("message");
                }

                FileOperationResponse operationResponse = FileOperationResponse
                        .success(operation.toUpperCase(), message).withDuration(startTime);

                log.info("API操作成功: serverId={}, operation={}, path={}", serverId, operation, path);
                return ReturnResult.ok(operationResponse);
            } else {
                return ReturnResult.error(operation + "操作失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("API操作失败: serverId={}, operation={}, path={}", serverId, operation, path, e);
            return ReturnResult.error(operation + "操作失败: " + e.getMessage());
        }
    }

    private List<FileInfo> parseFileList(Map<String, Object> responseBody) {
        List<FileInfo> files = new ArrayList<>();

        if (responseBody.containsKey("files")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fileList = (List<Map<String, Object>>) responseBody.get("files");

            for (Map<String, Object> fileData : fileList) {
                FileInfo fileInfo = parseFileInfo(fileData);
                if (fileInfo != null) {
                    files.add(fileInfo);
                }
            }
        }

        return files;
    }

    private FileInfo parseFileTree(Map<String, Object> responseBody) {
        if (responseBody.containsKey("tree")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> treeData = (Map<String, Object>) responseBody.get("tree");
            return parseFileInfo(treeData);
        }
        return null;
    }

    private FileInfo parseFileInfo(Map<String, Object> fileData) {
        try {
            FileInfo.FileInfoBuilder builder = FileInfo.builder();

            if (fileData.containsKey("name")) {
                builder.name((String) fileData.get("name"));
            }
            if (fileData.containsKey("path")) {
                builder.path((String) fileData.get("path"));
            }
            if (fileData.containsKey("size")) {
                Object size = fileData.get("size");
                if (size instanceof Number) {
                    builder.size(((Number) size).longValue());
                }
            }
            if (fileData.containsKey("isDirectory")) {
                builder.isDirectory((Boolean) fileData.get("isDirectory"));
            }
            if (fileData.containsKey("permissions")) {
                builder.permissions((String) fileData.get("permissions"));
            }
            if (fileData.containsKey("lastModified")) {
                // 处理时间戳或ISO字符串
                Object lastModified = fileData.get("lastModified");
                if (lastModified instanceof String) {
                    builder.modifyTime(LocalDateTime.parse((String) lastModified));
                } else if (lastModified instanceof Number) {
                    builder.modifyTime(LocalDateTime.ofEpochSecond(((Number) lastModified).longValue(), 0,
                            java.time.ZoneOffset.UTC));
                }
            }

            // 处理子文件列表
            if (fileData.containsKey("children")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> childrenData = (List<Map<String, Object>>) fileData.get("children");
                List<FileInfo> children = new ArrayList<>();
                for (Map<String, Object> childData : childrenData) {
                    FileInfo child = parseFileInfo(childData);
                    if (child != null) {
                        children.add(child);
                    }
                }
                builder.children(children);
            }

            return builder.build();

        } catch (Exception e) {
            log.warn("解析文件信息失败: {}", fileData, e);
            return null;
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileStatistics(Integer serverId, String path,
                                                                 Boolean includeSubdirectories) {
        Map<String, Object> params = new HashMap<>();
        params.put("includeSubdirectories", includeSubdirectories != null ? includeSubdirectories : false);
        return executeSimpleOperation(serverId, "statistics", path, params);
    }

    @Override
    public ReturnResult<String> watchFileChanges(Integer serverId, String path, Boolean recursive) {
        return ReturnResult.error("API模式暂不支持文件监控");
    }

    @Override
    public ReturnResult<Boolean> stopWatching(Integer serverId, String watchId) {
        return ReturnResult.error("API模式暂不支持文件监控");
    }

    @Override
    public ReturnResult<String> getFileChecksum(Integer serverId, String filePath, String algorithm) {
        Map<String, Object> params = new HashMap<>();
        params.put("algorithm", algorithm != null ? algorithm : "MD5");
        ReturnResult<FileOperationResponse> result = executeSimpleOperation(serverId, "checksum", filePath, params);
        if (result.isOk() && result.getData() != null) {
            return ReturnResult.ok(result.getData().getMessage());
        }
        return ReturnResult.error(result.getMsg());
    }

    @Override
    public ReturnResult<FileOperationResponse> syncFiles(Integer serverId, String sourcePath, String targetPath,
                                                         Boolean deleteExtra) {
        Map<String, Object> params = new HashMap<>();
        params.put("targetPath", targetPath);
        params.put("deleteExtra", deleteExtra != null ? deleteExtra : false);
        return executeSimpleOperation(serverId, "sync", sourcePath, params);
    }

    @Override
    public ReturnResult<FileOperationResponse> createSymbolicLink(Integer serverId, String targetPath,
                                                                  String linkPath) {
        Map<String, Object> params = new HashMap<>();
        params.put("linkPath", linkPath);
        return executeSimpleOperation(serverId, "symlink", targetPath, params);
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileSystemInfo(Integer serverId) {
        return executeSimpleOperation(serverId, "fsinfo", "/", Map.of());
    }

    // 内部类：InputStreamResource用于文件上传
    private static class InputStreamResource extends org.springframework.core.io.InputStreamResource {
        private final String filename;

        public InputStreamResource(InputStream inputStream, String filename) {
            super(inputStream);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public long contentLength() {
            return -1; // 未知长度
        }
    }
}
