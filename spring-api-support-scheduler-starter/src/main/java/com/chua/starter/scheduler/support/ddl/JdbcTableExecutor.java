package com.chua.starter.scheduler.support.ddl;

import javax.sql.DataSource;

/**
 * 表结构执行器。
 *
 * @author CH
 */
public class JdbcTableExecutor {

    /**
     * 数据源
     */
    private final DataSource dataSource;

    /**
     * 构造器。
     *
     * @param dataSource 数据源
     */
    public JdbcTableExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 执行表结构变更。
     *
     * @param metadata   表元数据
     * @param actionType 动作类型
     * @return 执行结果
     */
    public DdlResult doIt(TableMetadata metadata, ActionType actionType) {
        if (metadata == null || metadata.getName() == null) {
            return new DdlResult(true, "表元数据为空");
        }
        String tableName = metadata.getName();
        JdbcEngine engine = new JdbcEngine().setDataSource(dataSource);
        switch (actionType) {
            case DROP_CREATE:
                return dropThenCreate(engine, metadata, tableName);
            case UPDATE:
                return update(engine, metadata, tableName);
            case CREATE:
            default:
                return create(engine, metadata, tableName);
        }
    }

    /**
     * 创建表。
     *
     * @param engine    执行引擎
     * @param metadata  表元数据
     * @param tableName 表名
     * @return 执行结果
     */
    private DdlResult create(JdbcEngine engine, TableMetadata metadata, String tableName) {
        String columns = JdbcEngine.buildColumnsSql(metadata);
        return engine.execute("CREATE TABLE IF NOT EXISTS `" + tableName + "` (" + columns + ")");
    }

    /**
     * 删除后重建表。
     *
     * @param engine    执行引擎
     * @param metadata  表元数据
     * @param tableName 表名
     * @return 执行结果
     */
    private DdlResult dropThenCreate(JdbcEngine engine, TableMetadata metadata, String tableName) {
        DdlResult drop = engine.execute("DROP TABLE IF EXISTS `" + tableName + "`");
        if (drop.hasError()) {
            return drop;
        }
        return create(engine, metadata, tableName);
    }

    /**
     * 更新表结构，缺失的表直接创建。
     *
     * @param engine    执行引擎
     * @param metadata  表元数据
     * @param tableName 表名
     * @return 执行结果
     */
    private DdlResult update(JdbcEngine engine, TableMetadata metadata, String tableName) {
        return create(engine, metadata, tableName);
    }
}
