package com.chua.starter.geo.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
* 下载历史记录，持久化到 JSON 文件。
*
* @author CH
* @since 1.0.0
 */
@Data
public class DownloadHistoryRecord {
    /** 记录唯一 标识 */
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
    /** 输出文件路径 */
    private String filePath;
    /** 文件大小（字节） */
    private long fileSize;
    /** 总瓦片数 */
    private int tileCount;
    /** 失败瓦片数 */
    private int failedCount;
    /** 任务状态：完成 / 失败 */
    private String status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 耗时（秒） */
    private Long durationSecs;
    /** 日志文件路径 */
    private String logFile;
}
