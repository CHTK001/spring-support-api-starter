package com.chua.starter.oauth.sso.support.information;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RsocketAuthInformation 单元测试
 * <p>
 * 测试 RSocket 协议适配器的请求/响应对象，验证其实现了 HttpServletRequest/HttpServletResponse
 * 接口，并能正确传递 header、body、address 等信息。
 * </p>
 */
class RsocketAuthInformationTest {

    // ------------------------------------------------------------------
    // RsocketRequest 测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("RsocketRequest - 创建时应正确保存客户端地址")
    void request_shouldStoreClientAddress() {
        InetSocketAddress address = new InetSocketAddress("192.168.1.100", 12345);
        RsocketAuthInformation.RsocketRequest request =
                new RsocketAuthInformation.RsocketRequest(address);

        assertEquals("192.168.1.100", request.getRemoteAddr(),
                "应返回正确的客户端 IP");
        assertEquals(12345, request.getRemotePort(),
                "应返回正确的客户端端口");
    }

    @Test
    @DisplayName("RsocketRequest - null 地址时应返回 unknown")
    void request_withNullAddress_shouldReturnUnknown() {
        RsocketAuthInformation.RsocketRequest request =
                new RsocketAuthInformation.RsocketRequest(null);

        assertEquals("unknown", request.getRemoteAddr(),
                "null 地址时应返回 'unknown'");
        assertEquals(0, request.getRemotePort(),
                "null 地址时端口应为 0");
    }

    @Test
    @DisplayName("RsocketRequest - 应正确设置和获取 Header")
    void request_shouldSetAndGetHeaders() {
        RsocketAuthInformation.RsocketRequest request =
                new RsocketAuthInformation.RsocketRequest(null);

        request.setHeader("x-oauth-token", "abc123");
        request.setHeader("x-oauth-uuid", "test-uuid");
        request.setHeader("x-oauth-sign", "test-sign");

        assertEquals("abc123", request.getHeader("x-oauth-token"),
                "应正确返回设置的 token header");
        assertEquals("test-uuid", request.getHeader("x-oauth-uuid"),
                "应正确返回设置的 uuid header");
        assertEquals("test-sign", request.getHeader("x-oauth-sign"),
                "应正确返回设置的 sign header");
        assertNull(request.getHeader("non-existent"),
                "不存在的 header 应返回 null");
    }

    @Test
    @DisplayName("RsocketRequest - getHeaderNames 应返回所有 header 名")
    void request_getHeaderNames_shouldReturnAllHeaders() {
        RsocketAuthInformation.RsocketRequest request =
                new RsocketAuthInformation.RsocketRequest(null);

        request.setHeader("x-oauth-token", "abc");
        request.setHeader("Content-Type", "application/json");

        Enumeration<String> headerNames = request.getHeaderNames();
        Set<String> nameSet = new HashSet<>();
        while (headerNames.hasMoreElements()) {
            nameSet.add(headerNames.nextElement());
        }

        assertTrue(nameSet.contains("x-oauth-token"),
                "header 名集合应包含 x-oauth-token");
        assertTrue(nameSet.contains("Content-Type"),
                "header 名集合应包含 Content-Type");
        assertEquals(2, nameSet.size(), "应有 2 个 header 名");
    }

    @Test
    @DisplayName("RsocketRequest - getHeaders(name) 应返回指定 header 的值")
    void request_getHeaders_shouldReturnValuesForName() {
        RsocketAuthInformation.RsocketRequest request =
                new RsocketAuthInformation.RsocketRequest(null);

        request.setHeader("x-oauth-token", "abc123");

        Enumeration<String> headers = request.getHeaders("x-oauth-token");
        assertTrue(headers.hasMoreElements(),
                "应至少有一个 header 值");
        assertEquals("abc123", headers.nextElement(),
                "应返回正确的 header 值");
    }

    @Test
    @DisplayName("RsocketRequest - getHeaders 对不存在的 key 应返回空枚举")
    void request_getHeaders_unknownKey_shouldReturnEmpty() {
        RsocketAuthInformation.RsocketRequest request =
                new RsocketAuthInformation.RsocketRequest(null);

        Enumeration<String> headers = request.getHeaders("non-existent");
        assertFalse(headers.hasMoreElements(),
                "不存在的 key 应返回空枚举");
    }

    @Test
    @DisplayName("RsocketRequest - 应正确设置和获取 Body")
    void request_shouldSetAndGetBody() {
        RsocketAuthInformation.RsocketRequest request =
                new RsocketAuthInformation.RsocketRequest(null);

        String body = "{\"username\":\"test\",\"password\":\"123\"}";
        request.setBody(body);

        assertEquals(body, request.getBody(), "应返回正确的 body 内容");
        assertEquals(body.length(), request.getContentLength(),
                "content length 应等于 body 长度");
    }

