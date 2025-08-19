package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.metadata.command.CommandResult;
import com.chua.common.support.metadata.hierarchy.HierarchyMetadata;
import com.chua.common.support.metadata.hierarchy.HierarchyNode;
import com.chua.common.support.metadata.hierarchy.HierarchyResult;
import com.chua.common.support.metadata.query.MetadataQuery;
import com.chua.starter.monitor.pojo.ValueCount;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * 数据控制台服务
 *
 * @author CH
 * @since 2023-04-01
 */
public interface SystemDataConsoleService {

    /**
     * 获取根节点数据
     *
     * @param settingId 设置ID，用于标识特定的数据源配置
     *                  例如：1, 2, 3...
     * @param query     查询参数，包含分页、过滤等信息
     *                  例如：MetadataQuery.create().setPage(1, 10)
     * @return 根节点数据列表，包含层级结构信息
     */
    ReturnResult<HierarchyResult<HierarchyNode>> getRoot(Integer settingId, MetadataQuery query);

    /**
     * 获取子节点数据
     *
     * @param settingId  设置ID，用于标识特定的数据源配置
     *                   例如：1, 2, 3...
     * @param parentPath 父节点路径，表示要查询其子节点的父节点完整路径
     *                   例如："/database1/table1" 或 "/root/node1"
     * @param query      查询参数，包含分页、过滤等信息
     *                   例如：MetadataQuery.create().setPage(1, 10)
     * @return 子节点数据列表，包含层级结构信息
     */
    ReturnResult<HierarchyResult<HierarchyNode>> getChildren(Integer settingId, String parentPath, MetadataQuery query);

    /**
     * 获取指定节点数据
     *
     * @param settingId 设置ID，用于标识特定的数据源配置
     *                  例如：1, 2, 3...
     * @param nodePath  节点路径，表示要获取详细信息的节点完整路径
     *                  例如："/database1/table1/column1" 或 "/root/node1"
     * @param query     查询参数，包含额外的查询条件
     *                  例如：MetadataQuery.create()
     * @return 节点详细信息
     */
    ReturnResult<HierarchyNode> getNode(Integer settingId, String nodePath, MetadataQuery query);

    /**
     * 搜索节点数据
     *
     * @param settingId     设置ID，用于标识特定的数据源配置
     *                      例如：1, 2, 3...
     * @param searchPattern 搜索模式，支持通配符的搜索关键字
     *                      例如："user*" 或 "table1"
     * @param query         查询参数，包含分页、排序等信息
     *                      例如：MetadataQuery.create().setPage(1, 10)
     * @return 搜索结果列表，包含匹配的节点信息
     */
    ReturnResult<HierarchyResult<HierarchyNode>> search(Integer settingId, String searchPattern, MetadataQuery query);

    /**
     * 执行命令
     *
     * @param settingId 设置ID，用于标识特定的数据源配置
     *                  例如：1, 2, 3...
     * @param type      命令类型，标识命令的类别
     *                  例如："SQL", "SHELL", "DDL"
     * @param command   命令内容，具体要执行的命令语句
     *                  例如："SELECT * FROM table1" 或 "SHOW DATABASES"
     * @param query     查询参数，包含执行相关的配置信息
     *                  例如：MetadataQuery.create().setTimeout(30)
     * @return 命令执行结果，包含执行状态和结果数据
     */
    ReturnResult<CommandResult> execute(Integer settingId, String type, String command, MetadataQuery query);

    /**
     * 生成文档
     *
     * @param settingId 设置ID，用于标识特定的数据源配置
     *                  例如：1, 2, 3...
     * @return 文档数据，以字节数组形式返回生成的文档文件
     */
    ReturnResult<ResponseEntity<byte[]>> generateDocument(Integer settingId);

    /**
     * 开始备份
     *
     * @param settingId 设置ID，用于标识特定的数据源配置
     *                  例如：1, 2, 3...
     * @return 操作结果，true表示操作成功，false表示操作失败
     */
    ReturnResult<Boolean> startBackup(Integer settingId);

    /**
     * 停止备份
     *
     * @param settingId 设置ID，用于标识特定的数据源配置
     *                  例如：1, 2, 3...
     * @return 操作结果，true表示操作成功，false表示操作失败
     */
    ReturnResult<Boolean> stopBackup(Integer settingId);

    /**
     * 查询备份状态
     *
     * @param settingId 设置ID，用于标识特定的数据源配置
     *                  例如：1, 2, 3...
     * @return 备份状态，true表示正在备份，false表示未在备份
     */
    ReturnResult<Boolean> backupStatus(Integer settingId);

    /**
     * 直接获取元数据实例（仅用于调试或扩展）
     *
     * @param settingId 设置ID，用于标识特定的数据源配置
     *                  例如：1, 2, 3...
     * @return 元数据实例，提供对底层数据源的直接访问
     */
    HierarchyMetadata getOrCreateHierarchy(Integer settingId);

    /**
     * 打开表：查询数据并合并字段注释（手动优先）
     *
     * @param settingId 设置ID，用于标识特定的数据源配置
     *                  例如：1, 2, 3...
     * @param nodePath  节点路径，表示要打开的表节点完整路径
     *                  例如："/database1/table1"
     * @param limit     限制返回的记录数，null表示不限制
     *                  例如：100, 1000
     * @return 表数据响应，包含列信息、数据行和注释
     */
    ReturnResult<com.chua.starter.monitor.pojo.TableOpenResponse> openTable(Integer settingId, String nodePath, Integer limit);

    /**
     * 表分析：对每个字段进行分组统计（频次）
     *
     * @param settingId 设置ID，用于标识特定的数据源配置
     *                  例如：1, 2, 3...
     * @param nodePath  节点路径，表示要分析的表节点完整路径
     *                  例如："/database1/table1"
     * @param limit     限制分析的记录数，null表示不限制
     *                  例如：1000, 10000
     * @return 字段值统计结果，key为字段名，value为值和计数的列表
     */
    ReturnResult<Map<String, List<ValueCount>>> analyzeTable(Integer settingId, String nodePath, Integer limit);

    /**
     * 重命名表
     *
     * @param settingId 数据源设置ID
     * @param nodePath  节点路径，表示要重命名的表节点完整路径
     *                  例如："/database1/table1"
     * @param newName   新的表名
     *                  例如："table1_new"
     * @return 操作结果，true表示操作成功，false表示操作失败
     */
    Boolean renameTable(Integer settingId, String nodePath, String newName);

    /**
     * 备份表
     *
     * @param settingId  数据源设置ID
     * @param nodePath   节点路径，表示要备份的表节点完整路径
     *                   例如："/database1/table1"
     * @param backupName 备份表名
     *                   例如："table1_backup_20230401"
     * @return 操作结果，true表示操作成功，false表示操作失败
     */
    Boolean backupTable(Integer settingId, String nodePath, String backupName);
}


