package com.chua.starter.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.workflow.entity.WfDef;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程定义 Mapper
 */
@Mapper
public interface WfDefMapper extends BaseMapper<WfDef> {
}
