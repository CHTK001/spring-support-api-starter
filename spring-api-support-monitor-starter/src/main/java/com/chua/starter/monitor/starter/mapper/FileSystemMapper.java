package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.FileSystem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件系统管理 Mapper 接口
 *
 * @author CH
 * @since 2024/12/30
 */
@Mapper
public interface FileSystemMapper extends BaseMapper<FileSystem> {

    /**
     * 根据MD5查找文件
     *
     * @param fileMd5 文件MD5值
     * @return 文件信息
     */
    FileSystem findByMd5(@Param("fileMd5") String fileMd5);

    /**
     * 根据状态查询文件列表
     *
     * @param status 文件状态
     * @return 文件列表
     */
    List<FileSystem> findByStatus(@Param("status") Integer status);

    /**
     * 更新文件状态
     *
     * @param fileId 文件ID
     * @param status 新状态
     * @return 更新的记录数
     */
    int updateStatus(@Param("fileId") Integer fileId, @Param("status") Integer status);

    /**
     * 更新已上传分片数
     *
     * @param fileId 文件ID
     * @param chunkUploaded 已上传分片数
     * @return 更新的记录数
     */
    int updateChunkUploaded(@Param("fileId") Integer fileId, @Param("chunkUploaded") Integer chunkUploaded);

    /**
     * 批量删除过期文件
     *
     * @param retentionDays 保留天数
     * @return 删除的记录数
     */
    int deleteExpiredFiles(@Param("retentionDays") Integer retentionDays);

    /**
     * 统计各状态文件数量
     *
     * @return 状态统计信息
     */
    List<java.util.Map<String, Object>> countByStatus();
}
