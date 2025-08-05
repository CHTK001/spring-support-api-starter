package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.FileSystem;
import com.chua.starter.monitor.pojo.FileSystemStatisticsDTO;
import com.chua.starter.monitor.pojo.FileUploadStatusDTO;
import com.chua.starter.monitor.pojo.FileInitUploadDTO;
import com.chua.starter.monitor.pojo.response.FileSystemResponse;
import com.chua.starter.mybatis.entity.Query;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文件系统管理服务接口
 *
 * @author CH
 * @since 2024/12/30
 */
public interface FileSystemService extends IService<FileSystem> {

    /**
     * 获取文件列表
     *
     * @param absolutePath 绝对路径
     * @param name         文件名
     * @return 文件列表
     */
    ReturnResult<List<FileSystemResponse>> list(String absolutePath, String name);

    /**
     * 分页查询文件列表
     *
     * @param query      查询条件
     * @param fileSystem 查询参数
     * @return 分页结果
     */
    ReturnPageResult<FileSystem> pageFor(Query<FileSystem> query, FileSystem fileSystem);

    /**
     * 初始化分片上传
     *
     * @param fileName  文件名
     * @param fileSize  文件大小
     * @param fileMd5   文件MD5
     * @param chunkSize 分片大小
     * @param groupId   文件分组ID
     * @return 初始化结果，包含文件ID和分片数量
     */
    ReturnResult<FileInitUploadDTO> initChunkUpload(String fileName, Long fileSize, String fileMd5, Long chunkSize,
            Integer groupId);

    /**
     * 上传文件分片
     *
     * @param fileId      文件ID
     * @param chunkNumber 分片序号
     * @param chunkFile   分片文件
     * @param chunkMd5    分片MD5
     * @return 上传结果
     */
    ReturnResult<Boolean> uploadChunk(Integer fileId, Integer chunkNumber, MultipartFile chunkFile, String chunkMd5);

    /**
     * 检查分片上传状态
     *
     * @param fileId 文件ID
     * @return 上传状态信息
     */
    ReturnResult<FileUploadStatusDTO> checkUploadStatus(Integer fileId);

    /**
     * 手动触发文件合并
     *
     * @param fileId 文件ID
     * @return 合并结果
     */
    ReturnResult<Boolean> manualMergeFile(Integer fileId);

    /**
     * 设置文件分组
     *
     * @param fileId  文件ID
     * @param groupId 分组ID，null表示移除分组
     * @return 设置结果
     */
    ReturnResult<Boolean> setFileGroup(Integer fileId, Integer groupId);

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteFile(Integer fileId);

    /**
     * 批量删除文件
     *
     * @param fileIds 文件ID列表
     * @return 删除结果
     */
    ReturnResult<Boolean> batchDeleteFiles(List<Integer> fileIds);

    /**
     * 下载文件
     *
     * @param fileId 文件ID
     * @return 文件字节数组
     */
    ReturnResult<byte[]> downloadFile(Integer fileId);

    /**
     * 获取文件HTTP访问URL
     *
     * @param fileId 文件ID
     * @return HTTP访问URL
     */
    ReturnResult<String> getHttpAccessUrl(Integer fileId);

    /**
     * 启用/禁用文件HTTP访问
     *
     * @param fileId  文件ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    ReturnResult<Boolean> toggleHttpAccess(Integer fileId, Boolean enabled);

    /**
     * 获取文件统计信息
     *
     * @return 统计信息
     */
    ReturnResult<FileSystemStatisticsDTO> getFileStatistics();

    /**
     * 清理过期文件
     *
     * @return 清理结果
     */
    ReturnResult<Integer> cleanExpiredFiles();

    /**
     * 重试失败的合并任务
     *
     * @param fileId 文件ID
     * @return 重试结果
     */
    ReturnResult<Boolean> retryMergeTask(Integer fileId);

    /**
     * 获取文件详情
     *
     * @param fileId 文件ID
     * @return 文件详情
     */
    ReturnResult<FileSystem> getFileDetail(Integer fileId);

    /**
     * 检查文件是否存在(根据MD5)
     *
     * @param fileMd5 文件MD5
     * @return 检查结果
     */
    ReturnResult<FileSystem> checkFileExists(String fileMd5);

    /**
     * 获取存储统计信息
     *
     * @return 存储统计信息
     */
    Map<String, Object> getStorageStatistics();
}
