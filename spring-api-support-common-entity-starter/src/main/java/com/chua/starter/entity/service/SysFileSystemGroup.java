package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 文件系统分组表
 * 用于管理文件分组，支持树形结构
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@Schema(description = "文件系统分组表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_file_system_group")
public class SysFileSystemGroup extends SysBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分组ID
     */
    @TableId(value = "sys_file_system_group_id", type = IdType.AUTO)
    @Schema(description = "分组ID")
    private Integer sysFileSystemGroupId;

    /**
     * 分组名称
     */
    @TableField(value = "sys_file_system_group_name")
    @Schema(description = "分组名称")
    @Size(max = 100, message = "分组名称最大长度要小于 100")
    private String sysFileSystemGroupName;

    /**
     * 父分组ID
     */
    @TableField(value = "sys_file_system_group_parent_id")
    @Schema(description = "父分组ID，顶级分组为0")
    private Integer sysFileSystemGroupParentId;

    /**
     * 分组路径
     */
    @TableField(value = "sys_file_system_group_path")
    @Schema(description = "分组路径")
    @Size(max = 255, message = "分组路径最大长度要小于 255")
    private String sysFileSystemGroupPath;

    /**
     * 分组完整路径
     */
    @TableField(value = "sys_file_system_group_full_path")
    @Schema(description = "分组完整路径")
    @Size(max = 500, message = "分组完整路径最大长度要小于 500")
    private String sysFileSystemGroupFullPath;

    /**
     * 分组层级
     */
    @TableField(value = "sys_file_system_group_level")
    @Schema(description = "分组层级，从1开始")
    private Integer sysFileSystemGroupLevel;

    /**
     * 分组树路径（ID链路，用于快速查询）
     */
    @TableField(value = "sys_file_system_group_tree_path")
    @Schema(description = "分组树路径（ID链路，如：1/2/3）")
    @Size(max = 500, message = "分组树路径最大长度要小于 500")
    private String sysFileSystemGroupTreePath;

    /**
     * 分组描述
     */
    @TableField(value = "sys_file_system_group_description")
    @Schema(description = "分组描述")
    @Size(max = 500, message = "分组描述最大长度要小于 500")
    private String sysFileSystemGroupDescription;

    /**
     * 是否默认分组
     */
    @TableField(value = "sys_file_system_group_is_default")
    @Schema(description = "是否默认分组")
    private Boolean sysFileSystemGroupIsDefault;

    /**
     * 分组排序
     */
    @TableField(value = "sys_file_system_group_sort")
    @Schema(description = "分组排序，数字越小越靠前")
    private Integer sysFileSystemGroupSort;

    /**
     * 分组状态: 0-禁用, 1-启用
     */
    @TableField(value = "sys_file_system_group_status")
    @Schema(description = "分组状态: 0-禁用, 1-启用")
    private Integer sysFileSystemGroupStatus;

    /**
     * 分组图标
     */
    @TableField(value = "sys_file_system_group_icon")
    @Schema(description = "分组图标")
    @Size(max = 100, message = "分组图标最大长度要小于 100")
    private String sysFileSystemGroupIcon;

    /**
     * 分组颜色
     */
    @TableField(value = "sys_file_system_group_color")
    @Schema(description = "分组颜色")
    @Size(max = 20, message = "分组颜色最大长度要小于 20")
    private String sysFileSystemGroupColor;

    /**
     * 子分组列表（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "子分组列表")
    private List<SysFileSystemGroup> children;

    /**
     * 文件数量（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "文件数量")
    private Long fileCount;

    /**
     * 分组状态枚举
     */
    public static class Status {
        /**
         * 禁用
         */
        public static final int DISABLED = 0;
        /**
         * 启用
         */
        public static final int ENABLED = 1;
    }

    /**
     * 默认分组类型
     */
    public static class DefaultGroupType {
        /**
         * 图片分组
         */
        public static final String IMAGE = "images";
        /**
         * 视频分组
         */
        public static final String VIDEO = "videos";
        /**
         * 文档分组
         */
        public static final String DOCUMENT = "documents";
        /**
         * 音频分组
         */
        public static final String AUDIO = "audios";
        /**
         * 默认分组
         */
        public static final String DEFAULT = "default";
    }
}
