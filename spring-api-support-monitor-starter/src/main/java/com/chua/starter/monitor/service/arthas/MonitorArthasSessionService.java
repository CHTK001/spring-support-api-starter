package com.chua.starter.monitor.service;

import com.chua.starter.monitor.entity.MonitorArthasSession;
import com.chua.starter.monitor.mapper.MonitorArthasSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Arthas Session 管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorArthasSessionService {

    private final MonitorArthasSessionMapper sessionMapper;

    /**
     * 创建新的session记录
     */
    public MonitorArthasSession createSession(String sessionId, String consumerId, Integer jobId,
                                              String nodeId, String httpUrl, String command, String commandType) {
        MonitorArthasSession session = new MonitorArthasSession();
        session.setSessionId(sessionId);
        session.setConsumerId(consumerId);
        session.setJobId(jobId);
        session.setNodeId(nodeId);
        session.setHttpUrl(httpUrl);
        session.setCommand(command);
        session.setCommandType(commandType);
        session.setStatus(MonitorArthasSession.STATUS_RUNNING);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        // 设置过期时间为1小时后
        session.setExpireTime(LocalDateTime.now().plusHours(1));

        sessionMapper.insert(session);
        log.info("创建Arthas session记录: sessionId={}, nodeId={}, command={}", sessionId, nodeId, command);
        return session;
    }

    /**
     * 根据sessionId获取session信息
     */
    public MonitorArthasSession getSessionBySessionId(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return null;
        }
        return sessionMapper.selectBySessionId(sessionId);
    }

    /**
     * 更新session信息
     */
    public boolean updateSession(MonitorArthasSession session) {
        if (session == null || !StringUtils.hasText(session.getSessionId())) {
            return false;
        }
        session.setUpdateTime(LocalDateTime.now());
        int result = sessionMapper.updateById(session);
        if (result > 0) {
            log.info("更新Arthas session信息: sessionId={}", session.getSessionId());
        }
        return result > 0;
    }

    /**
     * 更新session状态
     */
    public boolean updateSessionStatus(String sessionId, String status) {
        if (!StringUtils.hasText(sessionId)) {
            return false;
        }

        MonitorArthasSession monitorArthasSession = sessionMapper.selectBySessionId(sessionId);
        monitorArthasSession.setStatus(status);
        sessionMapper.updateById(monitorArthasSession);
        return true;
    }

    /**
     * 标记session为已完成
     */
    public boolean completeSession(String sessionId) {
        return updateSessionStatus(sessionId, MonitorArthasSession.STATUS_COMPLETED);
    }

    /**
     * 标记session为已中断
     */
    public boolean interruptSession(String sessionId) {
        return updateSessionStatus(sessionId, MonitorArthasSession.STATUS_INTERRUPTED);
    }

    /**
     * 获取节点的运行中session列表
     */
    public List<MonitorArthasSession> getRunningSessionsByNodeId(String nodeId) {
        if (!StringUtils.hasText(nodeId)) {
            return List.of();
        }
        return sessionMapper.selectRunningSessionsByNodeId(nodeId);
    }

    /**
     * 清理过期的session
     */
    public int cleanExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        int count = sessionMapper.updateExpiredSessions(now, now);
        if (count > 0) {
            log.info("清理过期的Arthas session: {} 个", count);
        }
        return count;
    }

    /**
     * 删除历史session记录（保留最近7天的记录）
     */
    public int deleteOldSessions() {
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(7);
        int count = sessionMapper.deleteOldSessions(beforeTime);
        if (count > 0) {
            log.info("删除历史Arthas session记录: {} 个", count);
        }
        return count;
    }

    /**
     * 获取session状态统计
     */
    public List<java.util.Map<String, Object>> getSessionStatusStats() {
        return sessionMapper.countByStatus();
    }

    /**
     * 根据命令提取命令类型
     */
    public String extractCommandType(String command) {
        if (!StringUtils.hasText(command)) {
            return MonitorArthasSession.TYPE_OTHER;
        }

        String cmd = command.trim().toLowerCase();
        if (cmd.startsWith("trace")) {
            return MonitorArthasSession.TYPE_TRACE;
        } else if (cmd.startsWith("thread")) {
            return MonitorArthasSession.TYPE_THREAD;
        } else if (cmd.startsWith("jvm")) {
            return MonitorArthasSession.TYPE_JVM;
        } else if (cmd.startsWith("heap")) {
            return MonitorArthasSession.TYPE_HEAP;
        } else {
            return MonitorArthasSession.TYPE_OTHER;
        }
    }

    /**
     * 强制清理指定节点的所有运行中session
     */
    public int forceCleanNodeSessions(String nodeId) {
        if (!StringUtils.hasText(nodeId)) {
            return 0;
        }

        List<MonitorArthasSession> runningSessions = getRunningSessionsByNodeId(nodeId);
        int count = 0;
        for (MonitorArthasSession session : runningSessions) {
            if (updateSessionStatus(session.getSessionId(), MonitorArthasSession.STATUS_INTERRUPTED)) {
                count++;
            }
        }

        if (count > 0) {
            log.info("强制清理节点 {} 的运行中session: {} 个", nodeId, count);
        }
        return count;
    }
}
