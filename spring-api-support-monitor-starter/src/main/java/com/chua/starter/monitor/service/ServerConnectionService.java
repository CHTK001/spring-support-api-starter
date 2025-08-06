package com.chua.starter.monitor.service;

import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;

import java.util.Map;

/**
 * 服务器连接服务接口
 *
 * @author CH
 * @since 2024/12/18
 */
public interface ServerConnectionService {

    /**
     * 获取协议类型
     *
     * @return 协议类型
     */
    String getProtocol();

    /**
     * 测试连接
     *
     * @param server 服务器配置
     * @return 连接结果
     */
    boolean testConnection(MonitorSysGenServer server);

    /**
     * 建立连接
     *
     * @param server 服务器配置
     * @return 连接是否成功
     */
    boolean connect(MonitorSysGenServer server);

    /**
     * 断开连接
     *
     * @param serverId 服务器ID
     */
    void disconnect(Integer serverId);

    /**
     * 检查连接状态
     *
     * @param serverId 服务器ID
     * @return 是否连接
     */
    boolean isConnected(Integer serverId);

    /**
     * 执行命令
     *
     * @param serverId 服务器ID
     * @param command 命令
     * @return 执行结果
     */
    String executeCommand(Integer serverId, String command);

    /**
     * 收集服务器指标
     *
     * @param server 服务器配置
     * @return 服务器指标数据
     */
    MonitorSysGenServerMetrics collectMetrics(MonitorSysGenServer server);

    /**
     * 获取服务器基本信息
     *
     * @param serverId 服务器ID
     * @return 服务器信息
     */
    Map<String, Object> getServerInfo(Integer serverId);

    /**
     * 获取连接会话ID (用于WebSocket)
     *
     * @param serverId 服务器ID
     * @return 会话ID
     */
    String getSessionId(Integer serverId);

    /**
     * 发送数据到服务器
     *
     * @param serverId 服务器ID
     * @param data 数据
     */
    void sendData(Integer serverId, String data);

    /**
     * 获取终端大小
     *
     * @param serverId 服务器ID
     * @return 终端大小 [width, height]
     */
    int[] getTerminalSize(Integer serverId);

    /**
     * 调整终端大小
     *
     * @param serverId 服务器ID
     * @param width 宽度
     * @param height 高度
     */
    void resizeTerminal(Integer serverId, int width, int height);

    /**
     * 获取文件列表
     *
     * @param serverId 服务器ID
     * @param path 路径
     * @return 文件列表
     */
    Map<String, Object> listFiles(Integer serverId, String path);

    /**
     * 上传文件
     *
     * @param serverId 服务器ID
     * @param localPath 本地路径
     * @param remotePath 远程路径
     * @return 上传结果
     */
    boolean uploadFile(Integer serverId, String localPath, String remotePath);

    /**
     * 下载文件
     *
     * @param serverId 服务器ID
     * @param remotePath 远程路径
     * @param localPath 本地路径
     * @return 下载结果
     */
    boolean downloadFile(Integer serverId, String remotePath, String localPath);

    /**
     * 设置数据回调（用于实时数据传输）
     *
     * @param serverId 服务器ID
     * @param callback 数据回调函数
     */
    void setDataCallback(Integer serverId, DataCallback callback);

    /**
     * 数据回调接口
     */
    @FunctionalInterface
    interface DataCallback {
        void onData(String data);
    }
}
