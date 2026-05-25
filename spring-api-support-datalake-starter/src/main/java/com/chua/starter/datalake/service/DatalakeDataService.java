package com.chua.starter.datalake.service;

import com.chua.datalake.support.envelope.DataEnvelope;
import com.chua.datalake.support.envelope.PipelineState;
import com.chua.datalake.support.model.AggregationRequest;
import com.chua.datalake.support.model.AggregationResult;
import com.chua.datalake.support.model.QueryRequest;
import com.chua.datalake.support.model.QueryResult;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据查询服务 — 内存存储模拟，后续接入 RetrievalService
 */
@Slf4j
public class DatalakeDataService {

    /** traceId -> DataEnvelope 索引 */
    private final Map<String, DataEnvelope> traceIndex = new ConcurrentHashMap<>();

    /** pipelineId -> List<DataEnvelope> */
    private final Map<String, List<DataEnvelope>> pipelineData = new ConcurrentHashMap<>();

    public void ingest(DataEnvelope envelope) {
        traceIndex.put(envelope.getTraceId(), envelope);
        pipelineData.computeIfAbsent(envelope.getPipelineId(), k -> new ArrayList<>()).add(envelope);
    }

    public QueryResult query(QueryRequest request) {
        List<DataEnvelope> source = pipelineData.getOrDefault(request.getPipelineId(), List.of());
        int offset = request.getOffset();
        int limit = request.getLimit();

        List<DataEnvelope> page = new ArrayList<>();
        for (int i = offset; i < Math.min(offset + limit, source.size()); i++) {
            page.add(source.get(i));
        }

        QueryResult result = new QueryResult();
        result.setData(page);
        result.setTotal(source.size());
        result.setHasMore(offset + limit < source.size());
        return result;
    }

    public AggregationResult aggregate(AggregationRequest request) {
        List<DataEnvelope> source = pipelineData.getOrDefault(request.getPipelineId(), List.of());

        AggregationResult result = new AggregationResult();
        if ("count".equalsIgnoreCase(request.getFunction())) {
            result.setValue((long) source.size());
        } else {
            result.setValue(0L);
        }
        result.setGroups(List.of());
        return result;
    }

    public DataEnvelope trace(String traceId) {
        return traceIndex.get(traceId);
    }
}
