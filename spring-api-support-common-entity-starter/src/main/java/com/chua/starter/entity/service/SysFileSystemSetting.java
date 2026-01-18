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

/**
 * 文件系统配置表
 * 用于管理文件系统的全局配置，包括分片上传、自动合并、HTTP服务等功能
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@Schema(description = "文件系统配置表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_file_system_setting")
public class SysFileSystemSetting extends SysBase implements Serializable {

    @Serial
static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(value = "sys_file_system_setting_id", type = IdType.AUTO)
    @Schema(description = "配置ID")
    Integer sysFileSystemSettingId;

    /**
     * 配置名称
     */
    @TableField(value = "sys_file_system_setting_name")
    @Schema(description = "配置名称")
    @Size(max = 100, message = "配置名称最大长度要小于 100")
String sysFileSystemSettingName;

    /**
     * 是否启用分片上传
     */
    @TableField(value = "sys_file_system_setting_chunk_enabled")
    @Schema(description = "是否启用分片上传")
Boolean sysFileSystemSettingChunkEnabled;

    /**
     * 分片大小(MB)
     */
    @TableField(value = "sys_file_system_setting_chunk_size_mb")
    @Schema(description = "分片大小(MB)")
Integer sysFileSystemSettingChunkSizeMb;

    /**
     * 是否开启自动合并
     */
    @TableField(value = "sys_file_system_setting_auto_merge_enabled")
    @Schema(description = "是否开启自动合并")
Boolean sysFileSystemSettingAutoMergeEnabled;

    /**
     * 合并任务数量限制
     */
    @TableField(value = "sys_file_system_setting_merge_task_limit")
    @Schema(description = "合并任务数量限制")
Integer sysFileSystemSettingMergeTaskLimit;

    /**
     * 是否支持手动合并功能
     */
    @TableField(value = "sys_file_system_setting_manual_merge_enabled")
    @Schema(description = "是否支持手动合并功能（不影响自动合并）")
Boolean sysFileSystemSettingManualMergeEnabled;

    /**
     * 是否开启HTTP服务
     */
    @TableField(value = "sys_file_system_setting_http_server_enabled")
    @Schema(description = "是否开启HTTP服务（参考HttpProtocolServer）")
Boolean sysFileSystemSettingHttpServerEnabled;

    /**
     * HTTP服务端口
     */
    @TableField(value = "sys_file_system_setting_http_server_port")
    @Schema(description = "HTTP服务端口")
Integer sysFileSystemSettingHttpServerPort;

    /**
     * HTTP服务主机
     */
    @TableField(value = "sys_file_system_setting_http_server_host")
    @Schema(description = "HTTP服务主机")
    @Size(max = 100, message = "HTTP服务主机最大长度要小于 100")
String sysFileSystemSettingHttpServerHost;

    /**
     * HTTP服务上下文路径
     */
    @TableField(value = "sys_file_system_setting_http_server_context")
    @Schema(description = "HTTP服务上下文路径")
    @Size(max = 100, message = "HTTP服务上下文路径最大长度要小于 100")
String sysFileSystemSettingHttpServerContext;

    /**
     * 是否启用文件下载
     */
    @TableField(value = "sys_file_system_setting_download_enabled")
    @Schema(description = "是否启用文件下载")
Boolean sysFileSystemSettingDownloadEnabled;

    /**
     * 是否启用文件预览
     */
    @TableField(value = "sys_file_system_setting_preview_enabled")
    @Schema(description = "是否启用文件预览（参考FileStorageServletFilter）")
Boolean sysFileSystemSettingPreviewEnabled;

    /**
     * 是否启用 Webjar 支持
     */
    @TableField(value = "sys_file_system_setting_webjar_enabled")
    @Schema(description = "是否启用Webjar支持")
Boolean sysFileSystemSettingWebjarEnabled;

    /**
     * 是否启用远程文件支持
     */
    @TableField(value = "sys_file_system_setting_remote_file_enabled")
    @Schema(description = "是否启用远程文件支持")
