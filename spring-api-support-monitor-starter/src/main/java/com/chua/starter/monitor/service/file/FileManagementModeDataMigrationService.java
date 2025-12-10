package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 文件管理模式数据迁移服务
 * 用于将旧的数字格式数据迁移为新的字符串格式
 *
 * @author CH
 * @since 2024/12/13
 */
@Slf4j
@Service
public class FileManagementModeDataMigrationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 执行文件管理模式数据迁移
     * 将数字格式的枚举值转换为字符串格式
     */
    @Transactional
    public ReturnResult<String> migrateFileManagementModeData() {
        try {
            log.info("开始执行文件管理模式数据迁移...");

            // 1. 检查需要迁移的数据
            String checkSql = """
                    SELECT 
                        monitor_sys_gen_server_setting_file_management_mode,
                        COUNT(*) as count
                    FROM monitor_sys_gen_server_setting 
                    WHERE monitor_sys_gen_server_setting_file_management_mode REGEXP '^[0-9]+$'
                       OR monitor_sys_gen_server_setting_file_management_mode IS NULL
                    GROUP BY monitor_sys_gen_server_setting_file_management_mode
                    """;

            List<Map<String, Object>> needMigration = jdbcTemplate.queryForList(checkSql);
            if (needMigration.isEmpty()) {
                log.info("没有需要迁移的数据");
                return ReturnResult.ok("没有需要迁移的数据");
            }

            log.info("发现需要迁移的数据: {}", needMigration);

            // 2. 执行数据迁移
            String migrationSql = """
                    UPDATE monitor_sys_gen_server_setting 
                    SET monitor_sys_gen_server_setting_file_management_mode = 
                        CASE 
                            WHEN monitor_sys_gen_server_setting_file_management_mode = '0' THEN 'NONE'
                            WHEN monitor_sys_gen_server_setting_file_management_mode = '1' THEN 'SSH'
                            WHEN monitor_sys_gen_server_setting_file_management_mode = '2' THEN 'API'
                            WHEN monitor_sys_gen_server_setting_file_management_mode = '3' THEN 'NODE'
                            WHEN monitor_sys_gen_server_setting_file_management_mode = '4' THEN 'LOCAL'
                            ELSE 'NONE'
                        END
                    WHERE monitor_sys_gen_server_setting_file_management_mode IN ('0', '1', '2', '3', '4')
                       OR monitor_sys_gen_server_setting_file_management_mode REGEXP '^[0-9]+$'
                    """;

            int updatedRows = jdbcTemplate.update(migrationSql);
            log.info("迁移数字格式数据，更新了 {} 行", updatedRows);

            // 3. 处理 NULL 值
            String nullUpdateSql = """
                    UPDATE monitor_sys_gen_server_setting 
                    SET monitor_sys_gen_server_setting_file_management_mode = 'NONE'
                    WHERE monitor_sys_gen_server_setting_file_management_mode IS NULL
                    """;

            int nullUpdatedRows = jdbcTemplate.update(nullUpdateSql);
            log.info("处理 NULL 值，更新了 {} 行", nullUpdatedRows);

            // 4. 验证迁移结果
            String verifySql = """
                    SELECT 
                        monitor_sys_gen_server_setting_file_management_mode,
                        COUNT(*) as count
                    FROM monitor_sys_gen_server_setting 
                    GROUP BY monitor_sys_gen_server_setting_file_management_mode
                    ORDER BY monitor_sys_gen_server_setting_file_management_mode
                    """;

            List<Map<String, Object>> afterMigration = jdbcTemplate.queryForList(verifySql);
            log.info("迁移后的数据分布: {}", afterMigration);

            // 5. 检查是否还有数字格式的数据
            String remainingCheckSql = """
                    SELECT COUNT(*) as count
                    FROM monitor_sys_gen_server_setting 
                    WHERE monitor_sys_gen_server_setting_file_management_mode REGEXP '^[0-9]+$'
                       OR monitor_sys_gen_server_setting_file_management_mode IS NULL
                    """;

            Integer remainingCount = jdbcTemplate.queryForObject(remainingCheckSql, Integer.class);
            if (remainingCount != null && remainingCount > 0) {
                log.warn("迁移后仍有 {} 行数据格式不正确", remainingCount);
                return ReturnResult.error("迁移不完整，仍有 " + remainingCount + " 行数据格式不正确");
            }

            String result = String.format("数据迁移成功！更新了 %d 行数字格式数据，%d 行 NULL 值数据",
                    updatedRows, nullUpdatedRows);
            log.info(result);
            return ReturnResult.ok(result);

        } catch (Exception e) {
            log.error("文件管理模式数据迁移失败", e);
            return ReturnResult.error("数据迁移失败: " + e.getMessage());
        }
    }

    /**
     * 检查数据迁移状态
     */
    public ReturnResult<Map<String, Object>> checkMigrationStatus() {
        try {
            // 检查数据分布
            String distributionSql = """
                    SELECT 
                        monitor_sys_gen_server_setting_file_management_mode,
                        COUNT(*) as count
                    FROM monitor_sys_gen_server_setting 
                    GROUP BY monitor_sys_gen_server_setting_file_management_mode
                    ORDER BY monitor_sys_gen_server_setting_file_management_mode
                    """;

            List<Map<String, Object>> distribution = jdbcTemplate.queryForList(distributionSql);

            // 检查是否有需要迁移的数据
            String needMigrationSql = """
                    SELECT COUNT(*) as count
                    FROM monitor_sys_gen_server_setting 
                    WHERE monitor_sys_gen_server_setting_file_management_mode REGEXP '^[0-9]+$'
                       OR monitor_sys_gen_server_setting_file_management_mode IS NULL
                    """;

            Integer needMigrationCount = jdbcTemplate.queryForObject(needMigrationSql, Integer.class);

            Map<String, Object> status = Map.of(
                    "distribution", distribution,
                    "needMigrationCount", needMigrationCount != null ? needMigrationCount : 0,
                    "migrationNeeded", needMigrationCount != null && needMigrationCount > 0
            );

            return ReturnResult.ok(status);

        } catch (Exception e) {
            log.error("检查迁移状态失败", e);
            return ReturnResult.error("检查迁移状态失败: " + e.getMessage());
        }
    }
}
