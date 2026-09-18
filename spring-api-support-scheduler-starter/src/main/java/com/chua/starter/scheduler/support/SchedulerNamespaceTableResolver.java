package com.chua.starter.scheduler.support;

import com.chua.common.support.lang.datasource.dialect.meta.ColumnMetadata;
import com.chua.common.support.task.scheduler.CronExpression;
import com.chua.starter.job.support.store.JobEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 调度平台命名空间表解析器。
 * <p>
 * 平台已迁移到 job-starter 新架构，任务统一存储在单表中，不再维护多命名空间
 * 物理表。本解析器保留原 API 形态，命名空间表名按 {@code 表前缀_命名空间}
 * 规则解析，default 命名空间对应单表名。
 * </p>
 *
 * @since 4.0.0
 * @author CH
 */
@Component
public class SchedulerNamespaceTableResolver {

    /**
     * 默认命名空间
     */
    private static final String DEFAULT_NAMESPACE = "default";

    /**
     * 默认任务表名
     */
    private static final String DEFAULT_JOB_TABLE = "sys_job";

    /**
     * 默认日志表名
     */
    private static final String DEFAULT_LOG_TABLE = "sys_job_log";

    /**
     * 预读时间窗（毫秒）
     */
    private static final long PRE_READ_MS = 5000L;

    /**
     * 解析当前启用的命名空间列表。
     *
     * @return 命名空间列表，未配置时返回默认命名空间
     */
    public String[] namespaces() {
        return new String[] {DEFAULT_NAMESPACE};
    }

    /**
     * 解析命名空间对应的任务表名。
     *
     * @param namespace 命名空间
     * @return 任务表名，空命名空间返回默认任务表名
     */
    public String resolveJobTable(String namespace) {
        if (!hasText(namespace) || DEFAULT_NAMESPACE.equalsIgnoreCase(namespace)) {
            return DEFAULT_JOB_TABLE;
        }
        return DEFAULT_JOB_TABLE + "_" + normalize(namespace);
    }

    /**
     * 解析命名空间对应的日志表名。
     *
     * @param namespace 命名空间
     * @return 日志表名，空命名空间返回默认日志表名
     */
    public String resolveLogTable(String namespace) {
        if (!hasText(namespace) || DEFAULT_NAMESPACE.equalsIgnoreCase(namespace)) {
            return DEFAULT_LOG_TABLE;
        }
        return DEFAULT_LOG_TABLE + "_" + normalize(namespace);
    }

    /**
     * 解析命名空间对应的任务/日志/日志明细/日志备份表名。
     *
     * @param namespace 命名空间
     * @return 解析后的命名空间表
     */
    public ResolvedNamespaceTables resolve(String namespace) {
        String tablePrefix = hasText(namespace) && !DEFAULT_NAMESPACE.equalsIgnoreCase(namespace)
                ? normalize(namespace) : null;
        String jobTable = tablePrefix == null ? DEFAULT_JOB_TABLE : DEFAULT_JOB_TABLE + "_" + tablePrefix;
        String logTable = tablePrefix == null ? DEFAULT_LOG_TABLE : DEFAULT_LOG_TABLE + "_" + tablePrefix;
        return new ResolvedNamespaceTables(jobTable, logTable,
                logTable + "_detail", logTable + "_backup");
    }

    /**
     * 计算任务下次有效触发时间。
     * <p>
     * 替代旧 job-starter 的 JobHelper.generateNextValidTime，按
     * {@link JobEntity#getSysJobScheduleType()} 区分 CRON 与间隔型调度。
     * </p>
     *
     * @param job  任务
     * @param from 计算起点时间
     * @return 下次有效触发时间，调度配置无效时返回 空
     */
    public static Date generateNextValidTime(JobEntity job, Date from) {
        if (job == null || !StringUtils.hasText(job.getSysJobScheduleTime())) {
            return null;
        }
        String scheduleType = job.getSysJobScheduleType() == null
                ? "CRON" : job.getSysJobScheduleType().trim().toUpperCase(Locale.ROOT);
        String scheduleTime = job.getSysJobScheduleTime().trim();
        if ("CRON".equals(scheduleType)) {
            try {
                return new CronExpression(scheduleTime).getNextValidTimeAfter(from);
            } catch (Exception e) {
                return null;
            }
        }
        if ("NONE".equals(scheduleType)) {
            return null;
        }
        try {
            long interval = Long.parseLong(scheduleTime);
            if (interval <= 0) {
                return null;
            }
            return new Date(from.getTime() + interval);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取预读时间窗（毫秒）。
     *
     * @return 预读时间窗
     */
    public static long preReadMs() {
        return PRE_READ_MS;
    }

    /**
     * 匹配错失触发策略。
     *
     * @param value     策略值
     * @param fallback  缺省策略
     * @return 匹配后的策略
     */
    public static String matchMisfireStrategy(String value, String fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("DO_NOTHING".equals(normalized) || "FIRE_ONCE_NOW".equals(normalized)) {
            return normalized;
        }
        return fallback;
    }

    /**
     * 从实体类型推导默认列元数据。
     * <p>
     * 替代旧版 {@code com.chua.datasource.support.data.datasource.meta.TableMetadata#of(Class)}。
     * </p>
     *
     * @param entityType 实体类型
     * @return 列元数据列表
     */
    public static List<ColumnMetadata> defaultColumns(Class<?> entityType) {
        List<ColumnMetadata> columns = new ArrayList<>();
        Class<?> current = entityType;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                ColumnMetadata column = new ColumnMetadata();
                column.setColumnName(toSnakeCase(field.getName()));
                column.setJdbcType(mapJdbcType(field.getType()));
                if ("id".equals(field.getName()) || "sysJobId".equals(field.getName())
                        || "sysJobLogId".equals(field.getName())) {
                    column.setPrimaryKey(true);
                    column.setAutoIncrement(true);
                }
                columns.add(column);
            }
            current = current.getSuperclass();
        }
        return columns;
    }

    /**
     * 归一化命名空间表名后缀。
     *
     * @param namespace 命名空间
     * @return 归一化后缀
     */
    private String normalize(String namespace) {
        return namespace.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    /**
     * 判断文本是否有内容。
     *
     * @param value 文本
     * @return 有内容返回 true
     */
    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 驼峰转下划线。
     *
     * @param name 字段名
     * @return 下划线形式
     */
    private static String toSnakeCase(String name) {
        StringBuilder sb = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.isUpperCase(c) && sb.length() > 0) {
                sb.append('_');
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    /**
     * 映射 JDBC 类型名。
     *
     * @param type 字段类型
     * @return JDBC 类型名
     */
    private static String mapJdbcType(Class<?> type) {
        if (type == Long.class || type == long.class) {
            return "BIGINT";
        }
        if (type == Integer.class || type == int.class) {
            return "INT";
        }
        if (type == Boolean.class || type == boolean.class) {
            return "TINYINT";
        }
        if (type == BigDecimal.class) {
            return "DECIMAL(10,2)";
        }
        if (type == Date.class || type == LocalDateTime.class) {
            return "DATETIME";
        }
        return "VARCHAR(255)";
    }

    /**
     * 命名空间表解析结果。
     *
     * @param jobTable       任务表
     * @param logTable       日志表
     * @param logDetailTable 日志明细表
     * @param logBackupTable 日志备份表
     * @since 4.0.0
     * @author CH
     */
    public record ResolvedNamespaceTables(String jobTable,
                                          String logTable,
                                          String logDetailTable,
                                          String logBackupTable) {
    }
}
