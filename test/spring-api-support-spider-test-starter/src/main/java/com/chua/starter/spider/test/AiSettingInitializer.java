package com.chua.starter.spider.test;

import com.chua.starter.ai.support.chat.ChatClientFactoryBean;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 测试环境 AI 配置初始化。
 *
 * <p>在 H2 测试数据库中写入 AI 全局设置（白山智算 OpenAI 兼容），
 * 使 AI 流程生成功能可在测试环境中正常使用。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiSettingInitializer implements ApplicationRunner {

    private static final String GROUP = "runtime";
    private final ChatClientFactoryBean chatClientFactoryBean;

    private static final Object[][] SETTINGS = {
        {"enabled",           "true",                                   "BOOLEAN", "是否启用 AI ChatClient",                                    5},
        {"factory",           "default",                                "STRING",  "ChatClient SPI 工厂名称",                                     8},
        {"provider",          "openai",                                 "STRING",  "默认 LLM 提供商",                                             9},
        {"default_model",     "DeepSeek-R1-0528-Qwen3-8B",              "STRING",  "默认聊天模型编码",                                            10},
        {"provider_base_url", "https://api.edgefn.net/v1",                       "STRING", "LLM 提供商基础地址",                                         70},
        {"provider_api_key",  "sk-BI8zPmMU5QLAa1hV529eF085AcE94133B6F78412Eb510bAb", "STRING", "LLM 提供商 API Key",                                     75},
        {"system_prompt",     "",                                       "STRING",  "默认系统提示词",                                              15},
        {"temperature",       "0.7",                                    "NUMBER",  "默认采样温度",                                                20},
        {"max_tokens",        "4096",                                   "NUMBER",  "默认最大输出 token",                                          30},
        {"timeout_millis",    "60000",                                  "NUMBER",  "默认超时时间(毫秒)",                                          35},
    };

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        // 检查表是否存在（先通过查询试探，兼容 MySQL 和 H2）
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(1) FROM sys_ai_global_setting WHERE 1=0", Integer.class);
        } catch (Exception e) {
            log.warn("sys_ai_global_setting 表不存在（{}），尝试自动创建...", e.getMessage());
            tryCreateTable();
            return;
        }

        for (Object[] row : SETTINGS) {
            String field = (String) row[0];
            String value = (String) row[1];
            String valueType = (String) row[2];
            String desc = (String) row[3];
            int sort = (int) row[4];

            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_ai_global_setting WHERE sys_ai_global_setting_group = ? AND sys_ai_global_setting_field = ?",
                Integer.class,
                GROUP, field
            );
            if (count != null && count > 0) {
                // 更新现有记录的值
                jdbcTemplate.update(
                    "UPDATE sys_ai_global_setting SET sys_ai_global_setting_value = ?, update_time = ? WHERE sys_ai_global_setting_group = ? AND sys_ai_global_setting_field = ?",
                    value, LocalDateTime.now(), GROUP, field
                );
            } else {
                // 插入新记录
                jdbcTemplate.update(
                    "INSERT INTO sys_ai_global_setting (sys_ai_global_setting_group, sys_ai_global_setting_field, sys_ai_global_setting_value, sys_ai_global_setting_value_type, sys_ai_global_setting_description, sys_ai_global_setting_sort, sys_ai_global_setting_status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, 1, ?, ?)",
                    GROUP, field, value, valueType, desc, sort, LocalDateTime.now(), LocalDateTime.now()
                );
            }
        }
        log.info("AI 全局设置已初始化（白山智算 OpenAI 兼容），正在重建 ChatClient...");
        chatClientFactoryBean.rebuild();
        log.info("ChatClient 重建完成");
    }

    /**
     * 尝试创建 sys_ai_global_setting 表（兼容 H2 和 MySQL）。
     */
    private void tryCreateTable() {
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_ai_global_setting (
                    sys_ai_global_setting_id INT AUTO_INCREMENT PRIMARY KEY,
                    sys_ai_global_setting_group VARCHAR(64) NOT NULL DEFAULT 'default',
                    sys_ai_global_setting_field VARCHAR(128) NOT NULL,
                    sys_ai_global_setting_value TEXT,
                    sys_ai_global_setting_value_type VARCHAR(32) NOT NULL DEFAULT 'STRING',
                    sys_ai_global_setting_description VARCHAR(512),
                    sys_ai_global_setting_sort INT NOT NULL DEFAULT 0,
                    sys_ai_global_setting_status INT NOT NULL DEFAULT 1,
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """);
            log.info("成功创建 sys_ai_global_setting 表");
        } catch (Exception ex) {
            log.error("创建 sys_ai_global_setting 表失败: {}", ex.getMessage());
        }
    }
}
