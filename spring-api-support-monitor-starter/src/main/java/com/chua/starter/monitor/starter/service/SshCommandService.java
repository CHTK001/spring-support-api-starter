package com.chua.starter.monitor.starter.service;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;

/**
 * SSH 命令处理服务接口
 *
 * @author CH
 * @since 2024/12/23
 */
public interface SshCommandService {

    /**
     * 建立 SSH 连接
     *
     * @param server 服务器配置
     * @return 连接是否成功
     */
    boolean connectSSH(MonitorSysGenServer server);

    /**
     * 断开 SSH 连接
     *
     * @param serverId 服务器ID
     */
    void disconnectSSH(Integer serverId);

    /**
     * 发送 SSH 输入
     *
     * @param serverId 服务器ID
     * @param input 输入内容
     * @return 发送是否成功
     */
    boolean sendSSHInput(Integer serverId, String input);

    /**
     * 调整终端大小
     *
     * @param serverId 服务器ID
     * @param cols 列数
     * @param rows 行数
     * @return 调整是否成功
     */
    boolean resizeSSHTerminal(Integer serverId, int cols, int rows);

    /**
     * 检查 SSH 连接状态
     *
     * @param serverId 服务器ID
     * @return 是否连接
     */
    boolean isSSHConnected(Integer serverId);

    /**
     * 执行 SSH 命令
     *
     * @param serverId 服务器ID
     * @param command 命令
     * @return 执行结果
     */
    String executeSSHCommand(Integer serverId, String command);

    /**
     * 设置 SSH 数据回调
     *
     * @param serverId 服务器ID
     * @param callback 数据回调
     */
    void setSSHDataCallback(Integer serverId, DataCallback callback);

    /**
     * 数据回调接口
     */
    @FunctionalInterface
    interface DataCallback {
        void onData(String data);
    }
}
