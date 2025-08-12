package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.command.CommandResult;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadata;
import com.chua.common.support.metadata.hierarchy.HierarchyNode;
import com.chua.common.support.metadata.hierarchy.HierarchyResult;
import com.chua.common.support.metadata.query.MetadataQuery;
import org.springframework.http.ResponseEntity;

/**
 * 数据控制台服务
 *
 * @author CH
 * @since 2023-04-01
 */
public interface DataConsoleService {

    /**
     * 获取根节点数据
     *
     * @param settingId 设置ID
     * @param query     查询参数
     * @return 根节点数据
     */
    ReturnResult<HierarchyResult<HierarchyNode>> getRoot(Integer settingId, MetadataQuery query);

    /**
     * 获取子节点数据
     *
     * @param settingId  设置ID
     * @param parentPath 父节点路径
     * @param query      查询参数
     * @return 子节点数据
     */
    ReturnResult<HierarchyResult<HierarchyNode>> getChildren(Integer settingId, String parentPath, MetadataQuery query);

    /**
     * 获取指定节点数据
     *
     * @param settingId 设置ID
     * @param nodePath  节点路径
     * @param query     查询参数
     * @return 节点数据
     */
    ReturnResult<HierarchyNode> getNode(Integer settingId, String nodePath, MetadataQuery query);

    /**
     * 搜索节点数据
     *
     * @param settingId     设置ID
     * @param searchPattern 搜索模式
     * @param query         查询参数
     * @return 搜索结果
     */
    ReturnResult<HierarchyResult<HierarchyNode>> search(Integer settingId, String searchPattern, MetadataQuery query);

    /**
     * 执行命令
     *
     * @param settingId 设置ID
     * @param type      命令类型
     * @param command   命令内容
     * @param query     查询参数
     * @return 命令执行结果
     */
    ReturnResult<CommandResult> execute(Integer settingId, String type, String command, MetadataQuery query);

    /**
     * 生成文档
     *
     * @param settingId 设置ID
     * @return 文档数据
     */
    ReturnResult<ResponseEntity<byte[]>> generateDocument(Integer settingId);

    /**
     * 开始备份
     *
     * @param settingId 设置ID
     * @return 操作结果
     */
    ReturnResult<Boolean> startBackup(Integer settingId);

    /**
     * 停止备份
     *
     * @param settingId 设置ID
     * @return 操作结果
     */
    ReturnResult<Boolean> stopBackup(Integer settingId);

    /**
     * 查询备份状态
     *
     * @param settingId 设置ID
     * @return 备份状态
     */
    ReturnResult<Boolean> backupStatus(Integer settingId);

    /**
     * 直接获取元数据实例（仅用于调试或扩展）
     *
     * @param settingId 设置ID
     * @return 元数据实例
     */
    HierarchyMetadata getOrCreateHierarchy(Integer settingId);
}


