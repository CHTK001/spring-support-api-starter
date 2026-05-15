package com.chua.starter.monitor.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RAG 配置属性。
 *
 * @author CH
 */
@Data
@ConfigurationProperties(prefix = RagProperties.PRE)
public class RagProperties {

    public static final String PRE = "app.rag";

    /** 是否启用 RAG */
    private boolean enabled = true;

    /** 向量引擎（memory / openai / siliconflow） */
    private String embeddingEngine = "memory";

    /** 向量存储（jvector / milvus / mysql / memory） */
    private String vectorStorage = "jvector";

    /** LLM 提供商（openai / siliconflow / ollama 等） */
    private String llmProvider = "";

    /** LLM 模型名称 */
    private String llmModel = "";

    /** LLM API Key */
    private String llmApiKey = "";

    /** LLM API Base URL */
    private String llmBaseUrl = "";

    /** 分块大小 */
    private int chunkSize = 1000;

    /** 分块重叠 */
    private int chunkOverlap = 200;

    /** 默认检索数量 */
    private int defaultTopK = 5;

    /** 默认相似度阈值 */
    private double defaultSimilarityThreshold = 0.7;

    /** 上传目录 */
    private String uploadDir = "./data/rag/uploads";
}
