package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.docker.support.client.DockerProtocolClient;
import com.chua.docker.support.docker.DockerContainerConfig;
import com.chua.starter.monitor.constant.SystemSoftRecordStatus;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.entity.SystemSoftContainer;
import com.chua.starter.monitor.entity.SystemSoftRecord;
import com.chua.starter.monitor.factory.DockerProtocolClientFactory;
import com.chua.starter.monitor.mapper.SystemSoftContainerMapper;
import com.chua.starter.monitor.mapper.SystemSoftMapper;
import com.chua.starter.monitor.mapper.SystemSoftRecordMapper;
import com.chua.starter.monitor.service.SystemSoftContainerService;
import com.chua.starter.monitor.service.SystemSoftContainerWebSocketService;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Statistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final SystemSoftMapper systemSoftMapper;
    private final SystemSoftRecordMapper systemSoftRecordMapper;
    private final DockerProtocolClientFactory dockerProtocolClientFactory;

    @Lazy
    @Autowired(required = false)
    private SystemSoftContainerWebSocketService webSocketService;

    @Override
    public IPage<SystemSoftContainer> getContainerPage(Long current, Long size, Integer serverId, String status, String keyword) {
        log.debug("分页查询容器列表 - 当前页: {}, 每页大小: {}, 服务器ID: {}, 状态: {}, 关键词: {}",
                current, size, serverId, status, keyword);

        Page<SystemSoftContainer> page = new Page<>(current, size);
        LambdaQueryWrapper<SystemSoftContainer> wrapper = new LambdaQueryWrapper<>();

        // 服务器ID条件
        if (serverId != null) {
            wrapper.eq(SystemSoftContainer::getSystemServerId, serverId);
        }

        // 状态条件
        if (StringUtils.hasText(status)) {
            wrapper.eq(SystemSoftContainer::getSystemSoftContainerStatus, status);
        }

        // 关键词搜索（容器名称、镜像名称）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SystemSoftContainer::getSystemSoftContainerName, keyword)
                    .or().like(SystemSoftContainer::getSystemSoftContainerImage, keyword));
        }

        wrapper.orderByDesc(SystemSoftContainer::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    public List<SystemSoftContainer> getContainersBySoftId(Integer softId) {
        log.debug("根据软件ID查询容器列表 - 软件ID: {}", softId);
        return systemSoftContainerMapper.selectBySoftId(softId);
    }

    @Override
    public List<SystemSoftContainer> getContainersByServerId(Integer serverId) {
        log.debug("根据服务器ID查询容器列表 - 服务器ID: {}", serverId);
        return systemSoftContainerMapper.selectByServerId(serverId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createContainer(Integer softId, Integer serverId, String containerName, String imageTag,
                                   String ports, String envVars, String volumes, String command, String args) {
        log.info("开始创建容器 - 软件ID: {}, 服务器ID: {}, 容器名称: {}, 镜像标签: {}",
                softId, serverId, containerName, imageTag);

        try {
            // 检查容器名称是否已存在
            if (checkContainerNameExists(containerName, serverId, null)) {
                log.warn("容器名称已存在: {}", containerName);
                return false;
            }

            // 获取软件信息
            SystemSoft systemSoft = systemSoftMapper.selectById(softId);
            if (systemSoft == null) {
                log.warn("软件不存在 - ID: {}", softId);
                return false;
            }

            // 创建操作记录
            SystemSoftRecord record = new SystemSoftRecord();
            record.setSystemSoftId(softId);
            record.setSystemServerId(serverId);
            record.setSystemSoftRecordOperationType("CREATE_CONTAINER");
            record.setSystemSoftRecordRemark("创建容器: " + containerName);
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.IN_PROGRESS);
            record.setSystemSoftRecordStartTime(LocalDateTime.now());
            systemSoftRecordMapper.insert(record);

            String dockerContainerId = null;

            // 获取Docker客户端
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);

            // 调用Docker API创建容器
            try {
                String imageName = systemSoft.getSystemSoftDockerImage();
                String fullImageName = imageName + ":" + imageTag;

                log.info("开始创建Docker容器: {}, 镜像: {}", containerName, fullImageName);

                // 解析端口映射
                Map<String, String> portBindings = parsePortMappings(ports);

                // 解析环境变量
                List<String> envList = parseEnvironmentVariables(envVars);

                // 解析数据卷
                Map<String, String> volumeBindings = parseVolumeMappings(volumes);

                // 创建Docker容器配置对象
                DockerContainerConfig config = DockerContainerConfig.builder()
                        .imageName(fullImageName)
                        .containerName(containerName)
                        .portBindings(portBindings)
                        .environmentVariables(envList)
                        .volumeBindings(volumeBindings)
                        .command(command)
                        .args(args)
                        .autoRemove(false)
                        .restartPolicy("no")
                        .build();

                // 创建容器
                if (dockerClient != null) {
                    dockerContainerId = dockerClient.createContainer(config);
                } else {
                    log.error("无法创建Docker客户端，serverId: {}", serverId);
                    record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                    record.setSystemSoftRecordEndTime(LocalDateTime.now());
                    record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: 无法创建Docker客户端");
                    systemSoftRecordMapper.updateById(record);
                    return false;
                }

                if (StringUtils.hasText(dockerContainerId)) {
                    log.info("Docker容器创建成功 - 容器ID: {}", dockerContainerId);
                } else {
                    log.error("Docker容器创建失败 - 返回的容器ID为空");
                    // 更新记录状态为失败
                    record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                    record.setSystemSoftRecordEndTime(LocalDateTime.now());
                    record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: Docker容器创建失败");
                    systemSoftRecordMapper.updateById(record);
                    return false;
                }

            } catch (Exception e) {
                log.error("Docker容器创建失败: {}", e.getMessage(), e);
                // 更新记录状态为失败
                record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                record.setSystemSoftRecordEndTime(LocalDateTime.now());
                record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: " + e.getMessage());
                systemSoftRecordMapper.updateById(record);
                return false;
            }

            // 创建容器记录
            SystemSoftContainer container = new SystemSoftContainer();
            container.setSystemSoftId(softId);
            container.setSystemServerId(serverId);
            container.setSystemSoftContainerDockerId(dockerContainerId);
            container.setSystemSoftContainerName(containerName);
            container.setSystemSoftContainerImage(systemSoft.getSystemSoftDockerImage());
            container.setSystemSoftContainerImageTag(imageTag);
            container.setSystemSoftContainerStatus("CREATED");
            container.setSystemSoftContainerPorts(ports);
            container.setSystemSoftContainerEnv(envVars);
            container.setSystemSoftContainerVolumes(volumes);
            container.setSystemSoftContainerCommand(command);
            container.setSystemSoftContainerArgs(args);
            container.setSystemSoftContainerCreatedTime(LocalDateTime.now());
            container.setSystemSoftContainerRestartCount(0);
            container.setSystemSoftContainerAutoRestart(0);
            container.setCreateTime(LocalDateTime.now());
            container.setUpdateTime(LocalDateTime.now());

            this.save(container);

            // 推送容器创建事件
            if (webSocketService != null) {
                webSocketService.pushContainerEvent(container.getSystemSoftContainerId(), "create", "容器创建成功", true);
                webSocketService.pushContainerStatus(container.getSystemSoftContainerId(), "CREATED");
            }

            // 更新操作记录为成功
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.SUCCESS);
            record.setSystemSoftRecordEndTime(LocalDateTime.now());
            systemSoftRecordMapper.updateById(record);

            log.info("容器创建成功 - 容器名称: {}, Docker ID: {}", containerName, dockerContainerId);
            return true;

        } catch (Exception e) {
            log.error("创建容器失败: {}", e.getMessage(), e);
            // 推送容器创建失败事件
            if (webSocketService != null) {
                webSocketService.pushContainerEvent(null, "create", "容器创建失败: " + e.getMessage(), false);
            }
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startContainer(Integer containerId) {
        log.info("开始启动容器 - 容器ID: {}", containerId);

        try {
            SystemSoftContainer container = this.getById(containerId);
            if (container == null) {
                log.warn("容器不存在 - ID: {}", containerId);
                return false;
            }

            // 创建操作记录
            SystemSoftRecord record = new SystemSoftRecord();
            record.setSystemSoftId(container.getSystemSoftId());
            record.setSystemServerId(container.getSystemServerId());
            record.setSystemSoftRecordOperationType("START_CONTAINER");
            record.setSystemSoftRecordRemark("启动容器: " + container.getSystemSoftContainerName());
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.IN_PROGRESS);
            record.setSystemSoftRecordStartTime(LocalDateTime.now());
            systemSoftRecordMapper.insert(record);

            // 调用Docker API启动容器
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(container.getSystemServerId());
            if (dockerClient != null) {
                try {
                    // 优先使用容器名称启动，如果没有名称则使用容器ID
                    if (StringUtils.hasText(container.getSystemSoftContainerName())) {
                        dockerClient.startContainerByName(container.getSystemSoftContainerName());
                        log.info("Docker容器启动成功 - 容器名称: {}", container.getSystemSoftContainerName());
                    } else if (StringUtils.hasText(container.getSystemSoftContainerDockerId())) {
                        dockerClient.startContainer(container.getSystemSoftContainerDockerId());
                        log.info("Docker容器启动成功 - 容器ID: {}", container.getSystemSoftContainerDockerId());
                    } else {
                        log.warn("容器名称和Docker ID都为空，无法启动容器");
                        record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                        record.setSystemSoftRecordEndTime(LocalDateTime.now());
                        record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: 容器名称和Docker ID都为空");
                        systemSoftRecordMapper.updateById(record);
                        return false;
                    }
                } catch (Exception e) {
                    log.error("Docker容器启动失败: {}", e.getMessage(), e);
                    // 更新记录状态为失败
                    record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                    record.setSystemSoftRecordEndTime(LocalDateTime.now());
                    record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: " + e.getMessage());
                    systemSoftRecordMapper.updateById(record);
                    return false;
                }
            } else {
                log.warn("Docker客户端未配置，跳过实际容器启动操作");
            }

            // 更新容器状态
            container.setSystemSoftContainerStatus("RUNNING");
            container.setSystemSoftContainerStartedTime(LocalDateTime.now());
            container.setUpdateTime(LocalDateTime.now());
            this.updateById(container);

            // 推送容器启动事件
            if (webSocketService != null) {
                webSocketService.pushContainerEvent(containerId, "start", "容器启动成功", true);
                webSocketService.pushContainerStatus(containerId, "RUNNING");
            }

            // 更新操作记录为成功
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.SUCCESS);
            record.setSystemSoftRecordEndTime(LocalDateTime.now());
            systemSoftRecordMapper.updateById(record);

            log.info("容器启动成功 - 容器名称: {}", container.getSystemSoftContainerName());
            return true;

        } catch (Exception e) {
            log.error("启动容器失败: {}", e.getMessage(), e);
            // 推送容器启动失败事件
            if (webSocketService != null) {
                webSocketService.pushContainerEvent(containerId, "start", "容器启动失败: " + e.getMessage(), false);
            }
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean stopContainer(Integer containerId) {
        log.info("开始停止容器 - 容器ID: {}", containerId);

        try {
            SystemSoftContainer container = this.getById(containerId);
            if (container == null) {
                log.warn("容器不存在 - ID: {}", containerId);
                return false;
            }

            // 创建操作记录
            SystemSoftRecord record = new SystemSoftRecord();
            record.setSystemSoftId(container.getSystemSoftId());
            record.setSystemServerId(container.getSystemServerId());
            record.setSystemSoftRecordOperationType("STOP_CONTAINER");
            record.setSystemSoftRecordRemark("停止容器: " + container.getSystemSoftContainerName());
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.IN_PROGRESS);
            record.setSystemSoftRecordStartTime(LocalDateTime.now());
            systemSoftRecordMapper.insert(record);

            // 调用Docker API停止容器
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(container.getSystemServerId());
            if (dockerClient != null) {
                // 优先使用容器名称停止容器
                if (StringUtils.hasText(container.getSystemSoftContainerName())) {
                    dockerClient.stopContainerByName(container.getSystemSoftContainerName());
                    log.info("Docker容器停止成功 - 容器名称: {}", container.getSystemSoftContainerName());
                } else if (StringUtils.hasText(container.getSystemSoftContainerDockerId())) {
                    dockerClient.stopContainer(container.getSystemSoftContainerDockerId());
                    log.info("Docker容器停止成功 - 容器ID: {}", container.getSystemSoftContainerDockerId());
                } else {
                    log.error("容器名称和Docker ID都为空，无法停止容器");
                    record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                    record.setSystemSoftRecordEndTime(LocalDateTime.now());
                    record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: 容器名称和Docker ID都为空");
                    systemSoftRecordMapper.updateById(record);
                    return false;
                }
            } else {
                log.warn("Docker客户端未配置，跳过实际容器停止操作");
            }

            // 更新容器状态
            container.setSystemSoftContainerStatus("STOPPED");
            container.setSystemSoftContainerFinishedTime(LocalDateTime.now());
            container.setUpdateTime(LocalDateTime.now());
            this.updateById(container);

            // 推送容器停止事件
            if (webSocketService != null) {
                webSocketService.pushContainerEvent(containerId, "stop", "容器停止成功", true);
                webSocketService.pushContainerStatus(containerId, "STOPPED");
            }

            // 更新操作记录为成功
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.SUCCESS);
            record.setSystemSoftRecordEndTime(LocalDateTime.now());
            systemSoftRecordMapper.updateById(record);

            log.info("容器停止成功 - 容器名称: {}", container.getSystemSoftContainerName());
            return true;

        } catch (Exception e) {
            log.error("停止容器失败: {}", e.getMessage(), e);
            // 推送容器停止失败事件
            if (webSocketService != null) {
                webSocketService.pushContainerEvent(containerId, "stop", "容器停止失败: " + e.getMessage(), false);
            }
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restartContainer(Integer containerId) {
        log.info("开始重启容器 - 容器ID: {}", containerId);

        try {
            SystemSoftContainer container = this.getById(containerId);
            if (container == null) {
                log.warn("容器不存在 - ID: {}", containerId);
                return false;
            }

            // 创建操作记录
            SystemSoftRecord record = new SystemSoftRecord();
            record.setSystemSoftId(container.getSystemSoftId());
            record.setSystemServerId(container.getSystemServerId());
            record.setSystemSoftRecordOperationType("RESTART_CONTAINER");
            record.setSystemSoftRecordRemark("重启容器: " + container.getSystemSoftContainerName());
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.IN_PROGRESS);
            record.setSystemSoftRecordStartTime(LocalDateTime.now());
            systemSoftRecordMapper.insert(record);

            // 调用Docker API重启容器
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(container.getSystemServerId());
            if (dockerClient != null) {
                try {
                    // 优先使用容器名称重启容器
                    if (StringUtils.hasText(container.getSystemSoftContainerName())) {
                        dockerClient.restartContainerByName(container.getSystemSoftContainerName());
                        log.info("Docker容器重启成功 - 容器名称: {}", container.getSystemSoftContainerName());
                    } else if (StringUtils.hasText(container.getSystemSoftContainerDockerId())) {
                        dockerClient.restartContainer(container.getSystemSoftContainerDockerId());
                        log.info("Docker容器重启成功 - 容器ID: {}", container.getSystemSoftContainerDockerId());
                    } else {
                        log.error("容器名称和Docker ID都为空，无法重启容器");
                        record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                        record.setSystemSoftRecordEndTime(LocalDateTime.now());
                        record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: 容器名称和Docker ID都为空");
                        systemSoftRecordMapper.updateById(record);
                        return false;
                    }
                } catch (Exception e) {
                    log.error("Docker容器重启失败: {}", e.getMessage(), e);
                    // 更新记录状态为失败
                    record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                    record.setSystemSoftRecordEndTime(LocalDateTime.now());
                    record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: " + e.getMessage());
                    systemSoftRecordMapper.updateById(record);
                    return false;
                }
            } else {
                log.error("无法创建Docker客户端，serverId: {}", container.getSystemServerId());
                record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                record.setSystemSoftRecordEndTime(LocalDateTime.now());
                record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: 无法创建Docker客户端");
                systemSoftRecordMapper.updateById(record);
                return false;
            }

            // 更新容器状态和重启次数
            container.setSystemSoftContainerStatus("RUNNING");
            container.setSystemSoftContainerStartedTime(LocalDateTime.now());
            container.setSystemSoftContainerRestartCount(
                    (container.getSystemSoftContainerRestartCount() == null ? 0 : container.getSystemSoftContainerRestartCount()) + 1);
            container.setUpdateTime(LocalDateTime.now());
            this.updateById(container);

            // 推送容器重启事件
            if (webSocketService != null) {
                webSocketService.pushContainerEvent(containerId, "restart", "容器重启成功", true);
                webSocketService.pushContainerStatus(containerId, "RUNNING");
            }

            // 更新操作记录为成功
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.SUCCESS);
            record.setSystemSoftRecordEndTime(LocalDateTime.now());
            systemSoftRecordMapper.updateById(record);

            log.info("容器重启成功 - 容器名称: {}", container.getSystemSoftContainerName());
            return true;

        } catch (
                Exception e) {
            log.error("重启容器失败: {}", e.getMessage(), e);
            // 推送容器重启失败事件
            if (webSocketService != null) {
                webSocketService.pushContainerEvent(containerId, "restart", "容器重启失败: " + e.getMessage(), false);
            }
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeContainer(Integer containerId, boolean force) {
        log.info("开始删除容器 - 容器ID: {}, 强制删除: {}", containerId, force);

        try {
            SystemSoftContainer container = this.getById(containerId);
            if (container == null) {
                log.warn("容器不存在 - ID: {}", containerId);
                return false;
            }

            // 创建操作记录
            SystemSoftRecord record = new SystemSoftRecord();
            record.setSystemSoftId(container.getSystemSoftId());
            record.setSystemServerId(container.getSystemServerId());
            record.setSystemSoftRecordOperationType("REMOVE_CONTAINER");
            record.setSystemSoftRecordRemark("删除容器: " + container.getSystemSoftContainerName());
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.IN_PROGRESS);
            record.setSystemSoftRecordStartTime(LocalDateTime.now());
            systemSoftRecordMapper.insert(record);

            // 调用Docker API删除容器
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(container.getSystemServerId());
            if (dockerClient != null) {
                try {
                    // 优先使用容器名称删除容器
                    if (StringUtils.hasText(container.getSystemSoftContainerName())) {
                        dockerClient.removeContainerByName(container.getSystemSoftContainerName(), force);
                        log.info("Docker容器删除成功 - 容器名称: {}", container.getSystemSoftContainerName());
                    } else if (StringUtils.hasText(container.getSystemSoftContainerDockerId())) {
                        dockerClient.removeContainer(container.getSystemSoftContainerDockerId(), force);
                        log.info("Docker容器删除成功 - 容器ID: {}", container.getSystemSoftContainerDockerId());
                    } else {
                        log.error("容器名称和Docker ID都为空，无法删除容器");
                        record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                        record.setSystemSoftRecordEndTime(LocalDateTime.now());
                        record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: 容器名称和Docker ID都为空");
                        systemSoftRecordMapper.updateById(record);
                        return false;
                    }
                } catch (Exception e) {
                    log.error("Docker容器删除失败: {}", e.getMessage(), e);
                    // 更新记录状态为失败
                    record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                    record.setSystemSoftRecordEndTime(LocalDateTime.now());
                    record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: " + e.getMessage());
                    systemSoftRecordMapper.updateById(record);
                    return false;
                }
            } else {
                log.error("无法创建Docker客户端，serverId: {}", container.getSystemServerId());
                record.setSystemSoftRecordStatus(SystemSoftRecordStatus.FAILED);
                record.setSystemSoftRecordEndTime(LocalDateTime.now());
                record.setSystemSoftRecordRemark(record.getSystemSoftRecordRemark() + " - 失败原因: 无法创建Docker客户端");
                systemSoftRecordMapper.updateById(record);
                return false;
            }

            // 删除容器记录
            this.removeById(containerId);

            // 推送容器删除事件
            if (webSocketService != null) {
                webSocketService.pushContainerEvent(containerId, "remove", "容器删除成功", true);
                webSocketService.pushContainerStatus(containerId, "REMOVED");
            }

            // 更新操作记录为成功
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.SUCCESS);
            record.setSystemSoftRecordEndTime(LocalDateTime.now());
            systemSoftRecordMapper.updateById(record);

            log.info("容器删除成功 - 容器名称: {}", container.getSystemSoftContainerName());
            return true;

        } catch (Exception e) {
            log.error("删除容器失败: {}", e.getMessage(), e);
            // 推送容器删除失败事件
            if (webSocketService != null) {
                webSocketService.pushContainerEvent(containerId, "remove", "容器删除失败: " + e.getMessage(), false);
            }
            return false;
        }
    }

    @Override
    public String getContainerLogs(Integer containerId, Integer lines) {
        log.debug("获取容器日志 - 容器ID: {}, 行数: {}", containerId, lines);

        try {
            SystemSoftContainer container = this.getById(containerId);
            if (container == null) {
                log.warn("容器不存在 - ID: {}", containerId);
                return "容器不存在";
            }

            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(container.getSystemServerId());
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", container.getSystemServerId());
                return "Docker客户端创建失败";
            }

            if (StringUtils.hasText(container.getSystemSoftContainerDockerId())) {
                try {
                    return dockerClient.getContainerLogs(container.getSystemSoftContainerDockerId(), lines);
                } catch (Exception e) {
                    log.error("获取Docker容器日志失败: {}", e.getMessage(), e);
                    return "获取日志失败: " + e.getMessage();
                }
            } else {
                log.warn("容器Docker ID为空");
                return "容器Docker ID为空";
            }

        } catch (Exception e) {
            log.error("获取容器日志失败: {}", e.getMessage(), e);
            return "获取日志失败: " + e.getMessage();
        }
    }

    /**
     * 获取容器统计信息
     *
     * @param containerId 容器ID
     * @return 容器统计信息，包含CPU使用率、内存使用量、网络IO、磁盘IO等
     * @author [CH]
     * @since 2024-01-15
     */
    @Override
    public Statistics getContainerStats(Integer containerId) {
        log.debug("获取容器统计信息 - 容器ID: {}", containerId);

        try {
            SystemSoftContainer container = this.getById(containerId);
            if (container == null) {
                log.warn("容器不存在 - ID: {}", containerId);
                return null;
            }

            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(container.getSystemServerId());
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", container.getSystemServerId());
                return null;
            }

            if (StringUtils.hasText(container.getSystemSoftContainerDockerId())) {
                return dockerClient.getContainerStats(container.getSystemSoftContainerDockerId());
            }

        } catch (Exception e) {
            log.error("获取容器统计信息失败 - 容器ID: {}, 错误: {}", containerId, e.getMessage(), e);
        }

        return null;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncContainerStatus(Integer serverId) {
        log.info("开始同步容器状态 - 服务器ID: {}", serverId);

        int syncCount = 0;

        try {
            // 获取需要同步的容器列表
            List<SystemSoftContainer> containers;
            if (serverId != null) {
                containers = getContainersByServerId(serverId);
            } else {
                containers = this.list();
            }

            // 按服务器ID分组容器
            Map<Integer, List<SystemSoftContainer>> containersByServer = containers.stream()
                    .filter(c -> c.getSystemServerId() != null)
                    .collect(Collectors.groupingBy(SystemSoftContainer::getSystemServerId));

            for (Map.Entry<Integer, List<SystemSoftContainer>> entry : containersByServer.entrySet()) {
                Integer currentServerId = entry.getKey();
                List<SystemSoftContainer> serverContainers = entry.getValue();

                DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(currentServerId);
                if (dockerClient == null) {
                    log.warn("无法创建Docker客户端 - 服务器ID: {}, 跳过该服务器的容器同步", currentServerId);
                    continue;
                }

                try {
                    // 获取Docker中的所有容器
                    List<Container> dockerContainers = dockerClient.listContainers(true);
                    Map<String, Container> dockerContainerMap = dockerContainers.stream()
                            .collect(Collectors.toMap(Container::getId, c -> c, (existing, replacement) -> existing));

                    for (SystemSoftContainer container : serverContainers) {
                        if (StringUtils.hasText(container.getSystemSoftContainerDockerId())) {
                            Container dockerContainer = dockerContainerMap.get(container.getSystemSoftContainerDockerId());

                            String newStatus;
                            if (dockerContainer != null) {
                                newStatus = dockerContainer.getState();
                            } else {
                                newStatus = "REMOVED";
                            }

                            // 更新状态（如果有变化）
                            if (!newStatus.equals(container.getSystemSoftContainerStatus())) {
                                container.setSystemSoftContainerStatus(newStatus);
                                container.setUpdateTime(LocalDateTime.now());
                                this.updateById(container);
                                syncCount++;
                                log.debug("容器状态已更新 - 容器: {}, 新状态: {}",
                                        container.getSystemSoftContainerName(), newStatus);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("同步服务器{}的容器状态失败: {}", currentServerId, e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            log.error("同步容器状态失败: {}", e.getMessage(), e);
        }

        log.info("容器状态同步完成 - 同步数量: {}", syncCount);
        return syncCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemSoftContainer syncSingleContainerStatus(Integer containerId) {
        log.info("开始同步单个容器状态 - 容器ID: {}", containerId);

        try {
            // 获取容器信息
            SystemSoftContainer container = this.getById(containerId);
            if (container == null) {
                log.warn("容器不存在 - 容器ID: {}", containerId);
                return null;
            }

            // 检查容器是否有Docker ID
            if (!StringUtils.hasText(container.getSystemSoftContainerDockerId())) {
                log.warn("容器没有Docker ID - 容器ID: {}, 容器名称: {}", containerId, container.getSystemSoftContainerName());
                return null;
            }

            // 创建Docker客户端
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(container.getSystemServerId());
            if (dockerClient == null) {
                log.warn("无法创建Docker客户端 - 服务器ID: {}", container.getSystemServerId());
                return null;
            }

            try {
                // 获取Docker中的容器信息
                List<Container> dockerContainers = dockerClient.listContainers(true);
                Container dockerContainer = dockerContainers.stream()
                        .filter(c -> c.getId().equals(container.getSystemSoftContainerDockerId()))
                        .findFirst()
                        .orElse(null);

                String newStatus;
                if (dockerContainer != null) {
                    newStatus = dockerContainer.getState();
                    log.debug("从Docker获取容器状态 - 容器: {}, 状态: {}", container.getSystemSoftContainerName(), newStatus);
                } else {
                    newStatus = "REMOVED";
                    log.debug("容器在Docker中不存在，标记为已删除 - 容器: {}", container.getSystemSoftContainerName());
                }

                // 更新状态（如果有变化）
                if (!newStatus.equals(container.getSystemSoftContainerStatus())) {
                    String oldStatus = container.getSystemSoftContainerStatus();
                    container.setSystemSoftContainerStatus(newStatus);
                    container.setUpdateTime(LocalDateTime.now());
                    this.updateById(container);

                    log.info("容器状态已更新 - 容器: {}, 旧状态: {}, 新状态: {}",
                            container.getSystemSoftContainerName(), oldStatus, newStatus);

                    // 推送容器状态变更事件
                    if (webSocketService != null) {
                        webSocketService.pushContainerStatus(containerId, newStatus);
                    }
                } else {
                    log.debug("容器状态无变化 - 容器: {}, 状态: {}", container.getSystemSoftContainerName(), newStatus);
                }

                log.info("单个容器状态同步完成 - 容器ID: {}, 容器名称: {}, 状态: {}",
                        containerId, container.getSystemSoftContainerName(), newStatus);
                return container;

            } catch (Exception e) {
                log.error("同步容器状态失败 - 容器ID: {}, 错误: {}", containerId, e.getMessage(), e);
                return null;
            }

        } catch (Exception e) {
            log.error("同步单个容器状态失败 - 容器ID: {}, 错误: {}", containerId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchOperateContainers(List<Integer> containerIds, String operation) {
        log.info("批量操作容器 - 容器数量: {}, 操作类型: {}", containerIds.size(), operation);

        Map<String, Object> result = new HashMap<>();
        List<String> successList = new ArrayList<>();
        List<String> failedList = new ArrayList<>();

        for (Integer containerId : containerIds) {
            try {
                SystemSoftContainer container = this.getById(containerId);
                if (container == null) {
                    failedList.add("容器ID " + containerId + ": 容器不存在");
                    continue;
                }

                boolean success = false;
                switch (operation.toLowerCase()) {
                    case "start":
                        success = startContainer(containerId);
                        break;
                    case "stop":
                        success = stopContainer(containerId);
                        break;
                    case "restart":
                        success = restartContainer(containerId);
                        break;
                    case "remove":
                        success = removeContainer(containerId, false);
                        break;
                    default:
                        failedList.add("容器 " + container.getSystemSoftContainerName() + ": 不支持的操作类型");
                        continue;
                }

                if (success) {
                    successList.add(container.getSystemSoftContainerName());
                } else {
                    failedList.add(container.getSystemSoftContainerName());
                }

            } catch (Exception e) {
                log.error("批量操作容器失败 - 容器ID: {}, 错误: {}", containerId, e.getMessage(), e);
                failedList.add("容器ID " + containerId + ": " + e.getMessage());
            }
        }

        result.put("operation", operation);
        result.put("total", containerIds.size());
        result.put("success", successList.size());
        result.put("failed", failedList.size());
        result.put("successList", successList);
        result.put("failedList", failedList);

        log.info("批量操作容器完成 - 成功: {}, 失败: {}", successList.size(), failedList.size());
        return result;
    }

    @Override
    public boolean checkContainerNameExists(String containerName, Integer serverId, Integer excludeId) {
        return systemSoftContainerMapper.checkContainerNameExists(containerName, serverId, excludeId) > 0;
    }

    @Override
    public boolean checkPortExists(Integer hostPort, Integer serverId, Integer excludeId) {
        return systemSoftContainerMapper.checkPortExists(hostPort, serverId, excludeId) > 0;
    }

    @Override
    public Map<String, Long> getContainerStatusStats(Integer serverId) {
        log.debug("获取容器状态统计 - 服务器ID: {}", serverId);

        List<java.util.Map<String, Object>> stats = systemSoftContainerMapper.countByStatus(serverId);

        return stats.stream().collect(Collectors.toMap(
                stat -> (String) stat.get("status"),
                stat -> ((Number) stat.get("count")).longValue(),
                (existing, replacement) -> existing
        ));
    }

    @Override
    public List<SystemSoftContainer> getRunningContainers() {
        return systemSoftContainerMapper.selectRunningContainers();
    }

    @Override
    public List<SystemSoftContainer> getAbnormalContainers() {
        return systemSoftContainerMapper.selectAbnormalContainers();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateContainer(Integer containerId, com.chua.starter.monitor.pojo.request.UpdateContainerRequest request) {
        log.info("开始更新容器配置 - 容器ID: {}", containerId);

        try {
            // 检查容器是否存在
            SystemSoftContainer container = this.getById(containerId);
            if (container == null) {
                log.warn("容器不存在 - ID: {}", containerId);
                return false;
            }

            // 获取更新请求参数
            com.chua.starter.monitor.pojo.request.UpdateContainerRequest updateRequest = request;

            // 检查容器名称是否重复（排除当前容器）
            if (StringUtils.hasText(updateRequest.getContainerName()) &&
                    !updateRequest.getContainerName().equals(container.getSystemSoftContainerName())) {
                if (checkContainerNameExists(updateRequest.getContainerName(), container.getSystemServerId(), containerId)) {
                    log.warn("容器名称已存在: {}", updateRequest.getContainerName());
                    return false;
                }
            }

            // 创建操作记录
            SystemSoftRecord record = new SystemSoftRecord();
            record.setSystemSoftId(container.getSystemSoftId());
            record.setSystemServerId(container.getSystemServerId());
            record.setSystemSoftRecordOperationType("UPDATE_CONTAINER");
            record.setSystemSoftRecordRemark("更新容器配置: " + container.getSystemSoftContainerName());
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.IN_PROGRESS);
            record.setSystemSoftRecordStartTime(LocalDateTime.now());
            systemSoftRecordMapper.insert(record);

            // 更新容器配置
            boolean needUpdate = false;

            if (StringUtils.hasText(updateRequest.getContainerName()) &&
                    !updateRequest.getContainerName().equals(container.getSystemSoftContainerName())) {
                container.setSystemSoftContainerName(updateRequest.getContainerName());
                needUpdate = true;
                log.debug("更新容器名称: {}", updateRequest.getContainerName());
            }

            if (StringUtils.hasText(updateRequest.getImageTag()) &&
                    !updateRequest.getImageTag().equals(container.getSystemSoftContainerImageTag())) {
                container.setSystemSoftContainerImageTag(updateRequest.getImageTag());
                needUpdate = true;
                log.debug("更新镜像标签: {}", updateRequest.getImageTag());
            }

            if (updateRequest.getPorts() != null &&
                    !Objects.equals(updateRequest.getPorts(), container.getSystemSoftContainerPorts())) {
                container.setSystemSoftContainerPorts(updateRequest.getPorts());
                needUpdate = true;
                log.debug("更新端口映射: {}", updateRequest.getPorts());
            }

            if (updateRequest.getEnvVars() != null &&
                    !Objects.equals(updateRequest.getEnvVars(), container.getSystemSoftContainerEnv())) {
                container.setSystemSoftContainerEnv(updateRequest.getEnvVars());
                needUpdate = true;
                log.debug("更新环境变量: {}", updateRequest.getEnvVars());
            }

            if (updateRequest.getVolumes() != null &&
                    !Objects.equals(updateRequest.getVolumes(), container.getSystemSoftContainerVolumes())) {
                container.setSystemSoftContainerVolumes(updateRequest.getVolumes());
                needUpdate = true;
                log.debug("更新数据卷映射: {}", updateRequest.getVolumes());
            }

            if (updateRequest.getCommand() != null &&
                    !Objects.equals(updateRequest.getCommand(), container.getSystemSoftContainerCommand())) {
                container.setSystemSoftContainerCommand(updateRequest.getCommand());
                needUpdate = true;
                log.debug("更新启动命令: {}", updateRequest.getCommand());
            }

            if (updateRequest.getArgs() != null &&
                    !Objects.equals(updateRequest.getArgs(), container.getSystemSoftContainerArgs())) {
                container.setSystemSoftContainerArgs(updateRequest.getArgs());
                needUpdate = true;
                log.debug("更新启动参数: {}", updateRequest.getArgs());
            }

            if (updateRequest.getAutoRestart() != null &&
                    !Objects.equals(updateRequest.getAutoRestart(), container.getSystemSoftContainerAutoRestart())) {
                container.setSystemSoftContainerAutoRestart(updateRequest.getAutoRestart());
                needUpdate = true;
                log.debug("更新自动重启设置: {}", updateRequest.getAutoRestart());
            }

            if (!needUpdate) {
                log.info("容器配置无变化，跳过更新 - 容器ID: {}", containerId);
                // 更新操作记录为成功
                record.setSystemSoftRecordStatus(SystemSoftRecordStatus.SUCCESS);
                record.setSystemSoftRecordRemark("容器配置无变化");
                record.setSystemSoftRecordEndTime(LocalDateTime.now());
                systemSoftRecordMapper.updateById(record);
                return true;
            }

            // 更新数据库记录
            container.setUpdateTime(LocalDateTime.now());
            this.updateById(container);

            // 推送容器更新事件
            if (webSocketService != null) {
                webSocketService.pushContainerEvent(containerId, "update", "容器配置更新成功", true);
            }

            // 更新操作记录为成功
            record.setSystemSoftRecordStatus(SystemSoftRecordStatus.SUCCESS);
            record.setSystemSoftRecordEndTime(LocalDateTime.now());
            systemSoftRecordMapper.updateById(record);

            log.info("容器配置更新成功 - 容器ID: {}, 容器名称: {}", containerId, container.getSystemSoftContainerName());
            return true;

        } catch (Exception e) {
            log.error("更新容器配置失败 - 容器ID: {}, 错误: {}", containerId, e.getMessage(), e);
            // 推送容器更新失败事件
            if (webSocketService != null) {
                webSocketService.pushContainerEvent(containerId, "update", "容器配置更新失败: " + e.getMessage(), false);
            }
            return false;
        }
    }

    /**
     * 解析端口映射字符串
     * 格式: "8080:80,9090:90" 或 JSON格式
     *
     * @param ports 端口映射字符串
     * @return 端口映射Map
     */
    private Map<String, String> parsePortMappings(String ports) {
        Map<String, String> portBindings = new HashMap<>();

        if (!StringUtils.hasText(ports)) {
            return portBindings;
        }

        try {
            // 简单格式解析: "8080:80,9090:90"
            String[] portPairs = ports.split(",");
            for (String portPair : portPairs) {
                String[] parts = portPair.trim().split(":");
                if (parts.length == 2) {
                    portBindings.put(parts[1].trim(), parts[0].trim());
                }
            }
        } catch (Exception e) {
            log.warn("解析端口映射失败: {}", e.getMessage());
        }

        return portBindings;
    }

    /**
     * 解析环境变量字符串
     * 格式: "KEY1=value1,KEY2=value2" 或 JSON格式
     *
     * @param envVars 环境变量字符串
     * @return 环境变量列表
     */
    private List<String> parseEnvironmentVariables(String envVars) {
        List<String> envList = new ArrayList<>();

        if (!StringUtils.hasText(envVars)) {
            return envList;
        }

        try {
            // 简单格式解析: "KEY1=value1,KEY2=value2"
            String[] envPairs = envVars.split(",");
            for (String envPair : envPairs) {
                String trimmed = envPair.trim();
                if (trimmed.contains("=")) {
                    envList.add(trimmed);
                }
            }
        } catch (Exception e) {
            log.warn("解析环境变量失败: {}", e.getMessage());
        }

        return envList;
    }

    /**
     * 解析数据卷映射字符串
     * 格式: "/host/path:/container/path,/host/path2:/container/path2" 或 JSON格式
     *
     * @param volumes 数据卷映射字符串
     * @return 数据卷映射Map
     */
    private Map<String, String> parseVolumeMappings(String volumes) {
        Map<String, String> volumeBindings = new HashMap<>();

        if (!StringUtils.hasText(volumes)) {
            return volumeBindings;
        }

        try {
            // 简单格式解析: "/host/path:/container/path,/host/path2:/container/path2"
            String[] volumePairs = volumes.split(",");
            for (String volumePair : volumePairs) {
                String[] parts = volumePair.trim().split(":");
                if (parts.length == 2) {
                    volumeBindings.put(parts[1].trim(), parts[0].trim());
                }
            }
        } catch (Exception e) {
            log.warn("解析数据卷映射失败: {}", e.getMessage());
        }

        return volumeBindings;
    }

    /**
     * 根据条件查询容器列表
     *
     * @param queryParams 查询参数Map
     * @return 容器列表
     * @author CH
     * @since 2025/01/15
     */
    @Override
    public List<SystemSoftContainer> listByCondition(Map<String, Object> queryParams) {
        log.debug("根据条件查询容器列表 - 查询参数: {}", queryParams);

        if (queryParams == null || queryParams.isEmpty()) {
            log.debug("查询参数为空，返回所有容器");
            return this.list();
        }

        LambdaQueryWrapper<SystemSoftContainer> wrapper = new LambdaQueryWrapper<>();

        // 服务器ID
        Object serverId = queryParams.get("serverId");
        if (serverId != null) {
            wrapper.eq(SystemSoftContainer::getSystemServerId, serverId);
        }

        // 软件ID
        Object softId = queryParams.get("softId");
        if (softId != null) {
            wrapper.eq(SystemSoftContainer::getSystemSoftId, softId);
        }

        // 容器状态
        Object containerStatus = queryParams.get("containerStatus");
        if (containerStatus != null) {
            wrapper.eq(SystemSoftContainer::getSystemSoftContainerStatus, containerStatus);
        }

        // 容器名称（模糊查询）
        Object containerName = queryParams.get("containerName");
        if (containerName != null && StringUtils.hasText(containerName.toString())) {
            wrapper.like(SystemSoftContainer::getSystemSoftContainerName, containerName.toString());
        }

        // 镜像ID
        Object imageId = queryParams.get("imageId");
        if (imageId != null && StringUtils.hasText(imageId.toString())) {
            wrapper.eq(SystemSoftContainer::getSystemSoftContainerImage, imageId.toString());
        }

        // 主机端口
        Object hostPort = queryParams.get("hostPort");
        if (hostPort != null) {
            wrapper.like(SystemSoftContainer::getSystemSoftContainerPorts, hostPort.toString());
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(SystemSoftContainer::getCreateTime);

        List<SystemSoftContainer> result = this.list(wrapper);
        log.debug("根据条件查询容器列表完成 - 查询到 {} 条记录", result.size());

        return result;
    }
}