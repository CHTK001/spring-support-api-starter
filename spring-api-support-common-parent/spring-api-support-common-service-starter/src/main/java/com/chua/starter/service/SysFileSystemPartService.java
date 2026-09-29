package com.chua.starter.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.entity.service.SysFileSystemPart;

import java.util.List;

/**
 * 文件系统分片服务接口
 *
 * <p>提供分片上传的查询、状态管理、合并辅助等业务能力。
 * 底层 CRUD 由 MyBatis Plus {@link IService} 提供。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
public interface SysFileSystemPartService extends IService<SysFileSystemPart> {

    /**
     * 根据文件主键查询所有分片,按 seq 升序。
     *
     * @param fileId 文件主键
     * @return 分片列表
     */
    List<SysFileSystemPart> listByFileId(Integer fileId);

    /**
     * 根据文件主键 + 版本号查询分片,按 seq 升序。
     *
     * @param fileId  文件主键
     * @param version 分片版本号(乐观锁)
     * @return 分片列表
     */
    List<SysFileSystemPart> listByFileIdAndVersion(Integer fileId, Integer version);

    /**
     * 删除指定文件的所有分片(级联清理)。
     *
     * @param fileId 文件主键
     * @return 删除的分片数量
     */
    int deleteByFileId(Integer fileId);

    /**
     * 统计已上传完成分片数。
     *
     * @param fileId 文件主键
     * @return 完成分片数
     */
    long countCompletedByFileId(Integer fileId);

    /**
     * 重置失败分片为待上传(用于断点续传重试)。
     *
     * @param fileId 文件主键
     * @return 更新条数
     */
    int resetFailedToPending(Integer fileId);

    /**
     * 将所有分片标记为处理中(合并开始)。
     *
     * @param fileId 文件主键
     * @return 更新条数
     */
    int markProcessing(Integer fileId);

    /**
     * 校验分片完整性(总数与文件记录中的 chunk 数是否一致)。
     *
     * @param fileId 文件主键
     * @param expectedChunks 期望分片数
     * @return true 表示所有分片都已上传完成
     */
    ReturnResult<Boolean> verifyIntegrity(Integer fileId, int expectedChunks);
}
