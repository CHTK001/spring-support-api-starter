package com.chua.starter.monitor.starter.service.impl;

import com.chua.starter.monitor.starter.utils.MonitorSysGenServerSettingUtils;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.protocol.ClientSetting;
import com.chua.ssh.support.SftpItem;
import com.chua.ssh.support.sftp.SftpChannel;
import com.chua.ssh.support.ssh.SshChannelSession;
import com.chua.ssh.support.ssh.SshClient;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.pojo.FileInfo;
import com.chua.starter.monitor.starter.pojo.FileOperationRequest;
import com.chua.starter.monitor.starter.pojo.FileOperationResponse;
import com.chua.starter.monitor.starter.service.FileManagementService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * SSH文件管理服务实现
 *
 * 重构说明： - 移除了直接使用 JSch 的代码，改为使用增强的 SftpChannel - 使用 SftpChannel 的新增功能：exists(),
 * isDirectory(), isFile(), mkdirs(), deleteRecursively() 等 -
 * 简化了文件操作逻辑，提高了代码可读性和维护性 - 保持了原有的 API 接口不变，确保向后兼容
 *
 * @author CH
 * @since 2024/12/13
 * @updated 2024/12/13 - 重构为使用增强的 SftpChannel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SshFileManagementServiceImpl implements FileManagementService {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerSettingService serverSettingService;

    // 移除重复缓存 - SshClient 自带连接管理和缓存机制

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
    public ReturnResult<FileOperationResponse> listFiles(Integer serverId, String path, Boolean includeHidden,
            String sortBy, String sortOrder) {
        try {
            SftpChannel sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            List<FileInfo> files = new ArrayList<>();

            // 使用增强的 SftpChannel 获取文件列表
            SftpItem[] items = sftp.list(path);

            if (items == null) {
                log.warn("SFTP列表返回null: serverId={}, path={}", serverId, path);
                return ReturnResult.ok(FileOperationResponse.success("LIST", "文件列表获取成功（空目录）", files));
            }

            for (SftpItem item : items) {
                if (item == null) {
                    log.warn("跳过null的SftpItem: serverId={}, path={}", serverId, path);
                    continue;
                }

                String fileName = null;
                try {
                    fileName = item.getName();
                } catch (Exception e) {
                    log.warn("获取文件名失败，跳过该项: serverId={}, path={}", serverId, path, e);
                    continue;
                }

                if (fileName == null || fileName.trim().isEmpty()) {
                    log.warn("文件名为空，跳过该项: serverId={}, path={}", serverId, path);
                    continue;
                }

                // 跳过当前目录和父目录
                if (".".equals(fileName) || "..".equals(fileName)) {
                    continue;
                }

                // 跳过隐藏文件（如果不包含）
                if (!Boolean.TRUE.equals(includeHidden) && fileName.startsWith(".")) {
                    continue;
                }

                // 使用 SftpItem 构建 FileInfo
                try {
                    FileInfo fileInfo = buildFileInfoFromSftpItem(item, path);
                    if (fileInfo != null) {
                        files.add(fileInfo);
                    }
                } catch (Exception e) {
                    log.warn("构建FileInfo失败，跳过文件: fileName={}, serverId={}, path={}", fileName, serverId, path, e);
                }
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
    public ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth,
            Boolean includeHidden) {
        return getFileTree(serverId, path, maxDepth, includeHidden, false, 1000, 0);
    }

    /**
     * 获取文件树（支持懒加载）
     */
    @Override
    public ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth,
            Boolean includeHidden, Boolean lazyLoad, Integer pageSize, Integer pageIndex) {
        try {
            log.info("SSH文件管理: 获取文件树 - serverId={}, path={}, maxDepth={}, lazyLoad={}, pageSize={}, pageIndex={}",
                    serverId, path, maxDepth, lazyLoad, pageSize, pageIndex);

            SftpChannel sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            FileInfo fileTree;
            if (lazyLoad) {
                // 懒加载模式：只加载当前层级的文件，支持分页
                fileTree = buildFileTreeLazy(sftp, path, maxDepth != null ? maxDepth : 3, includeHidden, pageSize,
                        pageIndex);
            } else {
                // 传统模式：递归加载所有层级
                fileTree = buildFileTree(sftp, path, maxDepth != null ? maxDepth : 3, includeHidden, 0);
            }

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
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, MultipartFile file,
            Boolean overwrite) {
        try {
            SftpChannel sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            String fileName = file.getOriginalFilename();
            String fullPath = targetPath.endsWith("/") ? targetPath + fileName : targetPath + "/" + fileName;

            // 检查文件是否存在
            if (!Boolean.TRUE.equals(overwrite)) {
                if (sftp.exists(fullPath)) {
                    return ReturnResult.error("文件已存在: " + fullPath);
                }
            }

            // 确保目标目录存在
            if (!sftp.mkdirs(targetPath)) {
                return ReturnResult.error("无法创建目标目录: " + targetPath);
            }

            // 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                sftp.upload(inputStream, fullPath);
            }

            // 获取上传后的文件信息
            FileInfo fileInfo = buildFileInfoFromPath(sftp, fileName, targetPath, fullPath);

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
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, String fileName,
            InputStream inputStream, Boolean overwrite) {
        try {
            SftpChannel sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            String fullPath = targetPath.endsWith("/") ? targetPath + fileName : targetPath + "/" + fileName;

            // 检查文件是否存在
            if (!Boolean.TRUE.equals(overwrite)) {
                if (sftp.exists(fullPath)) {
                    return ReturnResult.error("文件已存在: " + fullPath);
                }
            }

            // 确保目标目录存在
            if (!sftp.mkdirs(targetPath)) {
                return ReturnResult.error("无法创建目标目录: " + targetPath);
            }

            // 上传文件
            sftp.upload(inputStream, fullPath);

            // 获取上传后的文件信息
            FileInfo fileInfo = buildFileInfoFromPath(sftp, fileName, targetPath, fullPath);

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
            SftpChannel sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            // 检查文件是否存在
            if (!sftp.exists(filePath)) {
                return ReturnResult.error("文件不存在: " + filePath);
            }

            if (sftp.isDirectory(filePath)) {
                return ReturnResult.error("无法下载目录: " + filePath);
            }

            // 获取文件流
            InputStream inputStream = sftp.download(filePath);
            if (inputStream == null) {
                return ReturnResult.error("无法获取文件流");
            }

            // 构建响应
            FileOperationResponse response = FileOperationResponse.success("DOWNLOAD", "文件下载成功");
            // 注意：这里需要根据 FileOperationResponse 的实际实现来设置输入流

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
            SftpChannel sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            // 检查文件是否存在
            if (!sftp.exists(path)) {
                return ReturnResult.error("文件不存在: " + path);
            }

            boolean success;
            try {
                if (sftp.isDirectory(path)) {
                    if (Boolean.TRUE.equals(recursive)) {
                        // 递归删除目录及其内容
                        success = sftp.deleteRecursively(path);
                    } else {
                        // 只删除空目录
                        sftp.deleteDirectory(path);
                        success = !sftp.exists(path);
                    }
                } else {
                    // 删除文件
                    sftp.deleteFile(path);
                    success = !sftp.exists(path);
                }

                if (success) {
                    return ReturnResult.ok(FileOperationResponse.success("DELETE", "文件删除成功"));
                } else {
                    return ReturnResult.error("文件删除失败");
                }
            } catch (RuntimeException e) {
                log.error("SFTP删除操作失败: {}", path, e);
                return ReturnResult.error("删除操作失败: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("删除文件失败: serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("删除文件失败: " + e.getMessage());
        }
    }

    // 辅助方法
    private SshClient getSshClient(Integer serverId) {
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

            // 创建SSH客户端（SshClient 自带连接管理和缓存）
            SshClient sshClient = createSshClient(server);

            return sshClient;

        } catch (Exception e) {
            log.error("创建SSH客户端失败: serverId={}", serverId, e);
            return null;
        }
    }

    /**
     * 获取SFTP通道
     */
    private SftpChannel getSftpChannel(Integer serverId) {
        try {
            SshClient sshClient = getSshClient(serverId);
            if (sshClient == null) {
                log.error("无法获取SSH客户端: serverId={}", serverId);
                return null;
            }

            SshChannelSession session = sshClient.createSession();
            if (session == null) {
                log.error("无法创建SSH会话: serverId={}", serverId);
                return null;
            }

            SftpChannel sftpChannel = (SftpChannel) session.openChannel("sftp");
            if (sftpChannel == null) {
                log.error("无法创建SFTP通道: serverId={}", serverId);
                return null;
            }

            return sftpChannel;

        } catch (Exception e) {
            log.error("获取SFTP通道失败: serverId={}", serverId, e);
            return null;
        }
    }

    private SshClient createSshClient(MonitorSysGenServer server) {
        ClientSetting.ClientSettingBuilder<?, ?> config = ClientSetting.builder();
        config.host(server.getMonitorSysGenServerHost());
        config.port(server.getMonitorSysGenServerPort() != null ? server.getMonitorSysGenServerPort() : 22);
        config.username(server.getMonitorSysGenServerUsername());
        config.password(server.getMonitorSysGenServerPassword());

        if (server.getMonitorSysGenServerTimeout() != null) {
            config.timeoutMillis(server.getMonitorSysGenServerTimeout());
        }

        return new SshClient(config.build());
    }

    private boolean isClientConnected(SshClient sshClient) {
        try {
            SshChannelSession session = sshClient.createSession();
            return session != null && !session.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    // parseFileInfo 方法已移除 - 不再需要解析 ls 命令输出，改用 SftpChannel 的增强功能

    /**
     * 从 SftpItem 构建 FileInfo
     */
    private FileInfo buildFileInfoFromSftpItem(SftpItem item, String parentPath) {
        try {
            String fileName = item.getName();
            String fullPath = parentPath.endsWith("/") ? parentPath + fileName : parentPath + "/" + fileName;

            // 从 SftpItem 的 entry 获取权限信息
            String permissions = "unknown";
            try {
                if (item.getEntry() != null && item.getEntry().getAttrs() != null) {
                    permissions = getPermissionString(item.getEntry().getAttrs().getPermissions());
                }
            } catch (Exception e) {
                log.warn("获取文件权限失败: {}", fileName, e);
            }

            // 转换修改时间
            LocalDateTime modifyTime = LocalDateTime.now();
            try {
                modifyTime = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(item.lastModified()),
                        ZoneId.systemDefault());
            } catch (Exception e) {
                log.warn("获取文件修改时间失败: {}", fileName, e);
            }

            // 获取文件大小
            long size = 0L;
            try {
                size = item.getSize();
            } catch (Exception e) {
                log.warn("获取文件大小失败: {}", fileName, e);
            }

            // 判断是否为目录
            boolean isDirectory = false;
            try {
                isDirectory = item.isDirectory();
            } catch (Exception e) {
                log.warn("判断文件类型失败: {}", fileName, e);
            }

            return FileInfo.builder().name(fileName).path(fullPath).size(size).isDirectory(isDirectory)
                    .permissions(permissions).modifyTime(modifyTime).build();

        } catch (Exception e) {
            log.error("构建FileInfo失败", e);
            // 返回一个基本的FileInfo对象
            String fileName = "unknown";
            try {
                fileName = item.getName();
            } catch (Exception ex) {
                log.error("无法获取文件名", ex);
            }

            return FileInfo.builder().name(fileName).path(parentPath + "/" + fileName).size(0L).isDirectory(false)
                    .permissions("unknown").modifyTime(LocalDateTime.now()).leaf(true) // 错误情况下默认为叶子节点
                    .build();
        }
    }

    /**
     * 从路径构建 FileInfo（使用 SftpChannel 的增强功能）
     */
    private FileInfo buildFileInfoFromPath(SftpChannel sftp, String fileName, String parentPath, String fullPath) {
        try {
            long size = sftp.getFileSize(fullPath);
            long lastModified = sftp.getLastModified(fullPath);
            boolean isDirectory = sftp.isDirectory(fullPath);

            LocalDateTime modifyTime = LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(lastModified),
                    ZoneId.systemDefault());

            return FileInfo.builder().name(fileName).path(fullPath).size(size).isDirectory(isDirectory)
                    .permissions("rw-r--r--") // 默认权限，可以根据需要调整
                    .modifyTime(modifyTime).leaf(!isDirectory) // 设置 leaf 属性：文件为叶子节点，目录不是叶子节点
                    .build();
        } catch (Exception e) {
            log.error("构建文件信息失败: {}", fullPath, e);
            // 返回基本信息
            return FileInfo.builder().name(fileName).path(fullPath).size(0L).isDirectory(false).permissions("unknown")
                    .modifyTime(LocalDateTime.now()).leaf(true) // 错误情况下默认为叶子节点
                    .build();
        }
    }

    // buildFileInfo(SftpATTRS) 方法已移除 - 已被 buildFileInfoFromSftpItem 和
    // buildFileInfoFromPath 替代

    private String getPermissionString(int permissions) {
        StringBuilder sb = new StringBuilder();

        // 文件类型
        if ((permissions & 0040000) != 0)
            sb.append('d'); // 目录
        else if ((permissions & 0120000) != 0)
            sb.append('l'); // 符号链接
        else
            sb.append('-'); // 普通文件

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

    private FileInfo buildFileTree(SftpChannel sftp, String path, int maxDepth, Boolean includeHidden,
            int currentDepth) {
        if (currentDepth >= maxDepth) {
            return null;
        }

        // 检查路径是否存在
        if (!sftp.exists(path)) {
            return null;
        }

        String fileName = path.substring(path.lastIndexOf('/') + 1);
        if (fileName.isEmpty()) {
            fileName = "/";
        }

        String parentPath = path.substring(0, path.lastIndexOf('/'));
        FileInfo fileInfo = buildFileInfoFromPath(sftp, fileName, parentPath, path);

        if (sftp.isDirectory(path)) {
            List<FileInfo> children = new ArrayList<>();

            // 使用增强的 SftpChannel 获取文件列表
            SftpItem[] items = null;
            try {
                items = sftp.list(path);
            } catch (Exception e) {
                log.warn("列出目录内容失败: {}", path, e);
                fileInfo.setChildren(children); // 设置空的子项列表
                return fileInfo;
            }

            if (items != null) {
                for (SftpItem item : items) {
                    if (item == null) {
                        continue;
                    }

                    String childName = null;
                    try {
                        childName = item.getName();
                    } catch (Exception e) {
                        log.warn("获取子项名称失败: {}", path, e);
                        continue;
                    }

                    if (childName == null || childName.trim().isEmpty()) {
                        continue;
                    }

                    if (".".equals(childName) || "..".equals(childName)) {
                        continue;
                    }

                    if (!Boolean.TRUE.equals(includeHidden) && childName.startsWith(".")) {
                        continue;
                    }

                    String childPath = path.endsWith("/") ? path + childName : path + "/" + childName;

                    try {
                        if (item.isDirectory()) {
                            FileInfo childTree = buildFileTree(sftp, childPath, maxDepth, includeHidden,
                                    currentDepth + 1);
                            if (childTree != null) {
                                children.add(childTree);
                            }
                        } else {
                            FileInfo childInfo = buildFileInfoFromSftpItem(item, path);
                            if (childInfo != null) {
                                children.add(childInfo);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("处理子项失败: childName={}, path={}", childName, path, e);
                    }
                }
            }

            fileInfo.setChildren(children);
        }

        return fileInfo;
    }

    /**
     * 构建文件树（懒加载模式） 只加载当前层级的文件夹，不递归加载子目录
     */
    private FileInfo buildFileTreeLazy(SftpChannel sftp, String path, int maxDepth, Boolean includeHidden, int pageSize,
            int pageIndex) {
        // 检查路径是否存在
        if (!sftp.exists(path)) {
            return null;
        }

        String fileName = path.substring(path.lastIndexOf('/') + 1);
        if (fileName.isEmpty()) {
            fileName = "/";
        }

        String parentPath = path.substring(0, path.lastIndexOf('/'));
        FileInfo fileInfo = buildFileInfoFromPath(sftp, fileName, parentPath, path);

        if (sftp.isDirectory(path)) {
            List<FileInfo> children = new ArrayList<>();
            List<SftpItem> allDirectories = new ArrayList<>();

            // 使用增强的 SftpChannel 获取文件列表
            SftpItem[] items = null;
            try {
                items = sftp.list(path);
            } catch (Exception e) {
                log.warn("列出目录内容失败: {}", path, e);
                fileInfo.setChildren(children); // 设置空的子项列表
                return fileInfo;
            }

            if (items != null) {
                // 先收集所有目录
                for (SftpItem item : items) {
                    if (item == null) {
                        continue;
                    }

                    String childName = null;
                    try {
                        childName = item.getName();
                    } catch (Exception e) {
                        log.warn("获取子项名称失败: {}", path, e);
                        continue;
                    }

                    if (childName == null || childName.trim().isEmpty()) {
                        continue;
                    }

                    if (".".equals(childName) || "..".equals(childName)) {
                        continue;
                    }

                    if (!Boolean.TRUE.equals(includeHidden) && childName.startsWith(".")) {
                        continue;
                    }

                    try {
                        // 只收集目录，懒加载模式下不递归加载文件
                        if (item.isDirectory()) {
                            allDirectories.add(item);
                        }
                    } catch (Exception e) {
                        log.warn("检查子项类型失败: childName={}, path={}", childName, path, e);
                    }
                }

                // 排序以确保一致的顺序
                allDirectories.sort(Comparator.comparing(item -> {
                    try {
                        return item.getName();
                    } catch (Exception e) {
                        return "";
                    }
                }));

                // 实现分页
                int totalEntries = allDirectories.size();
                int startIndex = pageIndex * pageSize;
                int endIndex = Math.min(startIndex + pageSize, totalEntries);

                if (startIndex < totalEntries) {
                    for (int i = startIndex; i < endIndex; i++) {
                        SftpItem item = allDirectories.get(i);
                        try {
                            // 创建目录节点，但不递归加载其子目录
                            FileInfo childInfo = createDirectoryNodeLazy(sftp, item, path, includeHidden);
                            if (childInfo != null) {
                                children.add(childInfo);
                            }
                        } catch (Exception e) {
                            log.warn("创建目录节点失败: item={}, path={}", item, path, e);
                        }
                    }
                }

                // 如果有更多数据，设置hasMore标志
                if (endIndex < totalEntries) {
                    fileInfo.setExtraProperties("{\"hasMore\": true, \"totalCount\": " + totalEntries + "}");
                }
            }

            fileInfo.setChildren(children);
        }

        return fileInfo;
    }

    /**
     * 创建目录节点（懒加载模式） 只创建节点信息，不加载子目录
     */
    private FileInfo createDirectoryNodeLazy(SftpChannel sftp, SftpItem item, String parentPath,
            Boolean includeHidden) {
        try {
            String childName = item.getName();
            String childPath = parentPath.endsWith("/") ? parentPath + childName : parentPath + "/" + childName;

            FileInfo childInfo = buildFileInfoFromSftpItem(item, parentPath);
            if (childInfo != null) {
                // 检查是否有子目录，用于前端显示展开图标
                boolean hasChildren = false;
                try {
                    SftpItem[] subItems = sftp.list(childPath);
                    if (subItems != null) {
                        for (SftpItem subItem : subItems) {
                            if (subItem != null && subItem.isDirectory()) {
                                String subName = subItem.getName();
                                if (!".".equals(subName) && !"..".equals(subName)) {
                                    if (Boolean.TRUE.equals(includeHidden) || !subName.startsWith(".")) {
                                        hasChildren = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // 忽略权限异常
                    log.debug("检查子目录失败: {}", childPath, e);
                }

                // 如果有子目录，设置一个空的children列表，让前端知道这个节点可以展开
                if (hasChildren) {
                    childInfo.setChildren(new ArrayList<>());
                    childInfo.setExtraProperties("{\"hasChildren\": true}");
                }
            }

            return childInfo;
        } catch (Exception e) {
            log.warn("创建目录节点失败: {}", item, e);
            return null;
        }
    }

    // ==================== 已移除的旧方法 ====================
    // createDirectoryRecursive - 已被 SftpChannel.mkdirs() 替代
    // deleteDirectoryRecursive - 已被 SftpChannel.deleteRecursively() 替代
    // 这些方法直接使用 JSch，现在改为使用我们增强的 SftpChannel

    // clearClientCache 方法已移除 - SshClient 自带连接管理，无需手动清理缓存

    @Override
    public ReturnResult<FileOperationResponse> renameFile(Integer serverId, String oldPath, String newName) {
        try {
            SftpChannel sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            // 检查源文件是否存在
            if (!sftp.exists(oldPath)) {
                return ReturnResult.error("源文件不存在: " + oldPath);
            }

            String parentPath = oldPath.substring(0, oldPath.lastIndexOf('/'));
            String newPath = parentPath + "/" + newName;

            // 检查目标文件是否已存在
            if (sftp.exists(newPath)) {
                return ReturnResult.error("目标文件已存在: " + newPath);
            }

            // 使用 SFTP 重命名操作
            try {
                sftp.rename(oldPath, newPath);

                // 验证重命名是否成功
                if (sftp.exists(newPath) && !sftp.exists(oldPath)) {
                    return ReturnResult.ok(FileOperationResponse.success("RENAME", "文件重命名成功"));
                } else {
                    return ReturnResult.error("重命名操作失败");
                }
            } catch (RuntimeException e) {
                log.error("SFTP重命名操作失败: {} -> {}", oldPath, newPath, e);
                return ReturnResult.error("重命名操作失败: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("重命名文件失败: serverId={}, oldPath={}, newName={}", serverId, oldPath, newName, e);
            return ReturnResult.error("重命名文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> createDirectory(Integer serverId, String path, Boolean recursive) {
        try {
            SftpChannel sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            // 检查目录是否已存在
            if (sftp.exists(path)) {
                if (sftp.isDirectory(path)) {
                    return ReturnResult.ok(FileOperationResponse.success("MKDIR", "目录已存在"));
                } else {
                    return ReturnResult.error("路径已存在且不是目录: " + path);
                }
            }

            boolean success;
            if (Boolean.TRUE.equals(recursive)) {
                // 递归创建目录
                success = sftp.mkdirs(path);
            } else {
                // 只创建单级目录
                try {
                    sftp.mkdir(path);
                    success = sftp.exists(path);
                } catch (RuntimeException e) {
                    log.warn("创建目录失败: {}", path, e);
                    success = false;
                }
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
    public ReturnResult<FileOperationResponse> copyFile(Integer serverId, String sourcePath, String targetPath,
            Boolean overwrite) {
        try {
            SftpChannel sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            // 检查源文件是否存在
            if (!sftp.exists(sourcePath)) {
                return ReturnResult.error("源文件不存在: " + sourcePath);
            }

            // 检查目标文件是否已存在
            if (sftp.exists(targetPath) && !Boolean.TRUE.equals(overwrite)) {
                return ReturnResult.error("目标文件已存在: " + targetPath);
            }

            boolean success;
            if (sftp.isDirectory(sourcePath)) {
                // 复制目录
                success = sftp.copyDirectory(sourcePath, targetPath);
            } else {
                // 复制文件
                success = sftp.copyFile(sourcePath, targetPath);
            }

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
            SftpChannel sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            // 检查源文件是否存在
            if (!sftp.exists(sourcePath)) {
                return ReturnResult.error("源文件不存在: " + sourcePath);
            }

            // 检查目标文件是否已存在
            if (sftp.exists(targetPath) && !Boolean.TRUE.equals(overwrite)) {
                return ReturnResult.error("目标文件已存在: " + targetPath);
            }

            // 如果目标文件存在且允许覆盖，先删除目标文件
            if (sftp.exists(targetPath) && Boolean.TRUE.equals(overwrite)) {
                if (sftp.isDirectory(targetPath)) {
                    if (!sftp.deleteRecursively(targetPath)) {
                        return ReturnResult.error("无法删除目标目录: " + targetPath);
                    }
                } else {
                    sftp.deleteFile(targetPath);
                }
            }

            // 使用 SFTP 移动操作
            boolean success = sftp.moveFile(sourcePath, targetPath);

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
            SftpChannel sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            // 检查文件是否存在
            if (!sftp.exists(path)) {
                return ReturnResult.error("文件不存在: " + path);
            }

            // 解析权限字符串为八进制数值
            int permissionValue;
            try {
                permissionValue = Integer.parseInt(permissions, 8);
            } catch (NumberFormatException e) {
                return ReturnResult.error("无效的权限格式: " + permissions);
            }

            if (Boolean.TRUE.equals(recursive) && sftp.isDirectory(path)) {
                // 递归修改权限
                boolean success = changePermissionsRecursively(sftp, path, permissionValue);
                if (success) {
                    return ReturnResult.ok(FileOperationResponse.success("CHMOD", "权限修改成功（递归）"));
                } else {
                    return ReturnResult.error("递归修改权限失败");
                }
            } else {
                // 只修改单个文件/目录的权限
                boolean success = sftp.setPermissions(path, permissionValue);
                if (success) {
                    return ReturnResult.ok(FileOperationResponse.success("CHMOD", "权限修改成功"));
                } else {
                    return ReturnResult.error("权限修改失败");
                }
            }

        } catch (Exception e) {
            log.error("修改权限失败: serverId={}, path={}, permissions={}", serverId, path, permissions, e);
            return ReturnResult.error("修改权限失败: " + e.getMessage());
        }
    }

    /**
     * 递归修改目录及其内容的权限
     *
     * @param sftp            SFTP通道
     * @param path            目录路径
     * @param permissionValue 权限值
     * @return 修改成功返回 true，否则返回 false
     */
    private boolean changePermissionsRecursively(SftpChannel sftp, String path, int permissionValue) {
        try {
            // 修改当前路径的权限
            if (!sftp.setPermissions(path, permissionValue)) {
                return false;
            }

            // 如果是目录，递归修改子项权限
            if (sftp.isDirectory(path)) {
                SftpItem[] items = sftp.list(path);
                if (items != null) {
                    for (SftpItem item : items) {
                        String fileName = item.getName();
                        if (".".equals(fileName) || "..".equals(fileName)) {
                            continue;
                        }

                        String fullPath = path.endsWith("/") ? path + fileName : path + "/" + fileName;
                        if (!changePermissionsRecursively(sftp, fullPath, permissionValue)) {
                            return false;
                        }
                    }
                }
            }

            return true;
        } catch (Exception e) {
            log.error("递归修改权限失败: {}", path, e);
            return false;
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> changeOwner(Integer serverId, String path, String owner, String group,
            Boolean recursive) {
        return ReturnResult.error("SSH模式暂不支持修改文件所有者操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> searchFiles(Integer serverId, String searchPath, String pattern,
            Boolean includeContent, Integer maxResults) {
        return ReturnResult.error("SSH模式暂不支持文件搜索操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> compressFiles(Integer serverId, List<String> filePaths,
            String archivePath, String format) {
        return ReturnResult.error("SSH模式暂不支持文件压缩操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> extractArchive(Integer serverId, String archivePath, String extractPath,
            Boolean overwrite) {
        return ReturnResult.error("SSH模式暂不支持文件解压操作");
    }

    @Override
    public ReturnResult<FileOperationResponse> previewFile(Integer serverId, String filePath, String previewType,
            Integer maxSizeMB) {
        try {
            SftpChannel sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            // 检查文件是否存在
            if (!sftp.exists(filePath)) {
                return ReturnResult.error("文件不存在: " + filePath);
            }

            if (sftp.isDirectory(filePath)) {
                return ReturnResult.error("无法预览目录");
            }

            long fileSize = sftp.getFileSize(filePath);
            long maxSizeBytes = maxSizeMB != null ? maxSizeMB * 1024L * 1024L : 10 * 1024L * 1024L; // 默认最大10MB
            long previewSize = Math.min(fileSize, maxSizeBytes);

            try (InputStream inputStream = sftp.download(filePath)) {
                byte[] buffer = new byte[(int) previewSize];
                int bytesRead = inputStream.read(buffer);

                String content = new String(buffer, 0, bytesRead, "UTF-8");

                FileOperationResponse response = new FileOperationResponse();
                response.setSuccess(true);
                response.setOperation("preview");
                response.setMessage("文件预览成功");
                response.setContent(content);
                response.setContentType("text");

                return ReturnResult.ok(response);
            }

        } catch (Exception e) {
            log.error("预览文件失败: serverId={}, filePath={}", serverId, filePath, e);
            return ReturnResult.error("预览文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileInfo> getFileInfo(Integer serverId, String path) {
        try {
            SftpChannel sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            if (!sftp.exists(path)) {
                return ReturnResult.error("文件不存在: " + path);
            }

            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String parentPath = path.substring(0, path.lastIndexOf('/'));

            FileInfo fileInfo = buildFileInfoFromPath(sftp, fileName, parentPath, path);

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
            SshClient sshClient = getSshClient(serverId);
            if (sshClient != null && isClientConnected(sshClient)) {
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
            return previewFile(serverId, request.getPath(), "auto", 10);
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
        try {
            SftpChannel sftp = getSftpChannel(serverId);
            if (sftp == null) {
                return ReturnResult.error("无法建立SFTP连接");
            }

            List<String> successPaths = new ArrayList<>();
            List<String> failedPaths = new ArrayList<>();

            for (String path : paths) {
                try {
                    if (!sftp.exists(path)) {
                        failedPaths.add(path);
                        continue;
                    }

                    if (sftp.isDirectory(path)) {
                        if (Boolean.TRUE.equals(recursive)) {
                            if (sftp.deleteRecursively(path)) {
                                successPaths.add(path);
                            } else {
                                failedPaths.add(path);
                            }
                        } else {
                            try {
                                sftp.deleteDirectory(path);
                                successPaths.add(path);
                            } catch (RuntimeException e) {
                                log.warn("删除目录失败: {}", path, e);
                                failedPaths.add(path);
                            }
                        }
                    } else {
                        try {
                            sftp.deleteFile(path);
                            successPaths.add(path);
                        } catch (RuntimeException e) {
                            log.warn("删除文件失败: {}", path, e);
                            failedPaths.add(path);
                        }
                    }
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
        List<String> operations = Arrays.asList("LIST", "TREE", "UPLOAD", "DOWNLOAD", "DELETE", "RENAME", "MKDIR",
                "MOVE", "PREVIEW", "INFO");
        return ReturnResult.ok(operations);
    }

    @Override
    public ReturnResult<FileOperationResponse> getDiskUsage(Integer serverId, String path) {
        return ReturnResult.error("SSH模式暂不支持磁盘使用情况查询");
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileStatistics(Integer serverId, String path,
            Boolean includeSubdirectories) {
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
    public ReturnResult<FileOperationResponse> syncFiles(Integer serverId, String sourcePath, String targetPath,
            Boolean deleteExtra) {
        return ReturnResult.error("SSH模式暂不支持文件同步功能");
    }

    @Override
    public ReturnResult<FileOperationResponse> createSymbolicLink(Integer serverId, String targetPath,
            String linkPath) {
        return ReturnResult.error("SSH模式暂不支持创建符号链接功能");
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileSystemInfo(Integer serverId) {
        return ReturnResult.error("SSH模式暂不支持文件系统信息获取");
    }

}
