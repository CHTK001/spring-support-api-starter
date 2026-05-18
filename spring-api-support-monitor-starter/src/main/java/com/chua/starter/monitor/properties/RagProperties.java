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

    /** 向量引擎（memory / openai / siliconflow / onnx） */
    private String embeddingEngine = "memory";

    /** 向量存储（jvector / milvus / mysql / memory） */
    private String vectorStorage = "jvector";

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

    // ── GraphRAG 配置 ─────────────────────────────────────

    /** 是否启用 GraphRAG 增强（上传文档后自动抽取实体/关系，问答时注入图谱上下文） */
    private boolean useGraph = false;

    /** 实体扩展最大跳数，默认 2（一跳为直接邻居） */
    private int graphMaxHops = 2;

    /** 图谱上下文最大 token 数 */
    private int graphMaxTokens = 1000;
}
