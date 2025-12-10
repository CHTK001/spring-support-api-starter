package com.chua.starter.monitor.pojo;

import com.chua.docker.support.client.DockerProtocolClient;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Docker客户端批量创建结果
 * 包含成功、失败、未启用等详细状态信息
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/04
 */
@Data
public class DockerClientCreateResult {

    /**
     * 成功创建的客户端映射
     * Key: serverId, Value: DockerProtocolClient
     */
    private Map<Integer, DockerProtocolClient> successClients = new LinkedHashMap<>();

    /**
     * 未启用Docker的服务器列表
     */
    private List<ServerStatus> disabledServers = new ArrayList<>();

    /**
     * 创建失败的服务器列表
     */
    private List<ServerStatus> failedServers = new ArrayList<>();

    /**
     * 配置不存在的服务器ID列表
     */
    private List<Integer> notFoundServers = new ArrayList<>();

    /**
     * 添加成功的客户端
     *
     * @param serverId 服务器ID
     * @param client   Docker客户端
     */
    public void addSuccess(Integer serverId, DockerProtocolClient client) {
        successClients.put(serverId, client);
    }

    /**
     * 添加未启用Docker的服务器
     *
     * @param serverId   服务器ID
     * @param serverName 服务器名称
     */
    public void addDisabled(Integer serverId, String serverName) {
        disabledServers.add(new ServerStatus(serverId, serverName, "未启用Docker"));
    }

    /**
     * 添加创建失败的服务器
     *
     * @param serverId   服务器ID
     * @param serverName 服务器名称
     * @param errorMsg   错误信息
     */
    public void addFailed(Integer serverId, String serverName, String errorMsg) {
        failedServers.add(new ServerStatus(serverId, serverName, errorMsg));
    }

    /**
     * 添加配置不存在的服务器
     *
     * @param serverId 服务器ID
     */
    public void addNotFound(Integer serverId) {
        notFoundServers.add(serverId);
    }

    /**
     * 获取成功数量
     */
    public int getSuccessCount() {
        return successClients.size();
    }

    /**
     * 获取失败数量（包括未启用和创建失败）
     */
    public int getFailedCount() {
        return disabledServers.size() + failedServers.size() + notFoundServers.size();
    }

    /**
     * 是否全部成功
     */
    public boolean isAllSuccess() {
        return getFailedCount() == 0;
    }

    /**
     * 是否有成功的
     */
    public boolean hasSuccess() {
        return !successClients.isEmpty();
    }

    /**
     * 生成摘要信息
     *
     * @return 摘要信息字符串
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("成功: ").append(getSuccessCount()).append("个");

        if (!disabledServers.isEmpty()) {
            sb.append(", 未启用Docker: ").append(disabledServers.size()).append("个");
        }
        if (!failedServers.isEmpty()) {
            sb.append(", 连接失败: ").append(failedServers.size()).append("个");
        }
        if (!notFoundServers.isEmpty()) {
            sb.append(", 配置不存在: ").append(notFoundServers.size()).append("个");
        }

        return sb.toString();
    }

    /**
     * 生成详细的错误信息
     *
     * @return 详细错误信息字符串
     */
    public String getDetailedErrorMessage() {
        if (isAllSuccess()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        if (!disabledServers.isEmpty()) {
            sb.append("【未启用Docker的服务器】\n");
            for (ServerStatus server : disabledServers) {
                sb.append("  - ").append(server.getServerName())
                        .append(" (ID: ").append(server.getServerId()).append(")\n");
            }
        }

        if (!failedServers.isEmpty()) {
            sb.append("【连接失败的服务器】\n");
            for (ServerStatus server : failedServers) {
                sb.append("  - ").append(server.getServerName())
                        .append(" (ID: ").append(server.getServerId()).append(")")
                        .append(": ").append(server.getMessage()).append("\n");
            }
        }

        if (!notFoundServers.isEmpty()) {
            sb.append("【配置不存在的服务器】\n");
            sb.append("  - 服务器ID: ").append(notFoundServers).append("\n");
        }

        return sb.toString();
    }

    /**
     * 服务器状态信息
     */
    @Data
    public static class ServerStatus {
        /**
         * 服务器ID
         */
        private Integer serverId;

        /**
         * 服务器名称
         */
        private String serverName;

        /**
         * 状态消息（错误原因等）
         */
        private String message;

        public ServerStatus(Integer serverId, String serverName, String message) {
            this.serverId = serverId;
            this.serverName = serverName;
            this.message = message;
        }
    }
}
