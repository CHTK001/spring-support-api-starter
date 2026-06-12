package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.core.annotation.Extension;
import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.entity.AuthRequest;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.information.*;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * IPC 协议
 * <p>
 * 基于 Unix Domain Socket 的 IPC 通信协议，适用于客户端与服务端在同一台机器上的场景，
 * 避免 TCP/IP 协议栈开销，极大提升认证性能。
 * </p>
 *
 * @author CH
 */
@Component
@Extension("ipc")
@Spi("ipc")
@Slf4j
@Conditional(OnBeanCondition.class)
@RequiredArgsConstructor
public class IpcOauthProtocol implements OauthProtocol, InitializingBean, DisposableBean {

    private final AuthServerProperties authServerProperties;
    private final LoginCheck loginCheck;
    private final ApplicationContext applicationContext;

    private ServerSocketChannel serverChannel;
    private Thread acceptThread;
    private volatile boolean running;
    private Path socketPath;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!authServerProperties.getProtocols().contains("ipc")) {
            log.info("[OAuth][IPC] 协议未启用，跳过启动");
            return;
        }
        startIpcServer();
    }

    private void startIpcServer() throws IOException {
        String pathStr = authServerProperties.getIpcSocketPath();
        if (pathStr == null || pathStr.isBlank()) {
            pathStr = System.getProperty("java.io.tmpdir") + java.io.File.separator + "oauth-ipc.sock";
        }
        socketPath = Path.of(pathStr);
        Files.deleteIfExists(socketPath);

        serverChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        serverChannel.bind(UnixDomainSocketAddress.of(socketPath));
        serverChannel.configureBlocking(false);

        running = true;
        acceptThread = Thread.ofVirtual().start(this::acceptLoop);

        log.info("[OAuth][IPC] IPC Server 启动成功, socket: {}", socketPath);
    }

    private void acceptLoop() {
        while (running) {
            try {
                SocketChannel channel = serverChannel.accept();
                if (channel != null) {
                    Thread.ofVirtual().start(() -> handleConnection(channel));
                } else {
                    Thread.sleep(10);
                }
            } catch (ClosedChannelException e) {
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                if (running) {
                    log.error("[OAuth][IPC] 接受连接失败", e);
                }
            }
        }
    }

    private void handleConnection(SocketChannel channel) {
        try (channel) {
            ByteBuffer lenBuf = ByteBuffer.allocate(4);
            if (!readFully(channel, lenBuf)) return;
            lenBuf.flip();
            int bodyLen = lenBuf.getInt();
            if (bodyLen <= 0 || bodyLen > 10 * 1024 * 1024) {
                log.warn("[OAuth][IPC] 非法请求体长度: {}", bodyLen);
                return;
            }

            ByteBuffer bodyBuf = ByteBuffer.allocate(bodyLen);
            if (!readFully(channel, bodyBuf)) return;
            String requestJson = new String(bodyBuf.array(), 0, bodyLen, StandardCharsets.UTF_8);

            IpcRequest envelope = Json.fromJson(requestJson, IpcRequest.class);
            if (envelope == null || envelope.getRoute() == null) {
                writeResponse(channel, ReturnResult.illegal("无效的请求格式"));
                return;
            }

            String remoteAddr;
            try {
                remoteAddr = channel.getRemoteAddress() != null ? channel.getRemoteAddress().toString() : "local";
            } catch (Exception e) {
                remoteAddr = "local";
            }

            IpcAuthInformation.IpcRequest ipcRequest = new IpcAuthInformation.IpcRequest(remoteAddr, 0);
            if (envelope.getHeaders() != null) {
                envelope.getHeaders().forEach(ipcRequest::setHeader);
            }
            ipcRequest.setBody(envelope.getBody());

            IpcAuthInformation.IpcResponse ipcResponse = new IpcAuthInformation.IpcResponse();

            ReturnResult<String> result = dispatch(envelope.getRoute(), envelope.getBody(), ipcRequest, ipcResponse);
            writeResponse(channel, result);
        } catch (Exception e) {
            log.error("[OAuth][IPC] 处理连接失败", e);
        }
    }

    private ReturnResult<String> dispatch(String route, String body,
                                           IpcAuthInformation.IpcRequest ipcRequest,
                                           IpcAuthInformation.IpcResponse ipcResponse) {
        AuthRequest authRequest = body != null ? Json.fromJson(body, AuthRequest.class) : new AuthRequest();
        if (authRequest == null) authRequest = new AuthRequest();

        return switch (route) {
            case "login" -> handleLogin(authRequest, ipcRequest, ipcResponse);
            case "logout" -> handleLogout(authRequest, ipcRequest, ipcResponse);
            case "oauth" -> handleOauth(authRequest, ipcRequest, ipcResponse);
            case "upgrade" -> handleUpgrade(authRequest, ipcRequest, ipcResponse);
            case "temporary-token" -> handleTemporaryToken(authRequest, ipcRequest, ipcResponse);
            default -> ReturnResult.error("不支持的路由: " + route);
        };
    }

    private ReturnResult<String> handleLogin(AuthRequest authRequest,
                                              IpcAuthInformation.IpcRequest ipcRequest,
                                              IpcAuthInformation.IpcResponse ipcResponse) {
        try {
            var authInformation = new LoginAuthInformation(
                    authServerProperties, authRequest.getData(), ipcRequest, ipcResponse,
                    applicationContext, loginCheck);
            if (!authInformation.isSuccessful()) {
                return ReturnResult.illegal(authInformation.errorMessage());
            }
            return authInformation.authorization().authentication();
        } catch (Exception e) {
            log.error("[OAuth][IPC] 登录失败", e);
            return ReturnResult.error("登录失败: " + e.getMessage());
        }
    }

    private ReturnResult<String> handleLogout(AuthRequest authRequest,
                                               IpcAuthInformation.IpcRequest ipcRequest,
                                               IpcAuthInformation.IpcResponse ipcResponse) {
        try {
            var authInformation = new LogoutAuthInformation(
                    authServerProperties, authRequest.getData(), ipcRequest, ipcResponse,
                    applicationContext, loginCheck);
            if (!authInformation.isSuccessful()) {
                return ReturnResult.illegal(authInformation.errorMessage());
            }
            return authInformation.authorization().authentication();
        } catch (Exception e) {
            log.error("[OAuth][IPC] 登出失败", e);
            return ReturnResult.error("登出失败: " + e.getMessage());
        }
    }

    private ReturnResult<String> handleOauth(AuthRequest authRequest,
                                              IpcAuthInformation.IpcRequest ipcRequest,
                                              IpcAuthInformation.IpcResponse ipcResponse) {
        try {
            var authInformation = new OauthAuthInformation(
                    authServerProperties, authRequest.getData(), ipcRequest, ipcResponse,
                    applicationContext, loginCheck);
            if (!authInformation.isSuccessful()) {
                return ReturnResult.illegal(authInformation.errorMessage());
            }
            return authInformation.authorization().authentication();
        } catch (Exception e) {
            log.error("[OAuth][IPC] 鉴权失败", e);
            return ReturnResult.error("鉴权失败: " + e.getMessage());
        }
    }

    private ReturnResult<String> handleUpgrade(AuthRequest authRequest,
                                                IpcAuthInformation.IpcRequest ipcRequest,
                                                IpcAuthInformation.IpcResponse ipcResponse) {
        try {
            var authInformation = new UpgradeAuthInformation(
                    authServerProperties, authRequest.getData(), ipcRequest, ipcResponse,
                    applicationContext, loginCheck);
            if (!authInformation.isSuccessful()) {
                return ReturnResult.illegal(authInformation.errorMessage());
            }
            return authInformation.authorization().authentication();
        } catch (Exception e) {
            log.error("[OAuth][IPC] 令牌升级失败", e);
            return ReturnResult.error("令牌升级失败: " + e.getMessage());
        }
    }

    private ReturnResult<String> handleTemporaryToken(AuthRequest authRequest,
                                                       IpcAuthInformation.IpcRequest ipcRequest,
                                                       IpcAuthInformation.IpcResponse ipcResponse) {
        try {
            var authInformation = new TemporaryTokenAuthInformation(
                    authServerProperties, authRequest.getData(), ipcRequest, ipcResponse,
                    applicationContext, loginCheck);
            if (!authInformation.isSuccessful()) {
                return ReturnResult.illegal(authInformation.errorMessage());
            }
            return authInformation.authorization().authentication();
        } catch (Exception e) {
            log.error("[OAuth][IPC] 临时令牌生成失败", e);
            return ReturnResult.error("临时令牌生成失败: " + e.getMessage());
        }
    }

    private boolean readFully(SocketChannel channel, ByteBuffer buf) throws IOException {
        while (buf.hasRemaining()) {
            int n = channel.read(buf);
            if (n < 0) return false;
        }
        return true;
    }

    private void writeResponse(SocketChannel channel, ReturnResult<?> result) throws IOException {
        String json = Json.toJson(result);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(4 + bytes.length);
        buf.putInt(bytes.length);
        buf.put(bytes);
        buf.flip();
        while (buf.hasRemaining()) {
            channel.write(buf);
        }
    }

    @Override
    public void destroy() throws Exception {
        running = false;
        if (acceptThread != null) {
            acceptThread.interrupt();
        }
        if (serverChannel != null) {
            try {
                serverChannel.close();
            } catch (IOException e) {
                log.warn("[OAuth][IPC] 关闭 ServerChannel 失败", e);
            }
        }
        if (socketPath != null) {
            try {
                Files.deleteIfExists(socketPath);
            } catch (IOException e) {
                log.warn("[OAuth][IPC] 清理 socket 文件失败", e);
            }
        }
        log.info("[OAuth][IPC] IPC Server 已关闭");
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IpcRequest {
        private String route;
        private Map<String, String> headers;
        private String body;
    }
}
