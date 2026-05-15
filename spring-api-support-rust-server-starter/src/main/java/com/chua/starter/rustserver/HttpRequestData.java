package com.chua.starter.rustserver;

import lombok.Builder;
import lombok.Data;

import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.foreign.ValueLayout.*;

/**
 * 从 Rust 共享内存解析的 HTTP 请求数据，零拷贝读取后转为 Java 对象。
 */
@Data
@Builder
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

        // 解析请求头 (TLV 格式)
        Map<String, String> headers = new LinkedHashMap<>();
        int offset = 0;
        while (offset + 4 <= headersLen) {
            int keyLen = Short.toUnsignedInt(headersSeg.get(JAVA_SHORT_UNALIGNED, offset));
            offset += 2;
            if (offset + keyLen + 2 > headersLen) break;
            String key = readString(headersSeg.asSlice(offset, keyLen));
            offset += keyLen;
            int valLen = Short.toUnsignedInt(headersSeg.get(JAVA_SHORT_UNALIGNED, offset));
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

        return HttpRequestData.builder()
                .requestId(requestId)
                .method(method)
                .path(path)
                .queryString(queryString)
                .requestUri(requestUri)
                .headers(headers)
                .body(body)
                .responseData(responseData)
                .responseCapacity(responseCapacity)
                .build();
    }

    private static String readString(MemorySegment seg, int len) {
        if (seg == null || len <= 0) return "";
        return new String(seg.asSlice(0, len).toArray(JAVA_BYTE), StandardCharsets.UTF_8);
    }

    private static String readString(MemorySegment seg) {
        return new String(seg.toArray(JAVA_BYTE), StandardCharsets.UTF_8);
    }

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