    @Test
    @DisplayName("RsocketRequest - 应正确设置和获取 Attribute")
    void request_shouldSetAndGetAttributes() {
        RsocketAuthInformation.RsocketRequest request =
                new RsocketAuthInformation.RsocketRequest(null);

        request.setAttribute("requestBody", "raw-payload");
        request.setAttribute("traceId", "trace-123");

        assertEquals("raw-payload", request.getAttribute("requestBody"),
                "应返回正确的 requestBody attribute");
        assertEquals("trace-123", request.getAttribute("traceId"),
                "应返回正确的 traceId attribute");
        assertNull(request.getAttribute("non-existent"),
                "不存在的 attribute 应返回 null");
    }

    @Test
    @DisplayName("RsocketRequest - removeAttribute 应正确移除")
    void request_removeAttribute_shouldWork() {
        RsocketAuthInformation.RsocketRequest request =
                new RsocketAuthInformation.RsocketRequest(null);

        request.setAttribute("key", "value");
        assertEquals("value", request.getAttribute("key"));

        request.removeAttribute("key");
        assertNull(request.getAttribute("key"),
                "移除后应返回 null");
    }

    @Test
    @DisplayName("RsocketRequest - getAttributeNames 应返回所有属性名")
    void request_getAttributeNames_shouldReturnAllAttributes() {
        RsocketAuthInformation.RsocketRequest request =
                new RsocketAuthInformation.RsocketRequest(null);

        request.setAttribute("a", 1);
        request.setAttribute("b", 2);

        Enumeration<String> names = request.getAttributeNames();
        Set<String> nameSet = new HashSet<>();
        while (names.hasMoreElements()) {
            nameSet.add(names.nextElement());
        }

        assertTrue(nameSet.contains("a"));
        assertTrue(nameSet.contains("b"));
        assertEquals(2, nameSet.size());
    }

    @Test
    @DisplayName("RsocketRequest - 固定属性值验证")
    void request_fixedProperties_shouldBeCorrect() {
        RsocketAuthInformation.RsocketRequest request =
                new RsocketAuthInformation.RsocketRequest(null);

        assertEquals("POST", request.getMethod(), "method 应为 POST");
        assertEquals("RSocket/1.0", request.getProtocol(), "protocol 应为 RSocket/1.0");
        assertEquals("rsocket", request.getScheme(), "scheme 应为 rsocket");
        assertEquals("application/json", request.getContentType(), "contentType 应为 application/json");
        assertEquals("UTF-8", request.getCharacterEncoding(), "encoding 应为 UTF-8");
        assertEquals("/", request.getRequestURI(), "requestURI 应为 /");
        assertEquals(7000, request.getServerPort(), "serverPort 应为 7000");
        assertEquals("localhost", request.getServerName(), "serverName 应为 localhost");
        assertEquals("127.0.0.1", request.getLocalAddr(), "localAddr 应为 127.0.0.1");
        assertEquals("rsocket://localhost/", request.getRequestURL().toString(),
                "requestURL 应为 rsocket://localhost/");
        assertEquals("", request.getContextPath(), "contextPath 应为空字符串");
        assertEquals("", request.getServletPath(), "servletPath 应为空字符串");
        assertFalse(request.isSecure(), "isSecure 应为 false");
    }

    @Test
    @DisplayName("RsocketRequest - getReader 应返回包含 body 的 BufferedReader")
    void request_getReader_shouldContainBody() throws Exception {
        RsocketAuthInformation.RsocketRequest request =
                new RsocketAuthInformation.RsocketRequest(null);

        String body = "line1\nline2\nline3";
        request.setBody(body);

        var reader = request.getReader();
        assertNotNull(reader, "reader 不应为 null");
        assertEquals("line1", reader.readLine(), "第一行应为 line1");
        assertEquals("line2", reader.readLine(), "第二行应为 line2");
        assertEquals("line3", reader.readLine(), "第三行应为 line3");
        assertNull(reader.readLine(), "第四行应为 null");
    }

    @Test
    @DisplayName("RsocketRequest - null body 时 getReader 应返回空 reader")
    void request_nullBody_getReader_shouldReturnEmptyReader() throws Exception {
        RsocketAuthInformation.RsocketRequest request =
                new RsocketAuthInformation.RsocketRequest(null);

        var reader = request.getReader();
        assertNotNull(reader, "reader 不应为 null");
        assertNull(reader.readLine(), "空 body 时 reader 应返回 null");
    }

    // ------------------------------------------------------------------
    // RsocketResponse 测试
    // ------------------------------------------------------------------

    @Test
    @DisplayName("RsocketResponse - 应正确设置和获取 Header")
    void response_shouldSetAndGetHeaders() {
        RsocketAuthInformation.RsocketResponse response =
                new RsocketAuthInformation.RsocketResponse();

        response.setHeader("x-oauth-response-serial", "serial-123");
        response.addHeader("Cache-Control", "no-cache");

        assertEquals("serial-123", response.getHeader("x-oauth-response-serial"),
                "应正确返回设置的 serial header");
        assertEquals("no-cache", response.getHeader("Cache-Control"),
                "应正确返回设置的 cache header");
    }

