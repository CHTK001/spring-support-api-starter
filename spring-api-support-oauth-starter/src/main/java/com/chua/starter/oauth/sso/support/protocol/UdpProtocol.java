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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * UDP协议实现 - 极致优化数据包大小
 * <p>
 * UDP协议优势：
 * 1. 无连接开销，每个数据包独立
 * 2. 协议头部仅8字节（vs TCP的20字节）
 * 3. 无握手过程，延迟最低
 * 4. 适合小数据包的快速传输
 * 5. 自定义压缩协议，数据包大小最小化
 * </p>
 * <p>
 * 数据包结构：[魔数(2字节)][版本(1字节)][类型(1字节)][序号(2字节)][长度(2字节)][压缩数据...]
 * 总头部开销仅8字节
 * </p>
 *
 * @author CH
 */
@Component
@Extension("udp")
@Slf4j
@Conditional(OnBeanCondition.class)
@RequiredArgsConstructor
public class UdpProtocol implements Protocol, InitializingBean {

    private final AuthServerProperties authServerProperties;
    private final LoginCheck loginCheck;
    private final ApplicationContext applicationContext;
    
    private DatagramSocket serverSocket;
    private final Map<String, SocketAddress> clientAddresses = new ConcurrentHashMap<>();
    
    // UDP协议常量
    private static final short MAGIC_NUMBER = (short) 0xABCD;
    private static final byte PROTOCOL_VERSION = 0x01;
    
    private static final byte LOGIN_REQUEST = 0x01;
    private static final byte LOGOUT_REQUEST = 0x02;
    private static final byte OAUTH_REQUEST = 0x03;
    private static final byte UPGRADE_REQUEST = 0x04;
    
    private static final byte SUCCESS_RESPONSE = 0x10;
    private static final byte ERROR_RESPONSE = 0x11;
    
    // UDP数据包最大大小（避免分片）
    private static final int MAX_UDP_SIZE = 1400;
    private static final int HEADER_SIZE = 8;

    @Override
    public void afterPropertiesSet() throws Exception {
        startUdpServer();
    }

