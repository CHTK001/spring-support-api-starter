package com.chua.starter.service.pojo.sso;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 分片上传任务信息响应（用于断点续传）
 *
 * @param taskId 任务标识
 * @param fileName 文件名称
 * @param fileMd5 文件MD5
 * @param fileSize 文件大小（字节）
 * @param chunkSize 分片大小（字节）
 * @param totalChunks 总分片数
 * @param uploadedChunks 已上传分片数
 * @param uploadedParts 已上传的分片序号列表（从1开始）
 * @param status 任务状态 0-待上传 1-上传中 2-待合并 3-合并中 4-已完成 5-失败
 * @param url 文件URL（已完成时返回）
 * @param exists 是否任务已存在
 *
 * @since 2024/12/20
 * @author CH
 */
@Schema(description = "分片上传任务信息响应")
public record SysFileTaskInfoResponse(
        @Schema(description = "任务ID") String taskId,
        @Schema(description = "文件名称") String fileName,
        @Schema(description = "文件MD5") String fileMd5,
        @Schema(description = "文件大小（字节）") Long fileSize,
        @Schema(description = "分片大小（字节）") Long chunkSize,
        @Schema(description = "总分片数") Integer totalChunks,
        @Schema(description = "已上传分片数") Integer uploadedChunks,
        @Schema(description = "已上传的分片序号列表（从1开始）") List<Integer> uploadedParts,
        @Schema(description = "任务状态：0-待上传 1-上传中 2-待合并 3-合并中 4-已完成 5-失败") Integer status,
        @Schema(description = "文件URL（已完成时返回）") String url,
        @Schema(description = "是否任务已存在") Boolean exists
) {
}
