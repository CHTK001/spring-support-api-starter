package com.chua.starter.datalake.service;

import com.chua.datalake.support.engine.PipelineLifecycle;
import com.chua.datalake.support.engine.ScGraph;
import com.chua.datalake.support.engine.ScGraphNode;
import com.chua.starter.datalake.model.PipelineCreateRequest;
import com.chua.starter.datalake.model.PipelineUpdateRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pipeline 业务逻辑 — 内存存储，后续可接入数据库
 */
@Slf4j
public class PipelineService {

    private final Map<String, ScGraph> graphs = new ConcurrentHashMap<>();
    private final Map<String, PipelineLifecycle> lifecycles = new ConcurrentHashMap<>();

    public List<ScGraph> list() {
        return new ArrayList<>(graphs.values());
    }

    public ScGraph getById(String id) {
        return graphs.get(id);
    }

    public PipelineLifecycle getLifecycle(String id) {
        return lifecycles.getOrDefault(id, PipelineLifecycle.STOPPED);
    }

    public ScGraph create(PipelineCreateRequest request) {
        ScGraph graph = new ScGraph();
        graph.setId(request.getId());
        graph.setName(request.getName());
        graph.setVersion(request.getVersion());
        graph.setDescription(request.getDescription());
        graph.setNodes(parseNodes(request.getNodes()));
        graph.setSinks(request.getSinks() != null ? request.getSinks() : new HashMap<>());
        graphs.put(graph.getId(), graph);
        lifecycles.put(graph.getId(), PipelineLifecycle.STOPPED);
        log.info("Pipeline created: {}", graph.getId());
        return graph;
    }

    public ScGraph update(PipelineUpdateRequest request) {
        ScGraph existing = graphs.get(request.getId());
        if (existing == null) {
            return null;
        }
        if (request.getName() != null) existing.setName(request.getName());
        if (request.getVersion() != null) existing.setVersion(request.getVersion());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getNodes() != null) existing.setNodes(parseNodes(request.getNodes()));
        if (request.getSinks() != null) existing.setSinks(request.getSinks());
        log.info("Pipeline updated: {}", existing.getId());
        return existing;
    }

    public boolean delete(String id) {
        ScGraph removed = graphs.remove(id);
        lifecycles.remove(id);
        if (removed != null) {
            log.info("Pipeline deleted: {}", id);
            return true;
        }
        return false;
    }

    public boolean start(String id) {
        if (!graphs.containsKey(id)) return false;
        lifecycles.put(id, PipelineLifecycle.RUNNING);
        log.info("Pipeline started: {}", id);
        return true;
    }

    public boolean stop(String id) {
        if (!graphs.containsKey(id)) return false;
        lifecycles.put(id, PipelineLifecycle.STOPPED);
        log.info("Pipeline stopped: {}", id);
        return true;
    }

    public boolean pause(String id) {
        if (!graphs.containsKey(id)) return false;
        PipelineLifecycle current = lifecycles.get(id);
        if (current != PipelineLifecycle.RUNNING) return false;
        lifecycles.put(id, PipelineLifecycle.PAUSED);
        log.info("Pipeline paused: {}", id);
        return true;
    }

    public boolean resume(String id) {
        if (!graphs.containsKey(id)) return false;
        PipelineLifecycle current = lifecycles.get(id);
        if (current != PipelineLifecycle.PAUSED) return false;
        lifecycles.put(id, PipelineLifecycle.RUNNING);
        log.info("Pipeline resumed: {}", id);
        return true;
    }

    private List<ScGraphNode> parseNodes(List<Map<String, Object>> rawNodes) {
        List<ScGraphNode> nodes = new ArrayList<>();
        if (rawNodes == null) return nodes;
        for (Map<String, Object> raw : rawNodes) {
            ScGraphNode node = new ScGraphNode();
            node.setType((String) raw.get("type"));
            node.setSpi((String) raw.get("spi"));
            Object config = raw.get("config");
            if (config instanceof Map) {
                node.setConfig((Map<String, Object>) config);
            }
            Object conditions = raw.get("conditions");
            if (conditions instanceof List) {
                List<ScGraphNode.RouteCondition> routeConditions = new ArrayList<>();
                for (Object c : (List<?>) conditions) {
                    if (c instanceof Map) {
                        Map<String, Object> cm = (Map<String, Object>) c;
                        ScGraphNode.RouteCondition rc = new ScGraphNode.RouteCondition();
                        rc.setWhen((String) cm.get("when"));
                        rc.setTarget((String) cm.get("target"));
                        routeConditions.add(rc);
                    }
                }
                node.setConditions(routeConditions);
            }
            nodes.add(node);
        }
        return nodes;
    }
}
