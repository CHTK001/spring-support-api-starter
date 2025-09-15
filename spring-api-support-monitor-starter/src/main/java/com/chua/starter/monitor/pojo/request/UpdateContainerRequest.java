package com.chua.starter.monitor.pojo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新容器请求
 *
 * @author CH
 * @version 1.0
 * @since 2025/01/15
 */
@Data
@ApiModel(description = "更新容器请求")
@Schema(description = "更新容器请求")
public class UpdateContainerRequest {

    /**
     * 容器名称
     */
    @ApiModelProperty(value = "容器名称", example = "my-container")
    @Schema(description = "容器名称", example = "my-container")
    private String containerName;

    /**
     * 镜像标签
     */
    @ApiModelProperty(value = "镜像标签", example = "nginx:latest")
    @Schema(description = "镜像标签", example = "nginx:latest")
    private String imageTag;

    /**
     * 端口映射配置
     */
    @ApiModelProperty(value = "端口映射配置", example = "8080:80,8443:443")
    @Schema(description = "端口映射配置，格式：主机端口:容器端口，多个用逗号分隔", example = "8080:80,8443:443")
    private String ports;

    /**
     * 环境变量配置
     */
    @ApiModelProperty(value = "环境变量配置", example = "ENV1=value1,ENV2=value2")
    @Schema(description = "环境变量配置，格式：KEY=VALUE，多个用逗号分隔", example = "ENV1=value1,ENV2=value2")
    private String envVars;

    /**
     * 数据卷挂载配置
     */
    @ApiModelProperty(value = "数据卷挂载配置", example = "/host/path:/container/path")
    @Schema(description = "数据卷挂载配置，格式：主机路径:容器路径，多个用逗号分隔", example = "/host/path:/container/path")
    private String volumes;

    /**
     * 启动命令
     */
    @ApiModelProperty(value = "启动命令", example = "nginx")
    @Schema(description = "容器启动命令", example = "nginx")
    private String command;

    /**
     * 启动参数
     */
    @ApiModelProperty(value = "启动参数", example = "-g daemon off;")
    @Schema(description = "容器启动参数", example = "-g daemon off;")
    private String args;

    /**
     * 自动重启设置
     */
    @ApiModelProperty(value = "自动重启设置", example = "1")
    @Schema(description = "自动重启设置：0-不重启，1-总是重启，2-失败时重启", example = "1")
    private Integer autoRestart;
}