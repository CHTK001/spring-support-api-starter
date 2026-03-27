package com.chua.starter.scheduler.support;

import com.chua.starter.job.support.JobProperties;
import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * 调度平台命名空间表解析器。
 * <p>
 * 平台管理侧不再依赖 ThreadLocal 切表，而是在进入业务逻辑前把 namespace
 * 解析成一组确定的物理表名，后续 JDBC 访问直接指向这些表。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class SchedulerNamespaceTableResolver {

    private static final String DEFAULT_NAMESPACE = "default";
    private static final Pattern SAFE_TABLE_NAME = Pattern.compile("[A-Za-z0-9_]+");

    private final SchedulerJobPlatformProperties platformProperties;
    private final JobProperties jobProperties;

    /**
     * 返回当前可管理的命名空间列表。
     */
    public List<String> namespaces() {
        TreeSet<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        names.add(DEFAULT_NAMESPACE);
        if (platformProperties.getNamespaces() != null) {
            platformProperties.getNamespaces().keySet().stream()
                    .map(this::trimToNull)
                    .filter(StringUtils::hasText)
                    .forEach(names::add);
        }
        return new ArrayList<>(names);
    }

    /**
     * 将 namespace 解析为物理表集合。
     */
    public ResolvedNamespaceTables resolve(String namespace) {
        String normalizedNamespace = trimToNull(namespace);
        if (!StringUtils.hasText(normalizedNamespace) || DEFAULT_NAMESPACE.equalsIgnoreCase(normalizedNamespace)) {
            return new ResolvedNamespaceTables(DEFAULT_NAMESPACE, sanitize(resolveDefaultTable()));
        }

        Map.Entry<String, SchedulerJobPlatformProperties.Namespace> entry = findNamespaceEntry(normalizedNamespace);
        if (entry == null) {
            throw new IllegalArgumentException("未知任务命名空间: " + normalizedNamespace);
        }

        JobProperties.Table resolved = resolveNamespaceTable(entry.getKey(), entry.getValue());
        return new ResolvedNamespaceTables(entry.getKey(), sanitize(resolved));
    }

    private JobProperties.Table resolveDefaultTable() {
        Map.Entry<String, SchedulerJobPlatformProperties.Namespace> defaultEntry = findNamespaceEntry(DEFAULT_NAMESPACE);
        if (defaultEntry == null || !hasConfiguredTableValue(defaultEntry.getValue())) {
            return copyResolved(jobProperties.getTable());
        }
        return resolveNamespaceTable(defaultEntry.getKey(), defaultEntry.getValue());
    }

    private Map.Entry<String, SchedulerJobPlatformProperties.Namespace> findNamespaceEntry(String namespace) {
        Map<String, SchedulerJobPlatformProperties.Namespace> namespaces = platformProperties.getNamespaces();
        if (namespaces == null || namespaces.isEmpty()) {
            return null;
        }
        return namespaces.entrySet().stream()
                .filter(item -> namespace.equalsIgnoreCase(item.getKey()))
                .findFirst()
                .orElse(null);
    }

    private JobProperties.Table resolveNamespaceTable(String namespace, JobProperties.Table configured) {
        if (!hasConfiguredTableValue(configured)) {
            return tableFromPrefix(namespace);
        }
        return copyResolved(configured);
    }

    private JobProperties.Table tableFromPrefix(String namespace) {
        JobProperties.Table table = new JobProperties.Table();
        if (!setPrefixIfSupported(table, namespace)) {
            table.setJob(namespace + "_sys_job");
            table.setLog(namespace + "_sys_job_log");
            table.setLogDetail(namespace + "_sys_job_log_detail");
            table.setLogBackup(namespace + "_sys_job_log_backup");
        }
        return copyResolved(table);
    }

    private boolean hasConfiguredTableValue(JobProperties.Table table) {
        if (table == null) {
            return false;
        }
        return StringUtils.hasText(readTextField(table, "prefix"))
                || StringUtils.hasText(readTextField(table, "job"))
                || StringUtils.hasText(readTextField(table, "log"))
                || StringUtils.hasText(readTextField(table, "logDetail"))
                || StringUtils.hasText(readTextField(table, "logBackup"));
    }

    private String readTextField(JobProperties.Table table, String fieldName) {
        try {
            Field field = table.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(table);
            return value instanceof String text && StringUtils.hasText(text) ? text.trim() : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private JobProperties.Table copyResolved(JobProperties.Table source) {
        JobProperties.Table target = new JobProperties.Table();
        if (source == null) {
            return target;
        }
        JobProperties.Table resolved = invokeResolvedCopy(source);
        if (resolved != null) {
            return resolved;
        }
        target.setJob(source.getJob());
        target.setLog(source.getLog());
        target.setLogDetail(source.getLogDetail());
        target.setLogBackup(source.getLogBackup());
        copyPrefixIfSupported(source, target);
        return target;
    }

    private JobProperties.Table invokeResolvedCopy(JobProperties.Table source) {
        try {
            Method method = source.getClass().getMethod("resolvedCopy");
            Object result = method.invoke(source);
            if (result instanceof JobProperties.Table table) {
                return table;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }

    private void copyPrefixIfSupported(JobProperties.Table source, JobProperties.Table target) {
        try {
            Method getter = source.getClass().getMethod("getPrefix");
            Method setter = target.getClass().getMethod("setPrefix", String.class);
            Object prefix = getter.invoke(source);
            if (prefix instanceof String text && StringUtils.hasText(text)) {
                setter.invoke(target, text);
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private boolean setPrefixIfSupported(JobProperties.Table target, String prefix) {
        try {
            Method setter = target.getClass().getMethod("setPrefix", String.class);
            setter.invoke(target, prefix);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private JobProperties.Table sanitize(JobProperties.Table source) {
        JobProperties.Table target = new JobProperties.Table();
        target.setJob(requireSafeTableName(source.getJob(), "任务表"));
        target.setLog(requireSafeTableName(source.getLog(), "任务日志表"));
        target.setLogDetail(requireSafeTableName(source.getLogDetail(), "任务日志详情表"));
        target.setLogBackup(requireSafeTableName(source.getLogBackup(), "任务日志备份表"));
        copyPrefixIfSupported(source, target);
        return target;
    }

    private String requireSafeTableName(String tableName, String label) {
        String normalized = trimToNull(tableName);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        if (!SAFE_TABLE_NAME.matcher(normalized).matches()) {
            throw new IllegalArgumentException(label + "非法: " + normalized);
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 已解析完成的一组物理表。
     */
    public record ResolvedNamespaceTables(String namespace, JobProperties.Table table) {

        public String jobTable() {
            return table.getJob();
        }

        public String logTable() {
            return table.getLog();
        }

        public String logDetailTable() {
            return table.getLogDetail();
        }

        public String logBackupTable() {
            return table.getLogBackup();
        }
    }
}
