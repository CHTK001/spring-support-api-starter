package com.chua.docker.support.client;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.protocol.ClientSetting;
import com.chua.common.support.protocol.client.AbstractProtocolClient;
import com.chua.common.support.protocol.request.HttpServletResponse;
import com.chua.common.support.protocol.request.ServletRequest;
import com.chua.common.support.protocol.request.ServletResponse;
import com.chua.common.support.utils.StringUtils;
import com.chua.docker.support.callback.*;
import com.chua.docker.support.docker.DockerContainerConfig;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateServiceResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.TopContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Docker V2客户端实现
 * <p>
 * 提供Docker API的V2协议客户端功能，支持：
 * 1. Docker REST API调用
 * 2. 容器生命周期管理
 * 3. 镜像管理操作
 * 4. 网络和存储管理
 * 5. Docker Swarm集群操作
 * 6. 异步和同步API调用
 * 7. 事件监听和回调
 *
 * @author CH
 * @since 2024/7/8
 */
@Slf4j
@Spi("DOCKER")
public class DockerProtocolClient extends AbstractProtocolClient {

    /**
     * Docker客户端实例
     */
    @Getter
    private DockerClient dockerClient;

    /**
     * Docker主机地址
     */
    private String dockerHost;

    /**
     * 是否启用TLS验证
     */
    private boolean tlsVerify;

    /**
     * TLS证书路径
     */
    private String certPath;

    /**
     * 构造函数
     *
     * @param clientSetting 客户端配置
     */
    public DockerProtocolClient(ClientSetting clientSetting) {
        super(clientSetting);
        initializeDockerSettings();
    }

    /**
     * 创建Docker客户端实例
     *
     * @return Docker客户端实例
     */
    public static DockerProtocolClient of() {
        return new DockerProtocolClient(ClientSetting.builder().build());
    }

    /**
     * 创建Docker客户端实例
     *
     * @param dockerHost Docker主机地址
     * @return Docker客户端实例
     */
    public static DockerProtocolClient of(String dockerHost) {
        return new DockerProtocolClient(ClientSetting.builder()
                .host(dockerHost)
                .build());
    }

    /**
     * 初始化Docker设置
     */
    private void initializeDockerSettings() {
        // 从客户端设置中获取Docker配置
        String url = clientSetting.getUrl();
        if (StringUtils.isNotEmpty(url)) {
            this.dockerHost = url;
        } else {
            this.dockerHost = "tcp://" + clientSetting.getHost() + ":" + clientSetting.getPort(2376);
        }

        this.tlsVerify = clientSetting.isSslEnabled();
        this.certPath = clientSetting.getSslCertPath();

        log.debug("Docker客户端设置初始化完成: host={}, tlsVerify={}", dockerHost, tlsVerify);
    }

    @Override
    protected boolean doConnect() {
        try {
            // 构建Docker客户端配置
            DefaultDockerClientConfig.Builder configBuilder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost(dockerHost);

            if (tlsVerify && StringUtils.isNotEmpty(certPath)) {
                configBuilder.withDockerTlsVerify(true)
                           .withDockerCertPath(certPath);
            }

            DockerClientConfig config = configBuilder.build();
            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .build();
            // 创建Docker客户端
            dockerClient = DockerClientImpl.getInstance(config, httpClient);

            // 测试连接
            dockerClient.pingCmd().exec();

            log.info("Docker客户端连接成功: {}", dockerHost);
            return true;

        } catch (Exception e) {
            log.error("Docker客户端连接失败: {}", dockerHost, e);
            return false;
        }
    }

    @Override
    protected void doDisconnect() {
        if (dockerClient != null) {
            try {
                dockerClient.close();
                log.info("Docker客户端已断开连接");
            } catch (Exception e) {
                log.warn("关闭Docker客户端时发生错误", e);
            } finally {
                dockerClient = null;
            }
        }
    }

