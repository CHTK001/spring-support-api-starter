package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.constant.DatabaseType;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadata;
import com.chua.common.support.metadata.hierarchy.HierarchyNode;
import com.chua.common.support.metadata.hierarchy.HierarchyResult;
import com.chua.common.support.metadata.query.MetadataQuery;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.EnumUtils;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.entity.SystemDataFieldComment;
import com.chua.starter.monitor.entity.SystemDataSetting;
import com.chua.starter.monitor.mapper.SystemDataFieldCommentMapper;
import com.chua.starter.monitor.mapper.SystemDataSettingMapper;
import com.chua.starter.monitor.service.SystemDataConsoleService;
import com.chua.starter.monitor.service.SystemDataFieldCommentService;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 字段注释 Service 实现
 * <p>
 * 提供基于 settingId + nodePath 的注释查询与新增/更新能力。
 */
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
        DatabaseType databaseType = EnumUtils.getEnum(DatabaseType.class, setting.getSystemDataSettingProtocol());
        if (null == databaseType) {
            return ReturnResult.ok(true);
        }
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
                String dataType = MapUtils.getString(hierarchyNode.getProperties(), "columnType")
                        + "(" + MapUtils.getString(hierarchyNode.getProperties(), "columnSize") + ")";
                systemDataConsoleService.execute(settingId,
                        "sql",
                        CollectionUtils.findFirst(databaseType.generateAlterColumnSql(tableName, columnName, dataType
                                , MapUtils.getBoolean(hierarchyNode.getProperties(), "nullable", true), comment)),
                        MetadataQuery.create());
            } catch (Exception ignored) {
            }
        }
        return ReturnResult.ok(true);
    }
}


