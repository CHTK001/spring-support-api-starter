package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysTenantBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * OSS
 */
@ApiModel(description = "OSS")
@Schema(description = "OSS")
@Data
@TableName(value = "sys_file_storage")
public class SysFileStorage extends SysTenantBase {
    /**
     * 主键
     */
    @TableId(value = "fs_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    @Schema(description = "主键")
    @NotNull(message = "唯一标识不能为空", groups = UpdateGroup.class)
    private Integer fsId;

    /**
     * name
     */
    @TableField(value = "fs_name")
    @ApiModelProperty(value = "名称")
    @Schema(description = "名称")
    @Size(max = 255, message = "名称最大长度要小于 255")
    @NotBlank(message = "名称不能为空", groups = AddGroup.class)
    private String fsName;

    /**
     * bucket
     */
    @TableField(value = "fs_bucket")
    @ApiModelProperty(value = "bucket")
    @Schema(description = "bucket")
    @Size(max = 255, message = "bucket最大长度要小于 255")
    @NotBlank(message = "bucket不能为空", groups = AddGroup.class)
    private String fsBucket;

    /**
     * OSS类型
     */
    @TableField(value = "fs_type")
    @ApiModelProperty(value = "OSS类型")
    @Schema(description = "OSS类型")
    @Size(max = 255, message = "OSS类型最大长度要小于 255")
    @NotBlank(message = "OSS类型不能为空", groups = AddGroup.class)
    private String fsType;

    /**
     * 图片滤镜
     */
    @TableField(value = "fs_filter")
    @ApiModelProperty(value = "图片滤镜")
    @Schema(description = "图片滤镜")
    private String fsFilter;

    /**
     * 插件, 文件上传时预处理
     */
    @TableField(value = "fs_plugin")
    @ApiModelProperty(value = "插件; 文件上传时预处理")
    @Schema(description = "插件; 文件上传时预处理")
    private String fsPlugin;

    /**
     * 三方服务器地址
     */
    @TableField(value = "fs_endpoint")
    @ApiModelProperty(value = "三方服务器地址")
    @Schema(description = "三方服务器地址")
    @Size(max = 255, message = "三方服务器地址最大长度要小于 255")
    private String fsEndpoint;

    /**
     * 前端访问地址
     */
    @TableField(value = "fs_domain")
    @ApiModelProperty(value = "前端访问地址")
    @Schema(description = "前端访问地址")
    @Size(max = 255, message = "前端访问地址最大长度要小于 255")
    private String fsDomain;

    /**
     * 文件存储位置
     */
    @TableField(value = "fs_storage_path")
    @ApiModelProperty(value = "文件存储位置")
    @Schema(description = "文件存储位置")
    @Size(max = 255, message = "文件存储位置最大长度要小于 255")
    private String fsStoragePath;

    /**
     * ak
     */
    @TableField(value = "fs_access_key_id")
    @ApiModelProperty(value = "ak")
    @Schema(description = "ak")
    @Size(max = 255, message = "ak最大长度要小于 255")
    private String fsAccessKeyId;

    /**
     * sk
     */
    @TableField(value = "fs_access_key_secret")
    @ApiModelProperty(value = "sk")
    @Schema(description = "sk")
    @Size(max = 255, message = "sk最大长度要小于 255")
    private String fsAccessKeySecret;

    /**
     * 状态(1:正常;0:禁用)
     */
    @TableField(value = "fs_status")
    @ApiModelProperty(value = "状态(1:正常;0:禁用)")
    @Schema(description = "状态(1:正常;0:禁用)")
    private Integer fsStatus;

}