package com.chua.starter.scheduler.support.ddl;

/**
 * 列元数据。
 *
 * @author CH
 */
public class ColumnMetadata {

    /**
     * 列名
     */
    private String columnName;

    /**
     * 构造器。
     *
     * @param columnName 列名
     */
    public ColumnMetadata(String columnName) {
        this.columnName = columnName;
    }

    /**
     * 获取列名
     *
     * @return 列名
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * 设置列名
     *
     * @param columnName 列名
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}