    @Test
    @DisplayName("RsocketResponse - getHeaders(name) 应返回指定 header 值")
    void response_getHeaders_shouldReturnValues() {
        RsocketAuthInformation.RsocketResponse response =
                new RsocketAuthInformation.RsocketResponse();

        response.setHeader("x-custom", "value1");

        Collection<String> headers = response.getHeaders("x-custom");
        assertEquals(1, headers.size());
        assertTrue(headers.contains("value1"));
    }

    @Test
    @DisplayName("RsocketResponse - getHeaders 对不存在的 key 应返回空集合")
    void response_getHeaders_unknownKey_shouldReturnEmpty() {
        RsocketAuthInformation.RsocketResponse response =
                new RsocketAuthInformation.RsocketResponse();

        Collection<String> headers = response.getHeaders("non-existent");
        assertTrue(headers.isEmpty());
    }

    @Test
    @DisplayName("RsocketResponse - getHeaderNames 应返回所有 header 名")
    void response_getHeaderNames_shouldReturnAllNames() {
        RsocketAuthInformation.RsocketResponse response =
                new RsocketAuthInformation.RsocketResponse();

        response.setHeader("h1", "v1");
        response.setHeader("h2", "v2");

        Collection<String> names = response.getHeaderNames();
        assertTrue(names.contains("h1"));
        assertTrue(names.contains("h2"));
        assertEquals(2, names.size());
    }

    @Test
    @DisplayName("RsocketResponse - getAllHeaders 应返回所有 header 的 Map")
    void response_getAllHeaders_shouldReturnMap() {
        RsocketAuthInformation.RsocketResponse response =
                new RsocketAuthInformation.RsocketResponse();

        response.setHeader("h1", "v1");
        response.setHeader("h2", "v2");

        Map<String, String> allHeaders = response.getAllHeaders();
        assertEquals("v1", allHeaders.get("h1"));
        assertEquals("v2", allHeaders.get("h2"));
        assertEquals(2, allHeaders.size());
    }

    @Test
    @DisplayName("RsocketResponse - getAllHeaders 应返回副本（不可修改原对象）")
    void response_getAllHeaders_shouldReturnCopy() {
        RsocketAuthInformation.RsocketResponse response =
                new RsocketAuthInformation.RsocketResponse();

        response.setHeader("key", "original");

        Map<String, String> copy = response.getAllHeaders();
        copy.put("key", "modified");

        assertEquals("original", response.getHeader("key"),
                "修改 getAllHeaders 返回的 Map 不应影响原对象");
    }

    @Test
    @DisplayName("RsocketResponse - 应正确设置和获取 Body")
    void response_shouldSetAndGetBody() {
        RsocketAuthInformation.RsocketResponse response =
                new RsocketAuthInformation.RsocketResponse();

        String body = "{\"success\":true}";
        response.setBody(body);

        assertEquals(body, response.getBody(), "应返回正确的 body");
    }

    @Test
    @DisplayName("RsocketResponse - 应正确设置和获取 Status")
    void response_shouldSetAndGetStatus() {
        RsocketAuthInformation.RsocketResponse response =
                new RsocketAuthInformation.RsocketResponse();

        // 默认状态码
        assertEquals(200, response.getStatus(), "默认状态码应为 200");

        response.setStatus(401);
        assertEquals(401, response.getStatus(), "状态码应更新为 401");
    }

    @Test
    @DisplayName("RsocketResponse - containsHeader 应正确判断")
    void response_containsHeader_shouldWork() {
        RsocketAuthInformation.RsocketResponse response =
                new RsocketAuthInformation.RsocketResponse();

        response.setHeader("x-oauth-token", "abc");

        assertTrue(response.containsHeader("x-oauth-token"),
                "存在的 header 应返回 true");
        assertFalse(response.containsHeader("non-existent"),
                "不存在的 header 应返回 false");
    }

    @Test
    @DisplayName("RsocketResponse - 固定属性值验证")
    void response_fixedProperties_shouldBeCorrect() {
        RsocketAuthInformation.RsocketResponse response =
                new RsocketAuthInformation.RsocketResponse();

        assertEquals("UTF-8", response.getCharacterEncoding(),
                "encoding 应为 UTF-8");
        assertEquals("application/json", response.getContentType(),
                "contentType 应为 application/json");
        assertEquals(0, response.getBufferSize(),
                "bufferSize 应为 0");
        assertFalse(response.isCommitted(),
                "isCommitted 应为 false");
    }

    @Test
    @DisplayName("RsocketResponse - encodeURL / encodeRedirectURL 应返回原始 URL")
    void response_urlEncoding_shouldReturnOriginalUrl() {
        RsocketAuthInformation.RsocketResponse response =
                new RsocketAuthInformation.RsocketResponse();

        String url = "http://example.com/path?key=value";
        assertEquals(url, response.encodeURL(url));
        assertEquals(url, response.encodeRedirectURL(url));
    }
}
