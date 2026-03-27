package com.chua.starter.monitor.engine;

import com.chua.common.support.data.datasource.meta.ColumnMetadata;
import com.chua.common.support.data.datasource.meta.TableMetadata;
import com.chua.common.support.data.page.Page;
import com.chua.common.support.data.query.definition.query.Search;
import com.chua.common.support.metadata.command.CommandMetadata;
import com.chua.common.support.metadata.command.CommandResult;
import com.chua.common.support.metadata.command.ResultItem;
import com.chua.common.support.text.json.Json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 基于 CommandMetadata 的查询适配器。
 */
class CommandSearchSystemDataEngineAdapter<T> extends Search<T> {

    private final Class<T> targetType;
    private final CommandMetadata commandMetadata;
    private final SystemDataOrmDialect ormDialect;

    CommandSearchSystemDataEngineAdapter(Class<T> targetType,
                                         CommandMetadata commandMetadata,
                                         SystemDataOrmDialect ormDialect) {
        super(targetType);
        this.targetType = targetType;
        this.commandMetadata = commandMetadata;
        this.ormDialect = ormDialect;
    }

    @Override
    public List<T> search() {
        return search(targetType);
    }

    @Override
    public <E> List<E> search(Class<E> resultType) {
        CommandResult result = executeSearch();
        return mapRows(result.getData(), resultType);
    }

    @Override
    public Page<T> search(int pageNum, int pageSize) {
        return search(pageNum, pageSize, targetType);
    }

    @Override
    public <E> Page<E> search(int pageNum, int pageSize, Class<E> resultType) {
        List<E> rows = search(resultType);
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.max(pageSize, 1);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, rows.size());
        int toIndex = Math.min(fromIndex + safePageSize, rows.size());

        Page<E> page = new Page<>();
        page.setData(fromIndex >= toIndex ? Collections.emptyList() : rows.subList(fromIndex, toIndex));
        page.setPageNum(safePageNum);
        page.setPageSize(safePageSize);
        page.setTotal((long) rows.size());
        page.setPages(rows.isEmpty() ? 0 : (int) Math.ceil((double) rows.size() / safePageSize));
        return page;
    }

    @Override
    public Long count() {
        CommandResult result = executeSearch();
        if (result.getRowCount() > 0) {
            return result.getRowCount();
        }
        ResultItem data = result.getData();
        if (data == null || data.getRows() == null) {
            return 0L;
        }
        return (long) data.getRows().size();
    }

    @Override
    public T one() {
        List<T> rows = search();
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public T getById(Serializable id) {
        TableMetadata tableMetadata = TableMetadata.get(targetType);
        if (tableMetadata != null && tableMetadata.hasPrimaryKey()) {
            ColumnMetadata keyColumn = tableMetadata.getKeyColumn();
            if (keyColumn != null) {
                eq(keyColumn.getColumnName(), id);
            }
        }
        return one();
    }

    @Override
    public List<T> list() {
        return search();
    }

    @Override
    public <R> List<R> list(Class<R> resultType) {
        return search(resultType);
    }

    private CommandResult executeSearch() {
        try {
            String query = toQuery(ormDialect.getCommandType());
            CommandResult result = commandMetadata.executeCommand(ormDialect.getCommandType(), query, true);
            if (result == null) {
                throw new IllegalStateException("查询结果为空");
            }
            if (!result.isSuccess()) {
                throw new IllegalStateException(result.getErrorMessage());
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("执行 ORM 查询失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <E> List<E> mapRows(ResultItem resultItem, Class<E> resultType) {
        if (resultItem == null || resultItem.getRows() == null || resultItem.getRows().isEmpty()) {
            return Collections.emptyList();
        }
        List<E> result = new ArrayList<>(resultItem.getRows().size());
        for (Map<String, Object> row : resultItem.getRows()) {
            result.add((E) convertRow(row, resultType));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <E> E convertRow(Map<String, Object> row, Class<E> resultType) {
        if (row == null) {
            return null;
        }
        if (Map.class.isAssignableFrom(resultType) || resultType == Object.class) {
            return (E) row;
        }
        if (row.size() == 1 && isSimpleType(resultType)) {
            Object value = row.values().iterator().next();
            return convertSimpleValue(value, resultType);
        }
        return Json.fromJson(Json.toJson(row), resultType);
    }

    @SuppressWarnings("unchecked")
    private <E> E convertSimpleValue(Object value, Class<E> resultType) {
        if (value == null) {
            return null;
        }
        if (resultType.isInstance(value)) {
            return (E) value;
        }
        if (String.class == resultType) {
            return (E) String.valueOf(value);
        }
        return Json.fromJson(Json.toJson(value), resultType);
    }

    private boolean isSimpleType(Class<?> resultType) {
        return resultType.isPrimitive()
                || Number.class.isAssignableFrom(resultType)
                || CharSequence.class.isAssignableFrom(resultType)
                || Boolean.class == resultType
                || Character.class == resultType;
    }
}
