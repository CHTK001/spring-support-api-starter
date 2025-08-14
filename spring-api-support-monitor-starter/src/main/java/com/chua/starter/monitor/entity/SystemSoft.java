package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 软件信息（Docker 生态）
 *
 * 作为软件主表，记录软件基础信息。后续通过 SystemSoftVersion 管理不同版本。
 *
 * @author CH
 * @since 2025/08/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_soft")
@ApiModel("软件信息")
@Schema(name = "SystemSoft", description = "软件信息（Docker 生态）")
public class SystemSoft extends SysBase {

    @TableId(value = "system_soft_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    @NotNull(message = "主键不能为空", groups = { UpdateGroup.class })
    private Integer systemSoftId;

    /** 软件名称 */
    @TableField("system_soft_name")
    @ApiModelProperty("软件名称")
    @Schema(description = "软件名称")
    @Size(max = 255, message = "名称最大长度要小于 255")
    private String systemSoftName;

    /** 软件编码（唯一） */
    @TableField("system_soft_code")
    @ApiModelProperty("软件编码")
    @Schema(description = "软件编码（唯一）")
    @Size(max = 128, message = "编码最大长度要小于 128")
    private String systemSoftCode;

    /** 分类 */
    @TableField("system_soft_category")
    @ApiModelProperty("分类")
    @Schema(description = "分类")
    private String systemSoftCategory;

    /** 图标URL */
    @TableField("system_soft_icon")
    @ApiModelProperty("图标URL")
    @Schema(description = "图标URL")
    private String systemSoftIcon;

    /** 标签（逗号分隔） */
    @TableField("system_soft_tags")
    @ApiModelProperty("标签，逗号分隔")
    @Schema(description = "标签（逗号分隔）")
    private String systemSoftTags;

    /** 说明 */
    @TableField("system_soft_desc")
    @ApiModelProperty("说明")
    @Schema(description = "说明")
    private String systemSoftDesc;

    /** 关联镜像名称（如：nginx） */
    @TableField("system_soft_image")
    @ApiModelProperty("镜像名称")
    @Schema(description = "关联镜像名称（例如 nginx、redis）")
    private String systemSoftImage;

    // ===== 仓库配置（可选） =====
    @TableField("system_soft_registry_type")
    @ApiModelProperty("仓库类型（dockerhub/harbor/aliyun等）")
    private String systemSoftRegistryType;

    @TableField("system_soft_registry_url")
    @ApiModelProperty("仓库地址")
    private String systemSoftRegistryUrl;

    @TableField("system_soft_registry_namespace")
    @ApiModelProperty("命名空间/组织")
    private String systemSoftRegistryNamespace;

    @TableField("system_soft_registry_repository")
    @ApiModelProperty("仓库名/镜像库")
    private String systemSoftRegistryRepository;

    @TableField("system_soft_registry_username")
    @ApiModelProperty("仓库用户名（私有仓库可选）")
    private String systemSoftRegistryUsername;

    @TableField("system_soft_registry_password")
    @ApiModelProperty("仓库密码（私有仓库可选）")
    private String systemSoftRegistryPassword;

    // ===== 默认安装配置（可选） =====
    @TableField("system_soft_default_install_method")
    @ApiModelProperty("默认安装方式（DOCKER_CLI/COMPOSE/SWARM）")
    private String systemSoftDefaultInstallMethod;

    @TableField("system_soft_default_install_params")
    @ApiModelProperty("默认安装参数（JSON文本）")
    private String systemSoftDefaultInstallParams;
}
