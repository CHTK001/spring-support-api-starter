package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CH
 * @since 2025/10/21 13:38
 */
@Schema
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "system_soft_version")
public class SystemSoftVersion extends SysBase {
    /**
     * 系统软件版本ID
     */
    @TableId(value = "system_soft_version_id", type = IdType.AUTO)
    @Schema(description = "系统软件版本ID")
    private Integer systemSoftVersionId;

    /**
     * 关联的软件ID
     */
    @TableField(value = "system_soft_id")
    @Schema(description = "关联的软件ID")
    private Integer systemSoftId;

    /**
     * 版本名称
     */
    @TableField(value = "system_soft_version_name")
    @Schema(description = "版本名称")
    private String systemSoftVersionName;

    /**
     * 摘要
     */
    @TableField(value = "sys_soft_version_digest")
    @Schema(description = "摘要")
    private String sysSoftVersionDigest;

    /**
     * 版本号
     */
    @TableField(value = "system_soft_version_number")
    @Schema(description = "版本号")
    private String systemSoftVersionNumber;

    /**
     * 版本下载地址
     */
    @TableField(value = "system_soft_version_download_url")
    @Schema(description = "版本下载地址")
    private String systemSoftVersionDownloadUrl;

    /**
     * 镜像标签
     */
    @TableField(value = "system_soft_version_image_tag")
    @Schema(description = "镜像标签")
    private String systemSoftVersionImageTag;

    /**
     * 安装模板
     */
    @TableField(value = "system_soft_version_install_template")
    @Schema(description = "安装模板")
    private String systemSoftVersionInstallTemplate;

    /**
     * 版本描述
     */
    @TableField(value = "system_soft_version_description")
    @Schema(description = "版本描述")
    private String systemSoftVersionDescription;

    /**
     * 版本大小
     */
    @TableField(value = "system_soft_version_size")
    @Schema(description = "版本大小")
    private Long systemSoftVersionSize;

    /**
     * 版本状态
     */
    @TableField(value = "system_soft_version_status")
    @Schema(description = "版本状态")
    private Integer systemSoftVersionStatus;

    /**
     * 是否为最新版本
     */
    @TableField(value = "system_soft_version_is_latest")
    @Schema(description = "是否为最新版本")
    private Integer systemSoftVersionIsLatest;

    /**
     * 是否为稳定版本
     */
    @TableField(value = "system_soft_version_is_stable")
    @Schema(description = "是否为稳定版本")
    private Integer systemSoftVersionIsStable;

    /**
     * 发布时间
     */
    @TableField(value = "system_soft_version_release_time")
    @Schema(description = "发布时间")
    private LocalDateTime systemSoftVersionReleaseTime;

    /**
     * 最小系统要求
     */
    @TableField(value = "system_soft_version_min_requirements")
    @Schema(description = "最小系统要求")
    private String systemSoftVersionMinRequirements;

    /**
     * 推荐系统要求
     */
    @TableField(value = "system_soft_version_recommended_requirements")
    @Schema(description = "推荐系统要求")
    private String systemSoftVersionRecommendedRequirements;

    /**
     * 版本备注
     */
    @TableField(value = "system_soft_version_remark")
    @Schema(description = "版本备注")
    private String systemSoftVersionRemark;

    /**
     * 扩展字段1
     */
    @TableField(value = "system_soft_version_ext_field1")
    @Schema(description = "扩展字段1")
    private String systemSoftVersionExtField1;

    /**
     * 扩展字段2
     */
    @TableField(value = "system_soft_version_ext_field2")
    @Schema(description = "扩展字段2")
    private String systemSoftVersionExtField2;

    /**
     * 扩展字段3
     */
    @TableField(value = "system_soft_version_ext_field3")
    @Schema(description = "扩展字段3")
    private String systemSoftVersionExtField3;
}