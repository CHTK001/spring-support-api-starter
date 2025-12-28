package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.json.Json;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.constant.MonitorSocketTopics;
import com.chua.starter.monitor.entity.SystemSoftOperationLog;
import com.chua.starter.monitor.mapper.SystemSoftOperationLogMapper;
import com.chua.starter.monitor.service.SystemSoftOperationLogService;
import com.chua.socket.support.session.SocketSessionTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统软件操作日志服务实现类
 *
 * @author CH
 * @since 2025/12/28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSoftOperationLogServiceImpl extends ServiceImpl<SystemSoftOperationLogMapper, SystemSoftOperationLog>
        implements SystemSoftOperationLogService {

    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * 缓存操作的开始信息，用于计算耗时
     */
    private final ConcurrentHashMap<String, OperationContext> operationContextMap = new ConcurrentHashMap<>();

    /**
     * 操作上下文
     */
    private static class OperationContext {
        String operationType;
        String operationTarget;
        Integer containerId;
        Integer imageId;
        Integer softId;
        Integer serverId;
        Integer recordId;
        LocalDateTime startTime;
    }

    @Override
    public SystemSoftOperationLog logStart(String operationId, String operationType, String operationTarget,
                                            Integer containerId, Integer imageId, Integer softId,
                                            Integer serverId, Integer recordId, String operator) {
        // 保存操作上下文
        OperationContext context = new OperationContext();
        context.operationType = operationType;
        context.operationTarget = operationTarget;
        context.containerId = containerId;
        context.imageId = imageId;
        context.softId = softId;
        context.serverId = serverId;
        context.recordId = recordId;
        context.startTime = LocalDateTime.now();
        operationContextMap.put(operationId, context);

        // 创建日志记录
        SystemSoftOperationLog logEntry = new SystemSoftOperationLog();
        logEntry.setOperationId(operationId);
        logEntry.setOperationType(operationType);
        logEntry.setOperationTarget(operationTarget);
        logEntry.setContainerId(containerId);
        logEntry.setImageId(imageId);
        logEntry.setSoftId(softId);
        logEntry.setServerId(serverId);
        logEntry.setRecordId(recordId);
        logEntry.setLogLevel("INFO");
        logEntry.setLogMessage("操作开始: " + operationType + " - " + operationTarget);
        logEntry.setProgress(0);
        logEntry.setStatus("RUNNING");
        logEntry.setLogTime(LocalDateTime.now());
        logEntry.setStartTime(LocalDateTime.now());
        logEntry.setOperator(operator);

        save(logEntry);

        // 推送 WebSocket 消息
        pushLogMessage(logEntry);

        log.info("记录操作开始日志: operationId={}, type={}, target={}", operationId, operationType, operationTarget);
        return logEntry;
    }

    @Override
    public SystemSoftOperationLog logProgress(String operationId, String message, Integer progress,
                                               String dockerStatus, String dockerProgress) {
        OperationContext context = operationContextMap.get(operationId);

        SystemSoftOperationLog logEntry = new SystemSoftOperationLog();
        logEntry.setOperationId(operationId);
        logEntry.setLogLevel("INFO");
        logEntry.setLogMessage(message);
        logEntry.setProgress(progress);
        logEntry.setStatus("RUNNING");
        logEntry.setDockerStatus(dockerStatus);
        logEntry.setDockerProgress(dockerProgress);
        logEntry.setLogTime(LocalDateTime.now());

        if (context != null) {
            logEntry.setOperationType(context.operationType);
            logEntry.setOperationTarget(context.operationTarget);
            logEntry.setContainerId(context.containerId);
            logEntry.setImageId(context.imageId);
            logEntry.setSoftId(context.softId);
            logEntry.setServerId(context.serverId);
            logEntry.setRecordId(context.recordId);
            logEntry.setStartTime(context.startTime);
        }

        save(logEntry);

        // 推送 WebSocket 消息
        pushLogMessage(logEntry);

        log.debug("记录操作进度日志: operationId={}, progress={}, message={}", operationId, progress, message);
        return logEntry;
    }

    @Override
    public SystemSoftOperationLog logComplete(String operationId, String message, boolean success) {
        OperationContext context = operationContextMap.remove(operationId);

        SystemSoftOperationLog logEntry = new SystemSoftOperationLog();
        logEntry.setOperationId(operationId);
        logEntry.setLogLevel(success ? "INFO" : "WARN");
        logEntry.setLogMessage(message);
        logEntry.setProgress(100);
        logEntry.setStatus(success ? "SUCCESS" : "FAILED");
        logEntry.setLogTime(LocalDateTime.now());
        logEntry.setEndTime(LocalDateTime.now());

        if (context != null) {
            logEntry.setOperationType(context.operationType);
            logEntry.setOperationTarget(context.operationTarget);
            logEntry.setContainerId(context.containerId);
            logEntry.setImageId(context.imageId);
            logEntry.setSoftId(context.softId);
            logEntry.setServerId(context.serverId);
            logEntry.setRecordId(context.recordId);
            logEntry.setStartTime(context.startTime);
            logEntry.setDuration(java.time.Duration.between(context.startTime, LocalDateTime.now()).toMillis());
        }

        save(logEntry);

        // 推送 WebSocket 消息
        pushLogMessage(logEntry);

        log.info("记录操作完成日志: operationId={}, success={}, message={}", operationId, success, message);
        return logEntry;
    }

    @Override
    public SystemSoftOperationLog logError(String operationId, String message, String errorMessage, String errorStack) {
        OperationContext context = operationContextMap.remove(operationId);

        SystemSoftOperationLog logEntry = new SystemSoftOperationLog();
        logEntry.setOperationId(operationId);
        logEntry.setLogLevel("ERROR");
        logEntry.setLogMessage(message);
        logEntry.setStatus("FAILED");
        logEntry.setErrorMessage(errorMessage);
        logEntry.setErrorStack(errorStack);
        logEntry.setLogTime(LocalDateTime.now());
        logEntry.setEndTime(LocalDateTime.now());

        if (context != null) {
            logEntry.setOperationType(context.operationType);
            logEntry.setOperationTarget(context.operationTarget);
            logEntry.setContainerId(context.containerId);
            logEntry.setImageId(context.imageId);
            logEntry.setSoftId(context.softId);
            logEntry.setServerId(context.serverId);
            logEntry.setRecordId(context.recordId);
            logEntry.setStartTime(context.startTime);
            logEntry.setDuration(java.time.Duration.between(context.startTime, LocalDateTime.now()).toMillis());
        }

        save(logEntry);

        // 推送 WebSocket 消息
        pushLogMessage(logEntry);

        log.error("记录操作错误日志: operationId={}, error={}", operationId, errorMessage);
        return logEntry;
    }

    @Override
    public List<SystemSoftOperationLog> getByOperationId(String operationId) {
        return baseMapper.selectByOperationId(operationId);
    }

    @Override
    public List<SystemSoftOperationLog> getRecentByContainerId(Integer containerId, int limit) {
        return baseMapper.selectRecentByContainerId(containerId, limit);
    }

    @Override
    public List<SystemSoftOperationLog> getRecentByServerId(Integer serverId, int limit) {
        return baseMapper.selectRecentByServerId(serverId, limit);
    }

    @Override
    public IPage<SystemSoftOperationLog> pageQuery(int page, int size, String operationType,
                                                    Integer serverId, Integer containerId, String status) {
        LambdaQueryWrapper<SystemSoftOperationLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(operationType)) {
            wrapper.eq(SystemSoftOperationLog::getOperationType, operationType);
        }
        if (serverId != null) {
            wrapper.eq(SystemSoftOperationLog::getServerId, serverId);
        }
        if (containerId != null) {
            wrapper.eq(SystemSoftOperationLog::getContainerId, containerId);
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(SystemSoftOperationLog::getStatus, status);
        }

        wrapper.orderByDesc(SystemSoftOperationLog::getLogTime);

        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 推送日志消息到 WebSocket
     */
    private void pushLogMessage(SystemSoftOperationLog logEntry) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("logId", logEntry.getLogId());
            data.put("operationId", logEntry.getOperationId());
            data.put("operationType", logEntry.getOperationType());
            data.put("operationTarget", logEntry.getOperationTarget());
            data.put("containerId", logEntry.getContainerId());
            data.put("imageId", logEntry.getImageId());
            data.put("softId", logEntry.getSoftId());
            data.put("serverId", logEntry.getServerId());
            data.put("recordId", logEntry.getRecordId());
            data.put("logLevel", logEntry.getLogLevel());
            data.put("logMessage", logEntry.getLogMessage());
            data.put("progress", logEntry.getProgress());
            data.put("status", logEntry.getStatus());
            data.put("dockerStatus", logEntry.getDockerStatus());
            data.put("dockerProgress", logEntry.getDockerProgress());
            data.put("errorMessage", logEntry.getErrorMessage());
            data.put("logTime", logEntry.getLogTime());
            data.put("duration", logEntry.getDuration());

            socketSessionTemplate.send(MonitorSocketTopics.Monitor.Docker.PROGRESS, Json.toJson(data));
        } catch (Exception e) {
            log.warn("推送操作日志消息失败", e);
        }
    }
}
