package com.chua.starter.monitor.pojo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 创建容器请求
 *
 * @author CH
 * @version 1.0
 * @since 2025/01/15
 */
@Data
@ApiModel(description = "创建容器请求")
@Schema(description = "创建容器请求")
public class CreateContainerRequest {

    /**
     * 软件ID
     */
    @NotNull(message = "软件ID不能为空")
    @ApiModelProperty(value = "软件ID", required = true, example = "1")
    @Schema(description = "软件ID", example = "1", required = true)
    private Integer softId;

    /**
     * 服务器ID
     */
    @NotNull(message = "服务器ID不能为空")
    @ApiModelProperty(value = "服务器ID", required = true, example = "1")
    @Schema(description = "服务器ID", example = "1", required = true)
    private Integer serverId;

    /**
     * 容器名称
     */
    @NotEmpty(message = "容器名称不能为空")
    @ApiModelProperty(value = "容器名称", required = true, example = "my-container")
    @Schema(description = "容器名称", example = "my-container", required = true)
    private String containerName;

    /**
     * 镜像标签
     */
    @NotEmpty(message = "镜像标签不能为空")
    @ApiModelProperty(value = "镜像标签", required = true, example = "nginx:latest")
    @Schema(description = "镜像标签", example = "nginx:latest", required = true)
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
}