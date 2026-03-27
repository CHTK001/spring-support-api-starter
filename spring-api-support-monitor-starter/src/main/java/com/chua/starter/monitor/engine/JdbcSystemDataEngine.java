package com.chua.starter.monitor.engine;

import com.chua.common.support.data.datasource.dialect.Dialect;
import com.chua.common.support.data.datasource.meta.TableMetadata;
import com.chua.common.support.data.datasource.jdbc.option.DataSourceOptions;
import com.chua.common.support.data.query.FeatureSupportLevel;
import com.chua.common.support.data.query.JdbcEngine;
import com.chua.common.support.data.query.definition.query.Search;
import com.chua.common.support.data.query.definition.query.Update;
import com.chua.common.support.metadata.base.ConstructMetadata;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.pojo.data.DeleteRowRequest;
import com.chua.starter.monitor.pojo.data.InsertRowRequest;
import com.chua.starter.monitor.pojo.data.UpdateRowRequest;
import com.chua.starter.monitor.spi.metadata.StructureMetadata;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JDBC 数据源的统一引擎实现。
 */
public class JdbcSystemDataEngine extends LegacySpiSystemDataEngine {

    private JdbcEngine jdbcEngine;
    private DataSource dataSource;

    public JdbcSystemDataEngine(SystemDataSetting setting, DataSourceOptions options) {
        super(setting, options);
    }

    @Override
    public FeatureSupportLevel capabilitySupport(SystemDataCapability capability) {
        if (capability == null) {
            return FeatureSupportLevel.NONE;
        }
        return switch (capability) {
            case ORM_SEARCH, ORM_UPDATE, SQL_QUERY, SQL_UPDATE, TABLE_BACKUP, STRUCTURE, INDEXES, ROW_MUTATION ->
                    FeatureSupportLevel.FULL;
            case PARTITIONS -> supportsPartitionManagement() ? FeatureSupportLevel.FULL : FeatureSupportLevel.NONE;
            default -> super.capabilitySupport(capability);
        };
    }

    @Override
    public <T> Search<T> search(Class<T> targetType) {
        return ensureJdbcEngine().search(targetType);
    }

    public <T> Update<T> update(Class<T> targetType) {
        return ensureJdbcEngine().update(targetType);
    }

    @Override
    public List<Map<String, Object>> query(String sql, Object... args) throws Exception {
        return ensureJdbcEngine().executeSql(resolveDataSourceName(getSetting()), sql, args);
    }

    @Override
    public int executeUpdate(String sql, Object... args) throws Exception {
        return ensureJdbcEngine().executeUpdate(resolveDataSourceName(getSetting()), sql, args);
    }

    @Override
    public boolean renameTable(String nodePath, String newName) {
        ConstructMetadata constructMetadata = getConstructMetadata();
        if (constructMetadata == null) {
            return false;
        }
        String[] pathParts = safeSplitPath(nodePath);
        try {
            return !constructMetadata.renameTable(pathParts[1], pathParts[2], newName).hasError();
        } catch (Exception e) {
            throw new RuntimeException("重命名表失败", e);
        }
    }

    @Override
    public boolean backupTable(String nodePath, String backupName) {
        ConstructMetadata constructMetadata = getConstructMetadata();
        if (constructMetadata == null) {
            return false;
        }
        String[] pathParts = safeSplitPath(nodePath);
        try {
            return !constructMetadata.copyTableStructure(pathParts[1], pathParts[2], backupName).hasError();
        } catch (Exception e) {
            throw new RuntimeException("备份表失败", e);
        }
    }

    @Override
    public boolean addColumn(StructureMetadata.AddColumnRequest request) {
        Dialect dialect = requiredDialect();
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(quoteIdentifier(request.getTableName(), dialect));
        sql.append(" ADD COLUMN ").append(quoteIdentifier(request.getColumnName(), dialect));
        sql.append(" ").append(request.getDataType());

        if (Boolean.FALSE.equals(request.getNullable())) {
            sql.append(" NOT NULL");
        }
        if (StringUtils.hasText(request.getDefaultValue())) {
            sql.append(" DEFAULT ").append(request.getDefaultValue());
        }
        if (StringUtils.hasText(request.getComment())) {
            sql.append(" COMMENT '").append(escapeSqlLiteral(request.getComment())).append("'");
        }
        if ("FIRST".equalsIgnoreCase(request.getPosition())) {
            sql.append(" FIRST");
        } else if ("AFTER".equalsIgnoreCase(request.getPosition()) && StringUtils.hasText(request.getAfterColumn())) {
            sql.append(" AFTER ").append(quoteIdentifier(request.getAfterColumn(), dialect));
        }
        return executeDdl(sql.toString());
    }

