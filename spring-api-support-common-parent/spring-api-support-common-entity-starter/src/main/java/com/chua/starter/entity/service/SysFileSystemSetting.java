// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
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
 * @since 2024/12/10
 * @author CH
 */
@Schema(description = "文件系统配置表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_file_system_setting")
public class SysFileSystemSetting extends SysBase implements Serializable {

    @Serial
static final long serialVersionUID = 1L; // 串行版本uid

    /**
     * 配置标识
     */
    @TableId(value = "sys_file_system_setting_id", type = IdType.AUTO)
    @Schema(description = "配置ID")
    Integer sysFileSystemSettingId; // sys文件系统settingid

    /**
     * 配置名称
     */
    @TableField(value = "sys_file_system_setting_name")
    @Schema(description = "配置名称")
    @Size(max = 100, message = "配置名称最大长度要小于 100")
String sysFileSystemSettingName; // sys文件系统setting名称

    /**
     * 是否启用分片上传
     */
    @TableField(value = "sys_file_system_setting_chunk_enabled")
    @Schema(description = "是否启用分片上传")
Boolean sysFileSystemSettingChunkEnabled; // sys文件系统settingchunk已启用

    /**
     * 分片大小(MB)
     */
    @TableField(value = "sys_file_system_setting_chunk_size_mb")
    @Schema(description = "分片大小(MB)")
Integer sysFileSystemSettingChunkSizeMb; // sys文件系统settingchunk大小mb

    /**
     * 是否开启自动合并
     */
    @TableField(value = "sys_file_system_setting_auto_merge_enabled")
    @Schema(description = "是否开启自动合并")
Boolean sysFileSystemSettingAutoMergeEnabled; // sys文件系统settingauto合并已启用

    /**
     * 合并任务数量限制
     */
    @TableField(value = "sys_file_system_setting_merge_task_limit")
    @Schema(description = "合并任务数量限制")
Integer sysFileSystemSettingMergeTaskLimit; // sys文件系统setting合并任务限制

    /**
     * 是否支持手动合并功能
     */
    @TableField(value = "sys_file_system_setting_manual_merge_enabled")
    @Schema(description = "是否支持手动合并功能（不影响自动合并）")
Boolean sysFileSystemSettingManualMergeEnabled; // sys文件系统settingmanual合并已启用

    /**
     * 是否开启HTTP服务
     */
    @TableField(value = "sys_file_system_setting_http_server_enabled")
    @Schema(description = "是否开启HTTP服务（参考HttpProtocolServer）")
Boolean sysFileSystemSettingHttpServerEnabled; // sys文件系统settinghttp服务端已启用

    /**
     * HTTP服务端口
     */
    @TableField(value = "sys_file_system_setting_http_server_port")
    @Schema(description = "HTTP服务端口")
Integer sysFileSystemSettingHttpServerPort; // sys文件系统settinghttp服务端端口

    /**
     * HTTP服务主机
     */
    @TableField(value = "sys_file_system_setting_http_server_host")
    @Schema(description = "HTTP服务主机")
    @Size(max = 100, message = "HTTP服务主机最大长度要小于 100")
String sysFileSystemSettingHttpServerHost; // sys文件系统settinghttp服务端主机

    /**
     * HTTP服务上下文路径
     */
    @TableField(value = "sys_file_system_setting_http_server_context")
    @Schema(description = "HTTP服务上下文路径")
    @Size(max = 100, message = "HTTP服务上下文路径最大长度要小于 100")
String sysFileSystemSettingHttpServerContext; // sys文件系统settinghttp服务端上下文

    /**
     * 是否启用文件下载
     */
    @TableField(value = "sys_file_system_setting_download_enabled")
    @Schema(description = "是否启用文件下载")
Boolean sysFileSystemSettingDownloadEnabled; // sys文件系统settingdownload已启用

