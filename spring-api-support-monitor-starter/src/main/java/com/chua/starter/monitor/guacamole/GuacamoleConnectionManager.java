package com.chua.starter.monitor.guacamole;

import com.chua.starter.monitor.guacamole.GuacamoleTunnel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guacamole连接管理器
 * 管理所有Guacamole连接的生命周期
 *
 * @author CH
 * @since 2024/12/18
 */
@Component
@Slf4j
public class GuacamoleConnectionManager {

    /**
     * 服务器连接映射
     */
    private final Map<Integer, com.chua.starter.monitor.guacamole.GuacamoleTunnel> serverConnections = new ConcurrentHashMap<>();

    /**
     * 会话连接映射
     */
    private final Map<String, com.chua.starter.monitor.guacamole.GuacamoleTunnel> sessionConnections = new ConcurrentHashMap<>();

    /**
     * 服务器与会话映射
     */
    private final Map<Integer, String> serverSessionMap = new ConcurrentHashMap<>();

    /**
     * 创建连接
     */
    public com.chua.starter.monitor.guacamole.GuacamoleTunnel createConnection(Integer serverId, WebSocketSession session, Object guacamoleClient) {
        try {
            String sessionId = session.getId();
            
            // 如果已存在连接，先关闭
            closeConnection(serverId);
            
            // 创建新的隧道
            com.chua.starter.monitor.guacamole.GuacamoleTunnel tunnel = new com.chua.starter.monitor.guacamole.GuacamoleTunnel(serverId, guacamoleClient);
            
            // 设置回调
            tunnel.setErrorCallback(error -> {
                log.error("Guacamole连接错误: serverId={}, error={}", serverId, error.getMessage(), error);
                closeConnection(serverId);
            });
            
            tunnel.setCloseCallback(() -> {
                log.info("Guacamole连接关闭: serverId={}", serverId);
                cleanupConnection(serverId, sessionId);
            });
            
            // 尝试连接
            if (tunnel.connect()) {
                serverConnections.put(serverId, tunnel);
                sessionConnections.put(sessionId, tunnel);
                serverSessionMap.put(serverId, sessionId);
                
                log.info("Guacamole连接创建成功: serverId={}, sessionId={}", serverId, sessionId);
                return tunnel;
            } else {
                log.error("Guacamole连接创建失败: serverId={}", serverId);
                return null;
            }
            
        } catch (Exception e) {
            log.error("创建Guacamole连接异常: serverId={}, error={}", serverId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 关闭连接
     */
    public void closeConnection(Integer serverId) {
        try {
            com.chua.starter.monitor.guacamole.GuacamoleTunnel tunnel = serverConnections.get(serverId);
            if (tunnel != null) {
                tunnel.disconnect();
                
                String sessionId = serverSessionMap.get(serverId);
                cleanupConnection(serverId, sessionId);
                
                log.info("Guacamole连接已关闭: serverId={}", serverId);
            }
        } catch (Exception e) {
            log.error("关闭Guacamole连接异常: serverId={}, error={}", serverId, e.getMessage(), e);
        }
    }

    /**
     * 通过会话关闭连接
     */
    public void closeConnectionBySession(String sessionId) {
        try {
            com.chua.starter.monitor.guacamole.GuacamoleTunnel tunnel = sessionConnections.get(sessionId);
            if (tunnel != null) {
                Integer serverId = tunnel.getServerId();
                closeConnection(serverId);
                log.info("通过会话关闭Guacamole连接: sessionId={}, serverId={}", sessionId, serverId);
            }
        } catch (Exception e) {
            log.error("通过会话关闭Guacamole连接异常: sessionId={}, error={}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * 发送数据
     */
    public boolean sendData(Integer serverId, String data) {
        try {
            com.chua.starter.monitor.guacamole.GuacamoleTunnel tunnel = serverConnections.get(serverId);
            if (tunnel != null && tunnel.isConnected()) {
                return tunnel.sendData(data);
            } else {
                log.warn("Guacamole连接不存在或未连接: serverId={}", serverId);
                return false;
            }
        } catch (Exception e) {
            log.error("发送Guacamole数据异常: serverId={}, error={}", serverId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取连接
     */
    public com.chua.starter.monitor.guacamole.GuacamoleTunnel getConnection(Integer serverId) {
        return serverConnections.get(serverId);
    }

    /**
     * 检查连接是否存在
     */
    public boolean hasConnection(Integer serverId) {
        com.chua.starter.monitor.guacamole.GuacamoleTunnel tunnel = serverConnections.get(serverId);
        return tunnel != null && tunnel.isConnected();
    }

    /**
     * 获取所有连接
     */
    public Map<Integer, GuacamoleTunnel> getAllConnections() {
        return new ConcurrentHashMap<>(serverConnections);
    }

    /**
     * 获取连接数量
     */
    public int getConnectionCount() {
        return serverConnections.size();
    }

    /**
     * 清理连接
     */
    private void cleanupConnection(Integer serverId, String sessionId) {
        if (serverId != null) {
            serverConnections.remove(serverId);
            serverSessionMap.remove(serverId);
        }
        if (sessionId != null) {
            sessionConnections.remove(sessionId);
        }
    }

    /**
     * 关闭所有连接
     */
    public void closeAllConnections() {
        try {
            log.info("开始关闭所有Guacamole连接，当前连接数: {}", serverConnections.size());
            
            for (Integer serverId : serverConnections.keySet()) {
                closeConnection(serverId);
            }
            
            // 清理所有映射
            serverConnections.clear();
            sessionConnections.clear();
            serverSessionMap.clear();
            
            log.info("所有Guacamole连接已关闭");
        } catch (Exception e) {
            log.error("关闭所有Guacamole连接异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取连接统计信息
     */
    public String getConnectionStats() {
        return String.format("GuacamoleConnectionManager[总连接数=%d, 服务器连接=%d, 会话连接=%d]",
                           getConnectionCount(),
                           serverConnections.size(),
                           sessionConnections.size());
    }
}
