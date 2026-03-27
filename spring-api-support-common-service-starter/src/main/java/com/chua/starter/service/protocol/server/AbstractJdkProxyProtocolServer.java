package com.chua.starter.service.protocol.server;

import com.chua.common.support.network.protocol.ServerSetting;
import com.chua.common.support.network.protocol.request.ServletRequest;
import com.chua.common.support.network.protocol.session.ProtocolSession;
import com.chua.common.support.network.protocol.server.AbstractSocketProtocolServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于 JDK Socket 的代理服务端抽象基类。
 */
@Slf4j
public abstract class AbstractJdkProxyProtocolServer extends AbstractSocketProtocolServer {

    private final AtomicLong connectionSequence = new AtomicLong();
    private final Map<String, Socket> connectionMap = new ConcurrentHashMap<>();

    private volatile boolean accepting;
    private volatile ServerSocket serverSocket;
    private volatile ExecutorService connectionExecutor;
    private volatile Thread acceptThread;

    protected AbstractJdkProxyProtocolServer(ServerSetting serverSetting) {
        super(serverSetting);
    }

    @Override
    protected void doStart() throws Exception {
        this.connectionExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.serverSocket = new ServerSocket();
        this.serverSocket.setReuseAddress(true);
        this.serverSocket.bind(
                new InetSocketAddress(getListenAddress(), getListenPort()),
                Math.max(64, serverSetting.getBacklog())
        );
        this.accepting = true;
        this.acceptThread = Thread.ofVirtual()
                .name(getProtocolName() + "-acceptor")
                .unstarted(this::acceptLoop);
        this.acceptThread.start();
    }

