package com.chua.starter.service.pojo.sso;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分片上传任务信息响应（用于断点续传）
 *
 * @author CH
 * @since 2024/12/20
 */
@Data
@Schema(description = "分片上传任务信息响应")
public class SysFileTaskInfoResponse {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    private String taskId;

    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    private String fileName;

    /**
     * 文件MD5
     */
    @Schema(description = "文件MD5")
    private String fileMd5;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    /**
     * 分片大小（字节）
     */
    @Schema(description = "分片大小（字节）")
    private Long chunkSize;

    /**
     * 总分片数
     */
    @Schema(description = "总分片数")
    private Integer totalChunks;

    /**
     * 已上传分片数
     */
    @Schema(description = "已上传分片数")
    private Integer uploadedChunks;

    /**
     * 已上传的分片序号列表（从1开始）
     */
    @Schema(description = "已上传的分片序号列表（从1开始）")
    private List<Integer> uploadedParts;

    /**
     * 任务状态
     * 0-待上传 1-上传中 2-待合并 3-合并中 4-已完成 5-失败
     */
    @Schema(description = "任务状态：0-待上传 1-上传中 2-待合并 3-合并中 4-已完成 5-失败")
    private Integer status;

    /**
     * 文件URL（已完成时返回）
     */
    @Schema(description = "文件URL（已完成时返回）")
    private String url;

    /**
     * 是否任务已存在
     */
    @Schema(description = "是否任务已存在")
    private Boolean exists;
}
