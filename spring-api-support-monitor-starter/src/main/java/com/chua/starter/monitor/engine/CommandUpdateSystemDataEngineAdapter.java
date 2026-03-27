package com.chua.starter.monitor.engine;

import com.chua.common.support.data.datasource.meta.TableMetadata;
import com.chua.common.support.data.orm.toolkit.SqlHelper;
import com.chua.common.support.data.query.definition.query.Update;
import com.chua.common.support.data.query.expression.selector.InsertSelector;
import com.chua.common.support.data.query.expression.selector.UpdateSelector;
import com.chua.common.support.metadata.command.CommandMetadata;
import com.chua.common.support.metadata.command.CommandResult;

import java.io.Serializable;
import java.util.List;

/**
 * 基于 CommandMetadata 的更新适配器。
 */
class CommandUpdateSystemDataEngineAdapter<T> extends Update<T> {

    private static final long serialVersionUID = 1L;

    private final transient CommandMetadata commandMetadata;
    private final SystemDataOrmDialect ormDialect;
    private final transient TableMetadata tableMetadata;

    CommandUpdateSystemDataEngineAdapter(Class<T> targetType,
                                         CommandMetadata commandMetadata,
                                         SystemDataOrmDialect ormDialect) {
        super(targetType);
        this.commandMetadata = commandMetadata;
        this.ormDialect = ormDialect;
        this.tableMetadata = TableMetadata.get(targetType);
    }

    @Override
    protected int doUpdate() {
        return doUpdate(this);
    }

    @Override
    protected int doUpdate(Update<T> update) {
        try {
            String query = update.toQuery(ormDialect.getCommandType());
            CommandResult result = commandMetadata.executeCommand(ormDialect.getCommandType(), query, true);
            if (result == null) {
                throw new IllegalStateException("更新结果为空");
            }
            if (!result.isSuccess()) {
                throw new IllegalStateException(result.getErrorMessage());
            }
            if (result.getAffectedRows() > 0) {
                return (int) result.getAffectedRows();
            }
            if (result.getRowCount() > 0) {
                return (int) result.getRowCount();
            }
            return 1;
        } catch (Exception e) {
            throw new RuntimeException("执行 ORM 更新失败", e);
        } finally {
            clear();
        }
    }

    @Override
    public int save(T entity) {
        setSelector(new InsertSelector<>(this));
        SqlHelper.save(this, entity, false);
        return doUpdate(this);
    }

    @Override
    public int save() {
        setSelector(new InsertSelector<>(this));
        return doUpdate(this);
    }

    @Override
    public int update(T entity) {
        setSelector(new UpdateSelector<>(this));
        SqlHelper.update(this, entity, false);
        return doUpdate(this);
    }

    @Override
    public int updateById(T entity) {
        setSelector(new UpdateSelector<>(this));
        SqlHelper.update(this, entity, false, true);
        return doUpdate(this);
    }

    @Override
    public int deleteById(Serializable id) {
        if (tableMetadata == null || !tableMetadata.hasPrimaryKey() || tableMetadata.getKeyColumn() == null) {
            return 0;
        }
        eq(tableMetadata.getKeyColumn().getColumnName(), id);
        return doUpdate(this);
    }

    @Override
    public int delete() {
        return doUpdate(this);
    }

    @Override
    public int batchUpdate(List<T> entityList) {
        int count = 0;
        if (entityList == null) {
            return count;
        }
        for (T entity : entityList) {
            count += update(entity);
        }
        return count;
    }

    @Override
    public int batchSave(List<T> entityList) {
        int count = 0;
        if (entityList == null) {
            return count;
        }
        for (T entity : entityList) {
            count += save(entity);
        }
        return count;
    }

    @Override
    public int saveOrUpdate(T entity) {
        if (tableMetadata != null && tableMetadata.hasPkValue(entity)) {
            return updateById(entity);
        }
        return save(entity);
    }

    @Override
    public boolean saveOrUpdateBatch(List<T> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return false;
        }
        int count = 0;
        for (T entity : entityList) {
            count += saveOrUpdate(entity);
        }
        return count > 0;
    }
}
