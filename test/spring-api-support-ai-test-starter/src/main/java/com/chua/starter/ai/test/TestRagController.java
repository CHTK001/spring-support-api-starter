package com.chua.starter.ai.test;

import com.chua.deeplearning.support.ai.KnowledgeGraphEntity;
import com.chua.deeplearning.support.ai.RagService;
import com.chua.deeplearning.support.ai.rag.RagDocument;
import com.chua.deeplearning.support.ai.rag.RagDocumentManager;
import com.chua.deeplearning.support.ai.rag.RagEngine;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.ai.support.configuration.RagProperties;
import com.chua.starter.ai.support.service.impl.KnowledgeGraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
   * RAG 测试控制器 — 覆盖文档上传 + 向量检索 + 图计算rag 知识图谱增强。
 *
 * @author CH
 * @since 4.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v2/test/rag")
@RequiredArgsConstructor
@Tag(name = "RAG 测试", description = "整合 RAG + GraphRAG 的完整测试端点")
@ConditionalOnProperty(prefix = "app.rag", name = "test-enabled", havingValue = "true", matchIfMissing = true)
public class TestRagController {

    /** RAG引擎 */
    private final RagEngine ragEngine;
    /** 文档管理器 */
    private final RagDocumentManager documentManager;
    /** RAG属性 */
    private final RagProperties ragProperties;
    /** Knowledgegraph服务 */
    private final KnowledgeGraphService knowledgeGraphService;
    /** RAG服务 */
    private final RagService ragService;

    @Value("${app.rag.use-graph:false}")
    /** usegraph */
    private boolean useGraph;

    @Value("${app.rag.graph-max-tokens:1000}")
    /** 图计算最大值令牌 */
    private int graphMaxTokens;

    @PostConstruct
    /** 初始化 */
    public void init() {
        if (useGraph) {
            ragService.setGraphContextProvider((query, documentIds, chunkContents) -> {
                if (chunkContents == null || chunkContents.isEmpty()) {
                    return null;
                }

                // 提取关键词 → 搜索实体 → 邻居扩展
                Set<String> keywords = extractKeywords(chunkContents);
                if (keywords.isEmpty()) {
                    return null;
                }

                Set<String> matchedUids = new LinkedHashSet<>();
                Map<String, String> uidToName = new LinkedHashMap<>();

                for (String keyword : keywords) {
                    if (matchedUids.size() >= 30) {
                        break;
                    }
                    try {
                        var page = knowledgeGraphService.listEntities(keyword, null, null, 1, 5);
                        for (var entity : page.getRecords()) {
                            String uid = entity.getSysAiKnowledgeGraphEntityUid();
                            if (uid != null && matchedUids.add(uid)) {
                                uidToName.put(uid, entity.getSysAiKnowledgeGraphEntityName());
                            }
                        }
                    } catch (Exception ignored) {}
                }

                if (matchedUids.isEmpty()) {
                    return null;
                }

                StringBuilder sb = new StringBuilder();
                int charLimit = graphMaxTokens * 4;

                sb.append("实体列表：\n");
                matchedUids.forEach(uid -> {
                    if (sb.length() >= charLimit) {
                        return;
                    }
                    sb.append("- ").append(uidToName.getOrDefault(uid, uid)).append("\n");
                });
                sb.append("\n关联关系：\n");
                for (String uid : matchedUids) {
                    if (sb.length() >= charLimit) {
                        break;
                    }
                    try {
                        List<KnowledgeGraphEntity> neighbors = knowledgeGraphService.getNeighbors(uid);
                        String name = uidToName.getOrDefault(uid, uid);
                        for (var n : neighbors) {
                            if (sb.length() >= charLimit) {
                                break;
                            }
                            sb.append("- ").append(name).append(" → ").append(n.name()).append("\n");
                        }
                    } catch (Exception ignored) {}
                }

                String result = sb.toString();
                if (result.isBlank()) {
                    return null;
                }
                log.info("[TestRag] GraphRAG context: {} entities, {} chars", matchedUids.size(), result.length());
                return result;
            });
            log.info("[TestRag] GraphRAG 增强已注入到 RAG 服务");
        }
    }

    // ── 文档管理 ─────────────────────────────────────────

