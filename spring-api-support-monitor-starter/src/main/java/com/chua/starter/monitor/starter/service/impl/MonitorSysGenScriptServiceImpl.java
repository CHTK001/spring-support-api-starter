package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.monitor.starter.entity.MonitorSysGenScript;
import com.chua.starter.monitor.starter.entity.MonitorSysGenScriptExecution;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenScriptMapper;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenScriptExecutionMapper;
import com.chua.starter.monitor.starter.pojo.ScriptManagementDTO;
import com.chua.starter.monitor.starter.service.MonitorSysGenScriptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 脚本管理服务实现
 * 
 * @author CH
 * @since 2024/12/19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenScriptServiceImpl implements MonitorSysGenScriptService {

    private final MonitorSysGenScriptMapper scriptMapper;
    private final MonitorSysGenScriptExecutionMapper executionMapper;

    @Override
    public IPage<MonitorSysGenScript> getScriptPage(ScriptManagementDTO.ScriptQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<MonitorSysGenScript> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getScriptName())) {
            queryWrapper.like(MonitorSysGenScript::getMonitorSysGenScriptName, queryDTO.getScriptName());
        }
        if (StringUtils.hasText(queryDTO.getScriptType())) {
            queryWrapper.eq(MonitorSysGenScript::getMonitorSysGenScriptType, queryDTO.getScriptType());
        }
        if (StringUtils.hasText(queryDTO.getScriptCategory())) {
            queryWrapper.eq(MonitorSysGenScript::getMonitorSysGenScriptCategory, queryDTO.getScriptCategory());
        }
        if (queryDTO.getScriptStatus() != null) {
            queryWrapper.eq(MonitorSysGenScript::getMonitorSysGenScriptStatus, queryDTO.getScriptStatus());
        }
        // 注释掉暂时不支持的字段查询
        // if (StringUtils.hasText(queryDTO.getScriptTags())) {
        // queryWrapper.like(MonitorSysGenScript::getMonitorSysGenScriptTags,
        // queryDTO.getScriptTags());
        // }
        // if (StringUtils.hasText(queryDTO.getScriptAuthor())) {
        // queryWrapper.like(MonitorSysGenScript::getMonitorSysGenScriptAuthor,
        // queryDTO.getScriptAuthor());
        // }

        // 排序
        queryWrapper.orderByDesc(MonitorSysGenScript::getMonitorSysGenScriptLastExecuteTime);
        queryWrapper.orderByDesc(MonitorSysGenScript::getCreateTime);

        // 分页查询
        Page<MonitorSysGenScript> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        return scriptMapper.selectPage(page, queryWrapper);
    }

    @Override
    public MonitorSysGenScript getScriptById(Integer scriptId) {
        if (scriptId == null) {
            log.warn("脚本ID不能为空");
            return null;
        }

        MonitorSysGenScript script = scriptMapper.selectById(scriptId);
        if (script == null) {
            log.warn("脚本不存在: scriptId={}", scriptId);
            return null;
        }

        log.info("获取脚本详情成功: scriptId={}, scriptName={}", scriptId, script.getMonitorSysGenScriptName());
        return script;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonitorSysGenScript createScript(MonitorSysGenScript script) {
        if (script == null) {
            throw new IllegalArgumentException("脚本信息不能为空");
        }

        // 验证脚本名称唯一性
        if (!isScriptNameAvailable(script.getMonitorSysGenScriptName(), null)) {
            throw new IllegalArgumentException("脚本名称已存在: " + script.getMonitorSysGenScriptName());
        }

        // 设置默认值
        if (script.getMonitorSysGenScriptStatus() == null) {
            script.setMonitorSysGenScriptStatus(MonitorSysGenScript.ScriptStatus.ENABLED);
        }
        if (script.getMonitorSysGenScriptTimeout() == null) {
            script.setMonitorSysGenScriptTimeout(300); // 默认5分钟超时
        }
        if (script.getMonitorSysGenScriptExecuteCount() == null) {
            script.setMonitorSysGenScriptExecuteCount(0);
        }
        if (!StringUtils.hasText(script.getMonitorSysGenScriptVersion())) {
            script.setMonitorSysGenScriptVersion("1.0.0");
        }

        // 保存脚本
        int result = scriptMapper.insert(script);
        if (result > 0) {
            log.info("创建脚本成功: scriptId={}, scriptName={}", script.getMonitorSysGenScriptId(),
                    script.getMonitorSysGenScriptName());
            return script;
        } else {
            throw new RuntimeException("创建脚本失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonitorSysGenScript updateScript(MonitorSysGenScript script) {
        if (script == null || script.getMonitorSysGenScriptId() == null) {
            throw new IllegalArgumentException("脚本ID不能为空");
        }

        // 检查脚本是否存在
        MonitorSysGenScript existingScript = scriptMapper.selectById(script.getMonitorSysGenScriptId());
        if (existingScript == null) {
            throw new IllegalArgumentException("脚本不存在: " + script.getMonitorSysGenScriptId());
        }

        // 验证脚本名称唯一性（排除当前脚本）
        if (StringUtils.hasText(script.getMonitorSysGenScriptName())
                && !isScriptNameAvailable(script.getMonitorSysGenScriptName(), script.getMonitorSysGenScriptId())) {
            throw new IllegalArgumentException("脚本名称已存在: " + script.getMonitorSysGenScriptName());
        }

        // 更新脚本
        int result = scriptMapper.updateById(script);
        if (result > 0) {
            log.info("更新脚本成功: scriptId={}, scriptName={}", script.getMonitorSysGenScriptId(),
                    script.getMonitorSysGenScriptName());
            return scriptMapper.selectById(script.getMonitorSysGenScriptId());
        } else {
            throw new RuntimeException("更新脚本失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteScript(Integer scriptId) {
        if (scriptId == null) {
            throw new IllegalArgumentException("脚本ID不能为空");
        }

        // 检查脚本是否存在
        MonitorSysGenScript script = scriptMapper.selectById(scriptId);
        if (script == null) {
            log.warn("脚本不存在: scriptId={}", scriptId);
            return false;
        }

        // 检查是否有正在运行的执行记录
        LambdaQueryWrapper<MonitorSysGenScriptExecution> executionQuery = new LambdaQueryWrapper<>();
        executionQuery.eq(MonitorSysGenScriptExecution::getMonitorSysGenScriptId, scriptId);
        executionQuery.eq(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionStatus,
                MonitorSysGenScriptExecution.ExecutionStatus.RUNNING);

        long runningCount = executionMapper.selectCount(executionQuery);
        if (runningCount > 0) {
            throw new IllegalStateException("脚本正在执行中，无法删除");
        }

        // 删除脚本
        int result = scriptMapper.deleteById(scriptId);
        if (result > 0) {
            log.info("删除脚本成功: scriptId={}, scriptName={}", scriptId, script.getMonitorSysGenScriptName());
            return true;
        } else {
            throw new RuntimeException("删除脚本失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteScripts(List<Integer> scriptIds) {
        if (scriptIds == null || scriptIds.isEmpty()) {
            throw new IllegalArgumentException("脚本ID列表不能为空");
        }

        // 检查是否有正在运行的执行记录
        LambdaQueryWrapper<MonitorSysGenScriptExecution> executionQuery = new LambdaQueryWrapper<>();
        executionQuery.in(MonitorSysGenScriptExecution::getMonitorSysGenScriptId, scriptIds);
        executionQuery.eq(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionStatus,
                MonitorSysGenScriptExecution.ExecutionStatus.RUNNING);

        long runningCount = executionMapper.selectCount(executionQuery);
        if (runningCount > 0) {
            throw new IllegalStateException("部分脚本正在执行中，无法删除");
        }

        // 批量删除脚本
        int result = scriptMapper.delete(new LambdaQueryWrapper<MonitorSysGenScript>()
                .in(MonitorSysGenScript::getMonitorSysGenScriptId, scriptIds));
        log.info("批量删除脚本成功: count={}, scriptIds={}", result, scriptIds);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateScriptStatus(Integer scriptId, Integer status) {
        if (scriptId == null || status == null) {
            throw new IllegalArgumentException("脚本ID和状态不能为空");
        }

        // 检查脚本是否存在
        MonitorSysGenScript script = scriptMapper.selectById(scriptId);
        if (script == null) {
            throw new IllegalArgumentException("脚本不存在: " + scriptId);
        }

        // 更新状态
        MonitorSysGenScript updateScript = new MonitorSysGenScript();
        updateScript.setMonitorSysGenScriptId(scriptId);
        updateScript.setMonitorSysGenScriptStatus(
                status == 1 ? MonitorSysGenScript.ScriptStatus.ENABLED : MonitorSysGenScript.ScriptStatus.DISABLED);

        int result = scriptMapper.updateById(updateScript);
        if (result > 0) {
            log.info("更新脚本状态成功: scriptId={}, status={}", scriptId, status);
            return true;
        } else {
            throw new RuntimeException("更新脚本状态失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonitorSysGenScript copyScript(Integer scriptId, String newScriptName) {
        if (scriptId == null || !StringUtils.hasText(newScriptName)) {
            throw new IllegalArgumentException("脚本ID和新脚本名称不能为空");
        }

        // 获取原脚本
        MonitorSysGenScript originalScript = scriptMapper.selectById(scriptId);
        if (originalScript == null) {
            throw new IllegalArgumentException("原脚本不存在: " + scriptId);
        }

        // 验证新脚本名称唯一性
        if (!isScriptNameAvailable(newScriptName, null)) {
            throw new IllegalArgumentException("脚本名称已存在: " + newScriptName);
        }

        // 创建新脚本
        MonitorSysGenScript newScript = new MonitorSysGenScript();
        BeanUtils.copyProperties(originalScript, newScript);
        newScript.setMonitorSysGenScriptId(null); // 清空ID，让数据库自动生成
        newScript.setMonitorSysGenScriptName(newScriptName);
        newScript.setMonitorSysGenScriptExecuteCount(0); // 重置执行次数
        newScript.setMonitorSysGenScriptLastExecuteTime(null); // 重置最后执行时间
        newScript.setCreateTime(null); // 重置创建时间
        newScript.setUpdateTime(null); // 重置更新时间

        // 保存新脚本
        int result = scriptMapper.insert(newScript);
        if (result > 0) {
            log.info("复制脚本成功: originalId={}, newId={}, newName={}", scriptId, newScript.getMonitorSysGenScriptId(),
                    newScriptName);
            return newScript;
        } else {
            throw new RuntimeException("复制脚本失败");
        }
    }

    @Override
    public boolean isScriptNameAvailable(String scriptName, Integer excludeId) {
        if (!StringUtils.hasText(scriptName)) {
            return false;
        }

        LambdaQueryWrapper<MonitorSysGenScript> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MonitorSysGenScript::getMonitorSysGenScriptName, scriptName);
        if (excludeId != null) {
            queryWrapper.ne(MonitorSysGenScript::getMonitorSysGenScriptId, excludeId);
        }

        long count = scriptMapper.selectCount(queryWrapper);
        return count == 0;
    }

    @Override
    public List<MonitorSysGenScript.ScriptType> getSupportedScriptTypes() {
        return Arrays.asList(MonitorSysGenScript.ScriptType.values());
    }

    @Override
    public MonitorSysGenScriptExecution executeScript(Integer scriptId,
            ScriptManagementDTO.ScriptExecuteRequestDTO requestDTO) {
        // TODO: 实现脚本执行逻辑
        log.info("执行脚本: scriptId={}, parameters={}", scriptId, requestDTO.getParameters());
        throw new UnsupportedOperationException("脚本执行功能待实现");
    }

    @Override
    public boolean stopScriptExecution(Long executionId) {
        // TODO: 实现停止脚本执行逻辑
        log.info("停止脚本执行: executionId={}", executionId);
        throw new UnsupportedOperationException("停止脚本执行功能待实现");
    }

    @Override
    public IPage<MonitorSysGenScriptExecution> getExecutionHistory(Integer scriptId, Integer pageNum,
            Integer pageSize) {
        LambdaQueryWrapper<MonitorSysGenScriptExecution> queryWrapper = new LambdaQueryWrapper<>();
        if (scriptId != null) {
            queryWrapper.eq(MonitorSysGenScriptExecution::getMonitorSysGenScriptId, scriptId);
        }
        queryWrapper.orderByDesc(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionStartTime);

        Page<MonitorSysGenScriptExecution> page = new Page<>(pageNum, pageSize);
        return executionMapper.selectPage(page, queryWrapper);
    }

    @Override
    public MonitorSysGenScriptExecution getExecutionDetail(Long executionId) {
        if (executionId == null) {
            return null;
        }
        return executionMapper.selectById(executionId);
    }

    @Override
    public List<MonitorSysGenScriptExecution> getRunningExecutions() {
        LambdaQueryWrapper<MonitorSysGenScriptExecution> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionStatus,
                MonitorSysGenScriptExecution.ExecutionStatus.RUNNING);
        queryWrapper.orderByDesc(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionStartTime);

        return executionMapper.selectList(queryWrapper);
    }

    @Override
    public Map<String, Object> getScriptStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 总脚本数
        long totalScripts = scriptMapper.selectCount(null);
        statistics.put("totalScripts", totalScripts);

        // 启用的脚本数
        LambdaQueryWrapper<MonitorSysGenScript> enabledQuery = new LambdaQueryWrapper<>();
        enabledQuery.eq(MonitorSysGenScript::getMonitorSysGenScriptStatus, MonitorSysGenScript.ScriptStatus.ENABLED);
        long enabledScripts = scriptMapper.selectCount(enabledQuery);
        statistics.put("enabledScripts", enabledScripts);

        // 禁用的脚本数
        statistics.put("disabledScripts", totalScripts - enabledScripts);

        // 总执行次数
        long totalExecutions = executionMapper.selectCount(null);
        statistics.put("totalExecutions", totalExecutions);

        // 正在运行的执行数
        LambdaQueryWrapper<MonitorSysGenScriptExecution> runningQuery = new LambdaQueryWrapper<>();
        runningQuery.eq(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionStatus,
                MonitorSysGenScriptExecution.ExecutionStatus.RUNNING);
        long runningExecutions = executionMapper.selectCount(runningQuery);
        statistics.put("runningExecutions", runningExecutions);

        return statistics;
    }

    @Override
    public List<MonitorSysGenScript> getScriptsByCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<MonitorSysGenScript> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MonitorSysGenScript::getMonitorSysGenScriptCategory, category);
        queryWrapper.eq(MonitorSysGenScript::getMonitorSysGenScriptStatus, MonitorSysGenScript.ScriptStatus.ENABLED);
        queryWrapper.orderByDesc(MonitorSysGenScript::getMonitorSysGenScriptLastExecuteTime);

        return scriptMapper.selectList(queryWrapper);
    }

    @Override
    public List<MonitorSysGenScript> getScriptsByType(String scriptType) {
        if (!StringUtils.hasText(scriptType)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<MonitorSysGenScript> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MonitorSysGenScript::getMonitorSysGenScriptType, scriptType);
        queryWrapper.eq(MonitorSysGenScript::getMonitorSysGenScriptStatus, MonitorSysGenScript.ScriptStatus.ENABLED);
        queryWrapper.orderByDesc(MonitorSysGenScript::getMonitorSysGenScriptLastExecuteTime);

        return scriptMapper.selectList(queryWrapper);
    }

    @Override
    public List<MonitorSysGenScript> searchScriptsByTag(String tag) {
        if (!StringUtils.hasText(tag)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<MonitorSysGenScript> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(MonitorSysGenScript::getMonitorSysGenScriptTags, tag);
        queryWrapper.eq(MonitorSysGenScript::getMonitorSysGenScriptStatus, MonitorSysGenScript.ScriptStatus.ENABLED);
        queryWrapper.orderByDesc(MonitorSysGenScript::getMonitorSysGenScriptLastExecuteTime);

        return scriptMapper.selectList(queryWrapper);
    }

    @Override
    public List<MonitorSysGenScript> getRecentScripts(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        LambdaQueryWrapper<MonitorSysGenScript> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MonitorSysGenScript::getMonitorSysGenScriptStatus, MonitorSysGenScript.ScriptStatus.ENABLED);
        queryWrapper.isNotNull(MonitorSysGenScript::getMonitorSysGenScriptLastExecuteTime);
        queryWrapper.orderByDesc(MonitorSysGenScript::getMonitorSysGenScriptLastExecuteTime);
        queryWrapper.last("LIMIT " + limit);

        return scriptMapper.selectList(queryWrapper);
    }

    @Override
    public List<MonitorSysGenScript> getPopularScripts(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        LambdaQueryWrapper<MonitorSysGenScript> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MonitorSysGenScript::getMonitorSysGenScriptStatus, MonitorSysGenScript.ScriptStatus.ENABLED);
        queryWrapper.gt(MonitorSysGenScript::getMonitorSysGenScriptExecuteCount, 0);
        queryWrapper.orderByDesc(MonitorSysGenScript::getMonitorSysGenScriptExecuteCount);
        queryWrapper.last("LIMIT " + limit);

        return scriptMapper.selectList(queryWrapper);
    }

    @Override
    public Map<String, Object> exportScript(Integer scriptId) {
        // TODO: 实现脚本导出逻辑
        log.info("导出脚本: scriptId={}", scriptId);
        throw new UnsupportedOperationException("脚本导出功能待实现");
    }

    @Override
    public MonitorSysGenScript importScript(Map<String, Object> scriptData) {
        // TODO: 实现脚本导入逻辑
        log.info("导入脚本: scriptData={}", scriptData);
        throw new UnsupportedOperationException("脚本导入功能待实现");
    }

    @Override
    public Map<String, Object> validateScript(String scriptType, String scriptContent) {
        // TODO: 实现脚本语法验证逻辑
        log.info("验证脚本语法: scriptType={}", scriptType);
        throw new UnsupportedOperationException("脚本语法验证功能待实现");
    }

    @Override
    public String getScriptTemplate(String scriptType) {
        // TODO: 实现获取脚本模板逻辑
        log.info("获取脚本模板: scriptType={}", scriptType);
        throw new UnsupportedOperationException("获取脚本模板功能待实现");
    }

    @Override
    public int cleanExpiredExecutions(Integer days) {
        // TODO: 实现清理过期执行记录逻辑
        log.info("清理过期执行记录: days={}", days);
        throw new UnsupportedOperationException("清理过期执行记录功能待实现");
    }
}
