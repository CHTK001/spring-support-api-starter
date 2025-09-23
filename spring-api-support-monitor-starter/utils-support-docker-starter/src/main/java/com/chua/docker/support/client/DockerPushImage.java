package com.chua.docker.support.client;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Docker推送镜像配置对象
 * <p>
 * 用于封装推送Docker镜像到仓库时所需的配置参数。
 * 
 * <p>使用示例：
 * <pre>
 * DockerPushImage config = DockerPushImage.builder()
 *     .imageName("my-image")
 *     .tag("latest")
 *     .repository("my-registry.com")
 *     .username("my-username")
 *     .password("my-password")
 *     .build();
 * </pre>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/01
 * 
 * @see DockerProtocolClient#pushImage(DockerPushImage, com.chua.docker.support.callback.DockerResultCallback) Docker客户端推送镜像方法
 */
@Data
@Accessors(chain = true)
@Builder
public class DockerPushImage {
    
    /**
     * 镜像名称（包括标签）
     * <p>
     * 要推送的Docker镜像名称，格式为"仓库名/镜像名:标签"。
     * 例如："nginx:latest"、"my-registry.com/my-image:v1.0"等。
     */
    private String imageName;
    
    /**
     * 镜像标签
     * <p>
     * 要推送的Docker镜像标签，如果imageName中已包含标签，则此字段可为空。
     */
    private String tag;
    
    /**
     * 仓库地址
     * <p>
     * 目标仓库的地址，例如："docker.io"、"quay.io"、"my-registry.com"等。
     */
    private String repository;
    
    /**
     * 用户名
     * <p>
     * 推送到私有仓库时的认证用户名。
     */
    private String username;
    
    /**
     * 密码
     * <p>
     * 推送到私有仓库时的认证密码。
     */
    private String password;
    
    /**
     * 检查是否包含认证信息
     * 
     * @return 如果包含用户名和密码则返回true，否则返回false
     */
    public boolean hasAuth() {
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
    }
}