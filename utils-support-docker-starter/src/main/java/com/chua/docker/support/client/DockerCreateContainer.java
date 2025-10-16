package com.chua.docker.support.client;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Docker创建容器配置对象
 * <p>
 * 用于封装创建Docker容器时所需的配置参数。该类不包含Map类型的字段，
 * 所有映射关系都通过字符串列表或其他数据结构来表示。
 * 
 * <p>使用示例：
 * <pre>
 * DockerCreateContainer config = DockerCreateContainer.builder()
 *     .imageName("nginx:latest")
 *     .containerName("my-nginx")
 *     .portBindings(List.of("8080:80", "8443:443"))
 *     .environmentVariables(List.of("ENV=production", "DEBUG=false"))
 *     .build();
 * </pre>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/01
 * 
 * @see DockerProtocolClient#createContainer(DockerCreateContainer, com.chua.docker.support.callback.DockerResultCallback) Docker客户端创建容器方法
 */
@Data
@Accessors(chain = true)
@Builder
public class DockerCreateContainer {
    
    /**
     * 镜像名称（包括标签）
     * <p>
     * 要用于创建容器的Docker镜像名称，格式为"仓库名/镜像名:标签"。
     * 例如："nginx:latest"、"mysql:8.0"等。
     */
    private String imageName;
    
    /**
     * 容器名称
     * <p>
     * 为容器指定的名称，用于标识和管理容器。如果为空，Docker会自动生成一个唯一名称。
     */
    private String containerName;
    
    /**
     * 端口绑定配置列表
     * <p>
     * 容器端口与主机端口的映射关系，格式为"hostPort:containerPort"。
     * 例如：List.of("8080:80", "8443:443") 表示将主机的8080端口映射到容器的80端口，
     * 将主机的8443端口映射到容器的443端口。
     */
    private List<String> portBindings;
    
    /**
     * 环境变量列表
     * <p>
     * 容器运行时的环境变量设置，格式为"KEY=VALUE"。
     * 例如：List.of("ENV=production", "DEBUG=false")
     */
    private List<String> environmentVariables;
    
    /**
     * 卷绑定配置列表
     * <p>
     * 主机路径与容器路径的映射关系，用于数据持久化和共享，格式为"hostPath:containerPath"。
     * 例如：List.of("/host/data:/container/data") 表示将主机的/host/data目录映射到容器的/container/data目录。
     */
    private List<String> volumeBindings;
    
    /**
     * 启动命令
     * <p>
     * 容器启动时执行的命令，会覆盖镜像中定义的默认命令。
     */
    private String command;
    
    /**
     * 启动参数
     * <p>
     * 传递给启动命令的参数，会追加到命令后面。
     */
    private String args;
    
    /**
     * 工作目录
     * <p>
     * 容器启动后的工作目录路径。
     */
    private String workingDirectory;
    
    /**
     * 是否自动删除容器（容器停止后自动删除）
     * <p>
     * 如果设置为true，容器在停止后会自动被删除，不会保留在系统中。
     */
    private Boolean autoRemove;
    
    /**
     * 重启策略
     * <p>
     * 容器的重启策略，可选值包括：
     * - "no"：不重启（默认）
     * - "always"：总是重启
     * - "on-failure"：仅在失败时重启
     * - "unless-stopped"：除非手动停止，否则总是重启
     */
    private String restartPolicy;
    
    /**
     * 网络模式
     * <p>
     * 容器的网络模式，可选值包括：
     * - "bridge"：桥接网络（默认）
     * - "host"：主机网络
     * - "none"：无网络
     * - 自定义网络名称
     */
    private String networkMode;
    
    /**
     * CPU限制
     * <p>
     * 容器可以使用的CPU资源限制，单位为微秒（1秒=1,000,000微秒）。
     */
    private Long cpuLimit;
    
    /**
     * 内存限制（字节）
     * <p>
     * 容器可以使用的内存资源限制，单位为字节。
     */
    private Long memoryLimit;
    
    /**
     * 是否以特权模式运行
     * <p>
     * 如果设置为true，容器将以特权模式运行，拥有更多的系统权限。
     */
    private Boolean privileged;
    
    /**
     * 用户ID
     * <p>
     * 容器内运行进程的用户ID或用户名。
     */
    private String user;
    
    /**
     * 主机名
     * <p>
     * 容器的主机名，可以在容器内部通过hostname命令查看。
     */
    private String hostname;
    
    /**
     * DNS服务器列表
     * <p>
     * 容器使用的DNS服务器地址列表。
     */
    private List<String> dnsServers;
    
    /**
     * 额外主机映射列表
     * <p>
     * 添加到容器/etc/hosts文件中的额外主机名映射，格式为"hostname:ip"。
     * 例如：List.of("host.docker.internal:192.168.1.100")
     */
    private List<String> extraHosts;
    
    /**
     * 标签列表
     * <p>
     * 为容器添加的键值对标签，用于标识和分类容器，格式为"key=value"。
     * 例如：List.of("com.example.version=1.0", "com.example.description=web-server")
     */
    private List<String> labels;
}