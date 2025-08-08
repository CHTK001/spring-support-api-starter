package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.discovery.ServiceDiscovery;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.protocol.ServerSetting;
import com.chua.common.support.protocol.filter.ServiceDiscoveryServletFilter;
import com.chua.common.support.protocol.server.ProtocolServer;
import com.chua.common.support.protocol.server.ServletFilter;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.spi.SpiOption;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.monitor.entity.SystemServer;
import com.chua.starter.monitor.entity.SystemServerSetting;
import com.chua.starter.monitor.mapper.SystemServerMapper;
import com.chua.starter.monitor.service.SystemServerService;
import com.chua.starter.monitor.service.SystemServerSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统服务器配置服务实现类
 *
 * @author CH
 * @since 2025/01/07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemServerServiceImpl extends ServiceImpl<SystemServerMapper, SystemServer> implements SystemServerService {

    /**
     * 存储已启动的服务器实例
     * Key: 服务器ID, Value: 服务器实例对象
     */
    private final Map<Integer, ProtocolServer> runningServerInstances = new ConcurrentHashMap<>();
    @Autowired
    @Lazy
    private SystemServerSettingService systemServerSettingService;

    @Override
    public IPage<SystemServer> pageFor(Page<SystemServer> page, SystemServer entity) {
        return baseMapper.pageFor(page, entity);
    }

    @Override
    public ReturnResult<List<SpiOption>> getAvailableServerTypes() {
        try {
            // 通过SPI机制获取可用的服务器类型
            ServiceProvider<ProtocolServer> serviceProvider = ServiceProvider.of(ProtocolServer.class);
            List<SpiOption> options = serviceProvider.options();
            log.info("获取可用服务器类型成功，共{}种类型", options.size());
            return ReturnResult.ok(options);
        } catch (Exception e) {
            log.error("获取可用服务器类型失败", e);
            return ReturnResult.error("获取服务器类型失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = baseMapper.getStatistics();
            log.info("获取服务器统计信息成功: {}", statistics);
            return ReturnResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取服务器统计信息失败", e);
            return ReturnResult.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> startServer(Integer serverId) {
        try {
            SystemServer server = getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            if (SystemServer.SystemServerStatus.RUNNING.equals(server.getSystemServerStatus())) {
                return ReturnResult.error("服务器已在运行中");
            }

            // 检查端口是否被占用
            if (isPortInUse(server.getSystemServerPort())) {
                return ReturnResult.error("端口 " + server.getSystemServerPort() + " 已被占用");
            }

            // 更新状态为启动中
            server.setSystemServerStatus(SystemServer.SystemServerStatus.STARTING);
            updateById(server);

            try {
                // 通过SPI机制创建服务器实例
                ProtocolServer serverInstance = createServerInstance(server);

                // 应用配置
                applyServerConfiguration(serverId, serverInstance);

                // 启动服务器
                startServerInstance(serverInstance);

                // 存储服务器实例
                runningServerInstances.put(serverId, serverInstance);

                // 更新状态为运行中
                server.setSystemServerStatus(SystemServer.SystemServerStatus.RUNNING);
                updateById(server);

                log.info("服务器启动成功: {} (端口: {})", server.getSystemServerName(), server.getSystemServerPort());
                return ReturnResult.ok(true);

            } catch (Exception e) {
                // 启动失败，更新状态为错误
                server.setSystemServerStatus(SystemServer.SystemServerStatus.ERROR);
                updateById(server);
                throw e;
            }

        } catch (Exception e) {
            log.error("启动服务器失败: serverId={}", serverId, e);
            return ReturnResult.error("启动服务器失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> stopServer(Integer serverId) {
        try {
            SystemServer server = getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            if (SystemServer.SystemServerStatus.STOPPED.equals(server.getSystemServerStatus())) {
                return ReturnResult.error("服务器已停止");
            }

            // 更新状态为停止中
            server.setSystemServerStatus(SystemServer.SystemServerStatus.STOPPING);
            updateById(server);

            try {
                // 获取服务器实例
                Object serverInstance = runningServerInstances.get(serverId);
                if (serverInstance != null) {
                    // 停止服务器
                    stopServerInstance(serverInstance);

                    // 移除服务器实例
                    runningServerInstances.remove(serverId);
                }

                // 更新状态为已停止
                server.setSystemServerStatus(SystemServer.SystemServerStatus.STOPPED);
                updateById(server);

                log.info("服务器停止成功: {}", server.getSystemServerName());
                return ReturnResult.ok(true);

            } catch (Exception e) {
                // 停止失败，更新状态为错误
                server.setSystemServerStatus(SystemServer.SystemServerStatus.ERROR);
                updateById(server);
                throw e;
            }

        } catch (Exception e) {
            log.error("停止服务器失败: serverId={}", serverId, e);
            return ReturnResult.error("停止服务器失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> getServerStatus(Integer serverId) {
        try {
            SystemServer server = getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            String status = server.getSystemServerStatus().getCode();
            return ReturnResult.ok(status);
        } catch (Exception e) {
            log.error("获取服务器状态失败: serverId={}", serverId, e);
            return ReturnResult.error("获取服务器状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> restartServer(Integer serverId) {
        try {
            // 先停止服务器
            ReturnResult<Boolean> stopResult = stopServer(serverId);
            if (!stopResult.isOk()) {
                return stopResult;
            }

            // 等待一段时间确保完全停止
            Thread.sleep(1000);

            // 再启动服务器
            return startServer(serverId);
        } catch (Exception e) {
            log.error("重启服务器失败: serverId={}", serverId, e);
            return ReturnResult.error("重启服务器失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> checkPortAvailable(Integer port, Integer serverId) {
        try {
            SystemServer existingServer = baseMapper.selectByPort(port);
            if (existingServer != null && !existingServer.getSystemServerId().equals(serverId)) {
                return ReturnResult.ok(false);
            }

            // 检查端口是否被其他进程占用
            boolean inUse = isPortInUse(port);
            return ReturnResult.ok(!inUse);
        } catch (Exception e) {
            log.error("检查端口可用性失败: port={}", port, e);
            return ReturnResult.error("检查端口失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<SystemServer> cloneServer(Integer sourceServerId, String newServerName, Integer newPort) {
        try {
            SystemServer sourceServer = getById(sourceServerId);
            if (sourceServer == null) {
                return ReturnResult.error("源服务器不存在");
            }

            // 检查新端口是否可用
            ReturnResult<Boolean> portCheck = checkPortAvailable(newPort, null);
            if (!portCheck.isOk() || !portCheck.getData()) {
                return ReturnResult.error("端口 " + newPort + " 不可用");
            }

            // 创建新服务器
            SystemServer newServer = new SystemServer();
            BeanUtils.copyProperties(sourceServer, newServer);
            newServer.setSystemServerId(null);
            newServer.setSystemServerName(newServerName);
            newServer.setSystemServerPort(newPort);
            newServer.setSystemServerStatus(SystemServer.SystemServerStatus.STOPPED);

            save(newServer);

            // 克隆配置
            systemServerSettingService.cloneServerSettings(sourceServerId, newServer.getSystemServerId());

            log.info("克隆服务器成功: {} -> {}", sourceServer.getSystemServerName(), newServerName);
            return ReturnResult.ok(newServer);
        } catch (Exception e) {
            log.error("克隆服务器失败: sourceServerId={}", sourceServerId, e);
            return ReturnResult.error("克隆服务器失败: " + e.getMessage());
        }
    }

    @Override
    public Object getRunningServerInstance(Integer serverId) {
        return runningServerInstances.get(serverId);
    }

    @Override
    public Map<Integer, Object> getAllRunningServerInstances() {
        return new HashMap<>(runningServerInstances);
    }

    @Override
    public ReturnResult<Boolean> applyConfigChanges(Integer serverId) {
        try {
            ProtocolServer serverInstance = runningServerInstances.get(serverId);
            if (serverInstance == null) {
                return ReturnResult.error("服务器未运行，无需应用配置");
            }

            // 重新应用配置
            applyServerConfiguration(serverId, serverInstance);

            log.info("应用配置更改成功: serverId={}", serverId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("应用配置更改失败: serverId={}", serverId, e);
            return ReturnResult.error("应用配置失败: " + e.getMessage());
        }
    }

    /**
     * 通过SPI机制创建服务器实例
     */
    private ProtocolServer createServerInstance(SystemServer server) throws Exception {
        ServiceProvider<ProtocolServer> serviceProvider = ServiceProvider.of(ProtocolServer.class);
        ServerSetting.ServerSettingBuilder builder = ServerSetting.builder()
                .serverId(String.valueOf(server.getSystemServerId()))
                .serverName(server.getSystemServerName())
                .host(server.getSystemServerHost())
                .port(server.getSystemServerPort());
        if(null != server.getSystemServerMaxConnections()) {
            builder.maxConnections(server.getSystemServerMaxConnections());
        }

        if(null != server.getSystemServerTimeout()) {
            builder.connectionTimeoutMillis(server.getSystemServerTimeout());
        }
        ServerSetting serverSetting = builder.build();
        return serviceProvider.getNewExtension(server.getSystemServerType(), serverSetting);
    }

    /**
     * 应用服务器配置
     */
    private void applyServerConfiguration(Integer serverId, ProtocolServer serverInstance) throws Exception {
        // 获取服务器配置
        ReturnResult<List<SystemServerSetting>> settingsResult = systemServerSettingService.getByServerIdAndEnabled(serverId, true);
        if (settingsResult.isOk() && settingsResult.getData() != null) {
            for (SystemServerSetting setting : settingsResult.getData()) {
                // 通过反射或其他方式应用配置到服务器实例
                applySettingToServerInstance(serverInstance, setting);
            }
        }
    }

    /**
     * 应用单个配置到服务器实例
     */
    private void applySettingToServerInstance(ProtocolServer serverInstance, SystemServerSetting setting) throws Exception {
        if("serviceDiscovery".equalsIgnoreCase(setting.getSystemServerSettingType())) {
            ServletFilter servletFilter = new ServiceDiscoveryServletFilter(
                    SpringBeanUtils.getBean(ServiceDiscovery.class)
            );
            serverInstance.addFilter(servletFilter);
        } else {
            ServletFilter servletFilter = ServiceProvider.of(ServletFilter.class).getNewExtension(setting.getSystemServerSettingType());
            serverInstance.addFilter(servletFilter);
        }
        log.debug("应用配置到服务器实例: settingType={}, settingName={}",
                setting.getSystemServerSettingType(), setting.getSystemServerSettingName());
    }

    /**
     * 启动服务器实例
     */
    private void startServerInstance(Object serverInstance) throws Exception {
        // 通过反射调用启动方法
        serverInstance.getClass().getMethod("start").invoke(serverInstance);
    }

    /**
     * 停止服务器实例
     */
    private void stopServerInstance(Object serverInstance) throws Exception {
        // 通过反射调用停止方法
        serverInstance.getClass().getMethod("stop").invoke(serverInstance);
    }

    /**
     * 检查端口是否被占用
     */
    private boolean isPortInUse(Integer port) {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(port)) {
            return false;
        } catch (java.io.IOException e) {
            return true;
        }
    }
}
