package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.service.SysRequestTraceBehavior;
import org.apache.ibatis.annotations.Mapper;

/**
* 请求追踪行为结果映射器。
*
* @since 2026/03/25
* @author CH
 */
@Mapper
public interface SysRequestTraceBehaviorMapper extends BaseMapper<SysRequestTraceBehavior> {
}
