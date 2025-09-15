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
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 系统软件版本信息实体类
 *
 * @author CH
 * @since 2025/01/15
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "系统软件版本信息")
@Schema(description = "系统软件版本信息")
@Data
@TableName(value = "system_soft_version")
public class SystemSoftVersion extends SysBase {

    /**
     * 系统软件版本ID
     */
    @TableId(value = "system_soft_version_id", type = IdType.AUTO)
    @ApiModelProperty(value = "系统软件版本ID")
    @Schema(description = "系统软件版本ID")
    @NotNull(message = "系统软件版本ID不能为空", groups = {UpdateGroup.class})
    private Integer systemSoftVersionId;

    /**
     * 关联的软件ID
     */
    @TableField(value = "system_soft_id")
    @ApiModelProperty(value = "关联的软件ID")
    @Schema(description = "关联的软件ID")
    @NotNull(message = "关联的软件ID不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private Integer systemSoftId;

    /**
     * 版本名称
     */
    @TableField(value = "system_soft_version_name")
    @ApiModelProperty(value = "版本名称")
    @Schema(description = "版本名称")
    @NotBlank(message = "版本名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 100, message = "版本名称最大长度要小于 100")
    private String systemSoftVersionName;

    /**
     * 版本号
     */
    @TableField(value = "system_soft_version_number")
    @ApiModelProperty(value = "版本号")
    @Schema(description = "版本号")
    @Size(max = 50, message = "版本号最大长度要小于 50")
    private String systemSoftVersionNumber;

    /**
     * 版本下载地址
     */
    @TableField(value = "system_soft_version_download_url")
    @ApiModelProperty(value = "版本下载地址")
    @Schema(description = "版本下载地址")
    @Size(max = 500, message = "版本下载地址最大长度要小于 500")
    private String systemSoftVersionDownloadUrl;

    /**
     * 镜像标签
     */
    @TableField(value = "system_soft_version_image_tag")
    @ApiModelProperty(value = "镜像标签")
    @Schema(description = "镜像标签")
    @Size(max = 100, message = "镜像标签最大长度要小于 100")
    private String systemSoftVersionImageTag;

    /**
     * 安装模板
     */
    @TableField(value = "system_soft_version_install_template")
    @ApiModelProperty(value = "安装模板")
    @Schema(description = "安装模板")
    @Size(max = 5000, message = "安装模板最大长度要小于 5000")
    private String systemSoftVersionInstallTemplate;

    /**
     * 版本描述
     */
    @TableField(value = "system_soft_version_description")
    @ApiModelProperty(value = "版本描述")
    @Schema(description = "版本描述")
    @Size(max = 1000, message = "版本描述最大长度要小于 1000")
    private String systemSoftVersionDescription;

    /**
     * 版本大小（字节）
     */
    @TableField(value = "system_soft_version_size")
    @ApiModelProperty(value = "版本大小")
    @Schema(description = "版本大小")
    private Long systemSoftVersionSize;

    /**
     * 版本状态
     */
    @TableField(value = "system_soft_version_status")
    @ApiModelProperty(value = "版本状态")
    @Schema(description = "版本状态")
    private Integer systemSoftVersionStatus;

    /**
     * 是否为最新版本
     */
    @TableField(value = "system_soft_version_is_latest")
    @ApiModelProperty(value = "是否为最新版本")
    @Schema(description = "是否为最新版本")
    private Integer systemSoftVersionIsLatest;

    /**
     * 是否为稳定版本
     */
    @TableField(value = "system_soft_version_is_stable")
    @ApiModelProperty(value = "是否为稳定版本")
    @Schema(description = "是否为稳定版本")
    private Integer systemSoftVersionIsStable;

    /**
     * 发布时间
     */
    @TableField(value = "system_soft_version_release_time")
    @ApiModelProperty(value = "发布时间")
    @Schema(description = "发布时间")
    private LocalDateTime systemSoftVersionReleaseTime;

    /**
     * 最小系统要求
     */
    @TableField(value = "system_soft_version_min_requirements")
    @ApiModelProperty(value = "最小系统要求")
    @Schema(description = "最小系统要求")
    @Size(max = 500, message = "最小系统要求最大长度要小于 500")
    private String systemSoftVersionMinRequirements;

    /**
     * 推荐系统要求
     */
    @TableField(value = "system_soft_version_recommended_requirements")
    @ApiModelProperty(value = "推荐系统要求")
    @Schema(description = "推荐系统要求")
    @Size(max = 500, message = "推荐系统要求最大长度要小于 500")
    private String systemSoftVersionRecommendedRequirements;

    /**
     * 版本备注
     */
    @TableField(value = "system_soft_version_remark")
    @ApiModelProperty(value = "版本备注")
    @Schema(description = "版本备注")
    @Size(max = 500, message = "版本备注最大长度要小于 500")
    private String systemSoftVersionRemark;

    /**
     * 扩展字段1
     */
    @TableField(value = "system_soft_version_ext_field1")
    @ApiModelProperty(value = "扩展字段1")
    @Schema(description = "扩展字段1")
    @Size(max = 255, message = "扩展字段1最大长度要小于 255")
    private String systemSoftVersionExtField1;

    /**
     * 扩展字段2
     */
    @TableField(value = "system_soft_version_ext_field2")
    @ApiModelProperty(value = "扩展字段2")
    @Schema(description = "扩展字段2")
    @Size(max = 255, message = "扩展字段2最大长度要小于 255")
    private String systemSoftVersionExtField2;

    /**
     * 扩展字段3
     */
    @TableField(value = "system_soft_version_ext_field3")
    @ApiModelProperty(value = "扩展字段3")
    @Schema(description = "扩展字段3")
    @Size(max = 255, message = "扩展字段3最大长度要小于 255")
    private String systemSoftVersionExtField3;
}