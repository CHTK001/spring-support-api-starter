package com.chua.starter.oauth.sso.support.information;

import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import org.springframework.context.ApplicationContext;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UDP协议认证信息实现类
 * 
 * @author CH
 */
public abstract class UdpAuthInformation extends BaseAuthInformation {

    /**
     * UDP请求对象
     */
    public static class UdpRequest {
        private final SocketAddress clientAddress;
        private final Map<String, Object> attributes = new ConcurrentHashMap<>();
        private final Map<String, String> headers = new ConcurrentHashMap<>();

        public UdpRequest(SocketAddress clientAddress) {
            this.clientAddress = clientAddress;
        }

        public String getRemoteAddr() {
            if (clientAddress instanceof InetSocketAddress) {
                return ((InetSocketAddress) clientAddress).getAddress().getHostAddress();
            }
            return clientAddress.toString();
        }

        public int getRemotePort() {
            if (clientAddress instanceof InetSocketAddress) {
                return ((InetSocketAddress) clientAddress).getPort();
            }
            return 0;
        }

        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }

        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        public void setHeader(String name, String value) {
            headers.put(name, value);
        }

        public String getHeader(String name) {
            return headers.get(name);
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public SocketAddress getClientAddress() {
            return clientAddress;
        }
    }

    /**
     * UDP响应对象
     */
    public static class UdpResponse {
        private final Map<String, String> headers = new ConcurrentHashMap<>();
        private int status = 200;
        private String characterEncoding = "UTF-8";

        public void setHeader(String name, String value) {
            headers.put(name, value);
        }

        public String getHeader(String name) {
            return headers.get(name);
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public void setCharacterEncoding(String encoding) {
            this.characterEncoding = encoding;
        }

        public String getCharacterEncoding() {
            return characterEncoding;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }
    }

    private final UdpRequest request;
    private final UdpResponse response;

    public UdpAuthInformation(AuthServerProperties authServerProperties,
                             String data,
                             UdpRequest request,
                             UdpResponse response,
                             ApplicationContext applicationContext,
                             LoginCheck loginCheck) {
        super(authServerProperties, data, request, response, applicationContext, loginCheck);
        this.request = request;
        this.response = response;
    }

    @Override
    protected String getRequestHeader(Object request, String headerName) {
        if (request instanceof UdpRequest) {
            return ((UdpRequest) request).getHeader(headerName);
        }
        return null;
    }

    @Override
    protected void setResponseHeader(Object response, String headerName, String headerValue) {
        if (response instanceof UdpResponse) {
            ((UdpResponse) response).setHeader(headerName, headerValue);
        }
    }

    @Override
    protected String getRemoteAddress(Object request) {
        if (request instanceof UdpRequest) {
            return ((UdpRequest) request).getRemoteAddr();
        }
        return null;
    }

    @Override
    protected Object getRequestAttribute(Object request, String attributeName) {
        if (request instanceof UdpRequest) {
            return ((UdpRequest) request).getAttribute(attributeName);
        }
        return null;
    }

    @Override
    protected void setRequestAttribute(Object request, String attributeName, Object attributeValue) {
        if (request instanceof UdpRequest) {
            ((UdpRequest) request).setAttribute(attributeName, attributeValue);
        }
    }

    @Override
    protected Object getResponse() {
        return response;
    }

    @Override
    protected Object getRequest() {
        return request;
    }

    // 提供类型安全的访问方法
    public UdpRequest getUdpRequest() {
        return request;
    }

    public UdpResponse getUdpResponse() {
        return response;
    }
}