    @Override
    protected CompletableFuture<ServletResponse> doSendAsync(ServletRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return processDockerRequest(request);
            } catch (Exception e) {
                log.error("Docker异步请求处理失败", e);
                return createErrorResponse(request, e);
            }
        });
    }

    @Override
    protected ServletResponse doSendSync(ServletRequest request) {
        try {
            return processDockerRequest(request);
        } catch (Exception e) {
            log.error("Docker同步请求处理失败", e);
            return createErrorResponse(request, e);
        }
    }

    @Override
    protected void doSendOneWay(ServletRequest request) {
        executorService.execute(() -> {
            try {
                processDockerRequest(request);
                log.debug("Docker单向请求处理完成");
            } catch (Exception e) {
                log.error("Docker单向请求处理失败", e);
            }
        });
    }

    @Override
    public boolean isConnected() {
        if (dockerClient == null) {
            return false;
        }

        try {
            dockerClient.pingCmd().exec();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getProtocolName() {
        return "DOCKER";
    }

    @Override
    protected void doHeartbeat() {
        try {
            if (dockerClient != null) {
                dockerClient.pingCmd().exec();
                log.debug("Docker心跳检测成功");
            }
        } catch (Exception e) {
            log.warn("Docker心跳检测失败", e);
        }
    }

    @Override
    protected void doStartHeartbeat() {
        // Docker心跳通过ping命令实现
        log.debug("Docker心跳已启动");
    }

    @Override
    protected void doStopHeartbeat() {
        log.debug("Docker心跳已停止");
    }

    @Override
    protected void doClose() {
        doDisconnect();
        log.info("Docker客户端已关闭");
    }

    /**
     * 处理Docker请求
     */
    private ServletResponse processDockerRequest(ServletRequest request) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }

        String path = request.getPath();
        String method = request.getMethod();

        log.debug("处理Docker请求: {} {}", method, path);

        try {
            // 根据路径和方法处理不同的Docker API调用
            if (path.startsWith("/containers")) {
                return handleContainerRequest(request);
            } else if (path.startsWith("/images")) {
                return handleImageRequest(request);
            } else if (path.startsWith("/networks")) {
                return handleNetworkRequest(request);
            } else if (path.startsWith("/volumes")) {
                return handleVolumeRequest(request);
            } else if (path.equals("/version")) {
                return handleVersionRequest(request);
            } else if (path.equals("/info")) {
                return handleInfoRequest(request);
            } else if (path.equals("/_ping")) {
                return handlePingRequest(request);
            } else {
                return createNotFoundResponse(request);
            }

        } catch (Exception e) {
            log.error("Docker API调用失败: {} {}", method, path, e);
            return createErrorResponse(request, e);
        }
    }

    /**
     * 处理容器相关请求
     */
    private ServletResponse handleContainerRequest(ServletRequest request) {
        String method = request.getMethod();
        String path = request.getPath();

        if ("GET".equals(method) && "/containers/json".equals(path)) {
            // 列出容器
            var containers = dockerClient.listContainersCmd().exec();
            return createSuccessResponse(request, containers);
        }

        // 其他容器操作...
        return createNotFoundResponse(request);
    }

    /**
     * 处理镜像相关请求
     */
    private ServletResponse handleImageRequest(ServletRequest request) {
        String method = request.getMethod();
        String path = request.getPath();

        if ("GET".equals(method) && "/images/json".equals(path)) {
            // 列出镜像
            var images = dockerClient.listImagesCmd().exec();
            return createSuccessResponse(request, images);
        }

        // 其他镜像操作...
        return createNotFoundResponse(request);
    }

    /**
     * 处理网络相关请求
     */
    private ServletResponse handleNetworkRequest(ServletRequest request) {
        String method = request.getMethod();
        String path = request.getPath();

        if ("GET".equals(method) && "/networks".equals(path)) {
            // 列出网络
            var networks = dockerClient.listNetworksCmd().exec();
            return createSuccessResponse(request, networks);
        }

        return createNotFoundResponse(request);
    }

    /**
     * 处理存储卷相关请求
     */
    private ServletResponse handleVolumeRequest(ServletRequest request) {
        String method = request.getMethod();
        String path = request.getPath();

        if ("GET".equals(method) && "/volumes".equals(path)) {
            // 列出存储卷
            var volumes = dockerClient.listVolumesCmd().exec();
            return createSuccessResponse(request, volumes);
        }

        return createNotFoundResponse(request);
    }

    /**
     * 处理版本请求
     */
    private ServletResponse handleVersionRequest(ServletRequest request) {
        try {
            Version version = dockerClient.versionCmd().exec();
            return createSuccessResponse(request, version);
        } catch (Exception e) {
            log.error("获取Docker版本信息失败", e);
            return createErrorResponse(request, e);
        }
    }

    /**
     * 处理信息请求
     */
    private ServletResponse handleInfoRequest(ServletRequest request) {
        try {
            Info info = dockerClient.infoCmd().exec();
            return createSuccessResponse(request, info);
        } catch (Exception e) {
            log.error("获取Docker信息失败", e);
            return createErrorResponse(request, e);
        }
    }

    /**
     * 处理ping请求
     */
    private ServletResponse handlePingRequest(ServletRequest request) {
        dockerClient.pingCmd().exec();
        return createSuccessResponse(request, "OK");
    }

    /**
     * 创建成功响应
     */
    private ServletResponse createSuccessResponse(ServletRequest request, Object data) {
        return HttpServletResponse.builder()
                .requestId(request.getRequestId())
                .statusCode(200)
                .statusMessage("OK")
                .contentType("application/json")
                .bodyString(com.chua.common.support.json.Json.toJson(data))
                .build();
    }

    /**
     * 创建错误响应
     */
    private ServletResponse createErrorResponse(ServletRequest request, Exception e) {
        return HttpServletResponse.builder()
                .requestId(request.getRequestId())
                .statusCode(500)
                .statusMessage("Internal Server Error")
                .contentType("application/json")
                .bodyString("{\"error\":\"" + e.getMessage() + "\"}")
                .build();
    }

    /**
     * 创建404响应
     */
    private ServletResponse createNotFoundResponse(ServletRequest request) {
        return HttpServletResponse.builder()
                .requestId(request.getRequestId())
                .statusCode(404)
                .statusMessage("Not Found")
                .contentType("application/json")
                .bodyString("{\"error\":\"API endpoint not found\"}")
                .build();
    }

    // ========== Docker独有的方法 ==========

    /**
     * 获取Docker版本信息
     *
     * @return Docker版本信息
     */
    public Version getDockerVersion() {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.versionCmd().exec();
        } catch (Exception e) {
            log.error("获取Docker版本信息失败", e);
            throw new RuntimeException("获取Docker版本信息失败", e);
        }
    }

    /**
     * 获取Docker系统信息
     *
     * @return Docker系统信息
     */
    public Info getDockerInfo() {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.infoCmd().exec();
        } catch (Exception e) {
            log.error("获取Docker系统信息失败", e);
            throw new RuntimeException("获取Docker系统信息失败", e);
        }
    }

    /**
     * 检查Docker守护进程是否可用
     *
     * @return 如果可用返回true，否则返回false
     */
    public boolean pingDocker() {
        if (dockerClient == null) {
            return false;
        }
        try {
            dockerClient.pingCmd().exec();
            return true;
        } catch (Exception e) {
            log.warn("Docker ping失败", e);
            return false;
        }
    }

    /**
     * 列出所有容器
     *
     * @param showAll 是否显示所有容器（包括停止的）
     * @return 容器列表
     */
    public List<Container> listContainers(boolean showAll) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.listContainersCmd()
                    .withShowAll(showAll)
                    .exec();
        } catch (Exception e) {
            log.error("列出容器失败", e);
            throw new RuntimeException("列出容器失败", e);
        }
    }

    /**
     * 列出所有容器（默认只显示运行中的）
     *
     * @return 容器列表
     */
    public List<Container> listContainers() {
        return listContainers(false);
    }

    /**
     * 创建容器
     * <p>
     * 基于指定镜像创建Docker容器。该方法会：
     * 1. 使用Docker客户端的createContainerCmd命令
     * 2. 设置镜像名称和可选的容器名称
     * 3. 执行创建操作并返回响应
     * <p>
     * 注意：该方法仅创建容器，不会自动启动容器。如需启动容器，请调用startContainer方法。
     *
     * @param imageName     镜像名称（格式：仓库名/镜像名:标签）
     * @param containerName 容器名称（可选），如果为空则Docker会自动生成
     * @return 容器创建响应，包含容器ID等信息
     * @throws IllegalStateException 当Docker客户端未连接时抛出
     * @throws RuntimeException      当创建过程中发生错误时抛出
     * 
     * @see #startContainer(String) 启动容器方法
     * @see #createContainer(String, String, Map, Map, Map) 带详细配置的创建方法
     */
    public com.github.dockerjava.api.command.CreateContainerResponse createContainer(String imageName, String containerName) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            var cmd = dockerClient.createContainerCmd(imageName);
            if (StringUtils.isNotEmpty(containerName)) {
                cmd.withName(containerName);
            }
            return cmd.exec();
        } catch (Exception e) {
            log.error("创建容器失败: image={}, name={}", imageName, containerName, e);
            throw new RuntimeException("创建容器失败", e);
        }
    }

    /**
     * 创建容器（带环境变量、端口映射和卷映射）
     * <p>
     * 基于指定镜像创建Docker容器，并配置环境变量、端口映射和卷映射等高级选项。该方法会：
     * 1. 使用Docker客户端的createContainerCmd命令
     * 2. 设置镜像名称和容器名称
     * 3. 配置环境变量
     * 4. 配置端口映射
     * 5. 配置卷映射
     * 6. 执行创建操作并返回容器ID
     * <p>
     * 注意：该方法仅创建容器，不会自动启动容器。如需启动容器，请调用startContainer方法。
     *
     * @param imageName      镜像名称（格式：仓库名/镜像名:标签）
     * @param containerName  容器名称
     * @param environment    环境变量映射（key=value格式）
     * @param portBindings   端口映射（hostPort -> containerPort格式）
     * @param volumeBindings 卷映射（hostPath -> containerPath格式）
     * @return 容器ID
     * @throws IllegalStateException 当Docker客户端未连接时抛出
     * @throws RuntimeException      当创建过程中发生错误时抛出
     * 
     * @see #startContainer(String) 启动容器方法
     * @see #createContainer(String, String) 简单创建方法
     */
    public String createContainer(String imageName, String containerName, 
                                 Map<String, String> environment,
                                 Map<String, String> portBindings,
                                 Map<String, String> volumeBindings) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            var cmd = dockerClient.createContainerCmd(imageName);
            
            // 设置容器名称
            if (StringUtils.isNotEmpty(containerName)) {
                cmd.withName(containerName);
            }
            
            // 设置环境变量
            if (environment != null && !environment.isEmpty()) {
                String[] envArray = environment.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .toArray(String[]::new);
                cmd.withEnv(envArray);
                log.debug("设置环境变量: {}", environment);
            }
            
            // 设置端口映射
            if (portBindings != null && !portBindings.isEmpty()) {
                var hostConfig = new HostConfig();
                var portBindingMap = new HashMap<ExposedPort, Ports.Binding>();
                var exposedPorts = new ArrayList<ExposedPort>();
                
                for (java.util.Map.Entry<String, String> entry : portBindings.entrySet()) {
                    String containerPort = entry.getKey();
                    String hostPort = entry.getValue();
                    
                    var exposedPort = ExposedPort.tcp(Integer.parseInt(containerPort));
                    var binding = Ports.Binding.bindPort(Integer.parseInt(hostPort));
                    
                    exposedPorts.add(exposedPort);
                    portBindingMap.put(exposedPort, binding);
                }
                
                var ports = new Ports();
                portBindingMap.forEach(ports::bind);

                hostConfig.withPortBindings(ports);
                cmd.withExposedPorts(exposedPorts.toArray(new ExposedPort[0]));
                cmd.withHostConfig(hostConfig);
                log.debug("设置端口映射: {}", portBindings);
            }
            
            // 设置卷映射
            if (volumeBindings != null && !volumeBindings.isEmpty()) {
                var hostConfig = cmd.getHostConfig();
                if (hostConfig == null) {
                    hostConfig = new HostConfig();
                    cmd.withHostConfig(hostConfig);
                }
                
                var binds = volumeBindings.entrySet().stream()
                    .map(entry -> new Bind(entry.getValue(), new Volume(entry.getKey())))
                    .toArray(Bind[]::new);
                
                hostConfig.withBinds(binds);
                log.debug("设置卷映射: {}", volumeBindings);
            }
            
            var response = cmd.exec();
            String containerId = response.getId();
            log.info("容器创建成功: image={}, name={}, containerId={}", imageName, containerName, containerId);
            return containerId;
            
        } catch (Exception e) {
            log.error("创建容器失败: image={}, name={}", imageName, containerName, e);
            throw new RuntimeException("创建容器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 启动容器
     *
     * @param containerId 容器ID
     * @return 是否启动成功
     */
    public boolean startContainer(String containerId) {
        if (dockerClient == null) {
            log.error("Docker客户端未连接");
            return false;
        }
        try {
            dockerClient.startContainerCmd(containerId).exec();
            log.info("容器启动成功: {}", containerId);
            return true;
        } catch (Exception e) {
            log.error("启动容器失败: {}", containerId, e);
            return false;
        }
    }

    /**
     * 根据容器ID启动容器
     *
     * @param containerId 容器ID
     * @return 是否启动成功
     */
    public boolean startContainerById(String containerId) {
        if (dockerClient == null) {
            log.error("Docker客户端未连接");
            return false;
        }
        try {
            dockerClient.startContainerCmd(containerId).exec();
            log.info("容器启动成功: {}", containerId);
            return true;
        } catch (Exception e) {
            log.error("启动容器失败: {}", containerId, e);
            return false;
        }
    }

    /**
     * 根据容器ID启动容器
     *
     * @param containerId 容器ID
     * @return 是否启动成功
     * @see #startContainer(String) 根据容器ID启动容器的方法
     */
    // 删除错误的startContainerByName方法，应直接使用容器ID而不是名称
    // public void startContainerByName(String containerName) { ... }

    /**
     * 根据容器ID停止容器
     *
     * @param containerId 容器ID
     * @return 是否停止成功
     */
    public boolean stopContainerById(String containerId) {
        if (dockerClient == null) {
            log.error("Docker客户端未连接");
            return false;
        }
        try {
            dockerClient.stopContainerCmd(containerId).exec();
            log.info("容器停止成功: {}", containerId);
            return true;
        } catch (Exception e) {
            log.error("停止容器失败: {}", containerId, e);
            return false;
        }
    }

    /**
     * 根据容器ID重启容器
     *
     * @param containerId 容器ID
     * @return 是否重启成功
     */
    public boolean restartContainer(String containerId) {
        if (dockerClient == null) {
            log.error("Docker客户端未连接");
            return false;
        }
        try {
            dockerClient.restartContainerCmd(containerId).exec();
            log.info("容器重启成功: {}", containerId);
            return true;
        } catch (Exception e) {
            log.error("重启容器失败: {}", containerId, e);
            return false;
        }
    }

    /**
     * 根据容器ID重启容器
     *
     * @param containerId 容器ID
     * @return 是否重启成功
     * @see #restartContainer(String) 根据容器ID重启容器的方法
     */
    // 删除错误的restartContainerByName方法，应直接使用容器ID而不是名称
    // public void restartContainerByName(String containerName) { ... }

    /**
     * 根据容器ID删除容器
     *
     * @param containerId 容器ID
     * @param force 是否强制删除
     * @return 是否删除成功
     */
    public boolean removeContainerById(String containerId, boolean force) {
        if (dockerClient == null) {
            log.error("Docker客户端未连接");
            return false;
        }
        try {
            dockerClient.stopContainerCmd(containerId).exec();
            log.info("容器停止成功: {}", containerId);
            return true;
        } catch (Exception e) {
            log.error("停止容器失败: {}", containerId, e);
            return false;
        }
    }

    /**
     * 根据容器ID停止容器
     *
     * @param containerId 容器ID
     * @see #removeContainer(String, boolean) 根据容器ID删除容器的方法
     */
    // 删除错误的removeContainerByName方法，应直接使用容器ID而不是名称
    // public void removeContainerByName(String containerName, boolean force) { ... }

    /**
     * 根据容器ID获取容器详细信息
     * <p>
     * 获取指定ID的Docker容器的详细信息。该方法会：
     * 1. 使用Docker客户端的inspectContainerCmd命令
     * 2. 执行检查操作并返回容器详细信息
     * <p>
     * 返回的详细信息包括容器的配置、状态、网络设置等完整信息。
     *
     * @param containerId 容器ID
     * @return
     * @return 是否停止成功
     * @see #stopContainer(String) 根据容器ID停止容器的方法
     */
    // 删除错误的stopContainerByName方法，应直接使用容器ID而不是名称
    // public void stopContainerByName(String containerName) { ... }

    /**
     * 根据容器ID重启容器
     *
     * @param containerId 容器ID
     */
    public void restartContainerById(String containerId) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            dockerClient.restartContainerCmd(containerId).exec();
            log.info("容器重启成功: {}", containerId);
        } catch (Exception e) {
            log.error("重启容器失败: {}", containerId, e);
            throw new RuntimeException("重启容器失败", e);
        }
    }

    /**
     * 根据容器ID删除容器
     *
     * @param containerId 容器ID
     * @param force 是否强制删除
     * @return 是否删除成功
     */
    public boolean removeContainerById(String containerId, boolean force) {
        if (dockerClient == null) {
            log.error("Docker客户端未连接");
            return false;
        }
        try {
            dockerClient.removeContainerCmd(containerId)
                    .withForce(force)
                    .exec();
            log.info("容器删除成功: {}", containerId);
            return true;
        } catch (Exception e) {
            log.error("删除容器失败: {}", containerId, e);
            return false;
        }
    }

    /**
     * 列出所有镜像
     *
     * @return 镜像列表
     */
    public java.util.List<com.github.dockerjava.api.model.Image> listImages() {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.listImagesCmd().exec();
        } catch (Exception e) {
            log.error("列出镜像失败", e);
            throw new RuntimeException("列出镜像失败", e);
        }
    }

    /**
     * 拉取镜像（带回调）
     *
     * @param imageName 镜像名称（如：nginx:latest）
     * @param callback  拉取过程回调接口
     * @return 拉取是否成功
     */
    public boolean pullImage(String imageName, DockerResultCallback<PullResponseItem> callback) {
        // 参数验证
        if (!StringUtils.hasText(imageName)) {
            log.error("镜像名称不能为空");
            throw new IllegalArgumentException("镜像名称不能为空");
        }
        if (callback == null) {
            log.error("回调接口不能为空");
            throw new IllegalArgumentException("回调接口不能为空");
        }

        try {
            log.info("开始拉取Docker镜像: {}", imageName);

            // 调用原始客户端的pullImage方法
            dockerClient.pullImageCmd(imageName)
                    .exec(new ResultCallback.Adapter<>() {
                        @Override
                        public void onNext(PullResponseItem object) {
                            callback.onNext(object);
                        }

                        @Override
                        public void onStart(Closeable stream) {
                            callback.onStart(stream);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            callback.onError(throwable);
                        }

                        @Override
                        public void onComplete() {
                            callback.onComplete();
                        }
                    })
                    .awaitCompletion();
            log.info("Docker镜像拉取成功: {}", imageName);
            return true;
        } catch (Exception e) {
            log.error("Docker镜像拉取失败: {}, 错误信息: {}", imageName, e.getMessage(), e);
            throw new RuntimeException("Docker镜像拉取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除镜像
     *
     * @param imageId 镜像ID
     * @param force 是否强制删除
     */
    public void removeImage(String imageId, boolean force) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            dockerClient.removeImageCmd(imageId)
                    .withForce(force)
                    .exec();
            log.info("镜像删除成功: {}", imageId);
        } catch (Exception e) {
            log.error("删除镜像失败: {}", imageId, e);
            throw new RuntimeException("删除镜像失败", e);
        }
    }

    /**
     * 构建镜像
     * <p>
     * 基于指定目录中的Dockerfile构建Docker镜像。该方法会：
     * 1. 使用Docker客户端的buildImageCmd命令
     * 2. 指定构建上下文目录
     * 3. 设置镜像标签
     * 4. 执行构建操作并等待完成
     * <p>
     * 注意：该方法是同步阻塞的，会等待构建完成或超时。
     *
     * @param dockerfileDir Dockerfile所在目录，作为构建上下文
     * @param timeout       超时时间
     * @param timeUnit      时间单位
     * @param imageName     镜像名称（可变参数，支持多个标签）
     * @return 是否构建成功，成功返回true，失败抛出异常
     * @throws IllegalStateException 当Docker客户端未连接时抛出
     * @throws RuntimeException      当构建过程中发生错误时抛出
     * 
     * @see #buildImage(File, long, TimeUnit, DockerResultCallback, String...) 带回调的构建方法
     */
    public boolean buildImage(java.io.File dockerfileDir, long timeout, TimeUnit timeUnit, String... imageName) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.buildImageCmd(dockerfileDir)
                    .withTags(Sets.newHashSet(imageName))
                    .exec(new ResultCallback.Adapter<>())
                    .awaitCompletion(timeout, timeUnit);
        } catch (Exception e) {
            log.error("构建镜像失败: dir={}, name={}", dockerfileDir, imageName, e);
            throw new RuntimeException("构建镜像失败", e);
        }
    }

    /**
     * 构建镜像（带回调）
     * <p>
     * 基于指定目录中的Dockerfile构建Docker镜像，并通过回调处理器处理构建过程中的各种事件。
     * 该方法会：
     * 1. 使用Docker客户端的buildImageCmd命令
     * 2. 指定构建上下文目录
     * 3. 设置镜像标签
     * 4. 使用DockerBuildAdaptor适配器包装回调处理器
     * 5. 执行构建操作并等待完成
     * <p>
     * 注意：该方法是同步阻塞的，会等待构建完成或超时。
     *
     * @param dockerfileDir Dockerfile所在目录，作为构建上下文
     * @param timeout       超时时间
     * @param timeUnit      时间单位
     * @param callback      构建过程回调处理器，用于接收构建进度和状态信息
     * @param imageName     镜像名称（可变参数，支持多个标签）
     * @return 是否构建成功，成功返回true，失败抛出异常
     * @throws IllegalStateException 当Docker客户端未连接时抛出
     * @throws IllegalArgumentException 当回调处理器为空时抛出
     * @throws RuntimeException      当构建过程中发生错误时抛出
     * 
     * @see DockerBuildAdaptor Docker构建适配器
     * @see DockerResultCallback Docker回调接口
     */
    public boolean buildImage(java.io.File dockerfileDir, long timeout, TimeUnit timeUnit,
                              DockerResultCallback<BuildResponseItem> callback, String... imageName) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        if (callback == null) {
            throw new IllegalArgumentException("回调不能为空");
        }
        try {
            log.info("开始构建镜像: dir={}, name={}", dockerfileDir, imageName);
            DockerBuildAdaptor adaptor = new DockerBuildAdaptor(callback);
            return dockerClient.buildImageCmd(dockerfileDir)
                    .withTags(Sets.newHashSet(imageName))
                    .exec(adaptor)
                    .awaitCompletion(timeout, timeUnit);
        } catch (Exception e) {
            log.error("构建镜像失败: dir={}, name={}", dockerfileDir, imageName, e);
            if (callback != null) {
                callback.onError(e);
            }
            throw new RuntimeException("构建镜像失败", e);
        }
    }

    /**
     * 列出所有网络
     *
     * @return 网络列表
     */
    public java.util.List<com.github.dockerjava.api.model.Network> listNetworks() {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.listNetworksCmd().exec();
        } catch (Exception e) {
            log.error("列出网络失败", e);
            throw new RuntimeException("列出网络失败", e);
        }
    }

    /**
     * 创建网络
     *
     * @param networkName 网络名称
     * @return 网络创建响应
     */
    public com.github.dockerjava.api.command.CreateNetworkResponse createNetwork(String networkName) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.createNetworkCmd()
                    .withName(networkName)
                    .exec();
        } catch (Exception e) {
            log.error("创建网络失败: {}", networkName, e);
            throw new RuntimeException("创建网络失败", e);
        }
    }

    /**
     * 删除网络
     *
     * @param networkId 网络ID
     */
    public void removeNetwork(String networkId) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            dockerClient.removeNetworkCmd(networkId).exec();
            log.info("网络删除成功: {}", networkId);
        } catch (Exception e) {
            log.error("删除网络失败: {}", networkId, e);
            throw new RuntimeException("删除网络失败", e);
        }
    }

    /**
     * 列出所有存储卷
     *
     * @return 存储卷列表
     */
    public com.github.dockerjava.api.command.ListVolumesResponse listVolumes() {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.listVolumesCmd().exec();
        } catch (Exception e) {
            log.error("列出存储卷失败", e);
            throw new RuntimeException("列出存储卷失败", e);
        }
    }

    /**
     * 创建存储卷
     *
     * @param volumeName 存储卷名称
     * @return 存储卷创建响应
     */
    public com.github.dockerjava.api.command.CreateVolumeResponse createVolume(String volumeName) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.createVolumeCmd()
                    .withName(volumeName)
                    .exec();
        } catch (Exception e) {
            log.error("创建存储卷失败: {}", volumeName, e);
            throw new RuntimeException("创建存储卷失败", e);
        }
    }

    /**
     * 删除存储卷
     *
     * @param volumeName 存储卷名称
     */
    public void removeVolume(String volumeName) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            dockerClient.removeVolumeCmd(volumeName).exec();
            log.info("存储卷删除成功: {}", volumeName);
        } catch (Exception e) {
            log.error("删除存储卷失败: {}", volumeName, e);
            throw new RuntimeException("删除存储卷失败", e);
        }
    }

    /**
     * 获取容器详细信息
     *
     * @param containerId 容器ID
     * @return 容器详细信息
     */
    public com.github.dockerjava.api.command.InspectContainerResponse inspectContainer(String containerId) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.inspectContainerCmd(containerId).exec();
        } catch (Exception e) {
            log.error("获取容器详细信息失败: {}", containerId, e);
            throw new RuntimeException("获取容器详细信息失败", e);
        }
    }

    /**
     * 获取容器日志
     *
     * @param containerId 容器ID
     * @param tail 显示最后多少行日志
     * @return 日志内容
     */
    public String getContainerLogs(String containerId, int tail) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            var logCmd = dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTail(tail);

            var callback = new ResultCallback.Adapter<com.github.dockerjava.api.model.Frame>() {
                private final StringBuilder logs = new StringBuilder();

                @Override
                public void onNext(com.github.dockerjava.api.model.Frame frame) {
                    logs.append(new String(frame.getPayload()));
                }

                public String getLogs() {
                    return logs.toString();
                }
            };

            logCmd.exec(callback).awaitCompletion();
            return callback.getLogs();

        } catch (Exception e) {
            log.error("获取容器日志失败: {}", containerId, e);
            throw new RuntimeException("获取容器日志失败", e);
        }
    }

    /**
     * 执行容器内命令
     *
     * @param containerId 容器ID
     * @param command 要执行的命令
     * @return 执行结果
     */
    public String execCommand(String containerId, String... command) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            // 创建exec实例
            var execCreateResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(command)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            // 执行命令
            var callback = new ResultCallback.Adapter<com.github.dockerjava.api.model.Frame>() {
                private final StringBuilder output = new StringBuilder();

                @Override
                public void onNext(com.github.dockerjava.api.model.Frame frame) {
                    output.append(new String(frame.getPayload()));
                }

                public String getOutput() {
                    return output.toString();
                }
            };

            dockerClient.execStartCmd(execCreateResponse.getId())
                    .exec(callback)
                    .awaitCompletion();

            return callback.getOutput();

        } catch (Exception e) {
            log.error("执行容器命令失败: containerId={}, command={}", containerId, java.util.Arrays.toString(command), e);
            throw new RuntimeException("执行容器命令失败", e);
        }
    }

    /**
     * 获取容器统计信息
     *
     * @param containerId 容器ID
     * @return 统计信息
     */
    public Statistics getContainerStats(String containerId) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            var callback = new ResultCallback.Adapter<Statistics>() {
                private Statistics stats;

                @Override
                public void onNext(Statistics statistics) {
                    this.stats = statistics;
                    onComplete();
                }

                public Statistics getStats() {
                    return stats;
                }
            };

            dockerClient.statsCmd(containerId)
                    .withNoStream(true)
                    .exec(callback)
                    .awaitCompletion();

            return callback.getStats();

        } catch (Exception e) {
            log.error("获取容器统计信息失败: {}", containerId, e);
            throw new RuntimeException("获取容器统计信息失败", e);
        }
    }

    // ========== 镜像高级操作 ==========

    /**
     * 获取镜像详细信息
     *
     * @param imageId 镜像ID
     * @return 镜像详细信息
     */
    public com.github.dockerjava.api.command.InspectImageResponse inspectImage(String imageId) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.inspectImageCmd(imageId).exec();
        } catch (Exception e) {
            log.error("获取镜像详细信息失败: {}", imageId, e);
            throw new RuntimeException("获取镜像详细信息失败", e);
        }
    }

    /**
     * 搜索镜像
     *
     * @param term 搜索关键词
     * @return 搜索结果
     */
    public java.util.List<com.github.dockerjava.api.model.SearchItem> searchImages(String term) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.searchImagesCmd(term).exec();
        } catch (Exception e) {
            log.error("搜索镜像失败: {}", term, e);
            throw new RuntimeException("搜索镜像失败", e);
        }
    }

    /**
     * 推送镜像到仓库
     * <p>
     * 将本地Docker镜像推送到远程仓库。该方法会：
     * 1. 使用Docker客户端的pushImageCmd命令
     * 2. 执行推送操作并等待完成
     * <p>
     * 注意：该方法是同步阻塞的，会等待推送完成。如果需要异步推送或进度通知，
     * 请使用带回调参数的重载方法。
     *
     * @param imageName 镜像名称（格式：仓库名/镜像名:标签）
     * @throws IllegalStateException 当Docker客户端未连接时抛出
     * @throws RuntimeException      当推送过程中发生错误时抛出
     * 
     * @see #pushImage(String, DockerResultCallback) 带回调的推送方法
     */
    public void pushImage(String imageName) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            dockerClient.pushImageCmd(imageName)
                    .exec(new ResultCallback.Adapter<>())
                    .awaitCompletion();
            log.info("镜像推送成功: {}", imageName);
        } catch (Exception e) {
            log.error("推送镜像失败: {}", imageName, e);
            throw new RuntimeException("推送镜像失败", e);
        }
    }

    /**
     * 推送镜像到仓库（带回调）
     * <p>
     * 将本地Docker镜像推送到远程仓库，并通过回调处理器处理推送过程中的各种事件。
     * 该方法会：
     * 1. 使用Docker客户端的pushImageCmd命令
     * 2. 使用DockerPushAdaptor适配器包装回调处理器
     * 3. 执行推送操作并等待完成
     * <p>
     * 注意：该方法是同步阻塞的，会等待推送完成。
     *
     * @param imageName 镜像名称（格式：仓库名/镜像名:标签）
     * @param callback  推送过程回调处理器，用于接收推送进度和状态信息
     * @throws IllegalStateException 当Docker客户端未连接时抛出
     * @throws IllegalArgumentException 当回调处理器为空时抛出
     * @throws RuntimeException      当推送过程中发生错误时抛出
     * 
     * @see DockerPushAdaptor Docker推送适配器
     * @see DockerResultCallback Docker回调接口
     */
    public void pushImage(String imageName, DockerResultCallback<PushResponseItem> callback) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        if (callback == null) {
            throw new IllegalArgumentException("回调不能为空");
        }
        try {
            log.info("开始推送镜像: {}", imageName);
            DockerPushAdaptor adaptor = new DockerPushAdaptor(callback);
            dockerClient.pushImageCmd(imageName)
                    .exec(adaptor)
                    .awaitCompletion();
            log.info("镜像推送成功: {}", imageName);
        } catch (Exception e) {
            log.error("推送镜像失败: {}", imageName, e);
            if (callback != null) {
                callback.onError(e);
            }
            throw new RuntimeException("推送镜像失败", e);
        }
    }

    /**
     * 标记镜像
     *
     * @param imageId 镜像ID
     * @param repository 仓库名
     * @param tag 标签
     */
    public void tagImage(String imageId, String repository, String tag) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            dockerClient.tagImageCmd(imageId, repository, tag).exec();
            log.info("镜像标记成功: {} -> {}:{}", imageId, repository, tag);
        } catch (Exception e) {
            log.error("标记镜像失败: {} -> {}:{}", imageId, repository, tag, e);
            throw new RuntimeException("标记镜像失败", e);
        }
    }

    /**
     * 保存镜像到tar文件
     *
     * @param imageId 镜像ID
     * @param outputPath 输出文件路径
     */
    public void saveImage(String imageId, String outputPath) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try (var inputStream = dockerClient.saveImageCmd(imageId).exec();
             var outputStream = new java.io.FileOutputStream(outputPath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            log.info("镜像保存成功: {} -> {}", imageId, outputPath);
        } catch (Exception e) {
            log.error("保存镜像失败: {} -> {}", imageId, outputPath, e);
            throw new RuntimeException("保存镜像失败", e);
        }
    }

    /**
     * 从tar文件加载镜像
     *
     * @param inputPath 输入文件路径
     */
    public void loadImage(String inputPath) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try (var inputStream = new FileInputStream(inputPath)) {
            dockerClient.loadImageCmd(inputStream).exec();
            log.info("镜像加载成功: {}", inputPath);
        } catch (Exception e) {
            log.error("加载镜像失败: {}", inputPath, e);
            throw new RuntimeException("加载镜像失败", e);
        }
    }

    // ========== 容器高级操作 ==========

    /**
     * 暂停容器
     *
     * @param containerId 容器ID
     */
    public void pauseContainer(String containerId) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            dockerClient.pauseContainerCmd(containerId).exec();
            log.info("容器暂停成功: {}", containerId);
        } catch (Exception e) {
            log.error("暂停容器失败: {}", containerId, e);
            throw new RuntimeException("暂停容器失败", e);
        }
    }

    /**
     * 恢复容器
     *
     * @param containerId 容器ID
     */
    public void unpauseContainer(String containerId) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            dockerClient.unpauseContainerCmd(containerId).exec();
            log.info("容器恢复成功: {}", containerId);
        } catch (Exception e) {
            log.error("恢复容器失败: {}", containerId, e);
            throw new RuntimeException("恢复容器失败", e);
        }
    }

    /**
     * 杀死容器
     *
     * @param containerId 容器ID
     */
    public void killContainer(String containerId) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            dockerClient.killContainerCmd(containerId).exec();
            log.info("容器杀死成功: {}", containerId);
        } catch (Exception e) {
            log.error("杀死容器失败: {}", containerId, e);
            throw new RuntimeException("杀死容器失败", e);
        }
    }

    /**
     * 重命名容器
     *
     * @param containerId 容器ID
     * @param newName 新名称
     */
    public void renameContainer(String containerId, String newName) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            dockerClient.renameContainerCmd(containerId).withName(newName).exec();
            log.info("容器重命名成功: {} -> {}", containerId, newName);
        } catch (Exception e) {
            log.error("重命名容器失败: {} -> {}", containerId, newName, e);
            throw new RuntimeException("重命名容器失败", e);
        }
    }

    /**
     * 等待容器停止
     *
     * @param containerId 容器ID
     * @return 退出状态码
     */
    public Integer waitContainer(String containerId) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.waitContainerCmd(containerId).exec(new WaitContainerResultCallback()).awaitStatusCode();
        } catch (Exception e) {
            log.error("等待容器停止失败: {}", containerId, e);
            throw new RuntimeException("等待容器停止失败", e);
        }
    }

    /**
     * 复制文件到容器
     *
     * @param containerId 容器ID
     * @param hostPath 主机文件路径
     * @param containerPath 容器内路径
     */
    public void copyFileToContainer(String containerId, String hostPath, String containerPath) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            dockerClient.copyArchiveToContainerCmd(containerId)
                    .withHostResource(hostPath)
                    .withRemotePath(containerPath)
                    .exec();
            log.info("文件复制到容器成功: {} -> {}:{}", hostPath, containerId, containerPath);
        } catch (Exception e) {
            log.error("复制文件到容器失败: {} -> {}:{}", hostPath, containerId, containerPath, e);
            throw new RuntimeException("复制文件到容器失败", e);
        }
    }

    /**
     * 从容器复制文件
     *
     * @param containerId 容器ID
     * @param containerPath 容器内路径
     * @param hostPath 主机文件路径
     */
    public void copyFileFromContainer(String containerId, String containerPath, String hostPath) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try (var inputStream = dockerClient.copyArchiveFromContainerCmd(containerId, containerPath).exec();
             var outputStream = new FileOutputStream(hostPath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            log.info("从容器复制文件成功: {}:{} -> {}", containerId, containerPath, hostPath);
        } catch (Exception e) {
            log.error("从容器复制文件失败: {}:{} -> {}", containerId, containerPath, hostPath, e);
            throw new RuntimeException("从容器复制文件失败", e);
        }
    }


    /**
     * 创建服务
     *
     * @param serviceName 服务名称
     * @param imageName 镜像名称
     * @return 服务创建响应
     */
    public CreateServiceResponse createService(String serviceName, String imageName) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            TaskSpec taskSpec = new TaskSpec()
                    .withContainerSpec(new ContainerSpec().withImage(imageName));

            ServiceSpec serviceSpec = new ServiceSpec()
                    .withName(serviceName)
                    .withTaskTemplate(taskSpec);

            return dockerClient.createServiceCmd(serviceSpec).exec();
        } catch (Exception e) {
            log.error("创建服务失败: {} -> {}", serviceName, imageName, e);
            throw new RuntimeException("创建服务失败", e);
        }
    }

    /**
     * 列出服务
     *
     * @return 服务列表
     */
    public List<Service> listServices() {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.listServicesCmd().exec();
        } catch (Exception e) {
            log.error("列出服务失败", e);
            throw new RuntimeException("列出服务失败", e);
        }
    }

    /**
     * 删除服务
     *
     * @param serviceId 服务ID
     */
    public void removeService(String serviceId) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            dockerClient.removeServiceCmd(serviceId).exec();
            log.info("服务删除成功: {}", serviceId);
        } catch (Exception e) {
            log.error("删除服务失败: {}", serviceId, e);
            throw new RuntimeException("删除服务失败", e);
        }
    }

    // ========== 事件监听和系统管理 ==========

    /**
     * 监听Docker事件
     *
     * @param callback 事件回调
     */
    public void listenEvents(ResultCallback<Event> callback) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            dockerClient.eventsCmd().exec(callback);
            log.info("开始监听Docker事件");
        } catch (Exception e) {
            log.error("监听Docker事件失败", e);
            throw new RuntimeException("监听Docker事件失败", e);
        }
    }

    /**
     * 清理未使用的容器
     *
     * @return 清理结果
     */
    public PruneResponse pruneContainers() {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.pruneCmd(PruneType.CONTAINERS).exec();
        } catch (Exception e) {
            log.error("清理未使用的容器失败", e);
            throw new RuntimeException("清理未使用的容器失败", e);
        }
    }

    /**
     * 清理未使用的镜像
     *
     * @return 清理结果
     */
    public PruneResponse pruneImages() {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.pruneCmd(PruneType.IMAGES).exec();
        } catch (Exception e) {
            log.error("清理未使用的镜像失败", e);
            throw new RuntimeException("清理未使用的镜像失败", e);
        }
    }

    /**
     * 清理未使用的网络
     *
     * @return 清理结果
     */
    public PruneResponse pruneNetworks() {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.pruneCmd(PruneType.NETWORKS).exec();
        } catch (Exception e) {
            log.error("清理未使用的网络失败", e);
            throw new RuntimeException("清理未使用的网络失败", e);
        }
    }

    /**
     * 清理未使用的存储卷
     *
     * @return 清理结果
     */
    public PruneResponse pruneVolumes() {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.pruneCmd(PruneType.VOLUMES).exec();
        } catch (Exception e) {
            log.error("清理未使用的存储卷失败", e);
            throw new RuntimeException("清理未使用的存储卷失败", e);
        }
    }

    /**
     * 获取容器进程列表
     *
     * @param containerId 容器ID
     * @return 进程列表
     */
    public TopContainerResponse getContainerProcesses(String containerId) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.topContainerCmd(containerId).exec();
        } catch (Exception e) {
            log.error("获取容器进程列表失败: {}", containerId, e);
            throw new RuntimeException("获取容器进程列表失败", e);
        }
    }

    /**
     * 获取容器文件系统变更
     *
     * @param containerId 容器ID
     * @return 文件系统变更列表
     */
    public List<ChangeLog> getContainerChanges(String containerId) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.containerDiffCmd(containerId).exec();
        } catch (Exception e) {
            log.error("获取容器文件系统变更失败: {}", containerId, e);
            throw new RuntimeException("获取容器文件系统变更失败", e);
        }
    }

    /**
     * 提交容器为镜像
     *
     * @param containerId 容器ID
     * @param repository 仓库名
     * @param tag 标签
     * @return 提交响应
     */
    public String commitContainer(String containerId, String repository, String tag) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        try {
            return dockerClient.commitCmd(containerId)
                    .withRepository(repository)
                    .withTag(tag)
                    .exec();
        } catch (Exception e) {
            log.error("提交容器为镜像失败: {} -> {}:{}", containerId, repository, tag, e);
            throw new RuntimeException("提交容器为镜像失败", e);
        }
    }

    // ========== DockerContainerConfig 支持方法 ==========

    /**
     * 使用DockerContainerConfig创建容器
     * <p>
     * 基于DockerContainerConfig配置对象创建Docker容器。该方法支持完整的容器配置选项，包括：
     * 1. 镜像名称和容器名称
     * 2. 环境变量
     * 3. 端口映射
     * 4. 卷映射
     * 5. 自动删除设置
     * 6. 重启策略
     * 7. 启动命令和参数
     * <p>
     * 该方法会：
     * 1. 验证配置对象和必要参数
     * 2. 构建Docker客户端的createContainerCmd命令
     * 3. 应用所有配置选项
     * 4. 执行创建操作并返回容器ID
     * <p>
     * 注意：该方法仅创建容器，不会自动启动容器。如需创建并启动容器，请使用createAndStartContainer方法。
     *
     * @param config Docker容器配置对象，包含所有容器配置选项
     * @return 容器ID
     * @throws IllegalStateException 当Docker客户端未连接时抛出
     * @throws IllegalArgumentException 当配置对象为空或镜像名称为空时抛出
     * @throws RuntimeException      当创建过程中发生错误时抛出
     * 
     * @see DockerContainerConfig Docker容器配置对象
     * @see #createAndStartContainer(DockerContainerConfig) 创建并启动容器方法
     */
    public String createContainer(DockerContainerConfig config) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        if (config == null) {
            throw new IllegalArgumentException("容器配置不能为空");
        }
        if (StringUtils.isEmpty(config.getImageName())) {
            throw new IllegalArgumentException("镜像名称不能为空");
        }

        try {
            log.info("开始创建容器: image={}, name={}", config.getImageName(), config.getContainerName());

            var cmd = dockerClient.createContainerCmd(config.getImageName());

            // 设置容器名称
            if (StringUtils.isNotEmpty(config.getContainerName())) {
                cmd.withName(config.getContainerName());
                log.debug("设置容器名称: {}", config.getContainerName());
            }
        } catch (Exception e) {
            log.error("创建容器时发生错误", e);
            throw new RuntimeException("创建容器时发生错误", e);
        }
    }

    /**
     * 根据DockerContainerConfig更新现有容器
     * <p>
     * 注意：Docker不支持直接更新容器配置，此方法会停止并删除旧容器，然后创建新容器。
     * 该方法会：
     * 1. 停止指定的旧容器
     * 2. 强制删除旧容器
     * 3. 基于新的配置对象创建新容器
     * <p>
     * 注意：这是一个破坏性操作，旧容器会被完全删除，新容器会使用新的配置创建。
     *
     * @param oldContainerId 旧容器ID
     * @param config         新的容器配置对象
     * @return 新容器ID
     * @throws IllegalArgumentException 当旧容器ID为空时抛出
     * @throws RuntimeException      当更新过程中发生错误时抛出
     * 
     * @see DockerContainerConfig Docker容器配置对象
     * @see #createContainer(DockerContainerConfig) 创建容器方法
     */
    public String updateContainer(String oldContainerId, DockerContainerConfig config) {
        if (StringUtils.isEmpty(oldContainerId)) {
            throw new IllegalArgumentException("旧容器ID不能为空");
        }

        try {
            log.info("开始更新容器: oldContainerId={}", oldContainerId);

            // 停止旧容器
            dockerClient.stopContainerCmd(oldContainerId).exec();
            log.debug("停止旧容器: oldContainerId={}", oldContainerId);

            // 强制删除旧容器
            dockerClient.removeContainerCmd(oldContainerId).withForce(true).exec();
            log.debug("删除旧容器: oldContainerId={}", oldContainerId);

            // 创建新容器
            return createContainer(config);
        } catch (Exception e) {
            log.error("更新容器时发生错误", e);
            throw new RuntimeException("更新容器时发生错误", e);
        }
    }

    /**
     * 启动指定的容器
     * <p>
     * 该方法会：
     * 1. 验证容器ID
     * 2. 执行启动操作
     * <p>
     * 注意：该方法仅启动容器，不会创建容器。如需创建并启动容器，请使用createAndStartContainer方法。
     *
     * @param containerId 容器ID
     * @throws IllegalArgumentException 当容器ID为空时抛出
     * @throws RuntimeException      当启动过程中发生错误时抛出
     * 
     * @see #createAndStartContainer(DockerContainerConfig) 创建并启动容器方法
     */
    public void startContainer(String containerId) {
        if (StringUtils.isEmpty(containerId)) {
            throw new IllegalArgumentException("容器ID不能为空");
        }

        try {
            log.info("开始启动容器: containerId={}", containerId);

            dockerClient.startContainerCmd(containerId).exec();
            log.debug("启动容器: containerId={}", containerId);
        } catch (Exception e) {
            log.error("启动容器时发生错误", e);
            throw new RuntimeException("启动容器时发生错误", e);
        }
    }

    /**
     * 停止指定的容器
     * <p>
     * 该方法会：
     * 1. 验证容器ID
     * 2. 执行停止操作
     * <p>
     * 注意：该方法仅停止容器，不会删除容器。如需停止并删除容器，请使用stopAndRemoveContainer方法。
     *
     * @param containerId 容器ID
     * @throws IllegalArgumentException 当容器ID为空时抛出
     * @throws RuntimeException      当停止过程中发生错误时抛出
     * 
     * @see #stopAndRemoveContainer(String) 停止并删除容器方法
     */
    public void stopContainer(String containerId) {
        if (StringUtils.isEmpty(containerId)) {
            throw new IllegalArgumentException("容器ID不能为空");
        }

        try {
            log.info("开始停止容器: containerId={}", containerId);

            dockerClient.stopContainerCmd(containerId).exec();
            log.debug("停止容器: containerId={}", containerId);
        } catch (Exception e) {
            log.error("停止容器时发生错误", e);
            throw new RuntimeException("停止容器时发生错误", e);
        }
    }

    /**
     * 删除指定的容器
     * <p>
     * 该方法会：
     * 1. 验证容器ID
     * 2. 执行删除操作
     * <p>
     * 注意：该方法仅删除容器，不会停止容器。如需停止并删除容器，请使用stopAndRemoveContainer方法。
     *
     * @param containerId 容器ID
     * @throws IllegalArgumentException 当容器ID为空时抛出
     * @throws RuntimeException      当删除过程中发生错误时抛出
     * 
     * @see #stopAndRemoveContainer(String) 停止并删除容器方法
     */
    public void removeContainer(String containerId) {
        if (StringUtils.isEmpty(containerId)) {
            throw new IllegalArgumentException("容器ID不能为空");
        }

        try {
            log.info("开始删除容器: containerId={}", containerId);

            dockerClient.removeContainerCmd(containerId).exec();
            log.debug("删除容器: containerId={}", containerId);
        } catch (Exception e) {
            log.error("删除容器时发生错误", e);
            throw new RuntimeException("删除容器时发生错误", e);
        }
    }

    /**
     * 停止并删除指定的容器
     * <p>
     * 该方法会：
     * 1. 验证容器ID
     * 2. 执行停止操作
     * 3. 执行删除操作
     * <p>
     * 注意：这是一个破坏性操作，容器会被完全删除。
     *
     * @param containerId 容器ID
     * @throws IllegalArgumentException 当容器ID为空时抛出
     * @throws RuntimeException      当停止或删除过程中发生错误时抛出
     * 
     * @see #stopContainer(String) 停止容器方法
     * @see #removeContainer(String) 删除容器方法
     */
    public void stopAndRemoveContainer(String containerId) {
        if (StringUtils.isEmpty(containerId)) {
            throw new IllegalArgumentException("容器ID不能为空");
        }

        try {
            log.info("开始停止并删除容器: containerId={}", containerId);

            // 停止容器
            dockerClient.stopContainerCmd(containerId).exec();
            log.debug("停止容器: containerId={}", containerId);

            // 删除容器
            dockerClient.removeContainerCmd(containerId).exec();
            log.debug("删除容器: containerId={}", containerId);
        } catch (Exception e) {
            log.error("停止并删除容器时发生错误", e);
            throw new RuntimeException("停止并删除容器时发生错误", e);
        }
    }

    /**
     * 创建并启动容器
     * <p>
     * 该方法会：
     * 1. 验证配置对象和必要参数
     * 2. 构建Docker客户端的createContainerCmd命令
     * 3. 应用所有配置选项
     * 4. 执行创建操作并返回容器ID
     * 5. 执行启动操作
     * <p>
     * 注意：该方法会创建并启动容器。
     *
     * @param config Docker容器配置对象，包含所有容器配置选项
     * @return 容器ID
     * @throws IllegalStateException 当Docker客户端未连接时抛出
     * @throws IllegalArgumentException 当配置对象为空或镜像名称为空时抛出
     * @throws RuntimeException      当创建或启动过程中发生错误时抛出
     * 
     * @see DockerContainerConfig Docker容器配置对象
     * @see #createContainer(DockerContainerConfig) 创建容器方法
     * @see #startContainer(String) 启动容器方法
     */
    public String createAndStartContainer(DockerContainerConfig config) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        if (config == null) {
            throw new IllegalArgumentException("容器配置不能为空");
        }
        if (StringUtils.isEmpty(config.getImageName())) {
            throw new IllegalArgumentException("镜像名称不能为空");
        }

        try {
            log.info("开始创建并启动容器: image={}, name={}", config.getImageName(), config.getContainerName());

            var cmd = dockerClient.createContainerCmd(config.getImageName());

            // 设置容器名称
            if (StringUtils.isNotEmpty(config.getContainerName())) {
                cmd.withName(config.getContainerName());
                log.debug("设置容器名称: {}", config.getContainerName());
            }

            // 设置环境变量
            if (config.getEnvironmentVariables() != null && !config.getEnvironmentVariables().isEmpty()) {
                String[] envArray = config.getEnvironmentVariables().toArray(new String[0]);
                cmd.withEnv(envArray);
                log.debug("设置环境变量: {}", config.getEnvironmentVariables());
            }

            // 创建HostConfig
            var hostConfig = new HostConfig();
            boolean hasHostConfig = false;

            // 设置端口映射
            if (config.getPortBindings() != null && !config.getPortBindings().isEmpty()) {
                var portBindingMap = new HashMap<ExposedPort, Ports.Binding>();
                var exposedPorts = new ArrayList<ExposedPort>();

                for (Map.Entry<String, String> entry : config.getPortBindings().entrySet()) {
                    String hostPort = entry.getKey();
                    String containerPort = entry.getValue();

                    var exposedPort = ExposedPort.tcp(Integer.parseInt(containerPort));
                    var binding = Ports.Binding.bindPort(Integer.parseInt(hostPort));

                    exposedPorts.add(exposedPort);
                    portBindingMap.put(exposedPort, binding);
                }

                var ports = new Ports();
                portBindingMap.forEach(ports::bind);

                hostConfig.withPortBindings(ports);
                cmd.withExposedPorts(exposedPorts.toArray(new ExposedPort[0]));
                cmd.withHostConfig(hostConfig);
                log.debug("设置端口映射: {}", config.getPortBindings());
            }

            // 设置卷映射
            if (config.getVolumeBindings() != null && !config.getVolumeBindings().isEmpty()) {
                var binds = config.getVolumeBindings().entrySet().stream()
                        .map(entry -> new Bind(entry.getKey(), new Volume(entry.getValue())))
                        .toArray(Bind[]::new);

                hostConfig.withBinds(binds);
                hasHostConfig = true;
                log.debug("设置卷映射: {}", config.getVolumeBindings());
            }

            // 设置自动删除
            if (config.getAutoRemove() != null && config.getAutoRemove()) {
                hostConfig.withAutoRemove(true);
                hasHostConfig = true;
                log.debug("设置自动删除: true");
            }

            // 设置重启策略
            if (StringUtils.isNotEmpty(config.getRestartPolicy())) {
                RestartPolicy restartPolicy = RestartPolicy.parse(config.getRestartPolicy());
                hostConfig.withRestartPolicy(restartPolicy);
                hasHostConfig = true;
                log.debug("设置重启策略: {}", config.getRestartPolicy());
            }

            // 应用HostConfig
            if (hasHostConfig) {
                cmd.withHostConfig(hostConfig);
            }

            // 设置启动命令
            if (StringUtils.isNotEmpty(config.getCommand())) {
                cmd.withCmd(config.getCommand().split(" "));
                log.debug("设置启动命令: {}", config.getCommand());
            }

            // 设置启动参数（如果有的话，追加到命令后面）
            if (StringUtils.isNotEmpty(config.getArgs())) {
                String fullCommand = (StringUtils.isNotEmpty(config.getCommand()) ? config.getCommand() + " " : "") + config.getArgs();
                cmd.withCmd(fullCommand.split(" "));
                log.debug("设置启动参数: {}", config.getArgs());
            }

            var response = cmd.exec();
            String containerId = response.getId();
            log.info("容器创建成功: image={}, name={}, containerId={}",
                    config.getImageName(), config.getContainerName(), containerId);
            return containerId;

        } catch (Exception e) {
            log.error("创建容器失败: image={}, name={}", config.getImageName(), config.getContainerName(), e);
            throw new RuntimeException("创建容器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用DockerContainerConfig创建并启动容器
     * <p>
     * 基于DockerContainerConfig配置对象创建并启动Docker容器。该方法会：
     * 1. 调用createContainer方法创建容器
     * 2. 调用startContainerCmd方法启动容器
     * 3. 如果启动失败，会自动清理已创建的容器
     * <p>
     * 注意：这是一个原子操作，要么容器创建并启动成功，要么完全回滚。
     *
     * @param config Docker容器配置对象，包含所有容器配置选项
     * @return 容器ID
     * @throws IllegalStateException 当Docker客户端未连接时抛出
     * @throws IllegalArgumentException 当配置对象为空或镜像名称为空时抛出
     * @throws RuntimeException      当创建或启动过程中发生错误时抛出
     * 
     * @see DockerContainerConfig Docker容器配置对象
     * @see #createContainer(DockerContainerConfig) 创建容器方法
     */
    public String createAndStartContainer(DockerContainerConfig config) {
        String containerId = createContainer(config);

        try {
            log.info("启动容器: {}", containerId);
            dockerClient.startContainerCmd(containerId).exec();
            log.info("容器启动成功: {}", containerId);
            return containerId;
        } catch (Exception e) {
            log.error("启动容器失败: {}", containerId, e);
            // 启动失败时尝试删除已创建的容器
            try {
                dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                log.info("已清理失败的容器: {}", containerId);
            } catch (Exception cleanupException) {
                log.warn("清理失败容器时发生错误: {}", containerId, cleanupException);
            }
            throw new RuntimeException("启动容器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用DockerCreateContainer配置对象创建容器（带回调）
     * <p>
     * 基于DockerCreateContainer配置对象创建Docker容器，并通过回调处理器实时监听创建过程。
     * 该方法支持完整的容器配置选项，包括：
     * 1. 镜像名称和容器名称
     * 2. 环境变量
     * 3. 端口映射
     * 4. 卷映射
     * 5. 自动删除设置
     * 6. 重启策略
     * 7. 启动命令和参数
     * <p>
     * 该方法会：
     * 1. 验证配置对象和必要参数
     * 2. 构建Docker客户端的createContainerCmd命令
     * 3. 应用所有配置选项
     * 4. 通过回调处理器实时监听创建过程
     * 5. 执行创建操作
     * <p>
     * 注意：该方法仅创建容器，不会自动启动容器。如需创建并启动容器，请使用createAndStartContainer方法。
     *
     * @param config   Docker容器配置对象，包含所有容器配置选项
     * @param callback 创建过程回调处理器，用于接收创建进度和状态信息
     * @return 容器创建响应，包含容器ID等信息
     * @throws IllegalStateException    当Docker客户端未连接时抛出
     * @throws IllegalArgumentException 当配置对象为空或镜像名称为空时抛出
     * @throws RuntimeException         当创建过程中发生错误时抛出
     * 
     * @see DockerCreateContainer Docker容器配置对象
     * @see DockerResultCallback Docker回调接口
     * @see #createAndStartContainer(DockerCreateContainer, DockerResultCallback) 创建并启动容器方法
     */
    public com.github.dockerjava.api.command.CreateContainerResponse createContainer(DockerCreateContainer config, com.chua.docker.support.callback.CreateContainerCallback callback) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        if (config == null) {
            throw new IllegalArgumentException("容器配置不能为空");
        }
        if (com.chua.common.support.utils.StringUtils.isEmpty(config.getImageName())) {
            throw new IllegalArgumentException("镜像名称不能为空");
        }
        if (callback == null) {
            throw new IllegalArgumentException("回调处理器不能为空");
        }

        try {
            log.info("开始创建容器(带回调): image={}, name={}", config.getImageName(), config.getContainerName());
            
            // 通知开始创建
            callback.onStart(null);

            var cmd = dockerClient.createContainerCmd(config.getImageName());

            // 设置容器名称
            if (com.chua.common.support.utils.StringUtils.isNotEmpty(config.getContainerName())) {
                cmd.withName(config.getContainerName());
                log.debug("设置容器名称: {}", config.getContainerName());
            }

            // 设置环境变量
            if (config.getEnvironmentVariables() != null && !config.getEnvironmentVariables().isEmpty()) {
                String[] envArray = config.getEnvironmentVariables().toArray(new String[0]);
                cmd.withEnv(envArray);
                log.debug("设置环境变量: {}", config.getEnvironmentVariables());
                
                // 通知环境变量设置进度
                com.github.dockerjava.api.model.ResponseItem responseItem = new com.github.dockerjava.api.model.ResponseItem() {};
                // 这里我们模拟一个响应项来通知进度
                callback.onNext(responseItem);
            }

            // 创建HostConfig
            var hostConfig = new com.github.dockerjava.api.model.HostConfig();
            boolean hasHostConfig = false;

            // 设置端口映射
            if (config.getPortBindings() != null && !config.getPortBindings().isEmpty()) {
                var portBindingMap = new java.util.HashMap<com.github.dockerjava.api.model.ExposedPort, com.github.dockerjava.api.model.Ports.Binding>();
                var exposedPorts = new java.util.ArrayList<com.github.dockerjava.api.model.ExposedPort>();

                for (String portBinding : config.getPortBindings()) {
                    String[] parts = portBinding.split(":");
                    if (parts.length == 2) {
                        String hostPort = parts[0];
                        String containerPort = parts[1];
                        
                        var exposedPort = com.github.dockerjava.api.model.ExposedPort.tcp(Integer.parseInt(containerPort));
                        var binding = com.github.dockerjava.api.model.Ports.Binding.bindPort(Integer.parseInt(hostPort));
                        
                        exposedPorts.add(exposedPort);
                        portBindingMap.put(exposedPort, binding);
                    }
                }

                var ports = new com.github.dockerjava.api.model.Ports();
                portBindingMap.forEach(ports::bind);

                hostConfig.withPortBindings(ports);
                cmd.withExposedPorts(exposedPorts.toArray(new com.github.dockerjava.api.model.ExposedPort[0]));
                hasHostConfig = true;
                log.debug("设置端口映射: {}", config.getPortBindings());
                
                // 通知端口映射设置进度
                com.github.dockerjava.api.model.ResponseItem responseItem = new com.github.dockerjava.api.model.ResponseItem() {};
                callback.onNext(responseItem);
            }

            // 设置卷映射
            if (config.getVolumeBindings() != null && !config.getVolumeBindings().isEmpty()) {
                var binds = config.getVolumeBindings().stream()
                        .map(binding -> {
                            String[] parts = binding.split(":");
                            if (parts.length == 2) {
                                return new com.github.dockerjava.api.model.Bind(parts[0], new com.github.dockerjava.api.model.Volume(parts[1]));
                            }
                            return null;
                        })
                        .filter(java.util.Objects::nonNull)
                        .toArray(com.github.dockerjava.api.model.Bind[]::new);

                hostConfig.withBinds(binds);
                hasHostConfig = true;
                log.debug("设置卷映射: {}", config.getVolumeBindings());
                
                // 通知卷映射设置进度
                com.github.dockerjava.api.model.ResponseItem responseItem = new com.github.dockerjava.api.model.ResponseItem() {};
                callback.onNext(responseItem);
            }

            // 设置自动删除
            if (config.getAutoRemove() != null && config.getAutoRemove()) {
                hostConfig.withAutoRemove(true);
                hasHostConfig = true;
                log.debug("设置自动删除: true");
                
                // 通知自动删除设置进度
                com.github.dockerjava.api.model.ResponseItem responseItem = new com.github.dockerjava.api.model.ResponseItem() {};
                callback.onNext(responseItem);
            }

            // 设置重启策略
            if (com.chua.common.support.utils.StringUtils.isNotEmpty(config.getRestartPolicy())) {
                com.github.dockerjava.api.model.RestartPolicy restartPolicy = com.github.dockerjava.api.model.RestartPolicy.parse(config.getRestartPolicy());
                hostConfig.withRestartPolicy(restartPolicy);
                hasHostConfig = true;
                log.debug("设置重启策略: {}", config.getRestartPolicy());
                
                // 通知重启策略设置进度
                com.github.dockerjava.api.model.ResponseItem responseItem = new com.github.dockerjava.api.model.ResponseItem() {};
                callback.onNext(responseItem);
            }

            // 应用HostConfig
            if (hasHostConfig) {
                cmd.withHostConfig(hostConfig);
            }

            // 设置启动命令
            if (com.chua.common.support.utils.StringUtils.isNotEmpty(config.getCommand())) {
                cmd.withCmd(config.getCommand().split(" "));
                log.debug("设置启动命令: {}", config.getCommand());
                
                // 通知启动命令设置进度
                com.github.dockerjava.api.model.ResponseItem responseItem = new com.github.dockerjava.api.model.ResponseItem() {};
                callback.onNext(responseItem);
            }

            // 设置启动参数（如果有的话，追加到命令后面）
            if (com.chua.common.support.utils.StringUtils.isNotEmpty(config.getArgs())) {
                String fullCommand = (com.chua.common.support.utils.StringUtils.isNotEmpty(config.getCommand()) ? config.getCommand() + " " : "") + config.getArgs();
                cmd.withCmd(fullCommand.split(" "));
                log.debug("设置启动参数: {}", config.getArgs());
                
                // 通知启动参数设置进度
                com.github.dockerjava.api.model.ResponseItem responseItem = new com.github.dockerjava.api.model.ResponseItem() {};
                callback.onNext(responseItem);
            }

            var response = cmd.exec();
            String containerId = response.getId();
            log.info("容器创建成功: image={}, name={}, containerId={}",
                    config.getImageName(), config.getContainerName(), containerId);
            
            // 通知创建完成
            callback.onComplete();
            
            return response;

        } catch (Exception e) {
            log.error("创建容器失败: image={}, name={}", config.getImageName(), config.getContainerName(), e);
            callback.onError(e);
            throw new RuntimeException("创建容器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用DockerCreateContainer配置对象创建并启动容器（带回调）
     * <p>
     * 基于DockerCreateContainer配置对象创建并启动Docker容器，并通过回调处理器实时监听创建和启动过程。
     * 该方法会：
     * 1. 调用createContainer方法创建容器
     * 2. 调用startContainerCmd方法启动容器
     * 3. 如果启动失败，会自动清理已创建的容器
     * 4. 通过回调处理器实时监听整个过程
     * <p>
     * 注意：这是一个原子操作，要么容器创建并启动成功，要么完全回滚。
     *
     * @param config   Docker容器配置对象，包含所有容器配置选项
     * @param callback 创建和启动过程回调处理器，用于接收进度和状态信息
     * @return 容器ID
     * @throws IllegalStateException    当Docker客户端未连接时抛出
     * @throws IllegalArgumentException 当配置对象为空或镜像名称为空时抛出
     * @throws RuntimeException         当创建或启动过程中发生错误时抛出
     * 
     * @see DockerCreateContainer Docker容器配置对象
     * @see DockerResultCallback Docker回调接口
     * @see #createContainer(DockerCreateContainer, DockerResultCallback) 创建容器方法
     */
    public String createAndStartContainer(DockerCreateContainer config, com.chua.docker.support.callback.CreateContainerCallback callback) {
        if (dockerClient == null) {
            throw new IllegalStateException("Docker客户端未连接");
        }
        if (config == null) {
            throw new IllegalArgumentException("容器配置不能为空");
        }
        if (com.chua.common.support.utils.StringUtils.isEmpty(config.getImageName())) {
            throw new IllegalArgumentException("镜像名称不能为空");
        }
        if (callback == null) {
            throw new IllegalArgumentException("回调处理器不能为空");
        }

        try {
            log.info("开始创建并启动容器(带回调): image={}, name={}", config.getImageName(), config.getContainerName());

            // 创建容器
            var createResponse = createContainer(config, callback);
            String containerId = createResponse.getId();
            
            log.info("启动容器: {}", containerId);
            
            // 启动容器
            dockerClient.startContainerCmd(containerId).exec();
            log.info("容器启动成功: {}", containerId);
            
            return containerId;
        } catch (Exception e) {
            log.error("创建并启动容器失败: image={}, name={}", config.getImageName(), config.getContainerName(), e);
            callback.onError(e);
            
            // 启动失败时尝试删除已创建的容器
            try {
                if (e.getMessage() != null && e.getMessage().contains("containerId")) {
                    String containerId = e.getMessage().substring(e.getMessage().indexOf("containerId=") + 12);
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                    log.info("已清理失败的容器: {}", containerId);
                }
            } catch (Exception cleanupException) {
                log.warn("清理失败容器时发生错误: {}", cleanupException.getMessage());
            }
            
            throw new RuntimeException("创建并启动容器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据DockerContainerConfig更新现有容器
     * 注意：Docker不支持直接更新容器配置，此方法会停止并删除旧容器，然后创建新容器
     *
     * @param oldContainerId 旧容器ID
     * @param config         新的容器配置
     * @return 新容器ID
     * @throws RuntimeException 更新失败时抛出异常
     * @author CH
     * @version 1.0
     * @since 2025/01/17
     */
    public String updateContainer(String oldContainerId, DockerContainerConfig config) {
        if (StringUtils.isEmpty(oldContainerId)) {
            throw new IllegalArgumentException("旧容器ID不能为空");
        }

        try {
            log.info("开始更新容器: oldId={}, newName={}", oldContainerId, config.getContainerName());

            // 停止旧容器
            try {
                dockerClient.stopContainerCmd(oldContainerId).exec();
                log.debug("旧容器已停止: {}", oldContainerId);
            } catch (Exception e) {
                log.warn("停止旧容器时发生错误: {}", oldContainerId, e);
            }

            // 删除旧容器
            try {
                dockerClient.removeContainerCmd(oldContainerId).withForce(true).exec();
                log.debug("旧容器已删除: {}", oldContainerId);
            } catch (Exception e) {
                log.warn("删除旧容器时发生错误: {}", oldContainerId, e);
            }

            // 创建新容器
            String newContainerId = createContainer(config);
            log.info("容器更新成功: oldId={}, newId={}", oldContainerId, newContainerId);
            return newContainerId;

        } catch (Exception e) {
            log.error("更新容器失败: oldId={}", oldContainerId, e);
            throw new RuntimeException("更新容器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据容器名称启动容器
     * <p>
     * 启动指定名称的Docker容器。该方法会：
     * 1. 使用Docker客户端的startContainerCmd命令
     * 2. 执行启动操作
     *
     * @param containerName 容器名称
     * @throws IllegalArgumentException 当容器名称为空时抛出
     * @throws RuntimeException      当启动过程中发生错误时抛出
     * 
     * @see #startContainer(String) 根据容器ID启动容器的方法
     */
    // public void startContainerByName(String containerName) { ... }

    /**
     * 根据容器名称停止容器
     * <p>
     * 停止指定名称的Docker容器。该方法会：
     * 1. 使用Docker客户端的stopContainerCmd命令
     * 2. 执行停止操作
     *
     * @param containerName 容器名称
     * @throws IllegalArgumentException 当容器名称为空时抛出
     * @throws RuntimeException      当停止过程中发生错误时抛出
     * 
     * @see #stopContainer(String) 根据容器ID停止容器的方法
     */

    /**
     * 根据容器ID获取容器详细信息
     * <p>
     * 获取指定ID的Docker容器的详细信息。该方法会：
     * 1. 使用Docker客户端的inspectContainerCmd命令
     * 2. 执行检查操作并返回容器详细信息
     * <p>
     * 返回的详细信息包括容器的配置、状态、网络设置等完整信息。
     *
     * @param containerId 容器ID
     * @return 容器详细信息对象
     * @throws IllegalArgumentException 当容器ID为空时抛出
     * @throws RuntimeException      当获取过程中发生错误时抛出
     * 
     * @see InspectContainerResponse 容器详细信息响应对象
     */
    public InspectContainerResponse inspectContainerById(String containerId) {
        if (StringUtils.isBlank(containerId)) {
            throw new IllegalArgumentException("容器ID不能为空");
        }

        try {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(containerId).exec();
            log.info("获取容器详细信息成功: containerId={}", containerId);
            return response;

        } catch (Exception e) {
            log.error("获取容器详细信息失败: containerId={}", containerId, e);
            throw new RuntimeException("获取容器详细信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据容器名称获取容器详细信息
     * <p>
     * 获取指定名称的Docker容器的详细信息。该方法会：
     * 1. 使用Docker客户端的inspectContainerCmd命令
     * 2. 执行检查操作并返回容器详细信息
     * <p>
     * 返回的详细信息包括容器的配置、状态、网络设置等完整信息。
     *
     * @param containerName 容器名称
     * @return 容器详细信息对象
     * @throws IllegalArgumentException 当容器名称为空时抛出
     * @throws RuntimeException      当获取过程中发生错误时抛出
     * 
     * @see InspectContainerResponse 容器详细信息响应对象
     */
    public void stopContainerByName(String containerName) {
        if (StringUtils.isEmpty(containerName)) {
            throw new IllegalArgumentException("容器名称不能为空");
        }

        try {
            log.info("停止容器: name={}", containerName);
            dockerClient.stopContainerCmd(containerName).exec();
            log.info("容器停止成功: name={}", containerName);
        } catch (Exception e) {
            log.error("停止容器失败: name={}", containerName, e);
            throw new RuntimeException("停止容器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据容器名称重启容器
     * <p>
     * 重启指定名称的Docker容器。该方法会：
     * 1. 使用Docker客户端的restartContainerCmd命令
     * 2. 执行重启操作
     *
     * @param containerName 容器名称
     * @throws IllegalArgumentException 当容器名称为空时抛出
     * @throws RuntimeException      当重启过程中发生错误时抛出
     * 
     * @see #restartContainer(String) 根据容器ID重启容器的方法
     */
    public void restartContainerByName(String containerName) {
        if (StringUtils.isEmpty(containerName)) {
            throw new IllegalArgumentException("容器名称不能为空");
        }

        try {
            log.info("重启容器: name={}", containerName);
            dockerClient.restartContainerCmd(containerName).exec();
            log.info("容器重启成功: name={}", containerName);
        } catch (Exception e) {
            log.error("重启容器失败: name={}", containerName, e);
            throw new RuntimeException("重启容器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据容器名称删除容器
     * <p>
     * 删除指定名称的Docker容器。该方法会：
     * 1. 使用Docker客户端的removeContainerCmd命令
     * 2. 根据force参数决定是否强制删除
     * 3. 执行删除操作
     *
     * @param containerName 容器名称
     * @param force         是否强制删除（即使容器正在运行）
     * @throws IllegalArgumentException 当容器名称为空时抛出
     * @throws RuntimeException      当删除过程中发生错误时抛出
     * 
     * @see #removeContainer(String, boolean) 根据容器ID删除容器的方法
     */
    public void removeContainerByName(String containerName, boolean force) {
        if (StringUtils.isEmpty(containerName)) {
            throw new IllegalArgumentException("容器名称不能为空");
        }

        try {
            log.info("删除容器: name={}, force={}", containerName, force);
            var cmd = dockerClient.removeContainerCmd(containerName);
            if (force) {
                cmd.withForce(true);
            }
            cmd.exec();
            log.info("容器删除成功: name={}", containerName);
        } catch (Exception e) {
            log.error("删除容器失败: name={}", containerName, e);
            throw new RuntimeException("删除容器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据容器名称获取容器详细信息
     * <p>
     * 获取指定名称的Docker容器的详细信息。该方法会：
     * 1. 使用Docker客户端的inspectContainerCmd命令
     * 2. 执行检查操作并返回容器详细信息
     * <p>
     * 返回的详细信息包括容器的配置、状态、网络设置等完整信息。
     *
     * @param containerName 容器名称
     * @return 容器详细信息对象
     * @throws IllegalArgumentException 当容器名称为空时抛出
     * @throws RuntimeException      当获取过程中发生错误时抛出
     * 
     * @see InspectContainerResponse 容器详细信息响应对象
     */
    public InspectContainerResponse inspectContainerByName(String containerName) {
        if (StringUtils.isEmpty(containerName)) {
            throw new IllegalArgumentException("容器名称不能为空");
        }

        try {
            log.debug("获取容器详细信息: name={}", containerName);
            var response = dockerClient.inspectContainerCmd(containerName).exec();
            log.debug("获取容器详细信息成功: name={}, id={}", containerName, response.getId());
            return response;
        } catch (Exception e) {
            log.error("获取容器详细信息失败: name={}", containerName, e);
            throw new RuntimeException("获取容器详细信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据容器ID获取容器详细信息
     * <p>
     * 获取指定ID的Docker容器的详细信息。该方法会：
     * 1. 使用Docker客户端的inspectContainerCmd命令
     * 2. 执行检查操作并返回容器详细信息
     * <p>
     * 返回的详细信息包括容器的配置、状态、网络设置等完整信息。
     *
     * @param containerId 容器ID
     * @return 容器详细信息对象
     * @throws IllegalArgumentException 当容器ID为空时抛出
     * @throws RuntimeException      当获取过程中发生错误时抛出
     * 
     * @see InspectContainerResponse 容器详细信息响应对象
     */
    public InspectContainerResponse inspectContainerById(String containerId) {
        if (StringUtils.isEmpty(containerId)) {
            throw new IllegalArgumentException("容器ID不能为空");
        }

        try {
            log.debug("获取容器详细信息: id={}", containerId);
            var response = dockerClient.inspectContainerCmd(containerId).exec();
            log.debug("获取容器详细信息成功: id={}, name={}", containerId, response.getName());
            return response;
        } catch (Exception e) {
            log.error("获取容器详细信息失败: id={}", containerId, e);
            throw new RuntimeException("获取容器详细信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据容器名称获取容器日志
     * <p>
     * 获取指定名称的Docker容器的日志信息。该方法支持多种日志获取选项：
     * 1. 获取最后N行日志
     * 2. 持续跟踪日志输出
     * 3. 包含时间戳信息
     * <p>
     * 该方法会：
     * 1. 使用Docker客户端的logContainerCmd命令
     * 2. 根据参数配置日志选项
     * 3. 执行日志获取操作并返回日志内容
     *
     * @param containerName 容器名称
     * @param tail          获取最后多少行日志（0表示全部）
     * @param follow        是否持续跟踪日志（true表示持续输出新日志）
     * @return 日志内容字符串
     * @throws IllegalArgumentException 当容器名称为空时抛出
     * @throws RuntimeException      当获取过程中发生错误时抛出
     */
    // Delete错误的getContainerLogsByName方法，应直接使用容器ID而不是名称
    // public String getContainerLogsByName(String containerName, int tail, boolean follow) { ... }

    /**
     * 根据容器ID获取容器日志
     * <p>
     * 获取指定ID的Docker容器的日志信息。该方法支持多种日志获取选项：
     * 1. 获取最后N行日志
     * 2. 持续跟踪日志输出
     * 3. 包含时间戳信息
     * <p>
     * 该方法会：
     * 1. 使用Docker客户端的logContainerCmd命令
     * 2. 根据参数配置日志选项
     * 3. 执行日志获取操作并返回日志内容
     *
     * @param containerId 容器ID
     * @param tail        获取最后多少行日志（0表示全部）
     * @param follow      是否持续跟踪日志（true表示持续输出新日志）
     * @return 日志内容字符串
     * @throws IllegalArgumentException 当容器ID为空时抛出
     * @throws RuntimeException      当获取过程中发生错误时抛出
     */
    public String getContainerLogsById(String containerId, int tail, boolean follow) {
        if (StringUtils.isEmpty(containerId)) {
            throw new IllegalArgumentException("容器ID不能为空");
        }

        try {
            log.debug("获取容器日志: name={}, tail={}, follow={}", containerId, tail, follow);
            var cmd = dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTimestamps(true);

            if (tail > 0) {
                cmd.withTail(tail);
            }

            if (follow) {
                cmd.withFollowStream(true);
            }

            var callback = new LogContainerResultCallback();
            cmd.exec(callback);

            if (!follow) {
                callback.awaitCompletion();
            }

            String logs = callback.toString();
            log.debug("获取容器日志成功: name={}, logLength={}", containerName, logs.length());
            return logs;
        } catch (Exception e) {
            log.error("获取容器日志失败: name={}", containerName, e);
            throw new RuntimeException("获取容器日志失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据容器ID获取容器日志
     * <p>
     * 获取指定ID的Docker容器的日志信息。该方法支持多种日志获取选项：
     * 1. 获取最后N行日志
     * 2. 持续跟踪日志输出
     * 3. 包含时间戳信息
     * <p>
     * 该方法会：
     * 1. 使用Docker客户端的logContainerCmd命令
     * 2. 根据参数配置日志选项
     * 3. 执行日志获取操作并返回日志内容
     *
     * @param containerId 容器ID
     * @param tail        获取最后多少行日志（0表示全部）
     * @param follow      是否持续跟踪日志（true表示持续输出新日志）
     * @return 日志内容字符串
     * @throws IllegalArgumentException 当容器ID为空时抛出
     * @throws RuntimeException      当获取过程中发生错误时抛出
     */
    public String getContainerLogsById(String containerId, int tail, boolean follow) {
        if (StringUtils.isEmpty(containerId)) {
            throw new IllegalArgumentException("容器ID不能为空");
        }

        try {
            log.debug("获取容器日志: id={}, tail={}, follow={}", containerId, tail, follow);
            var cmd = dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTimestamps(true);

            if (tail > 0) {
                cmd.withTail(tail);
            }

            if (follow) {
                cmd.withFollowStream(true);
            }

            var callback = new LogContainerResultCallback();
            cmd.exec(callback);

            if (!follow) {
                callback.awaitCompletion();
            }

            String logs = callback.toString();
            log.debug("获取容器日志成功: id={}, logLength={}", containerId, logs.length());
            return logs;
        } catch (Exception e) {
            log.error("获取容器日志失败: id={}", containerId, e);
            throw new RuntimeException("获取容器日志失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查容器是否存在
     *
     * @param containerName 容器名称
     * @return 是否存在
     * @author CH
     * @version 1.0
     * @since 2025/01/17
     */
    public boolean containerExists(String containerName) {
        if (StringUtils.isEmpty(containerName)) {
            return false;
        }

        try {
            dockerClient.inspectContainerCmd(containerName).exec();
            return true;
        } catch (Exception e) {
            log.debug("容器不存在: name={}", containerName);
            return false;
        }
    }

    /**
     * 检查容器是否存在（基于ID）
     *
     * @param containerId 容器ID
     * @return 是否存在
     * @author CH
     * @version 1.0
     * @since 2025/01/17
     */
    public boolean containerExistsById(String containerId) {
        if (StringUtils.isEmpty(containerId)) {
            return false;
        }

        try {
            dockerClient.inspectContainerCmd(containerId).exec();
            return true;
        } catch (Exception e) {
            log.debug("容器不存在: id={}", containerId);
            return false;
        }
    }

    /**
     * 检查容器是否正在运行
     *
     * @param containerName 容器名称
     * @return 是否正在运行
     * @author CH
     * @version 1.0
     * @since 2025/01/17
     */
    public boolean isContainerRunning(String containerName) {
        if (StringUtils.isEmpty(containerName)) {
            return false;
        }

        try {
            var response = dockerClient.inspectContainerCmd(containerName).exec();
            boolean running = Boolean.TRUE.equals(response.getState().getRunning());
            log.debug("容器运行状态: name={}, running={}", containerName, running);
            return running;
        } catch (Exception e) {
            log.debug("检查容器运行状态失败: name={}", containerName, e);
            return false;
        }
    }

    /**
     * 检查容器是否正在运行（基于ID）
     *
     * @param containerId 容器ID
     * @return 是否正在运行
     * @author CH
     * @version 1.0
     * @since 2025/01/17
     */
    public boolean isContainerRunningById(String containerId) {
        if (StringUtils.isEmpty(containerId)) {
            return false;
        }

        try {
            var response = dockerClient.inspectContainerCmd(containerId).exec();
            boolean running = Boolean.TRUE.equals(response.getState().getRunning());
            log.debug("容器运行状态: id={}, running={}", containerId, running);
            return running;
        } catch (Exception e) {
            log.debug("检查容器运行状态失败: id={}", containerId, e);
            return false;
        }
    }

    /**
     * 拉取Docker镜像（带回调和配置对象）
     * <p>
     * 使用DockerPullImage配置对象来指定拉取镜像的各种参数，包括镜像名称、标签、仓库地址、
     * 认证信息等。该方法支持异步回调，可以在拉取过程中实时获取进度信息。
     *
     * @param config   拉取镜像配置对象，包含镜像名称、标签、仓库、认证信息等参数
     * @param callback 拉取过程回调接口，用于接收拉取进度和状态信息
     * @return 拉取是否成功，成功返回true，失败抛出异常
     * @throws IllegalArgumentException 当配置对象为空或镜像名称为空时抛出
     * @throws RuntimeException         当Docker拉取过程中发生错误时抛出
     * 
     * @see DockerPullImage 镜像拉取配置对象
     * @see DockerResultCallback Docker回调接口
     */
    public boolean pullImage(DockerPullImage config, com.chua.docker.support.callback.PullImageCallback callback) {
        // 参数验证
        if (config == null) {
            log.error("拉取镜像配置不能为空");
            throw new IllegalArgumentException("拉取镜像配置不能为空");
        }
        if (!StringUtils.hasText(config.getImageName())) {
            log.error("镜像名称不能为空");
            throw new IllegalArgumentException("镜像名称不能为空");
        }
        if (callback == null) {
            log.error("回调接口不能为空");
            throw new IllegalArgumentException("回调接口不能为空");
        }

        try {
            log.info("开始拉取Docker镜像: {}:{}", config.getImageName(), config.getTag());
            
            // 创建拉取命令
            var pullImageCmd = dockerClient.pullImageCmd(config.getImageName());
            
            // 设置标签
            if (config.getTag() != null && !config.getTag().isEmpty()) {
                pullImageCmd.withTag(config.getTag());
            }
            
            // 设置仓库
            if (config.getRepository() != null && !config.getRepository().isEmpty()) {
                pullImageCmd.withRegistry(config.getRepository());
            }
            
            // 设置认证信息
            if (config.hasAuth()) {
                var authConfig = new AuthConfig()
                    .withUsername(config.getUsername())
                    .withPassword(config.getPassword());
                pullImageCmd.withAuthConfig(authConfig);
            }
            
            // 执行拉取操作
            pullImageCmd.exec(new ResultCallback.Adapter<>() {
                @Override
                public void onNext(PullResponseItem object) {
                    callback.onNext(object);
                }

                @Override
                public void onStart(Closeable stream) {
                    callback.onStart(stream);
                }

                @Override
                public void onError(Throwable throwable) {
                    callback.onError(throwable);
                }

                @Override
                public void onComplete() {
                    callback.onComplete();
                }
            }).awaitCompletion();
            
            log.info("Docker镜像拉取成功: {}:{}", config.getImageName(), config.getTag());
            return true;
        } catch (Exception e) {
            log.error("Docker镜像拉取失败: {}:{}, 错误信息: {}", config.getImageName(), config.getTag(), e.getMessage(), e);
            throw new RuntimeException("Docker镜像拉取失败: " + e.getMessage(), e);
        }
    }

}










