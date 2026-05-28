package com.chua.starter.datalake.subscription;

import com.chua.common.support.core.annotation.Spi;
import com.chua.datalake.support.envelope.DataEnvelope;
import com.chua.datalake.support.spi.subscription.Subscriber;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * MQ 订阅器 — 基于 TCP 的简易消息队列订阅分发
 * <p>支持客户端发布/订阅模式，默认端口: 58085。</p>
 *
 * <b>订阅接入标准（MQ 协议格式）:</b>
 * <pre>{@code
 * SUB <topic>           ← 订阅主题
 * PUB <topic> <json>    ← 发布消息到主题
 * POLL <topic>          ← 从主题拉取消息
 * }</pre>
 *
 * <b>输出格式:</b>
 * <ul>
 *   <li>POLL 成功：DATA {"traceId":"...","payload":"..."}</li>
 *   <li>POLL 无数据：NODATA</li>
 *   <li>PUSH 推送：DATA {"traceId":"...","payload":"..."}</li>
 * </ul>
 *
 * @author CH
 */
@Spi("mq-subscriber")
public class MqSubscriber implements Subscriber {

    /** TCP 服务器 Socket，accept 客户端连接 */
    private volatile ServerSocket serverSocket;
    /** 运行状态标记 */
    private volatile boolean running;
    /** 主题消息队列: topic -> 消息列表（CopyOnWriteArrayList 保证并发安全） */
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<DataEnvelope>> topicMessages = new ConcurrentHashMap<>();
    /** Boss 线程池：负责 accept 新连接 */
    private ExecutorService bossExecutor;
    /** Worker 线程池：负责处理客户端请求（SUB/PUB/POLL） */
    private ExecutorService workerExecutor;

    @Override
    public String protocol() {
        return "MQ";
    }

    @Override
    public int defaultPort() {
        return 58085;
    }

    @Override
    public void start(int port) throws Exception {
        if (running) return;
        running = true;
        serverSocket = new ServerSocket(port);
        bossExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "mq-subscriber-boss");
            t.setDaemon(true);
            return t;
        });
        workerExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "mq-subscriber-worker");
            t.setDaemon(true);
            return t;
        });
        bossExecutor.submit(this::acceptLoop);
        System.out.println("[MqSubscriber] MQ 订阅已启动: port=" + port);
    }

    /**
     * Boss 线程主循环，持续 accept 新客户端连接并提交给 worker 处理
     */
    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                workerExecutor.submit(() -> handleClient(socket));
            } catch (IOException e) {
                if (running) System.err.println("[MqSubscriber] accept error: " + e.getMessage());
                break;
            }
        }
    }

    /**
     * 处理单个客户端连接的 MQ 协议请求
     * <p>支持的协议命令:</p>
     * <ul>
     *   <li><b>SUB &lt;topic&gt;</b> — 订阅主题，创建消息队列</li>
     *   <li><b>PUB &lt;topic&gt; &lt;data&gt;</b> — 向主题发布消息</li>
     *   <li><b>POLL &lt;topic&gt;</b> — 从主题拉取一条消息（消费后移除）</li>
     * </ul>
     *
     * @param socket 客户端 socket
     */
    private void handleClient(Socket socket) {
        try (socket;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            String line = reader.readLine();
            if (line == null) return;

            String[] parts = line.split(" ", 3);
            if (parts.length < 2) {
                writer.write("ERR invalid command\n");
                writer.flush();
                return;
            }

            String cmd = parts[0];
            String topic = parts[1];

            switch (cmd.toUpperCase()) {
                case "SUB" -> {
                    topicMessages.putIfAbsent(topic, new CopyOnWriteArrayList<>());
                    writer.write("OK subscribed to " + topic + "\n");
                }
                case "PUB" -> {
                    String data = parts.length > 2 ? parts[2] : "";
                    var queue = topicMessages.get(topic);
                    if (queue != null) {
                        DataEnvelope env = new DataEnvelope();
                        env.addTopic(topic);
                        env.setRawPayload(data.getBytes(StandardCharsets.UTF_8));
                        queue.add(env);
                    }
                    writer.write("OK published to " + topic + "\n");
                }
                case "POLL" -> {
                    var queue = topicMessages.get(topic);
                    if (queue != null && !queue.isEmpty()) {
                        DataEnvelope msg = queue.remove(0);
                        writer.write("DATA " + (msg != null ? new String(msg.getRawPayload() != null ? msg.getRawPayload() : new byte[0], StandardCharsets.UTF_8) : "") + "\n");
                    } else {
                        writer.write("NODATA\n");
                    }
                }
                default -> writer.write("ERR unknown command\n");
            }
            writer.flush();
        } catch (Exception e) {
            System.err.println("[MqSubscriber] handle error: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        running = false;
        if (bossExecutor != null) bossExecutor.shutdownNow();
        if (workerExecutor != null) workerExecutor.shutdownNow();
        if (serverSocket != null && !serverSocket.isClosed()) {
            try { serverSocket.close(); } catch (IOException ignored) {}
        }
        topicMessages.clear();
        System.out.println("[MqSubscriber] MQ 订阅已停止");
    }

    /**
     * Push 模式下向所有匹配主题发布消息
     *
     * @param clientId 客户端 ID（MQ 模式下忽略，按 topic 路由）
     * @param envelope 数据信封
     */
    @Override
    public void push(String clientId, DataEnvelope envelope) {
        // MQ 模式下 push 相当于向 topic 发布消息
        if (envelope.getTopics() != null) {
            for (String topic : envelope.getTopics()) {
                topicMessages.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(envelope);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    // ==================== SPI 统一订阅接口实现 ====================

    @Override
    public void subscribe(String clientId, String pipelineId, List<String> topics, Map<String, Object> filter) {
        for (String topic : topics) {
            topicMessages.putIfAbsent(topic, new CopyOnWriteArrayList<>());
        }
    }

    @Override
    public void unsubscribe(String clientId) {
        // MQ 模式不维护客户端映射，无操作
    }

    @Override
    public boolean matches(DataEnvelope envelope) {
        if (envelope.getTopics() == null) return false;
        for (String topic : envelope.getTopics()) {
            if (topicMessages.containsKey(topic)) return true;
        }
        return false;
    }

    /**
     * 将 {@link DataEnvelope} 转换为 MQ 协议输出格式
     * <p>格式: {@code DATA <json>\n}</p>
     *
     * @param envelope 数据信封
     * @return MQ 协议格式字符串
     */
    @Override
    public String format(DataEnvelope envelope) {
        String json = toJson(envelope);
        return "DATA " + json + "\n";
    }
}
