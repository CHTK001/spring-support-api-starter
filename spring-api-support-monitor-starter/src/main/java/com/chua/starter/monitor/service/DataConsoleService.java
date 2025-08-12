package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.command.CommandResult;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadata;
import com.chua.common.support.metadata.hierarchy.HierarchyNode;
import com.chua.common.support.metadata.hierarchy.HierarchyResult;
import com.chua.common.support.metadata.query.MetadataQuery;

/**
 * 数据控制台服务
 */
public interface DataConsoleService {

    ReturnResult<HierarchyResult<HierarchyNode>> getRoot(Integer settingId, MetadataQuery query);

    ReturnResult<HierarchyResult<HierarchyNode>> getChildren(Integer settingId, String parentPath, MetadataQuery query);

    ReturnResult<HierarchyNode> getNode(Integer settingId, String nodePath, MetadataQuery query);

    ReturnResult<HierarchyResult<HierarchyNode>> search(Integer settingId, String searchPattern, MetadataQuery query);

    ReturnResult<CommandResult> execute(Integer settingId, String type, String command, MetadataQuery query);

    ReturnResult<Object> generateDocument(Integer settingId);

    ReturnResult<Boolean> startBackup(Integer settingId);

    ReturnResult<Boolean> stopBackup(Integer settingId);

    ReturnResult<Boolean> backupStatus(Integer settingId);

    /**
     * 直接获取元数据实例（仅用于调试或扩展）
     */
    HierarchyMetadata getOrCreateHierarchy(Integer settingId);
}


