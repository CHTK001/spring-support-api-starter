package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.starter.pojo.ConnectionTestResultDTO;
import com.chua.starter.monitor.starter.service.ConnectionTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 连接测试服务实现
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@Service
public class ConnectionTestServiceImpl implements ConnectionTestService {

    private static final int DEFAULT_TIMEOUT = 5000; // 5秒超时

    @Override
    public ReturnResult<ConnectionTestResultDTO> testHttpConnection(String url) {
        if (StringUtils.isEmpty(url)) {
            return ReturnResult.error("URL不能为空");
        }

        long startTime = System.currentTimeMillis();
        ConnectionTestResultDTO.ConnectionTestResultDTOBuilder builder = ConnectionTestResultDTO.builder()
                .address(url)
                .connectionType("HTTP")
                .testTime(LocalDateTime.now());

        try {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(DEFAULT_TIMEOUT);
            connection.setReadTimeout(DEFAULT_TIMEOUT);
            connection.setInstanceFollowRedirects(true);
            
            // 设置User-Agent避免某些服务器拒绝请求
            connection.setRequestProperty("User-Agent", "Monitor-Service/1.0");

            int responseCode = connection.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;

            Map<String, Object> extendedInfo = new HashMap<>();
            extendedInfo.put("contentType", connection.getContentType());
            extendedInfo.put("contentLength", connection.getContentLength());
            extendedInfo.put("lastModified", connection.getLastModified());

            boolean success = responseCode >= 200 && responseCode < 400;
            
            return ReturnResult.ok(builder
                    .success(success)
                    .responseTime(responseTime)
                    .httpStatusCode(responseCode)
                    .message(success ? "连接成功" : "HTTP错误: " + responseCode)
                    .extendedInfo(extendedInfo)
                    .build());

        } catch (MalformedURLException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.warn("URL格式错误: {}", e.getMessage());
            return ReturnResult.ok(builder
                    .success(false)
                    .responseTime(responseTime)
                    .errorMessage("URL格式错误: " + e.getMessage())
                    .message("URL格式错误")
                    .build());
        } catch (IOException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.warn("HTTP连接失败: {}", e.getMessage());
            return ReturnResult.ok(builder
                    .success(false)
                    .responseTime(responseTime)
                    .errorMessage("连接失败: " + e.getMessage())
                    .message("连接失败")
                    .build());
        }
    }

    @Override
    public ReturnResult<ConnectionTestResultDTO> testSocketConnection(String host, int port) {
        if (StringUtils.isEmpty(host)) {
            return ReturnResult.error("主机地址不能为空");
        }

        if (port <= 0 || port > 65535) {
            return ReturnResult.error("端口号无效");
        }

        String address = host + ":" + port;
        long startTime = System.currentTimeMillis();
        ConnectionTestResultDTO.ConnectionTestResultDTOBuilder builder = ConnectionTestResultDTO.builder()
                .address(address)
                .connectionType("SOCKET")
                .testTime(LocalDateTime.now());

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), DEFAULT_TIMEOUT);
            long responseTime = System.currentTimeMillis() - startTime;

            Map<String, Object> extendedInfo = new HashMap<>();
            extendedInfo.put("localAddress", socket.getLocalAddress().getHostAddress());
            extendedInfo.put("localPort", socket.getLocalPort());
            extendedInfo.put("remoteAddress", socket.getRemoteSocketAddress().toString());

            return ReturnResult.ok(builder
                    .success(true)
                    .responseTime(responseTime)
                    .message("Socket连接成功")
                    .extendedInfo(extendedInfo)
                    .build());

        } catch (IOException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return ReturnResult.ok(builder
                    .success(false)
                    .responseTime(responseTime)
                    .errorMessage("Socket连接失败: " + e.getMessage())
                    .message("Socket连接失败")
                    .build());
        }
    }

    @Override
    public ReturnResult<ConnectionTestResultDTO> testPrometheusConnection(String prometheusUrl) {
        if (StringUtils.isEmpty(prometheusUrl)) {
            return ReturnResult.error("Prometheus地址不能为空");
        }

        // 确保URL以/结尾
        if (!prometheusUrl.endsWith("/")) {
            prometheusUrl += "/";
        }

        // 测试Prometheus的健康检查端点
        String healthUrl = prometheusUrl + "-/healthy";
        
        long startTime = System.currentTimeMillis();
        ConnectionTestResultDTO.ConnectionTestResultDTOBuilder builder = ConnectionTestResultDTO.builder()
                .address(prometheusUrl)
                .connectionType("PROMETHEUS")
                .testTime(LocalDateTime.now());

        try {
            URL urlObj = new URL(healthUrl);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(DEFAULT_TIMEOUT);
            connection.setReadTimeout(DEFAULT_TIMEOUT);
            connection.setRequestProperty("User-Agent", "Monitor-Service/1.0");

            int responseCode = connection.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;

            Map<String, Object> extendedInfo = new HashMap<>();
            extendedInfo.put("healthCheckUrl", healthUrl);
            extendedInfo.put("serverHeader", connection.getHeaderField("Server"));

            boolean success = responseCode == 200;
            
            return ReturnResult.ok(builder
                    .success(success)
                    .responseTime(responseTime)
                    .httpStatusCode(responseCode)
                    .message(success ? "Prometheus服务器连接成功" : "Prometheus服务器响应异常: " + responseCode)
                    .extendedInfo(extendedInfo)
                    .build());

        } catch (MalformedURLException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return ReturnResult.ok(builder
                    .success(false)
                    .responseTime(responseTime)
                    .errorMessage("Prometheus URL格式错误: " + e.getMessage())
                    .message("URL格式错误")
                    .build());
        } catch (IOException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return ReturnResult.ok(builder
                    .success(false)
                    .responseTime(responseTime)
                    .errorMessage("Prometheus连接失败: " + e.getMessage())
                    .message("连接失败")
                    .build());
        }
    }

    @Override
    public ReturnResult<ConnectionTestResultDTO> testConnection(String address) {
        if (StringUtils.isEmpty(address)) {
            return ReturnResult.error("地址不能为空");
        }

        address = address.trim();

        // 判断连接类型并调用相应的测试方法
        if (address.startsWith("http://") || address.startsWith("https://")) {
            // 检查是否为Prometheus地址
            if (address.contains("prometheus") || address.contains(":9090")) {
                return testPrometheusConnection(address);
            } else {
                return testHttpConnection(address);
            }
        } else if (address.startsWith("socket://")) {
            // 解析socket地址
            String socketAddress = address.substring(9); // 移除 "socket://"
            String[] parts = socketAddress.split(":");
            if (parts.length != 2) {
                return ReturnResult.error("Socket地址格式错误，应为 socket://host:port");
            }
            try {
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                return testSocketConnection(host, port);
            } catch (NumberFormatException e) {
                return ReturnResult.error("端口号格式错误");
            }
        } else if (address.contains(":")) {
            // 尝试解析为host:port格式
            String[] parts = address.split(":");
            if (parts.length == 2) {
                try {
                    String host = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    return testSocketConnection(host, port);
                } catch (NumberFormatException e) {
                    // 如果端口解析失败，尝试作为HTTP地址
                    return testHttpConnection("http://" + address);
                }
            }
        }

        // 默认尝试HTTP连接
        if (!address.startsWith("http://") && !address.startsWith("https://")) {
            address = "http://" + address;
        }
        return testHttpConnection(address);
    }
}
