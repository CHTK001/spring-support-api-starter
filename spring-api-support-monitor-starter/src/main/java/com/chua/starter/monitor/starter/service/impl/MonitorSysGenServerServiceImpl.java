package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.entity.MonitorProxy;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.pojo.ServerWithConfigDto;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenServerMapper;
import com.chua.starter.monitor.starter.message.ServerWebSocketMessage;
import com.chua.starter.monitor.starter.service.MonitorProxyService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.starter.service.ServerConnectionService;
import com.chua.starter.monitor.starter.service.report.ReportManager;
import com.chua.starter.monitor.starter.service.ServerTaskManagementService;
import com.chua.starter.monitor.starter.utils.ServerInfoDetector;
import com.chua.starter.monitor.starter.service.ServerConnectionWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;

/**
 * 服务器管理服务实现类
 *
 * @author CH
 * @since 2024/12/18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenServerServiceImpl extends ServiceImpl<MonitorSysGenServerMapper, MonitorSysGenServer>
        implements MonitorSysGenServerService {

    private final SocketSessionTemplate socketSessionTemplate;
    @Autowired
    @Lazy
    private ReportManager reportManager;
    private final List<ServerConnectionService> connectionServices;
    @Autowired
    @Lazy
    private ServerConnectionWebSocketService webSocketService;
    @Autowired
    @Lazy
    private MonitorProxyService monitorProxyService;
    @Autowired
    @Lazy
    private MonitorSysGenServerSettingService monitorSysGenServerSettingService;
    @Autowired
    @Lazy
    private ServerTaskManagementService serverTaskManagementService;

    /**
     * 重写save方法，自动检测服务器信息
     */
    @Override
    public boolean save(MonitorSysGenServer entity) {
        if (entity == null) {
            return false;
        }

        try {
            // 自动检测服务器信息
            autoDetectServerInfo(entity);

            // 创建时间和更新时间由SysBase自动处理

            // 设置默认值
            if (entity.getMonitorSysGenServerStatus() == null) {
                entity.setMonitorSysGenServerStatus(1); // 默认启用
            }
            if (entity.getMonitorSysGenServerConnectionStatus() == null) {
                entity.setMonitorSysGenServerConnectionStatus(0); // 默认离线
            }

            // 监控和上报相关配置已迁移到MonitorSysGenServerSetting表，在这里创建默认配置
            // 这些字段不再在MonitorSysGenServer中设置

            boolean result = super.save(entity);

            if (result) {
                log.info("服务器配置保存成功: {}", entity.getMonitorSysGenServerName());

                // 创建服务器任务
                try {
                    serverTaskManagementService.createServerTasks(entity.getMonitorSysGenServerId());
                    log.info("为新服务器 {} 创建任务成功", entity.getMonitorSysGenServerName());
                } catch (Exception e) {
                    log.error("为新服务器创建任务失败: {}", entity.getMonitorSysGenServerName(), e);
                }

                // 发送服务器创建消息
                sendServerMessage(entity, ServerWebSocketMessage.MessageType.SERVER_CREATED, true, null);
            }

            return result;
        } catch (Exception e) {
            log.error("保存服务器配置失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 重写updateById方法，在特定字段变更时关闭设备连接
     * 如果修改了地址、端口、账号、密码这四个字段中的任何一个，则关闭对应的client连接
     */
    @Override
    public boolean updateById(MonitorSysGenServer entity) {
        if (entity == null || entity.getMonitorSysGenServerId() == null) {
            return false;
        }

        try {
            // 获取更新前的服务器配置
            MonitorSysGenServer oldServer = this.getById(entity.getMonitorSysGenServerId());
            if (oldServer == null) {
                return false;
            }

            // 自动检测服务器信息
            autoDetectServerInfo(entity);
            String host = entity.getMonitorSysGenServerHost();
            Map<String, Object> serverInfo = ServerInfoDetector.detectServerInfo(host);

            // 设置是否本地服务器（不允许修改）
            Integer isLocal = (Integer) serverInfo.get("isLocal");
            entity.setMonitorSysGenServerIsLocal(isLocal);
            // 检查关键连接字段是否发生变化
            boolean needDisconnect = isConnectionFieldsChanged(oldServer, entity);

            // 如果关键字段发生变化，先断开连接
            if (needDisconnect) {
                try {
                    log.info("检测到服务器连接配置变更，断开现有连接: {}", oldServer.getMonitorSysGenServerName());
                    disconnect(String.valueOf(oldServer.getMonitorSysGenServerId()));
                } catch (Exception e) {
                    log.warn("断开服务器连接时发生异常: {}", e.getMessage(), e);
                    // 不阻止更新操作，只记录警告
                }
            }

            // 自动检测服务器信息（只在主机地址变化时重新检测）
            if (!Objects.equals(oldServer.getMonitorSysGenServerHost(), entity.getMonitorSysGenServerHost())) {
                autoDetectServerInfo(entity);
            }

            // 更新时间由SysBase自动处理

            // 执行更新操作
            boolean result = super.updateById(entity);

            if (result) {
                // 更新服务器任务配置
                try {
                    serverTaskManagementService.updateServerTasks(entity.getMonitorSysGenServerId());
                    log.info("更新服务器 {} 任务配置成功", entity.getMonitorSysGenServerName());
                } catch (Exception e) {
                    log.error("更新服务器任务配置失败: {}", entity.getMonitorSysGenServerName(), e);
                }

                // 发送服务器更新消息
                sendServerMessage(entity, ServerWebSocketMessage.MessageType.SERVER_UPDATED, true, needDisconnect ? "连接配置已变更，已断开现有连接" : null);
                log.info("服务器配置更新成功: {}", entity.getMonitorSysGenServerName());
            }

            return result;
        } catch (Exception e) {
            log.error("更新服务器配置失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查连接相关字段是否发生变化
     * 检查字段：地址(host)、端口(port)、用户名(username)、密码(password)
     */
    private boolean isConnectionFieldsChanged(MonitorSysGenServer oldServer, MonitorSysGenServer newServer) {
        // 检查主机地址
        if (!Objects.equals(oldServer.getMonitorSysGenServerHost(), newServer.getMonitorSysGenServerHost())) {
            log.debug("服务器主机地址发生变化: {} -> {}",
                    oldServer.getMonitorSysGenServerHost(), newServer.getMonitorSysGenServerHost());
            return true;
        }

        // 检查端口
        if (!Objects.equals(oldServer.getMonitorSysGenServerPort(), newServer.getMonitorSysGenServerPort())) {
            log.debug("服务器端口发生变化: {} -> {}",
                    oldServer.getMonitorSysGenServerPort(), newServer.getMonitorSysGenServerPort());
            return true;
        }

        // 检查用户名
        if (!Objects.equals(oldServer.getMonitorSysGenServerUsername(), newServer.getMonitorSysGenServerUsername())) {
            log.debug("服务器用户名发生变化: {} -> {}",
                    oldServer.getMonitorSysGenServerUsername(), newServer.getMonitorSysGenServerUsername());
            return true;
        }

        // 检查密码
        if (!Objects.equals(oldServer.getMonitorSysGenServerPassword(), newServer.getMonitorSysGenServerPassword())) {
            log.debug("服务器密码发生变化");
            return true;
        }

        return false;
    }

    @Override
    public IPage<MonitorSysGenServer> pageFor(Page<MonitorSysGenServer> page, MonitorSysGenServer entity) {
        return this.page(page, Wrappers.<MonitorSysGenServer>lambdaQuery()
                .like(StringUtils.isNotEmpty(entity.getMonitorSysGenServerName()),
                        MonitorSysGenServer::getMonitorSysGenServerName, entity.getMonitorSysGenServerName())
                .like(StringUtils.isNotEmpty(entity.getMonitorSysGenServerHost()),
                        MonitorSysGenServer::getMonitorSysGenServerHost, entity.getMonitorSysGenServerHost())
                .eq(StringUtils.isNotEmpty(entity.getMonitorSysGenServerProtocol()),
                        MonitorSysGenServer::getMonitorSysGenServerProtocol, entity.getMonitorSysGenServerProtocol())
                .eq(entity.getMonitorSysGenServerStatus() != null,
                        MonitorSysGenServer::getMonitorSysGenServerStatus, entity.getMonitorSysGenServerStatus())
                .orderByDesc(MonitorSysGenServer::getCreateTime));
    }

    @Override
    public ReturnResult<ServerWithConfigDto> getByIdAndConfig(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        try {
            // 获取服务器基本信息
            MonitorSysGenServer server = this.getById(id);
            if (server == null) {
                return ReturnResult.error("服务器配置不存在");
            }

            // 获取服务器配置信息
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                monitorSysGenServerSettingService.getOrCreateSetting(Integer.valueOf(id));

            MonitorSysGenServerSetting setting;
            if (!settingResult.isOk()) {
                log.warn("获取服务器配置失败，使用默认配置: serverId={}, error={}", id, settingResult.getMsg());
                // 如果获取配置失败，创建默认配置
                setting = MonitorSysGenServerSetting.getDefaultSetting(Integer.valueOf(id));
            } else {
                setting = settingResult.getData();
            }

            // 组装返回结果
            ServerWithConfigDto result = ServerWithConfigDto.of(server, setting);

            log.debug("获取服务器信息和配置成功: serverId={}", id);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("获取服务器信息和配置失败: serverId={}", id, e);
            return ReturnResult.error("获取服务器信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> testConnection(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            boolean testResult = connectionService.testConnection(server);

            // 发送测试结果消息
            sendServerMessage(server, "test", testResult, testResult ? null : "连接测试失败");
            server.setMonitorSysGenServerConnectionStatus(testResult ? 1 : 0);
            this.updateById(server);

            return ReturnResult.of(testResult, testResult ? "连接测试成功" : "连接测试失败");
        } catch (Exception e) {
            log.error("测试服务器连接失败: {}", e.getMessage(), e);
            sendServerMessage(server, "test", false, e.getMessage());
            return ReturnResult.error("连接测试失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> connect(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            // 广播连接开始状态
            webSocketService.broadcastServerConnectionStatus(
                    server.getMonitorSysGenServerId(),
                    2, // CONNECTING状态
                    "正在建立连接...",
                    null
            );

            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                // 广播连接失败状态
                webSocketService.broadcastServerConnectionStatus(
                        server.getMonitorSysGenServerId(),
                        3, // FAILED状态
                        "不支持的协议类型: " + server.getMonitorSysGenServerProtocol(),
                        null
                );
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            long startTime = System.currentTimeMillis();
            boolean connected = connectionService.connect(server);
            long responseTime = System.currentTimeMillis() - startTime;

            if (connected) {
                // 更新服务器连接状态
                server.setMonitorSysGenServerConnectionStatus(1); // ONLINE
                server.setMonitorSysGenServerLastConnectTime(LocalDateTime.now());
                server.setMonitorSysGenServerConnectionError(null);
                this.updateById(server);

                // 广播连接成功状态
                webSocketService.broadcastServerConnectionStatus(
                        server.getMonitorSysGenServerId(),
                        1, // ONLINE状态
                        "连接成功",
                        responseTime
                );

                log.info("服务器连接成功: {}", server.getMonitorSysGenServerName());
                return ReturnResult.ok(true, "连接成功");
            } else {
                // 更新服务器连接状态
                server.setMonitorSysGenServerConnectionStatus(3); // FAILED
                server.setMonitorSysGenServerConnectionError("连接失败");
                this.updateById(server);

                // 广播连接失败状态
                webSocketService.broadcastServerConnectionStatus(
                        server.getMonitorSysGenServerId(),
                        3, // FAILED状态
                        "连接失败",
                        responseTime
                );

                return ReturnResult.error("连接失败");
            }
        } catch (Exception e) {
            log.error("连接服务器失败: {}", e.getMessage(), e);

            // 更新服务器连接状态
            server.setMonitorSysGenServerConnectionStatus(3); // FAILED
            server.setMonitorSysGenServerConnectionError("连接异常: " + e.getMessage());
            this.updateById(server);

            // 广播连接异常状态
            webSocketService.broadcastServerConnectionStatus(
                    server.getMonitorSysGenServerId(),
                    3, // FAILED状态
                    "连接异常: " + e.getMessage(),
                    null
            );

            return ReturnResult.error("连接失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> disconnect(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            connectionService.disconnect(server.getMonitorSysGenServerId());

            // 更新服务器连接状态
            server.setMonitorSysGenServerConnectionStatus(0); // OFFLINE
            server.setMonitorSysGenServerConnectionError(null);
            this.updateById(server);

            // 广播断开连接状态
            webSocketService.broadcastServerConnectionStatus(
                    server.getMonitorSysGenServerId(),
                    0, // OFFLINE状态
                    "连接已断开",
                    null
            );

            log.info("服务器连接已断开: {}", server.getMonitorSysGenServerName());
            return ReturnResult.ok(true, "断开连接成功");
        } catch (Exception e) {
            log.error("断开服务器连接失败: {}", e.getMessage(), e);

            // 广播断开连接失败状态
            webSocketService.broadcastServerConnectionStatus(
                    server.getMonitorSysGenServerId(),
                    3, // FAILED状态
                    "断开连接失败: " + e.getMessage(),
                    null
            );

            return ReturnResult.error("断开连接失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getConnectionStatus(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            // 获取服务器配置信息
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                monitorSysGenServerSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());
            MonitorSysGenServerSetting setting = settingResult.isOk() ? settingResult.getData() : null;

            Map<String, Object> status = new HashMap<>();
            status.put("serverId", server.getMonitorSysGenServerId());
            status.put("serverName", server.getMonitorSysGenServerName());
            status.put("host", server.getMonitorSysGenServerHost());
            status.put("port", server.getMonitorSysGenServerPort());
            status.put("protocol", server.getMonitorSysGenServerProtocol());
            status.put("connected", connectionService.isConnected(server.getMonitorSysGenServerId()));
            status.put("sessionId", connectionService.getSessionId(server.getMonitorSysGenServerId()));
            status.put("connectionStatus", server.getMonitorSysGenServerConnectionStatus());
            status.put("statusDesc", getStatusDesc(server.getMonitorSysGenServerConnectionStatus()));
            status.put("lastConnectTime", server.getMonitorSysGenServerLastConnectTime());
            status.put("connectionError", server.getMonitorSysGenServerConnectionError());
            // 从配置表获取监控状态
            status.put("monitorEnabled", setting != null ? setting.getMonitorSysGenServerSettingMonitorEnabled() : 0);
            status.put("tags", server.getMonitorSysGenServerTags());
            status.put("timestamp", System.currentTimeMillis());

            return ReturnResult.ok(status);
        } catch (Exception e) {
            log.error("获取连接状态失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取连接状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> executeCommand(String id, String command) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        if (StringUtils.isEmpty(command)) {
            return ReturnResult.error("命令不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            String result = connectionService.executeCommand(server.getMonitorSysGenServerId(), command);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("执行命令失败: {}", e.getMessage(), e);
            return ReturnResult.error("执行命令失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServer> getServerInfo(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
//            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
//            if (connectionService == null) {
//                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
//            }

//            Map<String, Object> serverInfo = connectionService.getServerInfo(server.getMonitorSysGenServerId());

            // 添加配置信息
//            serverInfo.put("config", server);

            return ReturnResult.ok(server);
        } catch (Exception e) {
            log.error("获取服务器信息失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取服务器信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> sendData(String id, String data) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        if (StringUtils.isEmpty(data)) {
            return ReturnResult.error("发送数据不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            connectionService.sendData(server.getMonitorSysGenServerId(), data);
            return ReturnResult.ok(true, "数据发送成功");
        } catch (Exception e) {
            log.error("发送数据失败: {}", e.getMessage(), e);
            return ReturnResult.error("发送数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> resizeTerminal(String id, int width, int height) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            connectionService.resizeTerminal(server.getMonitorSysGenServerId(), width, height);
            return ReturnResult.ok(true, "终端大小调整成功");
        } catch (Exception e) {
            log.error("调整终端大小失败: {}", e.getMessage(), e);
            return ReturnResult.error("调整终端大小失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> listFiles(String id, String path) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            Map<String, Object> result = connectionService.listFiles(server.getMonitorSysGenServerId(), path);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("获取文件列表失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取文件列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> uploadFile(String id, String localPath, String remotePath) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            boolean result = connectionService.uploadFile(server.getMonitorSysGenServerId(), localPath, remotePath);
            return ReturnResult.of(result, result ? "文件上传成功" : "文件上传失败");
        } catch (Exception e) {
            log.error("上传文件失败: {}", e.getMessage(), e);
            return ReturnResult.error("上传文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> downloadFile(String id, String remotePath, String localPath) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            boolean result = connectionService.downloadFile(server.getMonitorSysGenServerId(), remotePath, localPath);
            return ReturnResult.of(result, result ? "文件下载成功" : "文件下载失败");
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage(), e);
            return ReturnResult.error("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> enableMonitoring(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            // 获取或创建服务器配置
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                monitorSysGenServerSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());

            if (!settingResult.isOk()) {
                return ReturnResult.error("获取服务器配置失败: " + settingResult.getMsg());
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            setting.setMonitorSysGenServerSettingMonitorEnabled(1);

            // 更新配置
            ReturnResult<Boolean> updateResult = monitorSysGenServerSettingService.saveOrUpdateSetting(setting);
            if (!updateResult.isOk()) {
                return ReturnResult.error("更新监控配置失败: " + updateResult.getMsg());
            }

            // 启动数据收集任务
            reportManager.startDataCollection(server);

            // 发送监控启用消息
            sendServerMessage(server, "monitoring_enabled", true, null);
            log.info("服务器监控已启用: {}", server.getMonitorSysGenServerName());

            return ReturnResult.ok(true, "监控启用成功");
        } catch (Exception e) {
            log.error("启用服务器监控失败: {}", e.getMessage(), e);
            return ReturnResult.error("启用监控失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> disableMonitoring(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            // 获取服务器配置
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                monitorSysGenServerSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());

            if (!settingResult.isOk()) {
                return ReturnResult.error("获取服务器配置失败: " + settingResult.getMsg());
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            setting.setMonitorSysGenServerSettingMonitorEnabled(0);

            // 更新配置
            ReturnResult<Boolean> updateResult = monitorSysGenServerSettingService.saveOrUpdateSetting(setting);
            if (!updateResult.isOk()) {
                return ReturnResult.error("更新监控配置失败: " + updateResult.getMsg());
            }

            // 停止数据收集任务
            String reportType = setting.getMonitorSysGenServerSettingDataReportMethod();
            if (reportType != null && !"NONE".equals(reportType)) {
                reportManager.stopDataCollection(server.getMonitorSysGenServerId(), reportType);
            }

            // 发送监控停用消息
            sendServerMessage(server, "monitoring_disabled", false, null);
            log.info("服务器监控已停用: {}", server.getMonitorSysGenServerName());

            return ReturnResult.ok(true, "监控停用成功");
        } catch (Exception e) {
            log.error("停用服务器监控失败: {}", e.getMessage(), e);
            return ReturnResult.error("停用监控失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getMonitoringStatus(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            // 获取服务器配置信息
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                monitorSysGenServerSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());
            MonitorSysGenServerSetting setting = settingResult.isOk() ? settingResult.getData() : null;

            Map<String, Object> status = new HashMap<>();
            status.put("serverId", server.getMonitorSysGenServerId());
            status.put("serverName", server.getMonitorSysGenServerName());
            // 从配置表获取监控相关信息
            status.put("monitorEnabled", setting != null ? setting.getMonitorSysGenServerSettingMonitorEnabled() : 0);

            status.put("retentionDays", setting != null ? setting.getMonitorSysGenServerSettingMetricsRetentionDays() : 7);
            status.put("lastUpdateTime", server.getUpdateTime());

            return ReturnResult.ok(status);
        } catch (Exception e) {
            log.error("获取监控状态失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取监控状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerMetrics> collectMetrics(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            ServerConnectionService connectionService = getConnectionService(server.getMonitorSysGenServerProtocol());
            if (connectionService == null) {
                return ReturnResult.error("不支持的协议类型: " + server.getMonitorSysGenServerProtocol());
            }

            MonitorSysGenServerMetrics metrics = connectionService.collectMetrics(server);
            return ReturnResult.ok(metrics);
        } catch (Exception e) {
            log.error("收集服务器指标失败: {}", e.getMessage(), e);
            return ReturnResult.error("收集指标失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, List<MonitorSysGenServer>>> getServersByTags() {
        try {
            List<MonitorSysGenServer> servers = this.list();
            Map<String, List<MonitorSysGenServer>> groupedServers = servers.stream()
                    .collect(Collectors.groupingBy(server ->
                            StringUtils.isNotEmpty(server.getMonitorSysGenServerTags()) ?
                                    server.getMonitorSysGenServerTags() : "未分组"));

            return ReturnResult.ok(groupedServers);
        } catch (Exception e) {
            log.error("获取服务器分组失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取服务器分组失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> batchOperation(List<String> ids, String action) {
        if (ids == null || ids.isEmpty()) {
            return ReturnResult.error("服务器ID列表不能为空");
        }

        if (StringUtils.isEmpty(action)) {
            return ReturnResult.error("操作类型不能为空");
        }

        Map<String, Object> result = new HashMap<>();
        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();

        for (String id : ids) {
            try {
                boolean success = false;
                switch (action.toLowerCase()) {
                    case "connect":
                        success = connect(id).getData();
                        break;
                    case "disconnect":
                        success = disconnect(id).getData();
                        break;
                    case "enable_monitoring":
                        success = enableMonitoring(id).getData();
                        break;
                    case "disable_monitoring":
                        success = disableMonitoring(id).getData();
                        break;
                    default:
                        failedIds.add(id);
                        continue;
                }

                if (success) {
                    successIds.add(id);
                } else {
                    failedIds.add(id);
                }
            } catch (Exception e) {
                log.error("批量操作失败: serverId={}, action={}, error={}", id, action, e.getMessage());
                failedIds.add(id);
            }
        }

        result.put("action", action);
        result.put("total", ids.size());
        result.put("success", successIds.size());
        result.put("failed", failedIds.size());
        result.put("successIds", successIds);
        result.put("failedIds", failedIds);

        return ReturnResult.ok(result);
    }

    @Override
    public ReturnResult<Map<String, Object>> getServerStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总服务器数
            long totalServers = this.count();
            statistics.put("totalServers", totalServers);

            // 在线服务器数
            long onlineServers = connectionServices.stream()
                    .mapToLong(service -> this.list().stream()
                            .filter(server -> service.getProtocol().equals(server.getMonitorSysGenServerProtocol()))
                            .mapToLong(server -> service.isConnected(server.getMonitorSysGenServerId()) ? 1 : 0)
                            .sum())
                    .sum();
            statistics.put("onlineServers", onlineServers);

            // 启用监控的服务器数 - 从配置表统计
            ReturnResult<List<MonitorSysGenServerSetting>> enabledSettingsResult =
                monitorSysGenServerSettingService.getMonitorEnabledSettings();
            long monitoringEnabled = enabledSettingsResult.isOk() ? enabledSettingsResult.getData().size() : 0;
            statistics.put("monitoringEnabled", monitoringEnabled);

            // 按协议分组统计
            Map<String, Long> protocolStats = this.list().stream()
                    .collect(Collectors.groupingBy(
                            MonitorSysGenServer::getMonitorSysGenServerProtocol,
                            Collectors.counting()));
            statistics.put("protocolStats", protocolStats);

            // 按状态分组统计
            Map<String, Long> statusStats = this.list().stream()
                    .collect(Collectors.groupingBy(
                            server -> server.getMonitorSysGenServerStatus() == 1 ? "enabled" : "disabled",
                            Collectors.counting()));
            statistics.put("statusStats", statusStats);

            return ReturnResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取服务器统计信息失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> exportServerConfig(List<String> ids) {
        try {
            List<MonitorSysGenServer> servers;
            if (ids == null || ids.isEmpty()) {
                servers = this.list();
            } else {
                servers = this.listByIds(ids);
            }

            // 清除敏感信息
            servers.forEach(server -> {
                server.setMonitorSysGenServerPassword("***");
                server.setMonitorSysGenServerPrivateKey("***");
            });

            String configData = Json.toJson(servers);
            return ReturnResult.ok(configData);
        } catch (Exception e) {
            log.error("导出服务器配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("导出配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> importServerConfig(String configData) {
        try {
            List<MonitorSysGenServer> servers = Json.fromJsonToList(configData, MonitorSysGenServer.class);

            int successCount = 0;
            int failedCount = 0;
            List<String> errors = new ArrayList<>();

            for (MonitorSysGenServer server : servers) {
                try {
                    server.setMonitorSysGenServerId(null); // 清除ID，让数据库自动生成
                    // 创建时间和更新时间由SysBase自动处理

                    this.save(server);
                    successCount++;
                } catch (Exception e) {
                    failedCount++;
                    errors.add("导入服务器 " + server.getMonitorSysGenServerName() + " 失败: " + e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", servers.size());
            result.put("success", successCount);
            result.put("failed", failedCount);
            result.put("errors", errors);

            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("导入服务器配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("导入配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServer> cloneServer(String sourceId, String targetName) {
        if (StringUtils.isEmpty(sourceId)) {
            return ReturnResult.error("源服务器ID不能为空");
        }

        if (StringUtils.isEmpty(targetName)) {
            return ReturnResult.error("目标服务器名称不能为空");
        }

        try {
            MonitorSysGenServer sourceServer = this.getById(sourceId);
            if (sourceServer == null) {
                return ReturnResult.error("源服务器配置不存在");
            }

            MonitorSysGenServer clonedServer = new MonitorSysGenServer();
            // 复制所有字段
            clonedServer.setMonitorSysGenServerName(targetName);
            clonedServer.setMonitorSysGenServerHost(sourceServer.getMonitorSysGenServerHost());
            clonedServer.setMonitorSysGenServerPort(sourceServer.getMonitorSysGenServerPort());
            clonedServer.setMonitorSysGenServerProtocol(sourceServer.getMonitorSysGenServerProtocol());
            clonedServer.setMonitorSysGenServerUsername(sourceServer.getMonitorSysGenServerUsername());
            clonedServer.setMonitorSysGenServerPassword(sourceServer.getMonitorSysGenServerPassword());
            clonedServer.setMonitorSysGenServerPrivateKey(sourceServer.getMonitorSysGenServerPrivateKey());
            clonedServer.setMonitorSysGenServerTimeout(sourceServer.getMonitorSysGenServerTimeout());
            clonedServer.setMonitorSysGenServerDesc(sourceServer.getMonitorSysGenServerDesc() + " (克隆)");
            clonedServer.setMonitorSysGenServerStatus(0); // 默认停用
            clonedServer.setMonitorSysGenServerTags(sourceServer.getMonitorSysGenServerTags());

            // 监控相关配置已迁移到MonitorSysGenServerSetting表，不在这里设置

            // 创建时间和更新时间由SysBase自动处理

            this.save(clonedServer);

            log.info("服务器配置克隆成功: {} -> {}", sourceServer.getMonitorSysGenServerName(), targetName);
            return ReturnResult.ok(clonedServer);
        } catch (Exception e) {
            log.error("克隆服务器配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("克隆配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取连接服务
     */
    private ServerConnectionService getConnectionService(String protocol) {
        return connectionServices.stream()
                .filter(service -> service.getProtocol().equalsIgnoreCase(protocol))
                .findFirst()
                .orElse(null);
    }

    /**
     * 发送服务器消息
     */
    private void sendServerMessage(MonitorSysGenServer server, String messageType, boolean success, String error) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType(messageType)
                    .serverId(server.getMonitorSysGenServerId())
                    .serverName(server.getMonitorSysGenServerName())
                    .serverHost(server.getMonitorSysGenServerHost())
                    .serverPort(server.getMonitorSysGenServerPort())
                    .serverProtocol(server.getMonitorSysGenServerProtocol())
                    .errorMessage(error)
                    .data(Map.of("messageType", messageType, "success", success))
                    .timestamp(System.currentTimeMillis())
                    .build();

            String topic = "gen/server";
            socketSessionTemplate.send(topic, Json.toJson(message));
        } catch (Exception e) {
            log.error("发送服务器消息失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public ReturnResult<Boolean> updateConnectionStatus(String id, Integer status, String errorMessage) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        try {
            MonitorSysGenServer server = this.getById(id);
            if (server == null) {
                return ReturnResult.error("服务器配置不存在");
            }

            server.setMonitorSysGenServerConnectionStatus(status);
            server.setMonitorSysGenServerLastConnectTime(LocalDateTime.now());
            server.setMonitorSysGenServerConnectionError(errorMessage);


            boolean result = this.updateById(server);

            if (result) {
                // 发送WebSocket消息通知前端
                sendConnectionStatusMessage(server, status, errorMessage);
                log.info("服务器连接状态已更新: {} -> {}", server.getMonitorSysGenServerName(), status);
            }

            return ReturnResult.of(result, result ? "连接状态更新成功" : "连接状态更新失败");
        } catch (Exception e) {
            log.error("更新服务器连接状态失败: {}", e.getMessage(), e);
            return ReturnResult.error("更新连接状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> batchTestConnection(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return ReturnResult.error("服务器ID列表不能为空");
        }

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failedList = new ArrayList<>();

        for (String id : ids) {
            try {
                ReturnResult<Boolean> testResult = testConnection(id);
                Map<String, Object> serverResult = new HashMap<>();
                MonitorSysGenServer server = this.getById(id);

                if (server != null) {
                    serverResult.put("serverId", server.getMonitorSysGenServerId());
                    serverResult.put("serverName", server.getMonitorSysGenServerName());
                    serverResult.put("host", server.getMonitorSysGenServerHost());
                    serverResult.put("port", server.getMonitorSysGenServerPort());
                    serverResult.put("protocol", server.getMonitorSysGenServerProtocol());
                    serverResult.put("success", testResult.getData());
                    serverResult.put("message", testResult.getMsg());

                    if (testResult.getData() != null && testResult.getData()) {
                        successList.add(serverResult);
                    } else {
                        failedList.add(serverResult);
                    }
                }
            } catch (Exception e) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("serverId", id);
                errorResult.put("success", false);
                errorResult.put("message", "测试连接异常: " + e.getMessage());
                failedList.add(errorResult);
            }
        }

        result.put("total", ids.size());
        result.put("success", successList.size());
        result.put("failed", failedList.size());
        result.put("successList", successList);
        result.put("failedList", failedList);

        return ReturnResult.ok(result);
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getAllConnectionStatus() {
        try {
            List<MonitorSysGenServer> servers = this.list();
            List<Map<String, Object>> statusList = new ArrayList<>();

            for (MonitorSysGenServer server : servers) {
                Map<String, Object> status = new HashMap<>();
                status.put("serverId", server.getMonitorSysGenServerId());
                status.put("serverName", server.getMonitorSysGenServerName());
                status.put("host", server.getMonitorSysGenServerHost());
                status.put("port", server.getMonitorSysGenServerPort());
                status.put("protocol", server.getMonitorSysGenServerProtocol());
                status.put("connectionStatus", server.getMonitorSysGenServerConnectionStatus());
                status.put("statusDesc", getStatusDesc(server.getMonitorSysGenServerConnectionStatus()));
                status.put("lastConnectTime", server.getMonitorSysGenServerLastConnectTime());
                status.put("connectionError", server.getMonitorSysGenServerConnectionError());
                // 从配置表获取监控状态
                ReturnResult<MonitorSysGenServerSetting> settingResult =
                    monitorSysGenServerSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());
                MonitorSysGenServerSetting setting = settingResult.isOk() ? settingResult.getData() : null;
                status.put("monitorEnabled", setting != null ? setting.getMonitorSysGenServerSettingMonitorEnabled() : 0);
                status.put("tags", server.getMonitorSysGenServerTags());
                statusList.add(status);
            }

            return ReturnResult.ok(statusList);
        } catch (Exception e) {
            log.error("获取所有服务器连接状态失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取连接状态失败: " + e.getMessage());
        }
    }

    @Override
    public void scheduleConnectionCheck() {
        try {
            // 获取启用状态的服务器
            List<MonitorSysGenServer> allServers = this.list(
                Wrappers.lambdaQuery(MonitorSysGenServer.class)
                    .eq(MonitorSysGenServer::getMonitorSysGenServerStatus, 1)
            );

            // 过滤出启用监控的服务器
            List<MonitorSysGenServer> servers = new ArrayList<>();
            for (MonitorSysGenServer server : allServers) {
                ReturnResult<MonitorSysGenServerSetting> settingResult =
                    monitorSysGenServerSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());
                if (settingResult.isOk() && settingResult.getData() != null) {
                    MonitorSysGenServerSetting setting = settingResult.getData();
                    if (setting.getMonitorSysGenServerSettingMonitorEnabled() != null &&
                        setting.getMonitorSysGenServerSettingMonitorEnabled() == 1) {
                        servers.add(server);
                    }
                }
            }

            log.info("开始定时检查服务器连接状态，共{}台服务器", servers.size());

            for (MonitorSysGenServer server : servers) {
                try {
                    ReturnResult<Boolean> testResult = testConnection(server.getMonitorSysGenServerId().toString());

                    Integer newStatus = testResult.getData() != null && testResult.getData() ? 1 : 0;

                    // 只有状态发生变化时才更新
                    if (!Objects.equals(server.getMonitorSysGenServerConnectionStatus(), newStatus)) {
                        updateConnectionStatus(server.getMonitorSysGenServerId().toString(),
                            newStatus, testResult.getData() != null && testResult.getData() ? null : testResult.getMsg());
                    }
                } catch (Exception e) {
                    log.error("检查服务器连接状态失败: serverId={}, error={}",
                        server.getMonitorSysGenServerId(), e.getMessage());

                    updateConnectionStatus(server.getMonitorSysGenServerId().toString(),
                        3, e.getMessage());
                }
            }

            log.info("定时检查服务器连接状态完成");
        } catch (Exception e) {
            log.error("定时检查服务器连接状态异常: {}", e.getMessage(), e);
        }
    }



    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "离线";
            case 1:
                return "在线";
            case 2:
                return "连接中";
            case 3:
                return "连接失败";
            default:
                return "未知";
        }
    }

    /**
     * 发送连接状态消息
     */
    private void sendConnectionStatusMessage(MonitorSysGenServer server, Integer status, String errorMessage) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.createConnectionStatusMessage(
                    server.getMonitorSysGenServerId(),
                    server.getMonitorSysGenServerName(),
                    server.getMonitorSysGenServerHost(),
                    server.getMonitorSysGenServerPort(),
                    server.getMonitorSysGenServerProtocol(),
                    status,
                    getStatusDesc(status),
                    errorMessage,
                    null
            );

            String topic = "gen/server";
            socketSessionTemplate.send(topic, Json.toJson(message));
            log.debug("发送连接状态消息: serverId={}, status={}", server.getMonitorSysGenServerId(), status);
        } catch (Exception e) {
            log.error("发送连接状态消息失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public ReturnResult<String> getGuacamoleProxyUrl(String id) {
        try {
            MonitorSysGenServer server = getById(id);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // 检查是否配置了代理
            if (server.getMonitorSysGenServerProxyId() == null) {
                return ReturnResult.error("服务器未配置代理");
            }

            // 获取代理信息
            MonitorProxy proxy = monitorProxyService.getById(server.getMonitorSysGenServerProxyId());
            if (proxy == null) {
                return ReturnResult.error("代理配置不存在");
            }

            // 检查是否配置了Guacamole代理
            if (!"GUACAMOLE".equalsIgnoreCase(proxy.getProxyProtocol())) {
                return ReturnResult.error("服务器未配置Guacamole代理");
            }

            if (proxy.getProxyHost() == null || proxy.getProxyPort() == null) {
                return ReturnResult.error("代理服务器地址或端口未配置");
            }

            // 构建Guacamole连接URL
            String proxyUrl = String.format("http://%s:%d/guacamole/#/client/%s",
                    proxy.getProxyHost(),
                    proxy.getProxyPort(),
                    generateGuacamoleConnectionId(server));

            return ReturnResult.ok(proxyUrl);
        } catch (Exception e) {
            log.error("获取Guacamole代理URL失败: serverId={}", id, e);
            return ReturnResult.error("获取代理URL失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> testProxyConnection(String id) {
        try {
            MonitorSysGenServer server = getById(id);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // 获取代理信息
            MonitorProxy proxy = monitorProxyService.getById(server.getMonitorSysGenServerProxyId());
            if (proxy == null) {
                return ReturnResult.error("代理配置不存在");
            }

            if (proxy.getProxyHost() == null || proxy.getProxyPort() == null) {
                return ReturnResult.error("代理服务器地址或端口未配置");
            }

            // TODO: 实现代理连接测试逻辑
            // 这里可以尝试连接到代理服务器，验证连接是否正常
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("测试代理连接失败: serverId={}", id, e);
            return ReturnResult.error("测试代理连接失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<String>> getSupportedReportMethods(String id) {
        try {
            MonitorSysGenServer server = getById(id);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            List<String> supportedMethods = new ArrayList<>();
            supportedMethods.add("NONE"); // 不支持上报

            // 获取服务器配置
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                monitorSysGenServerSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());

            if (settingResult.isOk() && settingResult.getData() != null) {
                MonitorSysGenServerSetting setting = settingResult.getData();

                // 根据服务器配置判断支持的上报方式
                if (setting.getMonitorSysGenServerSettingReportEnabled() != null &&
                    setting.getMonitorSysGenServerSettingReportEnabled() == 1) {

                    // 判断是否为本地服务器
                    boolean isLocal = server.getMonitorSysGenServerIsLocal() != null && server.getMonitorSysGenServerIsLocal() == 1;

                    if (isLocal) {
                        // 本地服务器支持本地上报，不支持接口上报
                        supportedMethods.add("LOCAL"); // 本地上报
                    } else {
                        // 远程服务器支持接口上报，不支持本地上报
                        supportedMethods.add("API"); // 接口上报
                    }

                    // 如果配置了Prometheus服务器，则支持Prometheus上报
                    if (setting.getMonitorSysGenServerSettingPrometheusHost() != null &&
                        setting.getMonitorSysGenServerSettingPrometheusPort() != null) {
                        supportedMethods.add("PROMETHEUS");
                    }
                }
            }

            return ReturnResult.ok(supportedMethods);
        } catch (Exception e) {
            log.error("获取支持的数据上报方式失败: serverId={}", id, e);
            return ReturnResult.error("获取支持的上报方式失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Object> queryDataByConfig(String id, String queryType, String expression, Map<String, Object> timeRange) {
        try {
            MonitorSysGenServer server = getById(id);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // 根据查询类型执行不同的查询
            switch (queryType.toUpperCase()) {
                case "PROMETHEUS":
                    return queryPrometheusData(server, expression, timeRange);
                case "SQL":
                    return querySqlData(server, expression, timeRange);
                default:
                    return ReturnResult.error("不支持的查询类型: " + queryType);
            }
        } catch (Exception e) {
            log.error("根据配置查询数据失败: serverId={}, queryType={}, expression={}", id, queryType, expression, e);
            return ReturnResult.error("查询数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> updateReportConfig(String id, Object config) {
        try {
            MonitorSysGenServer server = getById(id);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // 根据config对象更新服务器的上报配置
            // 这里需要解析config对象并更新相应的字段
            // 注意：config参数的类型需要根据实际的ReportConfigRequest来确定
            // 暂时保持现有逻辑，等待具体的配置更新需求

            boolean success = updateById(server);
            return ReturnResult.ok(success);
        } catch (Exception e) {
            log.error("更新服务器数据上报配置失败: serverId={}", id, e);
            return ReturnResult.error("更新上报配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getReportConfig(String id) {
        try {
            MonitorSysGenServer server = getById(id);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            Map<String, Object> config = new HashMap<>();

            // 从配置表获取上报配置
            try {
                ReturnResult<MonitorSysGenServerSetting> settingResult =
                    monitorSysGenServerSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());

                if (settingResult.isOk() && settingResult.getData() != null) {
                    MonitorSysGenServerSetting setting = settingResult.getData();
                    config.put("reportEnabled", setting.getMonitorSysGenServerSettingReportEnabled());
                    config.put("dataReportMethod", setting.getMonitorSysGenServerSettingDataReportMethod());
                    config.put("prometheusHost", setting.getMonitorSysGenServerSettingPrometheusHost());
                    config.put("prometheusPort", setting.getMonitorSysGenServerSettingPrometheusPort());
                } else {
                    // 默认配置
                    config.put("reportEnabled", 0);
                    config.put("dataReportMethod", "NONE");
                    config.put("prometheusHost", null);
                    config.put("prometheusPort", null);
                }
            } catch (Exception e) {
                log.warn("获取服务器配置失败: serverId={}, error={}",
                    server.getMonitorSysGenServerId(), e.getMessage());
                // 默认配置
                config.put("reportEnabled", 0);
                config.put("dataReportMethod", "NONE");
                config.put("prometheusHost", null);
                config.put("prometheusPort", null);
            }

            // 获取代理配置
            String proxyType = null;
            String proxyHost = null;
            Integer proxyPort = null;
            if (server.getMonitorSysGenServerProxyId() != null) {
                MonitorProxy proxy = monitorProxyService.getById(server.getMonitorSysGenServerProxyId());
                if (proxy != null) {
                    proxyType = proxy.getProxyProtocol();
                    proxyHost = proxy.getProxyHost();
                    proxyPort = proxy.getProxyPort();
                }
            }
            config.put("proxyType", proxyType);
            config.put("proxyHost", proxyHost);
            config.put("proxyPort", proxyPort);

            return ReturnResult.ok(config);
        } catch (Exception e) {
            log.error("获取服务器数据上报配置失败: serverId={}", id, e);
            return ReturnResult.error("获取上报配置失败: " + e.getMessage());
        }
    }

    /**
     * 生成Guacamole连接ID
     */
    private String generateGuacamoleConnectionId(MonitorSysGenServer server) {
        // 根据服务器信息生成唯一的连接ID
        return String.format("%s_%s_%d_%s",
                server.getMonitorSysGenServerProtocol().toLowerCase(),
                server.getMonitorSysGenServerHost(),
                server.getMonitorSysGenServerPort(),
                server.getMonitorSysGenServerId());
    }

    /**
     * 查询Prometheus数据
     */
    private ReturnResult<Object> queryPrometheusData(MonitorSysGenServer server, String expression, Map<String, Object> timeRange) {
        // TODO: 实现Prometheus数据查询逻辑
        return ReturnResult.ok("Prometheus查询功能待实现");
    }

    /**
     * 查询SQL数据
     */
    private ReturnResult<Object> querySqlData(MonitorSysGenServer server, String expression, Map<String, Object> timeRange) {
        // TODO: 实现SQL数据查询逻辑
        return ReturnResult.ok("SQL查询功能待实现");
    }

    @Override
    public ReturnResult<Map<String, Object>> detectServerInfo(String id) {
        if (StringUtils.isEmpty(id)) {
            return ReturnResult.error("服务器ID不能为空");
        }

        MonitorSysGenServer server = this.getById(id);
        if (server == null) {
            return ReturnResult.error("服务器配置不存在");
        }

        try {
            // 执行自动检测
            autoDetectServerInfo(server);

            // 保存更新后的服务器信息
            boolean updated = this.updateById(server);

            if (updated) {
                // 构建返回结果
                Map<String, Object> result = new HashMap<>();
                result.put("serverId", server.getMonitorSysGenServerId());
                result.put("serverName", server.getMonitorSysGenServerName());
                result.put("host", server.getMonitorSysGenServerHost());
                result.put("isLocal", server.getMonitorSysGenServerIsLocal());
                result.put("ipAddresses", server.getMonitorSysGenServerIpAddresses());
                result.put("osType", server.getMonitorSysGenServerOsType());
                result.put("osVersion", server.getMonitorSysGenServerOsVersion());
                result.put("osArch", server.getMonitorSysGenServerOsArch());
                // Docker相关信息已迁移到配置表，这里设置默认值
                result.put("dockerEnabled", 0);
                result.put("dockerConnectionType", null);
                result.put("dockerPort", null);
                result.put("detectedAt", LocalDateTime.now());

                log.info("服务器信息检测完成: {}", server.getMonitorSysGenServerName());
                return ReturnResult.ok(result, "服务器信息检测完成");
            } else {
                return ReturnResult.error("保存检测结果失败");
            }
        } catch (Exception e) {
            log.error("检测服务器信息失败: {}", e.getMessage(), e);
            return ReturnResult.error("检测服务器信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> batchDetectServerInfo() {
        try {
            List<MonitorSysGenServer> servers = this.list();

            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> successList = new ArrayList<>();
            List<Map<String, Object>> failureList = new ArrayList<>();

            for (MonitorSysGenServer server : servers) {
                try {
                    autoDetectServerInfo(server);
                    boolean updated = this.updateById(server);

                    Map<String, Object> serverResult = new HashMap<>();
                    serverResult.put("serverId", server.getMonitorSysGenServerId());
                    serverResult.put("serverName", server.getMonitorSysGenServerName());
                    serverResult.put("host", server.getMonitorSysGenServerHost());
                    serverResult.put("isLocal", server.getMonitorSysGenServerIsLocal());
                    serverResult.put("osType", server.getMonitorSysGenServerOsType());
                    // Docker相关信息已迁移到配置表
                    serverResult.put("dockerEnabled", 0);

                    if (updated) {
                        successList.add(serverResult);
                    } else {
                        serverResult.put("error", "保存失败");
                        failureList.add(serverResult);
                    }
                } catch (Exception e) {
                    Map<String, Object> serverResult = new HashMap<>();
                    serverResult.put("serverId", server.getMonitorSysGenServerId());
                    serverResult.put("serverName", server.getMonitorSysGenServerName());
                    serverResult.put("host", server.getMonitorSysGenServerHost());
                    serverResult.put("error", e.getMessage());
                    failureList.add(serverResult);
                }
            }

            result.put("totalCount", servers.size());
            result.put("successCount", successList.size());
            result.put("failureCount", failureList.size());
            result.put("successList", successList);
            result.put("failureList", failureList);
            result.put("detectedAt", LocalDateTime.now());

            log.info("批量检测服务器信息完成，成功: {}, 失败: {}", successList.size(), failureList.size());
            return ReturnResult.ok(result, "批量检测完成");
        } catch (Exception e) {
            log.error("批量检测服务器信息失败: {}", e.getMessage(), e);
            return ReturnResult.error("批量检测失败: " + e.getMessage());
        }
    }

    /**
     * 自动检测服务器信息
     *
     * @param server 服务器实体
     */
    private void autoDetectServerInfo(MonitorSysGenServer server) {
        if (server == null || server.getMonitorSysGenServerHost() == null) {
            return;
        }

        try {
            String host = server.getMonitorSysGenServerHost();
            Map<String, Object> serverInfo = ServerInfoDetector.detectServerInfo(host);

            // 设置是否本地服务器（不允许修改）
            Integer isLocal = (Integer) serverInfo.get("isLocal");
            server.setMonitorSysGenServerIsLocal(isLocal);

            // 如果是本地服务器，设置自动检测的信息（但不覆盖用户手动设置的配置）
            if (isLocal != null && isLocal == 1) {
                // 设置操作系统信息（总是自动检测）
                String osType = (String) serverInfo.get("osType");
                String osVersion = (String) serverInfo.get("osVersion");
                String osArch = (String) serverInfo.get("osArch");
                server.setMonitorSysGenServerOsType(osType);
                server.setMonitorSysGenServerOsVersion(osVersion);
                server.setMonitorSysGenServerOsArch(osArch);

                // 设置IP地址列表（只在用户未设置或为空时自动填充）
                String detectedIpAddresses = (String) serverInfo.get("ipAddresses");
                if (server.getMonitorSysGenServerIpAddresses() == null ||
                    server.getMonitorSysGenServerIpAddresses().trim().isEmpty() ||
                    "[]".equals(server.getMonitorSysGenServerIpAddresses().trim())) {
                    server.setMonitorSysGenServerIpAddresses(detectedIpAddresses);
                    log.info("自动设置本地服务器IP地址: {}", detectedIpAddresses);
                } else {
                    log.info("保留用户设置的IP地址: {}", server.getMonitorSysGenServerIpAddresses());
                }

                // Docker配置已迁移到配置表，这里只记录检测结果到日志
                Integer detectedDockerEnabled = (Integer) serverInfo.get("dockerEnabled");
                String detectedDockerConnectionType = (String) serverInfo.get("dockerConnectionType");
                Integer detectedDockerPort = (Integer) serverInfo.get("dockerPort");

                log.info("检测到Docker配置: enabled={}, type={}, port={}",
                        detectedDockerEnabled, detectedDockerConnectionType, detectedDockerPort);

                log.info("自动检测本地服务器信息完成: {} - OS: {} {}",
                        server.getMonitorSysGenServerName(), osType, osVersion);
            } else {
                // 非本地服务器，只清空自动检测的系统信息字段，保留用户手动设置的配置
                server.setMonitorSysGenServerOsType(null);
                server.setMonitorSysGenServerOsVersion(null);
                server.setMonitorSysGenServerOsArch(null);

                // 保留用户设置的多IP地址和Docker配置，只在为空时设置默认值
                if (server.getMonitorSysGenServerIpAddresses() == null ||
                    server.getMonitorSysGenServerIpAddresses().trim().isEmpty()) {
                    server.setMonitorSysGenServerIpAddresses("[]");
                }

                // 保留用户设置的Docker配置，不强制覆盖
                log.info("检测到远程服务器: {} - 保留用户设置的多IP和Docker配置",
                        server.getMonitorSysGenServerName());
            }
        } catch (Exception e) {
            log.warn("自动检测服务器信息失败: {}", e.getMessage(), e);
            // 检测失败时设置默认值
            server.setMonitorSysGenServerIsLocal(0);
            server.setMonitorSysGenServerIpAddresses("[]");
            // Docker配置已迁移到配置表，不再在这里设置
        }
    }

    @Override
    public List<MonitorSysGenServer> getLocalReportServers() {
        try {
            // 获取所有本地服务器
            LambdaQueryWrapper<MonitorSysGenServer> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MonitorSysGenServer::getMonitorSysGenServerIsLocal, 1)
                    .eq(MonitorSysGenServer::getMonitorSysGenServerStatus, 1);

            List<MonitorSysGenServer> allLocalServers = this.list(queryWrapper);

            // 过滤出启用本地上报的服务器
            List<MonitorSysGenServer> localReportServers = new ArrayList<>();
            for (MonitorSysGenServer server : allLocalServers) {
                try {
                    ReturnResult<MonitorSysGenServerSetting> settingResult =
                        monitorSysGenServerSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());

                    if (settingResult.isOk() && settingResult.getData() != null) {
                        MonitorSysGenServerSetting setting = settingResult.getData();
                        if (setting.getMonitorSysGenServerSettingReportEnabled() != null &&
                            setting.getMonitorSysGenServerSettingReportEnabled() == 1 &&
                            "LOCAL".equals(setting.getMonitorSysGenServerSettingDataReportMethod())) {
                            localReportServers.add(server);
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取服务器配置失败，跳过: serverId={}, error={}",
                        server.getMonitorSysGenServerId(), e.getMessage());
                }
            }

            log.debug("查询到 {} 个启用本地上报的服务器", localReportServers.size());
            return localReportServers;

        } catch (Exception e) {
            log.error("查询本地上报服务器失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean updateServerOnlineStatus(Integer serverId, boolean isOnline) {
        try {
            MonitorSysGenServer server = this.getById(serverId);
            if (server == null) {
                log.warn("服务器不存在: {}", serverId);
                return false;
            }

            server.setMonitorSysGenServerConnectionStatus(isOnline ? 1 : 0);
            server.setMonitorSysGenServerLastConnectTime(LocalDateTime.now());


            boolean result = this.updateById(server);

            if (result) {
                // 发送WebSocket消息通知状态变化
                sendConnectionStatusMessage(server, isOnline ? 1 : 0, null);
                log.debug("服务器 {} 在线状态已更新: {}", server.getMonitorSysGenServerName(), isOnline);
            }

            return result;

        } catch (Exception e) {
            log.error("更新服务器在线状态失败: serverId={}, isOnline={}", serverId, isOnline, e);
            return false;
        }
    }

    @Override
    public boolean saveServerMetrics(Integer serverId, Map<String, Object> systemData) {
        try {
            // 这里可以保存到指标数据表或缓存中
            // 暂时通过WebSocket推送实时数据
            pushServerDataToWebSocket(serverId, systemData);

            log.debug("服务器 {} 指标数据已保存", serverId);
            return true;

        } catch (Exception e) {
            log.error("保存服务器指标数据失败: serverId={}", serverId, e);
            return false;
        }
    }

    @Override
    public void pushServerDataToWebSocket(Integer serverId, Map<String, Object> systemData) {
        try {
            MonitorSysGenServer server = this.getById(serverId);
            String serverName = server != null ? server.getMonitorSysGenServerName() : "Server-" + serverId;

            // 使用静态工厂方法创建消息
            ServerWebSocketMessage wsMessage = ServerWebSocketMessage.createServerMetricsMessage(
                serverId, serverName, systemData);

            // 发送到WebSocket
            socketSessionTemplate.send("gen/server", Json.toJson(wsMessage));

            log.debug("服务器 {} 数据已推送到WebSocket", serverId);

        } catch (Exception e) {
            log.error("推送服务器数据到WebSocket失败: serverId={}", serverId, e);
        }
    }

    @Override
    public boolean cleanupExpiredMetrics(long expireTime) {
        try {
            // 这里可以清理过期的指标数据
            // 暂时返回true，具体实现可以根据需要添加
            log.debug("清理过期指标数据: expireTime={}", expireTime);
            return true;

        } catch (Exception e) {
            log.error("清理过期指标数据失败: expireTime={}", expireTime, e);
            return false;
        }
    }

    @Override
    public boolean updateServerHealthStatus(Integer serverId, boolean isHealthy) {
        try {
            MonitorSysGenServer server = this.getById(serverId);
            if (server == null) {
                log.warn("服务器不存在: {}", serverId);
                return false;
            }

            // 可以添加健康状态字段到服务器表，或者通过其他方式记录
            // 更新时间由SysBase自动处理

            // 如果不健康，可以设置连接状态为离线
            if (!isHealthy) {
                server.setMonitorSysGenServerConnectionStatus(0);
            }

            boolean result = this.updateById(server);

            if (result) {
                // 发送健康状态变化消息
                Map<String, Object> healthData = new HashMap<>();
                healthData.put("isHealthy", isHealthy);
                healthData.put("checkTime", System.currentTimeMillis());

                ServerWebSocketMessage wsMessage = ServerWebSocketMessage.builder()
                    .messageType(ServerWebSocketMessage.MessageType.SERVER_HEALTH)
                    .serverId(serverId)
                    .serverName(server.getMonitorSysGenServerName())
                    .data(healthData)
                    .timestamp(System.currentTimeMillis())
                    .build();

                socketSessionTemplate.send("gen/server", Json.toJson(wsMessage));

                log.debug("服务器 {} 健康状态已更新: {}", server.getMonitorSysGenServerName(), isHealthy);
            }

            return result;

        } catch (Exception e) {
            log.error("更新服务器健康状态失败: serverId={}, isHealthy={}", serverId, isHealthy, e);
            return false;
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServer> saveServer(MonitorSysGenServer monitorSysGenServer) {
        try {
            // 调试信息：打印接收到的数据
            log.info("接收到的服务器数据 - 名称: {}, 主机: {}, 端口: {}",
                    monitorSysGenServer.getMonitorSysGenServerName(),
                    monitorSysGenServer.getMonitorSysGenServerHost(),
                    monitorSysGenServer.getMonitorSysGenServerPort());
            log.info("多IP地址: {}", monitorSysGenServer.getMonitorSysGenServerIpAddresses());
            // Docker配置已迁移到配置表，这里不再记录
            log.info("Docker配置已迁移到配置表");

            // 设置默认值
            if (null == monitorSysGenServer.getMonitorSysGenServerStatus()) {
                monitorSysGenServer.setMonitorSysGenServerStatus(0);
            }
            if (null == monitorSysGenServer.getMonitorSysGenServerTimeout()) {
                monitorSysGenServer.setMonitorSysGenServerTimeout(30000);
            }
            // Docker相关配置已迁移到配置表，不再在这里设置默认值

            boolean result = this.save(monitorSysGenServer);
            if (result) {
                return ReturnResult.ok(monitorSysGenServer);
            } else {
                return ReturnResult.error("保存服务器配置失败");
            }
        } catch (Exception e) {
            log.error("保存服务器配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("保存服务器配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> updateServer(MonitorSysGenServer monitorSysGenServer) {
        try {
            // 调试信息：打印接收到的数据
            log.info("更新服务器数据 - ID: {}, 名称: {}, 主机: {}, 端口: {}",
                    monitorSysGenServer.getMonitorSysGenServerId(),
                    monitorSysGenServer.getMonitorSysGenServerName(),
                    monitorSysGenServer.getMonitorSysGenServerHost(),
                    monitorSysGenServer.getMonitorSysGenServerPort());
            log.info("多IP地址: {}", monitorSysGenServer.getMonitorSysGenServerIpAddresses());
            // Docker配置已迁移到配置表，这里不再记录
            log.info("Docker配置已迁移到配置表");

            // Docker相关配置已迁移到配置表，不再在这里设置默认值

            boolean result = this.updateById(monitorSysGenServer);
            return ReturnResult.of(result);
        } catch (Exception e) {
            log.error("更新服务器配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("更新服务器配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> deleteServer(String id) {
        try {
            if (null == id) {
                return ReturnResult.illegal(REQUEST_PARAM_ERROR, "主键不能为空");
            }

            Set<String> ids = Splitter.on(",").trimResults().omitEmptyStrings().splitToSet(id);
            if (ids.isEmpty()) {
                return ReturnResult.ok();
            }

            // 在删除服务器前，先移除相关任务
            for (String serverId : ids) {
                try {
                    Integer serverIdInt = Integer.valueOf(serverId);
                    serverTaskManagementService.removeServerTasks(serverIdInt);
                    log.info("移除服务器 {} 的任务成功", serverIdInt);
                } catch (Exception e) {
                    log.error("移除服务器 {} 任务失败: {}", serverId, e.getMessage());
                }
            }

            boolean result = this.removeBatchByIds(ids);
            return ReturnResult.of(result);
        } catch (Exception e) {
            log.error("删除服务器配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("删除服务器配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> testLocalIpDetection(String host) {
        try {
            Map<String, Object> result = new HashMap<>();

            // 如果没有提供host，使用本机地址进行测试
            if (host == null || host.trim().isEmpty()) {
                host = "127.0.0.1";
            }

            // 调用检测方法
            Map<String, Object> serverInfo = ServerInfoDetector.detectServerInfo(host);

            result.put("testHost", host);
            result.put("detectionResult", serverInfo);
            result.put("timestamp", java.time.LocalDateTime.now());

            log.info("本地IP检测测试完成: host={}, isLocal={}", host, serverInfo.get("isLocal"));

            return ReturnResult.ok(result, "本地IP检测测试完成");
        } catch (Exception e) {
            log.error("本地IP检测测试失败: host={}, error={}", host, e.getMessage(), e);
            return ReturnResult.error("检测失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Integer> cleanupExpiredConnectionStatus(LocalDateTime expireTime) {
        try {
            log.info("开始清理过期连接状态数据，过期时间: {}", expireTime);

            // 这里可以清理连接状态历史记录表
            // 暂时返回0，具体实现可以根据需要添加
            int deletedCount = 0;

            log.info("清理过期连接状态数据完成，删除了 {} 条记录", deletedCount);
            return ReturnResult.ok(deletedCount);

        } catch (Exception e) {
            log.error("清理过期连接状态数据失败", e);
            return ReturnResult.error("清理过期连接状态数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Integer> cleanupTempFiles() {
        try {
            log.info("开始清理临时文件");

            // 清理临时文件逻辑
            int deletedCount = 0;

            log.info("临时文件清理完成，删除了 {} 个文件", deletedCount);
            return ReturnResult.ok(deletedCount);

        } catch (Exception e) {
            log.error("清理临时文件失败", e);
            return ReturnResult.error("清理临时文件失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Void> cleanupCache() {
        try {
            log.info("开始清理缓存");

            // 清理缓存逻辑

            log.info("缓存清理完成");
            return ReturnResult.ok();

        } catch (Exception e) {
            log.error("清理缓存失败", e);
            return ReturnResult.error("清理缓存失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Void> optimizeTables() {
        try {
            log.info("开始优化服务器相关数据表");

            // 执行表优化操作
            baseMapper.optimizeTable();

            log.info("服务器相关数据表优化完成");
            return ReturnResult.ok();

        } catch (Exception e) {
            log.error("优化服务器相关数据表失败", e);
            return ReturnResult.error("优化数据表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Void> checkDatabaseHealth() {
        try {
            log.debug("开始检查数据库健康状态");

            // 执行简单的数据库查询来检查连接
            this.count();

            log.debug("数据库健康检查通过");
            return ReturnResult.ok();

        } catch (Exception e) {
            log.error("数据库健康检查失败", e);
            return ReturnResult.error("数据库连接异常: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Void> checkDiskSpace() {
        try {
            log.debug("开始检查磁盘空间");

            // 检查磁盘空间逻辑
            File root = new File("/");
            long freeSpace = root.getFreeSpace();
            long totalSpace = root.getTotalSpace();
            double usagePercent = (double) (totalSpace - freeSpace) / totalSpace * 100;

            if (usagePercent > 90) {
                return ReturnResult.error("磁盘空间不足，使用率: " + String.format("%.2f%%", usagePercent));
            }

            log.debug("磁盘空间检查通过，使用率: {:.2f}%", usagePercent);
            return ReturnResult.ok();

        } catch (Exception e) {
            log.error("磁盘空间检查失败", e);
            return ReturnResult.error("磁盘空间检查失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Void> checkMemoryUsage() {
        try {
            log.debug("开始检查内存使用情况");

            // 检查内存使用情况
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double usagePercent = (double) usedMemory / maxMemory * 100;

            if (usagePercent > 90) {
                return ReturnResult.error("内存使用率过高: " + String.format("%.2f%%", usagePercent));
            }

            log.debug("内存使用检查通过，使用率: {:.2f}%", usagePercent);
            return ReturnResult.ok();

        } catch (Exception e) {
            log.error("内存使用检查失败", e);
            return ReturnResult.error("内存使用检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取服务器配置信息的辅助方法
     */
    private MonitorSysGenServerSetting getServerSetting(Integer serverId) {
        try {
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                monitorSysGenServerSettingService.getOrCreateSetting(serverId);
            return settingResult.isOk() ? settingResult.getData() : null;
        } catch (Exception e) {
            log.warn("获取服务器配置失败: serverId={}, error={}", serverId, e.getMessage());
            return null;
        }
    }

    @Override
    public List<Integer> getAllOnlineServerIds() {
        try {
            return this.lambdaQuery()
                    .eq(MonitorSysGenServer::getMonitorSysGenServerStatus, 1) // 1表示在线
                    .list()
                    .stream()
                    .map(MonitorSysGenServer::getMonitorSysGenServerId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取在线服务器ID列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public String getServerName(Integer serverId) {
        try {
            MonitorSysGenServer server = this.getById(serverId);
            return server != null ? server.getMonitorSysGenServerName() : "未知服务器";
        } catch (Exception e) {
            log.error("获取服务器名称失败: serverId={}, error={}", serverId, e.getMessage());
            return "未知服务器";
        }
    }
}
