package com.chua.starter.monitor.service.file;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.FileSystemPart;

import java.util.List;

/**
 * 文件系统分片管理服务接口
 *
 * @author CH
 * @since 2024/12/30
 */
public interface FileSystemPartService extends IService<FileSystemPart> {

    /**
     * 根据文件ID查询所有分片
     *
     * @param fileId 文件ID
     * @return 分片列表
     */
    List<FileSystemPart> findByFileId(Integer fileId);

    /**
     * 根据文件ID和分片序号查询分片
     *
     * @param fileId 文件ID
     * @param chunkNumber 分片序号
     * @return 分片信息
     */
    FileSystemPart findByFileIdAndChunkNumber(Integer fileId, Integer chunkNumber);

    /**
     * 统计文件已完成的分片数
     *
     * @param fileId 文件ID
     * @return 已完成分片数
     */
    int countCompletedChunks(Integer fileId);

    /**
     * 更新分片状态
     *
     * @param partId 分片ID
     * @param status 新状态
     * @return 更新结果
     */
    boolean updateStatus(Integer partId, Integer status);

    /**
     * 批量更新文件的所有分片状态
     *
     * @param fileId 文件ID
     * @param status 新状态
     * @return 更新结果
     */
    boolean updateStatusByFileId(Integer fileId, Integer status);

    /**
     * 根据文件ID删除所有分片
     *
     * @param fileId 文件ID
     * @return 删除结果
     */
    boolean deleteByFileId(Integer fileId);

    /**
     * 查询待处理的分片
     *
     * @param limit 限制数量
     * @return 待处理分片列表
     */
    List<FileSystemPart> findPendingChunks(Integer limit);

    /**
     * 查询失败的分片(可重试)
     *
     * @param maxRetryCount 最大重试次数
     * @param limit 限制数量
     * @return 失败分片列表
     */
    List<FileSystemPart> findFailedChunksForRetry(Integer maxRetryCount, Integer limit);

    /**
     * 增加重试次数
     *
     * @param partId 分片ID
     * @return 更新结果
     */
    boolean incrementRetryCount(Integer partId);
}
