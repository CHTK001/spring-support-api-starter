package com.chua.starter.monitor.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 表结构信息
 *
 * @author CH
 * @since 2025-11-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "表结构信息")
public class TableStructureInfo {

    /**
     * 表名
     */
    @Schema(description = "表名")
    private String tableName;

    /**
     * 表注释
     */
    @Schema(description = "表注释")
    private String tableComment;

    /**
     * 表类型（TABLE/VIEW等）
     */
    @Schema(description = "表类型")
    private String tableType;

    /**
     * 存储引擎
     */
    @Schema(description = "存储引擎")
    private String engine;

    /**
     * 字符集
     */
    @Schema(description = "字符集")
    private String charset;

    /**
     * 排序规则
     */
    @Schema(description = "排序规则")
    private String collation;

    /**
     * 行数（估算）
     */
    @Schema(description = "行数")
    private Long rowCount;

    /**
     * 数据大小（字节）
     */
    @Schema(description = "数据大小")
    private Long dataSize;

    /**
     * 索引大小（字节）
     */
    @Schema(description = "索引大小")
    private Long indexSize;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private String updateTime;

    /**
     * 字段列表
     */
    @Schema(description = "字段列表")
    private List<ColumnInfo> columns;

    /**
     * 索引列表
     */
    @Schema(description = "索引列表")
    private List<IndexInfo> indexes;

    /**
     * 主键字段列表
     */
    @Schema(description = "主键字段列表")
    private List<String> primaryKeys;

    /**
     * 建表DDL语句
     */
    @Schema(description = "建表DDL语句")
    private String createTableDdl;

    /**
     * 索引信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "索引信息")
    public static class IndexInfo {
        /**
         * 索引名
         */
        @Schema(description = "索引名")
        private String name;

        /**
         * 索引类型
         */
        @Schema(description = "索引类型")
        private String type;

        /**
         * 是否唯一
         */
        @Schema(description = "是否唯一")
        private Boolean unique;

        /**
         * 包含的字段
         */
        @Schema(description = "包含的字段")
        private List<String> columns;

        /**
         * 注释
         */
        @Schema(description = "注释")
        private String comment;
    }
}