    @Override
    public boolean modifyColumn(StructureMetadata.ModifyColumnRequest request) {
        Dialect dialect = requiredDialect();
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(quoteIdentifier(request.getTableName(), dialect));
        if (StringUtils.hasText(request.getNewColumnName())
                && !request.getNewColumnName().equals(request.getOldColumnName())) {
            sql.append(" CHANGE COLUMN ")
                    .append(quoteIdentifier(request.getOldColumnName(), dialect))
                    .append(" ")
                    .append(quoteIdentifier(request.getNewColumnName(), dialect));
        } else {
            sql.append(" MODIFY COLUMN ").append(quoteIdentifier(request.getOldColumnName(), dialect));
        }

        sql.append(" ").append(request.getDataType());
        if (Boolean.FALSE.equals(request.getNullable())) {
            sql.append(" NOT NULL");
        } else {
            sql.append(" NULL");
        }
        if (StringUtils.hasText(request.getDefaultValue())) {
            sql.append(" DEFAULT ").append(request.getDefaultValue());
        }
        if (StringUtils.hasText(request.getComment())) {
            sql.append(" COMMENT '").append(escapeSqlLiteral(request.getComment())).append("'");
        }
        return executeDdl(sql.toString());
    }

    @Override
    public boolean dropColumn(StructureMetadata.DropColumnRequest request) {
        Dialect dialect = requiredDialect();
        String sql = "ALTER TABLE " + quoteIdentifier(request.getTableName(), dialect)
                + " DROP COLUMN " + quoteIdentifier(request.getColumnName(), dialect);
        return executeDdl(sql);
    }

    @Override
    public boolean batchDropColumn(StructureMetadata.BatchDropColumnRequest request) {
        if (request.getColumnNames() == null || request.getColumnNames().isEmpty()) {
            return true;
        }
        for (String columnName : request.getColumnNames()) {
            StructureMetadata.DropColumnRequest item = new StructureMetadata.DropColumnRequest();
            item.setTableName(request.getTableName());
            item.setColumnName(columnName);
            dropColumn(item);
        }
        return true;
    }

    @Override
    public boolean batchAddColumn(StructureMetadata.BatchAddColumnRequest request) {
        if (request.getColumns() == null || request.getColumns().isEmpty()) {
            return true;
        }
        for (StructureMetadata.AddColumnRequest item : request.getColumns()) {
            item.setTableName(request.getTableName());
            addColumn(item);
        }
        return true;
    }

    @Override
    public boolean batchModifyColumn(StructureMetadata.BatchModifyColumnRequest request) {
        if (request.getColumns() == null || request.getColumns().isEmpty()) {
            return true;
        }
        for (StructureMetadata.ModifyColumnRequest item : request.getColumns()) {
            item.setTableName(request.getTableName());
            modifyColumn(item);
        }
        return true;
    }

    @Override
    public boolean reorderColumn(StructureMetadata.ReorderColumnRequest request) {
        Dialect dialect = requiredDialect();
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(quoteIdentifier(request.getTableName(), dialect));
        sql.append(" MODIFY COLUMN ").append(quoteIdentifier(request.getColumnName(), dialect));
        sql.append(" ").append(resolveColumnDefinition(request.getTableName(), request.getColumnName(), dialect));

        if ("FIRST".equalsIgnoreCase(request.getPosition())) {
            sql.append(" FIRST");
        } else if ("AFTER".equalsIgnoreCase(request.getPosition()) && StringUtils.hasText(request.getAfterColumn())) {
            sql.append(" AFTER ").append(quoteIdentifier(request.getAfterColumn(), dialect));
        }
        return executeDdl(sql.toString());
    }

    @Override
    public boolean modifyTableComment(StructureMetadata.ModifyTableCommentRequest request) {
        Dialect dialect = requiredDialect();
        String sql = "ALTER TABLE " + quoteIdentifier(request.getTableName(), dialect)
                + " COMMENT '" + escapeSqlLiteral(request.getComment()) + "'";
        return executeDdl(sql);
    }

