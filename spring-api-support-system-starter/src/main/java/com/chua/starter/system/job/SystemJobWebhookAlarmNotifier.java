package com.chua.starter.system.job;

import com.chua.starter.job.support.notify.JobAlarmMessage;
import com.chua.starter.job.support.notify.JobAlarmNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 系统管理应用的调度任务 Webhook 告警通道（类型 WEBHOOK）。
 *
 * <p>为什么要在系统应用里再实现一份：告警通道是 SPI，job-starter 自身不绑定通道，
 * 由宿主注册 {@link JobAlarmNotifier} Bean 接通。已有的实现类都在采集应用
 * （{@code com.chua.starter.collect.notify.job}）里，而系统管理应用<b>不依赖</b>采集模块，
 * 于是它的容器里一条通道都没有——台账能配「告警类型 Webhook + 回调地址」，
 * 任务失败时 {@code CompositeJobAlarmNotifier} 找不到实现，只在任务日志里留一句
 * 「未接入 WEBHOOK 类型告警通道」，外部收不到任何通知。这是系统应用自己的缺口，
 * 应当在自己这里补上，而不是把系统应用拖去依赖整个采集模块。</p>
 *
 * <p>与采集侧实现行为一致：把告警内容以 JSON POST 出去，HTTP 2xx 视为投递成功；
 * 非 2xx 抛带状态码与响应摘要的异常，由执行器写进任务日志的告警段（不影响任务终态）。
 * 只允许 http/https，避免把目标配成 file:// 之类让服务端读本地文件。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@Component
@ConditionalOnClass(JobAlarmNotifier.class)
public class SystemJobWebhookAlarmNotifier implements JobAlarmNotifier {

    /**
     * 单次回调的连接与读取超时毫秒。
     */
    private static final long TIMEOUT_MS = 5000L;

    /**
     * 时间展示格式，与任务日志一致。
     */
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    /**
     * 投递失败原因写进任务日志时的最大长度，避免把整个响应体灌进日志。
     */
    private static final int MAX_BRIEF = 200;

    @Override
    public String type() {
        return JobAlarmMessage.TYPE_WEBHOOK;
    }

    @Override
    public void notify(JobAlarmMessage message) throws Exception {
        String target = message == null || message.getTarget() == null ? "" : message.getTarget().trim();
        URI uri;
        try {
            uri = URI.create(target);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("回调地址不合法: " + target, ex);
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.startsWith("http") || uri.getHost() == null) {
            throw new IllegalStateException("回调地址必须是 http/https，当前: " + target);
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", JobAlarmMessage.normalizeType(message.getAlarmType()));
        payload.put("jobName", message.getJobName());
        payload.put("costMs", message.getCostMs());
        payload.put("exception", message.getException());
        payload.put("nodeId", message.getNodeId());
        payload.put("occurredAt", occurredAt(message));
        payload.put("occurredAtText", TIME_FORMAT.format(Instant.ofEpochMilli(occurredAt(message))));

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofMillis(TIMEOUT_MS))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(payload), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(TIMEOUT_MS))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new IllegalStateException("Webhook HTTP " + status + " " + brief(response.body()));
        }
        log.info("[system] 调度任务 Webhook 告警已投递 job={} url={} status={}",
                message.getJobName(), target, status);
    }

    /**
     * 告警发生时刻，缺失时退回当前时间。
     *
     * @param message 告警消息
     * @return 毫秒时间戳
     */
    private long occurredAt(JobAlarmMessage message) {
        return message.getOccurredAtMs() > 0 ? message.getOccurredAtMs() : System.currentTimeMillis();
    }

    /**
     * 序列化告警内容。
     *
     * <p>刻意不引三方 JSON 库：字段全是字符串与数字，手写转义就够，
     * 也避免为了一个告警通道给系统应用增加序列化依赖的版本约束。</p>
     *
     * @param payload 告警字段
     * @return JSON 文本
     */
    private String toJson(Map<String, Object> payload) {
        StringBuilder sb = new StringBuilder(128).append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(escape(entry.getKey())).append("\":");
            Object value = entry.getValue();
            if (value instanceof Number) {
                sb.append(value);
            } else {
                sb.append('"').append(escape(String.valueOf(value))).append('"');
            }
        }
        return sb.append('}').toString();
    }

    /**
     * JSON 字符串转义。
     *
     * @param text 原始文本
     * @return 转义后的文本
     */
    private String escape(String text) {
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    // 其余控制字符必须转成 JSON 的 u 转义，否则拼出来的不是合法 JSON
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * 响应体截断，避免整页 HTML 写进任务日志。
     *
     * @param body 响应体
     * @return 截断后的单行文本
     */
    private String brief(String body) {
        if (body == null) {
            return "";
        }
        String text = body.replaceAll("\\s+", " ").trim();
        return text.length() <= MAX_BRIEF ? text : text.substring(0, MAX_BRIEF) + "…";
    }
}
