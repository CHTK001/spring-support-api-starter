package com.chua.starter.oauth.sso.support.information;

import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * gRPC认证信息处理类
 * <p>
 * 为gRPC协议提供认证信息处理支持，模拟HTTP请求和响应对象的行为。
 * 由于gRPC使用不同的通信模型，需要适配现有的认证信息处理框架。
 * </p>
 *
 * @author CH
 */
public class GrpcAuthInformation extends BaseAuthInformation {

    public GrpcAuthInformation(AuthServerProperties authServerProperties,
            Map<String, Object> data,
            Object request,
            Object response) {
        super(authServerProperties, data, request, response);
    }

    @Override
    protected void setResponseHeader(Object response, String name, String value) {
        if (response instanceof GrpcResponse) {
            ((GrpcResponse) response).setHeader(name, value);
        }
    }

    /**
     * gRPC请求对象适配器
     * <p>
     * 模拟HttpServletRequest的行为，为现有的认证框架提供兼容性支持。
     * </p>
     */
    @Getter
    @Setter
    public static class GrpcRequest {
        private final Map<String, Object> requestData;
        private final Map<String, Object> attributes = new ConcurrentHashMap<>();
        private final Map<String, String> headers = new ConcurrentHashMap<>();
        private InetSocketAddress clientAddress;

        public GrpcRequest(Map<String, Object> requestData) {
            this.requestData = requestData;
            // 从请求数据中提取常用的头部信息
            extractHeaders();
        }

        /**
         * 从请求数据中提取头部信息
         */
        private void extractHeaders() {
            if (requestData != null) {
                requestData.forEach((key, value) -> {
                    if (key.startsWith("x-oauth-") && value != null) {
                        headers.put(key, value.toString());
                    }
                });
            }
        }

        /**
         * 获取远程客户端地址
         */
        public String getRemoteAddr() {
            if (clientAddress != null) {
                return clientAddress.getAddress().getHostAddress();
            }
            // 从请求数据中获取地址信息
            Object address = requestData.get("x-oauth-param-address");
            return address != null ? address.toString() : "127.0.0.1";
        }

        /**
         * 获取远程客户端端口
         */
        public int getRemotePort() {
            if (clientAddress != null) {
                return clientAddress.getPort();
            }
            return 0;
        }

        /**
         * 获取请求头
         */
        public String getHeader(String name) {
            return headers.get(name);
        }

        /**
         * 设置请求头
         */
        public void setHeader(String name, String value) {
            headers.put(name, value);
        }

        /**
         * 获取所有请求头
         */
        public Map<String, String> getHeaders() {
            return headers;
        }

        /**
         * 设置属性
         */
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }

        /**
         * 获取属性
         */
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        /**
         * 获取所有属性
         */
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        /**
         * 获取请求参数
         */
        public Object getParameter(String name) {
            return requestData.get(name);
        }

        /**
         * 获取所有请求数据
         */
        public Map<String, Object> getRequestData() {
            return requestData;
        }
    }

    /**
     * gRPC响应对象适配器
     * <p>
     * 模拟HttpServletResponse的行为，为现有的认证框架提供兼容性支持。
     * </p>
     */
    @Getter
    @Setter
    public static class GrpcResponse {
        private final Map<String, String> headers = new ConcurrentHashMap<>();
        private final Map<String, Object> attributes = new ConcurrentHashMap<>();
        private int status = 200;
        private String contentType = "application/json";

        /**
         * 设置响应头
         */
        public void setHeader(String name, String value) {
            headers.put(name, value);
        }

        /**
         * 获取响应头
         */
        public String getHeader(String name) {
            return headers.get(name);
        }

        /**
         * 获取所有响应头
         */
        public Map<String, String> getHeaders() {
            return headers;
        }

        /**
         * 设置状态码
         */
        public void setStatus(int status) {
            this.status = status;
        }

        /**
         * 获取状态码
         */
        public int getStatus() {
            return status;
        }

        /**
         * 设置内容类型
         */
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        /**
         * 获取内容类型
         */
        public String getContentType() {
            return contentType;
        }

        /**
         * 设置属性
         */
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }

        /**
         * 获取属性
         */
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        /**
         * 获取所有属性
         */
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        /**
         * 添加Cookie（gRPC中不直接支持，但可以通过头部模拟）
         */
        public void addCookie(String name, String value) {
            setHeader("Set-Cookie", name + "=" + value);
        }

        /**
         * 重定向（gRPC中不直接支持，但可以通过状态码和头部模拟）
         */
        public void sendRedirect(String location) {
            setStatus(302);
            setHeader("Location", location);
        }
    }

    /**
     * 解码十六进制数据
     * <p>
     * 为了与现有的认证框架兼容，提供数据解码功能。
     * </p>
     */
    public String decodeHex(String hexData) {
        return encodeHex(hexData); // 在基类中实现
    }

    /**
     * 验证gRPC请求签名
     * <p>
     * 基于现有的签名验证机制，确保gRPC请求的完整性和安全性。
     * </p>
     */
    public static boolean verifyGrpcRequestSign(String sign, Map<String, Object> requestData, String secretKey) {
        if (sign == null || requestData == null || secretKey == null) {
            return false;
        }

        // 使用gRPC安全工具类进行签名验证
        return com.chua.starter.oauth.sso.support.security.GrpcSecurityUtils.verifySign(sign, requestData, secretKey);
    }

    /**
     * 生成gRPC响应签名
     * <p>
     * 为gRPC响应生成签名，确保响应数据的完整性。
     * </p>
     */
    public static String generateGrpcResponseSign(Map<String, Object> responseData, String secretKey) {
        if (responseData == null || secretKey == null) {
            return "";
        }

        // 使用gRPC安全工具类进行签名生成
        return com.chua.starter.oauth.sso.support.security.GrpcSecurityUtils.generateSign(responseData, secretKey);
    }

    /**
     * 检查请求时间戳，防止重放攻击
     * <p>
     * 验证请求时间戳是否在允许的时间窗口内。
     * </p>
     */
    public static boolean isValidTimestamp(long timestamp, long maxAgeMillis) {
        return com.chua.starter.oauth.sso.support.security.GrpcSecurityUtils.isValidTimestamp(timestamp, maxAgeMillis);
    }

    /**
     * 验证访问密钥和秘密密钥
     * <p>
     * 检查客户端提供的AK/SK是否有效。
     * </p>
     */
    public static boolean verifyAccessKeys(String accessKey, String secretKey,
            String expectedAccessKey, String expectedSecretKey) {
        return com.chua.starter.oauth.sso.support.security.GrpcSecurityUtils.verifyAccessKeys(
                accessKey, secretKey, expectedAccessKey, expectedSecretKey);
    }
}
