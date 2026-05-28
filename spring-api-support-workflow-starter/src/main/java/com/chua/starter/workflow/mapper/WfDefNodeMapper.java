package com.chua.starter.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.workflow.entity.WfDefNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流程节点定义 Mapper
 */
@Mapper
public interface WfDefNodeMapper extends BaseMapper<WfDefNode> {

    /**
     * 根据流程定义ID查询所有节点，按排序字段排序
     */
    List<WfDefNode> selectByDefIdOrderBySort(@Param("defId") Integer defId);
}
