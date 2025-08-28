package com.chua.starter.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据管理-客户端配置
 *
 * 记录各类数据源的基础连接信息与控制台能力配置。
 *
 * @author CH
 * @since 2025-04-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("数据管理-客户端配置")
@Schema(name = "SystemDataSetting")
@TableName("system_data_setting")
public class SystemDataSetting extends SysBase {

    @TableId(value = "system_data_setting_id", type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    @Schema(description = "主键ID")
    @NotNull(message = "ID不能为空", groups = { })
    private Integer systemDataSettingId;

    /** 名称 */
    @Schema(description = "名称")
    @TableField("system_data_setting_name")
    @Size(max = 255)
    private String systemDataSettingName;

    /** 类型：JDBC/REDIS/ZK... */
    @Schema(description = "类型：JDBC/REDIS/ZK 等")
    @TableField("system_data_setting_type")
    @Size(max = 64)
    private String systemDataSettingType;

    /** 直连URL（优先） */
    @Schema(description = "直连URL（优先）")
    @TableField("system_data_setting_server")
    private String systemDataSettingServer;

    /** 主机名 */
    @Schema(description = "主机名")
    @TableField("system_data_setting_host")
    private String systemDataSettingHost;

    /** 端口 */
    @Schema(description = "端口")
    @TableField("system_data_setting_port")
    private Integer systemDataSettingPort;

    /** 用户名 */
    @Schema(description = "用户名")
    @TableField("system_data_setting_username")
    private String systemDataSettingUsername;

    /** 密码 */
    @Schema(description = "密码")
    @TableField("system_data_setting_password")
    private String systemDataSettingPassword;

    /**
     * 数据库名称
     */
    @Schema(description = "数据库名称")
    @TableField("system_data_setting_database")
    private String systemDataSettingDatabase;
    /**
     * 策略
     */
    @Schema(description = "策略")
    @TableField("system_data_setting_policy")
    private String systemDataSettingPolicy;

    /** 认证方式 */
    @Schema(description = "认证方式")
    @TableField("system_data_setting_auth_type")
    private String systemDataSettingAuthType;

    /** 图标 */
    @Schema(description = "图标URL")
    @TableField("system_data_setting_icon")
    private String systemDataSettingIcon;

    /** 控制台展示类型：TABLE/GRAPH/FILE */
    @Schema(description = "控制台展示类型：TABLE/GRAPH/FILE")
    @TableField("system_data_setting_console_type")
    private String systemDataSettingConsoleType;

    /** 图表类型：自定义前端展示用途 */
    @Schema(description = "图表类型")
    @TableField("system_data_setting_chart_type")
    private String systemDataSettingChartType;

    /** 超时毫秒 */
    @Schema(description = "超时毫秒")
    @TableField("system_data_setting_timeout_ms")
    private Integer systemDataSettingTimeoutMs;

    /** 模式：REMOTE/FILE */
    @Schema(description = "模式：REMOTE/FILE")
    @TableField("system_data_setting_mode")
    private String systemDataSettingMode;

    /** 是否启用 */
    @Schema(description = "是否启用")
    @TableField("system_data_setting_enabled")
    private Boolean systemDataSettingEnabled;

    /** 控制台权限/个性化配置(JSON) */
    @Schema(description = "控制台权限/个性化配置(JSON)")
    @TableField("system_data_setting_config")
    private String systemDataSettingConfig;

    /** 协议 */
    @Schema(description = "协议")
    @TableField("system_data_setting_protocol")
    private String systemDataSettingProtocol;

    /** JDBC 驱动类名（数据库类型可选） */
    @Schema(description = "JDBC 驱动类名")
    @TableField("system_data_setting_driver_class")
    private String systemDataSettingDriverClass;

    /** JDBC 驱动文件路径（数据库类型可选） */
    @Schema(description = "JDBC 驱动文件路径")
    @TableField("system_data_setting_driver_path")
    private String systemDataSettingDriverPath;

    /** 远程图片地址（卡片背景或展示） */
    @Schema(description = "远程图片地址")
    @TableField("system_data_setting_image_url")
    private String systemDataSettingImageUrl;
    /** 备份状态 */
    @Schema(description = "备份状态;0:未启动; 1:已启动")
    @TableField("system_data_setting_backup_status")
    private Integer systemDataSettingBackupStatus;
}


