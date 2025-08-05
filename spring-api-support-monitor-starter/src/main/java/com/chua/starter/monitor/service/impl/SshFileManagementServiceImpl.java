package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.protocol.ClientSetting;
import com.chua.common.support.protocol.client.FileClient;
import com.chua.common.support.protocol.client.ProtocolClientFactory;
import com.chua.report.client.starter.pojo.FileInfo;
import com.chua.report.client.starter.pojo.FileOperationRequest;
import com.chua.report.client.starter.pojo.FileOperationResponse;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.FileManagementService;
import com.chua.starter.monitor.service.MonitorSysGenServerService;
import com.chua.starter.monitor.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.utils.MonitorSysGenServerSettingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SSH文件管理服务实现
 *
 * @author CH
 * @since 2024/12/13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SshFileManagementServiceImpl implements FileManagementService {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerSettingService serverSettingService;

    @Override
    public ReturnResult<FileOperationResponse> listFiles(Integer serverId, String path, Boolean includeHidden,
            String sortBy, String sortOrder) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 获取文件列表
            List<com.chua.common.support.lang.file.FileInfo> commonFileInfos = fileClient.listFiles(path);
            if (commonFileInfos == null) {
                commonFileInfos = new ArrayList<>();
            }

            // 过滤隐藏文件
            if (!Boolean.TRUE.equals(includeHidden)) {
                commonFileInfos = commonFileInfos.stream().filter(file -> !file.getName().startsWith("."))
                        .collect(Collectors.toList());
            }

            // 转换为前端需要的格式
            List<FileInfo> result = commonFileInfos.stream().map(this::convertToFileInfo).collect(Collectors.toList());

            // 排序
            sortFiles(result, sortBy, sortOrder);

            return ReturnResult.ok(FileOperationResponse.success("LIST", "文件列表获取成功", result));
        } catch (Exception e) {
            log.error("列出文件失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("列出文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth,
            Boolean includeHidden) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 构建文件树
            FileInfo tree = buildFileTree(fileClient, path, maxDepth != null ? maxDepth : 3, includeHidden);
            return ReturnResult.ok(FileOperationResponse.successWithTree("TREE", "文件树获取成功", tree));
        } catch (Exception e) {
            log.error("获取文件树失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("获取文件树失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth,
            Boolean includeHidden, Boolean lazyLoad, Integer pageSize, Integer pageIndex) {
        // 对于SSH，暂时不支持分页加载，直接调用基础版本
        return getFileTree(serverId, path, maxDepth, includeHidden);
    }

    @Override
    public ReturnResult<FileOperationResponse> deleteFile(Integer serverId, String path, Boolean recursive) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            boolean success = fileClient.deleteFile(path);
            if (success) {
                return ReturnResult.ok(FileOperationResponse.success("DELETE", "文件删除成功"));
            } else {
                return ReturnResult.error("文件删除失败");
            }
        } catch (Exception e) {
            log.error("删除文件失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("删除文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> renameFile(Integer serverId, String oldPath, String newName) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            String parentPath = oldPath.substring(0, oldPath.lastIndexOf('/'));
            String newPath = parentPath + "/" + newName;

            boolean success = fileClient.rename(oldPath, newPath);
            if (success) {
                return ReturnResult.ok(FileOperationResponse.success("RENAME", "文件重命名成功"));
            } else {
                return ReturnResult.error("文件重命名失败");
            }
        } catch (Exception e) {
            log.error("重命名文件失败: serverId={}, oldPath={}, newName={}", serverId, oldPath, newName, e);
            return ReturnResult.error("重命名文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> createDirectory(Integer serverId, String path, Boolean recursive) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            boolean success;
            if (Boolean.TRUE.equals(recursive)) {
                success = fileClient.createDirectory(path, true);
            } else {
                success = fileClient.createDirectory(path);
            }

            if (success) {
                return ReturnResult.ok(FileOperationResponse.success("MKDIR", "目录创建成功"));
            } else {
                return ReturnResult.error("目录创建失败");
            }
        } catch (Exception e) {
            log.error("创建目录失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("创建目录失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, MultipartFile file,
            Boolean overwrite) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            String fileName = file.getOriginalFilename();
            String fullPath = targetPath.endsWith("/") ? targetPath + fileName : targetPath + "/" + fileName;

            // 检查文件是否存在
            if (!Boolean.TRUE.equals(overwrite) && fileClient.exists(fullPath)) {
                return ReturnResult.error("文件已存在: " + fullPath);
            }

            // 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                boolean success = fileClient.uploadFile(inputStream, fullPath);
                if (success) {
                    return ReturnResult.ok(FileOperationResponse.success("UPLOAD", "文件上传成功"));
                } else {
                    return ReturnResult.error("文件上传失败");
                }
            }
        } catch (Exception e) {
            log.error("上传文件失败: serverId={}, targetPath={}", serverId, targetPath, e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, String fileName,
            InputStream inputStream, Boolean overwrite) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            String fullPath = targetPath.endsWith("/") ? targetPath + fileName : targetPath + "/" + fileName;

            // 检查文件是否存在
            if (!Boolean.TRUE.equals(overwrite) && fileClient.exists(fullPath)) {
                return ReturnResult.error("文件已存在: " + fullPath);
            }

            boolean success = fileClient.uploadFile(inputStream, fullPath);
            if (success) {
                return ReturnResult.ok(FileOperationResponse.success("UPLOAD", "文件上传成功"));
            } else {
                return ReturnResult.error("文件上传失败");
            }
        } catch (Exception e) {
            log.error("上传文件失败: serverId={}, targetPath={}, fileName={}", serverId, targetPath, fileName, e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> downloadFile(Integer serverId, String filePath) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 检查文件是否存在
            if (!fileClient.exists(filePath)) {
                return ReturnResult.error("文件不存在: " + filePath);
            }

            // 下载文件到内存
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            boolean success = fileClient.downloadFile(filePath, outputStream);

            if (success) {
                FileOperationResponse response = FileOperationResponse.success("DOWNLOAD", "文件下载成功");
                response.setContentBytes(outputStream.toByteArray());
                return ReturnResult.ok(response);
            } else {
                return ReturnResult.error("文件下载失败");
            }
        } catch (Exception e) {
            log.error("下载文件失败: serverId={}, filePath={}", serverId, filePath, e);
            return ReturnResult.error("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> previewFile(Integer serverId, String filePath, String encoding,
            Integer maxLines) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 检查文件是否存在
            if (!fileClient.exists(filePath)) {
                return ReturnResult.error("文件不存在: " + filePath);
            }

            // 检查是否为文件
            if (!fileClient.isFile(filePath)) {
                return ReturnResult.error("不是文件: " + filePath);
            }

            // 下载文件内容
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            boolean success = fileClient.downloadFile(filePath, outputStream);

            if (success) {
                String content = outputStream.toString(encoding != null ? encoding : "UTF-8");

                // 限制行数
                if (maxLines != null && maxLines > 0) {
                    String[] lines = content.split("\n");
                    if (lines.length > maxLines) {
                        content = String.join("\n", Arrays.copyOf(lines, maxLines)) + "\n... (文件被截断)";
                    }
                }

                FileOperationResponse response = FileOperationResponse.success("PREVIEW", "文件预览成功");
                response.setContent(content);
                return ReturnResult.ok(response);
            } else {
                return ReturnResult.error("文件读取失败");
            }
        } catch (Exception e) {
            log.error("预览文件失败: serverId={}, filePath={}", serverId, filePath, e);
            return ReturnResult.error("预览文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> copyFile(Integer serverId, String sourcePath, String targetPath,
            Boolean overwrite) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 检查源文件是否存在
            if (!fileClient.exists(sourcePath)) {
                return ReturnResult.error("源文件不存在: " + sourcePath);
            }

            // 检查目标文件是否存在
            if (!Boolean.TRUE.equals(overwrite) && fileClient.exists(targetPath)) {
                return ReturnResult.error("目标文件已存在: " + targetPath);
            }

            boolean success = fileClient.copyFile(sourcePath, targetPath);
            if (success) {
                return ReturnResult.ok(FileOperationResponse.success("COPY", "文件复制成功"));
            } else {
                return ReturnResult.error("文件复制失败");
            }
        } catch (Exception e) {
            log.error("复制文件失败: serverId={}, sourcePath={}, targetPath={}", serverId, sourcePath, targetPath, e);
            return ReturnResult.error("复制文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> moveFile(Integer serverId, String sourcePath, String targetPath,
            Boolean overwrite) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 检查源文件是否存在
            if (!fileClient.exists(sourcePath)) {
                return ReturnResult.error("源文件不存在: " + sourcePath);
            }

            // 检查目标文件是否存在
            if (!Boolean.TRUE.equals(overwrite) && fileClient.exists(targetPath)) {
                return ReturnResult.error("目标文件已存在: " + targetPath);
            }

            boolean success = fileClient.move(sourcePath, targetPath);
            if (success) {
                return ReturnResult.ok(FileOperationResponse.success("MOVE", "文件移动成功"));
            } else {
                return ReturnResult.error("文件移动失败");
            }
        } catch (Exception e) {
            log.error("移动文件失败: serverId={}, sourcePath={}, targetPath={}", serverId, sourcePath, targetPath, e);
            return ReturnResult.error("移动文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> changePermissions(Integer serverId, String path, String permissions,
            Boolean recursive) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 检查文件是否存在
            if (!fileClient.exists(path)) {
                return ReturnResult.error("文件不存在: " + path);
            }

            // 转换权限字符串为八进制数字
            int permissionValue = Integer.parseInt(permissions, 8);
            boolean success = fileClient.setPermissions(path, permissionValue);

            if (success) {
                return ReturnResult.ok(FileOperationResponse.success("CHMOD", "权限修改成功"));
            } else {
                return ReturnResult.error("权限修改失败");
            }
        } catch (NumberFormatException e) {
            return ReturnResult.error("权限格式错误: " + permissions);
        } catch (Exception e) {
            log.error("修改权限失败: serverId={}, path={}, permissions={}", serverId, path, permissions, e);
            return ReturnResult.error("修改权限失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> changeOwner(Integer serverId, String path, String owner, String group,
            Boolean recursive) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 检查文件是否存在
            if (!fileClient.exists(path)) {
                return ReturnResult.error("文件不存在: " + path);
            }

            // 注意：FileClient接口中没有直接的changeOwner方法
            // 这个功能通常需要通过SSH命令来实现
            // 这里返回不支持的提示
            return ReturnResult.error("SSH文件管理暂不支持修改文件所有者功能");
        } catch (Exception e) {
            log.error("修改文件所有者失败: serverId={}, path={}, owner={}, group={}", serverId, path, owner, group, e);
            return ReturnResult.error("修改文件所有者失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> batchDeleteFiles(Integer serverId, List<String> paths,
            Boolean recursive) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            List<String> successPaths = new ArrayList<>();
            List<String> failedPaths = new ArrayList<>();

            for (String path : paths) {
                try {
                    boolean success;
                    if (fileClient.isDirectory(path)) {
                        success = fileClient.deleteDirectory(path, Boolean.TRUE.equals(recursive));
                    } else {
                        success = fileClient.deleteFile(path);
                    }

                    if (success) {
                        successPaths.add(path);
                    } else {
                        failedPaths.add(path);
                    }
                } catch (Exception e) {
                    log.warn("删除文件失败: path={}", path, e);
                    failedPaths.add(path);
                }
            }

            String message = String.format("批量删除完成，成功: %d, 失败: %d", successPaths.size(), failedPaths.size());
            FileOperationResponse response = FileOperationResponse.partialSuccess("BATCH_DELETE", message, paths.size(),
                    successPaths.size(), failedPaths.size());

            // 添加详细信息
            List<String> details = new ArrayList<>();
            if (!successPaths.isEmpty()) {
                details.add("成功删除: " + String.join(", ", successPaths));
            }
            if (!failedPaths.isEmpty()) {
                details.add("删除失败: " + String.join(", ", failedPaths));
            }
            response.setDetails(details);

            return ReturnResult.ok(response);
        } catch (Exception e) {
            log.error("批量删除文件失败: serverId={}", serverId, e);
            return ReturnResult.error("批量删除文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> searchFiles(Integer serverId, String searchPath, String pattern,
            Boolean includeContent, Integer maxResults) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 检查搜索路径是否存在
            if (!fileClient.exists(searchPath)) {
                return ReturnResult.error("搜索路径不存在: " + searchPath);
            }

            List<FileInfo> matchedFiles = new ArrayList<>();
            searchFilesRecursively(fileClient, searchPath, pattern, includeContent, matchedFiles,
                    maxResults != null ? maxResults : 100);

            String message = String.format("搜索完成，找到 %d 个匹配文件", matchedFiles.size());
            return ReturnResult.ok(FileOperationResponse.success("SEARCH", message, matchedFiles));
        } catch (Exception e) {
            log.error("搜索文件失败: serverId={}, searchPath={}, pattern={}", serverId, searchPath, pattern, e);
            return ReturnResult.error("搜索文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> compressFiles(Integer serverId, List<String> filePaths,
            String archivePath, String format) {
        // SSH文件管理暂不支持压缩功能，需要通过SSH命令来实现
        // 这里可以考虑执行tar、zip等命令
        return ReturnResult.error("SSH文件管理暂不支持压缩功能");
    }

    @Override
    public ReturnResult<FileOperationResponse> extractArchive(Integer serverId, String archivePath, String extractPath,
            Boolean overwrite) {
        // SSH文件管理暂不支持解压功能，需要通过SSH命令来实现
        // 这里可以考虑执行tar、unzip等命令
        return ReturnResult.error("SSH文件管理暂不支持解压功能");
    }

    @Override
    public ReturnResult<FileInfo> getFileInfo(Integer serverId, String path) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 检查文件是否存在
            if (!fileClient.exists(path)) {
                return ReturnResult.error("文件不存在: " + path);
            }

            com.chua.common.support.lang.file.FileInfo commonFileInfo = fileClient.getFileInfo(path);
            if (commonFileInfo != null) {
                FileInfo fileInfo = convertToFileInfo(commonFileInfo);
                return ReturnResult.ok(fileInfo);
            } else {
                return ReturnResult.error("获取文件信息失败");
            }
        } catch (Exception e) {
            log.error("获取文件信息失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("获取文件信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> getDiskUsage(Integer serverId, String path) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 检查路径是否存在
            if (!fileClient.exists(path)) {
                return ReturnResult.error("路径不存在: " + path);
            }

            // 计算目录大小
            long totalSize = calculateDirectorySize(fileClient, path);

            Map<String, Object> diskUsageData = new HashMap<>();
            diskUsageData.put("path", path);
            diskUsageData.put("totalSize", totalSize);
            diskUsageData.put("formattedSize", formatFileSize(totalSize));

            return ReturnResult.ok(FileOperationResponse.success("DISK_USAGE", "磁盘使用情况获取成功", diskUsageData));
        } catch (Exception e) {
            log.error("获取磁盘使用情况失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("获取磁盘使用情况失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileStatistics(Integer serverId, String path,
            Boolean includeSubdirectories) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 检查路径是否存在
            if (!fileClient.exists(path)) {
                return ReturnResult.error("路径不存在: " + path);
            }

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("path", path);
            statistics.put("totalFiles", 0);
            statistics.put("totalDirectories", 0);
            statistics.put("totalSize", 0L);
            statistics.put("includeSubdirectories", includeSubdirectories);

            // 计算统计信息
            calculateFileStatistics(fileClient, path, statistics, Boolean.TRUE.equals(includeSubdirectories));

            return ReturnResult.ok(FileOperationResponse.success("STATISTICS", "文件统计信息获取成功", statistics));
        } catch (Exception e) {
            log.error("获取文件统计信息失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("获取文件统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> watchFileChanges(Integer serverId, String path, Boolean recursive) {
        // SSH协议本身不支持文件监控，这个功能需要通过其他方式实现
        // 比如定期轮询或者使用SSH执行inotify命令
        return ReturnResult.error("SSH文件管理暂不支持文件监控功能");
    }

    @Override
    public ReturnResult<Boolean> stopWatching(Integer serverId, String watchId) {
        // SSH协议本身不支持文件监控，所以也不需要停止监控
        return ReturnResult.error("SSH文件管理暂不支持文件监控功能");
    }

    @Override
    public ReturnResult<String> getFileChecksum(Integer serverId, String filePath, String algorithm) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 检查文件是否存在
            if (!fileClient.exists(filePath)) {
                return ReturnResult.error("文件不存在: " + filePath);
            }

            // 下载文件内容并计算校验和
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            boolean success = fileClient.downloadFile(filePath, outputStream);

            if (success) {
                byte[] fileBytes = outputStream.toByteArray();
                String checksum = calculateChecksum(fileBytes, algorithm != null ? algorithm : "MD5");
                return ReturnResult.ok(checksum);
            } else {
                return ReturnResult.error("文件下载失败，无法计算校验和");
            }
        } catch (Exception e) {
            log.error("计算文件校验和失败: serverId={}, filePath={}, algorithm={}", serverId, filePath, algorithm, e);
            return ReturnResult.error("计算文件校验和失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> syncFiles(Integer serverId, String sourcePath, String targetPath,
            Boolean deleteExtra) {
        // SSH文件同步功能需要通过rsync等工具实现
        return ReturnResult.error("SSH文件管理暂不支持文件同步功能");
    }

    @Override
    public ReturnResult<FileOperationResponse> createSymbolicLink(Integer serverId, String targetPath,
            String linkPath) {
        // SSH创建符号链接需要通过SSH命令实现
        return ReturnResult.error("SSH文件管理暂不支持创建符号链接功能");
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileSystemInfo(Integer serverId) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 获取根目录信息作为文件系统信息
            Map<String, Object> fsInfo = new HashMap<>();
            fsInfo.put("type", "SSH/SFTP");
            fsInfo.put("protocol", "SFTP");
            fsInfo.put("connected", true);

            // 尝试获取当前目录
            try {
                String currentDir = fileClient.getCurrentDirectory();
                fsInfo.put("currentDirectory", currentDir);
            } catch (Exception e) {
                fsInfo.put("currentDirectory", "/");
            }

            return ReturnResult.ok(FileOperationResponse.success("FSINFO", "文件系统信息获取成功", fsInfo));
        } catch (Exception e) {
            log.error("获取文件系统信息失败: serverId={}", serverId, e);
            return ReturnResult.error("获取文件系统信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<String>> getSupportedOperations(Integer serverId) {
        List<String> operations = Arrays.asList("LIST", // 列出文件
                "TREE", // 文件树
                "UPLOAD", // 上传文件
                "DOWNLOAD", // 下载文件
                "DELETE", // 删除文件
                "RENAME", // 重命名
                "MKDIR", // 创建目录
                "COPY", // 复制文件
                "MOVE", // 移动文件
                "CHMOD", // 修改权限
                "PREVIEW", // 预览文件
                "EDIT", // 编辑文件
                "BATCH_DELETE", // 批量删除
                "TEST_CONNECTION" // 测试连接
        );
        return ReturnResult.ok(operations);
    }

    @Override
    public ReturnResult<Boolean> testConnection(Integer serverId) {
        try {
            FileClient fileClient = createFileClient(serverId);
            if (fileClient == null) {
                return ReturnResult.error("无法建立SSH连接");
            }

            // 测试连接 - 尝试获取当前目录
            String currentDir = fileClient.getCurrentDirectory();
            if (currentDir != null) {
                return ReturnResult.ok(true);
            } else {
                return ReturnResult.ok(false);
            }
        } catch (Exception e) {
            log.error("测试SSH连接失败: serverId={}", serverId, e);
            return ReturnResult.error("测试SSH连接失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> executeOperation(Integer serverId, FileOperationRequest request) {
        if (request == null || request.getOperation() == null) {
            return ReturnResult.error("操作请求不能为空");
        }

        try {
            switch (request.getOperation().toUpperCase()) {
            case "LIST":
                return listFiles(serverId, request.getPath(), request.getIncludeHidden(), request.getSortBy(),
                        request.getSortOrder());
            case "TREE":
                return getFileTree(serverId, request.getPath(), request.getMaxDepth(), request.getIncludeHidden());
            case "DELETE":
                return deleteFile(serverId, request.getPath(), request.getRecursive());
            case "RENAME":
                return renameFile(serverId, request.getPath(), request.getNewName());
            case "MKDIR":
                return createDirectory(serverId, request.getPath(), request.getRecursive());
            case "COPY":
                return copyFile(serverId, request.getPath(), request.getTargetPath(), request.getOverwrite());
            case "MOVE":
                return moveFile(serverId, request.getPath(), request.getTargetPath(), request.getOverwrite());
            case "CHMOD":
                return changePermissions(serverId, request.getPath(), request.getPermissions(), request.getRecursive());
            case "PREVIEW":
                // 将maxSize转换为maxLines，假设每行平均50个字符
                Integer maxLines = request.getMaxSize() != null ? (int) (request.getMaxSize() / 50) : null;
                return previewFile(serverId, request.getPath(), request.getEncoding(), maxLines);
            case "DOWNLOAD":
                return downloadFile(serverId, request.getPath());
            default:
                return ReturnResult.error("不支持的操作: " + request.getOperation());
            }
        } catch (Exception e) {
            log.error("执行文件操作失败: serverId={}, operation={}", serverId, request.getOperation(), e);
            return ReturnResult.error("执行文件操作失败: " + e.getMessage());
        }
    }

    /**
     * 创建FileClient
     */
    private FileClient createFileClient(Integer serverId) {
        try {
            // 获取服务器信息
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return null;
            }

            // 检查文件管理模式
            ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return null;
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            if (!"SSH".equals(MonitorSysGenServerSettingUtils.getFileManagementMode(setting))) {
                return null;
            }

            return createFileClient(server);
        } catch (Exception e) {
            log.error("创建FileClient失败: serverId={}", serverId, e);
            return null;
        }
    }

    private FileClient createFileClient(MonitorSysGenServer server) {
        ClientSetting.ClientSettingBuilder<?, ?> config = ClientSetting.builder();
        config.host(server.getMonitorSysGenServerHost());
        config.port(server.getMonitorSysGenServerPort() != null ? server.getMonitorSysGenServerPort() : 22);
        config.username(server.getMonitorSysGenServerUsername());
        config.password(server.getMonitorSysGenServerPassword());
        config.clientType("sftp"); // 指定使用SFTP客户端

        if (server.getMonitorSysGenServerTimeout() != null) {
            config.connectTimeoutMillis(server.getMonitorSysGenServerTimeout());
        }

        return ProtocolClientFactory.create(server.getMonitorSysGenServerId(), FileClient.class, config.build());
    }

    /**
     * 构建文件树
     */
    private FileInfo buildFileTree(FileClient fileClient, String path, int maxDepth, Boolean includeHidden) {
        try {
            com.chua.common.support.lang.file.FileInfo commonFileInfo = fileClient.getFileInfo(path);
            if (commonFileInfo == null) {
                return null;
            }

            FileInfo fileInfo = convertToFileInfo(commonFileInfo);

            if (fileInfo.getIsDirectory() && maxDepth > 0) {
                List<com.chua.common.support.lang.file.FileInfo> children = fileClient.listFiles(path);
                if (children != null) {
                    List<FileInfo> childrenList = new ArrayList<>();
                    for (com.chua.common.support.lang.file.FileInfo child : children) {
                        if (!Boolean.TRUE.equals(includeHidden) && child.getName().startsWith(".")) {
                            continue;
                        }
                        FileInfo childInfo = buildFileTree(fileClient, child.getPath(), maxDepth - 1, includeHidden);
                        if (childInfo != null) {
                            childrenList.add(childInfo);
                        }
                    }
                    fileInfo.setChildren(childrenList);
                }
            }

            return fileInfo;
        } catch (Exception e) {
            log.error("构建文件树失败: path={}", path, e);
            return null;
        }
    }

    /**
     * 转换文件信息
     */
    private FileInfo convertToFileInfo(com.chua.common.support.lang.file.FileInfo commonFileInfo) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(commonFileInfo.getName());
        fileInfo.setPath(commonFileInfo.getPath());
        fileInfo.setSize(commonFileInfo.getSize());
        fileInfo.setIsDirectory(commonFileInfo.isDirectory());
        if (commonFileInfo.getLastModified() != 0) {
            fileInfo.setLastModified(LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(commonFileInfo.getLastModified()), ZoneId.systemDefault()));
        }
        fileInfo.setPermissions(commonFileInfo.getPermissions());
        fileInfo.setOwner(commonFileInfo.getOwner());
        fileInfo.setGroup(commonFileInfo.getGroup());
        return fileInfo;
    }

    /**
     * 排序文件列表
     */
    private void sortFiles(List<FileInfo> files, String sortBy, String sortOrder) {
        if (files == null || files.isEmpty()) {
            return;
        }

        Comparator<FileInfo> comparator;
        switch (sortBy != null ? sortBy.toLowerCase() : "name") {
        case "size":
            comparator = Comparator.comparing(FileInfo::getSize);
            break;
        case "modified":
            comparator = Comparator.comparing(FileInfo::getLastModified);
            break;
        case "type":
            comparator = Comparator.comparing(f -> f.getName().substring(f.getName().lastIndexOf('.') + 1));
            break;
        default:
            comparator = Comparator.comparing(FileInfo::getName, String.CASE_INSENSITIVE_ORDER);
            break;
        }

        // 目录优先排序
        comparator = Comparator.comparing((FileInfo f) -> f.getIsDirectory() ? 0 : 1).thenComparing(comparator);

        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        files.sort(comparator);
    }

    /**
     * 计算文件校验和
     */
    private String calculateChecksum(byte[] data, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("计算校验和失败: algorithm={}", algorithm, e);
            return null;
        }
    }

    /**
     * 递归搜索文件
     */
    private void searchFilesRecursively(FileClient fileClient, String searchPath, String pattern,
            Boolean includeContent, List<FileInfo> matchedFiles, int maxResults) {
        try {
            if (matchedFiles.size() >= maxResults) {
                return;
            }

            List<com.chua.common.support.lang.file.FileInfo> files = fileClient.listFiles(searchPath);
            if (files == null) {
                return;
            }

            for (com.chua.common.support.lang.file.FileInfo file : files) {
                if (matchedFiles.size() >= maxResults) {
                    break;
                }

                // 检查文件名是否匹配模式
                if (file.getName().toLowerCase().contains(pattern.toLowerCase())) {
                    FileInfo matchedFile = convertToFileInfo(file);
                    matchedFiles.add(matchedFile);
                }

                // 如果是目录，递归搜索
                if (file.isDirectory()) {
                    searchFilesRecursively(fileClient, file.getPath(), pattern, includeContent, matchedFiles,
                            maxResults);
                }
            }
        } catch (Exception e) {
            log.warn("搜索文件时出错: path={}", searchPath, e);
        }
    }

    /**
     * 计算目录大小
     */
    private long calculateDirectorySize(FileClient fileClient, String path) {
        try {
            if (fileClient.isFile(path)) {
                return fileClient.getFileSize(path);
            }

            long totalSize = 0;
            List<com.chua.common.support.lang.file.FileInfo> files = fileClient.listFiles(path);
            if (files != null) {
                for (com.chua.common.support.lang.file.FileInfo file : files) {
                    if (file.isDirectory()) {
                        totalSize += calculateDirectorySize(fileClient, file.getPath());
                    } else {
                        totalSize += file.getSize();
                    }
                }
            }
            return totalSize;
        } catch (Exception e) {
            log.warn("计算目录大小时出错: path={}", path, e);
            return 0;
        }
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 计算文件统计信息
     */
    private void calculateFileStatistics(FileClient fileClient, String path, Map<String, Object> statistics,
            boolean includeSubdirectories) {
        try {
            List<com.chua.common.support.lang.file.FileInfo> files = fileClient.listFiles(path);
            if (files == null) {
                return;
            }

            int fileCount = (Integer) statistics.get("totalFiles");
            int dirCount = (Integer) statistics.get("totalDirectories");
            long totalSize = (Long) statistics.get("totalSize");

            for (com.chua.common.support.lang.file.FileInfo file : files) {
                if (file.isDirectory()) {
                    dirCount++;
                    if (includeSubdirectories) {
                        calculateFileStatistics(fileClient, file.getPath(), statistics, true);
                    }
                } else {
                    fileCount++;
                    totalSize += file.getSize();
                }
            }

            statistics.put("totalFiles", fileCount);
            statistics.put("totalDirectories", dirCount);
            statistics.put("totalSize", totalSize);
        } catch (Exception e) {
            log.warn("计算文件统计信息时出错: path={}", path, e);
        }
    }
}
