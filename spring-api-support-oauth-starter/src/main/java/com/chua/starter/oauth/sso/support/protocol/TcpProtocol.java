package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.annotations.Extension;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.sso.support.entity.AuthRequest;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.information.*;
import com.chua.starter.oauth.sso.support.parser.LoginAuthorization;
import com.chua.starter.oauth.sso.support.parser.LogoutAuthorization;
import com.chua.starter.oauth.sso.support.parser.OauthAuthorization;
import com.chua.starter.oauth.sso.support.parser.UpgradeAuthorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * TCP协议实现 - 优化数据包大小
 * <p>
 * 相比HTTP协议，TCP协议具有以下优势：
 * 1. 无HTTP头部开销，减少约200-500字节
 * 2. 自定义二进制协议，数据更紧凑
 * 3. 长连接复用，减少连接建立开销
 * 4. 压缩传输，进一步减少数据包大小
 * </p>
 *
 * @author CH
 */
@Component
@Extension("tcp")
@Slf4j
@Conditional(OnBeanCondition.class)
@RequiredArgsConstructor
public class TcpProtocol implements Protocol, InitializingBean {

    private final AuthServerProperties authServerProperties;
    private final LoginCheck loginCheck;
    private final ApplicationContext applicationContext;
    
    private ServerSocket serverSocket;
    private final Map<String, Socket> activeConnections = new ConcurrentHashMap<>();
    
    // TCP协议常量
    private static final byte LOGIN_REQUEST = 0x01;
    private static final byte LOGOUT_REQUEST = 0x02;
    private static final byte OAUTH_REQUEST = 0x03;
    private static final byte UPGRADE_REQUEST = 0x04;
    
    private static final byte SUCCESS_RESPONSE = 0x10;
    private static final byte ERROR_RESPONSE = 0x11;
    
    // 数据包头部结构：[版本(1字节)][类型(1字节)][长度(4字节)][数据...]
    private static final byte PROTOCOL_VERSION = 0x01;
    private static final int HEADER_SIZE = 6;

    @Override
    public void afterPropertiesSet() throws Exception {
        startTcpServer();
    }

