package com.chua.starter.collect.support;

import com.chua.starter.collect.entity.CollectDatasource;
import com.chua.starter.collect.mapper.CollectDatasourceMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

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
     * 合法标识符：字母或下划线开头，仅含字母数字下划线，最长 64。
     */
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,63}");

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
     * 采集数据源表访问。
     */
    private final CollectDatasourceMapper datasourceMapper;

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
     * 执行更新。
     *
     * @param reference 数据源引用
     * @param sql       单条更新语句
     * @return 受影响行数
     */
    public int update(String reference, String sql) {
        String statement = singleStatement(sql, false);
        try (Connection connection = resolve(reference).getConnection();
             Statement ps = connection.createStatement()) {
            return ps.executeUpdate(statement);
        } catch (SQLException ex) {
            throw new IllegalStateException("更新失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * 插入一行。
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
     * @param reference 数据源引用
     * @param table     表名
     * @param columns   列名集合
     */
    public void ensureTable(String reference, String table, Collection<String> columns) {
        String name = identifier(table);
        DataSource dataSource = resolve(reference);
        Set<String> exists = existingColumns(dataSource, name);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            if (exists.isEmpty()) {
                StringBuilder sql = new StringBuilder("CREATE TABLE ").append(name).append(" (")
                        .append(ID_COLUMN).append(" BIGINT NOT NULL AUTO_INCREMENT, PRIMARY KEY (").append(ID_COLUMN)
                        .append(")");
                for (String column : columns) {
                    sql.append(", ").append(identifier(column)).append(' ').append(COLUMN_TYPE);
                }
                sql.append(')');
                statement.executeUpdate(sql.toString());
                return;
            }
            for (String column : columns) {
                String legal = identifier(column);
                if (exists.contains(legal.toLowerCase(Locale.ROOT))) {
                    continue;
                }
                statement.executeUpdate("ALTER TABLE " + name + " ADD COLUMN " + legal + " " + COLUMN_TYPE);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("准备结果表 " + name + " 失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * 建数据源：应用主库直接取容器里的 DataSource，其余读 collect_datasource 建连接池。
     *
     * @param key 数据源引用
     * @return 数据源
     */
    private DataSource create(String key) {
        if (APP_DATASOURCE.equals(key)) {
            DataSource dataSource = applicationDataSource.getIfAvailable();
            if (dataSource == null) {
                throw new IllegalStateException("应用没有可用数据源，SQL 节点只能配置 collect_datasource 数据源");
            }
            return dataSource;
        }
        CollectDatasource entity = find(key);
        if (entity == null) {
            throw new IllegalStateException("找不到数据源: " + key);
        }
        if (entity.getCollectDatasourceStatus() != null && entity.getCollectDatasourceStatus() == 0) {
            throw new IllegalStateException("数据源已停用: " + entity.getCollectDatasourceName());
        }
        return new HikariDataSource(config(entity, key, 30000L));
    }

    /**
     * 试探一份数据源配置能否连通，不产生缓存连接池。
     *
     * @param datasource 数据源配置，密码按存储值或明文均可
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
     * 按ID或名称查数据源配置。
     *
     * @param key 引用文本
     * @return 数据源配置，不存在返回 null
     */
    private CollectDatasource find(String key) {
        if (key.chars().allMatch(Character::isDigit)) {
            return datasourceMapper.selectById(Integer.valueOf(key));
        }
        return datasourceMapper.selectOne(Wrappers.<CollectDatasource>lambdaQuery()
                .eq(CollectDatasource::getCollectDatasourceName, key)
                .last("LIMIT 1"));
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
        String legal = name == null ? "" : name.trim();
        if (!IDENTIFIER.matcher(legal).matches()) {
            throw new IllegalArgumentException("非法表名或列名: " + name);
        }
        return legal;
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
