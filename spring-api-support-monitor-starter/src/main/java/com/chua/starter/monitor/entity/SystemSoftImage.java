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
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

import java.time.LocalDateTime;

/**
 * 系统软件镜像信息实体类
 *
 * @author CH
 * @since 2025/01/15
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "系统软件镜像信息")
@Schema(description = "系统软件镜像信息")
@Data
@TableName(value = "system_soft_image")
public class SystemSoftImage extends SysBase {

    /**
     * 系统软件镜像ID
     */
    @TableId(value = "system_soft_image_id", type = IdType.AUTO)
    @ApiModelProperty(value = "系统软件镜像ID")
    @Schema(description = "系统软件镜像ID")
    @NotNull(message = "系统软件镜像ID不能为空", groups = {UpdateGroup.class})
    private Integer systemSoftImageId;

    /**
     * 关联的软件ID
     */
    @TableField(value = "system_soft_id")
    @ApiModelProperty(value = "关联的软件ID")
    @Schema(description = "关联的软件ID")
    @NotNull(message = "关联的软件ID不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private Integer systemSoftId;

    /**
     * 关联的服务器ID
     */
    @TableField(value = "system_soft_image_server_id")
    @ApiModelProperty(value = "关联的服务器ID")
    @Schema(description = "关联的服务器ID")
    @NotNull(message = "关联的服务器ID不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private Integer systemSoftImageServerId;

    /**
     * 服务器名称
     */
    @TableField(value = "system_soft_image_server_name")
    @ApiModelProperty(value = "服务器名称")
    @Schema(description = "服务器名称")
    @Size(max = 255, message = "服务器名称最大长度要小于 255")
    private String systemSoftImageServerName;

    /**
     * Docker镜像ID
     */
    @TableField(value = "system_soft_image_image_id")
    @ApiModelProperty(value = "Docker镜像ID")
    @Schema(description = "Docker镜像ID")
    @Size(max = 100, message = "Docker镜像ID最大长度要小于 100")
    private String systemSoftImageImageId;

    /**
     * 镜像名称
     */
    @TableField(value = "system_soft_image_name")
    @ApiModelProperty(value = "镜像名称")
    @Schema(description = "镜像名称")
    @NotBlank(message = "镜像名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
    @Size(max = 255, message = "镜像名称最大长度要小于 255")
    private String systemSoftImageName;

    /**
     * 镜像标签
     */
    @TableField(value = "system_soft_image_tag")
    @ApiModelProperty(value = "镜像标签")
    @Schema(description = "镜像标签")
    @Size(max = 100, message = "镜像标签最大长度要小于 100")
    private String systemSoftImageTag;

    /**
     * 镜像完整名称
     */
    @TableField(value = "system_soft_image_full_name")
    @ApiModelProperty(value = "镜像完整名称")
    @Schema(description = "镜像完整名称")
    @Size(max = 500, message = "镜像完整名称最大长度要小于 500")
    private String systemSoftImageFullName;

    /**
     * 镜像仓库地址
     */
    @TableField(value = "system_soft_image_repository")
    @ApiModelProperty(value = "镜像仓库地址")
    @Schema(description = "镜像仓库地址")
    @Size(max = 255, message = "镜像仓库地址最大长度要小于 255")
    private String systemSoftImageRepository;

    /**
     * 镜像大小（字节）
     */
    @TableField(value = "system_soft_image_size")
    @ApiModelProperty(value = "镜像大小")
    @Schema(description = "镜像大小")
    private Long systemSoftImageSize;

    /**
     * 镜像创建时间
     */
    @TableField(value = "system_soft_image_created")
    @ApiModelProperty(value = "镜像创建时间")
    @Schema(description = "镜像创建时间")
    private LocalDateTime systemSoftImageCreated;

    /**
     * 镜像架构
     */
    @TableField(value = "system_soft_image_architecture")
    @ApiModelProperty(value = "镜像架构")
    @Schema(description = "镜像架构")
    @Size(max = 50, message = "镜像架构最大长度要小于 50")
    private String systemSoftImageArchitecture;

    /**
     * 操作系统类型
     */
    @TableField(value = "system_soft_image_os_type")
    @ApiModelProperty(value = "操作系统类型")
    @Schema(description = "操作系统类型")
    @Size(max = 50, message = "操作系统类型最大长度要小于 50")
    private String systemSoftImageOsType;

    /**
     * 镜像摘要
     */
    @TableField(value = "system_soft_image_digest")
    @ApiModelProperty(value = "镜像摘要")
    @Schema(description = "镜像摘要")
    @Size(max = 255, message = "镜像摘要最大长度要小于 255")
    private String systemSoftImageDigest;

    /**
     * 镜像状态
     */
    @TableField(value = "system_soft_image_status", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class)
    @ApiModelProperty(value = "镜像状态")
    @Schema(description = "镜像状态")
    @Size(max = 50, message = "镜像状态最大长度要小于 50")
    private SystemSoftImageStatus systemSoftImageStatus;

    /**
     * 是否官方镜像
     */
    @TableField(value = "system_soft_image_is_official")
    @ApiModelProperty(value = "是否官方镜像")
    @Schema(description = "是否官方镜像")
    private Integer systemSoftImageIsOfficial;

    /**
     * 镜像描述
     */
    @TableField(value = "system_soft_image_description")
    @ApiModelProperty(value = "镜像描述")
    @Schema(description = "镜像描述")
    @Size(max = 1000, message = "镜像描述最大长度要小于 1000")
    private String systemSoftImageDescription;

    /**
     * 标签数量
     */
    @TableField(value = "system_soft_image_tag_count")
    @ApiModelProperty(value = "标签数量")
    @Schema(description = "标签数量")
    private Integer systemSoftImageTagCount;

    /**
     * 拉取次数
     */
    @TableField(value = "system_soft_image_pull_count")
    @ApiModelProperty(value = "拉取次数")
    @Schema(description = "拉取次数")
    private Long systemSoftImagePullCount;

    /**
     * 星标数量
     */
    @TableField(value = "system_soft_image_star_count")
    @ApiModelProperty(value = "星标数量")
    @Schema(description = "星标数量")
    private Long systemSoftImageStarCount;

    /**
     * 最后拉取时间
     */
    @TableField(value = "system_soft_image_last_pulled")
    @ApiModelProperty(value = "最后拉取时间")
    @Schema(description = "最后拉取时间")
    private LocalDateTime systemSoftImageLastPulled;

    /**
     * 最后推送时间
     */
    @TableField(value = "system_soft_image_last_pushed")
    @ApiModelProperty(value = "最后推送时间")
    @Schema(description = "最后推送时间")
    private LocalDateTime systemSoftImageLastPushed;

    /**
     * 镜像备注
     */
    @TableField(value = "system_soft_image_remark")
    @ApiModelProperty(value = "镜像备注")
    @Schema(description = "镜像备注")
    @Size(max = 500, message = "镜像备注最大长度要小于 500")
    private String systemSoftImageRemark;

    /**
     * 扩展字段1
     */
    @TableField(value = "system_soft_image_ext_field1")
    @ApiModelProperty(value = "扩展字段1")
    @Schema(description = "扩展字段1")
    @Size(max = 255, message = "扩展字段1最大长度要小于 255")
    private String systemSoftImageExtField1;

    /**
     * 扩展字段2
     */
    @TableField(value = "system_soft_image_ext_field2")
    @ApiModelProperty(value = "扩展字段2")
    @Schema(description = "扩展字段2")
    @Size(max = 255, message = "扩展字段2最大长度要小于 255")
    private String systemSoftImageExtField2;

    /**
     * 扩展字段3
     */
    @TableField(value = "system_soft_image_ext_field3")
    @ApiModelProperty(value = "扩展字段3")
    @Schema(description = "扩展字段3")
    @Size(max = 255, message = "扩展字段3最大长度要小于 255")
    private String systemSoftImageExtField3;
}