package com.chua.starter.collect.support;

import com.chua.starter.collect.entity.CollectDatasource;
import com.chua.starter.collect.mapper.CollectDatasourceMapper;
import com.chua.starter.collect.vo.CollectColumnVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.datasource.dialect.SqlName;
import com.chua.starter.datasourcehub.support.model.SchemaColumnVO;
import com.chua.starter.datasourcehub.support.model.dto.DdlColumn;
import com.chua.starter.datasourcehub.support.model.dto.TableDdlDTO;
import com.chua.starter.datasourcehub.support.service.DatasourceSchemaService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 采集平台的 JDBC 通道。
 *
 * <p>SQL 节点与结果导出共用它。数据源引用留空或填 {@code app} 时用应用主库，
 * 否则按 {@code collect_datasource} 的ID或名称解析，连接池按数据源缓存复用。</p>
 *
 * <p>语句由流程作者编写，本类只保证一次执行一条语句、表名列名合法、值全部走
 * PreparedStatement 绑定，不做语句白名单。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CollectJdbcSupport {

    /**
     * 应用主库引用名。
     */
    public static final String APP_DATASOURCE = "app";

    /**
     * 结果表主键列。
     */
    private static final String ID_COLUMN = "collect_export_id";

    /**
     * 导出表列的默认类型。
     */
    private static final String COLUMN_TYPE = "TEXT";

    /**
     * 连接池缓存，数据源引用到连接池。
     */
    private final Map<String, DataSource> pools = new ConcurrentHashMap<>();

    /**
     * 数据源解析器，台账读数据源中心。
     */
    private final CollectDatasourceRegistry registry;

    /**
     * 数据源模块（hub）的结构与 DDL 服务。
     *
     * <p>表清单、列元数据、建表/补列都委托它：这些能力归数据源模块，采集侧不再自实现。</p>
     */
    private final DatasourceSchemaService schemaService;

    /**
     * 应用自身数据源。
     */
    private final ObjectProvider<DataSource> applicationDataSource;

    /**
     * 解析数据源。
     *
     * @param reference 留空或 app 表示应用主库，其余按 collect_datasource 的ID或名称
     * @return 数据源
     */
    public DataSource resolve(String reference) {
        String key = reference == null || reference.isBlank() ? APP_DATASOURCE : reference.trim();
        DataSource cached = pools.get(key);
        if (cached != null) {
            return cached;
        }
        DataSource created = create(key);
        pools.put(key, created);
        return created;
    }

    /**
     * 数据源配置变更后丢弃缓存的连接池，应用主库只解除引用不关闭。
     *
     * @param reference 数据源引用
     */
    public void discard(String reference) {
        String key = reference == null || reference.isBlank() ? APP_DATASOURCE : reference.trim();
        DataSource dataSource = pools.remove(key);
        if (!APP_DATASOURCE.equals(key) && dataSource instanceof HikariDataSource hikari) {
            hikari.close();
        }
    }

    /**
     * 执行查询。
     *
     * @param reference 数据源引用
     * @param sql       单条查询语句
     * @return 有序行映射列表
     */
    public List<Map<String, Object>> query(String reference, String sql) {
        String statement = singleStatement(sql, true);
        try (Connection connection = resolve(reference).getConnection();
             PreparedStatement ps = connection.prepareStatement(statement);
             ResultSet rs = ps.executeQuery()) {
            return readRows(rs);
        } catch (SQLException ex) {
            throw new IllegalStateException("查询失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * 列出数据源里的表与视图。
     *
     * <p>表结构属于数据源模块的能力：这里直接委托 {@code DatasourceSchemaService}，
     * 采集侧不再自己读元数据，保证"同一份实现"。</p>
     *
     * @param reference 数据源引用
     * @return 表名列表，已去重并按名称排序
     */
    public List<String> listTables(String reference) {
        return schemaService.listTables(reference);
    }

    /**
     * 列出指定表的列元数据。
     *
     * <p>同样委托数据源模块：跨方言的大小写处理、主键标记都在
     * {@code DatasourceSchemaService} 里统一做，采集侧只做一次 VO 转换。</p>
     *
     * @param reference 数据源引用
     * @param table     表名
     * @return 列元数据，按列序号排序
     */
    public List<CollectColumnVO> listColumns(String reference, String table) {
        List<SchemaColumnVO> columns = schemaService.listColumns(reference, table);
        List<CollectColumnVO> result = new ArrayList<>(columns.size());
        for (SchemaColumnVO column : columns) {
            CollectColumnVO item = new CollectColumnVO(column.name(), column.label(), column.position(),
                    column.typeName(), column.size(), column.nullable());
            item.setPrimaryKey(column.primaryKey());
            result.add(item);
        }
        return result;
    }

    /**
     * 执行更新。
     *
     * @param reference 数据源引用
     * @param sql       单条更新语句
     * @return 受影响行数
     */
    public int update(String reference, String sql) {
        String statement = singleStatement(sql, false);
        try (Connection connection = resolve(reference).getConnection();
             PreparedStatement ps = connection.prepareStatement(statement)) {
            return ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("更新失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * 插入一行。
     *
     * <p>表名与列名只能来自严格 ASCII 标识符白名单，值全部通过 {@link PreparedStatement} 绑定。</p>
     *
     * @param reference 数据源引用
     * @param table     表名
     * @param row       列名到值的映射，顺序即插入顺序
     * @return 受影响行数
     */
    public int insert(String reference, String table, Map<String, Object> row) {
        String name = identifier(table);
        List<String> columns = new ArrayList<>(row.keySet());
        for (String column : columns) {
            identifier(column);
        }
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(name).append(" (");
        sql.append(String.join(", ", columns)).append(") VALUES (");
        sql.append(String.join(", ", columns.stream().map(column -> "?").toList())).append(")");
        try (Connection connection = resolve(reference).getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int index = 0; index < columns.size(); index++) {
                ps.setObject(index + 1, value(row.get(columns.get(index))));
            }
            return ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("写入 " + name + " 失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * 建结果表并补齐缺失列，列类型统一用大文本。
     *
     * <p>表名与列名只能来自严格 ASCII 标识符白名单；已存在表还必须由 JDBC 元数据确认，
     * 不允许将流程作者提供的任意标识符片段直接拼入 DDL。</p>
     *
     * @param reference 数据源引用
     * @param table     表名
     * @param columns   列名集合
     */
    public void ensureTable(String reference, String table, Collection<String> columns) {
        String name = identifier(table);
        List<DdlColumn> targets = new ArrayList<>(columns.size());
        for (String column : columns) {
            // 结果列一律按大文本建：导出内容可能是长 JSON / 长文本，
            // 交给数据源模块按来源类型推断会落成 VARCHAR(255)，会截断甚至写入报错。
            targets.add(new DdlColumn(identifier(column), null, Boolean.TRUE, null, null, column, "TEXT"));
        }
        // 建表/补列的方言拼装与"表已存在"策略由数据源模块负责：表不存在则建（自带自增主键 id），
        // 已存在则只补缺失列——与原来 ensureTable 的语义一致，但类型与主键由数据源模块统一决定。
        schemaService.executeDdl(new TableDdlDTO(reference, name, targets, null, null, "APPEND", Boolean.TRUE));
    }

    /**
     * 建数据源：应用主库直接取容器里的 DataSource，其余交数据源中心解析后建连接池。
     *
     * @param key 数据源引用
     * @return 数据源
     */
    private DataSource create(String key) {
        if (APP_DATASOURCE.equals(key)) {
            DataSource dataSource = applicationDataSource.getIfAvailable();
            if (dataSource == null) {
                throw new IllegalStateException("应用没有可用数据源，SQL 节点只能配置数据源中心里的数据源");
            }
            return dataSource;
        }
        CollectDatasourceRegistry.Target target = registry.resolve(key);
        return new HikariDataSource(registry.config(target, key, 30000L));
    }

    /**
     * 试探一份数据源配置能否连通，不产生缓存连接池。
     *
     * <p>供「保存前先测一下」用，因此入参仍是采集侧的 DTO 对象，
     * 密码按存储值（{@code b64:} 前缀）或明文均可。</p>
     *
     * @param datasource 数据源配置
     * @return 数据库产品名与版本
     * @throws IllegalStateException 连接失败
     */
    public String probe(CollectDatasource datasource) {
        HikariConfig config = config(datasource, "probe", 8000L);
        config.setInitializationFailTimeout(1L);
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(0);
        try (HikariDataSource dataSource = new HikariDataSource(config);
             Connection connection = dataSource.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            return meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
        } catch (SQLException ex) {
            throw new IllegalStateException("连接失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * 组装连接池配置。
     *
     * @param entity           数据源配置
     * @param poolName         连接池名
     * @param connectTimeoutMs 取连接超时毫秒
     * @return 连接池配置
     */
    private HikariConfig config(CollectDatasource entity, String poolName, long connectTimeoutMs) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(entity.getCollectDatasourceJdbcUrl());
        config.setUsername(entity.getCollectDatasourceUsername());
        config.setPassword(CollectSecrets.decode(entity.getCollectDatasourcePassword()));
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(connectTimeoutMs);
        config.setIdleTimeout(600000L);
        config.setMaxLifetime(1800000L);
        config.setPoolName("collect-ds-" + poolName);
        String driver = entity.getCollectDatasourceDriverClassName();
        if (driver != null && !driver.isBlank()) {
            config.setDriverClassName(driver.trim());
        }
        return config;
    }

    /**
     * 读取结果集。
     *
     * @param rs 结果集
     * @return 行映射列表
     * @throws SQLException 读取失败
     */
    private List<Map<String, Object>> readRows(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int index = 1; index <= count; index++) {
                row.put(meta.getColumnLabel(index).toLowerCase(Locale.ROOT), clean(rs.getObject(index)));
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * 把数据库专有类型转成可入表达式的普通值。
     *
     * @param value 原值
     * @return 转换后的值
     * @throws SQLException 读取大字段失败
     */
    private Object clean(Object value) throws SQLException {
        if (value instanceof Clob clob) {
            return clob.getSubString(1, (int) Math.min(clob.length(), 1_000_000L));
        }
        if (value instanceof java.sql.Time || value instanceof java.sql.Timestamp || value instanceof java.sql.Date) {
            return String.valueOf(value);
        }
        return value;
    }

    /**
     * 绑定前的取值转换，复杂类型序列化为 JSON 文本。
     *
     * @param value 原值
     * @return 可绑定值
     */
    private Object value(Object value) {
        if (value == null || value instanceof Number || value instanceof String || value instanceof Boolean) {
            return value;
        }
        return CollectJson.toJson(value);
    }

    /**
     * 校验并规范标识符。
     *
     * @param name 表名或列名
     * @return 合法名字
     */
    public static String identifier(String name) {
        try {
            return SqlName.checkSimple(name == null ? null : name.trim(), "表名或列名");
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("非法表名或列名: " + name, ex);
        }
    }

    /**
     * 取已存在的列名集合，表不存在返回空集合。
     *
     * @param dataSource 数据源
     * @param table      表名
     * @return 小写列名集合
     */
    private Set<String> existingColumns(DataSource dataSource, String table) {
        Set<String> columns = new HashSet<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            for (String candidate : new String[]{table, table.toUpperCase(Locale.ROOT)}) {
                try (ResultSet rs = meta.getColumns(null, null, candidate, null)) {
                    while (rs.next()) {
                        columns.add(rs.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
                    }
                }
            }
            return columns;
        } catch (SQLException ex) {
            log.warn("[collect] 读取表 {} 的列失败: {}", table, ex.getMessage());
            return columns;
        }
    }

    /**
     * 校验单条语句。
     *
     * @param sql     原始语句
     * @param query   是否查询语句
     * @return 可执行语句
     */
    private String singleStatement(String sql, boolean query) {
        String statement = sql == null ? "" : sql.trim();
        while (statement.endsWith(";")) {
            statement = statement.substring(0, statement.length() - 1).trim();
        }
        if (statement.isEmpty()) {
            throw new IllegalArgumentException("SQL 语句为空");
        }
        if (statement.contains(";")) {
            throw new IllegalArgumentException("一次只能执行一条 SQL 语句");
        }
        String head = statement.toLowerCase(Locale.ROOT);
        if (query != (head.startsWith("select") || head.startsWith("with") || head.startsWith("show"))) {
            throw new IllegalArgumentException(query ? "查询节点只能执行 SELECT 语句" : "更新节点不能执行 SELECT 语句");
        }
        return statement;
    }
}
