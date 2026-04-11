package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.docker.support.client.DockerProtocolClient;
import com.chua.docker.support.client.DockerPullImage;
import com.chua.starter.monitor.callback.WebSocketContainerCreateCallback;
import com.chua.starter.monitor.constant.MonitorSocketTopics;
import com.chua.starter.monitor.entity.SystemSoft;
import com.chua.starter.monitor.entity.DockerImage;
import com.chua.starter.monitor.entity.DockerImageStatus;
import com.chua.starter.monitor.enums.OperationType;
import com.chua.starter.monitor.factory.DockerProtocolClientFactory;
import com.chua.starter.monitor.mapper.DockerImageMapper;
import com.chua.starter.monitor.mapper.SystemSoftMapper;
import com.chua.starter.monitor.pojo.docker.DockerClientCreateResult;
import com.chua.starter.monitor.pojo.docker.DockerCreateContainerRequest;
import com.chua.starter.monitor.service.docker.DockerCallbackService;
import com.chua.starter.monitor.service.docker.DockerContainerService;
import com.chua.starter.monitor.service.docker.DockerImageService;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.InternetProtocol;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.RestartPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统软件镜像管理服务实现类
 *
 * @author CH
 * @since 2025/01/15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DockerImageServiceImpl extends ServiceImpl<DockerImageMapper, DockerImage> implements DockerImageService {

    private final DockerImageMapper systemSoftImageMapper;
    private final DockerProtocolClientFactory dockerProtocolClientFactory;
    private final SystemSoftMapper systemSoftMapper;

    private final TransactionTemplate transactionTemplate;
    @Autowired
    @Lazy
    private DockerCallbackService dockerCallbackService;
    @Autowired
    @Lazy
    private com.chua.starter.monitor.service.docker.SystemSoftRecordService systemSoftRecordService;
    @Autowired
    @Lazy
    private DockerContainerService systemSoftContainerService;

    /**
     * 根据条件查询镜像列表
     *
     * @param serverId  服务器ID
     * @param softId    软件ID
     * @param status    镜像状态
     * @param imageName 镜像名称
     * @return 镜像列表
     */
    @Override
    public ReturnResult<List<DockerImage>> selectImageList(Integer serverId, Integer softId, DockerImageStatus status, String imageName) {
        log.info("根据条件查询镜像列表 - 服务器ID: {}, 软件ID: {}, 状态: {}, 镜像名称: {}", serverId, softId, status, imageName);

        try {
            // 查询数据库中的镜像列表
            // 这里应该根据实际的查询条件构造查询逻辑
            List<DockerImage> imageList = systemSoftImageMapper.selectList(null);
            log.info("根据条件查询镜像列表完成");

            return ReturnResult.ok(imageList);
        } catch (Exception e) {
            log.error("根据条件查询镜像列表失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 从软件拉取镜像到服务器
     *
     * @param softId    软件ID
     * @param serverId  服务器ID
     * @param imageTag  镜像标签
     * @param config    拉取配置
     * @return 拉取结果
     */
    @Override
    public ReturnResult<DockerImage> pullImage(Integer softId, Integer serverId, String imageTag, DockerPullImage config) {
        log.info("拉取镜像 - 软件ID: {}, 服务器ID: {}, 镜像标签: {}", softId, serverId, imageTag);

        // 创建操作记录（先记录）
        com.chua.starter.monitor.entity.SystemSoftRecord record = new com.chua.starter.monitor.entity.SystemSoftRecord();
        record.setSystemSoftId(softId);
        record.setSystemServerId(serverId);
        record.setSystemSoftRecordOperationType("PULL_IMAGE");
        record.setSystemSoftRecordMethod("pullImage");
        record.setSystemSoftRecordMessage("开始拉取镜像");
        record.setSystemSoftRecordStatus(0); // 0: 进行中
        record.setSystemSoftRecordStartTime(java.time.LocalDateTime.now());
        record.setSystemSoftRecordTime(java.time.LocalDateTime.now());
        
        try {
            // 获取软件信息
            SystemSoft systemSoft = systemSoftMapper.selectById(softId);
            if (null == systemSoft) {
                log.error("无法获取镜像信息 - 软件ID: {}", softId);
                
                // 记录失败
                record.setSystemSoftRecordStatus(2); // 2: 失败
                record.setSystemSoftRecordErrorMessage("无法获取镜像信息");
                record.setSystemSoftRecordEndTime(java.time.LocalDateTime.now());
                record.setSystemSoftRecordDuration(java.time.Duration.between(record.getSystemSoftRecordStartTime(), record.getSystemSoftRecordEndTime()).toMillis());
                systemSoftRecordService.save(record);
                
                return ReturnResult.error("无法获取镜像信息");
            }

            String imageName = systemSoft.getSystemSoftDockerImage();
            // 构造完整的镜像名称
            String fullImageName = imageName;
            if (StringUtils.hasText(imageTag)) {
                fullImageName = imageName + ":" + imageTag;
            }
            
            // 更新记录的参数信息
            record.setSystemSoftRecordParams(String.format("imageName=%s, imageTag=%s", imageName, imageTag));

            // 保存记录到数据库（不管成功失败都记录）
            systemSoftRecordService.save(record);

            // 如果config为null，则创建默认的DockerPullImage配置对象
            if (config == null) {
                config = DockerPullImage.builder()
                        .imageName(imageName)
                        .tag(imageTag)
                        .build();
            }

            // 获取Docker客户端
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                
                // 更新记录状态为失败
                record.setSystemSoftRecordStatus(2); // 2: 失败
                record.setSystemSoftRecordErrorMessage("无法创建Docker客户端");
                record.setSystemSoftRecordEndTime(java.time.LocalDateTime.now());
                record.setSystemSoftRecordDuration(java.time.Duration.between(record.getSystemSoftRecordStartTime(), record.getSystemSoftRecordEndTime()).toMillis());
                systemSoftRecordService.updateById(record);
                
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 先创建一个DockerImage记录（状态为PULLING），以便获取imageId用于WebSocket回调
            DockerImage image = new DockerImage();
            image.setSystemSoftId(softId);
            image.setSystemSoftImageServerId(serverId);
            image.setSystemSoftImageName(imageName);
            image.setSystemSoftImageTag(imageTag);
            image.setSystemSoftImageFullName(fullImageName);
            image.setSystemSoftImageStatus(DockerImageStatus.PULLING);
            image.setSystemSoftImageRepositoryId(systemSoft.getSystemSoftRegistryId());
            image.setCreateTime(java.time.LocalDateTime.now());
            image.setUpdateTime(java.time.LocalDateTime.now());
            
            // 保存镜像记录到数据库，获取自动生成的ID
            this.save(image);
            
            // 创建状态更新WebSocket回调处理器，传入镜像ID
            var callback = dockerCallbackService.createStatusUpdatingWebSocketPullCallback(
                OperationType.PULL, 
                fullImageName, 
                image.getSystemSoftImageId()
            );

            // 执行拉取操作（带回调）
            log.info("开始拉取镜像(带回调): {}:{}, 镜像ID: {}", imageName, imageTag, image.getSystemSoftImageId());
            dockerCallbackService.pullImageWithCallback(dockerClient, config, callback);

            log.info("拉取镜像请求已发送 - 软件ID: {}, 服务器ID: {}, 镜像标签: {}, 镜像ID: {}, 记录ID: {}", 
                softId, serverId, imageTag, image.getSystemSoftImageId(), record.getSystemSoftRecordId());
            
            // 返回已保存的镜像对象（状态为PULLING，后续会通过回调更新状态）
            return ReturnResult.ok(image);
        } catch (Exception e) {
            // 更新记录状态为失败
            try {
                record.setSystemSoftRecordStatus(2); // 2: 失败
                record.setSystemSoftRecordErrorMessage("拉取镜像失败: " + e.getMessage());
                record.setSystemSoftRecordEndTime(java.time.LocalDateTime.now());
                record.setSystemSoftRecordDuration(java.time.Duration.between(record.getSystemSoftRecordStartTime(), record.getSystemSoftRecordEndTime()).toMillis());
                systemSoftRecordService.updateById(record);
            } catch (Exception updateEx) {
                log.warn("更新记录状态为失败时出错: {}", updateEx.getMessage());
            }

            log.error("拉取镜像失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("拉取镜像失败: " + e.getMessage());
        }
    }

    /**
     * 创建容器
     *
     * @param imageId 镜像ID
     * @param config  容器配置
     * @return 创建结果
     */
    @Override
    public ReturnResult<Boolean> createContainer(Integer imageId, DockerCreateContainerRequest config) {
        log.info("创建容器 - 镜像ID: {}", imageId);

        // 创建操作记录（先记录）
        com.chua.starter.monitor.entity.SystemSoftRecord record = new com.chua.starter.monitor.entity.SystemSoftRecord();
        record.setSystemSoftRecordOperationType("CREATE_CONTAINER");
        record.setSystemSoftRecordMethod("createContainer");
        record.setSystemSoftRecordMessage("开始创建容器");
        record.setSystemSoftRecordStatus(0); // 0: 进行中
        record.setSystemSoftRecordStartTime(java.time.LocalDateTime.now());
        record.setSystemSoftRecordTime(java.time.LocalDateTime.now());

        DockerCreateContainerRequest request = config != null ? config : new DockerCreateContainerRequest();
        WebSocketContainerCreateCallback callback = null;
        try {
            // 获取镜像信息
            DockerImage image = systemSoftImageMapper.selectById(imageId);
            if (image == null) {
                log.error("镜像不存在 - 镜像ID: {}", imageId);
                
                // 记录失败
                record.setSystemSoftRecordStatus(2); // 2: 失败
                record.setSystemSoftRecordErrorMessage("镜像不存在");
                record.setSystemSoftRecordEndTime(java.time.LocalDateTime.now());
                record.setSystemSoftRecordDuration(java.time.Duration.between(record.getSystemSoftRecordStartTime(), record.getSystemSoftRecordEndTime()).toMillis());
                systemSoftRecordService.save(record);
                
                return ReturnResult.error("镜像不存在");
            }

            // 设置记录的关联信息
            record.setSystemSoftId(image.getSystemSoftId());
            record.setSystemServerId(image.getSystemSoftImageServerId());

            // 获取容器名称（如果配置中没有提供，则生成一个默认名称）
            String containerName = request.getContainerName();
            if (!StringUtils.hasText(containerName)) {
                containerName = "container-" + image.getSystemSoftImageName().replace("/", "-") + "-" + System.currentTimeMillis();
                request.setContainerName(containerName);
            }
            
            // 设置记录参数
            record.setSystemSoftRecordParams(String.format("imageId=%d, containerName=%s", imageId, containerName));
            
            // 保存记录到数据库（不管成功失败都记录）
            systemSoftRecordService.save(record);

            // 获取Docker客户端
            Integer serverId = image.getSystemSoftImageServerId();
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                
                // 更新记录状态为失败
                record.setSystemSoftRecordStatus(2); // 2: 失败
                record.setSystemSoftRecordErrorMessage("无法创建Docker客户端");
                record.setSystemSoftRecordEndTime(java.time.LocalDateTime.now());
                record.setSystemSoftRecordDuration(java.time.Duration.between(record.getSystemSoftRecordStartTime(), record.getSystemSoftRecordEndTime()).toMillis());
                systemSoftRecordService.updateById(record);
                
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 构造完整的镜像名称（如果配置中没有提供）
            request.setImageName(resolveImageName(image, request.getImageName()));

            // 创建WebSocket回调处理器，传入完整的关联信息
            callback = new WebSocketContainerCreateCallback(
                    dockerCallbackService.getSocketSessionTemplate(), 
                    OperationType.CREATE, 
                    containerName,
                    record.getSystemSoftRecordId(),
                    image.getSystemSoftId(),
                    serverId);

            log.info("开始创建容器: {} 使用镜像: {}, 记录ID: {}, autoStart: {}",
                    containerName, request.getImageName(), record.getSystemSoftRecordId(), request.shouldAutoStart());
            callback.onStart(null);

            String dockerContainerId = createDockerContainer(dockerClient, request);
            boolean autoStarted = false;
            if (request.shouldAutoStart()) {
                autoStarted = dockerClient.startContainerById(dockerContainerId);
                if (!autoStarted) {
                    throw new IllegalStateException("容器创建成功，但启动失败");
                }
            }

            callback.onComplete();
            syncContainerStatusQuietly(serverId);

            record.setSystemSoftRecordStatus(1); // 1: 成功
            record.setSystemSoftRecordMessage(autoStarted ? "创建并启动容器成功" : "创建容器成功");
            record.setSystemSoftRecordContainerId(dockerContainerId);
            record.setSystemSoftRecordResult(String.format("containerName=%s, dockerContainerId=%s, autoStarted=%s",
                    containerName, dockerContainerId, autoStarted));
            record.setSystemSoftRecordEndTime(java.time.LocalDateTime.now());
            record.setSystemSoftRecordDuration(java.time.Duration.between(record.getSystemSoftRecordStartTime(), record.getSystemSoftRecordEndTime()).toMillis());
            systemSoftRecordService.updateById(record);

            log.info("创建容器成功 - 镜像ID: {}, 容器名: {}, Docker容器ID: {}, 记录ID: {}",
                    imageId, containerName, dockerContainerId, record.getSystemSoftRecordId());
            return ReturnResult.ok(true);
        } catch (Exception e) {
            // 更新记录状态为失败
            try {
                record.setSystemSoftRecordStatus(2); // 2: 失败
                record.setSystemSoftRecordErrorMessage("创建容器失败: " + e.getMessage());
                record.setSystemSoftRecordEndTime(java.time.LocalDateTime.now());
                record.setSystemSoftRecordDuration(java.time.Duration.between(record.getSystemSoftRecordStartTime(), record.getSystemSoftRecordEndTime()).toMillis());
                systemSoftRecordService.updateById(record);
            } catch (Exception updateEx) {
                log.warn("更新记录状态为失败时出错: {}", updateEx.getMessage());
            }

            if (callback != null) {
                try {
                    callback.onError(e);
                } catch (Exception callbackEx) {
                    log.warn("推送容器创建失败事件出错: {}", callbackEx.getMessage());
                }
            }

            log.error("创建容器失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("创建容器失败: " + e.getMessage());
        }
    }

    private String createDockerContainer(DockerProtocolClient dockerClient, DockerCreateContainerRequest config) {
        CreateContainerCmd createContainerCmd = dockerClient.getDockerClient().createContainerCmd(config.getImageName());
        HostConfig hostConfig = new HostConfig();
        boolean hasHostConfig = false;

        if (StringUtils.hasText(config.getContainerName())) {
            createContainerCmd.withName(config.getContainerName());
        }

        List<String> environmentVariables = normalizeStringList(config.getEnvironmentVariables());
        if (!environmentVariables.isEmpty()) {
            createContainerCmd.withEnv(environmentVariables);
        }

        List<ExposedPort> exposedPorts = new ArrayList<>();
        Ports portBindings = new Ports();
        for (String binding : normalizeStringList(config.getPortBindings())) {
            ParsedPortBinding parsedPortBinding = parsePortBinding(binding);
            ExposedPort exposedPort = new ExposedPort(
                    parsedPortBinding.containerPort(),
                    "udp".equals(parsedPortBinding.protocol()) ? InternetProtocol.UDP : InternetProtocol.TCP
            );
            Ports.Binding hostBinding = StringUtils.hasText(parsedPortBinding.hostIp())
                    ? Ports.Binding.bindIpAndPort(parsedPortBinding.hostIp(), parsedPortBinding.hostPort())
                    : Ports.Binding.bindPort(parsedPortBinding.hostPort());
            portBindings.bind(exposedPort, hostBinding);
            exposedPorts.add(exposedPort);
        }
        if (!exposedPorts.isEmpty()) {
            hostConfig.withPortBindings(portBindings);
            createContainerCmd.withExposedPorts(exposedPorts);
            hasHostConfig = true;
        }

        List<Bind> binds = normalizeStringList(config.getVolumeBindings()).stream()
                .map(Bind::parse)
                .toList();
        if (!binds.isEmpty()) {
            hostConfig.withBinds(binds);
            hasHostConfig = true;
        }

        if (config.getAutoRemove() != null) {
            hostConfig.withAutoRemove(config.getAutoRemove());
            hasHostConfig = true;
        }
        if (StringUtils.hasText(config.getRestartPolicy())) {
            hostConfig.withRestartPolicy(RestartPolicy.parse(config.getRestartPolicy()));
            hasHostConfig = true;
        }
        if (StringUtils.hasText(config.getNetworkMode())) {
            hostConfig.withNetworkMode(config.getNetworkMode());
            hasHostConfig = true;
        }
        if (config.getCpuLimit() != null && config.getCpuLimit() > 0) {
            hostConfig.withNanoCPUs(config.getCpuLimit());
            hasHostConfig = true;
        }
        if (config.getMemoryLimit() != null && config.getMemoryLimit() > 0) {
            hostConfig.withMemory(config.getMemoryLimit());
            hasHostConfig = true;
        }
        if (config.getPrivileged() != null) {
            hostConfig.withPrivileged(config.getPrivileged());
            hasHostConfig = true;
        }

        List<String> dnsServers = normalizeStringList(config.getDnsServers());
        if (!dnsServers.isEmpty()) {
            hostConfig.withDns(dnsServers);
            hasHostConfig = true;
        }

        List<String> extraHosts = normalizeStringList(config.getExtraHosts());
        if (!extraHosts.isEmpty()) {
            hostConfig.withExtraHosts(extraHosts.toArray(new String[0]));
            hasHostConfig = true;
        }

        if (StringUtils.hasText(config.getHostname())) {
            createContainerCmd.withHostName(config.getHostname());
        }
        if (StringUtils.hasText(config.getUser())) {
            createContainerCmd.withUser(config.getUser());
        }
        if (StringUtils.hasText(config.getWorkingDirectory())) {
            createContainerCmd.withWorkingDir(config.getWorkingDirectory());
        }

        Map<String, String> labels = parseKeyValueList(config.getLabels());
        if (!labels.isEmpty()) {
            createContainerCmd.withLabels(labels);
        }

        List<String> command = buildCommand(config.getCommand(), config.getArgs());
        if (!command.isEmpty()) {
            createContainerCmd.withCmd(command);
        }

        if (hasHostConfig) {
            createContainerCmd.withHostConfig(hostConfig);
        }

        CreateContainerResponse response = createContainerCmd.exec();
        if (response == null || !StringUtils.hasText(response.getId())) {
            throw new IllegalStateException("创建容器失败，未返回容器ID");
        }
        return response.getId();
    }

    private String resolveImageName(DockerImage image, String requestedImageName) {
        if (StringUtils.hasText(requestedImageName)) {
            return requestedImageName;
        }

        String fullImageName = image.getSystemSoftImageName();
        if (StringUtils.hasText(image.getSystemSoftImageTag())) {
            fullImageName = image.getSystemSoftImageName() + ":" + image.getSystemSoftImageTag();
        }
        return fullImageName;
    }

    private List<String> buildCommand(String command, String args) {
        List<String> result = new ArrayList<>();
        appendTokens(result, command);
        appendTokens(result, args);
        return result;
    }

    private void appendTokens(List<String> target, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        for (String token : value.trim().split("\\s+")) {
            if (StringUtils.hasText(token)) {
                target.add(token);
            }
        }
    }

    private List<String> normalizeStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                result.add(value.trim());
            }
        }
        return result;
    }

    private ParsedPortBinding parsePortBinding(String binding) {
        if (!StringUtils.hasText(binding)) {
            throw new IllegalArgumentException("端口映射不能为空");
        }

        String normalized = binding.trim();
        String protocol = "tcp";
        int protocolIndex = normalized.lastIndexOf('/');
        if (protocolIndex >= 0 && protocolIndex < normalized.length() - 1) {
            protocol = normalized.substring(protocolIndex + 1).trim().toLowerCase();
            normalized = normalized.substring(0, protocolIndex);
        }

        String[] parts = normalized.split(":");
        if (parts.length == 2) {
            return new ParsedPortBinding(null, parsePort(parts[0], binding), parsePort(parts[1], binding), protocol);
        }
        if (parts.length == 3) {
            return new ParsedPortBinding(parts[0].trim(), parsePort(parts[1], binding), parsePort(parts[2], binding), protocol);
        }

        throw new IllegalArgumentException("端口映射格式不正确: " + binding);
    }

    private int parsePort(String port, String rawBinding) {
        try {
            return Integer.parseInt(port.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("端口映射格式不正确: " + rawBinding, e);
        }
    }

    private Map<String, String> parseKeyValueList(List<String> values) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String value : normalizeStringList(values)) {
            int index = value.indexOf('=');
            if (index <= 0) {
                continue;
            }

            String key = value.substring(0, index).trim();
            String itemValue = value.substring(index + 1).trim();
            if (StringUtils.hasText(key)) {
                result.put(key, itemValue);
            }
        }
        return result;
    }

    private void syncContainerStatusQuietly(Integer serverId) {
        try {
            systemSoftContainerService.syncContainerStatus(serverId);
        } catch (Exception e) {
            log.warn("同步容器状态失败 - serverId: {}, error: {}", serverId, e.getMessage());
        }
    }

    private record ParsedPortBinding(String hostIp, int hostPort, int containerPort, String protocol) {
    }

    /**
     * 删除镜像（从服务器中删除）
     *
     * @param imageId 镜像ID
     * @param force   是否强制删除
     * @return 删除结果
     */
    @Override
    public ReturnResult<Boolean> removeImage(Integer imageId, Boolean force) {
        log.info("删除镜像（从服务器中删除） - 镜像ID: {}, 强制删除: {}", imageId, force);

        try {
            // 获取镜像信息
            DockerImage image = systemSoftImageMapper.selectById(imageId);
            if (image == null) {
                log.error("镜像不存在 - 镜像ID: {}", imageId);
                return ReturnResult.error("镜像不存在");
            }

            // 更新镜像状态为删除中
            image.setSystemSoftImageStatus(DockerImageStatus.DELETING);
            this.saveOrUpdate(image);

            // 获取Docker客户端
            Integer serverId = image.getSystemSoftImageServerId();
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                // 更新镜像状态为删除失败
                image.setSystemSoftImageStatus(DockerImageStatus.DELETE_FAILED);
                this.saveOrUpdate(image);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 使用事务模板执行删除操作
            return transactionTemplate.execute(status -> {
                try {
                    // 执行删除操作
                    // 根据文档，DockerProtocolClient有removeImage方法
                    dockerClient.removeImage(image.getSystemSoftImageName(), force != null ? force : false);

                    // 从数据库删除记录
                    super.removeById(imageId);

                    log.info("删除镜像成功 - 镜像ID: {}, 强制删除: {}", imageId, force);
                    return ReturnResult.ok(true);
                } catch (Exception e) {
                    // 更新镜像状态为删除失败
                    try {
                        DockerImage img = systemSoftImageMapper.selectById(imageId);
                        if (img != null) {
                            img.setSystemSoftImageStatus(DockerImageStatus.DELETE_FAILED);
                            this.saveOrUpdate(img);
                        }
                    } catch (Exception updateEx) {
                        log.warn("更新镜像状态为删除失败时出错: {}", updateEx.getMessage());
                    }

                    log.error("删除镜像失败 - 错误信息: {}", e.getMessage(), e);
                    return ReturnResult.error("删除镜像失败: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            // 更新镜像状态为删除失败
            try {
                DockerImage image = systemSoftImageMapper.selectById(imageId);
                if (image != null) {
                    image.setSystemSoftImageStatus(DockerImageStatus.DELETE_FAILED);
                    this.saveOrUpdate(image);
                }
            } catch (Exception updateEx) {
                log.warn("更新镜像状态为删除失败时出错: {}", updateEx.getMessage());
            }

            log.error("删除镜像失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("删除镜像失败: " + e.getMessage());
        }
    }

    /**
     * 获取镜像详情
     *
     * @param imageId 镜像ID
     * @return 镜像详情
     */
    @Override
    public ReturnResult<DockerImage> getImageDetails(Integer imageId) {
        log.info("获取镜像详情 - 镜像ID: {}", imageId);

        try {
            DockerImage image = systemSoftImageMapper.selectById(imageId);
            if (image == null) {
                log.error("镜像不存在 - 镜像ID: {}", imageId);
                return ReturnResult.error("镜像不存在");
            }

            log.info("获取镜像详情成功 - 镜像ID: {}", imageId);
            return ReturnResult.ok(image);
        } catch (Exception e) {
            log.error("获取镜像详情失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("获取镜像详情失败: " + e.getMessage());
        }
    }

    /**
     * 导出镜像到文件
     *
     * @param imageId  镜像ID
     * @param serverId 服务器ID
     * @return 导出结果（包含操作ID和文件路径）
     */
    @Override
    public ReturnResult<Map<String, Object>> exportImage(Integer imageId, Integer serverId) {
        log.info("导出镜像 - 镜像ID: {}, 服务器ID: {}", imageId, serverId);

        try {
            // 获取镜像信息
            DockerImage image = systemSoftImageMapper.selectById(imageId);
            if (image == null) {
                log.error("镜像不存在 - 镜像ID: {}", imageId);
                return ReturnResult.error("镜像不存在");
            }

            // 获取Docker客户端
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 生成操作ID
            String operationId = "export-image-" + System.currentTimeMillis();

            // 生成导出文件路径
            String fileName = image.getSystemSoftImageName().replace("/", "_") + "_" + image.getSystemSoftImageTag() + ".tar";
            String filePath = "/tmp/docker-exports/" + fileName;

            // 推送开始导出的WebSocket消息
            pushExportProgress(operationId, imageId, image.getSystemSoftImageName(), 0, "开始导出镜像...");

            // 异步执行导出任务
            new Thread(() -> {
                try {
                    // 调用Docker客户端导出镜像
                    String fullImageName = image.getSystemSoftImageName() + ":" + image.getSystemSoftImageTag();
                    dockerClient.saveImage(fullImageName, filePath);

                    // 推送完成消息
                    pushExportComplete(operationId, imageId, filePath);
                } catch (Exception e) {
                    log.error("导出镜像失败", e);
                    pushExportError(operationId, imageId, e.getMessage());
                }
            }).start();

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("operationId", operationId);
            result.put("filePath", filePath);
            
            log.info("导出镜像任务已创建 - 操作ID: {}", operationId);
            return ReturnResult.ok(result);

        } catch (Exception e) {
            log.error("导出镜像失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("导出镜像失败: " + e.getMessage());
        }
    }

    /**
     * 从文件导入镜像
     *
     * @param serverId    服务器ID
     * @param filePath    文件路径
     * @param imageName   镜像名称（可选）
     * @param imageTag    镜像标签（可选）
     * @param force       是否强制导入
     * @return 导入结果（包含操作ID）
     */
    @Override
    public ReturnResult<Map<String, Object>> importImage(Integer serverId, String filePath, String imageName, String imageTag, Boolean force) {
        log.info("导入镜像 - 服务器ID: {}, 文件路径: {}", serverId, filePath);

        try {
            // 获取Docker客户端
            DockerProtocolClient dockerClient = dockerProtocolClientFactory.createClient(serverId);
            if (dockerClient == null) {
                log.error("无法创建Docker客户端 - 服务器ID: {}", serverId);
                return ReturnResult.error("无法创建Docker客户端");
            }

            // 生成操作ID
            String operationId = "import-image-" + System.currentTimeMillis();

            // 推送开始导入的WebSocket消息
            pushImportProgress(operationId, serverId, filePath, 0, "开始导入镜像...");

            // 异步执行导入任务
            new Thread(() -> {
                try {
                    // 调用Docker客户端导入镜像
                    dockerClient.loadImage(filePath);

                    // 解析导入的镜像信息并保存到数据库
                    try {
                        // 重新获取镜像列表以找到刚导入的镜像
                        List<Image> images = dockerClient.listImages();
                        if (images != null && !images.isEmpty()) {
                            // 取最新的镜像（通常是刚导入的）
                            for (Image image : images) {
                                String imageFullName = image.getRepoTags() != null && image.getRepoTags().length != 0
                                    ? image.getRepoTags()[0] : image.getId();
                                
                                // 解析镜像名称和标签
                                String[] parts = imageFullName.split(":");
                                String innerImageName = parts.length > 0 ? parts[0] : imageFullName;
                                String innerImageTag = parts.length > 1 ? parts[1] : "latest";
                                
                                // 查找或创建软件记录
                                Integer softId = findOrCreateSoftwareByImageName(innerImageName);
                                
                                // 检查是否已存在
                                DockerImage existingImage = systemSoftImageMapper.selectList(null).stream()
                                    .filter(img -> img.getSystemSoftImageServerId() != null
                                        && img.getSystemSoftImageServerId().equals(serverId)
                                        && img.getSystemSoftImageImageId() != null
                                        && img.getSystemSoftImageImageId().equals(image.getId()))
                                    .findFirst()
                                    .orElse(null);
                                
                                if (existingImage == null) {
                                    // 创建新镜像记录
                                    DockerImage newImage = new DockerImage();
                                    newImage.setSystemSoftId(softId);
                                    newImage.setSystemSoftImageServerId(serverId);
                                    newImage.setSystemSoftImageImageId(image.getId());
                                    newImage.setSystemSoftImageName(innerImageName);
                                    newImage.setSystemSoftImageTag(innerImageTag);
                                    newImage.setSystemSoftImageFullName(imageFullName);
                                    newImage.setSystemSoftImageSize(image.getSize());
                                    newImage.setSystemSoftImageStatus(DockerImageStatus.AVAILABLE);
                                    newImage.setCreateTime(java.time.LocalDateTime.now());
                                    systemSoftImageMapper.insert(newImage);
                                    log.info("保存导入的镜像信息 - 镜像ID: {}, 名称: {}", image.getId(), imageFullName);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("解析并保存导入的镜像信息失败: {}", e.getMessage());
                    }

                    // 推送完成消息
                    pushImportComplete(operationId, serverId);
                } catch (Exception e) {
                    log.error("导入镜像失败", e);
                    pushImportError(operationId, serverId, e.getMessage());
                }
            }).start();

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("operationId", operationId);
            
            log.info("导入镜像任务已创建 - 操作ID: {}", operationId);
            return ReturnResult.ok(result);

        } catch (Exception e) {
            log.error("导入镜像失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("导入镜像失败: " + e.getMessage());
        }
    }

    /**
     * 从服务器同步Docker镜像到DockerImage表
     *
     * @param serverIds 服务器ID列表
     * @return 同步结果（包含操作ID和同步数量以及详细的服务器状态）
     */
    @Override
    public ReturnResult<Map<String, Object>> syncImagesFromServers(List<Integer> serverIds) {
        log.info("从服务器同步Docker镜像 - serverIds: {}", serverIds);

        try {
            String operationId = "sync-images-" + System.currentTimeMillis();
            int totalImages = 0;
            int successCount = 0;
            int errorCount = 0;

            // 批量创建Docker客户端，获取详细状态
            DockerClientCreateResult createResult = dockerProtocolClientFactory.createClients(serverIds);

            // 处理成功连接的服务器
            for (Map.Entry<Integer, DockerProtocolClient> entry : createResult.getSuccessClients().entrySet()) {
                Integer serverId = entry.getKey();
                DockerProtocolClient dockerClient = entry.getValue();
                try {
                    log.info("开始同步服务器镜像 - serverId: {}", serverId);

                    // 推送开始同步的WebSocket消息
                    pushSyncProgress(operationId, serverId, "服务器-" + serverId, 0, "开始同步镜像列表...");

                    // 获取服务器上的镜像列表
                    List<Image> images = dockerClient.listImages();
                    if (images == null || images.isEmpty()) {
                        log.warn("服务器上没有镜像 - 服务器ID: {}", serverId);
                        pushSyncProgress(operationId, serverId, "服务器-" + serverId, 100, "没有可同步的镜像");
                        continue;
                    }

                    totalImages += images.size();
                    int imageIndex = 0;

                    for (Image image : images) {
                        try {
                            imageIndex++;
                            int progress = (int) ((double) imageIndex / images.size() * 100);
                            pushSyncProgress(operationId, serverId, "服务器-" + serverId, progress, 
                                String.format("同步镜像 %d/%d", imageIndex, images.size()));

                            // 解析镜像名称和标签
                            String imageFullName = image.getRepoTags() != null && image.getRepoTags().length != 0
                                ? image.getRepoTags()[0] : image.getId();
                            String[] parts = imageFullName.split(":");
                            String name = parts.length > 0 ? parts[0] : imageFullName;
                            String tag = parts.length > 1 ? parts[1] : "latest";

                            // 查找或创建对应的SystemSoft记录
                            Integer softId = findOrCreateSoftwareByImageName(name);

                            // 检查数据库中是否已存在此镜像
                            DockerImage existingImage = systemSoftImageMapper.selectList(null).stream()
                                .filter(img -> img.getSystemSoftImageServerId().equals(serverId) 
                                    && img.getSystemSoftImageImageId() != null
                                    && img.getSystemSoftImageImageId().equals(image.getId()))
                                .findFirst()
                                .orElse(null);

                            if (existingImage != null) {
                                // 更新现有记录
                                existingImage.setSystemSoftImageTag(tag);
                                existingImage.setSystemSoftImageSize(image.getSize());
                                existingImage.setSystemSoftImageStatus(DockerImageStatus.AVAILABLE);
                                systemSoftImageMapper.updateById(existingImage);
                            } else {
                                // 创建新记录
                                DockerImage newImage = new DockerImage();
                                newImage.setSystemSoftId(softId);
                                newImage.setSystemSoftImageServerId(serverId);
                                newImage.setSystemSoftImageServerName("服务器-" + serverId);
                                newImage.setSystemSoftImageImageId(image.getId());
                                newImage.setSystemSoftImageName(name);
                                newImage.setSystemSoftImageTag(tag);
                                newImage.setSystemSoftImageFullName(imageFullName);
                                newImage.setSystemSoftImageSize(image.getSize());
                                newImage.setSystemSoftImageStatus(DockerImageStatus.AVAILABLE);
                                systemSoftImageMapper.insert(newImage);
                            }

                            successCount++;
                        } catch (Exception e) {
                            log.error("保存镜像信息失败 - image: {}", image.getId(), e);
                            errorCount++;
                        }
                    }

                    pushSyncProgress(operationId, serverId, "服务器-" + serverId, 100, 
                        String.format("同步完成，共 %d 个镜像", images.size()));

                } catch (Exception e) {
                    log.error("同步服务器镜像失败 - serverId: {}", serverId, e);
                    errorCount++;
                }
            }

            // 统计未启用和连接失败的服务器
            errorCount += createResult.getFailedCount();

            // 推送完成消息
            pushSyncComplete(operationId, totalImages, successCount, errorCount);

            // 返回结果（包含详细的服务器状态信息）
            Map<String, Object> result = new HashMap<>();
            result.put("operationId", operationId);
            result.put("syncCount", successCount);
            result.put("totalImages", totalImages);
            result.put("successCount", successCount);
            result.put("errorCount", errorCount);
            
            // 添加详细的服务器状态信息
            result.put("serverStatus", Map.of(
                "successServers", createResult.getSuccessClients().keySet(),
                "disabledServers", createResult.getDisabledServers(),
                "failedServers", createResult.getFailedServers(),
                "notFoundServers", createResult.getNotFoundServers()
            ));
            result.put("statusSummary", createResult.getSummary());
            
            // 如果有失败的服务器，添加详细错误信息
            if (!createResult.isAllSuccess()) {
                result.put("detailedError", createResult.getDetailedErrorMessage());
            }

            log.info("同步镜像完成 - 总数: {}, 成功: {}, 失败: {}, 服务器状态: {}", 
                totalImages, successCount, errorCount, createResult.getSummary());
            return ReturnResult.ok(result);

        } catch (Exception e) {
            log.error("同步镜像失败 - 错误信息: {}", e.getMessage(), e);
            return ReturnResult.error("同步镜像失败: " + e.getMessage());
        }
    }

    // ========= 辅助方法 =========

    /**
     * 根据镜像名称查找或创建对应的软件记录
     * 
     * @param imageName 镜像名称（例如：nginx、mysql、redis等）
     * @return 软件ID
     */
    private Integer findOrCreateSoftwareByImageName(String imageName) {
        if (!StringUtils.hasText(imageName)) {
            log.warn("镜像名称为空，无法查找或创建软件记录");
            return null;
        }

        try {
            // 1. 首先尝试通过Docker镜像名称精确匹配
            SystemSoft existingSoft = systemSoftMapper.selectByDockerImage(imageName);
            if (existingSoft != null) {
                log.debug("找到已存在的软件记录（通过镜像名称） - softId: {}, imageName: {}", existingSoft.getSystemSoftId(), imageName);
                return existingSoft.getSystemSoftId();
            }

            // 2. 提取镜像的简单名称（去除仓库前缀，如 docker.io/library/nginx -> nginx）
            String simpleName = imageName;
            if (imageName.contains("/")) {
                String[] parts = imageName.split("/");
                simpleName = parts[parts.length - 1];
            }

            // 3. 尝试通过软件代码查找（软件代码通常是镜像的简单名称）
            SystemSoft softByCode = systemSoftMapper.selectBySoftCode(simpleName);
            if (softByCode != null) {
                log.debug("找到已存在的软件记录（通过软件代码） - softId: {}, softCode: {}", softByCode.getSystemSoftId(), simpleName);
                return softByCode.getSystemSoftId();
            }

            // 4. 如果都没找到，创建新的软件记录
            log.info("未找到对应的软件记录，创建新的软件 - imageName: {}", imageName);
            SystemSoft newSoft = new SystemSoft();
            newSoft.setSystemSoftName(simpleName);
            newSoft.setSystemSoftCode(simpleName);
            newSoft.setSystemSoftDockerImage(imageName);
            newSoft.setSystemSoftDesc("从服务器镜像自动创建");
            newSoft.setSystemSoftIsOfficial(0);
            newSoft.setSystemSoftStatus(1);
            newSoft.setCreateTime(java.time.LocalDateTime.now());
            
            systemSoftMapper.insert(newSoft);
            log.info("创建软件记录成功 - softId: {}, softName: {}, dockerImage: {}", 
                newSoft.getSystemSoftId(), newSoft.getSystemSoftName(), imageName);
            
            return newSoft.getSystemSoftId();

        } catch (Exception e) {
            log.error("查找或创建软件记录失败 - imageName: {}, error: {}", imageName, e.getMessage(), e);
            // 如果创建失败，返回null，调用方需要处理这种情况
            return null;
        }
    }

    // ========= WebSocket消息推送辅助方法 =========

    private void pushExportProgress(String operationId, Integer imageId, String imageName, int progress, String message) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("operationId", operationId);
            data.put("imageId", imageId);
            data.put("imageName", imageName);
            data.put("progress", progress);
            data.put("message", message);
            dockerCallbackService.getSocketSessionTemplate().send(MonitorSocketTopics.Monitor.Docker.IMAGE_EXPORT_PROGRESS, data);
        } catch (Exception e) {
            log.warn("推送导出进度失败", e);
        }
    }

    private void pushExportComplete(String operationId, Integer imageId, String filePath) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("operationId", operationId);
            data.put("imageId", imageId);
            data.put("filePath", filePath);
            data.put("type", "export_image");
            data.put("status", "completed");
            data.put("message", "镜像导出完成");
            dockerCallbackService.getSocketSessionTemplate().send(MonitorSocketTopics.Monitor.Operation.COMPLETE, data);
        } catch (Exception e) {
            log.warn("推送导出完成消息失败", e);
        }
    }

    private void pushExportError(String operationId, Integer imageId, String error) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("operationId", operationId);
            data.put("imageId", imageId);
            data.put("type", "export_image");
            data.put("error", error);
            dockerCallbackService.getSocketSessionTemplate().send(MonitorSocketTopics.Monitor.Operation.ERROR, data);
        } catch (Exception e) {
            log.warn("推送导出错误消息失败", e);
        }
    }

    private void pushImportProgress(String operationId, Integer serverId, String filePath, int progress, String message) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("operationId", operationId);
            data.put("serverId", serverId);
            data.put("filePath", filePath);
            data.put("progress", progress);
            data.put("message", message);
            dockerCallbackService.getSocketSessionTemplate().send(MonitorSocketTopics.Monitor.Docker.IMAGE_IMPORT_PROGRESS, data);
        } catch (Exception e) {
            log.warn("推送导入进度失败", e);
        }
    }

    private void pushImportComplete(String operationId, Integer serverId) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("operationId", operationId);
            data.put("serverId", serverId);
            data.put("type", "import_image");
            data.put("status", "completed");
            data.put("message", "镜像导入完成");
            dockerCallbackService.getSocketSessionTemplate().send(MonitorSocketTopics.Monitor.Operation.COMPLETE, data);
        } catch (Exception e) {
            log.warn("推送导入完成消息失败", e);
        }
    }

    private void pushImportError(String operationId, Integer serverId, String error) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("operationId", operationId);
            data.put("serverId", serverId);
            data.put("type", "import_image");
            data.put("error", error);
            dockerCallbackService.getSocketSessionTemplate().send(MonitorSocketTopics.Monitor.Operation.ERROR, data);
        } catch (Exception e) {
            log.warn("推送导入错误消息失败", e);
        }
    }

    private void pushSyncProgress(String operationId, Integer serverId, String serverName, int progress, String message) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("operationId", operationId);
            data.put("serverId", serverId);
            data.put("serverName", serverName);
            data.put("progress", progress);
            data.put("message", message);
            dockerCallbackService.getSocketSessionTemplate().send(MonitorSocketTopics.Monitor.Software.SYNC_PROGRESS, data);
        } catch (Exception e) {
            log.warn("推送同步进度失败", e);
        }
    }

    private void pushSyncComplete(String operationId, int totalImages, int successCount, int errorCount) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("operationId", operationId);
            data.put("type", "sync_images");
            data.put("status", "completed");
            data.put("totalImages", totalImages);
            data.put("successCount", successCount);
            data.put("errorCount", errorCount);
            data.put("message", String.format("镜像同步完成，成功: %d, 失败: %d", successCount, errorCount));
            dockerCallbackService.getSocketSessionTemplate().send(MonitorSocketTopics.Monitor.Operation.COMPLETE, data);
        } catch (Exception e) {
            log.warn("推送同步完成消息失败", e);
        }
    }

}

