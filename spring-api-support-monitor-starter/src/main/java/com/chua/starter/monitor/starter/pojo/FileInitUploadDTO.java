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
 * 文件初始化上传DTO
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "FileInitUploadDTO", description = "文件初始化上传DTO")
@Schema(description = "文件初始化上传DTO")
public class FileInitUploadDTO implements Serializable {

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
     * 文件大小
     */
    @ApiModelProperty(value = "文件大小")
    @Schema(description = "文件大小")
    private Long fileSize;

    /**
     * 文件MD5
     */
    @ApiModelProperty(value = "文件MD5")
    @Schema(description = "文件MD5")
    private String fileMd5;

    /**
     * 分片总数
     */
    @ApiModelProperty(value = "分片总数")
    @Schema(description = "分片总数")
    private Integer chunkTotal;

    /**
     * 分片大小
     */
    @ApiModelProperty(value = "分片大小")
    @Schema(description = "分片大小")
    private Long chunkSize;

    /**
     * 是否已存在
     */
    @ApiModelProperty(value = "是否已存在")
    @Schema(description = "是否已存在")
    private Boolean exists;

    /**
     * 消息
     */
    @ApiModelProperty(value = "消息")
    @Schema(description = "消息")
    private String message;
}