    @Override
    public boolean batchModifyTableStructure(StructureMetadata.BatchTableStructureRequest request) {
        if (request.getDropColumns() != null && !request.getDropColumns().isEmpty()) {
            StructureMetadata.BatchDropColumnRequest dropRequest = new StructureMetadata.BatchDropColumnRequest();
            dropRequest.setTableName(request.getTableName());
            dropRequest.setColumnNames(request.getDropColumns());
            batchDropColumn(dropRequest);
        }
        if (request.getAddColumns() != null && !request.getAddColumns().isEmpty()) {
            StructureMetadata.BatchAddColumnRequest addRequest = new StructureMetadata.BatchAddColumnRequest();
            addRequest.setTableName(request.getTableName());
            addRequest.setColumns(request.getAddColumns());
            batchAddColumn(addRequest);
        }
        if (request.getModifyColumns() != null && !request.getModifyColumns().isEmpty()) {
            StructureMetadata.BatchModifyColumnRequest modifyRequest = new StructureMetadata.BatchModifyColumnRequest();
            modifyRequest.setTableName(request.getTableName());
            modifyRequest.setColumns(request.getModifyColumns());
            batchModifyColumn(modifyRequest);
        }
        if (request.getReorderColumns() != null && !request.getReorderColumns().isEmpty()) {
            for (StructureMetadata.ReorderColumnRequest item : request.getReorderColumns()) {
                item.setTableName(request.getTableName());
                reorderColumn(item);
            }
        }
        if (StringUtils.hasText(request.getTableComment())) {
            StructureMetadata.ModifyTableCommentRequest commentRequest = new StructureMetadata.ModifyTableCommentRequest();
            commentRequest.setTableName(request.getTableName());
            commentRequest.setComment(request.getTableComment());
            modifyTableComment(commentRequest);
        }
        return true;
    }

    @Override
    public List<Map<String, Object>> getIndexes(String tableName) {
        return structure().getIndexes(tableName);
    }

    @Override
    public boolean createIndex(StructureMetadata.CreateIndexRequest request) {
        return structure().createIndex(request);
    }

    @Override
    public boolean dropIndex(String tableName, String indexName) {
        return structure().dropIndex(tableName, indexName);
    }

    @Override
    public List<Map<String, Object>> getPartitions(String tableName) {
        if (!supports(SystemDataCapability.PARTITIONS)) {
            return List.of();
        }
        return structure().getPartitions(tableName);
    }

    @Override
    public boolean createPartition(StructureMetadata.CreatePartitionRequest request) {
        return supports(SystemDataCapability.PARTITIONS) && structure().createPartition(request);
    }

    @Override
    public boolean dropPartition(String tableName, String partitionName) {
        return supports(SystemDataCapability.PARTITIONS) && structure().dropPartition(tableName, partitionName);
    }

    @Override
    public boolean updateRow(UpdateRowRequest request) {
        Dialect dialect = requiredDialect();
        String tableName = quoteIdentifier(request.getTableName(), dialect);
        String columnName = quoteIdentifier(request.getColumnName(), dialect);
        String primaryKey = quoteIdentifier(request.getPrimaryKey(), dialect);

        String sql;
        Object[] args;
        if (request.getNewValue() == null) {
            sql = String.format("UPDATE %s SET %s = NULL WHERE %s = ?", tableName, columnName, primaryKey);
            args = new Object[]{request.getPrimaryValue()};
        } else {
            sql = String.format("UPDATE %s SET %s = ? WHERE %s = ?", tableName, columnName, primaryKey);
            args = new Object[]{request.getNewValue(), request.getPrimaryValue()};
        }
        return executePreparedUpdate(sql, args) >= 0;
    }

    @Override
    public boolean deleteRow(DeleteRowRequest request) {
        Dialect dialect = requiredDialect();
        String sql = String.format("DELETE FROM %s WHERE %s = ?",
                quoteIdentifier(request.getTableName(), dialect),
                quoteIdentifier(request.getPrimaryKey(), dialect));
        return executePreparedUpdate(sql, request.getPrimaryValue()) >= 0;
    }

