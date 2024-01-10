package com.chua.starter.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * OSS
 */
@ApiModel(description = "OSS")
@Schema(description = "OSS")
@Data
@TableName(value = "sys_file_storage")
public class SysFileStorage {
    /**
     * 主键
     */
    @TableId(value = "fs_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    @Schema(description = "主键")
    @NotNull(message = "主键不能为null")
    private Integer fsId;

    /**
     * bucket
     */
    @TableField(value = "fs_bucket")
    @ApiModelProperty(value = "bucket")
    @Schema(description = "bucket")
    @Size(max = 255, message = "bucket最大长度要小于 255")
    private String fsBucket;

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

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    @ApiModelProperty(value = "租户ID")
    @Schema(description = "租户ID")
    private Integer tenantId;

    /**
     * 创建人
     */
    @TableField(value = "create_name")
    @ApiModelProperty(value = "创建人")
    @Schema(description = "创建人")
    @Size(max = 255, message = "创建人最大长度要小于 255")
    private String createName;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    @ApiModelProperty(value = "创建人")
    @Schema(description = "创建人")
    private Integer createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    @ApiModelProperty(value = "更新时间")
    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 更新人
     */
    @TableField(value = "update_name")
    @ApiModelProperty(value = "更新人")
    @Schema(description = "更新人")
    @Size(max = 255, message = "更新人最大长度要小于 255")
    private String updateName;

    /**
     * 更新人
     */
    @TableField(value = "update_by")
    @ApiModelProperty(value = "更新人")
    @Schema(description = "更新人")
    private Integer updateBy;
}