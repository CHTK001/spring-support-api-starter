package com.chua.starter.collect.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.collect.entity.CollectFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 流程定义 Mapper。
 *
 * <p>到点查询与抢占已经交给 job-starter 的 {@code sys_job} 台账，这里只保留本表必须自己记的账。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Mapper
public interface CollectFlowMapper extends BaseMapper<CollectFlow> {

    /**
     * 记一次开始执行，只写最近开始时间与累计次数。
     *
     * @param flowId 流程ID
     * @param begin  开始时间
     * @return 影响行数
     */
    @Update("update collect_flow set collect_flow_last_exec_time = #{begin}, "
            + "collect_flow_exec_count = coalesce(collect_flow_exec_count, 0) + 1 "
            + "where collect_flow_id = #{flowId}")
    int markStarted(@Param("flowId") Integer flowId, @Param("begin") LocalDateTime begin);
}