    @Override
    protected void doStop() throws Exception {
        accepting = false;
        closeQuietly(serverSocket);
        for (Socket socket : connectionMap.values()) {
            closeQuietly(socket);
        }
        connectionMap.clear();
        if (connectionExecutor != null) {
            connectionExecutor.shutdownNow();
            connectionExecutor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Override
    protected ProtocolSession createSession(String connectionId, Object rawConnection) {
        if (!(rawConnection instanceof Socket socket)) {
            return null;
        }
        return new ProxySocketSession(connectionId, getProtocolName(), socket);
    }

    @Override
    protected String getVersion() {
        return "1.0.0-jdk";
    }

    protected abstract void handleConnection(String connectionId, Socket clientSocket, ProxySocketSession session) throws Exception;

    protected Socket openRemoteSocket(String host, int port) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), (int) serverSetting.getConnectionTimeoutMillis());
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        return socket;
    }

    protected void tunnel(Socket left, Socket right) throws InterruptedException {
        var latch = new java.util.concurrent.CountDownLatch(2);
        Thread.ofVirtual().start(() -> {
            relay(left, right, true);
            latch.countDown();
        });
        Thread.ofVirtual().start(() -> {
            relay(right, left, false);
            latch.countDown();
        });
        latch.await();
    }

    protected void writeAsciiResponse(Socket clientSocket, String response) throws IOException {
        clientSocket.getOutputStream().write(response.getBytes(StandardCharsets.ISO_8859_1));
        clientSocket.getOutputStream().flush();
    }

    protected void writeHttpError(Socket clientSocket, int statusCode, String reason, String message) {
        try {
            String body = message == null ? reason : message;
            String response = "HTTP/1.1 " + statusCode + " " + reason + "\r\n"
                    + "Content-Type: text/plain; charset=UTF-8\r\n"
                    + "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                    + "Connection: close\r\n\r\n"
                    + body;
            clientSocket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
            clientSocket.getOutputStream().flush();
        } catch (IOException ignored) {
        }
    }

    private void acceptLoop() {
        while (accepting && serverSocket != null && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                clientSocket.setKeepAlive(true);
                clientSocket.setTcpNoDelay(true);
                String connectionId = getProtocolName() + "-" + connectionSequence.incrementAndGet();
                ProtocolSession created = onConnectionEstablished(connectionId, clientSocket);
                if (!(created instanceof ProxySocketSession session)) {
                    closeQuietly(clientSocket);
                    continue;
                }
                connectionMap.put(connectionId, clientSocket);
                fireClientConnected(connectionId, String.valueOf(clientSocket.getRemoteSocketAddress()));
                connectionExecutor.execute(() -> handleConnectionSafely(connectionId, clientSocket, session));
            } catch (IOException e) {
                if (accepting) {
                    log.error("[{}] 接收连接失败", getProtocolName(), e);
                }
            }
        }
    }

    private void handleConnectionSafely(String connectionId, Socket clientSocket, ProxySocketSession session) {
        try {
            handleConnection(connectionId, clientSocket, session);
        } catch (Exception e) {
            log.error("[{}] 处理代理连接失败: {}", getProtocolName(), connectionId, e);
            fireOnError(connectionId, e, null);
        } finally {
            connectionMap.remove(connectionId);
            closeQuietly(clientSocket);
            onConnectionClosed(connectionId, (ServletRequest) null);
            fireClientDisconnected(connectionId, String.valueOf(clientSocket.getRemoteSocketAddress()), "connection closed");
        }
    }

    private void relay(Socket source, Socket target, boolean inbound) {
        byte[] buffer = new byte[serverSetting.isAutomaticOptimization() ? 32 * 1024 : 8 * 1024];
        try {
            InputStream in = source.getInputStream();
            OutputStream out = target.getOutputStream();
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                out.flush();
                if (inbound) {
                    addBytesReceived(len);
                } else {
                    addBytesSent(len);
                }
            }
            try {
                target.shutdownOutput();
            } catch (IOException ignored) {
            }
        } catch (IOException ignored) {
        } finally {
            closeQuietly(source);
            closeQuietly(target);
        }
    }

    protected static void closeQuietly(ServerSocket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    protected static void closeQuietly(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    protected static final class ProxySocketSession implements ProtocolSession {

        private final String sessionId;
        private final String protocolType;
        private final Socket socket;
        private final long createTime = System.currentTimeMillis();
        private final Map<String, Object> attributes = new ConcurrentHashMap<>();

        private ProxySocketSession(String sessionId, String protocolType, Socket socket) {
            this.sessionId = sessionId;
            this.protocolType = protocolType;
            this.socket = socket;
        }

        @Override
        public String getSessionId() {
            return sessionId;
        }

        @Override
        public String getProtocolType() {
            return protocolType;
        }

        @Override
        public String getRemoteAddress() {
            return String.valueOf(socket.getRemoteSocketAddress());
        }

        @Override
        public String getLocalAddress() {
            return String.valueOf(socket.getLocalSocketAddress());
        }

        @Override
        public boolean isValid() {
            return socket.isConnected() && !socket.isClosed();
        }

        @Override
        public void sendText(String text) throws IOException {
            sendBinary(text.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void sendBinary(byte[] data) throws IOException {
            socket.getOutputStream().write(data);
            socket.getOutputStream().flush();
        }

        @Override
        public void sendEvent(String event, Object data) throws IOException {
            sendText(event + ":" + (data == null ? "" : data));
        }

        @Override
        public CompletableFuture<Void> sendAsync(String event, Object data) {
            return CompletableFuture.runAsync(() -> {
                try {
                    sendEvent(event, data);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public CompletableFuture<Object> sendAndRequest(String event, Object data) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException("proxy session does not support request-response"));
        }

        @Override
        public CompletableFuture<Object> sendAndRequest(String event, Object data, int timeoutMs) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException("proxy session does not support request-response"));
        }

        @Override
        public void reply(String originalMessageId, Object data) throws IOException {
            sendText(String.valueOf(data));
        }

        @Override
        public void close() {
            closeQuietly(socket);
        }

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public void setAttribute(String name, Object value) {
            if (value == null) {
                attributes.remove(name);
                return;
            }
            attributes.put(name, value);
        }

        @Override
        public void removeAttribute(String name) {
            attributes.remove(name);
        }

        @Override
        public String[] getAttributeNames() {
            return attributes.keySet().toArray(new String[0]);
        }

        @Override
        public boolean supportsFeature(String feature) {
            return "binary".equalsIgnoreCase(feature) || "text".equalsIgnoreCase(feature);
        }

        @Override
        public ConnectionInfo getConnectionInfo() {
            return new ConnectionInfo(sessionId, protocolType, getRemoteAddress(), getLocalAddress(), isValid(), createTime);
        }

        @Override
        public Object getRawSession() {
            return socket;
        }
    }
}
