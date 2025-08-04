package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.pojo.FileOperationRequest;
import com.chua.starter.monitor.starter.pojo.FileOperationResponse;
import com.chua.starter.monitor.starter.service.FileManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.TEXT_PLAIN;

/**
 * 本地文件管理服务实现
 * 
 * @author CH
 * @since 2024/12/20
 */
@Slf4j
@Service
public class LocalFileManagementServiceImpl implements FileManagementService {

    @Override
    public ReturnResult<FileOperationResponse> listFiles(Integer serverId, String path, Boolean includeHidden,
            String sortBy, String sortOrder) {
        try {
            log.info("本地文件管理: 列出文件 - serverId={}, path={}", serverId, path);

            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                return ReturnResult.error("路径不存在: " + path);
            }

            if (!Files.isDirectory(dirPath)) {
                return ReturnResult.error("路径不是目录: " + path);
            }

            List<FileInfo> files = new ArrayList<>();

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
                for (Path entry : stream) {
                    String fileName = entry.getFileName().toString();

                    // 过滤隐藏文件
                    if (!includeHidden && fileName.startsWith(".")) {
                        continue;
                    }

                    BasicFileAttributes attrs = Files.readAttributes(entry, BasicFileAttributes.class);

                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setName(fileName);
                    fileInfo.setPath(entry.toString());
                    fileInfo.setSize(attrs.size());
                    fileInfo.setIsDirectory(Files.isDirectory(entry));
                    fileInfo.setModifyTime(
                            LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault()));
                    fileInfo.setCreateTime(
                            LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault()));
                    fileInfo.setIsHidden(fileName.startsWith("."));
                    fileInfo.setLeaf(!Files.isDirectory(entry)); // 设置 leaf 属性：文件为叶子节点，目录不是叶子节点

