package com.chua.docker.support.client;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Docker拉取镜像配置对象
 * <p>
 * 用于封装拉取Docker镜像时所需的配置参数，包括镜像名称、标签、仓库地址、认证信息等。
 * 该类使用Lombok注解简化了getter/setter方法和建造者模式的实现。
 * 
 * <p>使用示例：
 * <pre>
 * DockerPullImage config = DockerPullImage.builder()
 *     .imageName("nginx")
 *     .tag("latest")
 *     .repository("docker.io")
 *     .username("user")
 *     .password("pass")
 *     .build();
 * </pre>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/01
 * 
 * @see DockerProtocolClient#pullImage(DockerPullImage, com.chua.docker.support.callback.DockerResultCallback) Docker客户端拉取方法
 */
@Data
@Accessors(chain = true)
@Builder
public class DockerPullImage {

    /**
     * 镜像名称
     * <p>
     * Docker镜像的名称，如"nginx"、"mysql"等。这是拉取镜像时必需的参数。
     */
    private String imageName;

    /**
     * 仓库地址
     * <p>
     * Docker镜像仓库的地址，如"docker.io"、"registry.cn-hangzhou.aliyuncs.com"等。
     * 如果未指定，则使用默认的Docker Hub仓库。
     */
    private String repository;

    /**
     * 用户名
     * <p>
     * 访问私有仓库时所需的用户名。如果仓库需要认证，则必须同时提供用户名和密码。
     */
    private String username;

    /**
     * 密码
     * <p>
     * 访问私有仓库时所需的密码。如果仓库需要认证，则必须同时提供用户名和密码。
     */
    private String password;

    /**
     * 标签
     * <p>
     * Docker镜像的标签，如"latest"、"1.21.1"等。如果未指定，则默认使用"latest"标签。
     */
    private String tag;

    /**
     * 是否强制拉取
     * <p>
     * 是否强制从远程仓库拉取镜像，即使本地已存在相同标签的镜像。
     * 默认值为false，表示不强制拉取。
     */
    private Boolean force = false;

    /**
     * 超时时间（秒）
     * <p>
     * 拉取镜像操作的超时时间，单位为秒。如果拉取操作超过此时间仍未完成，则会抛出超时异常。
     * 默认值为300秒（5分钟）。
     */
    private Integer timeout = 300;

    /**
     * 获取完整镜像名称（包含标签）
     * <p>
     * 将镜像名称和标签组合成完整的镜像名称，格式为"imageName:tag"。
     * 如果未指定标签，则只返回镜像名称。
     *
     * @return 完整镜像名称
     */
    public String getFullImageName() {
        if (tag != null && !tag.isEmpty()) {
            return imageName + ":" + tag;
        }
        return imageName;
    }

    /**
     * 获取认证信息是否完整
     * <p>
     * 检查是否提供了完整的认证信息（用户名和密码）。
     * 只有当用户名和密码都不为空时，才认为认证信息完整。
     *
     * @return 是否需要认证
     */
    public boolean hasAuth() {
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
    }
}