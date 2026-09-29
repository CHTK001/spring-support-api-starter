package com.chua.starter.collect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.collect.entity.CollectTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 采集任务 Mapper。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Mapper
public interface CollectTaskMapper extends BaseMapper<CollectTask> {

    /**
     * 累加任务计量：节点执行次数、抓取次数、记录数各列独立自增。
     *
     * @param taskId      任务ID
     * @param nodeCount   本次新增节点执行次数
     * @param requestCount 本次新增抓取次数
     * @param recordCount 本次新增记录数
     * @return 影响行数
     */
    @Update("update collect_task set "
            + "collect_task_node_count = coalesce(collect_task_node_count, 0) + #{nodeCount}, "
            + "collect_task_request_count = coalesce(collect_task_request_count, 0) + #{requestCount}, "
            + "collect_task_record_count = coalesce(collect_task_record_count, 0) + #{recordCount} "
            + "where collect_task_id = #{taskId}")
    int accumulate(@Param("taskId") Long taskId,
                   @Param("nodeCount") int nodeCount,
                   @Param("requestCount") int requestCount,
                   @Param("recordCount") int recordCount);
}