    /**
     * 启动TCP服务器
     */
    private void startTcpServer() {
        CompletableFuture.runAsync(() -> {
            try {
                int port = authServerProperties.getTcpPort() != null ? 
                    authServerProperties.getTcpPort() : 8081;
                serverSocket = new ServerSocket(port);
                log.info("TCP OAuth服务器启动，监听端口: {}", port);
                
                while (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    String clientId = clientSocket.getRemoteSocketAddress().toString();
                    activeConnections.put(clientId, clientSocket);
                    
                    // 使用虚拟线程处理客户端连接
                    Executors.newVirtualThreadPerTaskExecutor().submit(() -> 
                        handleClientConnection(clientSocket, clientId));
                }
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    log.error("TCP服务器异常: {}", e.getMessage());
                }
            }
        });
    }

    /**
     * 处理客户端连接
     */
    private void handleClientConnection(Socket clientSocket, String clientId) {
        try (DataInputStream input = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream())) {
            
            while (!clientSocket.isClosed()) {
                // 读取数据包头部
                byte version = input.readByte();
                byte requestType = input.readByte();
                int dataLength = input.readInt();
                
                if (version != PROTOCOL_VERSION) {
                    sendErrorResponse(output, "不支持的协议版本");
                    continue;
                }
                
                // 读取数据内容
                byte[] data = new byte[dataLength];
                input.readFully(data);
                
                // 解压缩数据
                String jsonData = decompressData(data);
                
                // 处理请求
                ReturnResult<String> result = processRequest(requestType, jsonData, clientSocket);
                
                // 发送响应
                sendResponse(output, result);
            }
        } catch (IOException e) {
            log.debug("客户端连接断开: {}", clientId);
        } finally {
            activeConnections.remove(clientId);
            try {
                clientSocket.close();
            } catch (IOException e) {
                log.warn("关闭客户端连接异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 处理请求
     */
    private ReturnResult<String> processRequest(byte requestType, String jsonData, Socket clientSocket) {
        try {
            // 创建TCP协议专用的请求和响应对象
            InetSocketAddress clientAddress = (InetSocketAddress) clientSocket.getRemoteSocketAddress();
            TcpAuthInformation.TcpRequest tcpRequest = new TcpAuthInformation.TcpRequest(clientAddress);
            TcpAuthInformation.TcpResponse tcpResponse = new TcpAuthInformation.TcpResponse();
            
            AuthRequest authRequest = Json.fromJson(jsonData, AuthRequest.class);
            
            switch (requestType) {
                case LOGIN_REQUEST:
                    return handleLogin(authRequest, tcpRequest, tcpResponse);
                case LOGOUT_REQUEST:
                    return handleLogout(authRequest, tcpRequest, tcpResponse);
                case OAUTH_REQUEST:
                    return handleOauth(authRequest, tcpRequest, tcpResponse);
                case UPGRADE_REQUEST:
                    return handleUpgrade(authRequest, tcpRequest, tcpResponse);
                default:
                    return ReturnResult.illegal("未知的请求类型");
            }
        } catch (Exception e) {
            log.error("处理TCP请求异常: {}", e.getMessage(), e);
            return ReturnResult.error("服务器内部错误");
        }
    }

    /**
     * 处理登录请求
     */
    private ReturnResult<String> handleLogin(AuthRequest authRequest, TcpAuthInformation.TcpRequest request, TcpAuthInformation.TcpResponse response) {
        // 设置必要的请求头信息
        setupRequestHeaders(request, authRequest);

        LoginAuthInformation authInformation = new LoginAuthInformation(
                authServerProperties,
                authRequest.getData(),
                request,
                response,
                applicationContext,
                loginCheck);

        if (!authInformation.isSuccessful()) {
            return ReturnResult.illegal(authInformation.errorMessage());
        }

        LoginAuthorization authorization = authInformation.authorization();
        ReturnResult<String> authentication = authorization.authentication();

        if (authentication.isFailure()) {
            return ReturnResult.error(authentication.getMsg());
        }

        return authentication;
    }

    /**
     * 处理登出请求
     */
    private ReturnResult<String> handleLogout(AuthRequest authRequest, TcpAuthInformation.TcpRequest request, TcpAuthInformation.TcpResponse response) {
        // 设置必要的请求头信息
        setupRequestHeaders(request, authRequest);

        LogoutAuthInformation authInformation = new LogoutAuthInformation(
                authServerProperties,
                authRequest.getData(),
                request,
                response,
                applicationContext,
                loginCheck);

        if (!authInformation.isSuccessful()) {
            return ReturnResult.illegal(authInformation.errorMessage());
        }

        LogoutAuthorization authorization = authInformation.authorization();
        ReturnResult<String> authentication = authorization.authentication();

        if (authentication.isFailure()) {
            return ReturnResult.error(authentication.getMsg());
        }

        return authentication;
    }

    /**
     * 处理OAuth请求
     */
    private ReturnResult<String> handleOauth(AuthRequest authRequest, TcpAuthInformation.TcpRequest request, TcpAuthInformation.TcpResponse response) {
        // 设置必要的请求头信息
        setupRequestHeaders(request, authRequest);

        OauthAuthInformation authInformation = new OauthAuthInformation(
                authServerProperties,
                authRequest.getData(),
                request,
                response,
                applicationContext,
                loginCheck);

        if (!authInformation.isSuccessful()) {
            return ReturnResult.illegal(authInformation.errorMessage());
        }

        OauthAuthorization authorization = authInformation.authorization();
        return authorization.authentication();
    }

    /**
     * 处理升级请求
     */
    private ReturnResult<String> handleUpgrade(AuthRequest authRequest, TcpAuthInformation.TcpRequest request, TcpAuthInformation.TcpResponse response) {
        // 设置必要的请求头信息
        setupRequestHeaders(request, authRequest);

        UpgradeAuthInformation authInformation = new UpgradeAuthInformation(
                authServerProperties,
                authRequest.getData(),
                request,
                response,
                applicationContext,
                loginCheck);

        if (!authInformation.isSuccessful()) {
            return ReturnResult.illegal(authInformation.errorMessage());
        }

        UpgradeAuthorization authorization = authInformation.authorization();
        return authorization.authentication();
    }

    /**
     * 设置请求头信息
     */
    private void setupRequestHeaders(TcpAuthInformation.TcpRequest request, AuthRequest authRequest) {
        // 设置OAuth协议必需的头信息
        request.setHeader("x-oauth-uuid", java.util.UUID.randomUUID().toString());
        request.setHeader("x-oauth-serial", java.util.UUID.randomUUID().toString());
        request.setHeader("x-oauth-protocol", "TCP");
        request.setHeader("x-oauth-sign", "");
        request.setHeader("x-oauth-encode", "true");

        // 根据请求类型设置额外的头信息
        if (authRequest.getType() != null) {
            request.setHeader("x-oauth-type", authRequest.getType());
        }
        if (authRequest.getMode() != null) {
            request.setHeader("x-oauth-mode", authRequest.getMode());
        }
    }

    /**
     * 发送响应
     */
    private void sendResponse(DataOutputStream output, ReturnResult<String> result) throws IOException {
        byte responseType = result.isOk() ? SUCCESS_RESPONSE : ERROR_RESPONSE;
        String responseData = Json.toJson(result);
        byte[] compressedData = compressData(responseData);
        
        // 写入响应头部
        output.writeByte(PROTOCOL_VERSION);
        output.writeByte(responseType);
        output.writeInt(compressedData.length);
        
        // 写入响应数据
        output.write(compressedData);
        output.flush();
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(DataOutputStream output, String errorMessage) throws IOException {
        ReturnResult<String> errorResult = ReturnResult.error(errorMessage);
        sendResponse(output, errorResult);
    }

    /**
     * 压缩数据
     */
    private byte[] compressData(String data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            
            gzipOut.write(data.getBytes(StandardCharsets.UTF_8));
            gzipOut.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            log.warn("数据压缩失败，使用原始数据: {}", e.getMessage());
            return data.getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * 解压缩数据
     */
    private String decompressData(byte[] compressedData) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipIn = new GZIPInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("数据解压缩失败，尝试使用原始数据: {}", e.getMessage());
            return new String(compressedData, StandardCharsets.UTF_8);
        }
    }

}
