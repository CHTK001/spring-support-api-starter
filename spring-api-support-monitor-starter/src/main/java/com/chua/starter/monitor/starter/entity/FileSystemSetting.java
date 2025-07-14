package com.chua.starter.monitor.starter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.datasource.annotation.ColumnDesc;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 文件系统配置管理
 *
 * @author CH
 * @since 2024/12/30
 */
@ApiModel(description = "文件系统配置管理")
@Schema(description = "文件系统配置管理")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "file_system_setting")
public class FileSystemSetting extends SysBase implements Serializable {

    /**
     * 配置ID
     */
    @TableId(value = "file_system_setting_id", type = IdType.AUTO)
    @ApiModelProperty(value = "配置ID")
    @Schema(description = "配置ID")
    @NotNull(message = "配置ID不能为null")
    private Integer fileSystemSettingId;

    /**
     * 配置名称
     */
    @ColumnDesc("配置名称")
    @TableField(value = "file_system_setting_name")
    @ApiModelProperty(value = "配置名称")
    @Schema(description = "配置名称")
    @Size(max = 100, message = "配置名称最大长度要小于 100")
    private String fileSystemSettingName;

    /**
     * 是否启用分片上传
     */
    @ColumnDesc("是否启用分片上传")
    @TableField(value = "file_system_setting_chunk_upload_enabled")
    @ApiModelProperty(value = "是否启用分片上传")
    @Schema(description = "是否启用分片上传")
    private Boolean fileSystemSettingChunkUploadEnabled;

    /**
     * 分片大小(MB)
     */
    @ColumnDesc("分片大小(MB)")
    @TableField(value = "file_system_setting_chunk_size_mb")
    @ApiModelProperty(value = "分片大小(MB)")
    @Schema(description = "分片大小(MB)")
    private Integer fileSystemSettingChunkSizeMb;

    /**
     * 合并任务数量限制
     */
    @ColumnDesc("合并任务数量限制")
    @TableField(value = "file_system_setting_merge_task_limit")
    @ApiModelProperty(value = "合并任务数量限制")
    @Schema(description = "合并任务数量限制")
    private Integer fileSystemSettingMergeTaskLimit;

    /**
     * 是否开启手动合并
     */
    @ColumnDesc("是否开启手动合并")
    @TableField(value = "file_system_setting_manual_merge_enabled")
    @ApiModelProperty(value = "是否开启手动合并")
    @Schema(description = "是否开启手动合并")
    private Boolean fileSystemSettingManualMergeEnabled;

    /**
     * 是否启用文件下载
     */
    @ColumnDesc("是否启用文件下载")
    @TableField(value = "file_system_setting_download_enabled")
    @ApiModelProperty(value = "是否启用文件下载")
    @Schema(description = "是否启用文件下载")
    private Boolean fileSystemSettingDownloadEnabled;

    /**
     * 是否启用HTTP访问
     */
    @ColumnDesc("是否启用HTTP访问")
    @TableField(value = "file_system_setting_http_access_enabled")
    @ApiModelProperty(value = "是否启用HTTP访问")
    @Schema(description = "是否启用HTTP访问")
    private Boolean fileSystemSettingHttpAccessEnabled;

    /**
     * HTTP访问域名
     */
    @ColumnDesc("HTTP访问域名")
    @TableField(value = "file_system_setting_http_access_domain")
    @ApiModelProperty(value = "HTTP访问域名")
    @Schema(description = "HTTP访问域名")
    @Size(max = 255, message = "HTTP访问域名最大长度要小于 255")
    private String fileSystemSettingHttpAccessDomain;

    /**
     * 文件类型白名单(逗号分隔)
     */
    @ColumnDesc("文件类型白名单(逗号分隔)")
    @TableField(value = "file_system_setting_allowed_file_types")
    @ApiModelProperty(value = "文件类型白名单(逗号分隔)")
    @Schema(description = "文件类型白名单(逗号分隔)")
    @Size(max = 1000, message = "文件类型白名单最大长度要小于 1000")
    private String fileSystemSettingAllowedFileTypes;

    /**
     * 单个文件最大大小(MB)
     */
    @ColumnDesc("单个文件最大大小(MB)")
    @TableField(value = "file_system_setting_max_file_size_mb")
    @ApiModelProperty(value = "单个文件最大大小(MB)")
    @Schema(description = "单个文件最大大小(MB)")
    private Integer fileSystemSettingMaxFileSizeMb;

    /**
     * 文件存储根路径
     */
    @ColumnDesc("文件存储根路径")
    @TableField(value = "file_system_setting_storage_root_path")
    @ApiModelProperty(value = "文件存储根路径")
    @Schema(description = "文件存储根路径")
    @Size(max = 500, message = "文件存储根路径最大长度要小于 500")
    private String fileSystemSettingStorageRootPath;

    /**
     * 临时文件存储路径
     */
    @ColumnDesc("临时文件存储路径")
    @TableField(value = "file_system_setting_temp_storage_path")
    @ApiModelProperty(value = "临时文件存储路径")
    @Schema(description = "临时文件存储路径")
    @Size(max = 500, message = "临时文件存储路径最大长度要小于 500")
    private String fileSystemSettingTempStoragePath;

    /**
     * 文件保留天数(0表示永久保留)
     */
    @ColumnDesc("文件保留天数(0表示永久保留)")
    @TableField(value = "file_system_setting_file_retention_days")
    @ApiModelProperty(value = "文件保留天数(0表示永久保留)")
    @Schema(description = "文件保留天数(0表示永久保留)")
    private Integer fileSystemSettingFileRetentionDays;

    /**
     * 是否启用
     */
    @ColumnDesc("是否启用")
    @TableField(value = "file_system_setting_enabled")
    @ApiModelProperty(value = "是否启用")
    @Schema(description = "是否启用")
    private Boolean fileSystemSettingEnabled;

    /**
     * 备注
     */
    @ColumnDesc("备注")
    @TableField(value = "file_system_setting_remark")
    @ApiModelProperty(value = "备注")
    @Schema(description = "备注")
    @Size(max = 500, message = "备注最大长度要小于 500")
    private String fileSystemSettingRemark;

    private static final long serialVersionUID = 1L;

    /**
     * 获取默认配置
     */
    public static FileSystemSetting getDefaultSetting() {
        FileSystemSetting setting = new FileSystemSetting();
        setting.setFileSystemSettingName("默认配置");
        setting.setFileSystemSettingChunkUploadEnabled(true);
        setting.setFileSystemSettingChunkSizeMb(5);
        setting.setFileSystemSettingMergeTaskLimit(1);
        setting.setFileSystemSettingManualMergeEnabled(true);
        setting.setFileSystemSettingDownloadEnabled(true);
        setting.setFileSystemSettingHttpAccessEnabled(false);
        setting.setFileSystemSettingMaxFileSizeMb(1024);
        setting.setFileSystemSettingFileRetentionDays(0);
        setting.setFileSystemSettingEnabled(true);
        return setting;
    }
}
