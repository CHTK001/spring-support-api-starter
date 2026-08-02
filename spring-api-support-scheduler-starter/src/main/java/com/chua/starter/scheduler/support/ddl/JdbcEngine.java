package com.chua.starter.scheduler.support.ddl;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * JDBC 表结构执行引擎。
 *
 * @author CH
 */
public class JdbcEngine {

    /**
     * 数据源
     */
    private DataSource dataSource;

    /**
     * 设置数据源
     *
     * @param dataSource 数据源
     * @return 当前实例
     */
    public JdbcEngine setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    /**
     * 创建表执行器。
     *
     * @return 表执行器
     */
    public JdbcTableExecutor createTable() {
        return new JdbcTableExecutor(dataSource);
    }

    /**
     * 根据元数据拼接列定义。
     *
     * @param metadata 表元数据
     * @return 列定义 SQL 片段
     */
    public static String buildColumnsSql(TableMetadata metadata) {
        if (metadata == null || metadata.getColumnMetadata() == null) {
            return "";
        }
        return metadata.getColumnMetadata().stream()
                .map(column -> "`" + column.getColumnName() + "` VARCHAR(255) NULL")
                .collect(Collectors.joining(","));
    }

    /**
     * 执行 DDL 语句。
     *
     * @param sql DDL 语句
     * @return 执行结果
     */
    public DdlResult execute(String sql) {
        if (dataSource == null) {
            return new DdlResult(true, "数据源未配置");
        }
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            return new DdlResult(false, null);
        } catch (SQLException e) {
            return new DdlResult(true, e.getMessage());
        }
    }
}
