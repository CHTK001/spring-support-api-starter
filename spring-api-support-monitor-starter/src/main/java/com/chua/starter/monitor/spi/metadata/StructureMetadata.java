package com.chua.starter.monitor.spi.metadata;

import lombok.Data;

/**
 * 元数据结构操作接口
 * - 用于定义跨 SPI 的通用能力，如表重命名、表备份
 */
public interface StructureMetadata {

    /**
     * 是否支持重命名表
     */
    boolean supportsRenameTable();

    /**
     * 是否支持备份表
     */
    boolean supportsBackupTable();

    /**
     * 重命名表
     * @param nodePath 目标表路径（库/表）
     * @param newName 新表名
     * @return 是否成功
     */
    boolean renameTable(String nodePath, String newName);

    /**
     * 备份表
     * @param nodePath 目标表路径（库/表）
     * @param backupName 备份表名（或带后缀）
     * @return 是否成功
     */
    boolean backupTable(String nodePath, String backupName);

    /** 简单请求模型 */
    @Data
    class RenameTableRequest {
        private String nodePath;
        private String newName;
    }

    /** 简单请求模型 */
    @Data
    class BackupTableRequest {
        private String nodePath;
        private String backupName;
    }
}

