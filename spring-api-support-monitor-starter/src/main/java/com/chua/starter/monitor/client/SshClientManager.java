package com.chua.starter.monitor.client;

import com.chua.common.support.utils.DigestUtils;
import com.chua.starter.monitor.entity.MaintenanceHost;
import com.jcraft.jsch.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * SSH客户端管理器
 * <p>
 * 用于管理和缓存SSH客户端连接，包括创建、复用、定时清理等功能
 * </p>
 *
 * @author CH
 */
@Slf4j
@Component
public class SshClientManager implements DisposableBean {

    /**
     * 默认连接超时时间（毫秒）
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
    
    /**
     * 默认空闲超时时间（分钟）
     */
    private static final int DEFAULT_IDLE_TIMEOUT_MINUTES = 30;
    
    /**
     * 客户端缓存，key为主机ID
     */
    private final Map<Long, SshClientContext> clientCache = new ConcurrentHashMap<>();

    /**
     * 获取SSH会话
     *
     * @param host 维护主机信息
     * @return SSH会话
     * @throws JSchException SSH连接异常
     */
    public synchronized Session getSession(MaintenanceHost host) throws JSchException {
        if (host == null || host.getMaintenanceHostId() == null) {
            throw new IllegalArgumentException("主机信息不能为空");
        }

        // 检查缓存中是否已存在
        SshClientContext context = clientCache.get(host.getMaintenanceHostId());
        
        // 如果缓存中存在且连接有效，则更新最后访问时间并返回
        if (context != null && isSessionValid(context.getSession())) {
            log.debug("从缓存获取主机[{}]的SSH连接", host.getMaintenanceHostAddress());
            context.setLastAccessTime(LocalDateTime.now());
            return context.getSession();
        }

        // 如果缓存中不存在或连接无效，则创建新连接
        log.info("为主机[{}]创建新的SSH连接", host.getMaintenanceHostAddress());
        Session session = createSession(
            host.getMaintenanceHostAddress(),
            host.getMaintenanceHostPort(),
            host.getMaintenanceHostUsername(),
                DigestUtils.aesDecrypt(host.getMaintenanceHostPassword(), "1234567890Oil#@1")
        );
        
        // 更新缓存
        SshClientContext newContext = new SshClientContext();
        newContext.setHostId(host.getMaintenanceHostId());
        newContext.setSession(session);
        newContext.setHostAddress(host.getMaintenanceHostAddress());
        newContext.setLastAccessTime(LocalDateTime.now());
        
        clientCache.put(host.getMaintenanceHostId(), newContext);
        
        return session;
    }
    
    /**
     * 创建SFTP通道
     *
     * @param host 维护主机信息
     * @return SFTP通道
     * @throws JSchException SSH连接异常
     */
    public ChannelSftp createSftpChannel(MaintenanceHost host) throws JSchException {
        Session session = getSession(host);
        Channel channel = session.openChannel("sftp");
        channel.connect(DEFAULT_CONNECT_TIMEOUT);
        return (ChannelSftp) channel;
    }
    
    /**
     * 创建执行通道
     *
     * @param host 维护主机信息
     * @return 执行通道
     * @throws JSchException SSH连接异常
     */
    public ChannelExec createExecChannel(MaintenanceHost host) throws JSchException {
        Session session = getSession(host);
        Channel channel = session.openChannel("exec");
        return (ChannelExec) channel;
    }

    /**
     * 创建SSH会话
     *
     * @param host     主机地址
     * @param port     端口
     * @param username 用户名
     * @param password 密码
     * @return SSH会话
     * @throws JSchException SSH连接异常
     */
    private Session createSession(String host, Integer port, String username, String password) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        
        session.connect(DEFAULT_CONNECT_TIMEOUT);
        return session;
    }
    
    /**
     * 检查会话是否有效
     *
     * @param session SSH会话
     * @return 是否有效
     */
    private boolean isSessionValid(Session session) {
        return session != null && session.isConnected();
    }
    
    /**
     * 关闭指定主机的连接
     *
     * @param hostId 主机ID
     */
    public synchronized void closeSession(Long hostId) {
        SshClientContext context = clientCache.remove(hostId);
        if (context != null && context.getSession() != null) {
            try {
                context.getSession().disconnect();
                log.info("已关闭主机[{}]的SSH连接", context.getHostAddress());
            } catch (Exception e) {
                log.error("关闭主机[{}]的SSH连接失败: {}", context.getHostAddress(), e.getMessage(), e);
            }
        }
    }

    /**
     * 定时任务：清理长时间未使用的连接
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public synchronized void cleanIdleSessions() {
        log.debug("开始清理空闲SSH连接...");
        LocalDateTime now = LocalDateTime.now();
        
        clientCache.entrySet().removeIf(entry -> {
            SshClientContext context = entry.getValue();
            LocalDateTime lastAccessTime = context.getLastAccessTime();
            
            // 检查是否超过空闲时间
            if (lastAccessTime.plusMinutes(DEFAULT_IDLE_TIMEOUT_MINUTES).isBefore(now)) {
                try {
                    if (context.getSession() != null) {
                        context.getSession().disconnect();
                    }
                    log.info("已清理空闲超时的SSH连接: 主机[{}]", context.getHostAddress());
                    return true;
                } catch (Exception e) {
                    log.error("关闭空闲SSH连接失败: 主机[{}], 错误: {}", context.getHostAddress(), e.getMessage(), e);
                }
            }
            return false;
        });
    }

    /**
     * Spring容器关闭时关闭所有连接
     */
    @Override
    public void destroy() {
        log.info("应用关闭，正在关闭所有SSH连接...");
        
        clientCache.forEach((hostId, context) -> {
            try {
                if (context.getSession() != null) {
                    context.getSession().disconnect();
                    log.info("已关闭主机[{}]的SSH连接", context.getHostAddress());
                }
            } catch (Exception e) {
                log.error("关闭主机[{}]的SSH连接失败: {}", context.getHostAddress(), e.getMessage(), e);
            }
        });
        
        clientCache.clear();
        log.info("所有SSH连接已关闭");
    }

    /**
     * SSH客户端上下文
     */
    @Data
    private static class SshClientContext {
        /**
         * 主机ID
         */
        private Long hostId;
        
        /**
         * 主机地址
         */
        private String hostAddress;
        
        /**
         * SSH会话
         */
        private Session session;
        
        /**
         * 最后访问时间
         */
        private LocalDateTime lastAccessTime;
    }
} 