package com.chua.starter.monitor.service.spider;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.command.CommandResult;
import com.chua.common.support.metadata.query.MetadataQuery;
import com.chua.starter.monitor.pojo.data.ColumnInfo;
import com.chua.starter.monitor.pojo.data.TableStructureInfo;
import com.chua.starter.monitor.pojo.spider.SpiderOutputColumnDefinition;
import com.chua.starter.monitor.pojo.spider.SpiderOutputTableRequest;
import com.chua.starter.monitor.service.data.SystemDataConsoleService;
import com.chua.starter.monitor.spi.metadata.StructureMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 爬虫输出表管理服务
 *
 * @author CH
 * @since 2026/03/27
 */
@Slf4j
@Service
public class SpiderOutputTableService {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Pattern NUMERIC = Pattern.compile("^-?\\d+(\\.\\d+)?$");

    private final JdbcTemplate jdbcTemplate;
    private final SystemDataConsoleService systemDataConsoleService;

    public SpiderOutputTableService(JdbcTemplate jdbcTemplate,
                                    @Nullable SystemDataConsoleService systemDataConsoleService) {
        this.jdbcTemplate = jdbcTemplate;
        this.systemDataConsoleService = systemDataConsoleService;
    }

    public boolean tableExists(String tableName, Integer dataSourceSettingId) {
        validateIdentifier(tableName, "表名");
        if (dataSourceSettingId == null) {
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(1)
                    FROM information_schema.TABLES
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = ?
                    """, Integer.class, tableName);
            return count != null && count > 0;
        }

        return requireSystemDataConsoleService().listTables(dataSourceSettingId).stream()
                .anyMatch(it -> tableName.equalsIgnoreCase(it));
    }

    public TableStructureInfo getTableStructure(String tableName, Integer dataSourceSettingId) {
        validateIdentifier(tableName, "表名");
        if (dataSourceSettingId == null) {
            return getLocalTableStructure(tableName);
        }
        return requireSystemDataConsoleService().getTableStructure(dataSourceSettingId, tableName);
    }

    public String previewCreateTableSql(String tableName, SpiderOutputTableRequest request, String dbType) {
        validateIdentifier(tableName, "表名");
        List<SpiderOutputColumnDefinition> columns = resolveColumns(request);
        validateColumns(columns);

        String normalizedDbType = StringUtils.isBlank(dbType) ? "mysql" : dbType.trim().toLowerCase();
        if (!"mysql".equals(normalizedDbType)) {
            throw new IllegalArgumentException("当前仅支持 mysql 预览 SQL");
        }

        List<String> definitions = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();
        for (SpiderOutputColumnDefinition column : columns) {
            definitions.add(buildColumnDefinition(column));
            if (Boolean.TRUE.equals(column.getPrimaryKey())) {
                primaryKeys.add(quote(column.getName()));
            }
        }

        if (!primaryKeys.isEmpty()) {
            definitions.add("PRIMARY KEY (" + String.join(", ", primaryKeys) + ")");
        }

        String tableComment = escapeSqlComment(request == null ? null : request.getTableComment());
        return "CREATE TABLE " + quote(tableName) + " (\n  "
                + String.join(",\n  ", definitions)
                + "\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
                + (StringUtils.isNotBlank(tableComment) ? " COMMENT='" + tableComment + "'" : "")
                + ";";
    }

    public boolean createTable(String tableName, SpiderOutputTableRequest request, String dbType) {
        String sql = previewCreateTableSql(tableName, request, dbType);
        Integer dataSourceSettingId = request == null ? null : request.getDataSourceSettingId();
        return executeSql(sql, dataSourceSettingId);
    }

    public boolean syncStructure(String tableName, SpiderOutputTableRequest request, String dbType) {
        Integer dataSourceSettingId = request == null ? null : request.getDataSourceSettingId();
        if (!tableExists(tableName, dataSourceSettingId)) {
            return createTable(tableName, request, dbType);
        }

        TableStructureInfo structure = getTableStructure(tableName, dataSourceSettingId);
        Map<String, ColumnInfo> existing = structure.getColumns() == null
                ? Map.of()
                : structure.getColumns().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        column -> column.getName().toLowerCase(),
                        column -> column,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<SpiderOutputColumnDefinition> missing = resolveColumns(request).stream()
                .filter(column -> !existing.containsKey(column.getName().toLowerCase()))
                .toList();

        for (SpiderOutputColumnDefinition column : missing) {
            if (dataSourceSettingId == null) {
                String alterSql = "ALTER TABLE " + quote(tableName) + " ADD COLUMN " + buildColumnDefinition(column);
                executeSql(alterSql, null);
            } else {
                StructureMetadata.AddColumnRequest addColumnRequest = new StructureMetadata.AddColumnRequest();
                addColumnRequest.setTableName(tableName);
                addColumnRequest.setColumnName(column.getName());
                addColumnRequest.setDataType(resolveDataType(column));
                addColumnRequest.setNullable(defaultNullable(column));
                addColumnRequest.setDefaultValue(column.getDefaultValue());
                addColumnRequest.setComment(column.getComment());
                requireSystemDataConsoleService().addColumn(dataSourceSettingId, addColumnRequest);
            }
        }

        if (StringUtils.isNotBlank(request.getTableComment())) {
            if (dataSourceSettingId == null) {
                executeSql("ALTER TABLE " + quote(tableName) + " COMMENT='" + escapeSqlComment(request.getTableComment()) + "'", null);
            } else {
                StructureMetadata.ModifyTableCommentRequest commentRequest = new StructureMetadata.ModifyTableCommentRequest();
                commentRequest.setTableName(tableName);
                commentRequest.setComment(request.getTableComment());
                requireSystemDataConsoleService().modifyTableComment(dataSourceSettingId, commentRequest);
            }
        }
        return true;
    }

    private TableStructureInfo getLocalTableStructure(String tableName) {
        List<ColumnInfo> columns = jdbcTemplate.query("""
                SELECT COLUMN_NAME, COLUMN_TYPE, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION,
                       NUMERIC_SCALE, IS_NULLABLE, COLUMN_KEY, EXTRA, COLUMN_DEFAULT, COLUMN_COMMENT, ORDINAL_POSITION
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                ORDER BY ORDINAL_POSITION
                """, (rs, rowNum) -> ColumnInfo.builder()
                .name(rs.getString("COLUMN_NAME"))
                .dataType(rs.getString("DATA_TYPE"))
                .fullType(rs.getString("COLUMN_TYPE"))
                .length(asInteger(rs.getObject("CHARACTER_MAXIMUM_LENGTH")))
                .precision(asInteger(rs.getObject("NUMERIC_PRECISION")))
                .scale(asInteger(rs.getObject("NUMERIC_SCALE")))
                .nullable("YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")))
                .primaryKey("PRI".equalsIgnoreCase(rs.getString("COLUMN_KEY")))
                .autoIncrement(rs.getString("EXTRA") != null && rs.getString("EXTRA").toLowerCase().contains("auto_increment"))
                .defaultValue(rs.getString("COLUMN_DEFAULT"))
                .comment(rs.getString("COLUMN_COMMENT"))
                .ordinalPosition(rs.getInt("ORDINAL_POSITION"))
                .build(), tableName);

        Map<String, TableStructureInfo.IndexInfo> indexes = new LinkedHashMap<>();
        jdbcTemplate.query("""
                SELECT INDEX_NAME, NON_UNIQUE, INDEX_TYPE, COLUMN_NAME
                FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                ORDER BY INDEX_NAME, SEQ_IN_INDEX
                """, rs -> {
            String indexName = rs.getString("INDEX_NAME");
            boolean unique = rs.getInt("NON_UNIQUE") == 0;
            String indexType = rs.getString("INDEX_TYPE");
            String columnName = rs.getString("COLUMN_NAME");
            TableStructureInfo.IndexInfo indexInfo = indexes.computeIfAbsent(indexName, key -> TableStructureInfo.IndexInfo.builder()
                    .name(key)
                    .unique(unique)
                    .type(indexType)
                    .columns(new ArrayList<>())
                    .build());
            indexInfo.getColumns().add(columnName);
        }, tableName);

        Map<String, Object> tableInfo = jdbcTemplate.queryForMap("""
                SELECT TABLE_NAME, TABLE_TYPE, ENGINE, TABLE_COLLATION, TABLE_ROWS, DATA_LENGTH,
                       INDEX_LENGTH, CREATE_TIME, UPDATE_TIME, TABLE_COMMENT
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                """, tableName);

        String ddl = "";
        try {
            Map<String, Object> ddlRow = jdbcTemplate.queryForMap("SHOW CREATE TABLE " + quote(tableName));
            Object value = ddlRow.get("Create Table");
            if (value == null && ddlRow.size() > 1) {
                value = ddlRow.values().toArray()[1];
            }
            ddl = value == null ? "" : String.valueOf(value);
        } catch (Exception e) {
            log.debug("[Spider] 获取本地表 DDL 失败: {}", tableName, e);
        }

        List<String> primaryKeys = columns.stream()
                .filter(column -> Boolean.TRUE.equals(column.getPrimaryKey()))
                .map(ColumnInfo::getName)
                .toList();

        return TableStructureInfo.builder()
                .tableName(String.valueOf(tableInfo.get("TABLE_NAME")))
                .tableType(String.valueOf(tableInfo.get("TABLE_TYPE")))
                .engine(String.valueOf(tableInfo.get("ENGINE")))
                .collation(String.valueOf(tableInfo.get("TABLE_COLLATION")))
                .rowCount(asLong(tableInfo.get("TABLE_ROWS")))
                .dataSize(asLong(tableInfo.get("DATA_LENGTH")))
                .indexSize(asLong(tableInfo.get("INDEX_LENGTH")))
                .createTime(tableInfo.get("CREATE_TIME") == null ? null : String.valueOf(tableInfo.get("CREATE_TIME")))
                .updateTime(tableInfo.get("UPDATE_TIME") == null ? null : String.valueOf(tableInfo.get("UPDATE_TIME")))
                .tableComment(tableInfo.get("TABLE_COMMENT") == null ? null : String.valueOf(tableInfo.get("TABLE_COMMENT")))
                .columns(columns)
                .indexes(new ArrayList<>(indexes.values()))
                .primaryKeys(primaryKeys)
                .createTableDdl(ddl)
                .build();
    }

    private List<SpiderOutputColumnDefinition> resolveColumns(SpiderOutputTableRequest request) {
        if (request == null || request.getColumns() == null || request.getColumns().isEmpty()) {
            throw new IllegalArgumentException("字段定义不能为空");
        }
        return request.getColumns();
    }

    private void validateColumns(List<SpiderOutputColumnDefinition> columns) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (SpiderOutputColumnDefinition column : columns) {
            if (column == null || StringUtils.isBlank(column.getName())) {
                throw new IllegalArgumentException("字段名不能为空");
            }
            validateIdentifier(column.getName(), "字段名");
            if (StringUtils.isBlank(column.getType())) {
                throw new IllegalArgumentException("字段类型不能为空: " + column.getName());
            }
            if (!names.add(column.getName().toLowerCase())) {
                throw new IllegalArgumentException("字段名重复: " + column.getName());
            }
        }
    }

    private boolean executeSql(String sql, Integer dataSourceSettingId) {
        if (dataSourceSettingId == null) {
            jdbcTemplate.execute(sql);
            return true;
        }

        ReturnResult<CommandResult> result = requireSystemDataConsoleService()
                .execute(dataSourceSettingId, "sql", sql, new MetadataQuery());
        if (result == null || !result.isSuccess()) {
            throw new IllegalStateException(result == null ? "执行 SQL 失败" : result.getMsg());
        }
        return true;
    }

    private String buildColumnDefinition(SpiderOutputColumnDefinition column) {
        StringBuilder builder = new StringBuilder();
        builder.append(quote(column.getName())).append(" ").append(resolveDataType(column));
        builder.append(defaultNullable(column) ? " NULL" : " NOT NULL");
        if (column.getDefaultValue() != null) {
            builder.append(" DEFAULT ").append(formatDefaultValue(column.getDefaultValue()));
        }
        if (Boolean.TRUE.equals(column.getAutoIncrement())) {
            builder.append(" AUTO_INCREMENT");
        }
        if (StringUtils.isNotBlank(column.getComment())) {
            builder.append(" COMMENT '").append(escapeSqlComment(column.getComment())).append("'");
        }
        return builder.toString();
    }

    private String resolveDataType(SpiderOutputColumnDefinition column) {
        String type = column.getType().trim().toUpperCase();
        if (type.contains("(")) {
            return type;
        }

        return switch (type) {
            case "VARCHAR", "CHAR" -> type + "(" + (column.getLength() == null ? 255 : column.getLength()) + ")";
            case "DECIMAL", "NUMERIC" -> type + "(" + (column.getLength() == null ? 18 : column.getLength())
                    + "," + (column.getScale() == null ? 2 : column.getScale()) + ")";
            default -> type;
        };
    }

    private boolean defaultNullable(SpiderOutputColumnDefinition column) {
        return column.getNullable() == null || column.getNullable();
    }

    private String formatDefaultValue(String defaultValue) {
        if (defaultValue == null) {
            return "NULL";
        }
        String normalized = defaultValue.trim();
        if (normalized.isEmpty()) {
            return "''";
        }
        if ("NULL".equalsIgnoreCase(normalized)) {
            return "NULL";
        }
        if ("CURRENT_TIMESTAMP".equalsIgnoreCase(normalized)
                || "CURRENT_TIMESTAMP()".equalsIgnoreCase(normalized)) {
            return "CURRENT_TIMESTAMP";
        }
        if (NUMERIC.matcher(normalized).matches()) {
            return normalized;
        }
        return "'" + normalized.replace("'", "''") + "'";
    }

    private void validateIdentifier(String value, String label) {
        if (StringUtils.isBlank(value) || !IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException(label + "不合法: " + value);
        }
    }

    private String quote(String value) {
        return "`" + value + "`";
    }

    private String escapeSqlComment(String comment) {
        if (comment == null) {
            return null;
        }
        return comment.replace("'", "''");
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    private SystemDataConsoleService requireSystemDataConsoleService() {
        if (systemDataConsoleService == null) {
            throw new IllegalStateException("当前宿主未启用外部数据源控制台能力，无法操作外部数据源");
        }
        return systemDataConsoleService;
    }
}
