package com.chua.starter.spider.test;

import com.chua.starter.ai.support.chat.ChatClientFactoryBean;
import com.chua.starter.ai.support.chat.KnowledgeGraphClientFactoryBean;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 测试环境 AI 配置初始化。
 *
 * <p>在 H2 测试数据库中写入 AI 全局设置，使 AI 流程生成功能可在测试环境中正常使用。</p>
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.datasource.driverClassName", havingValue = "org.h2.Driver")
public class AiSettingInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AiSettingInitializer.class);
    private static final String GROUP = "runtime";
    private final ChatClientFactoryBean chatClientFactoryBean;
    private final KnowledgeGraphClientFactoryBean knowledgeGraphClientFactoryBean;

    /**
     * Z.AI（智谱生态）— 提供 GLM-4V-Flash 免费视觉模型，OpenAI 兼容接口。
     * API Key 来自 SQL 种子数据 V1.0__initdata_ai.sql:271
     */
    private static final Object[][] SETTINGS = {
        {"enabled",           "true",                                     "BOOLEAN", "是否启用 AI ChatClient",                                    5},
        {"factory",           "default",                                  "STRING",  "ChatClient SPI 工厂名称",                                     8},
        {"provider",          "openai",                                   "STRING",  "默认 LLM 提供商(Z.AI/OpenAI 兼容)",                          9},
        {"default_model",     "GLM-4V-Flash",                             "STRING",  "默认聊天模型编码(GLm-4V-Flash 免费视觉)",                     10},
        {"provider_base_url", "https://api.z.ai/api/paas/v4",             "STRING", "Z.AI 基础地址(智谱生态，OpenAI 兼容)",                       70},
        {"provider_api_key",  "acb7372b94c3428bb75c03f695fcb2ee.p82pPKkxRovK8dqz", "STRING", "Z.AI API Key(来自 SQL 种子数据)",                    75},
        {"system_prompt",     "",                                         "STRING",  "默认系统提示词",                                              15},
        {"temperature",       "0.1",                                      "NUMBER",  "默认采样温度",                                                20},
        {"max_tokens",        "16384",                                    "NUMBER",  "默认最大输出 token",                                          30},
        {"timeout_millis",    "60000",                                    "NUMBER",  "默认超时时间(毫秒)",                                          35},
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
        }

        // 先清理 runtime 组旧配置，确保我们的设置生效（覆盖 SQL 种子数据）
        jdbcTemplate.update("DELETE FROM sys_ai_global_setting WHERE sys_ai_global_setting_group = ?", GROUP);

        for (Object[] row : SETTINGS) {
            String field = (String) row[0];
            String value = (String) row[1];
            String valueType = (String) row[2];
            String desc = (String) row[3];
            int sort = (int) row[4];
            jdbcTemplate.update(
                "INSERT INTO sys_ai_global_setting (sys_ai_global_setting_group, sys_ai_global_setting_field, sys_ai_global_setting_value, sys_ai_global_setting_value_type, sys_ai_global_setting_description, sys_ai_global_setting_sort, sys_ai_global_setting_status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, 1, ?, ?)",
                GROUP, field, value, valueType, desc, sort, LocalDateTime.now(), LocalDateTime.now()
            );
        }
        log.info("AI 全局设置已初始化（覆盖 SQL 种子数据），正在重建 ChatClient...");

        // 验证 DB 写入是否成功
        List<Map<String, Object>> written = jdbcTemplate.queryForList(
            "SELECT sys_ai_global_setting_field, sys_ai_global_setting_value FROM sys_ai_global_setting WHERE sys_ai_global_setting_group = ?",
            GROUP);
        log.info("DB 中 runtime 组配置项共 {} 条:", written.size());
        for (Map<String, Object> row : written) {
            log.info("  >> field={}, value={}", row.get("sys_ai_global_setting_field"), row.get("sys_ai_global_setting_value"));
        }

        // rebuild 前先注册回调拦截，打印实际构建参数
        try {
            chatClientFactoryBean.rebuild();
            log.info("ChatClient 重建完成");

            // 尝试调一次 chat 验证配置，提前暴露 apiKey 等问题
            com.chua.starter.ai.support.chat.ChatClient client = chatClientFactoryBean.getObject();
            if (client != null) {
                log.info("ChatClient 类型: {}, provider={}, model={}, endpoint={}",
                    client.getClass().getSimpleName(),
                    client.getProvider(),
                    client.getDefaultModel(),
                    client.getEndpoint());
            }
        } catch (Exception e) {
            log.error("ChatClient 重建失败: {}", e.getMessage(), e);
        }

        log.info("正在重建 KnowledgeGraph Client...");
        try {
            knowledgeGraphClientFactoryBean.rebuild();
            log.info("KnowledgeGraph Client 重建完成");
        } catch (Exception e) {
            log.error("KnowledgeGraph Client 重建失败: {}", e.getMessage(), e);
        }
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
