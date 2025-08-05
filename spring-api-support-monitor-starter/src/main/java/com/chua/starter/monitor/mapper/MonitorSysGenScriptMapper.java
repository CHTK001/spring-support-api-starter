package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenScript;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 脚本管理Mapper
 * 
 * @author CH
 * @since 2024/12/19
 */
@Mapper
public interface MonitorSysGenScriptMapper extends BaseMapper<MonitorSysGenScript> {

    /**
     * 根据脚本名称查找脚本
     *
     * @param scriptName 脚本名称
     * @return 脚本信息
     */
    MonitorSysGenScript findByScriptName(@Param("scriptName") String scriptName);

    /**
     * 根据脚本类型查找脚本
     *
     * @param scriptType 脚本类型
     * @return 脚本列表
     */
    List<MonitorSysGenScript> findByScriptType(@Param("scriptType") String scriptType);

    /**
     * 根据分类查找脚本
     *
     * @param category 分类名称
     * @return 脚本列表
     */
    List<MonitorSysGenScript> findByCategory(@Param("category") String category);

    /**
     * 根据标签查找脚本
     *
     * @param tag 标签
     * @return 脚本列表
     */
    List<MonitorSysGenScript> findByTag(@Param("tag") String tag);

    /**
     * 更新脚本执行统计
     *
     * @param scriptId    脚本ID
     * @param executeTime 执行时间
     * @return 更新的记录数
     */
    int updateExecuteStatistics(@Param("scriptId") Integer scriptId, @Param("executeTime") LocalDateTime executeTime);

    /**
     * 获取脚本统计信息
     *
     * @return 脚本统计信息
     */
    ScriptStatistics getScriptStatistics();

    /**
     * 获取各类型脚本数量统计
     *
     * @return 脚本类型统计列表
     */
    List<ScriptTypeCount> getScriptTypeStatistics();

    /**
     * 获取各分类脚本数量统计
     *
     * @return 脚本分类统计列表
     */
    List<ScriptCategoryCount> getScriptCategoryStatistics();

    /**
     * 脚本统计信息
     */
    interface ScriptStatistics {
        Long getTotalCount();

        Long getEnabledCount();

        Long getDisabledCount();

        Long getTotalExecutions();
    }

    /**
     * 脚本类型统计
     */
    interface ScriptTypeCount {
        String getScriptType();

        Long getCount();
    }

    /**
     * 脚本分类统计
     */
    interface ScriptCategoryCount {
        String getCategory();

        Long getCount();
    }
}
