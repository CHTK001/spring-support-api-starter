package com.chua.starter.monitor.sync.handler;

import com.chua.common.support.core.annotation.Spi;
import com.chua.starter.monitor.service.node.NodeUrlQpsService;
import com.chua.sync.support.spi.SyncMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * URL QPS 上报消息处理器
 * <p>
 * 接收客户端上报的 URL QPS 统计数据
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Slf4j
@Component
@Spi("urlQpsReportHandler")
public class UrlQpsReportHandler implements SyncMessageHandler {

    private static final String URL_QPS_REPORT_TOPIC = "monitor/url/qps-report";

    @Autowired
    private NodeUrlQpsService nodeUrlQpsService;

    @Override
    public String getName() {
        return "urlQpsReportHandler";
    }

    @Override
    public boolean supports(String topic) {
        return URL_QPS_REPORT_TOPIC.equals(topic);
    }

    @Override
    public Object handle(String topic, String sessionId, Map<String, Object> data) {
        try {
            log.debug("[UrlQpsReportHandler] 收到 QPS 上报: sessionId={}", sessionId);
            nodeUrlQpsService.handleQpsReport(data);
            return Map.of("code", 200, "message", "SUCCESS");
        } catch (Exception e) {
            log.error("[UrlQpsReportHandler] 处理 QPS 上报失败: {}", e.getMessage(), e);
            return Map.of("code", 500, "message", "处理失败: " + e.getMessage());
        }
    }
}