    @Operation(summary = "上传文档", description = "上传文件并自动索引，启用 GraphRAG 时自动抽取实体")
    @PostMapping(value = "/document/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    /**
      * upload文档
     * @param file 文件
     * @param libraryId 图书馆id
     */
    public ReturnResult<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String libraryId) {
        if (file.isEmpty()) {
            return ReturnResult.error("上传文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            return ReturnResult.error("文件名不能为空");
        }

        try {
            RagDocument doc = documentManager.uploadDocument(fileName, file.getBytes());
            if ("FAILED".equals(doc.status())) {
                return ReturnResult.error(doc.errorMessage());
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("document", doc);

 // 图计算rag：自动抽取实体
            if (useGraph) {
                try {
                    String text = knowledgeGraphService.extractTextFromFile(file);
                    if (!text.isBlank()) {
                        int entityCount = knowledgeGraphService.extractAndSaveEntities(text, null).size();
                        result.put("extractedEntities", entityCount);
                        log.info("[TestRag] 文档[{}]自动抽取实体 {} 个", fileName, entityCount);
                    }
                } catch (Exception e) {
                    log.warn("[TestRag] 实体抽取失败(不影响上传): {}", e.getMessage());
                    result.put("extractError", e.getMessage());
                }
            }

            return ReturnResult.ok(result);
        } catch (IOException e) {
            log.error("[TestRag] 上传文件失败: {}", e.getMessage());
            return ReturnResult.error("文件读取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "文档列表")
    @GetMapping("/document/list")
    /**
      * 列表文档
     * @param page page
     * @param pageSize page大小
     * @param libraryId 图书馆id
     */
    public ReturnResult<RagDocumentManager.PaginatedResult> listDocuments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String libraryId) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }
        return ReturnResult.ok(documentManager.listDocuments(page, pageSize));
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/document/{id}")
    /**
     * 删除文档
     *
     * @param id id
     * @return 删除文档的结果
     */
    public ReturnResult<Boolean> deleteDocument(@PathVariable String id) {
        boolean deleted = documentManager.deleteDocument(id);
        return deleted ? ReturnResult.ok(true) : ReturnResult.error("文档不存在");
    }

    // ── 问答 ─────────────────────────────────────────────

    @Operation(summary = "RAG 问答", description = "基于已索引文档进行问答（useGraph=true 时自动注入图谱上下文）")
    @PostMapping("/query")
    /**
     * 查询
     *
     * @param body 主体
     * @return 查询的结果
     */
    public ReturnResult<Map<String, Object>> query(@RequestBody Map<String, Object> body) {
        String query = body.getOrDefault("query", "").toString();
        if (query.isBlank()) {
            return ReturnResult.error("查询内容不能为空");
        }

        int topK = body.containsKey("topK") ? Integer.parseInt(body.get("topK").toString()) : ragProperties.getDefaultTopK();
        double threshold = body.containsKey("threshold") ? Double.parseDouble(body.get("threshold").toString()) : ragProperties.getDefaultSimilarityThreshold();

        try {
            RagEngine.RagResult result = ragEngine.query(query, topK, threshold);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("answer", result.answer());
            resp.put("sources", result.sources().stream().map(s -> Map.of(
                    "documentId", s.documentId(),
                    "content", s.document() != null ? s.document().content() : "",
                    "score", s.score()
            )).toList());
            resp.put("useGraph", useGraph);
            return ReturnResult.ok(resp);
        } catch (Exception e) {
            log.error("[TestRag] 问答失败: {}", e.getMessage());
            return ReturnResult.error("问答失败: " + e.getMessage());
        }
    }

    // ── 状态 ─────────────────────────────────────────────

    @Operation(summary = "状态")
    @GetMapping("/status")
    /**
     * 状态
     *
     * @return 状态的结果
     */
    public ReturnResult<Map<String, Object>> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("engine", ragProperties.getEmbeddingEngine());
        status.put("uploadDir", ragProperties.getUploadDir());
        status.put("defaultTopK", ragProperties.getDefaultTopK());
        status.put("useGraph", useGraph);
        try {
            status.put("knowledgeClientReady", ragEngine.getKnowledgeClient() != null);
        } catch (Exception e) {
            status.put("knowledgeClientReady", false);
        }
        try {
            Object count = knowledgeGraphService.getStatistics(null).getOrDefault("entityCount", 0L);
            status.put("knowledgeGraphEntityCount", count instanceof Number ? ((Number) count).longValue() : 0);
        } catch (Exception e) {
            status.put("knowledgeGraphEntityCount", 0);
        }
        return ReturnResult.ok(status);
    }

    // ── 辅助 ─────────────────────────────────────────────

    /**
     * extractkeywords
     *
     * @param contents 内容
     * @return extractKeywords的结果
     */
    private Set<String> extractKeywords(List<String> contents) {
        Set<String> keywords = new LinkedHashSet<>();
        for (String content : contents) {
            if (content == null || content.isBlank()) {
                continue;
            }
            String[] parts = content.split("[，。！？、；：''（）【】\\[\\]\\s,.!?;:\"()\\-]+");
            for (String part : parts) {
                part = part.trim();
                if (part.length() >= 2 && part.length() <= 30 && !part.matches("\\d+(\\.\\d+)?")) {
                    keywords.add(part);
                }
            }
        }
        return keywords;
    }
}
