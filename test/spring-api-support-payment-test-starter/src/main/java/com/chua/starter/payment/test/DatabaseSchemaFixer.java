package com.chua.starter.payment.test;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 启动时自动检查并修复数据库缺失列。
 * pay_global_config 表所有列使用 pay_global_config_ 前缀，但 DDL 可能使用了非前缀名。
 * 此组件通过 JDBC 元数据检查并补全缺失的列。
 */
@Component
public class DatabaseSchemaFixer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaFixer.class);

    private static final Map<String, String> EXPECTED_COLUMNS = new LinkedHashMap<>();
    static {
        EXPECTED_COLUMNS.put("pay_global_config_id", "BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID'");
        EXPECTED_COLUMNS.put("pay_global_config_config_key", "VARCHAR(64) NOT NULL DEFAULT 'DEFAULT' COMMENT '配置键'");
        EXPECTED_COLUMNS.put("pay_global_config_notify_base_url", "VARCHAR(255) DEFAULT NULL COMMENT '回调基础地址'");
        EXPECTED_COLUMNS.put("pay_global_config_return_url", "VARCHAR(255) DEFAULT NULL COMMENT '默认回跳地址'");
        EXPECTED_COLUMNS.put("pay_global_config_callback_path_template", "VARCHAR(255) DEFAULT '/{orderNo}/{merchantId}' COMMENT '回调路径模板'");
        EXPECTED_COLUMNS.put("pay_global_config_auto_refresh_seconds", "INT DEFAULT 60 COMMENT '首页自动刷新秒数'");
        EXPECTED_COLUMNS.put("pay_global_config_remark", "VARCHAR(500) DEFAULT NULL COMMENT '备注'");
        EXPECTED_COLUMNS.put("pay_global_config_sample_pay_notify_url", "VARCHAR(500) DEFAULT NULL COMMENT '支付回调示例 URL'");
        EXPECTED_COLUMNS.put("pay_global_config_sample_refund_notify_url", "VARCHAR(500) DEFAULT NULL COMMENT '退款回调示例 URL'");
        EXPECTED_COLUMNS.put("pay_global_config_sample_wallet_recharge_notify_url", "VARCHAR(500) DEFAULT NULL COMMENT '钱包充值回调示例 URL'");
        EXPECTED_COLUMNS.put("pay_global_config_sample_wallet_transfer_notify_url", "VARCHAR(500) DEFAULT NULL COMMENT '钱包转账回调示例 URL'");
        EXPECTED_COLUMNS.put("pay_global_config_sample_wallet_withdraw_notify_url", "VARCHAR(500) DEFAULT NULL COMMENT '钱包提现回调示例 URL'");
        EXPECTED_COLUMNS.put("pay_global_config_create_time", "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'");
        EXPECTED_COLUMNS.put("pay_global_config_update_time", "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'");
    }

    private final DataSource dataSource;

    public DatabaseSchemaFixer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void fixMissingColumns() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String tableName = "pay_global_config";

            Set<String> existingColumns = new HashSet<>();
            try (ResultSet rs = meta.getColumns(null, null, tableName, "%")) {
                while (rs.next()) {
                    existingColumns.add(rs.getString("COLUMN_NAME").toLowerCase());
                }
            }

            int added = 0;
            try (Statement stmt = conn.createStatement()) {
                for (Map.Entry<String, String> entry : EXPECTED_COLUMNS.entrySet()) {
                    String colName = entry.getKey();
                    if (!existingColumns.contains(colName.toLowerCase())) {
                        String ddl = "ALTER TABLE `" + tableName + "` ADD COLUMN `" + colName + "` " + entry.getValue();
                        log.warn("[SchemaFixer] {} 缺少 {} 列，正在添加...", tableName, colName);
                        stmt.execute(ddl);
                        added++;
                        log.warn("[SchemaFixer] {} 列添加成功", colName);
                    }
                }
            }

            if (added == 0) {
                log.info("[SchemaFixer] {} 列完整性检查通过 (共 {} 列)", tableName, EXPECTED_COLUMNS.size());
            } else {
                log.warn("[SchemaFixer] {} 修复完成，共添加 {} 列", tableName, added);
            }
        } catch (Exception e) {
            log.warn("[SchemaFixer] 检查/修复数据库列失败，应用继续启动", e);
        }
    }
}
