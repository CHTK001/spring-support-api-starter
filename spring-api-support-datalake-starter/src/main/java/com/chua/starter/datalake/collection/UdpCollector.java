package com.chua.starter.datalake.collection;

import com.chua.common.support.core.annotation.Spi;
import com.chua.datalake.support.model.CollectData;
import com.chua.datalake.support.spi.collection.Collector;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * UDP 采集器 — 基于 DatagramSocket 的 UDP 数据报接入
 * <p>默认端口: 58084，每个 UDP 包一条消息，最大 65535 字节。</p>
 *
 * <b>中台接入标准（UDP 数据格式）:</b>
 * <pre>{@code
 * {"topic":"log","payload":{"level":"INFO","msg":"hello"},"pipelineId":"p1"}
 * }</pre>
 * <ul>
 *   <li>每个 UDP 数据报包含一条完整 JSON</li>
 *   <li>支持结构化格式（含 topic/payload/pipelineId）和非结构化纯文本</li>
 *   <li>无响应（UDP 协议特性，不保证送达）</li>
 * </ul>
 *
 * @author CH
 */
@Spi("udp-collector")
public class UdpCollector implements Collector {

    /** 最大 UDP 数据包大小（65535 字节） */
    private static final int MAX_PACKET_SIZE = 65535;

    /** UDP 套接字 */
    private volatile DatagramSocket socket;
    /** 运行状态标记（CAS 保证线程安全） */
    private final AtomicBoolean running = new AtomicBoolean(false);
    /** 采集数据回调处理器 */
    private Consumer<CollectData> handler;
    /** 单线程接收循环线程池 */
    private ExecutorService executor;

    @Override
    public String protocol() {
        return "UDP";
    }

    @Override
    public int defaultPort() {
        return 58084;
    }

    @Override
    public void start(int port) throws Exception {
        if (running.compareAndSet(false, true)) {
            socket = new DatagramSocket(port);
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "udp-collector");
                t.setDaemon(true);
                return t;
            });
            executor.submit(this::receiveLoop);
            System.out.println("[UdpCollector] UDP 采集已启动: port=" + port);
        }
    }

    /**
     * UDP 接收主循环
     * <p>阻塞等待 DatagramSocket 接收数据报，使用 {@link #parse(String)} 解析为标准 {@link CollectData}，
     * 设置来源 IP 后派发到回调处理器。</p>
     */
    private void receiveLoop() {
        byte[] buf = new byte[MAX_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        while (running.get()) {
            try {
                packet.setLength(buf.length);
                socket.receive(packet);
                String body = new String(packet.getData(), packet.getOffset(), packet.getLength(),
                        StandardCharsets.UTF_8).trim();
                if (!body.isEmpty()) {
                    CollectData data = parse(body);
                    data.setSource(packet.getAddress().getHostAddress());
                    dispatch(data);
                }
            } catch (Exception e) {
                if (running.get()) {
                    System.err.println("[UdpCollector] receive error: " + e.getMessage());
                }
                break;
            }
        }
    }

    @Override
    public void stop() {
        running.set(false);
        if (executor != null) executor.shutdownNow();
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        System.out.println("[UdpCollector] UDP 采集已停止");
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
