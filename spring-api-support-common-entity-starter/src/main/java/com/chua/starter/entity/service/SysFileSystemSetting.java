package com.chua.starter.entity.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.datasource.annotation.ColumnDesc;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件系统配置表
 * 用于管理文件系统的全局配置，包括分片上传、自动合并、HTTP服务等功能
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@ApiModel(description = "文件系统配置表")
@Schema(description = "文件系统配置表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_file_system_setting")
public class SysFileSystemSetting extends SysBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(value = "sys_file_system_setting_id", type = IdType.AUTO)
    @ApiModelProperty(value = "配置ID")
    @Schema(description = "配置ID")
    private Integer sysFileSystemSettingId;

    /**
     * 配置名称
     */
    @ColumnDesc("配置名称")
    @TableField(value = "sys_file_system_setting_name")
    @ApiModelProperty(value = "配置名称")
    @Schema(description = "配置名称")
    @Size(max = 100, message = "配置名称最大长度要小于 100")
    private String sysFileSystemSettingName;

    /**
     * 是否启用分片上传
     */
    @ColumnDesc("是否启用分片上传")
    @TableField(value = "sys_file_system_setting_chunk_enabled")
    @ApiModelProperty(value = "是否启用分片上传")
    @Schema(description = "是否启用分片上传")
    private Boolean sysFileSystemSettingChunkEnabled;

    /**
     * 分片大小(MB)
     */
    @ColumnDesc("分片大小(MB)")
    @TableField(value = "sys_file_system_setting_chunk_size_mb")
    @ApiModelProperty(value = "分片大小(MB)")
    @Schema(description = "分片大小(MB)")
    private Integer sysFileSystemSettingChunkSizeMb;

    /**
     * 是否开启自动合并
     */
    @ColumnDesc("是否开启自动合并")
    @TableField(value = "sys_file_system_setting_auto_merge_enabled")
    @ApiModelProperty(value = "是否开启自动合并")
    @Schema(description = "是否开启自动合并")
    private Boolean sysFileSystemSettingAutoMergeEnabled;

    /**
     * 合并任务数量限制
     */
    @ColumnDesc("合并任务数量限制")
    @TableField(value = "sys_file_system_setting_merge_task_limit")
    @ApiModelProperty(value = "合并任务数量限制")
    @Schema(description = "合并任务数量限制")
    private Integer sysFileSystemSettingMergeTaskLimit;

    /**
     * 是否支持手动合并功能
     */
    @ColumnDesc("是否支持手动合并功能")
    @TableField(value = "sys_file_system_setting_manual_merge_enabled")
    @ApiModelProperty(value = "是否支持手动合并功能")
    @Schema(description = "是否支持手动合并功能（不影响自动合并）")
    private Boolean sysFileSystemSettingManualMergeEnabled;

    /**
     * 是否开启HTTP服务
     */
    @ColumnDesc("是否开启HTTP服务")
    @TableField(value = "sys_file_system_setting_http_server_enabled")
    @ApiModelProperty(value = "是否开启HTTP服务")
    @Schema(description = "是否开启HTTP服务（参考HttpProtocolServer）")
    private Boolean sysFileSystemSettingHttpServerEnabled;

    /**
     * HTTP服务端口
     */
    @ColumnDesc("HTTP服务端口")
    @TableField(value = "sys_file_system_setting_http_server_port")
    @ApiModelProperty(value = "HTTP服务端口")
    @Schema(description = "HTTP服务端口")
    private Integer sysFileSystemSettingHttpServerPort;

    /**
     * HTTP服务主机
     */
    @ColumnDesc("HTTP服务主机")
    @TableField(value = "sys_file_system_setting_http_server_host")
    @ApiModelProperty(value = "HTTP服务主机")
    @Schema(description = "HTTP服务主机")
    @Size(max = 100, message = "HTTP服务主机最大长度要小于 100")
    private String sysFileSystemSettingHttpServerHost;

    /**
     * HTTP服务上下文路径
     */
    @ColumnDesc("HTTP服务上下文路径")
    @TableField(value = "sys_file_system_setting_http_server_context")
    @ApiModelProperty(value = "HTTP服务上下文路径")
    @Schema(description = "HTTP服务上下文路径")
    @Size(max = 100, message = "HTTP服务上下文路径最大长度要小于 100")
    private String sysFileSystemSettingHttpServerContext;

    /**
     * 是否启用文件下载
     */
    @ColumnDesc("是否启用文件下载")
    @TableField(value = "sys_file_system_setting_download_enabled")
    @ApiModelProperty(value = "是否启用文件下载")
    @Schema(description = "是否启用文件下载")
    private Boolean sysFileSystemSettingDownloadEnabled;

    /**
     * 是否启用文件预览
     */
    @ColumnDesc("是否启用文件预览")
    @TableField(value = "sys_file_system_setting_preview_enabled")
    @ApiModelProperty(value = "是否启用文件预览")
    @Schema(description = "是否启用文件预览（参考FileStorageServletFilter）")
    private Boolean sysFileSystemSettingPreviewEnabled;

    /**
     * HTTP访问域名
     */
    @ColumnDesc("HTTP访问域名")
    @TableField(value = "sys_file_system_setting_http_access_domain")
    @ApiModelProperty(value = "HTTP访问域名")
    @Schema(description = "HTTP访问域名")
    @Size(max = 255, message = "HTTP访问域名最大长度要小于 255")
    private String sysFileSystemSettingHttpAccessDomain;

    /**
     * 文件类型白名单(逗号分隔)
     */
    @ColumnDesc("文件类型白名单")
    @TableField(value = "sys_file_system_setting_allowed_types")
    @ApiModelProperty(value = "文件类型白名单")
    @Schema(description = "文件类型白名单(逗号分隔)")
    @Size(max = 1000, message = "文件类型白名单最大长度要小于 1000")
    private String sysFileSystemSettingAllowedTypes;

    /**
     * 单个文件最大大小(MB)
     */
    @ColumnDesc("单个文件最大大小(MB)")
    @TableField(value = "sys_file_system_setting_max_file_size_mb")
    @ApiModelProperty(value = "单个文件最大大小(MB)")
    @Schema(description = "单个文件最大大小(MB)")
    private Integer sysFileSystemSettingMaxFileSizeMb;

    /**
     * 文件存储根路径
     */
    @ColumnDesc("文件存储根路径")
    @TableField(value = "sys_file_system_setting_storage_root_path")
    @ApiModelProperty(value = "文件存储根路径")
    @Schema(description = "文件存储根路径")
    @Size(max = 500, message = "文件存储根路径最大长度要小于 500")
    private String sysFileSystemSettingStorageRootPath;

    /**
     * 临时文件存储路径
     */
    @ColumnDesc("临时文件存储路径")
    @TableField(value = "sys_file_system_setting_temp_path")
    @ApiModelProperty(value = "临时文件存储路径")
    @Schema(description = "临时文件存储路径")
    @Size(max = 500, message = "临时文件存储路径最大长度要小于 500")
    private String sysFileSystemSettingTempPath;

    /**
     * 文件保留天数(0表示永久保留)
     */
    @ColumnDesc("文件保留天数")
    @TableField(value = "sys_file_system_setting_retention_days")
    @ApiModelProperty(value = "文件保留天数")
    @Schema(description = "文件保留天数(0表示永久保留)")
    private Integer sysFileSystemSettingRetentionDays;

    /**
     * 是否启用
     */
    @ColumnDesc("是否启用")
    @TableField(value = "sys_file_system_setting_enabled")
    @ApiModelProperty(value = "是否启用")
    @Schema(description = "是否启用")
    private Boolean sysFileSystemSettingEnabled;

    /**
     * 默认存储类型
     */
    @ColumnDesc("默认存储类型")
    @TableField(value = "sys_file_system_setting_default_storage_type")
    @ApiModelProperty(value = "默认存储类型")
    @Schema(description = "默认存储类型：local, oss, cos, s3等")
    @Size(max = 50, message = "默认存储类型最大长度要小于 50")
    private String sysFileSystemSettingDefaultStorageType;

    /**
     * 备注
     */
    @ColumnDesc("备注")
    @TableField(value = "sys_file_system_setting_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    @Size(max = 500, message = "备注最大长度要小于 500")
    private String sysFileSystemSettingRemark;

    /**
     * 获取默认配置
     *
     * @return 默认配置实例
     */
    public static SysFileSystemSetting getDefaultSetting() {
        SysFileSystemSetting setting = new SysFileSystemSetting();
        setting.setSysFileSystemSettingName("默认配置");
        setting.setSysFileSystemSettingChunkEnabled(true);
        setting.setSysFileSystemSettingChunkSizeMb(100);
        setting.setSysFileSystemSettingAutoMergeEnabled(true);
        setting.setSysFileSystemSettingMergeTaskLimit(10);
        setting.setSysFileSystemSettingManualMergeEnabled(true);
        setting.setSysFileSystemSettingHttpServerEnabled(false);
        setting.setSysFileSystemSettingHttpServerPort(8888);
        setting.setSysFileSystemSettingHttpServerHost("0.0.0.0");
        setting.setSysFileSystemSettingHttpServerContext("/file");
        setting.setSysFileSystemSettingDownloadEnabled(true);
        setting.setSysFileSystemSettingPreviewEnabled(true);
        setting.setSysFileSystemSettingMaxFileSizeMb(1024);
        setting.setSysFileSystemSettingRetentionDays(0);
        setting.setSysFileSystemSettingEnabled(true);
        setting.setSysFileSystemSettingDefaultStorageType("local");
        return setting;
    }
}
