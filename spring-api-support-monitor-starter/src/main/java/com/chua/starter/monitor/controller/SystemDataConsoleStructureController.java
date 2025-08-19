package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.service.SystemDataConsoleService;
import com.chua.starter.monitor.spi.metadata.StructureMetadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 控制台结构操作控制器
 * - 提供能力探测与结构操作（重命名表、备份表）
 *
 * @author CH
 * @since 2023-04-01
 */
@Slf4j
@RestController
@RequestMapping("/system/data/console/{settingId}/table")
@Tag(name = "控制台结构操作", description = "表重命名/备份等")
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
}

