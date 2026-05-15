package com.chua.starter.monitor.configuration;

import com.chua.common.support.ai.DetectionConfiguration;
import com.chua.common.support.ai.rag.RagDocumentManager;
import com.chua.common.support.ai.rag.RagEngine;
import com.chua.common.support.text.splitter.RecursiveCharacterTextSplitter;
import com.chua.common.support.text.splitter.TextSplitter;
import com.chua.starter.ai.support.service.SysAiGlobalSettingService;
import com.chua.starter.monitor.controller.rag.RagController;
import com.chua.starter.monitor.properties.RagProperties;
import lombok.extern.slf4j.Slf4j;
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
    public RagEngine ragEngine(RagProperties properties) {
        DetectionConfiguration.DetectionConfigurationBuilder builder = DetectionConfiguration.builder()
                .engine(properties.getEmbeddingEngine());

        if (!properties.getLlmApiKey().isBlank()) {
            builder.apiKey(properties.getLlmApiKey());
        }
        if (!properties.getLlmBaseUrl().isBlank()) {
            builder.endpoint(properties.getLlmBaseUrl());
        }
        if (!properties.getLlmModel().isBlank()) {
            builder.modelName(properties.getLlmModel());
        }

        DetectionConfiguration config = builder.build();

        // 选项通过配置对象的 option map 设置
        if (!properties.getVectorStorage().isBlank()) {
            config.option().put("vectorStorage", properties.getVectorStorage());
        }
        if (!properties.getLlmProvider().isBlank()) {
            config.option().put("provider", properties.getLlmProvider());
        }

        log.info("[RagAutoConfiguration] 初始化 RagEngine: engine={}, llmProvider={}",
                properties.getEmbeddingEngine(), properties.getLlmProvider());
        return new RagEngine(config);
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
    public RagController ragController(RagEngine ragEngine, RagDocumentManager ragDocumentManager, RagProperties properties) {
        return new RagController(ragEngine, ragDocumentManager, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalSettingInitializer globalSettingInitializer(SysAiGlobalSettingService globalSettingService) {
        return new GlobalSettingInitializer(globalSettingService);
    }
}
