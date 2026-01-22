package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.EnumTypeHandler;

import java.time.LocalDateTime;

/**
 * 系统软件容器信息实体类
 *
 * @author CH
 * @since 2025/01/15
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "系统软件容器信息")
@Schema(description = "系统软件容器信息")
@Data
@TableName(value = "system_soft_container")
public class SystemSoftContainer extends SysBase {

    /**
     * 系统软件容器ID
     */
    @TableId(value = "system_soft_container_id", type = IdType.AUTO)
    @ApiModelProperty(value = "系统软件容器ID")
    @Schema(description = "系统软件容器ID")
    @NotNull(message = "系统软件容器ID不能为空", groups = {UpdateGroup.class})
    private Integer systemSoftContainerId;

    /**
     * 关联的软件ID
     */
    @TableField(value = "system_soft_id")
    @ApiModelProperty(value = "关联的软件ID")
    @Schema(description = "关联的软件ID")
    @NotNull(message = "关联的软件ID不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private Integer systemSoftId;

    /**
     * 关联的软件版本ID
     */
    @TableField(value = "system_soft_version_id")
    @ApiModelProperty(value = "关联的软件版本ID")
    @Schema(description = "关联的软件版本ID")
    private Integer systemSoftVersionId;

    /**
     * 关联的服务器ID
     */
    @TableField(value = "system_server_id")
    @ApiModelProperty(value = "关联的服务器ID")
    @Schema(description = "关联的服务器ID")
    @NotNull(message = "关联的服务器ID不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private Integer systemServerId;

    /**
     * 关联的镜像ID
     */
    @TableField(value = "system_soft_image_id")
    @ApiModelProperty(value = "关联的镜像ID")
    @Schema(description = "关联的镜像ID")
    private Integer systemSoftImageId;

    /**
     * Docker容器ID
     */
    @TableField(value = "system_soft_container_docker_id")
    @ApiModelProperty(value = "Docker容器ID")
    @Schema(description = "Docker容器ID")
    @Size(max = 100, message = "Docker容器ID最大长度要小于 100")
    private String systemSoftContainerDockerId;

    /**
     * 容器名称
     */
    @TableField(value = "system_soft_container_name")
    @ApiModelProperty(value = "容器名称")
    @Schema(description = "容器名称")
    @NotBlank(message = "容器名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 255, message = "容器名称最大长度要小于 255")
    private String systemSoftContainerName;

    /**
     * 容器镜像
     */
    @TableField(value = "system_soft_container_image")
    @ApiModelProperty(value = "容器镜像")
    @Schema(description = "容器镜像")
    @Size(max = 255, message = "容器镜像最大长度要小于 255")
    private String systemSoftContainerImage;

    /**
     * 容器镜像标签
     */
    @TableField(value = "system_soft_container_image_tag")
    @ApiModelProperty(value = "容器镜像标签")
    @Schema(description = "容器镜像标签")
    @Size(max = 100, message = "容器镜像标签最大长度要小于 100")
    private String systemSoftContainerImageTag;

    /**
     * 容器状态
     */
    @TableField(value = "system_soft_container_status", typeHandler = EnumTypeHandler.class)
    @ApiModelProperty(value = "容器状态")
    @Schema(description = "容器状态")
    private SystemSoftContainerStatus systemSoftContainerStatus;

    /**
     * 容器端口映射
     */
    @TableField(value = "system_soft_container_ports")
    @ApiModelProperty(value = "容器端口映射")
    @Schema(description = "容器端口映射")
    @Size(max = 1000, message = "容器端口映射最大长度要小于 1000")
    private String systemSoftContainerPorts;

    /**
     * 容器环境变量
     */
    @TableField(value = "system_soft_container_env")
    @ApiModelProperty(value = "容器环境变量")
    @Schema(description = "容器环境变量")
    @Size(max = 2000, message = "容器环境变量最大长度要小于 2000")
    private String systemSoftContainerEnv;

    /**
     * 容器数据卷
     */
    @TableField(value = "system_soft_container_volumes")
    @ApiModelProperty(value = "容器数据卷")
    @Schema(description = "容器数据卷")
    @Size(max = 1000, message = "容器数据卷最大长度要小于 1000")
    private String systemSoftContainerVolumes;

    /**
     * 容器网络
     */
    @TableField(value = "system_soft_container_networks")
    @ApiModelProperty(value = "容器网络")
    @Schema(description = "容器网络")
    @Size(max = 500, message = "容器网络最大长度要小于 500")
    private String systemSoftContainerNetworks;

    /**
     * 容器启动命令
     */
    @TableField(value = "system_soft_container_command")
    @ApiModelProperty(value = "容器启动命令")
    @Schema(description = "容器启动命令")
    @Size(max = 1000, message = "容器启动命令最大长度要小于 1000")
    private String systemSoftContainerCommand;

    /**
     * 容器启动参数
     */
    @TableField(value = "system_soft_container_args")
    @ApiModelProperty(value = "容器启动参数")
    @Schema(description = "容器启动参数")
    @Size(max = 1000, message = "容器启动参数最大长度要小于 1000")
    private String systemSoftContainerArgs;

    /**
     * 容器创建时间
     */
    @TableField(value = "system_soft_container_created_time")
    @ApiModelProperty(value = "容器创建时间")
    @Schema(description = "容器创建时间")
    private LocalDateTime systemSoftContainerCreatedTime;

    /**
     * 容器启动时间
     */
    @TableField(value = "system_soft_container_started_time")
    @ApiModelProperty(value = "容器启动时间")
    @Schema(description = "容器启动时间")
    private LocalDateTime systemSoftContainerStartedTime;

    /**
     * 容器结束时间
     */
    @TableField(value = "system_soft_container_finished_time")
    @ApiModelProperty(value = "容器结束时间")
    @Schema(description = "容器结束时间")
    private LocalDateTime systemSoftContainerFinishedTime;

    /**
     * 容器重启次数
     */
    @TableField(value = "system_soft_container_restart_count")
    @ApiModelProperty(value = "容器重启次数")
    @Schema(description = "容器重启次数")
    private Integer systemSoftContainerRestartCount;

    /**
     * 容器配置信息
     */
    @TableField(value = "system_soft_container_config")
    @ApiModelProperty(value = "容器配置信息")
    @Schema(description = "容器配置信息")
    @Size(max = 5000, message = "容器配置信息最大长度要小于 5000")
    private String systemSoftContainerConfig;

    /**
     * 容器健康状态
     */
    @TableField(value = "system_soft_container_health_status")
    @ApiModelProperty(value = "容器健康状态")
    @Schema(description = "容器健康状态")
    @Size(max = 50, message = "容器健康状态最大长度要小于 50")
    private String systemSoftContainerHealthStatus;

    /**
     * 是否自动重启
     */
    @TableField(value = "system_soft_container_auto_restart")
    @ApiModelProperty(value = "是否自动重启")
    @Schema(description = "是否自动重启")
    private Integer systemSoftContainerAutoRestart;

    /**
     * CPU使用限制
     */
    @TableField(value = "system_soft_container_cpu_limit")
    @ApiModelProperty(value = "CPU使用限制")
    @Schema(description = "CPU使用限制")
    private Double systemSoftContainerCpuLimit;

    /**
     * 内存使用限制（MB）
     */
    @TableField(value = "system_soft_container_memory_limit")
    @ApiModelProperty(value = "内存使用限制")
    @Schema(description = "内存使用限制")
    private Long systemSoftContainerMemoryLimit;

    /**
     * 容器备注
     */
    @TableField(value = "system_soft_container_remark")
    @ApiModelProperty(value = "容器备注")
    @Schema(description = "容器备注")
    @Size(max = 500, message = "容器备注最大长度要小于 500")
    private String systemSoftContainerRemark;

    /**
     * 扩展字段1
     */
    @TableField(value = "system_soft_container_ext_field1")
    @ApiModelProperty(value = "扩展字段1")
    @Schema(description = "扩展字段1")
    @Size(max = 255, message = "扩展字段1最大长度要小于 255")
    private String systemSoftContainerExtField1;

    /**
     * 扩展字段2
     */
    @TableField(value = "system_soft_container_ext_field2")
    @ApiModelProperty(value = "扩展字段2")
    @Schema(description = "扩展字段2")
    @Size(max = 255, message = "扩展字段2最大长度要小于 255")
    private String systemSoftContainerExtField2;

    /**
     * 扩展字段3
     */
    @TableField(value = "system_soft_container_ext_field3")
    @ApiModelProperty(value = "扩展字段3")
    @Schema(description = "扩展字段3")
    @Size(max = 255, message = "扩展字段3最大长度要小于 255")
    private String systemSoftContainerExtField3;
}