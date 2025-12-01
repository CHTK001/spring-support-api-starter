package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统软件镜像仓库信息实体类
 *
 * @author CH
 * @since 2025/01/15
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "系统软件镜像仓库信息")
@Schema(description = "系统软件镜像仓库信息")
@Data
@TableName("system_soft_registry")
public class SystemSoftRegistry extends SysBase {

    /**
     * 系统软件镜像仓库ID
     */
    @TableId(value = "system_soft_registry_id", type = IdType.AUTO)
    @ApiModelProperty(value = "系统软件镜像仓库ID")
    @Schema(description = "系统软件镜像仓库ID")
    @NotNull(message = "系统软件镜像仓库ID不能为空", groups = {UpdateGroup.class})
    private Integer systemSoftRegistryId;

    /**
     * 仓库名称
     */
    @TableField("system_soft_registry_name")
    @ApiModelProperty(value = "仓库名称")
    @Schema(description = "仓库名称")
    @NotBlank(message = "仓库名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "仓库名称长度不能超过100个字符")
    private String systemSoftRegistryName;

    /**
     * 仓库类型(docker_hub, harbor, aliyun, tencent, private)
     */
    @TableField("system_soft_registry_type")
    @ApiModelProperty(value = "仓库类型")
    @Schema(description = "仓库类型")
    @NotBlank(message = "仓库类型不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 50, message = "仓库类型长度不能超过50个字符")
    private String systemSoftRegistryType;

    /**
     * 仓库地址
     */
    @TableField("system_soft_registry_url")
    @ApiModelProperty(value = "仓库地址")
    @Schema(description = "仓库地址")
    @NotBlank(message = "仓库地址不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 500, message = "仓库地址长度不能超过500个字符")
    private String systemSoftRegistryUrl;

    /**
     * 仓库用户名
     */
    @TableField("system_soft_registry_username")
    @ApiModelProperty(value = "仓库用户名")
    @Schema(description = "仓库用户名")
    @Size(max = 100, message = "仓库用户名长度不能超过100个字符")
    private String systemSoftRegistryUsername;

    /**
     * 仓库密码
     */
    @TableField("system_soft_registry_password")
    @ApiModelProperty(value = "仓库密码")
    @Schema(description = "仓库密码")
    @Size(max = 200, message = "仓库密码长度不能超过200个字符")
    private String systemSoftRegistryPassword;

    /**
     * 仓库邮箱
     */
    @TableField("system_soft_registry_email")
    @ApiModelProperty(value = "仓库邮箱")
    @Schema(description = "仓库邮箱")
    @Size(max = 100, message = "仓库邮箱长度不能超过100个字符")
    private String systemSoftRegistryEmail;

    /**
     * 是否激活(0:否, 1:是)
     * 激活的仓库用于软件搜索
     */
    @TableField("system_soft_registry_active")
    @ApiModelProperty(value = "是否激活")
    @Schema(description = "是否激活，激活的仓库用于软件搜索")
    private Integer systemSoftRegistryActive;

    /**
     * 是否启用SSL(0:否, 1:是)
     */
    @TableField("system_soft_registry_ssl_enabled")
    @ApiModelProperty(value = "是否启用SSL")
    @Schema(description = "是否启用SSL")
    private Integer systemSoftRegistrySslEnabled;

    /**
     * 是否支持同步(0:否, 1:是)
     */
    @TableField("system_soft_registry_support_sync")
    @ApiModelProperty(value = "是否支持同步")
    @Schema(description = "是否支持同步")
    private Integer systemSoftRegistrySupportSync;

    /**
     * 连接超时时间(秒)
     */
    @TableField("system_soft_registry_timeout")
    @ApiModelProperty(value = "连接超时时间(秒)")
    @Schema(description = "连接超时时间(秒)")
    private Integer systemSoftRegistryTimeout;

    /**
     * 仓库描述
     */
    @TableField("system_soft_registry_description")
    @ApiModelProperty(value = "仓库描述")
    @Schema(description = "仓库描述")
    @Size(max = 500, message = "仓库描述长度不能超过500个字符")
    private String systemSoftRegistryDescription;

    /**
     * 仓库配置信息(JSON格式)
     */
    @TableField("system_soft_registry_config")
    @ApiModelProperty(value = "仓库配置信息")
    @Schema(description = "仓库配置信息")
    private String systemSoftRegistryConfig;

    /**
     * 最后连接时间
     */
    @TableField("system_soft_registry_last_connect_time")
    @ApiModelProperty(value = "最后连接时间")
    @Schema(description = "最后连接时间")
    private String systemSoftRegistryLastConnectTime;

    /**
     * 连接状态(0:未连接, 1:连接成功, 2:连接失败)
     */
    @TableField("system_soft_registry_connect_status")
    @ApiModelProperty(value = "连接状态")
    @Schema(description = "连接状态")
    private Integer systemSoftRegistryConnectStatus;

    /**
     * 连接错误信息
     */
    @TableField("system_soft_registry_error_message")
    @ApiModelProperty(value = "连接错误信息")
    @Schema(description = "连接错误信息")
    @Size(max = 1000, message = "连接错误信息长度不能超过1000个字符")
    private String systemSoftRegistryErrorMessage;

    /**
     * 排序号
     */
    @TableField("system_soft_registry_sort")
    @ApiModelProperty(value = "排序号")
    @Schema(description = "排序号")
    private Integer systemSoftRegistrySort;

    /**
     * 状态(0:禁用, 1:启用)
     */
    @TableField("system_soft_registry_status")
    @ApiModelProperty(value = "状态(0:禁用, 1:启用)")
    @Schema(description = "状态(0:禁用, 1:启用)")
    @NotNull(message = "状态不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private Integer systemSoftRegistryStatus;
}
