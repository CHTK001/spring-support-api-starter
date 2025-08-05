package com.chua.starter.monitor.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件上传状态DTO
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "FileUploadStatusDTO", description = "文件上传状态DTO")
@Schema(description = "文件上传状态DTO")
public class FileUploadStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    @ApiModelProperty(value = "文件ID")
    @Schema(description = "文件ID")
    private Integer fileId;

    /**
     * 文件名
     */
    @ApiModelProperty(value = "文件名")
    @Schema(description = "文件名")
    private String fileName;

    /**
     * 文件状态
     */
    @ApiModelProperty(value = "文件状态")
    @Schema(description = "文件状态")
    private Integer fileStatus;

    /**
     * 分片总数
     */
    @ApiModelProperty(value = "分片总数")
    @Schema(description = "分片总数")
    private Integer chunkTotal;

    /**
     * 已上传分片数
     */
    @ApiModelProperty(value = "已上传分片数")
    @Schema(description = "已上传分片数")
    private Integer chunkUploaded;

    /**
     * 上传进度百分比
     */
    @ApiModelProperty(value = "上传进度百分比")
    @Schema(description = "上传进度百分比")
    private Double uploadProgress;

    /**
     * 上传开始时间
     */
    @ApiModelProperty(value = "上传开始时间")
    @Schema(description = "上传开始时间")
    private LocalDateTime uploadStartTime;

    /**
     * 上传完成时间
     */
    @ApiModelProperty(value = "上传完成时间")
    @Schema(description = "上传完成时间")
    private LocalDateTime uploadCompleteTime;

    /**
     * 缺失的分片列表
     */
    @ApiModelProperty(value = "缺失的分片列表")
    @Schema(description = "缺失的分片列表")
    private List<Integer> missingChunks;

    /**
     * 失败的分片列表
     */
    @ApiModelProperty(value = "失败的分片列表")
    @Schema(description = "失败的分片列表")
    private List<Integer> failedChunks;

    /**
     * 是否可以合并
     */
    @ApiModelProperty(value = "是否可以合并")
    @Schema(description = "是否可以合并")
    private Boolean canMerge;

    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    @Schema(description = "错误信息")
    private String errorMessage;
}
