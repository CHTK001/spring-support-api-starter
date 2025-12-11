package com.chua.starter.monitor.controller.data;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.data.ColumnInfo;
import com.chua.starter.monitor.pojo.data.TableStructureInfo;
import com.chua.starter.monitor.service.data.SystemDataConsoleService;
import com.chua.starter.monitor.spi.metadata.StructureMetadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 控制台结构操作控制器
 * 提供表结构查看、字段管理、自动提示等功能
 *
 * @author CH
 * @since 2023-04-01
 */
@Slf4j
@RestController
@RequestMapping("/system/data/console/{settingId}/table")
@Tag(name = "控制台结构操作", description = "表结构管理/字段操作/自动提示等")
@RequiredArgsConstructor
public class SystemDataConsoleStructureController {
    private final SystemDataConsoleService systemDataConsoleService;


    /**
     * 重命名表
     *
     * @param settingId 数据源设置ID，用于标识特定的数据源配置
     *                  例如：1, 2, 3...
     * @param req       重命名表请求参数，包含节点路径和新表名
     *                  例如：{"nodePath": "/database1/table1", "newName": "table1_new"}
     * @return 操作结果，true表示操作成功，false表示操作失败
     */
    @PostMapping("/rename")
    @Operation(summary = "重命名表")
    public ReturnResult<Boolean> rename(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @RequestBody StructureMetadata.RenameTableRequest req) {
        try {
            return ReturnResult.ok(systemDataConsoleService.renameTable(settingId, req.getNodePath(), req.getNewName()));
        } catch (Exception e) {
            return ReturnResult.error("重命名失败: " + e.getMessage());
        }
    }

