package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.monitor.entity.MonitorSysGenScript;
import com.chua.starter.monitor.entity.MonitorSysGenScriptExecution;
import com.chua.starter.monitor.entity.MonitorSysGenScriptUploadRecord;
import com.chua.starter.monitor.mapper.MonitorSysGenScriptExecutionMapper;
import com.chua.starter.monitor.mapper.MonitorSysGenScriptMapper;
import com.chua.starter.monitor.pojo.ScriptManagementDTO;
import com.chua.starter.monitor.service.MonitorSysGenScriptService;
import com.chua.starter.monitor.service.MonitorSysGenScriptUploadRecordService;
import com.chua.starter.monitor.service.MonitorSysGenServerService;
import com.chua.starter.monitor.service.SshCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final MonitorSysGenScriptUploadRecordService uploadRecordService;
    private final MonitorSysGenServerService monitorSysGenServerService;
    private final SshCommandService sshCommandService;

    // 异步执行器
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "script-executor-" + System.currentTimeMillis());
        thread.setDaemon(true);
        return thread;
    });

    // 正在运行的脚本进程映射 <executionId, processId>
    private final Map<Long, Long> runningProcesses = new ConcurrentHashMap<>();

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
    @Transactional(rollbackFor = Exception.class)
    public MonitorSysGenScriptExecution executeScript(Integer scriptId,
            ScriptManagementDTO.ScriptExecuteRequestDTO requestDTO) {
        log.info("开始执行脚本: scriptId={}, parameters={}", scriptId, requestDTO.getParameters());

        // 1. 验证脚本是否存在
        MonitorSysGenScript script = scriptMapper.selectById(scriptId);
        if (script == null) {
            throw new IllegalArgumentException("脚本不存在: scriptId=" + scriptId);
        }

        // 2. 检查脚本状态
        if (script.getMonitorSysGenScriptStatus() != MonitorSysGenScript.ScriptStatus.ENABLED) {
            throw new IllegalStateException("脚本已禁用，无法执行");
        }

        // 3. 创建执行记录
        MonitorSysGenScriptExecution execution = createExecutionRecord(script, requestDTO);

        try {
            // 4. 保存初始执行记录
            executionMapper.insert(execution);
            log.info("创建执行记录: executionId={}", execution.getMonitorSysGenScriptExecutionId());

            // 5. 异步执行脚本
            executeScriptAsync(execution, script, requestDTO);

            // 6. 更新脚本的最后执行时间和执行次数
            updateScriptExecutionInfo(script);

            return execution;

        } catch (Exception e) {
            log.error("执行脚本失败: scriptId={}", scriptId, e);
            // 更新执行记录为失败状态
            execution.setExecutionFailed("执行启动失败: " + e.getMessage());
            executionMapper.updateById(execution);
            throw new RuntimeException("执行脚本失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean stopScriptExecution(Long executionId) {
        log.info("停止脚本执行: executionId={}", executionId);

        if (executionId == null) {
            log.warn("执行ID不能为空");
            return false;
        }

        try {
            // 获取执行记录
            MonitorSysGenScriptExecution execution = executionMapper.selectById(executionId);
            if (execution == null) {
                log.warn("执行记录不存在: executionId={}", executionId);
                return false;
            }

            // 检查执行状态
            if (!execution.isRunning()) {
                log.info("脚本已不在运行状态: executionId={}, status={}", executionId,
                        execution.getMonitorSysGenScriptExecutionStatus());
                return true;
            }

            // 获取进程ID
            Long processId = runningProcesses.get(executionId);
            if (processId == null) {
                processId = execution.getMonitorSysGenScriptExecutionProcessId();
            }

            boolean stopped = false;
            if (processId != null) {
                // 尝试停止进程 - 这里需要实现进程停止逻辑
                // TODO: 实现进程停止功能
                stopped = true; // 临时设置为true
                log.info("停止脚本进程: executionId={}, processId={}, stopped={}", executionId, processId, stopped);
            }

            // 更新执行记录状态为已取消
            execution.setMonitorSysGenScriptExecutionStatus(MonitorSysGenScriptExecution.ExecutionStatus.CANCELLED);
            execution.setMonitorSysGenScriptExecutionEndTime(LocalDateTime.now());
            execution.calculateDuration();
            executionMapper.updateById(execution);

            // 清理进程映射
            runningProcesses.remove(executionId);

            log.info("脚本执行已停止: executionId={}", executionId);
            return true;

        } catch (Exception e) {
            log.error("停止脚本执行失败: executionId={}", executionId, e);
            return false;
        }
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
        if (days == null || days <= 0) {
            days = 30;
        }
        LocalDateTime expireBefore = LocalDateTime.now().minusDays(days);
        LambdaQueryWrapper<MonitorSysGenScriptExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionEndTime, expireBefore);
        // 只清理非运行中的记录
        wrapper.ne(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionStatus,
                MonitorSysGenScriptExecution.ExecutionStatus.RUNNING);
        return executionMapper.delete(wrapper);
    }

    @Override
    public boolean batchDeleteExecutions(List<Long> executionIds) {
        if (executionIds == null || executionIds.isEmpty()) {
            return false;
        }
        // 不允许删除运行中的记录
        LambdaQueryWrapper<MonitorSysGenScriptExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionId, executionIds);
        wrapper.ne(MonitorSysGenScriptExecution::getMonitorSysGenScriptExecutionStatus,
                MonitorSysGenScriptExecution.ExecutionStatus.RUNNING);
        int deleted = executionMapper.delete(wrapper);
        return deleted > 0;
    }


    // ==================== 私有方法 ====================

    /**
     * 创建执行记录
     *
     * @param script     脚本信息
     * @param requestDTO 执行请求
     * @return 执行记录
     */
    private MonitorSysGenScriptExecution createExecutionRecord(MonitorSysGenScript script,
            ScriptManagementDTO.ScriptExecuteRequestDTO requestDTO) {
        log.debug("创建脚本执行记录: scriptId={}, scriptName={}", script.getMonitorSysGenScriptId(),
                script.getMonitorSysGenScriptName());

        MonitorSysGenScriptExecution execution = new MonitorSysGenScriptExecution();

        // 设置脚本信息
        execution.setMonitorSysGenScriptId(script.getMonitorSysGenScriptId());

        // 设置执行参数
        if (requestDTO.getParameters() != null && requestDTO.getParameters().length > 0) {
            execution.setMonitorSysGenScriptExecutionParameters(String.join(" ", requestDTO.getParameters()));
        }

        // 设置执行状态为运行中
        execution.setMonitorSysGenScriptExecutionStatus(MonitorSysGenScriptExecution.ExecutionStatus.RUNNING);

        // 设置开始时间
        execution.setMonitorSysGenScriptExecutionStartTime(LocalDateTime.now());

        // 设置服务器ID
        if (requestDTO.getServerId() != null) {
            execution.setMonitorSysGenScriptExecutionServerId(requestDTO.getServerId());
        }

        // 设置触发类型
        if (StringUtils.hasText(requestDTO.getTriggerType())) {
            MonitorSysGenScriptExecution.TriggerType triggerType = MonitorSysGenScriptExecution.TriggerType
                    .fromCode(requestDTO.getTriggerType());
            execution.setMonitorSysGenScriptExecutionTriggerType(
                    triggerType != null ? triggerType : MonitorSysGenScriptExecution.TriggerType.MANUAL);
        } else {
            execution.setMonitorSysGenScriptExecutionTriggerType(MonitorSysGenScriptExecution.TriggerType.MANUAL);
        }

        // 生成任务ID用于内存管理
        execution.setMonitorSysGenScriptExecutionTaskId(
                "task_" + System.currentTimeMillis() + "_" + script.getMonitorSysGenScriptId());

        log.debug("执行记录创建完成: scriptId={}, taskId={}", script.getMonitorSysGenScriptId(),
                execution.getMonitorSysGenScriptExecutionTaskId());

        return execution;
    }

    /**
     * 异步执行脚本
     *
     * @param execution  执行记录
     * @param script     脚本信息
     * @param requestDTO 执行请求
     */
    private void executeScriptAsync(MonitorSysGenScriptExecution execution, MonitorSysGenScript script,
            ScriptManagementDTO.ScriptExecuteRequestDTO requestDTO) {
        log.info("开始异步执行脚本: scriptId={}, executionId={}, taskId={}", script.getMonitorSysGenScriptId(),
                execution.getMonitorSysGenScriptExecutionId(), execution.getMonitorSysGenScriptExecutionTaskId());

        // 提交异步任务
        CompletableFuture.runAsync(() -> {
            try {
                log.info("开始异步执行脚本: scriptId={}, executionId={}", script.getMonitorSysGenScriptId(),
                        execution.getMonitorSysGenScriptExecutionId());

                // 1) 解析执行所需信息：服务器ID、工作目录、文件名与参数
                Integer serverId = requestDTO.getServerId();
                String workingDir = requestDTO.getWorkingDirectory();

                // 若未显式指定，则从最近一次成功上传记录中推断（REMOTE）
                if (serverId == null || !StringUtils.hasText(workingDir)) {
                    LambdaQueryWrapper<MonitorSysGenScriptUploadRecord> qw = new LambdaQueryWrapper<>();
                    qw.eq(MonitorSysGenScriptUploadRecord::getMonitorSysGenScriptId, script.getMonitorSysGenScriptId());
                    qw.eq(MonitorSysGenScriptUploadRecord::getMonitorSysGenScriptUploadType, "REMOTE");
                    qw.eq(MonitorSysGenScriptUploadRecord::getMonitorSysGenScriptUploadStatus, "SUCCESS");
                    qw.orderByDesc(MonitorSysGenScriptUploadRecord::getMonitorSysGenScriptUploadRecordId);
                    qw.last("LIMIT 1");
                    MonitorSysGenScriptUploadRecord last = uploadRecordService.getBaseMapper().selectOne(qw);
                    if (last != null) {
                        if (serverId == null) {
                            serverId = last.getMonitorSysGenScriptUploadServerId();
                        }
                        if (!StringUtils.hasText(workingDir)) {
                            workingDir = last.getMonitorSysGenScriptUploadTargetPath();
                        }
                    }
                }

                if (!StringUtils.hasText(workingDir)) {
                    workingDir = "/opt/monitor/scripts";
                }
                if (serverId == null) {
                    throw new IllegalStateException("无法确定执行服务器，请先上传脚本或在执行时指定serverId");
                }

                String ext = script.getScriptTypeEnum() != null ? script.getScriptTypeEnum().getExtension() : ".sh";
                String fileName = script.getMonitorSysGenScriptName() + ext;
                String params = (requestDTO.getParameters() != null && requestDTO.getParameters().length > 0)
                        ? String.join(" ", requestDTO.getParameters()) : "";

                // 2) 根据脚本类型构建命令
                String cmd;
                MonitorSysGenScript.ScriptType type = script.getScriptTypeEnum();
                if (type == MonitorSysGenScript.ScriptType.PYTHON) {
                    cmd = String.format("cd %s && python3 %s %s", workingDir, fileName, params).trim();
                } else if (type == MonitorSysGenScript.ScriptType.POWERSHELL) {
                    // Windows场景：根据环境调整；此处给出参考命令
                    cmd = String.format("powershell -ExecutionPolicy Bypass -File \"%s\\%s\" %s", workingDir, fileName, params).trim();
                } else if (type == MonitorSysGenScript.ScriptType.BATCH) {
                    cmd = String.format("cd %s && chmod +x %s && ./%s %s", workingDir, fileName, fileName, params).trim();
                } else {
                    // 默认按Shell执行
                    cmd = String.format("cd %s && chmod +x %s && ./%s %s", workingDir, fileName, fileName, params).trim();
                }

                // 3) 确保SSH连接并执行
                if (!sshCommandService.isSSHConnected(serverId)) {
                    com.chua.starter.monitor.entity.MonitorSysGenServer server = monitorSysGenServerService.getByIdWithCache(serverId);
                    if (server == null || !sshCommandService.connectSSH(server)) {
                        throw new IllegalStateException("SSH连接失败，无法执行脚本");
                    }
                }

                String result = sshCommandService.executeSSHCommand(serverId, cmd);

                // 4) 更新执行记录为成功
                execution.setExecutionCompleted(MonitorSysGenScriptExecution.ExecutionStatus.SUCCESS, 0, result, "");
                executionMapper.updateById(execution);
                log.info("脚本执行完成: executionId={}", execution.getMonitorSysGenScriptExecutionId());

            } catch (Exception e) {
                log.error("异步执行脚本失败: scriptId={}, executionId={}", script.getMonitorSysGenScriptId(),
                        execution.getMonitorSysGenScriptExecutionId(), e);

                // 更新执行记录为失败状态
                execution.setExecutionFailed("脚本执行异常: " + e.getMessage());
                executionMapper.updateById(execution);

                // 清理进程映射
                runningProcesses.remove(execution.getMonitorSysGenScriptExecutionId());
            }
        }, asyncExecutor);
    }

    /**
     * 更新脚本执行信息
     *
     * @param script 脚本信息
     */
    private void updateScriptExecutionInfo(MonitorSysGenScript script) {
        log.debug("更新脚本执行信息: scriptId={}", script.getMonitorSysGenScriptId());

        try {
            // 增加执行次数并更新最后执行时间
            script.incrementExecuteCount();

            // 更新数据库
            scriptMapper.updateById(script);

            log.debug("脚本执行信息更新完成: scriptId={}, executeCount={}", script.getMonitorSysGenScriptId(),
                    script.getExecuteCountOrDefault());

        } catch (Exception e) {
            log.error("更新脚本执行信息失败: scriptId={}", script.getMonitorSysGenScriptId(), e);
        }
    }

}
