package com.chua.starter.panel.test;

import com.chua.starter.ai.support.chat.ChatClientFactoryBean;
import com.chua.starter.ai.support.chat.KnowledgeGraphClientFactoryBean;
import com.chua.starter.ai.support.entity.SysAiGlobalSetting;
import com.chua.starter.ai.support.model.SysAiGlobalSettingSaveRequest;
import com.chua.starter.ai.support.service.SysAiGlobalSettingService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 测试环境 AI 配置初始化。
 *
 * <p>写入 AI 全局设置（覆盖 SQL 种子数据），清除 Redis 缓存后重建 ChatClient。</p>
 */
@Component
@ConditionalOnProperty(prefix = "panel", name = "ai-enabled", havingValue = "true", matchIfMissing = false)
public class AiSettingInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AiSettingInitializer.class);
    private static final String GROUP = "runtime";
    private final ChatClientFactoryBean chatClientFactoryBean;
    private final KnowledgeGraphClientFactoryBean knowledgeGraphClientFactoryBean;
    private final SysAiGlobalSettingService settingService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

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

    public AiSettingInitializer(@Qualifier("&chatClient") ChatClientFactoryBean chatClientFactoryBean,
                                @Qualifier("&knowledgeGraphClient") KnowledgeGraphClientFactoryBean knowledgeGraphClientFactoryBean,
                                SysAiGlobalSettingService settingService,
                                JdbcTemplate jdbcTemplate) {
        this.chatClientFactoryBean = chatClientFactoryBean;
        this.knowledgeGraphClientFactoryBean = knowledgeGraphClientFactoryBean;
        this.settingService = settingService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 检查表是否存在
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(1) FROM sys_ai_global_setting WHERE 1=0", Integer.class);
        } catch (Exception e) {
            log.warn("sys_ai_global_setting 表不存在（{}），尝试自动创建...", e.getMessage());
            tryCreateTable();
        }

        // 先清理 runtime 组旧配置，确保我们的设置生效（覆盖 SQL 种子数据）
        jdbcTemplate.update("DELETE FROM sys_ai_global_setting WHERE sys_ai_global_setting_group = ?", GROUP);

        // 使用 replace() 写入并通过 @CacheEvict 正确清除 Redis 缓存
        List<SysAiGlobalSetting> settingEntities = new ArrayList<>();
        for (Object[] row : SETTINGS) {
            SysAiGlobalSetting entity = new SysAiGlobalSetting();
            entity.setSysAiGlobalSettingGroup(GROUP);
            entity.setSysAiGlobalSettingField((String) row[0]);
            entity.setSysAiGlobalSettingValue((String) row[1]);
            entity.setSysAiGlobalSettingValueType((String) row[2]);
            entity.setSysAiGlobalSettingDescription((String) row[3]);
            entity.setSysAiGlobalSettingSort((int) row[4]);
            entity.setSysAiGlobalSettingStatus(1);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            settingEntities.add(entity);
        }

        SysAiGlobalSettingSaveRequest request = new SysAiGlobalSettingSaveRequest();
        request.setSysAiGlobalSettingGroup(GROUP);
        request.setSettings(settingEntities);

        try {
            boolean saved = settingService.replace(request);
            log.info("通过 service.replace() 写入 AI 设置并清除缓存: success={}", saved);
        } catch (Exception e) {
            log.warn("service.replace() 失败，回退到 JDBC 写入: {}", e.getMessage());
            // fallback: 直接 JDBC 写入 + 手动清除缓存
            for (Object[] row : SETTINGS) {
                jdbcTemplate.update(
                    "INSERT INTO sys_ai_global_setting (sys_ai_global_setting_group, sys_ai_global_setting_field, sys_ai_global_setting_value, sys_ai_global_setting_value_type, sys_ai_global_setting_description, sys_ai_global_setting_sort, sys_ai_global_setting_status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, 1, ?, ?)",
                    GROUP, row[0], row[1], row[2], row[3], row[4], LocalDateTime.now(), LocalDateTime.now()
                );
            }
            clearRedisAiSettingCache();
        }

        // 验证 DB 写入是否成功
        List<Map<String, Object>> written = jdbcTemplate.queryForList(
            "SELECT sys_ai_global_setting_field, sys_ai_global_setting_value FROM sys_ai_global_setting WHERE sys_ai_global_setting_group = ?",
            GROUP);
        log.info("DB 中 runtime 组配置项共 {} 条:", written.size());
        for (Map<String, Object> row : written) {
            log.info("  >> field={}, value={}", row.get("sys_ai_global_setting_field"), row.get("sys_ai_global_setting_value"));
        }

        // rebuild
        try {
            chatClientFactoryBean.rebuild();
            log.info("ChatClient 重建完成");

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

    private void clearRedisAiSettingCache() {
        if (stringRedisTemplate != null) {
            try {
                // 使用更宽泛的模式匹配 Redis 缓存 key
                Set<String> keys = stringRedisTemplate.keys("*sys:ai:global-setting*");
                if (keys != null && !keys.isEmpty()) {
                    stringRedisTemplate.delete(keys);
                    log.info("已清除 {} 个 AI 配置 Redis 缓存 key", keys.size());
                }
                // 同时尝试带 cache: 前缀的模式
                Set<String> cacheKeys = stringRedisTemplate.keys("cache:*sys:ai:global-setting*");
                if (cacheKeys != null && !cacheKeys.isEmpty()) {
                    stringRedisTemplate.delete(cacheKeys);
                    log.info("已清除 {} 个带 cache: 前缀的 Redis 缓存 key", cacheKeys.size());
                }
            } catch (Exception e) {
                log.warn("StringRedisTemplate 清除缓存失败(非致命): {}", e.getMessage());
            }
        }
    }

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
