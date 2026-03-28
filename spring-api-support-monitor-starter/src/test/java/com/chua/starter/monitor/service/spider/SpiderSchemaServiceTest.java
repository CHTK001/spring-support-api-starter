package com.chua.starter.monitor.service.spider;

import com.chua.starter.monitor.pojo.spider.SpiderTableStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpiderSchemaServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Spy
    @InjectMocks
    private SpiderSchemaService service;

    @Test
    void checkTableStatusMarksMissingTablesAndColumns() {
        Map<String, List<String>> requiredColumns = requiredColumns();

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString()))
                .thenAnswer(invocation -> switch (invocation.getArgument(2, String.class)) {
                    case "monitor_spider_task", "monitor_spider_data", "monitor_spider_node" -> 1;
                    default -> 0;
                });
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), anyString()))
                .thenAnswer(invocation -> {
                    String tableName = invocation.getArgument(2, String.class);
                    List<String> columns = new ArrayList<>(requiredColumns.get(tableName));
                    if ("monitor_spider_data".equals(tableName)) {
                        columns.remove("spider_data_hash");
                    }
                    return columns;
                });

        SpiderTableStatus status = service.checkTableStatus();

        assertThat(status.getReady()).isFalse();
        assertThat(status.getTables()).containsEntry("monitor_spider_task", true);
        assertThat(status.getTables()).containsEntry("monitor_spider_log", false);
        assertThat(status.getMissingTables()).containsExactly("monitor_spider_log", "monitor_spider_connection");
        assertThat(status.getMissingColumns().get("monitor_spider_data")).containsExactly("spider_data_hash");
        assertThat(status.getMissingColumns().get("monitor_spider_log"))
                .containsAll(requiredColumns.get("monitor_spider_log"));
    }

    @Test
    void checkTableStatusReturnsReadyWhenAllRequiredTablesAndColumnsExist() {
        Map<String, List<String>> requiredColumns = requiredColumns();

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString())).thenReturn(1);
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), anyString()))
                .thenAnswer(invocation -> new ArrayList<>(requiredColumns.get(invocation.getArgument(2, String.class))));

        SpiderTableStatus status = service.checkTableStatus();

        assertThat(status.getReady()).isTrue();
        assertThat(status.getMissingTables()).isEmpty();
        assertThat(status.getMissingColumns()).isEmpty();
    }

    @Test
    void initializeTablesBackfillsLegacyMissingColumns() {
        Map<String, List<String>> requiredColumns = requiredColumns();
        Map<String, Set<String>> missingColumns = Map.of(
                "monitor_spider_task", Set.of(
                        "spider_task_max_depth",
                        "spider_task_enable_url_discover",
                        "spider_task_page_pattern",
                        "spider_task_max_pages",
                        "spider_task_incremental_mode",
                        "spider_task_rate_limit"
                ),
                "monitor_spider_data", Set.of("create_time"),
                "monitor_spider_log", Set.of("create_time")
        );
        Set<String> addedColumns = new HashSet<>();

        doNothing().when(service).executeSchemaScript();
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString())).thenReturn(1);
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), anyString()))
                .thenAnswer(invocation -> {
                    String tableName = invocation.getArgument(2, String.class);
                    List<String> columns = new ArrayList<>(requiredColumns.get(tableName));
                    Set<String> missing = missingColumns.get(tableName);
                    if (missing != null) {
                        columns.removeIf(column -> missing.contains(column)
                                && !addedColumns.contains(tableName + "." + column));
                    }
                    return columns;
                });
        doAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            Matcher matcher = Pattern.compile("ALTER TABLE `([^`]+)` ADD COLUMN `([^`]+)`").matcher(sql);
            assertThat(matcher.find()).isTrue();
            addedColumns.add(matcher.group(1) + "." + matcher.group(2));
            return null;
        }).when(jdbcTemplate).execute(anyString());

        boolean ready = service.initializeTables(false);

        assertThat(ready).isTrue();
        assertThat(addedColumns).containsExactlyInAnyOrder(
                "monitor_spider_task.spider_task_max_depth",
                "monitor_spider_task.spider_task_enable_url_discover",
                "monitor_spider_task.spider_task_page_pattern",
                "monitor_spider_task.spider_task_max_pages",
                "monitor_spider_task.spider_task_incremental_mode",
                "monitor_spider_task.spider_task_rate_limit",
                "monitor_spider_data.create_time",
                "monitor_spider_log.create_time"
        );
        verify(jdbcTemplate, times(8)).execute(anyString());
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> requiredColumns() {
        try {
            Field field = SpiderSchemaService.class.getDeclaredField("REQUIRED_COLUMNS");
            field.setAccessible(true);
            return (Map<String, List<String>>) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("读取 SpiderSchemaService.REQUIRED_COLUMNS 失败", e);
        }
    }
}
