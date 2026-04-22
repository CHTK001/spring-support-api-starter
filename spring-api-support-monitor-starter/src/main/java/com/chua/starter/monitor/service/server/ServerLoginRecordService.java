package com.chua.starter.monitor.service.server;

import com.chua.starter.monitor.entity.MonitorSysGenServerLoginRecord;
import com.chua.starter.monitor.spi.ServerLoginRecordProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 服务器登录记录服务
 *
 * @author CH
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ServerLoginRecordService {

    private static final int DEFAULT_TOP_N = 10;

    private final ObjectProvider<ServerLoginRecordProvider> loginRecordProvider;

    /**
     * 获取登录记录提供者
     */
    private ServerLoginRecordProvider getProvider() {
        try {
            return loginRecordProvider.orderedStream()
                    .filter(provider -> "database".equalsIgnoreCase(provider.provider()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.warn("获取登录记录提供者失败", e);
            return null;
        }
    }

    /**
     * 获取最近N条登录记录
     *
     * @param serverId 服务器ID（null表示所有服务器）
     * @param topN    获取记录数
     * @return 登录记录列表
     */
    public java.util.List<MonitorSysGenServerLoginRecord> getTopNLoginRecords(Integer serverId, int topN) {
        ServerLoginRecordProvider provider = getProvider();
        if (provider != null) {
            try {
                return provider.getTopNLoginRecords(serverId, topN > 0 ? topN : DEFAULT_TOP_N);
            } catch (Exception e) {
                log.error("获取登录记录失败", e);
            }
        }
        return java.util.Collections.emptyList();
    }

    /**
     * 记录登录事件
     *
     * @param serverId   服务器ID
     * @param serverName 服务器名称
     * @param host       主机地址
     * @param username   用户名
     * @param loginType 登录方式
     * @param loginIp   登录IP
     * @param status    状态
     * @param message   消息
     */
    public void recordLogin(Integer serverId, String serverName, String host,
                            String username, String loginType, String loginIp,
                            String status, String message) {
        ServerLoginRecordProvider provider = getProvider();
        if (provider != null) {
            try {
                MonitorSysGenServerLoginRecord record = new MonitorSysGenServerLoginRecord();
                record.setServerId(serverId);
                record.setServerName(serverName);
                record.setHost(host);
                record.setUsername(username);
                record.setLoginType(loginType);
                record.setLoginIp(loginIp);
                record.setStatus(status);
                record.setLoginTime(LocalDateTime.now());
                record.setMessage(message);
                record.setCreateTime(LocalDateTime.now());
                provider.recordLogin(record);
                log.debug("登录记录已保存: serverId={}, username={}, status={}", serverId, username, status);
            } catch (Exception e) {
                log.error("保存登录记录失败", e);
            }
        }
    }

    /**
     * 记录登录成功
     */
    public void recordLoginSuccess(Integer serverId, String serverName, String host,
                                   String username, String loginType, String loginIp) {
        recordLogin(serverId, serverName, host, username, loginType, loginIp, "SUCCESS", "登录成功");
    }

    /**
     * 记录登录失败
     */
    public void recordLoginFailed(Integer serverId, String serverName, String host,
                                  String username, String loginType, String loginIp, String reason) {
        recordLogin(serverId, serverName, host, username, loginType, loginIp, "FAILED", reason);
    }

    /**
     * 记录登出事件
     *
     * @param serverId 服务器ID
     * @param duration 登录时长（毫秒）
     */
    public void recordLogout(Integer serverId, long duration) {
        // 登出记录可以通过更新现有的登录记录来实现
        // 这里暂时不做处理，因为查询最近记录时主要看登录事件
    }
}
