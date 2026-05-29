package com.chua.starter.datalake.subscription;

import com.chua.common.support.core.annotation.Spi;
import com.chua.datalake.support.envelope.DataEnvelope;
import com.chua.datalake.support.spi.subscription.Subscriber;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * TCP 订阅器 — TCP 长连接推送通道
 * <p>客户端连接后保持长连接，服务端主动推送数据。默认端口: 58086。</p>
 *
 * <b>订阅接入标准（TCP 协议格式）:</b>
 * <pre>{@code
 * TCP 连接建立 → 服务端推送
 *
 * 客户端 → 服务端: PING     ← 保活
 * 服务端 → 客户端: PONG     ← 保活响应
 * 服务端 → 客户端: PUSH <json>\n  ← 数据推送
 * }</pre>
 *
 * <b>输出格式:</b>
 * <pre>{@code
 * PUSH {"traceId":"...","topics":["t1"],"rawPayload":"...","formatType":"..."}
 * }</pre>
 *
 * @author CH
 */
@Spi("tcp-subscriber")
public class TcpSubscriber implements Subscriber {

    /** TCP 服务器 Socket，accept 客户端连接 */
    private volatile ServerSocket serverSocket;
    /** 运行状态标记 */
    private volatile boolean running;
    /** 已连接的 TCP 客户端映射: clientId -> Socket */
    private final ConcurrentHashMap<String, Socket> clients = new ConcurrentHashMap<>();
    /** Boss 线程池：负责 accept 新连接 */
    private ExecutorService bossExecutor;
    /** Worker 线程池：负责处理已建立连接的 PING/PONG 保活 */
    private ExecutorService workerExecutor;

    @Override
    public String protocol() {
        return "TCP";
    }

    @Override
    public int defaultPort() {
        return 58086;
    }

    @Override
    public void start(int port) throws Exception {
        if (running) return;
        running = true;
        serverSocket = new ServerSocket(port);
        bossExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "tcp-subscriber-boss");
            t.setDaemon(true);
            return t;
        });
        workerExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "tcp-subscriber-worker");
            t.setDaemon(true);
            return t;
        });
        bossExecutor.submit(this::acceptLoop);
        System.out.println("[TcpSubscriber] TCP 订阅已启动: port=" + port);
    }

    /**
     * Boss 线程主循环，持续 accept 新 TCP 连接
     * <p>每个连接分配 clientId（ip:port），放入 clients 映射后交给 worker 保活</p>
     */
    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                String clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
                clients.put(clientId, socket);
                workerExecutor.submit(() -> keepAlive(socket, clientId));
            } catch (IOException e) {
                if (running) System.err.println("[TcpSubscriber] accept error: " + e.getMessage());
                break;
            }
        }
    }

    /**
     * 客户端保活循环
     * <p>持续读取客户端数据，处理 PING 请求（回复 PONG），连接断开时自动清理</p>
     *
     * @param socket   客户端 socket
     * @param clientId 客户端标识
     */
    private void keepAlive(Socket socket, String clientId) {
        try (socket) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while (running && (line = reader.readLine()) != null) {
                if ("PING".equalsIgnoreCase(line.trim())) {
                    OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
                    writer.write("PONG\n");
                    writer.flush();
                }
            }
        } catch (Exception ignored) {
        } finally {
            clients.remove(clientId);
        }
    }

    @Override
    public void stop() {
        running = false;
        clients.values().forEach(s -> { try { s.close(); } catch (IOException ignored) {} });
        clients.clear();
        if (bossExecutor != null) bossExecutor.shutdownNow();
        if (workerExecutor != null) workerExecutor.shutdownNow();
        if (serverSocket != null && !serverSocket.isClosed()) {
            try { serverSocket.close(); } catch (IOException ignored) {}
        }
        System.out.println("[TcpSubscriber] TCP 订阅已停止");
    }

    /**
     * 向指定客户端推送数据
     *
     * @param clientId 客户端 ID（为空时广播给所有已连接客户端）
     * @param envelope 数据信封
     */
    @Override
    public void push(String clientId, DataEnvelope envelope) {
        if (clientId != null) {
            sendToClient(clientId, envelope);
        } else {
            clients.keySet().forEach(cid -> sendToClient(cid, envelope));
        }
    }

    /**
     * 向单个客户端 socket 写入 PUSH 数据
     *
     * @param clientId 客户端 ID
     * @param envelope 数据信封
     */
    private void sendToClient(String clientId, DataEnvelope envelope) {
        Socket socket = clients.get(clientId);
        if (socket == null || socket.isClosed()) {
            clients.remove(clientId);
            return;
        }
        try {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            writer.write(format(envelope));
            writer.flush();
        } catch (IOException e) {
            clients.remove(clientId);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    // ==================== SPI 统一订阅接口实现 ====================

    @Override
    public void subscribe(String clientId, String pipelineId, List<String> topics, Map<String, Object> filter) {
        // TCP 长连接模式：客户端连接即视为订阅，无需额外操作
    }

    @Override
    public void unsubscribe(String clientId) {
        clients.remove(clientId);
    }

    @Override
    public boolean matches(DataEnvelope envelope) {
        return !clients.isEmpty();
    }

    /**
     * 获取所有已连接客户端 ID 列表（供管理 API 调用）
     *
     * @return 已连接客户端 ID 集合
     */
    public Set<String> getClientIds() {
        return new java.util.HashSet<>(clients.keySet());
    }

    /**
     * 将 {@link DataEnvelope} 转换为 TCP PUSH 格式
     * <p>格式: {@code PUSH <json>\n}</p>
     *
     * @param envelope 数据信封
     * @return TCP 协议格式字符串
     */
    @Override
    public String format(DataEnvelope envelope) {
        String json = toJson(envelope);
        return "PUSH " + json + "\n";
    }
}
