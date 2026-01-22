package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.data.datasource.dialect.Dialect;
import com.chua.common.support.data.datasource.dialect.DialectFactory;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadata;
import com.chua.common.support.metadata.hierarchy.HierarchyNode;
import com.chua.common.support.metadata.hierarchy.HierarchyResult;
import com.chua.common.support.metadata.query.MetadataQuery;
import com.chua.common.support.core.utils.MapUtils;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.monitor.entity.SystemDataFieldComment;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.mapper.SystemDataFieldCommentMapper;
import com.chua.starter.monitor.mapper.SystemDataSettingMapper;
import com.chua.starter.monitor.service.data.SystemDataConsoleService;
import com.chua.starter.monitor.service.data.SystemDataFieldCommentService;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 字段注释 Service 实现
 * <p>
 * 提供基于 settingId + nodePath 的注释查询与新增/更新能力。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemDataFieldCommentServiceImpl
        extends ServiceImpl<SystemDataFieldCommentMapper, SystemDataFieldComment>
        implements SystemDataFieldCommentService {

    final SystemDataConsoleService systemDataConsoleService;
    private final SystemDataSettingMapper systemDataSettingMapper;

    @Override
    public ReturnResult<Boolean> saveOrUpdateComment(Integer settingId, String nodePath, String comment,
                                                     String databaseName, String tableName, String columnName) {
        SystemDataSetting setting = systemDataSettingMapper.selectById(settingId);
        if (setting == null) {
            return ReturnResult.error("配置不存在");
        }
        SystemDataFieldComment one = getOne(Wrappers.<SystemDataFieldComment>lambdaQuery()
                .eq(SystemDataFieldComment::getSystemDataFieldCommentSettingId, settingId)
                .eq(SystemDataFieldComment::getSystemDataFieldCommentNodePath, nodePath));

        List<String> strings = Splitter.on("/").trimResults().omitEmptyStrings().splitToList(nodePath);
        if (one == null) {
            one = new SystemDataFieldComment();
            one.setSystemDataFieldCommentSettingId(settingId);
            one.setSystemDataFieldCommentNodePath(nodePath);
        }
        one.setSystemDataFieldCommentComment(comment);
        one.setSystemDataFieldCommentDatabaseName(databaseName);
        one.setSystemDataFieldCommentTableName(tableName);
        one.setSystemDataFieldCommentColumnName(columnName);
        if (!strings.isEmpty() && StringUtils.isEmpty(databaseName)) {
            databaseName = strings.getFirst();
            one.setSystemDataFieldCommentDatabaseName(databaseName);
        }

        if (strings.size() > 1 && StringUtils.isEmpty(tableName)) {
            tableName = strings.get(1);
            one.setSystemDataFieldCommentTableName(tableName);
        }

        if (strings.size() > 2 && StringUtils.isEmpty(columnName)) {
            columnName = strings.getLast();
            one.setSystemDataFieldCommentColumnName(columnName);
        }

        boolean b = saveOrUpdate(one);
        // DatabaseType 类已移除，暂时跳过类型检查
        // DatabaseType databaseType = EnumUtils.getEnum(DatabaseType.class, setting.getSystemDataSettingProtocol());
        // if (null == databaseType) {
        //     return ReturnResult.ok(true);
        // }
        if (b && strings.size() > 2) {
            try {
                HierarchyMetadata orCreateHierarchy = systemDataConsoleService.getOrCreateHierarchy(settingId);
                HierarchyResult<HierarchyNode> childNodes = orCreateHierarchy.getChildNodes("/" + databaseName + "/" + tableName, MetadataQuery.create());
                if (childNodes.isEmpty()) {
                    return ReturnResult.ok(true);
                }
                String finalColumnName = columnName;
                Optional<HierarchyNode> first = childNodes.getData().stream().filter(item -> item.getName().equals(finalColumnName)).findFirst();
                if (!first.isPresent()) {
                    return ReturnResult.ok(true);
                }
                HierarchyNode hierarchyNode = first.get();
                // 生成并执行 ALTER COLUMN SQL
                String alterSql = generateAlterColumnSql(setting, tableName, columnName, hierarchyNode, comment);
                if (StringUtils.isNotBlank(alterSql)) {
                    systemDataConsoleService.execute(settingId, "sql", alterSql, MetadataQuery.create());
                }
            } catch (Exception e) {
                log.warn("[字段注释][生成SQL] 生成 ALTER COLUMN SQL 失败: {}", e.getMessage());
            }
        }
        return ReturnResult.ok(true);
    }

    /**
     * 生成 ALTER COLUMN SQL 语句
     *
     * @param setting       数据源配置
     * @param tableName     表名
     * @param columnName    列名
     * @param hierarchyNode 层级节点（包含列信息）
     * @param comment       注释内容
     * @return ALTER COLUMN SQL 语句，如果无法生成则返回 null
     */
    private String generateAlterColumnSql(SystemDataSetting setting, String tableName, String columnName,
                                          HierarchyNode hierarchyNode, String comment) {
        if (StringUtils.isBlank(setting.getSystemDataSettingProtocol())) {
            return null;
        }

        try {
            // 获取数据库方言
            var protocol = setting.getSystemDataSettingProtocol();
            var url = setting.getSystemDataSettingServer();
            var driver = setting.getSystemDataSettingDriverClass();

            Dialect dialect = null;
            if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(driver)) {
                dialect = DialectFactory.createDialect(url, driver);
            } else if (StringUtils.isNotBlank(protocol)) {
                // 尝试通过协议获取方言
                try {
                    dialect = DialectFactory.createDialect(protocol, null);
                } catch (Exception e) {
                    log.debug("[字段注释][生成SQL] 无法通过协议获取方言: {}", protocol);
                }
            }

            if (dialect == null) {
                return null;
            }

            // 获取列信息
            var columnType = MapUtils.getString(hierarchyNode.getProperties(), "columnType");
            var columnSize = MapUtils.getString(hierarchyNode.getProperties(), "columnSize");
            var nullable = MapUtils.getBoolean(hierarchyNode.getProperties(), "nullable", true);

            // 构建数据类型
            var dataType = columnType;
            if (StringUtils.isNotBlank(columnSize)) {
                dataType = columnType + "(" + columnSize + ")";
            }

            // 根据方言类型生成 SQL
            var protocolName = dialect.protocol().toLowerCase();
            return switch (protocolName) {
                case "mysql", "mariadb", "tidb", "tdsql", "polardb", "oceanbase" -> {
                    // MySQL 系列: ALTER TABLE table_name MODIFY COLUMN column_name data_type COMMENT 'comment'
                    var nullableStr = nullable ? "" : " NOT NULL";
                    yield String.format("ALTER TABLE `%s` MODIFY COLUMN `%s` %s%s COMMENT '%s'",
                            tableName, columnName, dataType, nullableStr, escapeSqlString(comment));
                }
                case "postgresql", "oscar", "kingbase", "gaussdb" -> {
                    // PostgreSQL 系列: COMMENT ON COLUMN table_name.column_name IS 'comment'
                    yield String.format("COMMENT ON COLUMN %s.%s IS '%s'",
                            tableName, columnName, escapeSqlString(comment));
                }
                case "oracle", "dm" -> {
                    // Oracle/DM: COMMENT ON COLUMN table_name.column_name IS 'comment'
                    yield String.format("COMMENT ON COLUMN %s.%s IS '%s'",
                            tableName, columnName, escapeSqlString(comment));
                }
                case "sqlserver" -> {
                    // SQL Server: EXEC sp_addextendedproperty 'MS_Description', 'comment', 'SCHEMA', 'dbo', 'TABLE', table_name, 'COLUMN', column_name
                    yield String.format("EXEC sp_addextendedproperty 'MS_Description', '%s', 'SCHEMA', 'dbo', 'TABLE', '%s', 'COLUMN', '%s'",
                            escapeSqlString(comment), tableName, columnName);
                }
                default -> {
                    log.warn("[字段注释][生成SQL] 不支持的数据库类型: {}", protocolName);
                    yield null;
                }
            };
        } catch (Exception e) {
            log.warn("[字段注释][生成SQL] 生成 ALTER COLUMN SQL 异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 转义 SQL 字符串中的单引号
     *
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private String escapeSqlString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "''");
    }
}


