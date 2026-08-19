package com.chua.starter.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.base.validator.group.AddGroup;
import com.chua.common.support.base.validator.group.UpdateGroup;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目管理
 *
 * @since 2026/08/03
 * @author CH
 */
@Schema(description="项目管理")
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value = "sys_project")
public class SysProject extends SysBase {
    /**
     * 项目主键ID
     */
    @TableId(value = "sys_project_id", type = IdType.AUTO)
    @Schema(description = "")
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    /** SYSprojectID */
    private Integer sysProjectId;

    /**
     * 项目名称
     */
    @TableField(value = "sys_project_name")
    @Schema(description = "项目名称")
    @NotBlank(message = "名称不能为空", groups = {AddGroup.class})
    /** SYSproject名称 */
    private String sysProjectName;

    /**
     * 图标
     */
    @TableField(value = "sys_project_icon")
    @Schema(description="图标")
    /** SYSprojecticon */
    private String sysProjectIcon;

    /**
     * 分组
     */
    @TableField(value = "sys_project_group")
    @Schema(description="分组")
    /** SYSproject分组 */
    private String sysProjectGroup;

    /**
     * 编码, 可自行作为唯一标识
     */
    @TableField(value = "sys_project_code")
    @Schema(description="编码, 可自行作为唯一标识")
    /** SYSproject代码 */
    private String sysProjectCode;

    /**
     * appid
     */
    @TableField(value = "sys_project_app_id")
    @Schema(description="appid")
    /** SYSprojectAPPID */
    private String sysProjectAppId;

    /**
     * appsecret
     */
    @TableField(value = "sys_project_app_secret")
    @Schema(description="appsecret")
    /** SYSprojectAPPsecret */
    private String sysProjectAppSecret;

    /**
     * 签名
     */
    @TableField(value = "sys_project_sign")
    @Schema(description = "签名/项目标识")
    /** SYSprojectsign */
    private String sysProjectSign;
    /**
     * 密钥
     */
    @TableField(value = "sys_project_app_key")
    @Schema(description = "密钥")
    /** SYSprojectAPP密钥 */
    private String sysProjectAppKey;

    /**
     * 签名密钥
     */
    @TableField(value = "sys_project_sign_code")
    @Schema(description = "签名编码/项目标识编码")
    /** SYSprojectsign代码 */
    private String sysProjectSignCode;

    /**
     * 是否删除0:未删除
     */
    @TableField(value = "sys_project_delete")
    @Schema(description="是否删除0:未删除")
    /** SYSprojectdelete */
    private Integer sysProjectDelete;

    /**
     * 是否启用0:未启用
     */
    @TableField(value = "sys_project_status")
    @Schema(description="是否启用0:未启用")
    /** SYSproject状态 */
    private Integer sysProjectStatus;

    /**
     * cdn
     */
    @TableField(value = "sys_project_cdn")
    @Schema(description="cdn")
    /** SYSprojectCDN */
    private String sysProjectCdn;

    /**
     * 端点
     */
    @TableField(value = "sys_project_endpoint")
    @Schema(description="端点")
    /** SYSprojectendpoint */
    private String sysProjectEndpoint;
    /**
     * 本地路径
     */
    @TableField(value = "sys_project_local_path")
    @Schema(description = "本地路径")
    /** SYSproject本地路径 */
    private String sysProjectLocalPath;

    /**
     * api地址
     */
    @TableField(value = "sys_project_api_address")
    @Schema(description="api地址")
    /** SYSprojectAPI地址 */
    private String sysProjectApiAddress;
    /**
     * 临时文件路径
     */
    @TableField(value = "sys_project_location_temp_path")
    @Schema(description = "临时文件路径")
    /** SYSprojectlocationtemp路径 */
    private String sysProjectLocationTempPath;
    /**
     * oss地址
     */
    @TableField(value = "sys_project_location_oss_address")
    @Schema(description = "oss地址")
    /** SYSprojectlocationOSS地址 */
    private String sysProjectLocationOssAddress;

    /**
     * 厂家编码
     */
    @TableField(value = "sys_project_vender")
    @Schema(description="厂家编码(前端固定值)")
    /** SYSprojectvender */
    private String sysProjectVender;
    /**
     * 排序
     */
    @TableField(value = "sys_project_sort")
    @Schema(description = "排序")
    /** SYSprojectsort */
    private Integer sysProjectSort;

    /**
     * 描述
     */
    @TableField(value = "sys_project_remark")
    @Schema(description="描述")
    /** SYSprojectremark */
    private String sysProjectRemark;

    /**
     * 功能; 前端固定值
     */
    @TableField(value = "sys_project_function")
    @Schema(description="功能; 前端固定值")
    /** SYSprojectfunction */
    private String sysProjectFunction;

    /**
     * smtp主机
     */
    @TableField(value = "sys_project_smtp_host")
    @Schema(description="smtp主机")
    /** SYSprojectSMTP主机 */
    private String sysProjectSmtpHost;

    /**
     * smtp端口
     */
    @TableField(value = "sys_project_smtp_port")
    @Schema(description="smtp端口")
    /** SYSprojectSMTP端口 */
    private Integer sysProjectSmtpPort;

    /**
     * smtp密码
     */
    @TableField(value = "sys_project_smtp_password")
    @Schema(description="smtp密码")
    /** SYSprojectSMTP密码 */
    private String sysProjectSmtpPassword;

    /**
     * smtp主题账号
     */
    @TableField(value = "sys_project_smtp_from")
    @Schema(description="smtp主题账号")
    /** SYSprojectSMTPfrom */
    private String sysProjectSmtpFrom;

    /**
     * 默认功能
     */
    @TableField(exist = false)
    @Schema(description = "默认功能")
    /** SYSprojectfunction默认标签 */
    private String sysProjectFunctionDefaultLabel;
    /**
     * 默认功能
     */
    @TableField(exist = false)
    @Schema(description = "默认功能")
    /** SYSprojectfunction默认IDS */
    private String sysProjectFunctionDefaultIds;

    /**
     * 是否默认
     */
    @TableField(exist = false)
    @Schema(description = "是否默认")
    /** SYSprojectfunction默认 */
    private Integer sysProjectFunctionDefault;
}