                    files.add(fileInfo);
                }
            }

            // 排序
            sortFiles(files, sortBy, sortOrder);

            FileOperationResponse response = new FileOperationResponse();
            response.setSuccess(true);
            response.setMessage("获取文件列表成功");
            response.setFiles(files);
            response.setPath(path);

            return ReturnResult.ok(response);

        } catch (Exception e) {
            log.error("本地文件管理: 列出文件失败 - serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("列出文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth,
            Boolean includeHidden) {
        return getFileTree(serverId, path, maxDepth, includeHidden, false, 1000, 0);
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth,
            Boolean includeHidden, Boolean lazyLoad, Integer pageSize, Integer pageIndex) {
        try {
            log.info("本地文件管理: 获取文件树 - serverId={}, path={}, maxDepth={}, lazyLoad={}, pageSize={}, pageIndex={}",
                    serverId, path, maxDepth, lazyLoad, pageSize, pageIndex);

            FileInfo rootNode;

            // 特殊处理根目录请求
            if ("/".equals(path)) {
                log.info("处理根目录请求，检测系统类型");
                rootNode = buildSystemRootTree(maxDepth, includeHidden, lazyLoad, pageSize, pageIndex);
            } else {
                Path rootPath = Paths.get(path);
                if (!Files.exists(rootPath)) {
                    return ReturnResult.error("路径不存在: " + path);
                }

                if (lazyLoad) {
                    // 懒加载模式：只加载当前层级的文件，支持分页
                    rootNode = buildFileTreeLazy(rootPath, maxDepth, includeHidden, pageSize, pageIndex);
                } else {
                    // 传统模式：递归加载所有层级
                    rootNode = buildFileTree(rootPath, maxDepth, includeHidden, 0);
                }
            }

            FileOperationResponse response = new FileOperationResponse();
            response.setSuccess(true);
            response.setMessage("获取文件树成功");
            response.setFileTree(rootNode);

            return ReturnResult.ok(response);

        } catch (Exception e) {
            log.error("本地文件管理: 获取文件树失败 - serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("获取文件树失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, MultipartFile file,
            Boolean overwrite) {
        try {
            log.info("本地文件管理: 上传文件 - serverId={}, targetPath={}, fileName={}", serverId, targetPath,
                    file.getOriginalFilename());

            Path targetDir = Paths.get(targetPath);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String fileName = file.getOriginalFilename();
            Path targetFile = targetDir.resolve(fileName);

            // 检查文件是否存在
            if (Files.exists(targetFile) && !overwrite) {
                return ReturnResult.error("文件已存在: " + targetFile);
            }

            // 保存文件
            file.transferTo(targetFile.toFile());

            FileOperationResponse response = new FileOperationResponse();
            response.setSuccess(true);
            response.setMessage("文件上传成功");
            response.setPath(targetFile.toString());

            return ReturnResult.ok(response);

        } catch (Exception e) {
            log.error("本地文件管理: 上传文件失败 - serverId={}, targetPath={}", serverId, targetPath, e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> downloadFile(Integer serverId, String filePath) {
        try {
            log.info("本地文件管理: 下载文件 - serverId={}, filePath={}", serverId, filePath);

            Path file = Paths.get(filePath);
            if (!Files.exists(file)) {
                return ReturnResult.error("文件不存在: " + filePath);
            }

            if (Files.isDirectory(file)) {
                return ReturnResult.error("不能下载目录: " + filePath);
            }

            FileOperationResponse response = new FileOperationResponse();
            response.setSuccess(true);
            response.setMessage("文件下载准备完成");
            response.setDownloadUrl(filePath); // 本地文件直接返回路径

            return ReturnResult.ok(response);

        } catch (Exception e) {
            log.error("本地文件管理: 下载文件失败 - serverId={}, filePath={}", serverId, filePath, e);
            return ReturnResult.error("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> deleteFile(Integer serverId, String path, Boolean recursive) {
        try {
            log.info("本地文件管理: 删除文件 - serverId={}, path={}, recursive={}", serverId, path, recursive);

            Path file = Paths.get(path);
            if (!Files.exists(file)) {
                return ReturnResult.error("文件不存在: " + path);
            }

            if (Files.isDirectory(file) && recursive) {
                // 递归删除目录
                Files.walkFileTree(file, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                Files.delete(file);
            }

            FileOperationResponse response = new FileOperationResponse();
            response.setSuccess(true);
            response.setMessage("删除成功");

            return ReturnResult.ok(response);

        } catch (Exception e) {
            log.error("本地文件管理: 删除文件失败 - serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("删除文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> renameFile(Integer serverId, String oldPath, String newName) {
        try {
            log.info("本地文件管理: 重命名文件 - serverId={}, oldPath={}, newName={}", serverId, oldPath, newName);

            Path oldFile = Paths.get(oldPath);
            if (!Files.exists(oldFile)) {
                return ReturnResult.error("文件不存在: " + oldPath);
            }

            Path newFile = oldFile.getParent().resolve(newName);
            if (Files.exists(newFile)) {
                return ReturnResult.error("目标文件已存在: " + newFile);
            }

            Files.move(oldFile, newFile);

            FileOperationResponse response = new FileOperationResponse();
            response.setSuccess(true);
            response.setMessage("重命名成功");
            response.setPath(newFile.toString());

            return ReturnResult.ok(response);

        } catch (Exception e) {
            log.error("本地文件管理: 重命名文件失败 - serverId={}, oldPath={}, newName={}", serverId, oldPath, newName, e);
            return ReturnResult.error("重命名文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> createDirectory(Integer serverId, String path, Boolean recursive) {
        try {
            log.info("本地文件管理: 创建目录 - serverId={}, path={}, recursive={}", serverId, path, recursive);

            Path dir = Paths.get(path);
            if (Files.exists(dir)) {
                return ReturnResult.error("目录已存在: " + path);
            }

            if (recursive) {
                Files.createDirectories(dir);
            } else {
                Files.createDirectory(dir);
            }

            FileOperationResponse response = new FileOperationResponse();
            response.setSuccess(true);
            response.setMessage("创建目录成功");

            return ReturnResult.ok(response);

        } catch (Exception e) {
            log.error("本地文件管理: 创建目录失败 - serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("创建目录失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, String fileName,
            InputStream inputStream, Boolean overwrite) {
        try {
            log.info("本地文件管理: 上传文件流 - serverId={}, targetPath={}, fileName={}", serverId, targetPath, fileName);

            Path targetDir = Paths.get(targetPath);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Path targetFile = targetDir.resolve(fileName);

            // 检查文件是否存在
            if (Files.exists(targetFile) && !overwrite) {
                return ReturnResult.error("文件已存在: " + targetFile);
            }

            // 保存文件
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);

            FileOperationResponse response = new FileOperationResponse();
            response.setSuccess(true);
            response.setMessage("文件上传成功");
            response.setPath(targetFile.toString());

            return ReturnResult.ok(response);

        } catch (Exception e) {
            log.error("本地文件管理: 上传文件流失败 - serverId={}, targetPath={}", serverId, targetPath, e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> copyFile(Integer serverId, String sourcePath, String targetPath,
            Boolean overwrite) {
        try {
            log.info("本地文件管理: 复制文件 - serverId={}, sourcePath={}, targetPath={}", serverId, sourcePath, targetPath);

            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);

            if (!Files.exists(source)) {
                return ReturnResult.error("源文件不存在: " + sourcePath);
            }

            // 检查目标路径是否已存在
            if (Files.exists(target) && !overwrite) {
                return ReturnResult.error("目标文件已存在: " + targetPath);
            }

            // 确保目标路径的父目录存在
            Path targetParent = target.getParent();
            if (targetParent != null && !Files.exists(targetParent)) {
                Files.createDirectories(targetParent);
            }

            if (Files.isDirectory(source)) {
                // 复制目录：targetPath是目标目录的完整路径
                copyDirectory(source, target);
            } else {
                // 复制文件：targetPath是目标文件的完整路径
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }

            FileOperationResponse response = new FileOperationResponse();
            response.setSuccess(true);
            response.setMessage("复制成功");
            response.setPath(targetPath);

            return ReturnResult.ok(response);

        } catch (Exception e) {
            log.error("本地文件管理: 复制文件失败 - serverId={}, sourcePath={}, targetPath={}", serverId, sourcePath, targetPath,
                    e);
            return ReturnResult.error("复制文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> moveFile(Integer serverId, String sourcePath, String targetPath,
            Boolean overwrite) {
        try {
            log.info("本地文件管理: 移动文件 - serverId={}, sourcePath={}, targetPath={}", serverId, sourcePath, targetPath);

            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);

            if (!Files.exists(source)) {
                return ReturnResult.error("源文件不存在: " + sourcePath);
            }

            // 检查目标路径是否已存在
            if (Files.exists(target) && !overwrite) {
                return ReturnResult.error("目标文件已存在: " + targetPath);
            }

            // 确保目标路径的父目录存在
            Path targetParent = target.getParent();
            if (targetParent != null && !Files.exists(targetParent)) {
                Files.createDirectories(targetParent);
            }

            // 移动文件或目录：targetPath是目标的完整路径
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

            FileOperationResponse response = new FileOperationResponse();
            response.setSuccess(true);
            response.setMessage("移动成功");
            response.setPath(targetPath);

            return ReturnResult.ok(response);

        } catch (Exception e) {
            log.error("本地文件管理: 移动文件失败 - serverId={}, sourcePath={}, targetPath={}", serverId, sourcePath, targetPath,
                    e);
            return ReturnResult.error("移动文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> executeOperation(Integer serverId, FileOperationRequest request) {
        try {
            String operation = request.getOperation();
            log.info("本地文件管理: 执行操作 - serverId={}, operation={}", serverId, operation);

            switch (operation.toUpperCase()) {
            case "COPY":
                return copyFile(serverId, request.getPath(), request.getTargetPath(), request.getOverwrite());
            case "MOVE":
                return moveFile(serverId, request.getPath(), request.getTargetPath(), request.getOverwrite());
            default:
                return ReturnResult.error("本地文件管理不支持操作: " + operation);
            }

        } catch (Exception e) {
            log.error("本地文件管理: 执行操作失败 - serverId={}, operation={}", serverId, request.getOperation(), e);
            return ReturnResult.error("执行操作失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> testConnection(Integer serverId) {
        try {
            log.info("本地文件管理: 测试连接 - serverId={}", serverId);

            // 本地文件管理总是可用的
            return ReturnResult.ok(true);

        } catch (Exception e) {
            log.error("本地文件管理: 测试连接失败 - serverId={}", serverId, e);
            return ReturnResult.error("测试连接失败: " + e.getMessage());
        }
    }

    /**
     * 构建文件树
     */
    private FileInfo buildFileTree(Path path, int maxDepth, boolean includeHidden, int currentDepth)
            throws IOException {
        String fileName = path.getFileName() != null ? path.getFileName().toString() : path.toString();

        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

        FileInfo node = new FileInfo();
        node.setName(fileName);
        node.setPath(path.toString());
        node.setIsDirectory(Files.isDirectory(path));
        node.setSize(attrs.size());
        node.setModifyTime(LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault()));
        node.setCreateTime(LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault()));
        node.setIsHidden(fileName.startsWith("."));

        // 设置 leaf 属性：文件为叶子节点，目录不是叶子节点
        node.setLeaf(!Files.isDirectory(path));

        if (Files.isDirectory(path) && currentDepth < maxDepth) {
            List<FileInfo> children = new ArrayList<>();

            try {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                    for (Path entry : stream) {
                        String entryName = entry.getFileName().toString();

                        // 过滤隐藏文件
                        if (!includeHidden && entryName.startsWith(".")) {
                            continue;
                        }

                        if (Files.isDirectory(entry)) {
                            children.add(buildFileTree(entry, maxDepth, includeHidden, currentDepth + 1));
                        }
                    }
                }
            } catch (AccessDeniedException e) {
                // 忽略权限异常
            }

            node.setChildren(children);
        }

        return node;
    }

    /**
     * 构建文件树（懒加载模式） 只加载当前层级的文件夹，不递归加载子目录
     */
    private FileInfo buildFileTreeLazy(Path path, int maxDepth, boolean includeHidden, int pageSize, int pageIndex)
            throws IOException {
        String fileName = path.getFileName() != null ? path.getFileName().toString() : path.toString();

        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

        FileInfo node = new FileInfo();
        node.setName(fileName);
        node.setPath(path.toString());
        node.setIsDirectory(Files.isDirectory(path));
        node.setSize(attrs.size());
        node.setModifyTime(LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault()));
        node.setCreateTime(LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault()));
        node.setIsHidden(fileName.startsWith("."));

        // 设置 leaf 属性：文件为叶子节点，目录不是叶子节点
        node.setLeaf(!Files.isDirectory(path));

        if (Files.isDirectory(path)) {
            List<FileInfo> children = new ArrayList<>();
            List<Path> allEntries = new ArrayList<>();

            try {
                // 先收集所有目录条目
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                    for (Path entry : stream) {
                        String entryName = entry.getFileName().toString();

                        // 过滤隐藏文件
                        if (!includeHidden && entryName.startsWith(".")) {
                            continue;
                        }

                        // 只收集目录，懒加载模式下不递归加载文件
                        if (Files.isDirectory(entry)) {
                            allEntries.add(entry);
                        }
                    }
                }

                // 排序以确保一致的顺序
                allEntries.sort(Comparator.comparing(p -> p.getFileName().toString()));

                // 实现分页
                int totalEntries = allEntries.size();
                int startIndex = pageIndex * pageSize;
                int endIndex = Math.min(startIndex + pageSize, totalEntries);

                if (startIndex < totalEntries) {
                    for (int i = startIndex; i < endIndex; i++) {
                        Path entry = allEntries.get(i);
                        // 创建目录节点，但不递归加载其子目录
                        FileInfo childNode = createDirectoryNodeLazy(entry, includeHidden);
                        children.add(childNode);
                    }
                }

                // 如果有更多数据，设置hasMore标志
                if (endIndex < totalEntries) {
                    // 可以在FileInfo中添加额外属性来标识是否有更多数据
                    node.setExtraProperties("{\"hasMore\": true, \"totalCount\": " + totalEntries + "}");
                }

            } catch (AccessDeniedException e) {
                log.warn("访问目录被拒绝: {}", path);
                // 忽略权限异常
            }

            node.setChildren(children);
        }

        return node;
    }

    /**
     * 创建目录节点（懒加载模式） 只创建节点信息，不加载子目录
     */
    private FileInfo createDirectoryNodeLazy(Path path, boolean includeHidden) throws IOException {
        String fileName = path.getFileName().toString();
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

        FileInfo node = new FileInfo();
        node.setName(fileName);
        node.setPath(path.toString());
        node.setIsDirectory(true);
        node.setSize(0L); // 目录大小设为0
        node.setModifyTime(LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault()));
        node.setCreateTime(LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault()));
        node.setIsHidden(fileName.startsWith("."));

        // 设置 leaf 属性：目录不是叶子节点
        node.setLeaf(false);

        // 检查是否有子目录，用于前端显示展开图标
        boolean hasChildren = false;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    String entryName = entry.getFileName().toString();
                    if (includeHidden || !entryName.startsWith(".")) {
                        hasChildren = true;
                        break;
                    }
                }
            }
        } catch (AccessDeniedException e) {
            // 忽略权限异常
        }

        // 如果有子目录，设置一个空的children列表，让前端知道这个节点可以展开
        if (hasChildren) {
            node.setChildren(new ArrayList<>());
            node.setExtraProperties("{\"hasChildren\": true}");
        }

        return node;
    }

    /**
     * 构建系统根目录树（处理"/"路径请求）
     */
    private FileInfo buildSystemRootTree(Integer maxDepth, Boolean includeHidden, Boolean lazyLoad, Integer pageSize,
            Integer pageIndex) throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        log.info("检测到操作系统: {}", osName);

        if (osName.contains("win")) {
            // Windows系统：返回所有可用磁盘驱动器
            return buildWindowsRootTree(maxDepth, includeHidden, lazyLoad, pageSize, pageIndex);
        } else {
            // Linux/Unix系统：返回真正的根目录"/"
            return buildUnixRootTree(maxDepth, includeHidden, lazyLoad, pageSize, pageIndex);
        }
    }

    /**
     * 构建Windows系统根目录树（磁盘驱动器列表）
     */
    private FileInfo buildWindowsRootTree(Integer maxDepth, Boolean includeHidden, Boolean lazyLoad, Integer pageSize,
            Integer pageIndex) throws IOException {
        log.info("构建Windows系统根目录树");

        // 创建虚拟根节点
        FileInfo rootNode = new FileInfo();
        rootNode.setName("我的电脑");
        rootNode.setPath("/");
        rootNode.setIsDirectory(true);
        rootNode.setSize(0L);
        rootNode.setCreateTime(LocalDateTime.now());
        rootNode.setModifyTime(LocalDateTime.now());
        rootNode.setLeaf(false); // 根节点不是叶子节点

        List<FileInfo> drives = new ArrayList<>();

        // 获取所有可用的文件系统根目录（磁盘驱动器）
        File[] roots = File.listRoots();
        if (roots != null) {
            for (File root : roots) {
                try {
                    FileInfo driveInfo = new FileInfo();
                    String drivePath = root.getAbsolutePath();
                    String driveName = drivePath.replace("\\", "");

                    driveInfo.setName(driveName);
                    driveInfo.setPath(drivePath);
                    driveInfo.setIsDirectory(true);
                    driveInfo.setSize(0L);
                    driveInfo.setCreateTime(LocalDateTime.now());
                    driveInfo.setModifyTime(LocalDateTime.now());
                    driveInfo.setLeaf(false); // 驱动器不是叶子节点

                    // 检查驱动器是否可访问
                    if (root.exists() && root.canRead()) {
                        // 检查是否有子目录，用于懒加载
                        boolean hasChildren = hasSubDirectories(root.toPath(), includeHidden);
                        if (hasChildren) {
                            driveInfo.setChildren(new ArrayList<>());
                            driveInfo.setExtraProperties("{\"hasChildren\": true}");
                        }

                        drives.add(driveInfo);
                        log.debug("添加可用驱动器: {}", driveName);
                    }
                } catch (Exception e) {
                    log.warn("处理驱动器失败: {}", root.getAbsolutePath(), e);
                }
            }
        }

        rootNode.setChildren(drives);
        return rootNode;
    }

    /**
     * 构建Unix/Linux系统根目录树
     */
    private FileInfo buildUnixRootTree(Integer maxDepth, Boolean includeHidden, Boolean lazyLoad, Integer pageSize,
            Integer pageIndex) throws IOException {
        log.info("构建Unix/Linux系统根目录树");

        Path rootPath = Paths.get("/");
        if (lazyLoad) {
            return buildFileTreeLazy(rootPath, maxDepth, includeHidden, pageSize, pageIndex);
        } else {
            return buildFileTree(rootPath, maxDepth, includeHidden, 0);
        }
    }

    /**
     * 检查目录是否有子目录
     */
    private boolean hasSubDirectories(Path path, Boolean includeHidden) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    String entryName = entry.getFileName().toString();
                    if (includeHidden || !entryName.startsWith(".")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("检查子目录失败: {}", path, e);
        }
        return false;
    }

    /**
     * 递归复制目录
     *
     * @param source 源目录路径
     * @param target 目标目录路径（完整路径，不是父目录）
     * @throws IOException 复制过程中的IO异常
     */
    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // 计算相对于源目录的相对路径，然后在目标目录下创建对应的目录结构
                Path targetDir = target.resolve(source.relativize(dir));
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // 计算相对于源目录的相对路径，然后在目标目录下创建对应的文件
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 文件排序
     */
    private void sortFiles(List<FileInfo> files, String sortBy, String sortOrder) {
        Comparator<FileInfo> comparator;

        switch (sortBy.toLowerCase()) {
        case "size":
            comparator = Comparator.comparing(FileInfo::getSize);
            break;
        case "modified":
            comparator = Comparator.comparing(FileInfo::getModifyTime);
            break;
        case "name":
        default:
            comparator = Comparator.comparing(FileInfo::getName, String.CASE_INSENSITIVE_ORDER);
            break;
        }

        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        // 目录优先排序
        files.sort(Comparator.comparing(FileInfo::getIsDirectory).reversed().thenComparing(comparator));
    }

    // 以下是接口要求的其他方法实现，本地文件管理暂不支持或简单实现

    @Override
    public ReturnResult<FileOperationResponse> changePermissions(Integer serverId, String path, String permissions,
            Boolean recursive) {
        return ReturnResult.error("本地文件管理暂不支持权限修改");
    }

    @Override
    public ReturnResult<FileOperationResponse> changeOwner(Integer serverId, String path, String owner, String group,
            Boolean recursive) {
        return ReturnResult.error("本地文件管理暂不支持所有者修改");
    }

    @Override
    public ReturnResult<FileOperationResponse> searchFiles(Integer serverId, String searchPath, String pattern,
            Boolean includeContent, Integer maxResults) {
        return ReturnResult.error("本地文件管理暂不支持文件搜索");
    }

    @Override
    public ReturnResult<FileOperationResponse> compressFiles(Integer serverId, List<String> filePaths,
            String archivePath, String format) {
        return ReturnResult.error("本地文件管理暂不支持文件压缩");
    }

    @Override
    public ReturnResult<FileOperationResponse> extractArchive(Integer serverId, String archivePath, String extractPath,
            Boolean overwrite) {
        return ReturnResult.error("本地文件管理暂不支持文件解压");
    }

    @Override
    public ReturnResult<FileInfo> getFileInfo(Integer serverId, String path) {
        try {
            Path file = Paths.get(path);
            if (!Files.exists(file)) {
                return ReturnResult.error("文件不存在: " + path);
            }

            BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
            String fileName = file.getFileName() != null ? file.getFileName().toString() : file.toString();

            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(fileName);
            fileInfo.setPath(path);
            fileInfo.setSize(attrs.size());
            fileInfo.setIsDirectory(Files.isDirectory(file));
            fileInfo.setModifyTime(
                    LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault()));
            fileInfo.setCreateTime(LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.systemDefault()));
            fileInfo.setIsHidden(fileName.startsWith("."));

            return ReturnResult.ok(fileInfo);

        } catch (Exception e) {
            log.error("本地文件管理: 获取文件信息失败 - serverId={}, path={}", serverId, path, e);
            return ReturnResult.error("获取文件信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<FileOperationResponse> batchDeleteFiles(Integer serverId, List<String> paths,
            Boolean recursive) {
        return ReturnResult.error("本地文件管理暂不支持批量删除");
    }

    @Override
    public ReturnResult<List<String>> getSupportedOperations(Integer serverId) {
        List<String> operations = Arrays.asList("LIST", "TREE", "UPLOAD", "DOWNLOAD", "DELETE", "RENAME", "MKDIR",
                "COPY", "MOVE", "INFO");
        return ReturnResult.ok(operations);
    }

    @Override
    public ReturnResult<FileOperationResponse> getDiskUsage(Integer serverId, String path) {
        return ReturnResult.error("本地文件管理暂不支持磁盘使用情况查询");
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileStatistics(Integer serverId, String path,
            Boolean includeSubdirectories) {
        return ReturnResult.error("本地文件管理暂不支持文件统计");
    }

    @Override
    public ReturnResult<String> watchFileChanges(Integer serverId, String path, Boolean recursive) {
        return ReturnResult.error("本地文件管理暂不支持文件监控");
    }

    @Override
    public ReturnResult<Boolean> stopWatching(Integer serverId, String watchId) {
        return ReturnResult.error("本地文件管理暂不支持文件监控");
    }

    @Override
    public ReturnResult<String> getFileChecksum(Integer serverId, String filePath, String algorithm) {
        return ReturnResult.error("本地文件管理暂不支持文件校验和");
    }

    @Override
    public ReturnResult<FileOperationResponse> syncFiles(Integer serverId, String sourcePath, String targetPath,
            Boolean deleteExtra) {
        return ReturnResult.error("本地文件管理暂不支持文件同步");
    }

    @Override
    public ReturnResult<FileOperationResponse> createSymbolicLink(Integer serverId, String targetPath,
            String linkPath) {
        return ReturnResult.error("本地文件管理暂不支持符号链接");
    }

    @Override
    public ReturnResult<FileOperationResponse> getFileSystemInfo(Integer serverId) {
        return ReturnResult.error("本地文件管理暂不支持文件系统信息查询");
    }

    @Override
    public ReturnResult<FileOperationResponse> previewFile(Integer serverId, String filePath, String previewType,
            Integer maxSizeMB) {
        log.info("预览文件: serverId={}, filePath={}, previewType={}, maxSizeMB={}", serverId, filePath, previewType,
                maxSizeMB);

        FileOperationResponse response = new FileOperationResponse();
        response.setOperation("preview");

        try {
            Path path = Paths.get(filePath);

            // 检查文件是否存在
            if (!Files.exists(path)) {
                response.setSuccess(false);
                response.setMessage("文件不存在: " + filePath);
                return ReturnResult.ok(response);
            }

            // 检查是否为文件
            if (Files.isDirectory(path)) {
                response.setSuccess(false);
                response.setMessage("无法预览目录: " + filePath);
                return ReturnResult.ok(response);
            }

            // 检查文件大小
            long fileSizeBytes = Files.size(path);
            long maxSizeBytes = maxSizeMB * 1024L * 1024L;
            if (fileSizeBytes > maxSizeBytes) {
                response.setSuccess(false);
                response.setMessage(
                        "文件过大，无法预览。文件大小: " + (fileSizeBytes / 1024 / 1024) + "MB，最大限制: " + maxSizeMB + "MB");
                return ReturnResult.ok(response);
            }

            // 根据文件类型和预览类型处理
            String fileName = path.getFileName().toString().toLowerCase();
            String actualPreviewType = determinePreviewType(fileName, previewType);

            switch (actualPreviewType) {
            case "text":
                return previewTextFile(path, response);
            case "image":
                return previewImageFile(path, response);
                case "pdf":
                    return previewPdfFile(path, response);
            case "binary":
                response.setSuccess(false);
                response.setMessage("二进制文件无法预览");
                return ReturnResult.ok(response);
            default:
                response.setSuccess(false);
                response.setMessage("不支持的预览类型: " + actualPreviewType);
                return ReturnResult.ok(response);
            }

        } catch (Exception e) {
            log.error("预览文件失败: serverId={}, filePath={}", serverId, filePath, e);
            response.setSuccess(false);
            response.setMessage("预览文件失败: " + e.getMessage());
            return ReturnResult.ok(response);
        }
    }

    private ReturnResult<FileOperationResponse> previewPdfFile(Path path, FileOperationResponse response) throws IOException {
        response.setSuccess(true);
        response.setMessage("预览成功");
        response.setContentType("application/pdf");
        response.setMediaType(APPLICATION_PDF);
        response.setContentBytes(Files.readAllBytes(path));
        return ReturnResult.ok(response);
    }

    /**
     * 确定预览类型
     */
    private String determinePreviewType(String fileName, String requestedType) {
        if (!"auto".equals(requestedType)) {
            return requestedType;
        }

        // 根据文件扩展名自动判断
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1);

        if ("pdf".equals(ext)) {
            return "pdf";
        }

        // 图片文件
        if (ext.matches("jpg|jpeg|png|gif|bmp|svg|webp|ico")) {
            return "image";
        }

        // 文本文件
        if (ext.matches(
                "txt|md|json|xml|html|htm|css|js|ts|vue|py|java|cpp|c|h|hpp|cs|php|rb|go|rs|yaml|yml|toml|ini|conf|config|log|jsx|tsx|scss|sass|less|stylus|sql|sh|bat|ps1")) {
            return "text";
        }

        return "binary";
    }

    /**
     * 预览文本文件
     */
    private ReturnResult<FileOperationResponse> previewTextFile(Path path, FileOperationResponse response)
            throws IOException {
        response.setSuccess(true);
        response.setMessage("文本文件预览成功");
        response.setContentType("text");
        response.setMediaType(TEXT_PLAIN);
        response.setContentBytes(Files.readAllBytes(path));
        return ReturnResult.ok(response);
    }

    /**
     * 预览图片文件
     */
    private ReturnResult<FileOperationResponse> previewImageFile(Path path, FileOperationResponse response)
            throws IOException {
        byte[] imageBytes = Files.readAllBytes(path);

        // 获取MIME类型
        String mimeType = Files.probeContentType(path);
        if (mimeType == null) {
            String ext = path.getFileName().toString().toLowerCase();
            if (ext.endsWith(".jpg") || ext.endsWith(".jpeg")) {
                mimeType = "image/jpeg";
            } else if (ext.endsWith(".png")) {
                mimeType = "image/png";
            } else if (ext.endsWith(".webp")) {
                mimeType = "image/webp";
            } else if (ext.endsWith(".gif")) {
                mimeType = "image/gif";
            } else {
                mimeType = "image/unknown";
            }
        }

        response.setMediaType(MediaType.parseMediaTypes(mimeType).getFirst());
        response.setContentBytes(imageBytes);
        response.setSuccess(true);
        response.setMessage("图片文件预览成功");
        response.setContentType("image");
        return ReturnResult.ok(response);
    }
}
