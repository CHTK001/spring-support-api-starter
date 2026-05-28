package com.chua.starter.datalake.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 管道数据采集请求 — 定义数据提交标准格式
 * <pre>
 * POST /v2/datalake/pipeline/{id}/submit
 * Content-Type: application/json
 *
 * {
 *   "topic": "sensor-temp",            // 数据主题，标识数据归属
 *   "topics": ["sensor-temp", "alert"],// 多主题（可选）
 *   "payload": {                        // 原始数据
 *     "temperature": 26.5,
 *     "humidity": 68.3,
 *     "device_id": "SENSOR-A001",
 *     "timestamp": "2026-05-28T10:30:00"
 *   },
 *   "parsed": {                         // 预解析数据（可选，跳过 parse 节点）
 *     "temp": 26.5,
 *     "humi": 68.3
 *   },
 *   "formatType": "json"                // 数据格式
 * }
 * </pre>
 *
 * @author CH
 * @since 2026/5/28
 */
@Data
public class CollectRequest {

    /** 数据主题，标识数据推送给哪些 topic 的订阅者 */
    private String topic;

    /** 多主题（可选，优先级高于 topic） */
    private List<String> topics;

    /** 原始数据 */
    private Object payload;

    /** 预解析数据（可选，跳过 parse 节点直接进入后续处理） */
    private Map<String, Object> parsed;

    /** 数据格式，默认 json */
    private String formatType = "json";
}
