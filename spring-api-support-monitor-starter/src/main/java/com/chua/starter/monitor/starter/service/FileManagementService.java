package com.chua.starter.monitor.starter.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.pojo.FileInfo;
import com.chua.starter.monitor.starter.pojo.FileOperationRequest;
import com.chua.starter.monitor.starter.pojo.FileOperationResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 文件管理服务接口
 *
 * @author CH
 * @since 2024/12/13
 */
public interface FileManagementService {

    /**
     * 列出文件
     *
     * @param serverId      服务器ID
     * @param path          目录路径
     * @param includeHidden 是否包含隐藏文件
     * @param sortBy        排序字段
     * @param sortOrder     排序顺序
     * @return 文件列表
     */
    ReturnResult<FileOperationResponse> listFiles(Integer serverId, String path, Boolean includeHidden, String sortBy,
            String sortOrder);

    /**
     * 获取文件树
     *
     * @param serverId      服务器ID
     * @param path          根目录路径
     * @param maxDepth      最大深度
     * @param includeHidden 是否包含隐藏文件
     * @return 文件树
     */
    ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth,
            Boolean includeHidden);

    /**
     * 获取文件树（支持懒加载）
     *
     * @param serverId      服务器ID
     * @param path          根目录路径
     * @param maxDepth      最大深度
     * @param includeHidden 是否包含隐藏文件
     * @param lazyLoad      是否启用懒加载模式
     * @param pageSize      每页文件数量限制
     * @param pageIndex     页码（从0开始）
     * @return 文件树
     */
    ReturnResult<FileOperationResponse> getFileTree(Integer serverId, String path, Integer maxDepth,
            Boolean includeHidden, Boolean lazyLoad, Integer pageSize, Integer pageIndex);

    /**
     * 上传文件
     *
     * @param serverId   服务器ID
     * @param targetPath 目标路径
     * @param file       上传的文件
     * @param overwrite  是否覆盖
     * @return 上传结果
     */
    ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, MultipartFile file,
            Boolean overwrite);

    /**
     * 上传文件（流方式）
     *
     * @param serverId    服务器ID
     * @param targetPath  目标路径
     * @param fileName    文件名
     * @param inputStream 文件流
     * @param overwrite   是否覆盖
     * @return 上传结果
     */
    ReturnResult<FileOperationResponse> uploadFile(Integer serverId, String targetPath, String fileName,
            InputStream inputStream, Boolean overwrite);

    /**
     * 下载文件
     *
     * @param serverId 服务器ID
     * @param filePath 文件路径
     * @return 下载结果
     */
    ReturnResult<FileOperationResponse> downloadFile(Integer serverId, String filePath);

    /**
     * 预览文件
     *
     * @param serverId    服务器ID
     * @param filePath    文件路径
     * @param previewType 预览类型 (auto/text/image/binary)
     * @param maxSizeMB   最大文件大小(MB)
     * @return 预览结果
     */
    ReturnResult<FileOperationResponse> previewFile(Integer serverId, String filePath, String previewType,
            Integer maxSizeMB);

    /**
     * 删除文件
     *
     * @param serverId  服务器ID
     * @param path      文件路径
     * @param recursive 是否递归删除
     * @return 删除结果
     */
    ReturnResult<FileOperationResponse> deleteFile(Integer serverId, String path, Boolean recursive);

    /**
     * 重命名文件
     *
     * @param serverId 服务器ID
     * @param oldPath  原文件路径
     * @param newName  新文件名
     * @return 重命名结果
     */
    ReturnResult<FileOperationResponse> renameFile(Integer serverId, String oldPath, String newName);

    /**
     * 创建目录
     *
     * @param serverId  服务器ID
     * @param path      目录路径
     * @param recursive 是否递归创建
     * @return 创建结果
     */
    ReturnResult<FileOperationResponse> createDirectory(Integer serverId, String path, Boolean recursive);

    /**
     * 复制文件
     *
     * @param serverId   服务器ID
     * @param sourcePath 源文件路径
     * @param targetPath 目标路径
     * @param overwrite  是否覆盖
     * @return 复制结果
     */
    ReturnResult<FileOperationResponse> copyFile(Integer serverId, String sourcePath, String targetPath,
            Boolean overwrite);

    /**
     * 移动文件
     *
     * @param serverId   服务器ID
     * @param sourcePath 源文件路径
     * @param targetPath 目标路径
     * @param overwrite  是否覆盖
     * @return 移动结果
     */
    ReturnResult<FileOperationResponse> moveFile(Integer serverId, String sourcePath, String targetPath,
            Boolean overwrite);

    /**
     * 修改文件权限
     *
     * @param serverId    服务器ID
     * @param path        文件路径
     * @param permissions 权限（八进制格式）
     * @param recursive   是否递归应用
     * @return 修改结果
     */
    ReturnResult<FileOperationResponse> changePermissions(Integer serverId, String path, String permissions,
            Boolean recursive);

    /**
     * 修改文件所有者
     *
     * @param serverId  服务器ID
     * @param path      文件路径
     * @param owner     所有者
     * @param group     组
     * @param recursive 是否递归应用
     * @return 修改结果
     */
    ReturnResult<FileOperationResponse> changeOwner(Integer serverId, String path, String owner, String group,
            Boolean recursive);

    /**
     * 搜索文件
     *
     * @param serverId       服务器ID
     * @param searchPath     搜索路径
     * @param pattern        搜索模式
     * @param includeContent 是否包含内容搜索
     * @param maxResults     最大结果数
     * @return 搜索结果
     */
    ReturnResult<FileOperationResponse> searchFiles(Integer serverId, String searchPath, String pattern,
            Boolean includeContent, Integer maxResults);

    /**
     * 压缩文件
     *
     * @param serverId    服务器ID
     * @param filePaths   文件路径列表
     * @param archivePath 压缩包路径
     * @param format      压缩格式
     * @return 压缩结果
     */
    ReturnResult<FileOperationResponse> compressFiles(Integer serverId, List<String> filePaths, String archivePath,
            String format);

    /**
     * 解压文件
     *
     * @param serverId    服务器ID
     * @param archivePath 压缩包路径
     * @param extractPath 解压路径
     * @param overwrite   是否覆盖
     * @return 解压结果
     */
    ReturnResult<FileOperationResponse> extractArchive(Integer serverId, String archivePath, String extractPath,
            Boolean overwrite);

    /**
     * 获取文件信息
     *
     * @param serverId 服务器ID
     * @param path     文件路径
     * @return 文件信息
     */
    ReturnResult<FileInfo> getFileInfo(Integer serverId, String path);

    /**
     * 执行文件操作
     *
     * @param serverId 服务器ID
     * @param request  操作请求
     * @return 操作结果
     */
    ReturnResult<FileOperationResponse> executeOperation(Integer serverId, FileOperationRequest request);

    /**
     * 批量删除文件
     *
     * @param serverId  服务器ID
     * @param paths     文件路径列表
     * @param recursive 是否递归删除
     * @return 删除结果
     */
    ReturnResult<FileOperationResponse> batchDeleteFiles(Integer serverId, List<String> paths, Boolean recursive);

    /**
     * 获取支持的操作列表
     *
     * @param serverId 服务器ID
     * @return 支持的操作列表
     */
    ReturnResult<List<String>> getSupportedOperations(Integer serverId);

    /**
     * 测试连接
     *
     * @param serverId 服务器ID
     * @return 连接测试结果
     */
    ReturnResult<Boolean> testConnection(Integer serverId);

    /**
     * 获取磁盘使用情况
     *
     * @param serverId 服务器ID
     * @param path     路径
     * @return 磁盘使用情况
     */
    ReturnResult<FileOperationResponse> getDiskUsage(Integer serverId, String path);

    /**
     * 获取文件统计信息
     *
     * @param serverId              服务器ID
     * @param path                  路径
     * @param includeSubdirectories 是否包含子目录
     * @return 文件统计信息
     */
    ReturnResult<FileOperationResponse> getFileStatistics(Integer serverId, String path, Boolean includeSubdirectories);

    /**
     * 监控文件变化
     *
     * @param serverId  服务器ID
     * @param path      监控路径
     * @param recursive 是否递归监控
     * @return 监控结果
     */
    ReturnResult<String> watchFileChanges(Integer serverId, String path, Boolean recursive);

    /**
     * 停止文件监控
     *
     * @param serverId 服务器ID
     * @param watchId  监控ID
     * @return 停止结果
     */
    ReturnResult<Boolean> stopWatching(Integer serverId, String watchId);

    /**
     * 获取文件内容摘要
     *
     * @param serverId  服务器ID
     * @param filePath  文件路径
     * @param algorithm 摘要算法（MD5, SHA1, SHA256等）
     * @return 文件摘要
     */
    ReturnResult<String> getFileChecksum(Integer serverId, String filePath, String algorithm);

    /**
     * 同步文件
     *
     * @param serverId    服务器ID
     * @param sourcePath  源路径
     * @param targetPath  目标路径
     * @param deleteExtra 是否删除多余文件
     * @return 同步结果
     */
    ReturnResult<FileOperationResponse> syncFiles(Integer serverId, String sourcePath, String targetPath,
            Boolean deleteExtra);

    /**
     * 创建符号链接
     *
     * @param serverId   服务器ID
     * @param targetPath 目标路径
     * @param linkPath   链接路径
     * @return 创建结果
     */
    ReturnResult<FileOperationResponse> createSymbolicLink(Integer serverId, String targetPath, String linkPath);

    /**
     * 获取文件系统信息
     *
     * @param serverId 服务器ID
     * @return 文件系统信息
     */
    ReturnResult<FileOperationResponse> getFileSystemInfo(Integer serverId);
}
