package com.chua.starter.oauth.sso.support.information;

import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import org.springframework.context.ApplicationContext;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TCP协议认证信息实现类
 * 
 * @author CH
 */
public abstract class TcpAuthInformation extends BaseAuthInformation {

    /**
     * TCP请求对象
     */
    public static class TcpRequest {
        private final InetSocketAddress clientAddress;
        private final Map<String, Object> attributes = new ConcurrentHashMap<>();
        private final Map<String, String> headers = new ConcurrentHashMap<>();

        public TcpRequest(InetSocketAddress clientAddress) {
            this.clientAddress = clientAddress;
        }

        public String getRemoteAddr() {
            return clientAddress.getAddress().getHostAddress();
        }

        public int getRemotePort() {
            return clientAddress.getPort();
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

        public InetSocketAddress getClientAddress() {
            return clientAddress;
        }
    }

    /**
     * TCP响应对象
     */
    public static class TcpResponse {
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

    private final TcpRequest request;
    private final TcpResponse response;

    public TcpAuthInformation(AuthServerProperties authServerProperties,
                             String data,
                             TcpRequest request,
                             TcpResponse response,
                             ApplicationContext applicationContext,
                             LoginCheck loginCheck) {
        super(authServerProperties, data, request, response, applicationContext, loginCheck);
        this.request = request;
        this.response = response;
    }

    @Override
    protected String getRequestHeader(Object request, String headerName) {
        if (request instanceof TcpRequest) {
            return ((TcpRequest) request).getHeader(headerName);
        }
        return null;
    }

    @Override
    protected void setResponseHeader(Object response, String headerName, String headerValue) {
        if (response instanceof TcpResponse) {
            ((TcpResponse) response).setHeader(headerName, headerValue);
        }
    }

    @Override
    protected String getRemoteAddress(Object request) {
        if (request instanceof TcpRequest) {
            return ((TcpRequest) request).getRemoteAddr();
        }
        return null;
    }

    @Override
    protected Object getRequestAttribute(Object request, String attributeName) {
        if (request instanceof TcpRequest) {
            return ((TcpRequest) request).getAttribute(attributeName);
        }
        return null;
    }

    @Override
    protected void setRequestAttribute(Object request, String attributeName, Object attributeValue) {
        if (request instanceof TcpRequest) {
            ((TcpRequest) request).setAttribute(attributeName, attributeValue);
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
    public TcpRequest getTcpRequest() {
        return request;
    }

    public TcpResponse getTcpResponse() {
        return response;
    }
}
