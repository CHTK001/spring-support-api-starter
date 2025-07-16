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
import com.chua.starter.monitor.starter.service.ScriptExecutionTaskManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final ScriptExecutionTaskManager taskManager;

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
        if (StringUtils.hasText(queryDTO.getScriptTags())) {
            queryWrapper.like(MonitorSysGenScript::getMonitorSysGenScriptTags, queryDTO.getScriptTags());
        }
        if (StringUtils.hasText(queryDTO.getScriptAuthor())) {
            queryWrapper.like(MonitorSysGenScript::getMonitorSysGenScriptAuthor, queryDTO.getScriptAuthor());
        }

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
        int result = scriptMapper.deleteBatchIds(scriptIds);
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

            // 5. 创建内存任务
            String executeUser = getCurrentUser();
            String description = String.format("执行脚本: %s", script.getMonitorSysGenScriptName());
            ScriptExecutionTaskManager.ExecutionTask task = taskManager.createTask(execution, executeUser, description);

            // 6. 更新执行记录的任务ID
            execution.setMonitorSysGenScriptExecutionTaskId(task.getTaskId());
            executionMapper.updateById(execution);

            // 7. 异步执行脚本
            executeScriptAsync(execution, script, requestDTO, task);

            // 8. 更新脚本的最后执行时间和执行次数
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

    /**
     * 创建执行记录
     */
    private MonitorSysGenScriptExecution createExecutionRecord(MonitorSysGenScript script,
            ScriptManagementDTO.ScriptExecuteRequestDTO requestDTO) {
        MonitorSysGenScriptExecution execution = new MonitorSysGenScriptExecution();

        // 基本信息
        execution.setMonitorSysGenScriptId(script.getMonitorSysGenScriptId());
        execution.setMonitorSysGenScriptExecutionParameters(requestDTO.getParameters());
        execution.setMonitorSysGenScriptExecutionStatus(MonitorSysGenScriptExecution.ExecutionStatus.RUNNING);
        execution.setMonitorSysGenScriptExecutionStartTime(LocalDateTime.now());

        // 执行配置
        execution.setMonitorSysGenScriptExecutionServerId(requestDTO.getServerId());
        execution.setMonitorSysGenScriptExecutionTriggerType(
                requestDTO.getTriggerType() != null ? requestDTO.getTriggerType() : "MANUAL");
        execution.setMonitorSysGenScriptExecutionTriggerUser(getCurrentUser());
        execution.setMonitorSysGenScriptExecutionTimeoutSeconds(
                requestDTO.getTimeoutSeconds() != null ? requestDTO.getTimeoutSeconds()
                        : script.getMonitorSysGenScriptTimeoutSeconds());

        // 执行模式和优先级
        execution.setMonitorSysGenScriptExecutionMode(script.getMonitorSysGenScriptExecutionMode());
        execution.setMonitorSysGenScriptExecutionPriority(
                requestDTO.getPriority() != null ? requestDTO.getPriority() : 5);

        // 重试配置
        execution.setMonitorSysGenScriptExecutionRetryCount(0);
        execution.setMonitorSysGenScriptExecutionMaxRetry(script.getMonitorSysGenScriptRetryCount());

        // 工作目录和环境变量
        execution.setMonitorSysGenScriptExecutionWorkingDirectory(script.getMonitorSysGenScriptWorkingDirectory());
        execution.setMonitorSysGenScriptExecutionEnvironmentVars(script.getMonitorSysGenScriptEnvironmentVars());

        // 标签和元数据
        execution.setMonitorSysGenScriptExecutionTags(requestDTO.getTags());
        execution.setMonitorSysGenScriptExecutionMetadata(buildExecutionMetadata(script, requestDTO));

        // 设置创建时间和更新时间
        execution.setMonitorSysGenScriptExecutionCreateTime(LocalDateTime.now());
        execution.setMonitorSysGenScriptExecutionUpdateTime(LocalDateTime.now());

        return execution;
    }

    /**
     * 异步执行脚本
     */
    private void executeScriptAsync(MonitorSysGenScriptExecution execution, MonitorSysGenScript script,
            ScriptManagementDTO.ScriptExecuteRequestDTO requestDTO, ScriptExecutionTaskManager.ExecutionTask task) {
        // 使用线程池异步执行脚本
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                executeScriptProcess(execution, script, requestDTO, task);
            } catch (Exception e) {
                log.error("异步执行脚本失败: executionId={}, taskId={}", execution.getMonitorSysGenScriptExecutionId(),
                        task.getTaskId(), e);

                // 更新任务状态为失败
                taskManager.completeTask(task.getTaskId(), ScriptExecutionTaskManager.TaskStatus.FAILED);

                // 更新执行记录为失败状态
                execution.setExecutionFailed("执行失败: " + e.getMessage());
                executionMapper.updateById(execution);

                // 销毁任务
                taskManager.destroyTask(task.getTaskId());
            }
        });

        // 启动任务（此时还没有进程，进程会在executeScriptProcess中创建）
        taskManager.startTask(task.getTaskId(), null, future);
    }

    /**
     * 执行脚本进程
     */
    private void executeScriptProcess(MonitorSysGenScriptExecution execution, MonitorSysGenScript script,
            ScriptManagementDTO.ScriptExecuteRequestDTO requestDTO, ScriptExecutionTaskManager.ExecutionTask task) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        Process process = null;

        try {
            // 1. 构建命令
            List<String> command = buildScriptCommand(script, requestDTO);
            processBuilder.command(command);

            // 2. 设置工作目录
            if (execution.getMonitorSysGenScriptExecutionWorkingDirectory() != null) {
                processBuilder.directory(new File(execution.getMonitorSysGenScriptExecutionWorkingDirectory()));
            }

            // 3. 设置环境变量
            if (execution.getMonitorSysGenScriptExecutionEnvironmentVars() != null) {
                Map<String, String> env = parseEnvironmentVars(
                        execution.getMonitorSysGenScriptExecutionEnvironmentVars());
                processBuilder.environment().putAll(env);
            }

            // 4. 启动进程
            process = processBuilder.start();
            execution.setMonitorSysGenScriptExecutionProcessId((long) process.pid());
            executionMapper.updateById(execution);

            // 5. 更新任务进程信息
            taskManager.updateTaskMetadata(task.getTaskId(), "processId", process.pid());
            task.setProcess(process);

            // 5. 读取输出
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            // 异步读取标准输出和错误输出
            CompletableFuture<Void> stdoutFuture = CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stdout.append(line).append("\n");
                    }
                } catch (IOException e) {
                    log.error("读取标准输出失败", e);
                }
            });

            CompletableFuture<Void> stderrFuture = CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stderr.append(line).append("\n");
                    }
                } catch (IOException e) {
                    log.error("读取错误输出失败", e);
                }
            });

            // 6. 等待进程完成（带超时）
            boolean finished = process.waitFor(execution.getMonitorSysGenScriptExecutionTimeoutSeconds(),
                    TimeUnit.SECONDS);

            if (!finished) {
                // 超时，强制终止进程
                process.destroyForcibly();
                execution.setExecutionFailed("执行超时");
                taskManager.completeTask(task.getTaskId(), ScriptExecutionTaskManager.TaskStatus.TIMEOUT);
            } else {
                // 等待输出读取完成
                CompletableFuture.allOf(stdoutFuture, stderrFuture).get(5, TimeUnit.SECONDS);

                // 7. 更新执行结果
                int exitCode = process.exitValue();
                MonitorSysGenScriptExecution.ExecutionStatus status = exitCode == 0
                        ? MonitorSysGenScriptExecution.ExecutionStatus.SUCCESS
                        : MonitorSysGenScriptExecution.ExecutionStatus.FAILED;

                execution.setExecutionCompleted(status, exitCode, stdout.toString(), stderr.toString());

                // 8. 更新任务状态
                ScriptExecutionTaskManager.TaskStatus taskStatus = exitCode == 0
                        ? ScriptExecutionTaskManager.TaskStatus.COMPLETED
                        : ScriptExecutionTaskManager.TaskStatus.FAILED;
                taskManager.completeTask(task.getTaskId(), taskStatus);
            }

        } catch (Exception e) {
            log.error("执行脚本进程失败: executionId={}, taskId={}", execution.getMonitorSysGenScriptExecutionId(),
                    task.getTaskId(), e);
            execution.setExecutionFailed("执行异常: " + e.getMessage());
            taskManager.completeTask(task.getTaskId(), ScriptExecutionTaskManager.TaskStatus.FAILED);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            // 更新执行记录
            executionMapper.updateById(execution);

            // 销毁任务（从内存中移除）
            taskManager.destroyTask(task.getTaskId());
        }
    }

    @Override
    public boolean stopScriptExecution(Long executionId) {
        log.info("停止脚本执行: executionId={}", executionId);

        // 1. 查找执行记录
        MonitorSysGenScriptExecution execution = executionMapper.selectById(executionId);
        if (execution == null) {
            log.warn("执行记录不存在: executionId={}", executionId);
            return false;
        }

        // 2. 检查执行状态
        if (execution.getMonitorSysGenScriptExecutionStatus() != MonitorSysGenScriptExecution.ExecutionStatus.RUNNING) {
            log.warn("执行记录不在运行状态: executionId={}, status={}", executionId,
                    execution.getMonitorSysGenScriptExecutionStatus());
            return false;
        }

        try {
            // 3. 取消内存任务
            String taskId = execution.getMonitorSysGenScriptExecutionTaskId();
            if (taskId != null) {
                boolean taskCancelled = taskManager.cancelTask(taskId);
                log.info("取消任务: taskId={}, success={}", taskId, taskCancelled);
            }

            // 4. 停止进程
            if (execution.getMonitorSysGenScriptExecutionProcessId() != null) {
                stopProcess(execution.getMonitorSysGenScriptExecutionProcessId());
            }

            // 5. 更新执行记录状态
            execution.setMonitorSysGenScriptExecutionStatus(MonitorSysGenScriptExecution.ExecutionStatus.CANCELLED);
            execution.setMonitorSysGenScriptExecutionEndTime(LocalDateTime.now());
            execution.setMonitorSysGenScriptExecutionUpdateTime(LocalDateTime.now());
            execution.calculateDuration();

            executionMapper.updateById(execution);

            // 6. 销毁任务
            if (taskId != null) {
                taskManager.destroyTask(taskId);
            }

            log.info("脚本执行已停止: executionId={}, taskId={}", executionId, taskId);
            return true;

        } catch (Exception e) {
            log.error("停止脚本执行失败: executionId={}", executionId, e);
            return false;
        }
    }

    /**
     * 构建脚本执行命令
     */
    private List<String> buildScriptCommand(MonitorSysGenScript script,
            ScriptManagementDTO.ScriptExecuteRequestDTO requestDTO) {
        List<String> command = new ArrayList<>();

        // 根据脚本类型构建命令
        switch (script.getMonitorSysGenScriptType()) {
        case SHELL:
            command.add("bash");
            command.add("-c");
            command.add(script.getMonitorSysGenScriptContent());
            break;
        case PYTHON:
            command.add("python");
            command.add("-c");
            command.add(script.getMonitorSysGenScriptContent());
            break;
        case POWERSHELL:
            command.add("powershell");
            command.add("-Command");
            command.add(script.getMonitorSysGenScriptContent());
            break;
        case BATCH:
            command.add("cmd");
            command.add("/c");
            command.add(script.getMonitorSysGenScriptContent());
            break;
        default:
            throw new UnsupportedOperationException("不支持的脚本类型: " + script.getMonitorSysGenScriptType());
        }

        // 添加参数
        if (requestDTO.getParameters() != null && !requestDTO.getParameters().trim().isEmpty()) {
            String[] params = requestDTO.getParameters().trim().split("\\s+");
            command.addAll(Arrays.asList(params));
        }

        return command;
    }

    /**
     * 解析环境变量
     */
    private Map<String, String> parseEnvironmentVars(String envVars) {
        Map<String, String> env = new HashMap<>();
        if (envVars != null && !envVars.trim().isEmpty()) {
            String[] lines = envVars.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty() && line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        env.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        }
        return env;
    }

    /**
     * 构建执行元数据
     */
    private String buildExecutionMetadata(MonitorSysGenScript script,
            ScriptManagementDTO.ScriptExecuteRequestDTO requestDTO) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("scriptName", script.getMonitorSysGenScriptName());
        metadata.put("scriptType", script.getMonitorSysGenScriptType());
        metadata.put("scriptVersion", script.getMonitorSysGenScriptVersion());
        metadata.put("requestTime", LocalDateTime.now());
        metadata.put("requestUser", getCurrentUser());

        try {
            return new ObjectMapper().writeValueAsString(metadata);
        } catch (Exception e) {
            log.warn("构建执行元数据失败", e);
            return "{}";
        }
    }

    /**
     * 更新脚本执行信息
     */
    private void updateScriptExecutionInfo(MonitorSysGenScript script) {
        script.setMonitorSysGenScriptLastExecuteTime(LocalDateTime.now());
        script.setMonitorSysGenScriptExecuteCount(
                script.getMonitorSysGenScriptExecuteCount() != null ? script.getMonitorSysGenScriptExecuteCount() + 1
                        : 1);
        scriptMapper.updateById(script);
    }

    /**
     * 停止进程
     */
    private void stopProcess(Long processId) {
        try {
            // 根据操作系统选择停止命令
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                pb = new ProcessBuilder("taskkill", "/F", "/PID", processId.toString());
            } else {
                pb = new ProcessBuilder("kill", "-9", processId.toString());
            }

            Process killProcess = pb.start();
            killProcess.waitFor(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("停止进程失败: processId={}", processId, e);
        }
    }

    /**
     * 获取当前用户
     */
    private String getCurrentUser() {
        // TODO: 从安全上下文获取当前用户
        return "system";
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
