package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.spi.metadata.StructureMetadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 控制台结构操作控制器
 * - 提供能力探测与结构操作（重命名表、备份表）
 */
@Slf4j
@RestController
@RequestMapping("/system/data/console/{settingId}/table")
@Tag(name = "控制台结构操作", description = "表重命名/备份等")
@RequiredArgsConstructor
public class SystemDataConsoleStructureController {

    /** SPI 适配器（后续可引入 Factory 按数据源类型选择具体实现） */
    private final StructureMetadata structureMetadata;

    @GetMapping("/capabilities")
    @Operation(summary = "结构操作能力探测")
    public ReturnResult<Object> capabilities(@PathVariable("settingId") Long settingId) {
        try {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("supportsRenameTable", structureMetadata.supportsRenameTable());
            map.put("supportsBackupTable", structureMetadata.supportsBackupTable());
            return ReturnResult.ok(map);
        } catch (Exception e) {
            return ReturnResult.error("能力探测失败: " + e.getMessage());
        }
    }

    @PostMapping("/rename")
    @Operation(summary = "重命名表")
    public ReturnResult<Boolean> rename(@PathVariable("settingId") Long settingId,
                                        @RequestBody StructureMetadata.RenameTableRequest req) {
        try {
            if (!structureMetadata.supportsRenameTable()) {
                return ReturnResult.error("当前数据源不支持重命名表");
            }
            return ReturnResult.ok(structureMetadata.renameTable(req.getNodePath(), req.getNewName()));
        } catch (Exception e) {
            return ReturnResult.error("重命名失败: " + e.getMessage());
        }
    }

    @PostMapping("/backup")
    @Operation(summary = "备份表")
    public ReturnResult<Boolean> backup(@PathVariable("settingId") Long settingId,
                                        @RequestBody StructureMetadata.BackupTableRequest req) {
        try {
            if (!structureMetadata.supportsBackupTable()) {
                return ReturnResult.error("当前数据源不支持备份表");
            }
            return ReturnResult.ok(structureMetadata.backupTable(req.getNodePath(), req.getBackupName()));
        } catch (Exception e) {
            return ReturnResult.error("备份失败: " + e.getMessage());
        }
    }
}

