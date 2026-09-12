package com.chua.starter.geo.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
* 下载任务信息，描述一个瓦片下载任务的当前状态。
*
* @author CH
* @since 1.0.0
 */
@Data
public class TaskInfo {
    /** 任务唯一标识 */
    private String id;
    /** 任务名称 */
    private String name;
    /** 图源 标识 */
    private String source;
    /** 图源显示名称 */
    private String sourceName;
    /** 缩放级别 */
    private int zoom;
    /** 输出格式 */
    private String format;
    /** 保存路径 */
    private String savePath;
    /** 当前任务状态 */
    private TaskStatus status;
    /** 进度百分比（0-100） */
    private double progress;
    /** 已完成的瓦片数 */
    private int completed;
    /** 总瓦片数 */
    private int total;
    /** 失败的瓦片数 */
    private int failedCount;
    /** 成功的瓦片数（= 完成 - 失败数量） */
    private int successCount;
    /** 输出文件大小（字节） */
    private long fileSize;
    /** 状态消息 */
    private String message;
    /** 错误信息（失败时） */
    private String error;
    /** 任务创建时间 */
    private LocalDateTime createdAt;
}
