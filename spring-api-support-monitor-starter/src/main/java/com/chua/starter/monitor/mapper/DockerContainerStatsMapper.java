package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.DockerContainerStats;
import org.apache.ibatis.annotations.Mapper;

/**
 * 容器统计信息Mapper接口
 *
 * @author CH
 * @since 2025/01/15
 */
@Mapper
public interface DockerContainerStatsMapper extends BaseMapper<DockerContainerStats> {

}