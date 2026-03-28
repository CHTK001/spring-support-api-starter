package com.chua.starter.monitor.service.spider;

import com.chua.starter.monitor.pojo.data.ColumnInfo;
import com.chua.starter.monitor.pojo.data.TableStructureInfo;
import com.chua.starter.monitor.pojo.spider.SpiderOutputColumnDefinition;
import com.chua.starter.monitor.pojo.spider.SpiderOutputTableRequest;
import com.chua.starter.monitor.service.data.SystemDataConsoleService;
import com.chua.starter.monitor.spi.metadata.StructureMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpiderOutputTableServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SystemDataConsoleService systemDataConsoleService;

    @InjectMocks
    private SpiderOutputTableService service;

    @Test
    void previewCreateTableSqlBuildsMysqlDdlWithPrimaryKeyCommentAndDefaults() {
        SpiderOutputTableRequest request = new SpiderOutputTableRequest();
        request.setTableComment("抓取结果表");
        request.setColumns(List.of(
                column("id", "BIGINT", false, null, true, true, null, "主键"),
                column("title", "VARCHAR", false, 128, false, false, "", "标题"),
                column("amount", "DECIMAL", true, 10, false, false, "0", "金额", 2),
                column("created_at", "DATETIME", false, null, false, false, "CURRENT_TIMESTAMP", "创建时间")
        ));

        String sql = service.previewCreateTableSql("spider_result", request, "mysql");

        assertThat(sql).contains("CREATE TABLE `spider_result`");
        assertThat(sql).contains("`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键'");
        assertThat(sql).contains("`title` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '标题'");
        assertThat(sql).contains("`amount` DECIMAL(10,2) NULL DEFAULT 0 COMMENT '金额'");
        assertThat(sql).contains("`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'");
        assertThat(sql).contains("PRIMARY KEY (`id`)");
        assertThat(sql).contains("COMMENT='抓取结果表'");
    }

    @Test
    void tableExistsChecksCurrentMonitorDatabaseByInformationSchema() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("spider_result"))).thenReturn(1);

        boolean exists = service.tableExists("spider_result", null);

        assertThat(exists).isTrue();
    }

    @Test
    void tableExistsChecksExternalDataSourceByMetadataService() {
        when(systemDataConsoleService.listTables(7)).thenReturn(List.of("spider_result", "spider_log"));

        boolean exists = service.tableExists("SPIDER_RESULT", 7);

        assertThat(exists).isTrue();
    }

    @Test
    void localMonitorDatabaseOperationsDoNotRequireExternalConsoleService() {
        SpiderOutputTableService localOnlyService = new SpiderOutputTableService(jdbcTemplate, null);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("spider_result"))).thenReturn(1);

        boolean exists = localOnlyService.tableExists("spider_result", null);

        assertThat(exists).isTrue();
    }

    @Test
    void externalDataSourceOperationsFailFastWhenConsoleServiceIsUnavailable() {
        SpiderOutputTableService localOnlyService = new SpiderOutputTableService(jdbcTemplate, null);

        assertThatThrownBy(() -> localOnlyService.tableExists("spider_result", 7))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("外部数据源控制台能力");
    }

    @Test
    void syncStructureAddsMissingColumnsAndCommentForExternalDataSource() {
        SpiderOutputTableRequest request = new SpiderOutputTableRequest();
        request.setDataSourceSettingId(7);
        request.setTableComment("抓取结果");
        request.setColumns(List.of(
                column("id", "BIGINT", false, null, true, true, null, "主键"),
                column("title", "VARCHAR", false, 128, false, false, "", "标题")
        ));

        TableStructureInfo structureInfo = TableStructureInfo.builder()
                .columns(List.of(ColumnInfo.builder().name("id").build()))
                .build();

        when(systemDataConsoleService.listTables(7)).thenReturn(List.of("spider_result"));
        when(systemDataConsoleService.getTableStructure(7, "spider_result")).thenReturn(structureInfo);
        when(systemDataConsoleService.addColumn(eq(7), any(StructureMetadata.AddColumnRequest.class))).thenReturn(true);
        when(systemDataConsoleService.modifyTableComment(eq(7), any(StructureMetadata.ModifyTableCommentRequest.class))).thenReturn(true);

        boolean synced = service.syncStructure("spider_result", request, "mysql");

        assertThat(synced).isTrue();

        ArgumentCaptor<StructureMetadata.AddColumnRequest> addColumnCaptor =
                ArgumentCaptor.forClass(StructureMetadata.AddColumnRequest.class);
        verify(systemDataConsoleService).addColumn(eq(7), addColumnCaptor.capture());
        assertThat(addColumnCaptor.getValue().getTableName()).isEqualTo("spider_result");
        assertThat(addColumnCaptor.getValue().getColumnName()).isEqualTo("title");
        assertThat(addColumnCaptor.getValue().getDataType()).isEqualTo("VARCHAR(128)");
        assertThat(addColumnCaptor.getValue().getDefaultValue()).isEqualTo("");
        assertThat(addColumnCaptor.getValue().getNullable()).isFalse();

        ArgumentCaptor<StructureMetadata.ModifyTableCommentRequest> commentCaptor =
                ArgumentCaptor.forClass(StructureMetadata.ModifyTableCommentRequest.class);
        verify(systemDataConsoleService).modifyTableComment(eq(7), commentCaptor.capture());
        assertThat(commentCaptor.getValue().getTableName()).isEqualTo("spider_result");
        assertThat(commentCaptor.getValue().getComment()).isEqualTo("抓取结果");
    }

    @Test
    void syncStructureAddsMissingColumnsAndCommentForCurrentMonitorDatabase() {
        SpiderOutputTableRequest request = new SpiderOutputTableRequest();
        request.setTableComment("抓取结果");
        request.setColumns(List.of(
                column("id", "BIGINT", false, null, true, true, null, "主键"),
                column("title", "VARCHAR", false, 128, false, false, "", "标题")
        ));

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("spider_result"))).thenReturn(1);
        when(jdbcTemplate.query(
                argThat(sql -> sql.contains("FROM information_schema.COLUMNS")
                        && sql.contains("CHARACTER_MAXIMUM_LENGTH")),
                org.mockito.ArgumentMatchers.<RowMapper<ColumnInfo>>any(),
                eq("spider_result")))
                .thenReturn(List.of(ColumnInfo.builder().name("id").primaryKey(true).build()));
        when(jdbcTemplate.queryForMap(
                argThat(sql -> sql.contains("FROM information_schema.TABLES")),
                eq("spider_result")))
                .thenReturn(Map.of(
                        "TABLE_NAME", "spider_result",
                        "TABLE_TYPE", "BASE TABLE",
                        "ENGINE", "InnoDB",
                        "TABLE_COLLATION", "utf8mb4_unicode_ci",
                        "TABLE_ROWS", 0L,
                        "DATA_LENGTH", 0L,
                        "INDEX_LENGTH", 0L,
                        "TABLE_COMMENT", "old"
                ));
        when(jdbcTemplate.queryForMap("SHOW CREATE TABLE `spider_result`"))
                .thenReturn(Map.of("Table", "spider_result", "Create Table", "CREATE TABLE `spider_result` (`id` bigint)"));
        doNothing().when(jdbcTemplate).execute(anyString());

        boolean synced = service.syncStructure("spider_result", request, "mysql");

        assertThat(synced).isTrue();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, org.mockito.Mockito.times(2)).execute(sqlCaptor.capture());
        assertThat(sqlCaptor.getAllValues()).containsExactly(
                "ALTER TABLE `spider_result` ADD COLUMN `title` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '标题'",
                "ALTER TABLE `spider_result` COMMENT='抓取结果'"
        );
    }

    @Test
    void getTableStructureSupportsBigIntegerMetadataValues() throws Exception {
        when(jdbcTemplate.query(
                argThat(sql -> sql.contains("FROM information_schema.COLUMNS")
                        && sql.contains("CHARACTER_MAXIMUM_LENGTH")),
                org.mockito.ArgumentMatchers.<RowMapper<ColumnInfo>>any(),
                eq("spider_result")))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<ColumnInfo> rowMapper = invocation.getArgument(1, RowMapper.class);
                    ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
                    when(rs.getString("COLUMN_NAME")).thenReturn("title");
                    when(rs.getString("DATA_TYPE")).thenReturn("varchar");
                    when(rs.getString("COLUMN_TYPE")).thenReturn("varchar(128)");
                    when(rs.getObject("CHARACTER_MAXIMUM_LENGTH")).thenReturn(new BigInteger("128"));
                    when(rs.getObject("NUMERIC_PRECISION")).thenReturn(new BigInteger("18"));
                    when(rs.getObject("NUMERIC_SCALE")).thenReturn(new BigInteger("2"));
                    when(rs.getString("IS_NULLABLE")).thenReturn("YES");
                    when(rs.getString("COLUMN_KEY")).thenReturn("");
                    when(rs.getString("EXTRA")).thenReturn("");
                    when(rs.getString("COLUMN_DEFAULT")).thenReturn("");
                    when(rs.getString("COLUMN_COMMENT")).thenReturn("标题");
                    when(rs.getInt("ORDINAL_POSITION")).thenReturn(1);
                    return List.of(rowMapper.mapRow(rs, 0));
                });
        when(jdbcTemplate.queryForMap(
                argThat(sql -> sql.contains("FROM information_schema.TABLES")),
                eq("spider_result")))
                .thenReturn(Map.of(
                        "TABLE_NAME", "spider_result",
                        "TABLE_TYPE", "BASE TABLE",
                        "ENGINE", "InnoDB",
                        "TABLE_COLLATION", "utf8mb4_unicode_ci",
                        "TABLE_ROWS", BigInteger.ZERO,
                        "DATA_LENGTH", BigInteger.ZERO,
                        "INDEX_LENGTH", BigInteger.ZERO,
                        "TABLE_COMMENT", "comment"
                ));
        when(jdbcTemplate.queryForMap("SHOW CREATE TABLE `spider_result`"))
                .thenReturn(Map.of("Table", "spider_result", "Create Table", "CREATE TABLE `spider_result` (`title` varchar(128))"));

        TableStructureInfo info = service.getTableStructure("spider_result", null);

        assertThat(info.getColumns()).hasSize(1);
        assertThat(info.getColumns().getFirst().getLength()).isEqualTo(128);
        assertThat(info.getColumns().getFirst().getPrecision()).isEqualTo(18);
        assertThat(info.getColumns().getFirst().getScale()).isEqualTo(2);
        assertThat(info.getRowCount()).isZero();
        assertThat(info.getDataSize()).isZero();
        assertThat(info.getIndexSize()).isZero();
    }

    private SpiderOutputColumnDefinition column(
            String name,
            String type,
            boolean nullable,
            Integer length,
            boolean primaryKey,
            boolean autoIncrement,
            String defaultValue,
            String comment) {
        return column(name, type, nullable, length, primaryKey, autoIncrement, defaultValue, comment, null);
    }

    private SpiderOutputColumnDefinition column(
            String name,
            String type,
            boolean nullable,
            Integer length,
            boolean primaryKey,
            boolean autoIncrement,
            String defaultValue,
            String comment,
            Integer scale) {
        SpiderOutputColumnDefinition definition = new SpiderOutputColumnDefinition();
        definition.setName(name);
        definition.setType(type);
        definition.setNullable(nullable);
        definition.setLength(length);
        definition.setScale(scale);
        definition.setPrimaryKey(primaryKey);
        definition.setAutoIncrement(autoIncrement);
        definition.setDefaultValue(defaultValue);
        definition.setComment(comment);
        return definition;
    }
}
