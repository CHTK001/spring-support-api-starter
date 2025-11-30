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

    /** 重命名表请求 */
    @Data
    class RenameTableRequest {
        private String nodePath;
        private String newName;
    }

    /** 备份表请求 */
    @Data
    class BackupTableRequest {
        private String nodePath;
        private String backupName;
    }

    /** 添加字段请求 */
    @Data
    class AddColumnRequest {
        /**
         * 表名
         */
        private String tableName;
        /**
         * 字段名
         */
        private String columnName;
        /**
         * 数据类型（如 VARCHAR(255)、INT、BIGINT 等）
         */
        private String dataType;
        /**
         * 是否可空
         */
        private Boolean nullable = true;
        /**
         * 默认值
         */
        private String defaultValue;
        /**
         * 注释
         */
        private String comment;
        /**
         * 添加位置：FIRST 或 AFTER columnName
         */
        private String position;
        /**
         * 在哪个字段之后（当 position 为 AFTER 时使用）
         */
        private String afterColumn;
    }

    /** 修改字段请求 */
    @Data
    class ModifyColumnRequest {
        /**
         * 表名
         */
        private String tableName;
        /**
         * 原字段名
         */
        private String oldColumnName;
        /**
         * 新字段名（如果需要重命名）
         */
        private String newColumnName;
        /**
         * 新数据类型
         */
        private String dataType;
        /**
         * 是否可空
         */
        private Boolean nullable;
        /**
         * 默认值
         */
        private String defaultValue;
        /**
         * 注释
         */
        private String comment;
    }

    /** 删除字段请求 */
    @Data
    class DropColumnRequest {
        /**
         * 表名
         */
        private String tableName;
        /**
         * 字段名
         */
        private String columnName;
    }

    /** 调整字段位置请求 */
    @Data
    class ReorderColumnRequest {
        /**
         * 表名
         */
        private String tableName;
        /**
         * 字段名
         */
        private String columnName;
        /**
         * 位置：FIRST 或 AFTER
         */
        private String position;
        /**
         * 在哪个字段之后（当 position 为 AFTER 时使用）
         */
        private String afterColumn;
    }
}