    @Override
    public boolean insertRow(InsertRowRequest request) {
        Dialect dialect = requiredDialect();
        Map<String, Object> data = request.getData();
        if (data == null || data.isEmpty()) {
            return false;
        }

        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> args = new ArrayList<>();
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append(quoteIdentifier(entry.getKey(), dialect));
            if (entry.getValue() == null) {
                values.append("NULL");
            } else {
                values.append("?");
                args.add(entry.getValue());
            }
            first = false;
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                quoteIdentifier(request.getTableName(), dialect), columns, values);
        return executePreparedUpdate(sql, args.toArray()) >= 0;
    }

    public JdbcEngine getJdbcEngine() {
        return ensureJdbcEngine();
    }

    public Dialect getDialect() {
        try {
            return ensureJdbcEngine()
                    .getEngineDataSource(new TableMetadata())
                    .getDialect();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        if (jdbcEngine != null) {
            jdbcEngine.close();
        }
    }

    private static String resolveDataSourceName(SystemDataSetting setting) {
        if (setting.getSystemDataSettingName() != null && !setting.getSystemDataSettingName().isBlank()) {
            return setting.getSystemDataSettingName();
        }
        return "system-data-" + setting.getSystemDataSettingId();
    }

    private synchronized JdbcEngine ensureJdbcEngine() {
        if (jdbcEngine != null) {
            return jdbcEngine;
        }
        dataSource = getOptions().createDataSource(10);
        jdbcEngine = new JdbcEngine();
        String dataSourceName = resolveDataSourceName(getSetting());
        jdbcEngine.addDataSource(dataSourceName, dataSource);
        jdbcEngine.setDefaultDataSourceName(dataSourceName);
        return jdbcEngine;
    }

    private ConstructMetadata getConstructMetadata() {
        try {
            if (hierarchy() instanceof ConstructMetadata constructMetadata) {
                return constructMetadata;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String[] safeSplitPath(String nodePath) {
        String[] pathParts = nodePath == null ? new String[0] : nodePath.split("/");
        if (pathParts.length < 3) {
            throw new IllegalArgumentException("非法表路径: " + nodePath);
        }
        return pathParts;
    }

    private boolean executeDdl(String sql) {
        return executePreparedUpdate(sql) >= 0;
    }

    private int executePreparedUpdate(String sql, Object... args) {
        try {
            return ensureJdbcEngine().executeUpdate(resolveDataSourceName(getSetting()), sql, args);
        } catch (Exception e) {
            throw new RuntimeException("执行 SQL 失败: " + sql, e);
        }
    }

    private Dialect requiredDialect() {
        Dialect dialect = getDialect();
        if (dialect == null) {
            throw new IllegalStateException("无法识别当前数据源方言");
        }
        return dialect;
    }

    private String quoteIdentifier(String identifier, Dialect dialect) {
        if (!StringUtils.hasText(identifier)) {
            return "";
        }
        char openQuote = dialect.openQuote();
        char closeQuote = dialect.closeQuote();
        if (openQuote == ' ' && closeQuote == ' ') {
            return identifier;
        }
        return Arrays.stream(identifier.split("\\."))
                .map(item -> openQuote + item + closeQuote)
                .collect(Collectors.joining("."));
    }

    private String escapeSqlLiteral(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

    private String resolveColumnDefinition(String tableName, String columnName, Dialect dialect) {
        if (!SystemDataEngineSupport.containsAny(getSetting(),
                "mysql", "mariadb", "tidb", "oceanbase", "polardb", "tdsql")) {
            throw new UnsupportedOperationException("当前方言暂不支持字段重排: " + getSetting().getSystemDataSettingType());
        }

        try {
            List<Map<String, Object>> rows = query(
                    "SHOW FULL COLUMNS FROM " + quoteIdentifier(tableName, dialect) + " LIKE ?",
                    columnName
            );
            if (rows == null || rows.isEmpty()) {
                throw new IllegalArgumentException("字段不存在: " + columnName);
            }
            Map<String, Object> row = rows.get(0);
            String type = stringValue(row.getOrDefault("Type", row.get("COLUMN_TYPE")));
            String nullable = stringValue(row.getOrDefault("Null", row.get("IS_NULLABLE")));
            Object defaultValue = row.getOrDefault("Default", row.get("COLUMN_DEFAULT"));
            String extra = stringValue(row.getOrDefault("Extra", row.get("EXTRA")));
            String comment = stringValue(row.getOrDefault("Comment", row.get("COLUMN_COMMENT")));

            StringBuilder definition = new StringBuilder(type);
            if ("NO".equalsIgnoreCase(nullable)) {
                definition.append(" NOT NULL");
            } else {
                definition.append(" NULL");
            }
            if (defaultValue != null) {
                definition.append(" DEFAULT ").append(formatDefaultValue(defaultValue));
            }
            if (StringUtils.hasText(extra)) {
                definition.append(" ").append(extra);
            }
            if (StringUtils.hasText(comment)) {
                definition.append(" COMMENT '").append(escapeSqlLiteral(comment)).append("'");
            }
            return definition.toString();
        } catch (Exception e) {
            throw new RuntimeException("解析字段定义失败: " + tableName + "." + columnName, e);
        }
    }

    private String formatDefaultValue(Object defaultValue) {
        if (defaultValue == null) {
            return "NULL";
        }
        if (defaultValue instanceof Number || defaultValue instanceof Boolean) {
            return String.valueOf(defaultValue);
        }
        String value = String.valueOf(defaultValue);
        if ("NULL".equalsIgnoreCase(value)) {
            return "NULL";
        }
        String upper = value.toUpperCase(Locale.ROOT);
        if (upper.startsWith("CURRENT_") || upper.startsWith("NOW(")) {
            return value;
        }
        if ((value.startsWith("'") && value.endsWith("'")) || (value.startsWith("\"") && value.endsWith("\""))) {
            return value;
        }
        return "'" + escapeSqlLiteral(value) + "'";
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
