package com.chua.starter.datalake.collection;

import com.chua.common.support.core.annotation.Spi;
import com.chua.datalake.support.model.CollectData;
import com.chua.datalake.support.spi.collection.Collector;

import java.util.Map;
import java.util.function.Consumer;

/**
 * HTTP 采集器 — 接收 HTTP POST 提交的数据
 * <p>与 API 服务共享 58081 端口，路由由 DatalakeApiServer 注册。</p>
 *
 * <b>中台接入标准（HTTP JSON 请求体格式）:</b>
 * <pre>{@code
 * {
 *   "topic": "数据主题（可选，默认 default）",
 *   "payload": {},        // 数据内容，任意 JSON
 *   "pipelineId": "xxx",  // 目标管线 ID（可选）
 *   "source": "来源标识",  // （可选）
 *   "headers": {}         // （可选）
 * }
 * }</pre>
 *
 * @author CH
 */
@Spi("http-collector")
public class HttpCollector implements Collector {

    /** 采集数据回调处理器，收到数据后由 DatalakeApiServer 设置 */
    private volatile Consumer<CollectData> handler;
    /** 运行状态标记 */
    private volatile boolean running;

    @Override
    public String protocol() {
        return "HTTP";
    }

    @Override
    public int defaultPort() {
        return 58081;
    }

    @Override
    public void start(int port) {
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
    }

    @Override
    public void setHandler(Consumer<CollectData> handler) {
        this.handler = handler;
    }

    @Override
    public Consumer<CollectData> getHandler() {
        return handler;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * HTTP 采集路由被调用时触发（兼容旧版调用方）
     * <p>内部调用 {@link #collect(String, Object, String, String, Map)} 完成标准封装和派发。</p>
     *
     * @param topic      数据主题
     * @param payload    原始数据
     * @param source     来源标识
     * @param pipelineId 目标管线 ID
     * @param headers    消息头
     */
    public void accept(String topic, Object payload, String source, String pipelineId, Map<String, Object> headers) {
        collect(topic, payload, source, pipelineId, headers);
    }
}
