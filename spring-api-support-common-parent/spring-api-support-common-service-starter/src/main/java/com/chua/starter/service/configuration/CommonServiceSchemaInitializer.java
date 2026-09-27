package com.chua.starter.service.configuration;

import com.chua.common.support.lang.datasource.dialect.SqlName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * common-service 自有业务表结构自愈初始化器
 *
 * <p>背景：本模块（而非 common-system）拥有 {@code sys_log} / {@code sys_setting} 的建表语句，
 * 但早期版本的建表脚本与实体演进不同步：实体与 MyBatis-Plus 生成的 SQL 已经携带了若干新字段，
 * 建表脚本却漏建。存量库上这些字段一旦被查询或写入，H2 与 MySQL 都会直接抛
 * {@code Column "..." not found}，使整条接口 500，而不是只缺一个展示字段。</p>
 *
 * <p>典型两处：{@code sys_log} 缺客户端指纹三列（{@code sys_log_system} /
 * {@code sys_log_ua} / {@code sys_log_fingerprint}），{@code sys_setting} 缺
 * {@code sys_setting_default_value}（与当前值分离、用于“恢复默认”）。</p>
 *
 * <p>建表脚本 {@code V1.0__init_all_api_common_service.sql} 已同步补齐，因此全新库不会进入
 * 补列分支；本类只服务于存量库与历史快照。</p>
 *
 * <p>安全豁免：表名与列名均为本类常量，并经 {@link SqlName#checkSimple(String, String)}
 * 白名单校验；列类型为固定字面量，不接受任何请求参数或外部输入。单个列补齐失败只记录
 * 警告，不阻断应用启动。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommonServiceSchemaInitializer implements ApplicationRunner, Ordered {

    /**
     * 实体已使用但历史建表脚本可能缺失的列
     *
     * <p>外层键为小写表名，内层键为小写列名，值为列类型定义；类型与建表脚本保持一致，
     * 避免两条路径产生类型漂移。</p>
     */
    private static final Map<String, Map<String, String>> NEEDED_COLUMNS = Map.of(
            "sys_log", Map.of(
                    "sys_log_system", "varchar(255) NULL DEFAULT NULL",
                    "sys_log_ua", "varchar(512) NULL DEFAULT NULL",
                    "sys_log_fingerprint", "varchar(64) NULL DEFAULT NULL"),
            "sys_setting", Map.of(
                    "sys_setting_default_value", "varchar(255) NULL DEFAULT NULL"));

    /**
     * 数据源
     */
    private final DataSource dataSource;

    /**
     * 必须排在所有其它 {@link ApplicationRunner} 之前。
     *
     * <p>本类是补列的唯一兜底。{@code ApplicationRunner} 默认无序，
     * 一旦有别的 runner（如 {@code UserAgreementSettingInitializer}）
     * 先查 {@code sys_setting}，就会在补列之前撞上
     * {@code Unknown column 'sys_setting_default_value'}，
     * 异常冒泡直接终止启动 —— 表现为「同一份产物，有时能起有时起不来」。</p>
     *
     * @return 最高优先级
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            for (Map.Entry<String, Map<String, String>> table : NEEDED_COLUMNS.entrySet()) {
                heal(connection, table.getKey(), table.getValue());
            }
        } catch (Exception e) {
            log.warn("[表结构自愈] 自愈检查失败（非致命）: {}", e.getMessage());
        }
    }

    /**
     * 对单张表执行补列
     *
     * @param connection 数据库连接
     * @param table      表名（小写）
     * @param columns    期望存在的列及其类型定义
     */
    private void heal(Connection connection, String table, Map<String, String> columns) {
        try {
            if (!tableExists(connection, table)) {
                log.debug("[表结构自愈] 表 {} 尚不存在，跳过补列（交由初始化脚本建表）", table);
                return;
            }
            int added = addMissingColumns(connection, table, columns);
            if (added > 0) {
                log.info("[表结构自愈] 表 {} 结构补列完成，共补齐 {} 列", table, added);
            } else {
                log.debug("[表结构自愈] 表 {} 结构完整，无需补列", table);
            }
        } catch (Exception e) {
            log.warn("[表结构自愈] 表 {} 自愈失败（非致命）: {}", table, e.getMessage());
        }
    }

    /**
     * 补齐缺失列，返回实际补齐的列数
     *
     * @param connection 数据库连接
     * @param table      表名（小写）
     * @param columns    期望存在的列及其类型定义
     * @return 补齐的列数
     * @throws SQLException 读取列元数据失败时抛出
     */
    private int addMissingColumns(Connection connection, String table, Map<String, String> columns) throws SQLException {
        List<String> existing = columnNames(connection, table);
        int added = 0;
        for (Map.Entry<String, String> entry : columns.entrySet()) {
            if (existing.contains(entry.getKey())) {
                continue;
            }
            String column = SqlName.checkSimple(entry.getKey(), "自愈列名");
            String alter = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + entry.getValue();
            try (Statement statement = connection.createStatement()) {
                statement.execute(alter);
                added++;
                log.info("[表结构自愈] 表 {} 已补齐缺失列 {}", table, entry.getKey());
            } catch (SQLException e) {
                log.warn("[表结构自愈] 表 {} 补齐列 {} 失败: {}", table, entry.getKey(), e.getMessage());
            }
        }
        return added;
    }

    /**
     * 判断表是否存在
     *
     * @param connection 数据库连接
     * @param table      表名（小写）
     * @return 表存在返回 true
     */
    private boolean tableExists(Connection connection, String table) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();
            for (String candidate : nameCandidates(table)) {
                try (ResultSet resultSet = metaData.getTables(catalog, schema, candidate, new String[]{"TABLE"})) {
                    if (resultSet.next()) {
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            log.warn("[表结构自愈] 读取表 {} 清单失败: {}", table, e.getMessage());
            return false;
        }
    }

    /**
     * 读取表的列名（小写），按大小写候选逐一尝试以兼容不同数据库的元数据行为
     *
     * @param connection 数据库连接
     * @param table      表名（小写）
     * @return 列名列表（小写）
     * @throws SQLException 读取列元数据失败时抛出
     */
    private List<String> columnNames(Connection connection, String table) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        String schema = connection.getSchema();
        List<String> columns = new ArrayList<>();
        for (String candidate : nameCandidates(table)) {
            try (ResultSet resultSet = metaData.getColumns(catalog, schema, candidate, null)) {
                while (resultSet.next()) {
                    columns.add(resultSet.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
                }
            }
            if (!columns.isEmpty()) {
                return columns;
            }
        }
        return columns;
    }

    /**
     * 标识符大小写候选，兼容 H2（默认大写）与 MySQL（默认小写）的元数据差异
     *
     * @param table 表名（小写）
     * @return 表名候选列表
     */
    private List<String> nameCandidates(String table) {
        return List.of(table, table.toUpperCase(Locale.ROOT), table.toLowerCase(Locale.ROOT));
    }
}
