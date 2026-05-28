package com.chua.starter.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.workflow.entity.WfTaskHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流转历史 Mapper
 */
@Mapper
public interface WfTaskHistoryMapper extends BaseMapper<WfTaskHistory> {

    /**
     * 查询实例的流转历史
     */
    List<WfTaskHistory> selectByInstId(@Param("instId") Integer instId);
}
