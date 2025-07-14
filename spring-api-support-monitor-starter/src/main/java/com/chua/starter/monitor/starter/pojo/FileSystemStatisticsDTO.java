package com.chua.starter.monitor.starter.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文件系统统计信息DTO
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "FileSystemStatisticsDTO", description = "文件系统统计信息DTO")
@Schema(description = "文件系统统计信息DTO")
public class FileSystemStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总文件数
     */
    @ApiModelProperty(value = "总文件数")
    @Schema(description = "总文件数")
    private Long totalFiles;

    /**
     * 总文件大小(字节)
     */
    @ApiModelProperty(value = "总文件大小(字节)")
    @Schema(description = "总文件大小(字节)")
    private Long totalSize;

    /**
     * 待合并文件数
     */
    @ApiModelProperty(value = "待合并文件数")
    @Schema(description = "待合并文件数")
    private Long pendingFiles;

    /**
     * 合并中文件数
     */
    @ApiModelProperty(value = "合并中文件数")
    @Schema(description = "合并中文件数")
    private Long mergingFiles;

    /**
     * 已完成文件数
     */
    @ApiModelProperty(value = "已完成文件数")
    @Schema(description = "已完成文件数")
    private Long completedFiles;

    /**
     * 合并失败文件数
     */
    @ApiModelProperty(value = "合并失败文件数")
    @Schema(description = "合并失败文件数")
    private Long failedFiles;

    /**
     * 启用HTTP访问的文件数
     */
    @ApiModelProperty(value = "启用HTTP访问的文件数")
    @Schema(description = "启用HTTP访问的文件数")
    private Long httpAccessEnabledFiles;

    /**
     * 今日上传文件数
     */
    @ApiModelProperty(value = "今日上传文件数")
    @Schema(description = "今日上传文件数")
    private Long todayUploadFiles;

    /**
     * 今日上传文件大小(字节)
     */
    @ApiModelProperty(value = "今日上传文件大小(字节)")
    @Schema(description = "今日上传文件大小(字节)")
    private Long todayUploadSize;

    /**
     * 存储使用率(百分比)
     */
    @ApiModelProperty(value = "存储使用率(百分比)")
    @Schema(description = "存储使用率(百分比)")
    private Double storageUsageRate;

    /**
     * 平均文件大小(字节)
     */
    @ApiModelProperty(value = "平均文件大小(字节)")
    @Schema(description = "平均文件大小(字节)")
    private Long averageFileSize;
}
