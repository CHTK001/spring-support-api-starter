package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerComponent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务器组件配置Mapper接口
 *
 * @author CH
 * @since 2025/01/03
 */
@Mapper
public interface MonitorSysGenServerComponentMapper extends BaseMapper<MonitorSysGenServerComponent> {

    /**
     * 根据服务器ID获取组件列表
     *
     * @param serverId 服务器ID
     * @return 组件列表
     */
    List<MonitorSysGenServerComponent> selectByServerId(@Param("serverId") Integer serverId);

    /**
     * 根据服务器ID和组件类型获取组件列表
     *
     * @param serverId 服务器ID
     * @param componentType 组件类型
     * @return 组件列表
     */
    List<MonitorSysGenServerComponent> selectByServerIdAndType(@Param("serverId") Integer serverId, 
                                                               @Param("componentType") String componentType);

    /**
     * 根据服务器ID和表达式类型获取组件列表
     *
     * @param serverId 服务器ID
     * @param expressionType 表达式类型
     * @return 组件列表
     */
    List<MonitorSysGenServerComponent> selectByServerIdAndExpressionType(@Param("serverId") Integer serverId, 
                                                                          @Param("expressionType") String expressionType);

    /**
     * 获取启用的组件列表
     *
     * @param serverId 服务器ID
     * @return 启用的组件列表
     */
    List<MonitorSysGenServerComponent> selectEnabledByServerId(@Param("serverId") Integer serverId);

    /**
     * 批量更新组件位置信息
     *
     * @param components 组件列表
     * @return 更新数量
     */
    int batchUpdatePosition(@Param("components") List<MonitorSysGenServerComponent> components);

    /**
     * 获取组件模板列表
     *
     * @return 模板列表
     */
    List<MonitorSysGenServerComponent> selectTemplates();

    /**
     * 复制模板组件到指定服务器
     *
     * @param serverId 目标服务器ID
     * @param templateIds 模板ID列表
     * @return 复制数量
     */
    int copyTemplatesForServer(@Param("serverId") Integer serverId, @Param("templateIds") List<Long> templateIds);

    /**
     * 获取服务器组件统计信息
     *
     * @param serverId 服务器ID
     * @return 统计信息
     */
    List<java.util.Map<String, Object>> getComponentStatistics(@Param("serverId") Integer serverId);
}
