package com.chua.starter.monitor.controller.file;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.service.FileManagementModeDataMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 文件管理模式数据迁移控制器
 * 提供数据迁移相关的接口
 *
 * @author CH
 * @since 2024/12/13
 */
@Slf4j
@RestController
@RequestMapping("v1/gen/server/setting/migration")
@Tag(name = "文件管理模式数据迁移", description = "文件管理模式数据迁移相关接口")
public class FileManagementModeDataMigrationController {

    @Autowired
    private FileManagementModeDataMigrationService migrationService;

    /**
     * 检查数据迁移状态
     *
     * @return 迁移状态信息
     */
    @ResponseBody
    @Operation(summary = "检查数据迁移状态")
    @GetMapping("status")
    public ReturnResult<Map<String, Object>> checkMigrationStatus() {
        log.info("检查文件管理模式数据迁移状态");
        return migrationService.checkMigrationStatus();
    }

    /**
     * 执行数据迁移
     *
     * @return 迁移结果
     */
    @ResponseBody
    @Operation(summary = "执行数据迁移")
    @PostMapping("execute")
    public ReturnResult<String> executeMigration() {
        log.info("开始执行文件管理模式数据迁移");
        return migrationService.migrateFileManagementModeData();
    }

    /**
     * 获取迁移帮助信息
     *
     * @return 帮助信息
     */
    @ResponseBody
    @Operation(summary = "获取迁移帮助信息")
    @GetMapping("help")
    public ReturnResult<Map<String, Object>> getMigrationHelp() {
        Map<String, Object> help = Map.of(
                "description", "文件管理模式数据迁移工具",
                "purpose", "将旧的数字格式枚举值转换为新的字符串格式",
                "mapping", Map.of(
                        "0", "NONE - 不启用文件管理",
                        "1", "SSH - SSH模式",
                        "2", "API - API模式",
                        "3", "NODE - NODE模式",
                        "4", "LOCAL - 本地模式"
                ),
                "steps", new String[]{
                        "1. 调用 /status 接口检查当前数据状态",
                        "2. 如果 needMigrationCount > 0，调用 /execute 接口执行迁移",
                        "3. 再次调用 /status 接口验证迁移结果"
                },
                "safety", new String[]{
                        "迁移过程使用事务保护",
                        "迁移前会检查数据状态",
                        "迁移后会验证结果",
                        "建议在维护窗口期间执行"
                }
        );

        return ReturnResult.ok(help);
    }
}
