package com.chua.starter.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.chua.common.support.validator.group.AddGroup;
import com.chua.common.support.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目管理
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "项目管理")
@Schema(description = "项目管理")
@Data
@TableName(value = "sys_project")
public class SysProject extends SysBase {
    @TableId(value = "sys_project_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Integer sysProjectId;


    /**
     * 项目名称
     */
    @TableField(value = "sys_project_name")
    @ApiModelProperty(value = "项目名称")
    @Schema(description = "项目名称")
    @NotBlank(message = "名称不能为空", groups = {AddGroup.class})
    private String sysProjectName;


    /**
     * 图标
     */
    @TableField(value = "sys_project_icon")
    @ApiModelProperty(value = "图标")
    @Schema(description = "图标")
    private String sysProjectIcon;
    /**
     * 分组
     */
    @TableField(value = "sys_project_group")
    @ApiModelProperty(value = "分组")
    @Schema(description = "分组")
    private String sysProjectGroup;

    /**
     * 编码, 可自行作为唯一标识
     */
    @TableField(value = "sys_project_code")
    @ApiModelProperty(value = "编码, 可自行作为唯一标识")
    @Schema(description = "编码, 可自行作为唯一标识")
    @NotBlank(message = "编码不能为空", groups = {AddGroup.class})
    private String sysProjectCode;

    /**
     * appid
     */
    @TableField(value = "sys_project_app_id")
    @ApiModelProperty(value = "appid")
    @Schema(description = "appid")
    private String sysProjectAppId;

    /**
     * appsecret
     */
    @TableField(value = "sys_project_app_secret")
    @ApiModelProperty(value = "appsecret")
    @Schema(description = "appsecret")
    private String sysProjectAppSecret;

    /**
     * 签名
     */
    @TableField(value = "sys_project_sign")
    @ApiModelProperty(value = "签名")
    @Schema(description = "签名")
    private String sysProjectSign;

    /**
     * 是否删除0:未删除
     */
    @TableField(value = "sys_project_delete")
    @ApiModelProperty(value = "是否删除0:未删除")
    @Schema(description = "是否删除0:未删除")
    @JsonIgnore
    @TableLogic(value = "0", delval = "1")
    private Integer sysProjectDelete;

    /**
     * 是否启用0:未启用
     */
    @TableField(value = "sys_project_status")
    @ApiModelProperty(value = "是否启用0:未启用")
    @Schema(description = "是否启用0:未启用")
    private Integer sysProjectStatus;

    /**
     * cdn
     */
    @TableField(value = "sys_project_cdn")
    @ApiModelProperty(value = "cdn")
    @Schema(description = "cdn")
    private String sysProjectCdn;

    /**
     * 端点
     */
    @TableField(value = "sys_project_endpoint")
    @ApiModelProperty(value = "端点")
    @Schema(description = "端点")
    private String sysProjectEndpoint;

    /**
     * api地址
     */
    @TableField(value = "sys_project_api_address")
    @ApiModelProperty(value = "api地址")
    @Schema(description = "api地址")
    private String sysProjectApiAddress;

    /**
     * 字典项ID(字典1)
     */
    @TableField(value = "sys_project_dict_item_id")
    @ApiModelProperty(value = "厂家(字典1)")
    @Schema(description = "厂家(字典1)")
    private Integer sysProjectDictItemId;

    /**
     * 厂家(字典1)
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "厂家(字典1)")
    @Schema(description = "厂家(字典1)")
    private String sysProjectDictItemLabel;

    /**
     * 描述
     */
    @TableField(value = "sys_project_remark")
    @ApiModelProperty(value = "描述")
    @Schema(description = "描述")
    private String sysProjectRemark;

    /**
     * 功能
     */
    @TableField(value = "sys_project_function")
    @ApiModelProperty(value = "功能; 字典表6")
    @Schema(description = "功能; 字典表6")
    private String sysProjectFunction;


    /**
     * 默认功能
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "默认功能")
    @Schema(description = "默认功能")
    private String sysProjectFunctionDefaultLabel;
}