Boolean sysFileSystemSettingRemoteFileEnabled;

    /**
     * 是否启用视图预览
     */
    @TableField(value = "sys_file_system_setting_view_enabled")
    @Schema(description = "是否启用视图预览（Office文档转换等，参考ViewServletFilter）")
Boolean sysFileSystemSettingViewEnabled;

    /**
     * HTTP访问域名
     */
    @TableField(value = "sys_file_system_setting_http_access_domain")
    @Schema(description = "HTTP访问域名")
    @Size(max = 255, message = "HTTP访问域名最大长度要小于 255")
String sysFileSystemSettingHttpAccessDomain;

    /**
     * 文件类型白名单(逗号分隔)
     */
    @TableField(value = "sys_file_system_setting_allowed_types")
    @Schema(description = "文件类型白名单(逗号分隔)")
    @Size(max = 1000, message = "文件类型白名单最大长度要小于 1000")
String sysFileSystemSettingAllowedTypes;

    /**
     * 单个文件最大大小(MB)
     */
    @TableField(value = "sys_file_system_setting_max_file_size_mb")
    @Schema(description = "单个文件最大大小(MB)")
Integer sysFileSystemSettingMaxFileSizeMb;

    /**
     * 文件存储根路径
     */
    @TableField(value = "sys_file_system_setting_storage_root_path")
    @Schema(description = "文件存储根路径")
    @Size(max = 500, message = "文件存储根路径最大长度要小于 500")
String sysFileSystemSettingStorageRootPath;

    /**
     * 临时文件存储路径
     */
    @TableField(value = "sys_file_system_setting_temp_path")
    @Schema(description = "临时文件存储路径")
    @Size(max = 500, message = "临时文件存储路径最大长度要小于 500")
String sysFileSystemSettingTempPath;

    /**
     * 文件保留天数(0表示永久保留)
     */
    @TableField(value = "sys_file_system_setting_retention_days")
    @Schema(description = "文件保留天数(0表示永久保留)")
Integer sysFileSystemSettingRetentionDays;

    /**
     * 是否启用
     */
    @TableField(value = "sys_file_system_setting_enabled")
    @Schema(description = "是否启用")
Boolean sysFileSystemSettingEnabled;

    /**
     * 默认存储类型
     */
    @TableField(value = "sys_file_system_setting_default_storage_type")
    @Schema(description = "默认存储类型：local, oss, cos, s3等")
    @Size(max = 50, message = "默认存储类型最大长度要小于 50")
String sysFileSystemSettingDefaultStorageType;

    /**
     * 备注
     */
    @TableField(value = "sys_file_system_setting_remark")
    @Schema(description = "备注")
    @Size(max = 500, message = "备注最大长度要小于 500")
String sysFileSystemSettingRemark;

    /**
     * 获取默认配置
     *
     * @return 默认配置实例
     */
    public static SysFileSystemSetting getDefaultSetting() {
        var setting = new SysFileSystemSetting();
        setting.sysFileSystemSettingName = "默认配置";
        setting.sysFileSystemSettingChunkEnabled = true;
        setting.sysFileSystemSettingChunkSizeMb = 100;
        setting.sysFileSystemSettingAutoMergeEnabled = true;
        setting.sysFileSystemSettingMergeTaskLimit = 10;
        setting.sysFileSystemSettingManualMergeEnabled = true;
        setting.sysFileSystemSettingHttpServerEnabled = false;
        setting.sysFileSystemSettingHttpServerPort = 8888;
        setting.sysFileSystemSettingHttpServerHost = "0.0.0.0";
        setting.sysFileSystemSettingHttpServerContext = "/file";
        setting.sysFileSystemSettingDownloadEnabled = true;
        setting.sysFileSystemSettingPreviewEnabled = true;
        setting.sysFileSystemSettingWebjarEnabled = false;
        setting.sysFileSystemSettingRemoteFileEnabled = false;
        setting.sysFileSystemSettingViewEnabled = true;
        setting.sysFileSystemSettingMaxFileSizeMb = 1024;
        setting.sysFileSystemSettingRetentionDays = 0;
        setting.sysFileSystemSettingEnabled = true;
        setting.sysFileSystemSettingDefaultStorageType = "local";
        return setting;
    }
}
