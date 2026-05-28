package com.chua.starter.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.workflow.entity.WfDefEdge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流程边定义 Mapper
 */
@Mapper
public interface WfDefEdgeMapper extends BaseMapper<WfDefEdge> {

    /**
     * 根据流程定义ID查询所有边
     */
    List<WfDefEdge> selectByDefId(@Param("defId") Integer defId);
}
