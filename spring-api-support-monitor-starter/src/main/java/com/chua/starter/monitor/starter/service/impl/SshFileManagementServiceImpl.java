package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.enums.FileManagementMode;
import com.chua.starter.monitor.starter.pojo.FileInfo;
import com.chua.starter.monitor.starter.pojo.FileOperationRequest;
import com.chua.starter.monitor.starter.pojo.FileOperationResponse;
import com.chua.starter.monitor.starter.service.FileManagementService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

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

    // SSH会话缓存
    private final Map<Integer, Session> sessionCache = new ConcurrentHashMap<>();
    private final Map<Integer, Long> sessionCacheTime = new ConcurrentHashMap<>();
    private static final long SESSION_TIMEOUT = 5 * 60 * 1000; // 5分钟

    // 文件类型映射
    private static final Map<String, String> FILE_TYPE_MAP = new HashMap<>();
    static {
        FILE_TYPE_MAP.put("txt", "text");
        FILE_TYPE_MAP.put("log", "text");
        FILE_TYPE_MAP.put("conf", "text");
        FILE_TYPE_MAP.put("config", "text");
        FILE_TYPE_MAP.put("xml", "code");
        FILE_TYPE_MAP.put("json", "code");
        FILE_TYPE_MAP.put("yml", "code");
        FILE_TYPE_MAP.put("yaml", "code");
        FILE_TYPE_MAP.put("java", "code");
        FILE_TYPE_MAP.put("js", "code");
        FILE_TYPE_MAP.put("css", "code");
        FILE_TYPE_MAP.put("html", "code");
        FILE_TYPE_MAP.put("sql", "code");
        FILE_TYPE_MAP.put("sh", "script");
        FILE_TYPE_MAP.put("bat", "script");
        FILE_TYPE_MAP.put("py", "script");
        FILE_TYPE_MAP.put("zip", "archive");
        FILE_TYPE_MAP.put("tar", "archive");
        FILE_TYPE_MAP.put("gz", "archive");
        FILE_TYPE_MAP.put("rar", "archive");
        FILE_TYPE_MAP.put("7z", "archive");
        FILE_TYPE_MAP.put("jpg", "image");
        FILE_TYPE_MAP.put("jpeg", "image");
        FILE_TYPE_MAP.put("png", "image");
        FILE_TYPE_MAP.put("gif", "image");
        FILE_TYPE_MAP.put("bmp", "image");
        FILE_TYPE_MAP.put("pdf", "document");
        FILE_TYPE_MAP.put("doc", "document");
        FILE_TYPE_MAP.put("docx", "document");
        FILE_TYPE_MAP.put("xls", "document");
        FILE_TYPE_MAP.put("xlsx", "document");
    }

    /**
     * 列出文件
     */
    @Override
    public ReturnResult<FileOperationResponse> listFiles(Integer serverId, String path, Boolean includeHidden, String sortBy, String sortOrder) {
        try {
            ChannelSftp sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            List<FileInfo> files = new ArrayList<>();
            
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> entries = sftp.ls(path);
            
            for (ChannelSftp.LsEntry entry : entries) {
                String fileName = entry.getFilename();
                
                // 跳过当前目录和父目录
                if (".".equals(fileName) || "..".equals(fileName)) {
                    continue;
                }
                
                // 跳过隐藏文件（如果不包含）
                if (!Boolean.TRUE.equals(includeHidden) && fileName.startsWith(".")) {
                    continue;
                }
                
                SftpATTRS attrs = entry.getAttrs();
                FileInfo fileInfo = buildFileInfo(fileName, path, attrs);
                files.add(fileInfo);
            }
            
            // 排序
            sortFiles(files, sortBy, sortOrder);
            
            return ReturnResult.ok(FileOperationResponse.success("LIST", "文件列表获取成功", files));
            
        } catch (Exception e) {
            log.error("列出文件失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("列出文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件树
     */
    @Override
    public ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth, Boolean includeHidden) {
        try {
            ChannelSftp sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            FileInfo fileTree = buildFileTree(sftp, path, maxDepth != null ? maxDepth : 3, includeHidden, 0);
            
            return ReturnResult.ok(FileOperationResponse.successWithTree("TREE", "文件树获取成功", fileTree));
            
        } catch (Exception e) {
            log.error("获取文件树失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("获取文件树失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件
     */
    @Override
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, MultipartFile file, Boolean overwrite) {
        try {
            ChannelSftp sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            String fileName = file.getOriginalFilename();
            String fullPath = targetPath.endsWith("/") ? targetPath + fileName : targetPath + "/" + fileName;
            
            // 检查文件是否存在
            if (!Boolean.TRUE.equals(overwrite)) {
                try {
                    sftp.stat(fullPath);
                    return ReturnResult.error("文件已存在: " + fullPath);
                } catch (SftpException e) {
                    // 文件不存在，可以上传
                }
            }
            
            // 确保目标目录存在
            createDirectoryRecursive(sftp, targetPath);
            
            // 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                sftp.put(inputStream, fullPath);
            }
            
            // 获取上传后的文件信息
            SftpATTRS attrs = sftp.stat(fullPath);
            FileInfo fileInfo = buildFileInfo(fileName, targetPath, attrs);
            
            return ReturnResult.ok(FileOperationResponse.success("UPLOAD", "文件上传成功", fileInfo));
            
        } catch (Exception e) {
            log.error("上传文件失败: serverId={}, targetPath={}", serverId, targetPath, e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件（流方式）
     */
    @Override
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, String fileName, InputStream inputStream, Boolean overwrite) {
        try {
            ChannelSftp sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            String fullPath = targetPath.endsWith("/") ? targetPath + fileName : targetPath + "/" + fileName;
            
            // 检查文件是否存在
            if (!Boolean.TRUE.equals(overwrite)) {
                try {
                    sftp.stat(fullPath);
                    return ReturnResult.error("文件已存在: " + fullPath);
                } catch (SftpException e) {
                    // 文件不存在，可以上传
                }
            }
            
            // 确保目标目录存在
            createDirectoryRecursive(sftp, targetPath);
            
            // 上传文件
            sftp.put(inputStream, fullPath);
            
            // 获取上传后的文件信息
            SftpATTRS attrs = sftp.stat(fullPath);
            FileInfo fileInfo = buildFileInfo(fileName, targetPath, attrs);
            
            return ReturnResult.ok(FileOperationResponse.success("UPLOAD", "文件上传成功", fileInfo));
            
        } catch (Exception e) {
            log.error("上传文件失败: serverId={}, targetPath={}, fileName={}", serverId, targetPath, fileName, e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    @Override
    public ReturnResult<FileOperationResponse> downloadFile(Integer serverId, String filePath) {
        try {
            ChannelSftp sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            // 检查文件是否存在
            SftpATTRS attrs = sftp.stat(filePath);
            if (attrs.isDir()) {
                return ReturnResult.error("无法下载目录: " + filePath);
            }
            
            // 创建临时文件
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            File tempFile = File.createTempFile("download_", "_" + fileName);
            
            // 下载文件
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                sftp.get(filePath, fos);
            }
            
            // 构建响应
            FileOperationResponse response = FileOperationResponse.success("DOWNLOAD", "文件下载成功");
            response.setDownloadUrl(tempFile.getAbsolutePath());
            
            return ReturnResult.ok(response);
            
        } catch (Exception e) {
            log.error("下载文件失败: serverId={}, filePath={}", serverId, filePath, e);
            return ReturnResult.error("下载文件失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @Override
    public ReturnResult<FileOperationResponse> deleteFile(Integer serverId, String path, Boolean recursive) {
        try {
            ChannelSftp sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            SftpATTRS attrs = sftp.stat(path);
            
            if (attrs.isDir()) {
                if (Boolean.TRUE.equals(recursive)) {
                    deleteDirectoryRecursive(sftp, path);
                } else {
                    sftp.rmdir(path);
                }
            } else {
                sftp.rm(path);
            }
            
            return ReturnResult.ok(FileOperationResponse.success("DELETE", "文件删除成功"));
            
        } catch (Exception e) {
            log.error("删除文件失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("删除文件失败: " + e.getMessage());
        }
    }


    // 辅助方法
    private ChannelSftp getSftpChannel(Integer serverId) {
        try {
            Session session = getSession(serverId);
            if (session == null || !session.isConnected()) {
                return null;
            }
            
            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
            return sftp;
            
        } catch (Exception e) {
            log.error("获取SFTP通道失败: serverId={}", serverId, e);
            return null;
        }
    }

    private Session getSession(Integer serverId) {
        try {
            // 检查缓存
            Long cacheTime = sessionCacheTime.get(serverId);
            if (cacheTime != null && System.currentTimeMillis() - cacheTime < SESSION_TIMEOUT) {
                Session session = sessionCache.get(serverId);
                if (session != null && session.isConnected()) {
                    return session;
                }
            }
            
            // 获取服务器信息
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return null;
            }
            
            // 检查文件管理模式
            MonitorSysGenServerSetting setting = serverSettingService.getByServerId(serverId).getData();
            if (setting == null || setting.getMonitorSysGenServerSettingFileManagementMode() != FileManagementMode.SSH) {
                return null;
            }
            
            // 创建SSH连接
            JSch jsch = new JSch();
            Session session = jsch.getSession(server.getMonitorSysGenServerUsername(), 
                                            server.getMonitorSysGenServerHost(), 
                                            server.getMonitorSysGenServerPort());
            session.setPassword(server.getMonitorSysGenServerPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            
            // 缓存会话
            sessionCache.put(serverId, session);
            sessionCacheTime.put(serverId, System.currentTimeMillis());
            
            return session;
            
        } catch (Exception e) {
            log.error("创建SSH会话失败: serverId={}", serverId, e);
            return null;
        }
    }

    private FileInfo buildFileInfo(String fileName, String parentPath, SftpATTRS attrs) {
        String fullPath = parentPath.endsWith("/") ? parentPath + fileName : parentPath + "/" + fileName;

        return FileInfo.builder()
                .name(fileName)
                .path(fullPath)
                .size(attrs.getSize())
                .isDirectory(attrs.isDir())
                .permissions(getPermissionString(attrs.getPermissions()))
                .modifyTime(LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochSecond(attrs.getMTime()),
                    ZoneId.systemDefault()))
                .build();
    }

    private String getPermissionString(int permissions) {
        StringBuilder sb = new StringBuilder();

        // 文件类型
        if ((permissions & 0040000) != 0) sb.append('d'); // 目录
        else if ((permissions & 0120000) != 0) sb.append('l'); // 符号链接
        else sb.append('-'); // 普通文件

        // 所有者权限
        sb.append((permissions & 0400) != 0 ? 'r' : '-');
        sb.append((permissions & 0200) != 0 ? 'w' : '-');
        sb.append((permissions & 0100) != 0 ? 'x' : '-');

        // 组权限
        sb.append((permissions & 0040) != 0 ? 'r' : '-');
        sb.append((permissions & 0020) != 0 ? 'w' : '-');
        sb.append((permissions & 0010) != 0 ? 'x' : '-');

        // 其他权限
        sb.append((permissions & 0004) != 0 ? 'r' : '-');
        sb.append((permissions & 0002) != 0 ? 'w' : '-');
        sb.append((permissions & 0001) != 0 ? 'x' : '-');

        return sb.toString();
    }

    private void sortFiles(List<FileInfo> files, String sortBy, String sortOrder) {
        if (files == null || files.isEmpty()) {
            return;
        }

        Comparator<FileInfo> comparator;

        switch (sortBy != null ? sortBy : "name") {
            case "size":
                comparator = Comparator.comparing(f -> f.getSize() != null ? f.getSize() : 0L);
                break;
            case "modifyTime":
                comparator = Comparator.comparing(f -> f.getModifyTime() != null ? f.getModifyTime() : LocalDateTime.MIN);
                break;
            case "type":
                comparator = Comparator.comparing(f -> f.getIsDirectory() != null && f.getIsDirectory() ? "0" : "1");
                break;
            default:
                comparator = Comparator.comparing(FileInfo::getName, String.CASE_INSENSITIVE_ORDER);
                break;
        }

        // 目录优先
        comparator = Comparator.comparing((FileInfo f) -> f.getIsDirectory() != null && f.getIsDirectory() ? 0 : 1)
                .thenComparing(comparator);

        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        files.sort(comparator);
    }

    private FileInfo buildFileTree(ChannelSftp sftp, String path, int maxDepth, Boolean includeHidden, int currentDepth) throws SftpException {
        if (currentDepth >= maxDepth) {
            return null;
        }

        SftpATTRS attrs = sftp.stat(path);
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        if (fileName.isEmpty()) {
            fileName = "/";
        }

        FileInfo fileInfo = buildFileInfo(fileName, path.substring(0, path.lastIndexOf('/')), attrs);

        if (attrs.isDir()) {
            List<FileInfo> children = new ArrayList<>();

            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> entries = sftp.ls(path);

            for (ChannelSftp.LsEntry entry : entries) {
                String childName = entry.getFilename();

                if (".".equals(childName) || "..".equals(childName)) {
                    continue;
                }

                if (!Boolean.TRUE.equals(includeHidden) && childName.startsWith(".")) {
                    continue;
                }

                String childPath = path.endsWith("/") ? path + childName : path + "/" + childName;

                if (entry.getAttrs().isDir()) {
                    FileInfo childTree = buildFileTree(sftp, childPath, maxDepth, includeHidden, currentDepth + 1);
                    if (childTree != null) {
                        children.add(childTree);
                    }
                } else {
                    FileInfo childInfo = buildFileInfo(childName, path, entry.getAttrs());
                    children.add(childInfo);
                }
            }

            fileInfo.setChildren(children);
        }

        return fileInfo;
    }

    private void createDirectoryRecursive(ChannelSftp sftp, String path) throws SftpException {
        String[] parts = path.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) {
                currentPath.append("/");
                continue;
            }

            currentPath.append(part).append("/");

            try {
                sftp.stat(currentPath.toString());
            } catch (SftpException e) {
                // 目录不存在，创建它
                sftp.mkdir(currentPath.toString());
            }
        }
    }

    private void deleteDirectoryRecursive(ChannelSftp sftp, String path) throws SftpException {
        @SuppressWarnings("unchecked")
        Vector<ChannelSftp.LsEntry> entries = sftp.ls(path);

        for (ChannelSftp.LsEntry entry : entries) {
            String fileName = entry.getFilename();

            if (".".equals(fileName) || "..".equals(fileName)) {
                continue;
            }

            String fullPath = path.endsWith("/") ? path + fileName : path + "/" + fileName;

            if (entry.getAttrs().isDir()) {
                deleteDirectoryRecursive(sftp, fullPath);
            } else {
                sftp.rm(fullPath);
            }
        }

        sftp.rmdir(path);
    }

    /**
     * 清理会话缓存
     */
    public void clearSessionCache() {
        for (Session session : sessionCache.values()) {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        sessionCache.clear();
        sessionCacheTime.clear();
    }

    @Override
    public ReturnResult<FileOperationResponse> renameFile(Integer serverId, String oldPath, String newName) {
        try {
            ChannelSftp sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            String parentPath = oldPath.substring(0, oldPath.lastIndexOf('/'));
            String newPath = parentPath + "/" + newName;

            sftp.rename(oldPath, newPath);

            return ReturnResult.ok(FileOperationResponse.success("RENAME", "文件重命名成功"));

        } catch (Exception e) {
            log.error("重命名文件失败: serverId={}, oldPath={}, newName={}", serverId, oldPath, newName, e);
            return ReturnResult.error("重命名文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> createDirectory(Integer serverId, String path, Boolean recursive) {
        try {
            ChannelSftp sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            if (Boolean.TRUE.equals(recursive)) {
                createDirectoryRecursive(sftp, path);
            } else {
                sftp.mkdir(path);
            }

            return ReturnResult.ok(FileOperationResponse.success("MKDIR", "目录创建成功"));

        } catch (Exception e) {
            log.error("创建目录失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("创建目录失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> copyFile(Integer serverId, String sourcePath, String targetPath, Boolean overwrite) {
        return ReturnResult.error("SSH模式暂不支持文件复制操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> moveFile(Integer serverId, String sourcePath, String targetPath, Boolean overwrite) {
        try {
            ChannelSftp sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            // 检查目标文件是否存在
            if (!Boolean.TRUE.equals(overwrite)) {
                try {
                    sftp.stat(targetPath);
                    return ReturnResult.error("目标文件已存在: " + targetPath);
                } catch (SftpException e) {
                    // 文件不存在，可以移动
                }
            }

            sftp.rename(sourcePath, targetPath);

            return ReturnResult.ok(FileOperationResponse.success("MOVE", "文件移动成功"));

        } catch (Exception e) {
            log.error("移动文件失败: serverId={}, sourcePath={}, targetPath={}", serverId, sourcePath, targetPath, e);
            return ReturnResult.error("移动文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> changePermissions(Integer serverId, String path, String permissions, Boolean recursive) {
        try {
            ChannelSftp sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            int permissionValue = Integer.parseInt(permissions, 8);
            sftp.chmod(permissionValue, path);

            return ReturnResult.ok(FileOperationResponse.success("CHMOD", "权限修改成功"));

        } catch (Exception e) {
            log.error("修改权限失败: serverId={}, path={}, permissions={}", serverId, path, permissions, e);
            return ReturnResult.error("修改权限失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> changeOwner(Integer serverId, String path, String owner, String group, Boolean recursive) {
        return ReturnResult.error("SSH模式暂不支持修改文件所有者操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> searchFiles(Integer serverId, String searchPath, String pattern, Boolean includeContent, Integer maxResults) {
        return ReturnResult.error("SSH模式暂不支持文件搜索操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> compressFiles(Integer serverId, List<String> filePaths, String archivePath, String format) {
        return ReturnResult.error("SSH模式暂不支持文件压缩操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> extractArchive(Integer serverId, String archivePath, String extractPath, Boolean overwrite) {
        return ReturnResult.error("SSH模式暂不支持文件解压操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> previewFile(Integer serverId, String filePath, String encoding, Long maxSize) {
        try {
            ChannelSftp sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            SftpATTRS attrs = sftp.stat(filePath);
            if (attrs.isDir()) {
                return ReturnResult.error("无法预览目录");
            }

            long fileSize = attrs.getSize();
            long previewSize = maxSize != null ? Math.min(fileSize, maxSize) : Math.min(fileSize, 1024 * 1024); // 默认最大1MB

            try (InputStream inputStream = sftp.get(filePath)) {
                byte[] buffer = new byte[(int) previewSize];
                int bytesRead = inputStream.read(buffer);

                String content = new String(buffer, 0, bytesRead, encoding != null ? encoding : "UTF-8");

                Map<String, Object> data = new HashMap<>();
                data.put("content", content);
                data.put("size", fileSize);
                data.put("previewSize", bytesRead);
                data.put("encoding", encoding != null ? encoding : "UTF-8");

                return ReturnResult.ok(FileOperationResponse.success("PREVIEW", "文件预览成功", data));
            }

        } catch (Exception e) {
            log.error("预览文件失败: serverId={}, filePath={}", serverId, filePath, e);
            return ReturnResult.error("预览文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileInfo> getFileInfo(Integer serverId, String path) {
        try {
            ChannelSftp sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            SftpATTRS attrs = sftp.stat(path);
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String parentPath = path.substring(0, path.lastIndexOf('/'));

            FileInfo fileInfo = buildFileInfo(fileName, parentPath, attrs);

            return ReturnResult.ok(fileInfo);

        } catch (Exception e) {
            log.error("获取文件信息失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 测试连接
     */
    @Override
    public ReturnResult<Boolean> testConnection(Integer serverId) {
        try {
            Session session = getSession(serverId);
            if (session != null && session.isConnected()) {
                return ReturnResult.ok(true);
            }
            return ReturnResult.error("SSH连接测试失败");
        } catch (Exception e) {
            log.error("SSH连接测试失败: serverId={}", serverId, e);
            return ReturnResult.error("SSH连接测试失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> executeOperation(Integer serverId, FileOperationRequest request) {
        if (request == null || request.getOperation() == null) {
            return ReturnResult.error("操作请求不能为空");
        }

        switch (FileOperationRequest.OperationType.fromCode(request.getOperation())) {
            case LIST:
                return listFiles(serverId, request.getPath(), request.getIncludeHiddenOrDefault(),
                        request.getSortByOrDefault(), request.getSortOrderOrDefault());
            case TREE:
                return getFileTree(serverId, request.getPath(), request.getMaxDepthOrDefault(), request.getIncludeHiddenOrDefault());
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
                return searchFiles(serverId, request.getPath(), request.getPattern(),
                        request.getIncludeContentOrDefault(), request.getMaxResultsOrDefault());
            case PREVIEW:
                return previewFile(serverId, request.getPath(), request.getEncoding(), request.getMaxSize());
            case COMPRESS:
                return compressFiles(serverId, request.getPaths(), request.getArchivePath(), request.getFormat());
            case EXTRACT:
                return extractArchive(serverId, request.getPath(), request.getExtractPath(), request.getOverwriteOrDefault());
            default:
                return ReturnResult.error("不支持的操作类型: " + request.getOperation());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> batchDeleteFiles(Integer serverId, List<String> paths, Boolean recursive) {
        try {
            ChannelSftp sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            List<String> successPaths = new ArrayList<>();
            List<String> failedPaths = new ArrayList<>();

            for (String path : paths) {
                try {
                    SftpATTRS attrs = sftp.stat(path);
                    if (attrs.isDir()) {
                        if (Boolean.TRUE.equals(recursive)) {
                            deleteDirectoryRecursive(sftp, path);
                        } else {
                            sftp.rmdir(path);
                        }
                    } else {
                        sftp.rm(path);
                    }
                    successPaths.add(path);
                } catch (Exception e) {
                    log.error("删除文件失败: path={}", path, e);
                    failedPaths.add(path);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("successPaths", successPaths);
            result.put("failedPaths", failedPaths);
            result.put("successCount", successPaths.size());
            result.put("failedCount", failedPaths.size());

            return ReturnResult.ok(FileOperationResponse.success("BATCH_DELETE", "批量删除完成", result));

        } catch (Exception e) {
            log.error("批量删除文件失败: serverId={}", serverId, e);
            return ReturnResult.error("批量删除文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<String>> getSupportedOperations(Integer serverId) {
        List<String> operations = Arrays.asList(
            "LIST", "TREE", "UPLOAD", "DOWNLOAD", "DELETE", "RENAME", "MKDIR",
            "MOVE", "PREVIEW", "INFO"
        );
        return ReturnResult.ok(operations);
    }

    @Override
    public ReturnResult<FileOperationResponse> getDiskUsage(Integer serverId, String path) {
        return ReturnResult.error("SSH模式暂不支持磁盘使用情况查询");
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileStatistics(Integer serverId, String path, Boolean includeSubdirectories) {
        return ReturnResult.error("SSH模式暂不支持文件统计信息查询");
    }

    @Override
    public ReturnResult<String> watchFileChanges(Integer serverId, String path, Boolean recursive) {
        return ReturnResult.error("SSH模式暂不支持文件监控功能");
    }

    @Override
    public ReturnResult<Boolean> stopWatching(Integer serverId, String watchId) {
        return ReturnResult.error("SSH模式暂不支持文件监控功能");
    }

    @Override
    public ReturnResult<String> getFileChecksum(Integer serverId, String filePath, String algorithm) {
        return ReturnResult.error("SSH模式暂不支持文件校验和计算");
    }

    @Override
    public ReturnResult<FileOperationResponse> syncFiles(Integer serverId, String sourcePath, String targetPath, Boolean deleteExtra) {
        return ReturnResult.error("SSH模式暂不支持文件同步功能");
    }

    @Override
    public ReturnResult<FileOperationResponse> createSymbolicLink(Integer serverId, String targetPath, String linkPath) {
        return ReturnResult.error("SSH模式暂不支持创建符号链接功能");
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileSystemInfo(Integer serverId) {
        return ReturnResult.error("SSH模式暂不支持文件系统信息获取");
    }
}
