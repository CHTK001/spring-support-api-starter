package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务器分组Mapper接口
 *
 * @author CH
 * @since 2025/01/11
 */
@Mapper
public interface MonitorSysGenServerGroupMapper extends BaseMapper<MonitorSysGenServerGroup> {

    /**
     * 根据状态获取分组列表
     *
     * @param status 状态 0:禁用 1:启用
     * @return 分组列表
     */
    List<MonitorSysGenServerGroup> selectByStatus(@Param("status") Integer status);

    /**
     * 获取默认分组
     *
     * @return 默认分组
     */
    MonitorSysGenServerGroup selectDefaultGroup();

    /**
     * 清除所有分组的默认状态
     *
     * @return 影响行数
     */
    int clearDefaultGroups();

    /**
     * 获取最大排序号
     *
     * @return 最大排序号
     */
    Integer selectMaxSort();

    /**
     * 批量更新排序号
     *
     * @param groups 分组列表
     * @return 影响行数
     */
    int batchUpdateSort(@Param("groups") List<MonitorSysGenServerGroup> groups);

    /**
     * 根据名称查询分组（排除指定ID）
     *
     * @param groupName 分组名称
     * @param excludeId 排除的分组ID
     * @return 分组数量
     */
    long countByNameExcludeId(@Param("groupName") String groupName, @Param("excludeId") Integer excludeId);
}
