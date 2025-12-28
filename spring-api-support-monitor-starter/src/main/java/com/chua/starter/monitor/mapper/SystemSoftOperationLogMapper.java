package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.SystemSoftOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统软件操作日志Mapper
 *
 * @author CH
 * @since 2025/12/28
 */
@Mapper
public interface SystemSoftOperationLogMapper extends BaseMapper<SystemSoftOperationLog> {

    /**
     * 根据操作ID查询日志列表
     *
     * @param operationId 操作ID
     * @return 日志列表
     */
    @Select("SELECT * FROM system_soft_operation_log WHERE operation_id = #{operationId} ORDER BY log_time ASC")
    List<SystemSoftOperationLog> selectByOperationId(@Param("operationId") String operationId);

    /**
     * 根据容器ID查询最近的操作日志
     *
     * @param containerId 容器ID
     * @param limit 限制数量
     * @return 日志列表
     */
    @Select("SELECT * FROM system_soft_operation_log WHERE container_id = #{containerId} ORDER BY log_time DESC LIMIT #{limit}")
    List<SystemSoftOperationLog> selectRecentByContainerId(@Param("containerId") Integer containerId, @Param("limit") int limit);

    /**
     * 根据服务器ID查询最近的操作日志
     *
     * @param serverId 服务器ID
     * @param limit 限制数量
     * @return 日志列表
     */
    @Select("SELECT * FROM system_soft_operation_log WHERE server_id = #{serverId} ORDER BY log_time DESC LIMIT #{limit}")
    List<SystemSoftOperationLog> selectRecentByServerId(@Param("serverId") Integer serverId, @Param("limit") int limit);
}
