package com.chua.starter.entity.project;

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
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目管理
 */
@ApiModel(description="项目管理")
@Schema(description="项目管理")
@Data
@EqualsAndHashCode(callSuper=true)
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
    @ApiModelProperty(value="图标")
    @Schema(description="图标")
    private String sysProjectIcon;

    /**
     * 分组
     */
    @TableField(value = "sys_project_group")
    @ApiModelProperty(value="分组")
    @Schema(description="分组")
    private String sysProjectGroup;

    /**
     * 编码, 可自行作为唯一标识
     */
    @TableField(value = "sys_project_code")
    @ApiModelProperty(value="编码, 可自行作为唯一标识")
    @Schema(description="编码, 可自行作为唯一标识")
    private String sysProjectCode;

    /**
     * appid
     */
    @TableField(value = "sys_project_app_id")
    @ApiModelProperty(value="appid")
    @Schema(description="appid")
    private String sysProjectAppId;

    /**
     * appsecret
     */
    @TableField(value = "sys_project_app_secret")
    @ApiModelProperty(value="appsecret")
    @Schema(description="appsecret")
    private String sysProjectAppSecret;

    /**
     * 签名
     */
    @TableField(value = "sys_project_sign")
    @ApiModelProperty(value = "签名/项目标识")
    @Schema(description = "签名/项目标识")
    private String sysProjectSign;
    /**
     * 密钥
     */
    @TableField(value = "sys_project_app_key")
    @ApiModelProperty(value = "密钥")
    @Schema(description = "密钥")
    private String sysProjectAppKey;

    /**
     * 签名密钥
     */
    @TableField(value = "sys_project_sign_code")
    @ApiModelProperty(value = "签名编码/项目标识编码")
    @Schema(description = "签名编码/项目标识编码")
    private String sysProjectSignCode;

    /**
     * 是否删除0:未删除
     */
    @TableField(value = "sys_project_delete")
    @ApiModelProperty(value="是否删除0:未删除")
    @Schema(description="是否删除0:未删除")
    private Integer sysProjectDelete;

    /**
     * 是否启用0:未启用
     */
    @TableField(value = "sys_project_status")
    @ApiModelProperty(value="是否启用0:未启用")
    @Schema(description="是否启用0:未启用")
    private Integer sysProjectStatus;

    /**
     * cdn
     */
    @TableField(value = "sys_project_cdn")
    @ApiModelProperty(value="cdn")
    @Schema(description="cdn")
    private String sysProjectCdn;

    /**
     * 端点
     */
    @TableField(value = "sys_project_endpoint")
    @ApiModelProperty(value="端点")
    @Schema(description="端点")
    private String sysProjectEndpoint;
    /**
     * 本地路径
     */
    @TableField(value = "sys_project_local_path")
    @ApiModelProperty(value = "本地路径")
    @Schema(description = "本地路径")
    private String sysProjectLocalPath;

    /**
     * api地址
     */
    @TableField(value = "sys_project_api_address")
    @ApiModelProperty(value="api地址")
    @Schema(description="api地址")
    private String sysProjectApiAddress;
    /**
     * 临时文件路径
     */
    @TableField(value = "sys_project_location_temp_path")
    @ApiModelProperty(value = "临时文件路径")
    @Schema(description = "临时文件路径")
    private String sysProjectLocationTempPath;
    /**
     * oss地址
     */
    @TableField(value = "sys_project_location_oss_address")
    @ApiModelProperty(value = "oss地址")
    @Schema(description = "oss地址")
    private String sysProjectLocationOssAddress;

    /**
     * 厂家(字典1)
     */
    @TableField(value = "sys_project_vender")
    @ApiModelProperty(value="厂家(字典1)")
    @Schema(description="厂家(字典1)")
    private Integer sysProjectVender;
    /**
     * 排序
     */
    @TableField(value = "sys_project_sort")
    @ApiModelProperty(value = "排序")
    @Schema(description = "排序")
    private Integer sysProjectSort;

    /**
     * 厂家(字典1)
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "厂家编码(字典1)")
    @Schema(description = "厂家编码(字典1)")
    private String sysProjectDictItemCode;

    /**
     * 厂家(字典1)
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "厂家名称(字典1)")
    @Schema(description = "厂家名称(字典1)")
    private String sysProjectDictItemLabel;

    /**
     * 描述
     */
    @TableField(value = "sys_project_remark")
    @ApiModelProperty(value="描述")
    @Schema(description="描述")
    private String sysProjectRemark;

    /**
     * 功能; 字典表6
     */
    @TableField(value = "sys_project_function")
    @ApiModelProperty(value="功能; 字典表6")
    @Schema(description="功能; 字典表6")
    private String sysProjectFunction;

    /**
     * smtp主机
     */
    @TableField(value = "sys_project_smtp_host")
    @ApiModelProperty(value="smtp主机")
    @Schema(description="smtp主机")
    private String sysProjectSmtpHost;

    /**
     * smtp端口
     */
    @TableField(value = "sys_project_smtp_port")
    @ApiModelProperty(value="smtp端口")
    @Schema(description="smtp端口")
    private Integer sysProjectSmtpPort;

    /**
     * smtp密码
     */
    @TableField(value = "sys_project_smtp_password")
    @ApiModelProperty(value="smtp密码")
    @Schema(description="smtp密码")
    private String sysProjectSmtpPassword;

    /**
     * smtp主题账号
     */
    @TableField(value = "sys_project_smtp_from")
    @ApiModelProperty(value="smtp主题账号")
    @Schema(description="smtp主题账号")
    private String sysProjectSmtpFrom;

    /**
     * 默认功能
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "默认功能")
    @Schema(description = "默认功能")
    private String sysProjectFunctionDefaultLabel;
    /**
     * 默认功能
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "默认功能")
    @Schema(description = "默认功能")
    private String sysProjectFunctionDefaultIds;

    /**
     * 是否默认
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "是否默认")
    @Schema(description = "是否默认")
    private Integer sysProjectFunctionDefault;
}