    /**
     * 备份表
     *
     * @param settingId 数据源设置ID，用于标识特定的数据源配置
     *                  例如：1, 2, 3...
     * @param req       备份表请求参数，包含节点路径和备份表名
     *                  例如：{"nodePath": "/database1/table1", "backupName": "table1_backup_20230401"}
     * @return 操作结果，true表示操作成功，false表示操作失败
     */
    @PostMapping("/backup")
    @Operation(summary = "备份表")
    public ReturnResult<Boolean> backup(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @RequestBody StructureMetadata.BackupTableRequest req) {
        try {
            return ReturnResult.ok(systemDataConsoleService.backupTable(settingId, req.getNodePath(), req.getBackupName()));
        } catch (Exception e) {
            return ReturnResult.error("备份失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有表名列表（用于SQL自动提示）
     *
     * @param settingId 数据源设置ID
     * @return 表名列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取表名列表")
    public ReturnResult<List<String>> listTables(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId) {
        try {
            return ReturnResult.ok(systemDataConsoleService.listTables(settingId));
        } catch (Exception e) {
            log.error("获取表名列表失败", e);
            return ReturnResult.error("获取表名列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定表的字段列表（用于SQL自动提示）
     *
     * @param settingId 数据源设置ID
     * @param tableName 表名
     * @return 字段名列表
     */
    @GetMapping("/columns")
    @Operation(summary = "获取字段列表")
    public ReturnResult<List<ColumnInfo>> listColumns(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @Parameter(description = "表名", example = "user")
            @RequestParam String tableName) {
        try {
            return ReturnResult.ok(systemDataConsoleService.listColumns(settingId, tableName));
        } catch (Exception e) {
            log.error("获取字段列表失败", e);
            return ReturnResult.error("获取字段列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取表结构详情
     *
     * @param settingId 数据源设置ID
     * @param tableName 表名
     * @return 表结构信息
     */
    @GetMapping("/structure")
    @Operation(summary = "获取表结构")
    public ReturnResult<TableStructureInfo> getTableStructure(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @Parameter(description = "表名", example = "user")
            @RequestParam String tableName) {
        try {
            return ReturnResult.ok(systemDataConsoleService.getTableStructure(settingId, tableName));
        } catch (Exception e) {
            log.error("获取表结构失败", e);
            return ReturnResult.error("获取表结构失败: " + e.getMessage());
        }
    }

    /**
     * 添加字段
     *
     * @param settingId 数据源设置ID
     * @param req       添加字段请求
     * @return 操作结果
     */
    @PostMapping("/column/add")
    @Operation(summary = "添加字段")
    public ReturnResult<Boolean> addColumn(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @RequestBody StructureMetadata.AddColumnRequest req) {
        try {
            return ReturnResult.ok(systemDataConsoleService.addColumn(settingId, req));
        } catch (Exception e) {
            log.error("添加字段失败", e);
            return ReturnResult.error("添加字段失败: " + e.getMessage());
        }
    }

    /**
     * 修改字段
     *
     * @param settingId 数据源设置ID
     * @param req       修改字段请求
     * @return 操作结果
     */
    @PostMapping("/column/modify")
    @Operation(summary = "修改字段")
    public ReturnResult<Boolean> modifyColumn(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @RequestBody StructureMetadata.ModifyColumnRequest req) {
        try {
            return ReturnResult.ok(systemDataConsoleService.modifyColumn(settingId, req));
        } catch (Exception e) {
            log.error("修改字段失败", e);
            return ReturnResult.error("修改字段失败: " + e.getMessage());
        }
    }

    /**
     * 删除字段
     *
     * @param settingId 数据源设置ID
     * @param req       删除字段请求
     * @return 操作结果
     */
    @PostMapping("/column/drop")
    @Operation(summary = "删除字段")
    public ReturnResult<Boolean> dropColumn(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @RequestBody StructureMetadata.DropColumnRequest req) {
        try {
            return ReturnResult.ok(systemDataConsoleService.dropColumn(settingId, req));
        } catch (Exception e) {
            log.error("删除字段失败", e);
            return ReturnResult.error("删除字段失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除字段
     *
     * @param settingId 数据源设置ID
     * @param req       批量删除字段请求
     * @return 操作结果
     */
    @PostMapping("/column/batch-drop")
    @Operation(summary = "批量删除字段")
    public ReturnResult<Boolean> batchDropColumn(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @RequestBody StructureMetadata.BatchDropColumnRequest req) {
        try {
            return ReturnResult.ok(systemDataConsoleService.batchDropColumn(settingId, req));
        } catch (Exception e) {
            log.error("批量删除字段失败", e);
            return ReturnResult.error("批量删除字段失败: " + e.getMessage());
        }
    }

    /**
     * 批量添加字段
     *
     * @param settingId 数据源设置ID
     * @param req       批量添加字段请求
     * @return 操作结果
     */
    @PostMapping("/column/batch-add")
    @Operation(summary = "批量添加字段")
    public ReturnResult<Boolean> batchAddColumn(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @RequestBody StructureMetadata.BatchAddColumnRequest req) {
        try {
            return ReturnResult.ok(systemDataConsoleService.batchAddColumn(settingId, req));
        } catch (Exception e) {
            log.error("批量添加字段失败", e);
            return ReturnResult.error("批量添加字段失败: " + e.getMessage());
        }
    }

    /**
     * 批量修改字段
     *
     * @param settingId 数据源设置ID
     * @param req       批量修改字段请求
     * @return 操作结果
     */
    @PostMapping("/column/batch-modify")
    @Operation(summary = "批量修改字段")
    public ReturnResult<Boolean> batchModifyColumn(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @RequestBody StructureMetadata.BatchModifyColumnRequest req) {
        try {
            return ReturnResult.ok(systemDataConsoleService.batchModifyColumn(settingId, req));
        } catch (Exception e) {
            log.error("批量修改字段失败", e);
            return ReturnResult.error("批量修改字段失败: " + e.getMessage());
        }
    }

    /**
     * 调整字段位置
     *
     * @param settingId 数据源设置ID
     * @param req       调整位置请求
     * @return 操作结果
     */
    @PostMapping("/column/reorder")
    @Operation(summary = "调整字段位置")
    public ReturnResult<Boolean> reorderColumn(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @RequestBody StructureMetadata.ReorderColumnRequest req) {
        try {
            return ReturnResult.ok(systemDataConsoleService.reorderColumn(settingId, req));
        } catch (Exception e) {
            log.error("调整字段位置失败", e);
            return ReturnResult.error("调整字段位置失败: " + e.getMessage());
        }
    }

    /**
     * 获取建表语句
     *
     * @param settingId 数据源设置ID
     * @param tableName 表名
     * @return 建表SQL语句
     */
    @GetMapping("/ddl")
    @Operation(summary = "获取建表语句")
    public ReturnResult<String> getCreateTableDdl(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @Parameter(description = "表名", example = "user")
            @RequestParam String tableName) {
        try {
            return ReturnResult.ok(systemDataConsoleService.getCreateTableDdl(settingId, tableName));
        } catch (Exception e) {
            log.error("获取建表语句失败", e);
            return ReturnResult.error("获取建表语句失败: " + e.getMessage());
        }
    }

    /**
     * 修改表注释
     *
     * @param settingId 数据源设置ID
     * @param req       修改表注释请求
     * @return 操作结果
     */
    @PostMapping("/comment")
    @Operation(summary = "修改表注释")
    public ReturnResult<Boolean> modifyTableComment(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @RequestBody StructureMetadata.ModifyTableCommentRequest req) {
        try {
            return ReturnResult.ok(systemDataConsoleService.modifyTableComment(settingId, req));
        } catch (Exception e) {
            log.error("修改表注释失败", e);
            return ReturnResult.error("修改表注释失败: " + e.getMessage());
        }
    }

    /**
     * 批量修改表结构
     * 包括添加字段、修改字段、删除字段、调整顺序、修改表注释等操作
     * 减少前后端交互次数，一次请求完成所有修改
     *
     * @param settingId 数据源设置ID
     * @param req       批量操作请求
     * @return 操作结果
     */
    @PostMapping("/batch")
    @Operation(summary = "批量修改表结构")
    public ReturnResult<Boolean> batchModifyTableStructure(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @RequestBody StructureMetadata.BatchTableStructureRequest req) {
        try {
            return ReturnResult.ok(systemDataConsoleService.batchModifyTableStructure(settingId, req));
        } catch (Exception e) {
            log.error("批量修改表结构失败", e);
            return ReturnResult.error("批量修改表结构失败: " + e.getMessage());
        }
    }

    // ==================== 索引管理 ====================

    /**
     * 获取表索引列表
     *
     * @param settingId 数据源设置ID
     * @param tableName 表名
     * @return 索引列表
     * @author CH
     * @since 2024-12-10
     */
    @GetMapping("/indexes")
    @Operation(summary = "获取表索引列表")
    public ReturnResult<List<Map<String, Object>>> getTableIndexes(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @Parameter(description = "表名", example = "user")
            @RequestParam String tableName) {
        try {
            return ReturnResult.ok(systemDataConsoleService.getTableIndexes(settingId, tableName));
        } catch (Exception e) {
            log.error("获取表索引列表失败", e);
            return ReturnResult.error("获取表索引列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建索引
     *
     * @param settingId 数据源设置ID
     * @param req       创建索引请求
     * @return 操作结果
     * @author CH
     * @since 2024-12-10
     */
    @PostMapping("/indexes")
    @Operation(summary = "创建索引")
    public ReturnResult<Boolean> createIndex(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @RequestBody StructureMetadata.CreateIndexRequest req) {
        try {
            return ReturnResult.ok(systemDataConsoleService.createIndex(settingId, req));
        } catch (Exception e) {
            log.error("创建索引失败", e);
            return ReturnResult.error("创建索引失败: " + e.getMessage());
        }
    }

    /**
     * 删除索引
     *
     * @param settingId 数据源设置ID
     * @param tableName 表名
     * @param indexName 索引名
     * @return 操作结果
     * @author CH
     * @since 2024-12-10
     */
    @DeleteMapping("/indexes")
    @Operation(summary = "删除索引")
    public ReturnResult<Boolean> dropIndex(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @Parameter(description = "表名", example = "user")
            @RequestParam String tableName,
            @Parameter(description = "索引名", example = "idx_name")
            @RequestParam String indexName) {
        try {
            return ReturnResult.ok(systemDataConsoleService.dropIndex(settingId, tableName, indexName));
        } catch (Exception e) {
            log.error("删除索引失败", e);
            return ReturnResult.error("删除索引失败: " + e.getMessage());
        }
    }

    // ==================== 分区管理 ====================

    /**
     * 获取表分区列表
     *
     * @param settingId 数据源设置ID
     * @param tableName 表名
     * @return 分区列表
     * @author CH
     * @since 2024-12-10
     */
    @GetMapping("/partitions")
    @Operation(summary = "获取表分区列表")
    public ReturnResult<List<Map<String, Object>>> getTablePartitions(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @Parameter(description = "表名", example = "user")
            @RequestParam String tableName) {
        try {
            return ReturnResult.ok(systemDataConsoleService.getTablePartitions(settingId, tableName));
        } catch (Exception e) {
            log.error("获取表分区列表失败", e);
            return ReturnResult.error("获取表分区列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建分区
     *
     * @param settingId 数据源设置ID
     * @param req       创建分区请求
     * @return 操作结果
     * @author CH
     * @since 2024-12-10
     */
    @PostMapping("/partitions")
    @Operation(summary = "创建分区")
    public ReturnResult<Boolean> createPartition(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @RequestBody StructureMetadata.CreatePartitionRequest req) {
        try {
            return ReturnResult.ok(systemDataConsoleService.createPartition(settingId, req));
        } catch (Exception e) {
            log.error("创建分区失败", e);
            return ReturnResult.error("创建分区失败: " + e.getMessage());
        }
    }

    /**
     * 删除分区
     *
     * @param settingId     数据源设置ID
     * @param tableName     表名
     * @param partitionName 分区名
     * @return 操作结果
     * @author CH
     * @since 2024-12-10
     */
    @DeleteMapping("/partitions")
    @Operation(summary = "删除分区")
    public ReturnResult<Boolean> dropPartition(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId,
            @Parameter(description = "表名", example = "user")
            @RequestParam String tableName,
            @Parameter(description = "分区名", example = "p0")
            @RequestParam String partitionName) {
        try {
            return ReturnResult.ok(systemDataConsoleService.dropPartition(settingId, tableName, partitionName));
        } catch (Exception e) {
            log.error("删除分区失败", e);
            return ReturnResult.error("删除分区失败: " + e.getMessage());
        }
    }

    /**
     * 检测数据库能力
     * 返回数据库是否支持索引和分区功能
     *
     * @param settingId 数据源设置ID
     * @return 数据库能力信息
     * @author CH
     * @since 2024-12-10
     */
    @GetMapping("/capabilities")
    @Operation(summary = "检测数据库能力")
    public ReturnResult<Map<String, Boolean>> getDatabaseCapabilities(
            @Parameter(description = "数据源设置ID", example = "1")
            @PathVariable("settingId") Integer settingId) {
        try {
            return ReturnResult.ok(systemDataConsoleService.getDatabaseCapabilities(settingId));
        } catch (Exception e) {
            log.error("检测数据库能力失败", e);
            return ReturnResult.error("检测数据库能力失败: " + e.getMessage());
        }
    }
}

