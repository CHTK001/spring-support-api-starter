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
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 系统软件信息实体类
 *
 * @author CH
 * @since 2025/01/15
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "系统软件信息")
@Schema(description = "系统软件信息")
@Data
@TableName(value = "system_soft")
public class SystemSoft extends SysBase {

    /**
     * 系统软件ID
     */
    @TableId(value = "system_soft_id", type = IdType.AUTO)
    @ApiModelProperty(value = "系统软件ID")
    @Schema(description = "系统软件ID")
    @NotNull(message = "系统软件ID不能为空", groups = {UpdateGroup.class})
    private Integer systemSoftId;

    /**
     * 软件名称
     */
    @TableField(value = "system_soft_name")
    @ApiModelProperty(value = "软件名称")
    @Schema(description = "软件名称")
    @NotBlank(message = "软件名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 255, message = "软件名称最大长度要小于 255")
    private String systemSoftName;

    /**
     * 软件代码
     */
    @TableField(value = "system_soft_code")
    @ApiModelProperty(value = "软件代码")
    @Schema(description = "软件代码")
    @NotBlank(message = "软件代码不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "软件代码最大长度要小于 100")
    private String systemSoftCode;

    /**
     * 软件分类
     */
    @TableField(value = "system_soft_category")
    @ApiModelProperty(value = "软件分类")
    @Schema(description = "软件分类")
    @Size(max = 100, message = "软件分类最大长度要小于 100")
    private String systemSoftCategory;

    /**
     * 软件图标
     */
    @TableField(value = "system_soft_icon")
    @ApiModelProperty(value = "软件图标")
    @Schema(description = "软件图标")
    @Size(max = 500, message = "软件图标最大长度要小于 500")
    private String systemSoftIcon;

    /**
     * 软件标签
     */
    @TableField(value = "system_soft_tags")
    @ApiModelProperty(value = "软件标签")
    @Schema(description = "软件标签")
    @Size(max = 500, message = "软件标签最大长度要小于 500")
    private String systemSoftTags;

    /**
     * 软件描述
     */
    @TableField(value = "system_soft_desc")
    @ApiModelProperty(value = "软件描述")
    @Schema(description = "软件描述")
    @Size(max = 1000, message = "软件描述最大长度要小于 1000")
    private String systemSoftDesc;

    /**
     * 镜像仓库ID
     */
    @TableField(value = "system_soft_registry_id")
    @ApiModelProperty(value = "镜像仓库ID")
    @Schema(description = "镜像仓库ID")
    private Integer systemSoftRegistryId;

    /**
     * Docker镜像名称
     */
    @TableField(value = "system_soft_docker_image")
    @ApiModelProperty(value = "Docker镜像名称")
    @Schema(description = "Docker镜像名称")
    @Size(max = 255, message = "Docker镜像名称最大长度要小于 255")
    private String systemSoftDockerImage;


    /**
     * 默认安装方式
     */
    @TableField(value = "system_soft_default_install_method")
    @ApiModelProperty(value = "默认安装方式")
    @Schema(description = "默认安装方式")
    @Size(max = 50, message = "默认安装方式最大长度要小于 50")
    private String systemSoftDefaultInstallMethod;

    /**
     * 默认安装参数
     */
    @TableField(value = "system_soft_default_install_params")
    @ApiModelProperty(value = "默认安装参数")
    @Schema(description = "默认安装参数")
    @Size(max = 2000, message = "默认安装参数最大长度要小于 2000")
    private String systemSoftDefaultInstallParams;

    /**
     * 软件状态
     */
    @TableField(value = "system_soft_status")
    @ApiModelProperty(value = "软件状态")
    @Schema(description = "软件状态")
    private Integer systemSoftStatus;

    /**
     * 是否官方软件
     */
    @TableField(value = "system_soft_is_official")
    @ApiModelProperty(value = "是否官方软件")
    @Schema(description = "是否官方软件")
    private Integer systemSoftIsOfficial;

    /**
     * 软件备注
     */
    @TableField(value = "system_soft_remark")
    @ApiModelProperty(value = "软件备注")
    @Schema(description = "软件备注")
    @Size(max = 500, message = "软件备注最大长度要小于 500")
    private String systemSoftRemark;

    /**
     * Star 数
     */
    @TableField(value = "system_soft_star_count")
    @ApiModelProperty(value = "Star 数")
    @Schema(description = "Star 数")
    private Integer systemSoftStarCount;

    /**
     * Pull 数（在线检索展示，）
     */
    @TableField(value = "system_soft_pull_count")
    @ApiModelProperty(value = "Pull 数")
    @Schema(description = "Pull 数")
    private Integer systemSoftPullCount;

    /**
     * 扩展字段1
     */
    @TableField(value = "system_soft_ext_field1")
    @ApiModelProperty(value = "扩展字段1")
    @Schema(description = "扩展字段1")
    @Size(max = 255, message = "扩展字段1最大长度要小于 255")
    private String systemSoftExtField1;

    /**
     * 扩展字段2
     */
    @TableField(value = "system_soft_ext_field2")
    @ApiModelProperty(value = "扩展字段2")
    @Schema(description = "扩展字段2")
    @Size(max = 255, message = "扩展字段2最大长度要小于 255")
    private String systemSoftExtField2;

    /**
     * 扩展字段3
     */
    @TableField(value = "system_soft_ext_field3")
    @ApiModelProperty(value = "扩展字段3")
    @Schema(description = "扩展字段3")
    @Size(max = 255, message = "扩展字段3最大长度要小于 255")
    private String systemSoftExtField3;
}