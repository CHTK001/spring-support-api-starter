package com.chua.starter.datalake.collection;

import com.chua.common.support.core.annotation.Spi;
import com.chua.datalake.support.model.CollectData;
import com.chua.datalake.support.spi.collection.Collector;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * TCP 采集器 — 基于 ServerSocket 的 TCP 长/短连接数据接入
 * <p>默认端口: 58083</p>
 *
 * <b>中台接入标准（TCP 数据格式）:</b>
 * <pre>{@code
 * {"topic":"log","payload":{"level":"INFO","msg":"hello"},"pipelineId":"p1"}
 * <空行>   ← 空行表示消息结束
 * }</pre>
 * <ul>
 *   <li>每连接一次发送一条消息</li>
 *   <li>数据为单行 JSON，以空行结尾作为消息终止符</li>
 *   <li>支持结构化格式（含 topic/payload/pipelineId）和非结构化纯文本</li>
 * </ul>
 *
 * @author CH
 */
@Spi("tcp-collector")
public class TcpCollector implements Collector {

    /** TCP 服务器 Socket，accept 客户端连接 */
    private volatile ServerSocket serverSocket;
    /** 运行状态标记（CAS 保证线程安全） */
    private final AtomicBoolean running = new AtomicBoolean(false);
    /** 采集数据回调处理器 */
    private Consumer<CollectData> handler;
    /** Boss 线程池：负责 accept 新连接 */
    private ExecutorService bossExecutor;
    /** Worker 线程池：负责处理已建立的 socket 连接（读写） */
    private ExecutorService workerExecutor;

    @Override
    public String protocol() {
        return "TCP";
    }

    @Override
    public int defaultPort() {
        return 58083;
    }

    @Override
    public void start(int port) throws Exception {
        if (running.compareAndSet(false, true)) {
            serverSocket = new ServerSocket(port);
            bossExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "tcp-collector-boss");
                t.setDaemon(true);
                return t;
            });
            workerExecutor = Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "tcp-collector-worker");
                t.setDaemon(true);
                return t;
            });
            bossExecutor.submit(this::acceptLoop);
            System.out.println("[TcpCollector] TCP 采集已启动: port=" + port);
        }
    }

    /**
     * Boss 线程主循环，持续 accept 新客户端连接并提交给 worker 处理
     */
    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket socket = serverSocket.accept();
                workerExecutor.submit(() -> handleSocket(socket));
            } catch (IOException e) {
                if (running.get()) {
                    System.err.println("[TcpCollector] accept error: " + e.getMessage());
                }
                break;
            }
        }
    }

    /**
     * 处理单个 TCP socket 连接
     * <p>逐行读取数据，空行结束，使用 {@link #parse(String)} 解析为标准 {@link CollectData}，
     * 设置来源 IP 后派发到回调处理器。</p>
     *
     * @param socket 客户端 socket
     */
    private void handleSocket(Socket socket) {
        try (socket;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) break;
                sb.append(line);
            }

            String body = sb.toString().trim();
            if (!body.isEmpty()) {
                CollectData data = parse(body);
                data.setSource(socket.getInetAddress().getHostAddress());
                dispatch(data);
                writer.write("OK\n");
                writer.flush();
            }
        } catch (Exception e) {
            System.err.println("[TcpCollector] handle error: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        running.set(false);
        if (bossExecutor != null) bossExecutor.shutdownNow();
        if (workerExecutor != null) workerExecutor.shutdownNow();
        if (serverSocket != null && !serverSocket.isClosed()) {
            try { serverSocket.close(); } catch (IOException ignored) {}
        }
        System.out.println("[TcpCollector] TCP 采集已停止");
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
        return running.get();
    }
}
