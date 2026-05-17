package com.chua.starter.monitor.service;

import com.chua.common.support.ai.KnowledgeGraphEntity;
import com.chua.common.support.ai.rag.GraphContextProvider;
import com.chua.starter.ai.support.service.impl.KnowledgeGraphService;
import com.chua.starter.monitor.properties.RagProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * GraphRAG 增强器。
 * <p>
 * 实现 {@link GraphContextProvider} 接口，从知识图谱中检索与文档片段匹配的实体及关系，
 * 为 RAG 问答提供图谱上下文增强。
 * 当图谱数据为空或未匹配到实体时，返回 null 自动降级为纯向量 RAG。
 * </p>
 *
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraphRagEnhancer implements GraphContextProvider {

    private final KnowledgeGraphService knowledgeGraphService;
    private final RagProperties ragProperties;

    @Override
    public String getContext(String query, List<String> documentIds, List<String> chunkContents) {
        if (chunkContents == null || chunkContents.isEmpty()) {
            return null;
        }

        // 1. 从文档片段中提取关键词
        Set<String> keywords = extractKeywords(chunkContents);
        if (keywords.isEmpty()) {
            log.debug("[GraphRagEnhancer] 未能从文档片段中提取到关键词");
            return null;
        }

        // 2. 按关键词搜索匹配的实体
        Set<String> matchedUids = new LinkedHashSet<>();
        Map<String, String> uidToName = new LinkedHashMap<>();
        int maxEntitySearch = 30;
        int maxPerKeyword = 5;

        for (String keyword : keywords) {
            if (matchedUids.size() >= maxEntitySearch) break;
            try {
                var page = knowledgeGraphService.listEntities(keyword, null, null, 1, maxPerKeyword);
                for (var entity : page.getRecords()) {
                    String uid = entity.getSysAiKnowledgeGraphEntityUid();
                    if (uid != null && matchedUids.add(uid)) {
                        uidToName.put(uid, entity.getSysAiKnowledgeGraphEntityName());
                    }
                }
            } catch (Exception e) {
                log.debug("[GraphRagEnhancer] 关键词[{}]搜索实体失败: {}", keyword, e.getMessage());
            }
        }

        if (matchedUids.isEmpty()) {
            log.debug("[GraphRagEnhancer] 未找到匹配的实体");
            return null;
        }

        // 3. 构建图谱上下文（限制 token 数量）
        StringBuilder sb = new StringBuilder();
        int charLimit = ragProperties.getGraphMaxTokens() * 4;

        sb.append("匹配实体：\n");
        for (String uid : matchedUids) {
            if (sb.length() >= charLimit) break;
            sb.append("- ").append(uidToName.getOrDefault(uid, uid)).append("\n");
        }

        sb.append("\n关联关系：\n");
        for (String uid : matchedUids) {
            if (sb.length() >= charLimit) break;
            try {
                List<KnowledgeGraphEntity> neighbors = knowledgeGraphService.getNeighbors(uid);
                String name = uidToName.getOrDefault(uid, uid);
                for (var neighbor : neighbors) {
                    if (sb.length() >= charLimit) break;
                    sb.append("- ").append(name).append(" → ").append(neighbor.name()).append("\n");
                }
            } catch (Exception e) {
                log.debug("[GraphRagEnhancer] 实体[{}]邻居扩展失败: {}", uid, e.getMessage());
            }
        }

        String result = sb.toString();
        if (result.isBlank()) return null;

        log.info("[GraphRagEnhancer] 图谱上下文生成完成: {} 实体, {} 字符",
                matchedUids.size(), result.length());
        return result;
    }

    /**
     * 从文档片段中提取关键词。
     * <p>按标点符号和空白符拆分，过滤长度小于 2 和纯数字的词。</p>
     */
    private Set<String> extractKeywords(List<String> contents) {
        Set<String> keywords = new LinkedHashSet<>();
        for (String content : contents) {
            if (content == null || content.isBlank()) continue;
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
