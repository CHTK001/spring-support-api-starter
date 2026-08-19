package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 默认功能 <---->对象的项目; e.g. 短信默认采用那个项目配置
 *
 * @since 2026/08/03
 * @author CH
 */
@Schema(description = "默认功能 <---->对象的项目; e.g. 短信默认采用那个项目配置")
@Data
@TableName(value = "sys_project_default")
public class SysProjectDefault {
    /**
     * 默认功能主键ID
     */
    @TableId(value = "sys_project_default_id", type = IdType.AUTO)
    @Schema(description = "")
    /** SYSproject默认ID */
    private Integer sysProjectDefaultId;

    /**
     * 类型； 字典表6;
     */
    @TableField(value = "sys_project_default_type")
    @Schema(description = "类型； 字典表6;")
    /** SYSproject默认类型 */
    private String sysProjectDefaultType;

    /**
     * 项目ID
     */
    @TableField(value = "sys_project_id")
    @Schema(description = "项目ID")
    /** SYSprojectID */
    private Integer sysProjectId;
}
