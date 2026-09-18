package com.chua.starter.scheduler.support;

import com.chua.common.support.lang.datasource.dialect.Dialect;
import com.chua.common.support.lang.datasource.dialect.meta.ColumnMetadata;
import com.chua.common.support.lang.datasource.dialect.meta.TableMetadata;
import com.chua.common.support.lang.datasource.engine.ddl.ColumnDef;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 基于 JDBC 方言的表结构执行器。
 * <p>
 * 替代旧版 {@code com.chua.datasource.support.data.query.ddl.JdbcTableExecutor}，
 * 使用新架构的 {@link Dialect} SPI 生成 CREATE TABLE / DROP TABLE / ALTER 语句，
 * 通过 {@link DataSource} 直接执行。
 * </p>
 *
 * @since 4.0.0
 * @author CH
 */
@Slf4j
public class JdbcTableExecutor {

    /**
     * 数据源
     */
    private final DataSource dataSource;

    /**
     * 方言
     */
    private final Dialect dialect;

    /**
     * 构造执行器。
     *
     * @param dataSource 数据源
     * @param dialect    方言
     */
    public JdbcTableExecutor(DataSource dataSource, Dialect dialect) {
        this.dataSource = dataSource;
        this.dialect = dialect;
    }

    /**
     * 获取数据源。
     *
     * @return 数据源
     */
    public DataSource dataSource() {
        return dataSource;
    }

    /**
     * 按动作执行表结构变更。
     *
     * @param metadata 表元数据
     * @param action   动作
     * @return 错误消息列表，无错误时返回空列表
     */
    public List<String> doIt(TableMetadata metadata, ActionType action) {
        List<String> errors = new ArrayList<>();
        if (metadata == null || metadata.getName() == null) {
            errors.add("表元数据缺失");
            return errors;
        }
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : buildSqls(metadata, action)) {
                try {
                    statement.execute(sql);
                } catch (SQLException e) {
                    errors.add(e.getMessage());
                }
            }
        } catch (SQLException e) {
            errors.add(e.getMessage());
        }
        return errors;
    }

    /**
     * 按动作构建 DDL 语句列表。
     *
     * @param metadata 表元数据
     * @param action   动作
     * @return DDL 语句列表
     */
    public List<String> buildSqls(TableMetadata metadata, ActionType action) {
        List<String> sqls = new ArrayList<>();
        switch (action) {
            case CREATE:
                sqls.add(buildCreateTable(metadata));
                break;
            case DROP_CREATE:
                sqls.add(dialect.getDropTableString(metadata.getName()));
                sqls.add(buildCreateTable(metadata));
                break;
            case UPDATE:
                sqls.addAll(buildUpdateSqls(metadata));
                break;
        }
        return sqls;
    }

    /**
     * 构建 CREATE TABLE 语句。
     *
     * @param metadata 表元数据
     * @return CREATE TABLE 语句
     */
    private String buildCreateTable(TableMetadata metadata) {
        List<ColumnDef> columns = toColumnDefs(metadata);
        StringBuilder sql = new StringBuilder(dialect.getCreateTableString().toUpperCase(Locale.ROOT) + " IF NOT EXISTS ")
                .append(dialect.quote(metadata.getName()));
        sql.append(" (");
        boolean first = true;
        for (ColumnDef column : columns) {
            if (!first) {
                sql.append(", ");
            }
            first = false;
            if (column.isAutoIncrement()) {
                column.setPrimaryKey(true);
            }
            if (column.isPrimaryKey()) {
                sql.append("PRIMARY KEY ");
            }
            sql.append(dialect.quote(column.getName()))
                    .append(' ')
                    .append(column.getType());
            if (column.isAutoIncrement()) {
                sql.append(' ').append(dialect.getAutoIncrementKeyword());
            }
            if (!column.isNullable()) {
                sql.append(" NOT NULL");
            }
            if (column.getDefaultValue() != null) {
                sql.append(" DEFAULT ").append(column.getDefaultValue());
            }
        }
        sql.append(')');
        if (metadata.getComment() != null) {
            sql.append(" COMMENT '").append(metadata.getComment()).append('\'');
        }
        return sql.toString();
    }

    /**
     * 将表元数据列转换为 DDL 列定义。
     *
     * @param metadata 表元数据
     * @return 列定义列表
     */
    private List<ColumnDef> toColumnDefs(TableMetadata metadata) {
        List<ColumnDef> columns = new ArrayList<>();
        if (metadata.getColumns() != null) {
            for (ColumnMetadata column : metadata.getColumns()) {
                columns.add(new ColumnDef()
                        .setName(column.getColumnName())
                        .setType(resolveColumnType(column))
                        .setPrimaryKey(column.isPrimaryKey())
                        .setAutoIncrement(column.isAutoIncrement())
                        .setNullable(column.isNullable())
                        .setDefaultValue(column.getDefaultValue())
                        .setComment(column.getComment()));
            }
        }
        return columns;
    }

    /**
     * 解析列类型名。
     *
     * @param column 列元数据
     * @return 列类型名
     */
    private String resolveColumnType(ColumnMetadata column) {
        if (column.getJdbcType() != null) {
            return column.getJdbcType();
        }
        return dialect.getTypeName(column.getSqlType(), column.getLength(), column.getPrecision(), column.getScale());
    }

    /**
     * 构建 UPDATE 动作的 DDL（补齐缺失列）。
     *
     * @param metadata 表元数据
     * @return DDL 语句列表
     */
    private List<String> buildUpdateSqls(TableMetadata metadata) {
        List<String> sqls = new ArrayList<>();
        if (metadata.getColumns() == null) {
            return sqls;
        }
        for (ColumnDef column : toColumnDefs(metadata)) {
            sqls.add(dialect.getAlterTableString(metadata.getName()) + " " + dialect.getAddColumnString() + " "
                    + dialect.quote(column.getName()) + " " + column.getType() + " NULL");
        }
        return sqls;
    }

    /**
     * 表结构动作。
     *
     * @since 4.0.0
     * @author CH
     */
    public enum ActionType {
        /**
         * 创建表（已存在则跳过）
         */
        CREATE,
        /**
         * 先删后建
         */
        DROP_CREATE,
        /**
         * 更新表结构（补齐缺失列）
         */
        UPDATE
    }
}
