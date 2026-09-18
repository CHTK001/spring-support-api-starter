package com.chua.agent;

import com.chua.common.support.network.sync.SyncClient;
import com.chua.common.support.network.sync.impl.TcpSyncClient;

/**
 * 指标上报线程：先向平台注册，之后每 10 秒推送一次指标。
 *
 * <p>使用 TcpSyncClient 长连接推送，替代原来的 HttpClient 短连接。
 * 连接 monitor-starter 的 TCP SyncServer（默认端口 19390），
 * 按 {@code topic:message} 格式发送消息。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public class MetricsPusher implements Runnable {

    /** 平台 TCP 地址（如 tcp://127.0.0.1:19390） */
    private final String serverTcpUrl;
    /** 主机探针标识（定位资产主机，同时作为 clientId） */
    private final String agentKey;
    /** 系统信息采集器 */
    private final AgentSystemInfo sysInfo;
    /** TCP 长连接客户端 */
    private SyncClient syncClient;

    /**
     * 构造上报线程。
     *
     * @param serverTcpUrl 平台 TCP 地址
     * @param agentKey     主机探针标识
     * @param sysInfo      系统信息采集器
     */
    public MetricsPusher(String serverTcpUrl, String agentKey, AgentSystemInfo sysInfo) {
        this.serverTcpUrl = serverTcpUrl;
        this.agentKey = agentKey == null ? "" : agentKey;
        this.sysInfo = sysInfo;
    }

    /**
     * 线程主体：连接成功后进入 10 秒间隔的指标推送循环，被中断时退出。
     */
    @Override
    public void run() {
        // 连接 TCP SyncServer
        try {
            syncClient = new TcpSyncClient(agentKey, serverTcpUrl);
            syncClient.connect();
            System.out.println("[Agent] Connected to TCP server: " + serverTcpUrl + " agentKey=" + agentKey);
        } catch (Exception e) {
            System.err.println("[Agent] TCP connect failed: " + e.getMessage());
            return;
        }

        // 先上报一次基础信息
        try {
            String info = sysInfo.info();
            syncClient.send("agent-info", info);
            System.out.println("[Agent] Sent base info");
        } catch (Exception e) {
            System.err.println("[Agent] Send info failed: " + e.getMessage());
        }

        // 指标推送循环
        while (true) {
            try {
                Thread.sleep(10_000);
                String metrics = sysInfo.metrics();
                syncClient.send("agent-metrics", metrics);
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.err.println("[Agent] Push failed: " + e.getMessage());
                // 尝试重连
                try {
                    if (syncClient != null) {
                        syncClient.disconnect();
                    }
                    syncClient = new TcpSyncClient(agentKey, serverTcpUrl);
                    syncClient.connect();
                    System.out.println("[Agent] Reconnected to TCP server");
                } catch (Exception reconnectError) {
                    System.err.println("[Agent] Reconnect failed: " + reconnectError.getMessage());
                }
            }
        }

        // 断开连接
        if (syncClient != null) {
            syncClient.disconnect();
        }
    }
}
