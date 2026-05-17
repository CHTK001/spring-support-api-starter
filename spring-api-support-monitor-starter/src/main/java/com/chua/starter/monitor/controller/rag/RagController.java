package com.chua.starter.monitor.controller.rag;

import com.chua.common.support.ai.rag.RagDocument;
import com.chua.common.support.ai.rag.RagDocumentManager;
import com.chua.common.support.ai.rag.RagEngine;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.ai.support.service.impl.KnowledgeGraphService;
import com.chua.starter.monitor.properties.RagProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * RAG REST 控制器。
 *
 * @author CH
 */
@Slf4j
@RestController
@RequestMapping("/v2/ai/rag")
@RequiredArgsConstructor
@Tag(name = "RAG 知识库", description = "RAG 文档管理、问答和配置接口")
public class RagController {

    private final RagEngine ragEngine;
    private final RagDocumentManager documentManager;
    private final RagProperties ragProperties;
    private final KnowledgeGraphService knowledgeGraphService;

    // ── 文档管理 ─────────────────────────────────────────

    @Operation(summary = "上传文档", description = "上传文件并自动索引到知识库")
    @PostMapping(value = "/document/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReturnResult<RagDocument> uploadDocument(@RequestParam("file") MultipartFile file) {
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

            // GraphRAG: 上传文档后自动抽取实体和关系
            if (ragProperties.isUseGraph()) {
                try {
                    String text = knowledgeGraphService.extractTextFromFile(file);
                    if (!text.isBlank()) {
                        knowledgeGraphService.extractAndSaveEntities(text, null);
                        log.info("[RagController] GraphRAG 自动抽取实体完成: {}", fileName);
                    }
                } catch (Exception e) {
                    log.warn("[RagController] GraphRAG 实体抽取失败(不影响上传): {}", e.getMessage());
                }
            }

            return ReturnResult.ok(doc);
        } catch (IOException e) {
            log.error("[RagController] 上传文件失败: {}", e.getMessage());
            return ReturnResult.error("文件读取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "文档列表", description = "分页查询已上传文档")
    @GetMapping("/document/list")
    public ReturnResult<RagDocumentManager.PaginatedResult> listDocuments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 20;
        if (pageSize > 100) pageSize = 100;
        return ReturnResult.ok(documentManager.listDocuments(page, pageSize));
    }

    @Operation(summary = "删除文档", description = "删除文档及其向量索引")
    @DeleteMapping("/document/{id}")
    public ReturnResult<Boolean> deleteDocument(@PathVariable String id) {
        boolean deleted = documentManager.deleteDocument(id);
        return deleted ? ReturnResult.ok(true) : ReturnResult.error("文档不存在");
    }

    // ── 问答 ─────────────────────────────────────────────

    @Operation(summary = "RAG 问答", description = "基于已索引文档进行问答，返回答案和引用来源")
    @PostMapping("/query")
    public ReturnResult<Map<String, Object>> query(
            @RequestBody Map<String, Object> body) {
        String query = body.getOrDefault("query", "").toString();
        if (query.isBlank()) {
            return ReturnResult.error("查询内容不能为空");
        }

        int topK = body.containsKey("topK")
                ? Integer.parseInt(body.get("topK").toString())
                : ragProperties.getDefaultTopK();
        double threshold = body.containsKey("threshold")
                ? Double.parseDouble(body.get("threshold").toString())
                : ragProperties.getDefaultSimilarityThreshold();

        try {
            RagEngine.RagResult result = ragEngine.query(query, topK, threshold);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("answer", result.answer());
            resp.put("sources", result.sources().stream().map(s -> Map.of(
                    "documentId", s.documentId(),
                    "content", s.document() != null ? s.document().content() : "",
                    "score", s.score()
            )).toList());
            return ReturnResult.ok(resp);
        } catch (Exception e) {
            log.error("[RagController] 问答失败: {}", e.getMessage());
            return ReturnResult.error("问答失败: " + e.getMessage());
        }
    }

    // ── 状态与管理 ───────────────────────────────────────

    @Operation(summary = "服务状态", description = "获取 RAG 服务运行状态")
    @GetMapping("/status")
    public ReturnResult<Map<String, Object>> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("engine", ragProperties.getEmbeddingEngine());
        status.put("llmProvider", "global (使用全局 LLM 配置)");
        status.put("uploadDir", ragProperties.getUploadDir());
        status.put("defaultTopK", ragProperties.getDefaultTopK());
        status.put("defaultSimilarityThreshold", ragProperties.getDefaultSimilarityThreshold());
        try {
            var client = ragEngine.getKnowledgeClient();
            status.put("knowledgeClientReady", client != null);
        } catch (Exception e) {
            status.put("knowledgeClientReady", false);
            status.put("error", e.getMessage());
        }
        return ReturnResult.ok(status);
    }

    @Operation(summary = "重置索引", description = "清空向量存储并重新索引所有文档")
    @PostMapping("/reset")
    public ReturnResult<Integer> reset() {
        int count = documentManager.resetAndReindex();
        return ReturnResult.ok(count, "重新索引完成，共 " + count + " 个文档");
    }
}
