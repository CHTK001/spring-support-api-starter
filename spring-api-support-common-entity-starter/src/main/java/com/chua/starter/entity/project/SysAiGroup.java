package com.chua.starter.entity.project;

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
 * @since 2025/9/4 13:07
 */

/**
 * 分组
 */
@Schema(description = "分组")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_ai_group")
public class SysAiGroup extends SysBase {
    /**
     * ID
     */
    @TableId(value = "sys_ai_group_id", type = IdType.AUTO)
    @Schema(description = "ID")
    private Integer sysAiGroupId;

    /**
     * 名称
     */
    @TableField(value = "sys_ai_group_name")
    @Schema(description = "名称")
    private String sysAiGroupName;

    /**
     * 项目ID
     */
    @TableField(value = "sys_project_id")
    @Schema(description = "项目ID")
    private Integer sysProjectId;

    /**
     * 账号ID
     */
    @TableField(value = "sys_user_id")
    @Schema(description = "账号ID")
    private String sysUserId;

    /**
     * 分组颜色
     */
    @TableField(value = "sys_ai_group_color")
    @Schema(description = "分组颜色")
    private String sysAiGroupColor;

    /**
     * 分组配置
     */
    @TableField(value = "sys_ai_group_config")
    @Schema(description = "分组配置")
    private String sysAiGroupConfig;
}