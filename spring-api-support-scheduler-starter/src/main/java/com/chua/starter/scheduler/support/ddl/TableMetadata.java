package com.chua.starter.scheduler.support.ddl;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 表结构元数据。
 *
 * @author CH
 */
public class TableMetadata {

    /**
     * 表名
     */
    private String name;

    /**
     * 列元数据列表
     */
    private List<ColumnMetadata> columnMetadata = new ArrayList<>();

    /**
     * 根据实体类生成表元数据。
     *
     * @param entityType 实体类型
     * @return 表元数据
     */
    public static TableMetadata of(Class<?> entityType) {
        TableMetadata metadata = new TableMetadata();
        metadata.setName(entityType.getSimpleName().toLowerCase());
        collectColumns(entityType, metadata.columnMetadata);
        return metadata;
    }

    /**
     * 递归收集实体的列信息。
     *
     * @param entityType 实体类型
     * @param columns    列元数据列表
     */
    private static void collectColumns(Class<?> entityType, List<ColumnMetadata> columns) {
        if (entityType == null || entityType == Object.class) {
            return;
        }
        ReflectionUtils.doWithFields(entityType, field -> {
            if (Modifier.isStatic(field.getModifiers())) {
                return;
            }
            columns.add(new ColumnMetadata(field.getName()));
        }, field -> !Modifier.isStatic(field.getModifiers()));
        collectColumns(entityType.getSuperclass(), columns);
    }

    /**
     * 获取表名
     *
     * @return 表名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置表名
     *
     * @param name 表名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取列元数据列表
     *
     * @return 列元数据列表
     */
    public List<ColumnMetadata> getColumnMetadata() {
        return columnMetadata;
    }

    /**
     * 设置列元数据列表
     *
     * @param columnMetadata 列元数据列表
     */
    public void setColumnMetadata(List<ColumnMetadata> columnMetadata) {
        this.columnMetadata = columnMetadata;
    }
}
