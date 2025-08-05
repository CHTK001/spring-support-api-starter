package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.FileSystemPart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件系统分片管理 Mapper 接口
 *
 * @author CH
 * @since 2024/12/30
 */
@Mapper
public interface FileSystemPartMapper extends BaseMapper<FileSystemPart> {

    /**
     * 根据文件ID查询所有分片
     *
     * @param fileId 文件ID
     * @return 分片列表
     */
    List<FileSystemPart> findByFileId(@Param("fileId") Integer fileId);

    /**
     * 根据文件ID和分片序号查询分片
     *
     * @param fileId 文件ID
     * @param chunkNumber 分片序号
     * @return 分片信息
     */
    FileSystemPart findByFileIdAndChunkNumber(@Param("fileId") Integer fileId, @Param("chunkNumber") Integer chunkNumber);

    /**
     * 统计文件已完成的分片数
     *
     * @param fileId 文件ID
     * @return 已完成分片数
     */
    int countCompletedChunks(@Param("fileId") Integer fileId);

    /**
     * 更新分片状态
     *
     * @param partId 分片ID
     * @param status 新状态
     * @return 更新的记录数
     */
    int updateStatus(@Param("partId") Integer partId, @Param("status") Integer status);

    /**
     * 批量更新文件的所有分片状态
     *
     * @param fileId 文件ID
     * @param status 新状态
     * @return 更新的记录数
     */
    int updateStatusByFileId(@Param("fileId") Integer fileId, @Param("status") Integer status);

    /**
     * 根据文件ID删除所有分片
     *
     * @param fileId 文件ID
     * @return 删除的记录数
     */
    int deleteByFileId(@Param("fileId") Integer fileId);

    /**
     * 查询待处理的分片
     *
     * @param limit 限制数量
     * @return 待处理分片列表
     */
    List<FileSystemPart> findPendingChunks(@Param("limit") Integer limit);

    /**
     * 查询失败的分片(可重试)
     *
     * @param maxRetryCount 最大重试次数
     * @param limit 限制数量
     * @return 失败分片列表
     */
    List<FileSystemPart> findFailedChunksForRetry(@Param("maxRetryCount") Integer maxRetryCount, @Param("limit") Integer limit);

    /**
     * 增加重试次数
     *
     * @param partId 分片ID
     * @return 更新的记录数
     */
    int incrementRetryCount(@Param("partId") Integer partId);
}
