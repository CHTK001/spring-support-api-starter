package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.starter.monitor.entity.MonitorSysGenScript;
import com.chua.starter.monitor.entity.MonitorSysGenScriptExecution;
import com.chua.starter.monitor.pojo.ScriptManagementDTO;

import java.util.List;
import java.util.Map;

/**
 * 脚本管理服务接口
 * 
 * @author CH
 * @since 2024/12/19
 */
public interface MonitorSysGenScriptService {

    /**
     * 分页查询脚本列表
     */
    IPage<MonitorSysGenScript> getScriptPage(ScriptManagementDTO.ScriptQueryDTO queryDTO);

    /**
     * 根据ID获取脚本详情
     */
    MonitorSysGenScript getScriptById(Integer scriptId);

    /**
     * 创建脚本
     */
    MonitorSysGenScript createScript(MonitorSysGenScript script);

    /**
     * 更新脚本
     */
    MonitorSysGenScript updateScript(MonitorSysGenScript script);

    /**
     * 删除脚本
     */
    boolean deleteScript(Integer scriptId);

    /**
     * 批量删除脚本
     */
    boolean batchDeleteScripts(List<Integer> scriptIds);

    /**
     * 启用/禁用脚本
     */
    boolean updateScriptStatus(Integer scriptId, Integer status);

    /**
     * 复制脚本
     */
    MonitorSysGenScript copyScript(Integer scriptId, String newScriptName);

    /**
     * 执行脚本
     */
    MonitorSysGenScriptExecution executeScript(Integer scriptId,
            ScriptManagementDTO.ScriptExecuteRequestDTO requestDTO);

    /**
     * 停止脚本执行
     */
    boolean stopScriptExecution(Long executionId);

    /**
     * 获取脚本执行历史
     */
    IPage<MonitorSysGenScriptExecution> getExecutionHistory(Integer scriptId, Integer pageNum, Integer pageSize);

    /**
     * 获取脚本执行详情
     */
    MonitorSysGenScriptExecution getExecutionDetail(Long executionId);

    /**
     * 获取正在运行的脚本
     */
    List<MonitorSysGenScriptExecution> getRunningExecutions();

    /**
     * 获取脚本统计信息
     */
    Map<String, Object> getScriptStatistics();

    /**
     * 获取支持的脚本类型
     */
    List<MonitorSysGenScript.ScriptType> getSupportedScriptTypes();

    /**
     * 验证脚本名称是否可用
     */
    boolean isScriptNameAvailable(String scriptName, Integer excludeId);

    /**
     * 根据分类获取脚本列表
     */
    List<MonitorSysGenScript> getScriptsByCategory(String category);

    /**
     * 根据类型获取脚本列表
     */
    List<MonitorSysGenScript> getScriptsByType(String scriptType);

    /**
     * 根据标签搜索脚本
     */
    List<MonitorSysGenScript> searchScriptsByTag(String tag);

    /**
     * 获取最近执行的脚本
     */
    List<MonitorSysGenScript> getRecentScripts(Integer limit);

    /**
     * 获取热门脚本(按执行次数排序)
     */
    List<MonitorSysGenScript> getPopularScripts(Integer limit);

    /**
     * 导出脚本
     */
    Map<String, Object> exportScript(Integer scriptId);

    /**
     * 导入脚本
     */
    MonitorSysGenScript importScript(Map<String, Object> scriptData);

    /**
     * 验证脚本语法
     */
    Map<String, Object> validateScript(String scriptType, String scriptContent);

    /**
     * 获取脚本模板
     */
    String getScriptTemplate(String scriptType);

    /**
     * 清理过期的执行记录
     */
    int cleanExpiredExecutions(Integer days);
}