    /**
     * 启动UDP服务器
     */
    private void startUdpServer() {
        CompletableFuture.runAsync(() -> {
            try {
                int port = authServerProperties.getUdpPort() != null ? 
                    authServerProperties.getUdpPort() : 8082;
                serverSocket = new DatagramSocket(port);
                log.info("UDP OAuth服务器启动，监听端口: {}", port);
                
                byte[] buffer = new byte[MAX_UDP_SIZE];
                
                while (!serverSocket.isClosed()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    serverSocket.receive(packet);
                    
                    // 使用虚拟线程处理请求
                    Executors.newVirtualThreadPerTaskExecutor().submit(() -> 
                        handleUdpRequest(packet));
                }
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    log.error("UDP服务器异常: {}", e.getMessage());
                }
            }
        });
    }

    /**
     * 处理UDP请求
     */
    private void handleUdpRequest(DatagramPacket packet) {
        try {
            // 解析数据包
            UdpPacket udpPacket = parsePacket(packet.getData(), packet.getLength());
            if (udpPacket == null) {
                return;
            }
            
            // 记录客户端地址
            String clientKey = packet.getAddress().getHostAddress() + ":" + packet.getPort();
            clientAddresses.put(clientKey, packet.getSocketAddress());
            
            // 处理请求
            ReturnResult<String> result = processUdpRequest(udpPacket, packet.getSocketAddress());
            
            // 发送响应
            sendUdpResponse(packet.getSocketAddress(), udpPacket.getSequence(), result);
            
        } catch (Exception e) {
            log.error("处理UDP请求异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 解析UDP数据包
     */
    private UdpPacket parsePacket(byte[] data, int length) {
        if (length < HEADER_SIZE) {
            log.warn("UDP数据包长度不足");
            return null;
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(data, 0, length);
        
        // 验证魔数
        short magic = buffer.getShort();
        if (magic != MAGIC_NUMBER) {
            log.warn("UDP数据包魔数错误: {}", Integer.toHexString(magic));
            return null;
        }
        
        // 读取头部信息
        byte version = buffer.get();
        byte type = buffer.get();
        short sequence = buffer.getShort();
        short dataLength = buffer.getShort();
        
        if (version != PROTOCOL_VERSION) {
            log.warn("UDP协议版本不支持: {}", version);
            return null;
        }
        
        // 读取数据
        byte[] packetData = new byte[dataLength];
        buffer.get(packetData);
        
        // 解压缩数据
        String jsonData = decompressData(packetData);
        
        return new UdpPacket(type, sequence, jsonData);
    }

    /**
     * 处理UDP请求
     */
    private ReturnResult<String> processUdpRequest(UdpPacket udpPacket, SocketAddress clientAddress) {
        try {
            // 创建UDP协议专用的请求和响应对象
            UdpAuthInformation.UdpRequest udpRequest = new UdpAuthInformation.UdpRequest((InetSocketAddress) clientAddress);
            UdpAuthInformation.UdpResponse udpResponse = new UdpAuthInformation.UdpResponse();
            
            AuthRequest authRequest = Json.fromJson(udpPacket.getData(), AuthRequest.class);
            
            switch (udpPacket.getType()) {
                case LOGIN_REQUEST:
                    return handleLogin(authRequest, udpRequest, udpResponse);
                case LOGOUT_REQUEST:
                    return handleLogout(authRequest, udpRequest, udpResponse);
                case OAUTH_REQUEST:
                    return handleOauth(authRequest, udpRequest, udpResponse);
                case UPGRADE_REQUEST:
                    return handleUpgrade(authRequest, udpRequest, udpResponse);
                default:
                    return ReturnResult.illegal("未知的请求类型");
            }
        } catch (Exception e) {
            log.error("处理UDP请求异常: {}", e.getMessage(), e);
            return ReturnResult.error("服务器内部错误");
        }
    }

    /**
     * 发送UDP响应
     */
    private void sendUdpResponse(SocketAddress clientAddress, short sequence, ReturnResult<String> result) {
        try {
            byte responseType = result.isOk() ? SUCCESS_RESPONSE : ERROR_RESPONSE;
            String responseData = Json.toJson(result);
            
            // 压缩响应数据
            byte[] compressedData = compressData(responseData);
            
            // 检查数据包大小
            if (compressedData.length + HEADER_SIZE > MAX_UDP_SIZE) {
                log.warn("UDP响应数据包过大，将被截断: {} > {}", 
                    compressedData.length + HEADER_SIZE, MAX_UDP_SIZE);
                // 可以考虑分片或者返回错误
                compressedData = "响应数据过大".getBytes(StandardCharsets.UTF_8);
                responseType = ERROR_RESPONSE;
            }
            
            // 构建响应数据包
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + compressedData.length);
            buffer.putShort(MAGIC_NUMBER);
            buffer.put(PROTOCOL_VERSION);
            buffer.put(responseType);
            buffer.putShort(sequence);
            buffer.putShort((short) compressedData.length);
            buffer.put(compressedData);
            
            // 发送响应
            DatagramPacket responsePacket = new DatagramPacket(
                buffer.array(), buffer.position(), clientAddress);
            serverSocket.send(responsePacket);
            
            log.debug("发送UDP响应，序号: {}, 大小: {}字节", sequence, buffer.position());
            
        } catch (IOException e) {
            log.error("发送UDP响应异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理登录请求
     */
    private ReturnResult<String> handleLogin(AuthRequest authRequest, UdpAuthInformation.UdpRequest request, UdpAuthInformation.UdpResponse response) {
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
    private ReturnResult<String> handleLogout(AuthRequest authRequest, UdpAuthInformation.UdpRequest request, UdpAuthInformation.UdpResponse response) {
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
    private ReturnResult<String> handleOauth(AuthRequest authRequest, UdpAuthInformation.UdpRequest request, UdpAuthInformation.UdpResponse response) {
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
    private ReturnResult<String> handleUpgrade(AuthRequest authRequest, UdpAuthInformation.UdpRequest request, UdpAuthInformation.UdpResponse response) {
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
    private void setupRequestHeaders(UdpAuthInformation.UdpRequest request, AuthRequest authRequest) {
        // 设置OAuth协议必需的头信息
        request.setHeader("x-oauth-uuid", java.util.UUID.randomUUID().toString());
        request.setHeader("x-oauth-serial", java.util.UUID.randomUUID().toString());
        request.setHeader("x-oauth-protocol", "UDP");
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

    /**
     * UDP数据包封装
     */
    private static class UdpPacket {
        private final byte type;
        private final short sequence;
        private final String data;

        public UdpPacket(byte type, short sequence, String data) {
            this.type = type;
            this.sequence = sequence;
            this.data = data;
        }

        public byte getType() { return type; }
        public short getSequence() { return sequence; }
        public String getData() { return data; }
    }

}
