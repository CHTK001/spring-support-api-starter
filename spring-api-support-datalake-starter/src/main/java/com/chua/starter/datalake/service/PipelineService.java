package com.chua.starter.datalake.service;

import com.chua.datalake.support.envelope.DataEnvelope;
import com.chua.datalake.support.engine.PipelineLifecycle;
import com.chua.datalake.support.engine.ScGraph;
import com.chua.datalake.support.engine.ScGraphNode;
import com.chua.datalake.support.spi.pipeline.PipelineManager;
import com.chua.starter.datalake.model.PipelineCreateRequest;
import com.chua.starter.datalake.model.PipelineUpdateRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Pipeline 业务逻辑 — 委托给 {@link PipelineManager} SPI 实现
 */
@Slf4j
public class PipelineService {

    private final PipelineManager pipelineManager;

    public PipelineService(PipelineManager pipelineManager) {
        this.pipelineManager = pipelineManager;
    }

    public List<ScGraph> list() {
        return pipelineManager.list();
    }

    public ScGraph getById(String id) {
        return pipelineManager.getById(id);
    }

    public PipelineLifecycle getLifecycle(String id) {
        return pipelineManager.getLifecycle(id);
    }

    public ScGraph create(PipelineCreateRequest request) {
        ScGraph graph = new ScGraph();
        graph.setId(request.getId());
        graph.setName(request.getName());
        graph.setVersion(request.getVersion());
        graph.setDescription(request.getDescription());
        graph.setNodes(parseNodes(request.getNodes()));
        graph.setSinks(request.getSinks() != null ? request.getSinks() : new HashMap<>());
        return pipelineManager.create(graph);
    }

    public ScGraph update(PipelineUpdateRequest request) {
        ScGraph graph = pipelineManager.getById(request.getId());
        if (graph == null) return null;

        ScGraph updated = new ScGraph();
        updated.setId(request.getId());
        updated.setName(request.getName() != null ? request.getName() : graph.getName());
        updated.setVersion(request.getVersion() != null ? request.getVersion() : graph.getVersion());
        updated.setDescription(request.getDescription() != null ? request.getDescription() : graph.getDescription());
        updated.setNodes(request.getNodes() != null ? parseNodes(request.getNodes()) : graph.getNodes());
        updated.setSinks(request.getSinks() != null ? request.getSinks() : graph.getSinks());
        return pipelineManager.update(updated);
    }

    public boolean delete(String id) {
        return pipelineManager.delete(id);
    }

    public boolean start(String id) {
        return pipelineManager.start(id);
    }

    public boolean stop(String id) {
        return pipelineManager.stop(id);
    }

    public boolean pause(String id) {
        return pipelineManager.pause(id);
    }

    public boolean resume(String id) {
        return pipelineManager.resume(id);
    }

    public void submit(String pipelineId, DataEnvelope envelope) {
        pipelineManager.submit(pipelineId, envelope);
    }

    public DataEnvelope execute(String pipelineId, DataEnvelope envelope) {
        return pipelineManager.execute(pipelineId, envelope);
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
