package com.chua.starter.rustserver;

import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.foreign.ValueLayout.*;

/**
 * 从 Rust 共享内存解析的 HTTP 请求数据，零拷贝读取后转为 Java 对象。
 */
public class HttpRequestData {
    private final long requestId;
    private final String method;
    private final String path;
    private final String queryString;
    private final String requestUri;
    private final Map<String, String> headers;
    private final byte[] body;
    private final MemorySegment responseData;
    private final int responseCapacity;

    private HttpRequestData(long requestId, String method, String path, String queryString,
                           String requestUri, Map<String, String> headers, byte[] body,
                           MemorySegment responseData, int responseCapacity) {
        this.requestId = requestId;
        this.method = method;
        this.path = path;
        this.queryString = queryString;
        this.requestUri = requestUri;
        this.headers = headers;
        this.body = body;
        this.responseData = responseData;
        this.responseCapacity = responseCapacity;
    }

    public static HttpRequestData parse(
            long requestId,
            MemorySegment methodSeg, int methodLen,
            MemorySegment pathSeg, int pathLen,
            MemorySegment headersSeg, int headersLen,
            MemorySegment bodySeg, int bodyLen,
            MemorySegment responseData, int responseCapacity) {

        // 读取方法
        String method = readString(methodSeg, methodLen);

        // 读取原始路径 (含 query string)
        String rawPath = readString(pathSeg, pathLen);

        // 切分 path + query string
        String path = rawPath;
        String queryString = null;
        int qIdx = rawPath.indexOf('?');
        if (qIdx >= 0) {
            path = rawPath.substring(0, qIdx);
            queryString = rawPath.substring(qIdx + 1);
        }

        // 解析请求头 (TLV 格式, big-endian, 大小写不敏感)
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        int offset = 0;
        while (offset + 4 <= headersLen) {
            int keyLen = Short.toUnsignedInt(Short.reverseBytes(headersSeg.get(JAVA_SHORT_UNALIGNED, offset)));
            offset += 2;
            if (offset + keyLen + 2 > headersLen) break;
            String key = readString(headersSeg.asSlice(offset, keyLen));
            offset += keyLen;
            int valLen = Short.toUnsignedInt(Short.reverseBytes(headersSeg.get(JAVA_SHORT_UNALIGNED, offset)));
            offset += 2;
            if (offset + valLen > headersLen) break;
            String value = readString(headersSeg.asSlice(offset, valLen));
            offset += valLen;
            headers.put(key, value);
        }

        // 读取请求体
        byte[] body = (bodySeg != null && bodyLen > 0)
                ? bodySeg.asSlice(0, bodyLen).toArray(JAVA_BYTE)
                : new byte[0];

        String requestUri = path + (queryString != null ? "?" + queryString : "");

        return new HttpRequestData(requestId, method, path, queryString, requestUri,
                headers, body, responseData, responseCapacity);
    }

    private static String readString(MemorySegment seg, int len) {
        if (seg == null || len <= 0) return "";
        return new String(seg.asSlice(0, len).toArray(JAVA_BYTE), StandardCharsets.UTF_8);
    }

    private static String readString(MemorySegment seg) {
        return new String(seg.toArray(JAVA_BYTE), StandardCharsets.UTF_8);
    }

    // ── Getters ──

    public long getRequestId() { return requestId; }
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public String getQueryString() { return queryString; }
    public String getRequestUri() { return requestUri; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getBody() { return body; }
    public MemorySegment getResponseData() { return responseData; }
    public int getResponseCapacity() { return responseCapacity; }

    // ── 便捷方法 ──

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getContentType() {
        return getHeader("Content-Type");
    }

    public int getContentLength() {
        return body.length;
    }

    public boolean isFormPost() {
        return "POST".equalsIgnoreCase(method)
                && getContentType() != null
                && getContentType().startsWith("application/x-www-form-urlencoded");
    }
}
