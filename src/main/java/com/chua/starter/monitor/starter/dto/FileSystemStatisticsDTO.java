package com.chua.starter.monitor.starter.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
public class FileSystemStatisticsDTO {

    /**
     * 总文件数
     */
    private Long totalFiles;

    /**
     * 总文件大小（字节）
     */
    private Long totalSize;

    /**
     * 待合并文件数
     */
    private Long pendingFiles;

    /**
     * 合并中文件数
     */
    private Long mergingFiles;

    /**
     * 已完成文件数
     */
    private Long completedFiles;

    /**
     * 合并失败文件数
     */
    private Long failedFiles;
}
