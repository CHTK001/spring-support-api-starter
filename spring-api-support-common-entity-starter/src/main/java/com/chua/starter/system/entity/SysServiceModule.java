package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模块
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "模块")
@Schema(description = "模块")
@Data
@TableName(value = "sys_service_module")
public class SysServiceModule extends SysBase {
    @TableId(value = "sys_service_module_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "模块ID不能为空", groups = {UpdateGroup.class})
    private Integer sysServiceModuleId;

    /**
     * 模块名称
     */
    @TableField(value = "sys_service_module_name")
    @ApiModelProperty(value = "模块名称")
    @Schema(description = "模块名称")
    @NotBlank(message = "模块名称不能为空", groups = {AddGroup.class})
    private String sysServiceModuleName;

    /**
     * 模块编码
     */
    @TableField(value = "sys_service_module_code")
    @ApiModelProperty(value = "模块编码")
    @Schema(description = "模块编码")
    @NotBlank(message = "模块编码不能为空", groups = {AddGroup.class})
    private String sysServiceModuleCode;

    /**
     * 是否删除; 0:正常
     */
    @TableField(value = "sys_service_module_delete")
    @ApiModelProperty(value = "是否删除; 0:正常")
    @Schema(description = "是否删除; 0:正常")
    @TableLogic(value = "0", delval = "1")
    private Integer sysServiceModuleDelete;

    /**
     * 是否禁用; 0:正常
     */
    @TableField(value = "sys_service_module_status")
    @ApiModelProperty(value = "是否禁用; 0:正常")
    @Schema(description = "是否禁用; 0:正常")
    private Integer sysServiceModuleStatus;

    /**
     * 优先级
     */
    @TableField(value = "sys_service_module_sort")
    @ApiModelProperty(value = "优先级")
    @Schema(description = "优先级")
    private Integer sysServiceModuleSort;
    /**
     * 版本
     */
    @TableField(value = "sys_service_module_version")
    @ApiModelProperty(value = "版本")
    @Schema(description = "版本")
    private String sysServiceModuleVersion;


    /**
     * 类型; API, SERVICE
     */
    @TableField(value = "sys_service_module_type")
    @ApiModelProperty(value = "类型; API, SERVICE")
    @Schema(description = "类型; API, SERVICE")
    @NotBlank(message = "类型不能为空", groups = {AddGroup.class})
    private String sysServiceModuleType;

    /**
     * 菜单标签
     */
    @TableField(value = "sys_service_module_menu_tags")
    @ApiModelProperty(value = "模块菜单标签")
    @Schema(description = "模块菜单标签")
    private String sysServiceModuleMenuTags;
    /**
     * 菜单标签
     */
    @TableField(value = "sys_service_module_menu_tags2")
    @ApiModelProperty(value = "模块菜单标签(包含父标签)")
    @Schema(description = "模块菜单标签(包含父标签)")
    private String sysServiceModuleMenuTags2;

}