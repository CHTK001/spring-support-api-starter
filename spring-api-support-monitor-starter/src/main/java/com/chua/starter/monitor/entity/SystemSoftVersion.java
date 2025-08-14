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
 * 软件版本信息
 *
 * 记录不同版本的镜像标签、下载地址、安装脚本模板等
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_soft_version")
@ApiModel("软件版本信息")
@Schema(name = "SystemSoftVersion")
public class SystemSoftVersion extends SysBase {

    @TableId(value = "system_soft_version_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    @NotNull(message = "主键不能为空", groups = { UpdateGroup.class })
    private Integer systemSoftVersionId;

    /** 关联软件ID */
    @TableField("system_soft_id")
    @ApiModelProperty("关联软件ID")
    private Integer systemSoftId;

    /** 版本号/标签 */
    @TableField("version")
    @ApiModelProperty("版本号/标签")
    @Size(max = 128, message = "版本号最大长度要小于 128")
    private String version;

    /** 下载地址（可选） */
    @TableField("download_url")
    @ApiModelProperty("下载地址")
    private String downloadUrl;

    /** 镜像标签（如 nginx:1.25） */
    @TableField("image_tag")
    @ApiModelProperty("镜像标签")
    private String imageTag;

    /** 安装参数/脚本模板（JSON/YAML） */
    @TableField("install_template")
    @ApiModelProperty("安装参数/脚本模板（JSON/YAML）")
    private String installTemplate;
}
