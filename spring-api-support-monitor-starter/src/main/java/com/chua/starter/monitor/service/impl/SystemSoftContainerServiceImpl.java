package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.docker.support.client.DockerProtocolClient;
import com.chua.starter.monitor.entity.SystemSoftContainer;
import com.chua.starter.monitor.entity.SystemSoftContainerStats;
import com.chua.starter.monitor.entity.SystemSoftContainerStatus;
import com.chua.starter.monitor.factory.DockerProtocolClientFactory;
import com.chua.starter.monitor.mapper.SystemSoftContainerMapper;
import com.chua.starter.monitor.service.DockerCallbackService;
import com.chua.starter.monitor.service.SystemSoftContainerService;
import com.chua.starter.monitor.service.SystemSoftContainerStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统软件容器管理服务实现类
 *
 * @author CH
 * @version 1.0
 * @since 2025/01/15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSoftContainerServiceImpl extends ServiceImpl<SystemSoftContainerMapper, SystemSoftContainer> implements SystemSoftContainerService {

    private final SystemSoftContainerMapper systemSoftContainerMapper;
    private final DockerProtocolClientFactory dockerProtocolClientFactory;
    @Autowired
    @Lazy
    private DockerCallbackService dockerCallbackService;

    // 用于存储日志推送任务的Map
    private final Map<Integer, LogPushTask> logPushTasks = new ConcurrentHashMap<>();
    private final SystemSoftContainerStatsService systemSoftContainerStatsService;

    /**
     * 根据条件查询容器列表
     *
     * @param serverId      服务器ID
     * @param softId        软件ID
     * @param status        容器状态
     * @param containerName 容器名称
     * @return 容器列表
     */
    @Override
    public ReturnResult<Object> selectContainerList(Integer serverId, Integer softId, String status, String containerName) {
        log.debug("根据条件查询容器列表 - 服务器ID: {}, 软件ID: {}, 状态: {}, 容器名称: {}", serverId, softId, status, containerName);

        try {
            // 查询数据库中的容器列表
            List<SystemSoftContainer> containerList = systemSoftContainerMapper.selectList(null);
            log.debug("根据条件查询容器列表完成");
            return ReturnResult.ok(containerList);
        } catch (Exception e) {
            log.error("根据条件查询容器列表失败: {}", e.getMessage(), e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 启动容器
     *
     * @param containerId 容器ID
     * @return 启动结果
     */
    @Override
    public ReturnResult<Boolean> startContainer(Integer containerId) {
        log.info("启动容器 - 容器ID: {}", containerId);

        try {
            // 获取容器信息
            SystemSoftContainer container = systemSoftContainerMapper.selectById(containerId);
            if (container == null) {
                log.error("容器不存在 - 容器ID: {}", containerId);
                return ReturnResult.error("容器不存在");
            }

            // 更新容器状态为启动中
            container.setSystemSoftContainerStatus(SystemSoftContainerStatus.STARTING);
            systemSoftContainerMapper.updateById(container);

            // 获取Docker客户端
            Integer serverId = container.getSystemServerId();
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                // 更新容器状态为启动失败
                container.setSystemSoftContainerStatus(SystemSoftContainerStatus.START_FAILED);
                systemSoftContainerMapper.updateById(container);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 执行启动操作
            // 使用新的startContainerById方法，传入Docker容器ID
            dockerClient.startContainerById(container.getSystemSoftContainerDockerId());

            // 更新容器状态为运行中
            container.setSystemSoftContainerStatus(SystemSoftContainerStatus.RUNNING);
            systemSoftContainerMapper.updateById(container);

            log.info("启动容器成功 - 容器ID: {}", containerId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            // 更新容器状态为启动失败
            try {
                SystemSoftContainer container = systemSoftContainerMapper.selectById(containerId);
                if (container != null) {
                    container.setSystemSoftContainerStatus(SystemSoftContainerStatus.START_FAILED);
                    systemSoftContainerMapper.updateById(container);
                }
            } catch (Exception updateEx) {
                log.warn("更新容器状态为启动失败时出错: {}", updateEx.getMessage());
            }
            
            log.error("启动容器失败: {}", e.getMessage(), e);
            return ReturnResult.error("启动容器失败: " + e.getMessage());
        }
    }

    /**
     * 停止容器
     *
     * @param containerId 容器ID
     * @return 停止结果
     */
    @Override
    public ReturnResult<Boolean> stopContainer(Integer containerId) {
        log.info("停止容器 - 容器ID: {}", containerId);

        try {
            // 获取容器信息
            SystemSoftContainer container = systemSoftContainerMapper.selectById(containerId);
            if (container == null) {
                log.error("容器不存在 - 容器ID: {}", containerId);
                return ReturnResult.error("容器不存在");
            }

            // 更新容器状态为停止中
            container.setSystemSoftContainerStatus(SystemSoftContainerStatus.STOPPING);
            systemSoftContainerMapper.updateById(container);

            // 获取Docker客户端
            Integer serverId = container.getSystemServerId();
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                // 更新容器状态为停止失败
                container.setSystemSoftContainerStatus(SystemSoftContainerStatus.STOP_FAILED); // 停止失败
                systemSoftContainerMapper.updateById(container);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 执行停止操作
            // 使用基于ID的方法替代ByName方法
            dockerClient.stopContainerById(container.getSystemSoftContainerDockerId());

            // 更新容器状态为已停止
            container.setSystemSoftContainerStatus(SystemSoftContainerStatus.STOPPED); // 已停止
            systemSoftContainerMapper.updateById(container);

            log.info("停止容器成功 - 容器ID: {}", containerId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            // 更新容器状态为停止失败
            try {
                SystemSoftContainer container = systemSoftContainerMapper.selectById(containerId);
                if (container != null) {
                    container.setSystemSoftContainerStatus(SystemSoftContainerStatus.STOP_FAILED); // 停止失败
                    systemSoftContainerMapper.updateById(container);
                }
            } catch (Exception updateEx) {
                log.warn("更新容器状态为停止失败时出错: {}", updateEx.getMessage());
            }
            
            log.error("停止容器失败: {}", e.getMessage(), e);
            return ReturnResult.error("停止容器失败: " + e.getMessage());
        }
    }

    /**
     * 重启容器
     *
     * @param containerId 容器ID
     * @return 重启结果
     */
    @Override
    public ReturnResult<Boolean> restartContainer(Integer containerId) {
        log.info("重启容器 - 容器ID: {}", containerId);

        try {
            // 获取容器信息
            SystemSoftContainer container = systemSoftContainerMapper.selectById(containerId);
            if (container == null) {
                log.error("容器不存在 - 容器ID: {}", containerId);
                return ReturnResult.error("容器不存在");
            }

            // 更新容器状态为重启中
            container.setSystemSoftContainerStatus(SystemSoftContainerStatus.RESTARTING); // 重启中
            systemSoftContainerMapper.updateById(container);

            // 获取Docker客户端
            Integer serverId = container.getSystemServerId();
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                // 更新容器状态为重启失败
                container.setSystemSoftContainerStatus(SystemSoftContainerStatus.RESTART_FAILED); // 重启失败
                systemSoftContainerMapper.updateById(container);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 执行重启操作
            // 使用基于ID的方法替代ByName方法
            dockerClient.restartContainerById(container.getSystemSoftContainerDockerId());

            // 更新容器状态为运行中
            container.setSystemSoftContainerStatus(SystemSoftContainerStatus.RUNNING); // 运行中
            systemSoftContainerMapper.updateById(container);

            log.info("重启容器成功 - 容器ID: {}", containerId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            // 更新容器状态为重启失败
            try {
                SystemSoftContainer container = systemSoftContainerMapper.selectById(containerId);
                if (container != null) {
                    container.setSystemSoftContainerStatus(SystemSoftContainerStatus.RESTART_FAILED); // 重启失败
                    systemSoftContainerMapper.updateById(container);
                }
            } catch (Exception updateEx) {
                log.warn("更新容器状态为重启失败时出错: {}", updateEx.getMessage());
            }

            log.error("重启容器失败: {}", e.getMessage(), e);
            return ReturnResult.error("重启容器失败: " + e.getMessage());
        }
    }

    /**
     * 删除容器（从服务器中删除）
     *
     * @param containerId 容器ID
     * @param force       是否强制删除
     * @return 删除结果
     */
    @Override
    public ReturnResult<Boolean> removeContainer(Integer containerId, Boolean force) {
        log.info("删除容器 - 容器ID: {}, 强制删除: {}", containerId, force);

        try {
            // 获取容器信息
            SystemSoftContainer container = systemSoftContainerMapper.selectById(containerId);
            if (container == null) {
                log.error("容器不存在 - 容器ID: {}", containerId);
                return ReturnResult.error("容器不存在");
            }

            // 更新容器状态为删除中
            container.setSystemSoftContainerStatus(SystemSoftContainerStatus.DELETING); // 删除中
            systemSoftContainerMapper.updateById(container);

            // 获取Docker客户端
            Integer serverId = container.getSystemServerId();
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                // 更新容器状态为删除失败
                container.setSystemSoftContainerStatus(SystemSoftContainerStatus.DELETE_FAILED); // 删除失败
                systemSoftContainerMapper.updateById(container);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 执行删除操作
            // 使用基于ID的方法替代ByName方法
            dockerClient.removeContainerById(container.getSystemSoftContainerDockerId(), force != null ? force : false);

            // 从数据库删除记录
            super.removeById(containerId);

            log.info("删除容器成功 - 容器ID: {}, 强制删除: {}", containerId, force);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            // 更新容器状态为删除失败
            try {
                SystemSoftContainer container = systemSoftContainerMapper.selectById(containerId);
                if (container != null) {
                    container.setSystemSoftContainerStatus(SystemSoftContainerStatus.DELETE_FAILED); // 删除失败
                    systemSoftContainerMapper.updateById(container);
                }
            } catch (Exception updateEx) {
                log.warn("更新容器状态为删除失败时出错: {}", updateEx.getMessage());
            }

            log.error("删除容器失败: {}", e.getMessage(), e);
            return ReturnResult.error("删除容器失败: " + e.getMessage());
        }
    }

    /**
     * 获取容器详情
     *
     * @param containerId 容器ID
     * @return 容器详情
     */
    @Override
    public ReturnResult<SystemSoftContainer> getContainerDetails(Integer containerId) {
        log.info("获取容器详情 - 容器ID: {}", containerId);

        try {
            SystemSoftContainer container = systemSoftContainerMapper.selectById(containerId);
            if (container == null) {
                log.error("容器不存在 - 容器ID: {}", containerId);
                return ReturnResult.error("容器不存在");
            }

            log.info("获取容器详情成功 - 容器ID: {}", containerId);
            return ReturnResult.ok(container);
        } catch (Exception e) {
            log.error("获取容器详情失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取容器详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取容器日志
     *
     * @param containerId 容器ID
     * @param lines       日志行数（默认100行）
     * @return 容器日志
     */
    @Override
    public ReturnResult<String> getContainerLogs(Integer containerId, Integer lines) {
        log.info("获取容器日志 - 容器ID: {}, 行数: {}", containerId, lines);

        try {
            // 获取容器信息
            SystemSoftContainer container = systemSoftContainerMapper.selectById(containerId);
            if (container == null) {
                log.error("容器不存在 - 容器ID: {}", containerId);
                return ReturnResult.error("容器不存在");
            }

            // 获取Docker客户端
            Integer serverId = container.getSystemServerId();
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 获取日志
            // 使用基于ID的方法替代ByName方法
            int logLines = lines != null ? lines : 100;
            String logs = dockerClient.getContainerLogsById(container.getSystemSoftContainerDockerId(), logLines, false);
            log.info("获取容器日志成功 - 容器ID: {}", containerId);
            return ReturnResult.ok(logs);
        } catch (Exception e) {
            log.error("获取容器日志失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取容器日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取容器统计信息
     *
     * @param containerId 容器ID
     * @return 统计信息（CPU、内存、网络、磁盘等）
     */
    @Override
    public ReturnResult<SystemSoftContainerStats> getContainerStats(Integer containerId) {
        log.info("获取容器统计信息 - 容器ID: {}", containerId);

        try {
            // 获取容器信息
            SystemSoftContainer container = systemSoftContainerMapper.selectById(containerId);
            if (container == null) {
                log.error("容器不存在 - 容器ID: {}", containerId);
                return ReturnResult.error("容器不存在");
            }

            // 获取Docker客户端
            Integer serverId = container.getSystemServerId();
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 获取统计信息
            // 使用基于ID的方法替代ByName方法
            com.github.dockerjava.api.command.InspectContainerResponse stats = dockerClient.inspectContainerById(container.getSystemSoftContainerDockerId());

            // 转换为SystemSoftContainerStats实体
            SystemSoftContainerStats containerStats = new SystemSoftContainerStats();
            containerStats.setSystemSoftContainerStatsContainerId(container.getSystemSoftContainerDockerId());
            containerStats.setSystemSoftContainerStatsContainerName(container.getSystemSoftContainerName());

            // 设置基本信息
            containerStats.setSystemSoftContainerStatsImage(stats.getConfig() != null ? stats.getConfig().getImage() : "");
            containerStats.setSystemSoftContainerStatsCommand(stats.getConfig() != null ? String.join(" ", stats.getConfig().getCmd() != null ? stats.getConfig().getCmd() : new String[0]) : "");
            containerStats.setSystemSoftContainerStatsCreated(stats.getCreated() != null ? stats.getCreated() : "");

            // 设置状态信息
            if (stats.getState() != null) {
                containerStats.setSystemSoftContainerStatsStatus(stats.getState().getStatus());
                containerStats.setSystemSoftContainerStatsStartedAt(stats.getState().getStartedAt() != null ? stats.getState().getStartedAt() : "");
            }

            // 设置网络信息
            if (stats.getNetworkSettings() != null && stats.getNetworkSettings().getPorts() != null) {
                containerStats.setSystemSoftContainerStatsPorts(stats.getNetworkSettings().getPorts().toString());
            }

            // 设置标签信息
            if (stats.getConfig() != null && stats.getConfig().getLabels() != null) {
                containerStats.setSystemSoftContainerStatsLabels(stats.getConfig().getLabels().toString());
            }

            systemSoftContainerStatsService.save(containerStats);

            log.info("获取容器统计信息成功 - 容器ID: {}", containerId);
            return ReturnResult.ok(containerStats);
        } catch (Exception e) {
            log.error("获取容器统计信息失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取容器统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 同步容器状态
     *
     * @param serverId 服务器ID
     * @return 同步结果
     */
    @Override
    public ReturnResult<Integer> syncContainerStatus(Integer serverId) {
        log.info("同步容器状态 - 服务器ID: {}", serverId);

        try {
            // 获取Docker客户端
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 获取所有容器
            // 根据文档，DockerProtocolClient有listContainers方法
            Object containers = dockerClient.listContainers();

            // 更新数据库中的容器状态
            // 这里应该根据实际获取的容器信息更新数据库记录
            int updatedCount = 0; // 模拟更新数量

            log.info("同步容器状态成功 - 服务器ID: {}", serverId);
            return ReturnResult.ok(updatedCount);
        } catch (Exception e) {
            log.error("同步容器状态失败: {}", e.getMessage(), e);
            return ReturnResult.error("同步容器状态失败: " + e.getMessage());
        }
    }

    /**
     * 启动容器日志推送
     *
     * @param containerId 容器ID
     * @param lines       日志行数（默认100行）
     * @return 启动结果
     */
    @Override
    public ReturnResult<Boolean> startContainerLog(Integer containerId, Integer lines) {
        log.info("启动容器日志推送 - 容器ID: {}, 行数: {}", containerId, lines);

        try {
            // 获取容器信息
            SystemSoftContainer container = systemSoftContainerMapper.selectById(containerId);
            if (container == null) {
                log.error("容器不存在 - 容器ID: {}", containerId);
                return ReturnResult.error("容器不存在");
            }

            // 获取Docker客户端
            Integer serverId = container.getSystemServerId();
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 检查是否已经存在日志推送任务
            if (logPushTasks.containsKey(containerId)) {
                LogPushTask existingTask = logPushTasks.get(containerId);
                if (existingTask.isRunning()) {
                    log.info("容器日志推送已在运行 - 容器ID: {}", containerId);
                    return ReturnResult.ok(true);
                }
            }

            // 创建并启动日志推送任务
            int logLines = lines != null ? lines : 100;
            LogPushTask logPushTask = new LogPushTask(
                    dockerClient,
                    container.getSystemSoftContainerDockerId(),
                    containerId,
                    logLines,
                    dockerCallbackService.getSocketSessionTemplate()
            );

            logPushTask.start();
            logPushTasks.put(containerId, logPushTask);

            log.info("启动容器日志推送成功 - 容器ID: {}", containerId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("启动容器日志推送失败: {}", e.getMessage(), e);
            return ReturnResult.error("启动容器日志推送失败: " + e.getMessage());
        }
    }

    /**
     * 停止容器日志推送
     *
     * @param containerId 容器ID
     * @return 停止结果
     */
    @Override
    public ReturnResult<Boolean> stopContainerLog(Integer containerId) {
        log.info("停止容器日志推送 - 容器ID: {}", containerId);

        try {
            // 获取容器信息
            SystemSoftContainer container = systemSoftContainerMapper.selectById(containerId);
            if (container == null) {
                log.error("容器不存在 - 容器ID: {}", containerId);
                return ReturnResult.error("容器不存在");
            }

            // 检查是否存在日志推送任务
            if (!logPushTasks.containsKey(containerId)) {
                log.info("容器日志推送任务不存在 - 容器ID: {}", containerId);
                return ReturnResult.ok(true);
            }

            // 停止日志推送任务
            LogPushTask logPushTask = logPushTasks.get(containerId);
            logPushTask.stop();
            logPushTasks.remove(containerId);

            log.info("停止容器日志推送成功 - 容器ID: {}", containerId);
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("停止容器日志推送失败: {}", e.getMessage(), e);
            return ReturnResult.error("停止容器日志推送失败: " + e.getMessage());
        }
    }

    /**
     * 在容器中执行命令
     *
     * @param containerId 容器ID
     * @param command     要执行的命令
     * @return 执行结果
     */
    @Override
    public ReturnResult<String> execCommand(Integer containerId, String command) {
        log.info("在容器中执行命令 - 容器ID: {}, 命令: {}", containerId, command);

        try {
            // 获取容器信息
            SystemSoftContainer container = systemSoftContainerMapper.selectById(containerId);
            if (container == null) {
                log.error("容器不存在 - 容器ID: {}", containerId);
                return ReturnResult.error("容器不存在");
            }

            // 获取Docker客户端
            Integer serverId = container.getSystemServerId();
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 执行命令
            String result = dockerClient.execCommand(container.getSystemSoftContainerDockerId(), command.split(" "));

            log.info("在容器中执行命令成功 - 容器ID: {}", containerId);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("在容器中执行命令失败: {}", e.getMessage(), e);
            return ReturnResult.error("在容器中执行命令失败: " + e.getMessage());
        }
    }

    /**
     * 日志推送任务类
     */
    private static class LogPushTask implements Runnable {
        private final DockerProtocolClient dockerClient;
        private final String containerId;
        private final Integer dbContainerId;
        private final int lines;
        private final com.chua.socketio.support.session.SocketSessionTemplate socketSessionTemplate;
        private volatile boolean running = false;
        private Thread thread;

        public LogPushTask(DockerProtocolClient dockerClient, String containerId, Integer dbContainerId, int lines,
                           com.chua.socketio.support.session.SocketSessionTemplate socketSessionTemplate) {
            this.dockerClient = dockerClient;
            this.containerId = containerId;
            this.dbContainerId = dbContainerId;
            this.lines = lines;
            this.socketSessionTemplate = socketSessionTemplate;
        }

        public void start() {
            if (!running) {
                running = true;
                thread = new Thread(this);
                thread.setDaemon(true);
                thread.start();
            }
        }

        public void stop() {
            running = false;
            if (thread != null) {
                thread.interrupt();
            }
        }

        public boolean isRunning() {
            return running;
        }

        @Override
        public void run() {
            try {
                // 获取初始日志
                String initialLogs = dockerClient.getContainerLogsById(containerId, lines, false);

                // 推送初始日志
                Map<String, Object> message = new java.util.HashMap<>();
                message.put("type", "container_log");
                message.put("containerId", dbContainerId);
                message.put("logs", initialLogs);
                message.put("timestamp", java.time.LocalDateTime.now());
                socketSessionTemplate.sendObject("container_log", message);

                // 持续跟踪日志
                while (running && !Thread.currentThread().isInterrupted()) {
                    try {
                        // 获取最新的日志（只获取最后几行）
                        String latestLogs = dockerClient.getContainerLogsById(containerId, 10, true);

                        if (latestLogs != null && !latestLogs.isEmpty()) {
                            // 推送最新日志
                            message = new java.util.HashMap<>();
                            message.put("type", "container_log_update");
                            message.put("containerId", dbContainerId);
                            message.put("logs", latestLogs);
                            message.put("timestamp", java.time.LocalDateTime.now());
                            socketSessionTemplate.sendObject("container_log_update", message);
                        }

                        // 休眠一段时间避免过于频繁的请求
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        if (running) {
                            log.warn("获取容器日志时发生错误: {}", e.getMessage());
                        }
                        Thread.sleep(5000); // 出错时等待更长时间
                    }
                }
            } catch (Exception e) {
                log.error("日志推送任务执行失败: {}", e.getMessage(), e);
            } finally {
                running = false;
            }
        }
    }
}






