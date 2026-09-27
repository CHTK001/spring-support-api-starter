package com.chua.starter.geo.model;

import java.time.LocalDateTime;

/**
 * 下载任务信息，描述一个瓦片下载任务的当前状态。
 *
 * @param id 任务唯一标识
 * @param name 任务名称
 * @param source 图源 标识
 * @param sourceName 图源显示名称
 * @param zoom 缩放级别
 * @param format 输出格式
 * @param savePath 保存路径
 * @param status 当前任务状态
 * @param progress 进度百分比（0-100）
 * @param completed 已完成的瓦片数
 * @param total 总瓦片数
 * @param failedCount 失败的瓦片数
 * @param successCount 成功的瓦片数（= 完成 - 失败数量）
 * @param fileSize 输出文件大小（字节）
 * @param message 状态消息
 * @param error 错误信息（失败时）
 * @param createdAt 任务创建时间
 *
 * @author CH
 * @since 1.0.0
 */
public record TaskInfo(
        String id,
        String name,
        String source,
        String sourceName,
        int zoom,
        String format,
        String savePath,
        TaskStatus status,
        double progress,
        int completed,
        int total,
        int failedCount,
        int successCount,
        long fileSize,
        String message,
        String error,
        LocalDateTime createdAt
) {
}
