package com.chua.starter.monitor.starter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.datasource.annotation.ColumnDesc;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 文件系统分组管理
 *
 * @author CH
 * @since 2024/12/30
 */
@ApiModel(description = "文件系统分组管理")
@Schema(description = "文件系统分组管理")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "file_system_group")
public class FileSystemGroup extends SysBase implements Serializable {

    /**
     * 分组ID
     */
    @TableId(value = "file_system_group_id", type = IdType.AUTO)
    @ApiModelProperty(value = "分组ID")
    @Schema(description = "分组ID")
    @NotNull(message = "分组ID不能为null")
    private Integer fileSystemGroupId;

    /**
     * 分组名称
     */
    @ColumnDesc("分组名称")
    @TableField(value = "file_system_group_name")
    @ApiModelProperty(value = "分组名称")
    @Schema(description = "分组名称")
    @Size(max = 100, message = "分组名称最大长度要小于 100")
    private String fileSystemGroupName;

    /**
     * 父分组ID
     */
    @ColumnDesc("父分组ID")
    @TableField(value = "file_system_group_parent_id")
    @ApiModelProperty(value = "父分组ID")
    @Schema(description = "父分组ID")
    private Integer fileSystemGroupParentId;

    /**
     * 分组路径
     */
    @ColumnDesc("分组路径")
    @TableField(value = "file_system_group_path")
    @ApiModelProperty(value = "分组路径")
    @Schema(description = "分组路径")
    @Size(max = 255, message = "分组路径最大长度要小于 255")
    private String fileSystemGroupPath;

    /**
     * 分组完整路径
     */
    @ColumnDesc("分组完整路径")
    @TableField(value = "file_system_group_full_path")
    @ApiModelProperty(value = "分组完整路径")
    @Schema(description = "分组完整路径")
    @Size(max = 500, message = "分组完整路径最大长度要小于 500")
    private String fileSystemGroupFullPath;

    /**
     * 分组层级
     */
    @ColumnDesc("分组层级")
    @TableField(value = "file_system_group_level")
    @ApiModelProperty(value = "分组层级")
    @Schema(description = "分组层级")
    private Integer fileSystemGroupLevel;

    /**
     * 分组树路径
     */
    @ColumnDesc("分组树路径")
    @TableField(value = "file_system_group_tree_path")
    @ApiModelProperty(value = "分组树路径")
    @Schema(description = "分组树路径")
    @Size(max = 500, message = "分组树路径最大长度要小于 500")
    private String fileSystemGroupTreePath;

    /**
     * 分组描述
     */
    @ColumnDesc("分组描述")
    @TableField(value = "file_system_group_description")
    @ApiModelProperty(value = "分组描述")
    @Schema(description = "分组描述")
    @Size(max = 500, message = "分组描述最大长度要小于 500")
    private String fileSystemGroupDescription;

    /**
     * 是否默认分组
     */
    @ColumnDesc("是否默认分组")
    @TableField(value = "file_system_group_is_default")
    @ApiModelProperty(value = "是否默认分组")
    @Schema(description = "是否默认分组")
    private Boolean fileSystemGroupIsDefault;

    /**
     * 分组排序
     */
    @ColumnDesc("分组排序")
    @TableField(value = "file_system_group_sort")
    @ApiModelProperty(value = "分组排序")
    @Schema(description = "分组排序")
    private Integer fileSystemGroupSort;

    /**
     * 分组状态
     */
    @ColumnDesc("分组状态")
    @TableField(value = "file_system_group_status")
    @ApiModelProperty(value = "分组状态")
    @Schema(description = "分组状态")
    private Integer fileSystemGroupStatus;

    /**
     * 分组图标
     */
    @ColumnDesc("分组图标")
    @TableField(value = "file_system_group_icon")
    @ApiModelProperty(value = "分组图标")
    @Schema(description = "分组图标")
    @Size(max = 100, message = "分组图标最大长度要小于 100")
    private String fileSystemGroupIcon;

    /**
     * 分组颜色
     */
    @ColumnDesc("分组颜色")
    @TableField(value = "file_system_group_color")
    @ApiModelProperty(value = "分组颜色")
    @Schema(description = "分组颜色")
    @Size(max = 20, message = "分组颜色最大长度要小于 20")
    private String fileSystemGroupColor;

    /**
     * 子分组列表（非数据库字段）
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "子分组列表")
    @Schema(description = "子分组列表")
    private List<FileSystemGroup> children;

    /**
     * 文件数量（非数据库字段）
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "文件数量")
    @Schema(description = "文件数量")
    private Long fileCount;

    private static final long serialVersionUID = 1L;

    /**
     * 分组状态枚举
     */
    public static class GroupStatus {
        public static final int DISABLED = 0; // 禁用
        public static final int ENABLED = 1; // 启用
    }

    /**
     * 默认分组类型
     */
    public static class DefaultGroupType {
        public static final String IMAGE = "images"; // 图片分组
        public static final String VIDEO = "videos"; // 视频分组
        public static final String DOCUMENT = "documents"; // 文档分组
        public static final String DEFAULT = "default"; // 默认分组
    }
}
