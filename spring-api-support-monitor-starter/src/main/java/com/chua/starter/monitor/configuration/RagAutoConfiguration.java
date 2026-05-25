package com.chua.starter.monitor.configuration;

import com.chua.common.support.ai.ChatClient;
import com.chua.common.support.ai.DetectionConfiguration;
import com.chua.common.support.ai.bigmodel.BigModelClient;
import com.chua.common.support.ai.bigmodel.BigModelSetting;
import com.chua.common.support.ai.RagService;
import com.chua.common.support.ai.rag.RagDocumentManager;
import com.chua.common.support.ai.rag.RagEngine;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.text.splitter.RecursiveCharacterTextSplitter;
import com.chua.common.support.text.splitter.TextSplitter;
import com.chua.starter.ai.support.service.SysAiGlobalSettingService;
import com.chua.starter.monitor.properties.RagProperties;
import com.chua.starter.monitor.service.GraphRagEnhancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * RAG 模块自动装配。
 *
 * @author CH
 */
@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = RagProperties.PRE, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RagProperties.class)
public class RagAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RagService ragService(RagProperties properties) {
        DetectionConfiguration.DetectionConfigurationBuilder builder = DetectionConfiguration.builder()
                .engine(properties.getEmbeddingEngine());

        DetectionConfiguration config = builder.build();

        if (!properties.getVectorStorage().isBlank()) {
            config.option().put("vectorStorage", properties.getVectorStorage());
        }

        return RagService.auto(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public RagEngine ragEngine(RagService ragService, SysAiGlobalSettingService globalSettingService,
                               RagProperties properties, ObjectProvider<GraphRagEnhancer> enhancerProvider) {
        DetectionConfiguration.DetectionConfigurationBuilder builder = DetectionConfiguration.builder()
                .engine(properties.getEmbeddingEngine());
        DetectionConfiguration config = builder.build();
        if (!properties.getVectorStorage().isBlank()) {
            config.option().put("vectorStorage", properties.getVectorStorage());
        }
        RagEngine engine = new RagEngine(config);

        // 注入全局 ChatClient（从 sys_ai_global_setting runtime 组读取 LLM 配置）
        try {
            com.chua.common.support.ai.ChatClient globalChatClient = createGlobalChatClient(globalSettingService);
            if (globalChatClient != null) {
                engine.setChatClient(globalChatClient);
                log.info("[RagAutoConfiguration] 全局 ChatClient 已注入到 RAG 引擎");
            }
        } catch (Exception e) {
            log.warn("[RagAutoConfiguration] 无法创建全局 ChatClient，RAG 问答将降级为仅返回检索内容: {}", e.getMessage());
        }

        // GraphRAG 增强：注入知识图谱上下文提供者
        if (properties.isUseGraph()) {
            GraphRagEnhancer enhancer = enhancerProvider.getIfAvailable();
            if (enhancer != null) {
                ragService.setGraphContextProvider(enhancer);
                log.info("[RagAutoConfiguration] GraphRAG 增强已启用");
            } else {
                log.warn("[RagAutoConfiguration] GraphRAG 增强已启用但找不到 GraphRagEnhancer Bean");
            }
        }

        log.info("[RagAutoConfiguration] 初始化 RagEngine: engine={}, useGraph={}",
                properties.getEmbeddingEngine(), properties.isUseGraph());
        return engine;
    }

    /**
     * 从 sys_ai_global_setting runtime 组读取 LLM 配置，创建 common ChatClient。
     */
    private com.chua.common.support.ai.ChatClient createGlobalChatClient(SysAiGlobalSettingService settingService) {
        String provider = settingService.getValue("runtime", "provider").orElse("");
        String factory = settingService.getValue("runtime", "factory").orElse("default");
        String baseUrl = settingService.getValue("runtime", "provider_base_url").orElse("");
        String apiKey = settingService.getValue("runtime", "provider_api_key").orElse("");
        String defaultModel = settingService.getValue("runtime", "default_model").orElse("");

        if (provider.isBlank()) {
            log.warn("[RagAutoConfiguration] 全局设置中未配置 LLM provider，跳过 ChatClient 注入");
            return null;
        }

        BigModelSetting setting = BigModelSetting.builder()
                .provider(provider)
                .baseUrl(baseUrl)
                .appKey(apiKey)
                .build();
        BigModelClient.normalize(setting);

        ServiceProvider<ChatClient.Factory> sp = ServiceProvider.of(ChatClient.Factory.class);
        ChatClient.Factory factoryInstance = "default".equals(factory) ? sp.getDefault() : sp.getNewExtension(factory);
        if (factoryInstance == null) {
            factoryInstance = sp.getDefault();
        }
        if (factoryInstance == null) {
            log.warn("[RagAutoConfiguration] 找不到 ChatClient.Factory SPI 实现");
            return null;
        }

        ChatClient chatClient = factoryInstance.create(setting);
        if (!defaultModel.isBlank()) {
            chatClient.model(defaultModel);
        }
        return chatClient;
    }

    @Bean
    @ConditionalOnMissingBean
    public RagDocumentManager ragDocumentManager(RagProperties properties, RagEngine ragEngine) throws Exception {
        TextSplitter textSplitter = new RecursiveCharacterTextSplitter(
                properties.getChunkSize(), properties.getChunkOverlap());
        return new RagDocumentManager(properties.getUploadDir(), ragEngine.getKnowledgeClient(), textSplitter);
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalSettingInitializer globalSettingInitializer(SysAiGlobalSettingService globalSettingService) {
        return new GlobalSettingInitializer(globalSettingService);
    }
}
