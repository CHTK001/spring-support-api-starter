package com.chua.starter.spider.test;

import com.chua.starter.ai.support.chat.ChatClientFactoryBean;
import com.chua.starter.ai.support.chat.KnowledgeGraphClientFactoryBean;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 测试环境 AI 配置初始化。
 *
 * <p>写入 AI 全局设置（覆盖 SQL 种子数据），清除 Redis 缓存后重建 ChatClient。</p>
 */
@Component
public class AiSettingInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AiSettingInitializer.class);
    private static final String GROUP = "runtime";
    private final ChatClientFactoryBean chatClientFactoryBean;
    private final KnowledgeGraphClientFactoryBean knowledgeGraphClientFactoryBean;
    private final JdbcTemplate jdbcTemplate;

    /** 可选：注入 StringRedisTemplate 用于直接清除 Redis key */
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

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
        {"max_tokens",        "1024",                                     "NUMBER",  "默认最大输出 token(GLM-4V-Flash 限 1024)",                      30},
        {"timeout_millis",    "60000",                                    "NUMBER",  "默认超时时间(毫秒)",                                          35},
    };

    public AiSettingInitializer(ChatClientFactoryBean chatClientFactoryBean,
                                KnowledgeGraphClientFactoryBean knowledgeGraphClientFactoryBean,
                                JdbcTemplate jdbcTemplate) {
        this.chatClientFactoryBean = chatClientFactoryBean;
        this.knowledgeGraphClientFactoryBean = knowledgeGraphClientFactoryBean;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 启动时清理所有卡住的 RUNNING/WAITING 状态执行，防止并发锁
        try {
            int updated = jdbcTemplate.update(
                "UPDATE spider_execution SET spider_execution_status = 'FAILED' WHERE spider_execution_status IN ('RUNNING', 'WAITING')");
            if (updated > 0) {
                log.info("已清理 {} 条卡住的执行记录（RUNNING/WAITING → FAILED）", updated);
            }
            // 清空 spider_trace_snapshot 防止慢查询（包含 base64 截图数据）
            int snapshotCleaned = jdbcTemplate.update(
                "UPDATE spider_execution SET spider_trace_snapshot = NULL WHERE spider_trace_snapshot IS NOT NULL AND LENGTH(spider_trace_snapshot) > 10000");
            if (snapshotCleaned > 0) {
                log.info("已清空 {} 条执行记录的快照数据（防止慢查询）", snapshotCleaned);
            }
        } catch (Exception e) {
            log.debug("清理旧执行状态跳过（表可能不存在）: {}", e.getMessage());
        }

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
        log.info("AI 全局设置已初始化（覆盖 SQL 种子数据），正在清除 Redis 缓存并重建 ChatClient...");

        // 清除 Redis 缓存（SysAiGlobalSettingServiceImpl 使用 @Cacheable(cacheManager = "redis-cache--1")）
        // AiSettingInitializer 直接写 JDBC 绕过 Service 层，Redis 缓存不会自动清除
        clearRedisAiSettingCache();

        // 验证 DB 写入是否成功
        List<Map<String, Object>> written = jdbcTemplate.queryForList(
            "SELECT sys_ai_global_setting_field, sys_ai_global_setting_value FROM sys_ai_global_setting WHERE sys_ai_global_setting_group = ?",
            GROUP);
        log.info("DB 中 runtime 组配置项共 {} 条:", written.size());
        for (Map<String, Object> row : written) {
            log.info("  >> field={}, value={}", row.get("sys_ai_global_setting_field"), row.get("sys_ai_global_setting_value"));
        }

        // rebuild — 由于已清除缓存，resolve() 将读取真实 DB 值
        try {
            chatClientFactoryBean.rebuild();
            log.info("ChatClient 重建完成");

            // 验证配置
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
     * 清除 sys_ai_global_setting 相关缓存。
     * <p>
     * 尝试通过 CacheManager evict 及 StringRedisTemplate 直接删除 Redis key。
     * CacheName = redis-cache--1，customTenantedKeyGenerator 生成 "cache:..." 前缀 key。
     */
    private void clearRedisAiSettingCache() {
        if (stringRedisTemplate != null) {
            try {
                Set<String> keys = stringRedisTemplate.keys("cache:*sys:ai:global-setting*");
                if (keys != null && !keys.isEmpty()) {
                    stringRedisTemplate.delete(keys);
                    log.info("已清除 {} 个 AI 配置 Redis 缓存 key", keys.size());
                }
            } catch (Exception e) {
                log.warn("StringRedisTemplate 清除缓存失败(非致命): {}", e.getMessage());
            }
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