    /**
     * 是否启用文件预览
     */
    @TableField(value = "sys_file_system_setting_preview_enabled")
    @Schema(description = "是否启用文件预览（参考FileStorageServletFilter）")
Boolean sysFileSystemSettingPreviewEnabled; // sys文件系统settingpreview已启用

    /**
     * 是否启用 Webjar 支持
     */
    @TableField(value = "sys_file_system_setting_webjar_enabled")
    @Schema(description = "是否启用Webjar支持")
Boolean sysFileSystemSettingWebjarEnabled; // sys文件系统settingwebjar已启用

    /**
     * 是否启用远程文件支持
     */
    @TableField(value = "sys_file_system_setting_remote_file_enabled")
    @Schema(description = "是否启用远程文件支持")
Boolean sysFileSystemSettingRemoteFileEnabled; // sys文件系统setting远程文件已启用

    /**
     * 是否启用视图预览
     */
    @TableField(value = "sys_file_system_setting_view_enabled")
    @Schema(description = "是否启用视图预览（Office文档转换等，参考ViewServletFilter）")
Boolean sysFileSystemSettingViewEnabled; // sys文件系统settingview已启用

    /**
     * HTTP访问域名
     */
    @TableField(value = "sys_file_system_setting_http_access_domain")
    @Schema(description = "HTTP访问域名")
    @Size(max = 255, message = "HTTP访问域名最大长度要小于 255")
String sysFileSystemSettingHttpAccessDomain; // sys文件系统settinghttpaccessdomain

    /**
     * 文件类型白名单(逗号分隔)
     */
    @TableField(value = "sys_file_system_setting_allowed_types")
    @Schema(description = "文件类型白名单(逗号分隔)")
    @Size(max = 1000, message = "文件类型白名单最大长度要小于 1000")
String sysFileSystemSettingAllowedTypes; // sys文件系统settingallowed类型

    /**
     * 单个文件最大大小(MB)
     */
    @TableField(value = "sys_file_system_setting_max_file_size_mb")
    @Schema(description = "单个文件最大大小(MB)")
Integer sysFileSystemSettingMaxFileSizeMb; // sys文件系统setting最大文件大小mb

    /**
     * 文件存储根路径
     */
    @TableField(value = "sys_file_system_setting_storage_root_path")
    @Schema(description = "文件存储根路径")
    @Size(max = 500, message = "文件存储根路径最大长度要小于 500")
String sysFileSystemSettingStorageRootPath; // sys文件系统settingstorage根路径

    /**
     * 临时文件存储路径
     */
    @TableField(value = "sys_file_system_setting_temp_path")
    @Schema(description = "临时文件存储路径")
    @Size(max = 500, message = "临时文件存储路径最大长度要小于 500")
String sysFileSystemSettingTempPath; // sys文件系统settingtemp路径

    /**
     * 文件保留天数(0表示永久保留)
     */
    @TableField(value = "sys_file_system_setting_retention_days")
    @Schema(description = "文件保留天数(0表示永久保留)")
Integer sysFileSystemSettingRetentionDays; // sys文件系统settingretentiondays

    /**
     * 是否启用
     */
    @TableField(value = "sys_file_system_setting_enabled")
    @Schema(description = "是否启用")
Boolean sysFileSystemSettingEnabled; // sys文件系统setting已启用

    /**
     * 默认存储类型
     */
    @TableField(value = "sys_file_system_setting_default_storage_type")
    @Schema(description = "默认存储类型：local, oss, cos, s3等")
    @Size(max = 50, message = "默认存储类型最大长度要小于 50")
String sysFileSystemSettingDefaultStorageType; // sys文件系统setting默认storage类型

    /**
     * 备注
     */
    @TableField(value = "sys_file_system_setting_remark")
    @Schema(description = "备注")
    @Size(max = 500, message = "备注最大长度要小于 500")
String sysFileSystemSettingRemark; // sys文件系统setting评论

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