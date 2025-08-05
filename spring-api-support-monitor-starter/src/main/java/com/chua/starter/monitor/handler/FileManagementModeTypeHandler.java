package com.chua.starter.monitor.handler;

import com.chua.starter.monitor.enums.FileManagementMode;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * FileManagementMode 枚举类型处理器 用于 MyBatis 中 FileManagementMode 枚举与数据库字符串之间的转换
 * 支持新旧格式的兼容性处理
 * <p>
 * 注意：由于当前已通过 SQL 级别的 CASE 语句解决了兼容性问题， 此 TypeHandler 作为备用方案保留，暂时不启用。
 *
 * @author CH
 * @since 2024/12/13
 */
public class FileManagementModeTypeHandler {

    /**
     * 设置参数到 PreparedStatement 将枚举值转换为字符串存储到数据库
     *
     * @param ps        PreparedStatement
     * @param i         参数索引
     * @param parameter 枚举参数
     * @throws SQLException SQL异常
     */
    public void setNonNullParameter(PreparedStatement ps, int i, FileManagementMode parameter) throws SQLException {
        ps.setString(i, parameter.getCode());
    }

    /**
     * 从 ResultSet 获取结果 将数据库中的字符串或数字转换为枚举值
     *
     * @param rs         ResultSet
     * @param columnName 列名
     * @return 枚举值
     * @throws SQLException SQL异常
     */
    public FileManagementMode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        return convertToEnum(value);
    }

    /**
     * 从 ResultSet 获取结果（通过列索引） 将数据库中的字符串或数字转换为枚举值
     *
     * @param rs          ResultSet
     * @param columnIndex 列索引
     * @return 枚举值
     * @throws SQLException SQL异常
     */
    public FileManagementMode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object value = rs.getObject(columnIndex);
        return convertToEnum(value);
    }

    /**
     * 从 CallableStatement 获取结果 将数据库中的字符串或数字转换为枚举值
     *
     * @param cs          CallableStatement
     * @param columnIndex 列索引
     * @return 枚举值
     * @throws SQLException SQL异常
     */
    public FileManagementMode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object value = cs.getObject(columnIndex);
        return convertToEnum(value);
    }

    /**
     * 将数据库值转换为枚举值 支持字符串和数字格式的兼容性处理
     */
    private FileManagementMode convertToEnum(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return FileManagementMode.fromCode((String) value);
        } else if (value instanceof Number) {
            return FileManagementMode.fromLegacyCode(((Number) value).intValue());
        } else {
            // 尝试转换为字符串处理
            return FileManagementMode.fromCode(value.toString());
        }
    }
}
