package com.chua.starter.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.workflow.entity.WfInst;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 流程实例 Mapper
 */
@Mapper
public interface WfInstMapper extends BaseMapper<WfInst> {

    /**
     * 根据幂等键查询实例
     */
    WfInst selectByIdempotentKey(@Param("key") String idempotentKey);

    /**
     * 更新实例状态(带乐观锁/状态检查)
     */
    int updateStatus(@Param("instId") Integer instId,
                     @Param("oldStatus") String oldStatus,
                     @Param("newStatus") String newStatus,
                     @Param("currentNodeId") Integer currentNodeId);
}
