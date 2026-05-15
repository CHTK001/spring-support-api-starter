package com.chua.starter.rustserver;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.foreign.MemorySegment;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.foreign.ValueLayout.*;

/**
 * 轻量级 {@link HttpServletResponse} 实现。
 * 捕获状态码、响应头和响应体，最终序列化到 Rust 共享内存缓冲区。
 */
public class RustHttpServletResponse implements HttpServletResponse {

    private int status = 200;
    private String statusMessage;
    private final Map<String, List<String>> headers = new LinkedHashMap<>();
    private final ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream(4096);
    private final ServletOutputStream outputStream;
    private PrintWriter writer;
    private String characterEncoding = StandardCharsets.UTF_8.name();
    private String contentType;
    private int bufferSize = 8192;
    private boolean committed = false;
    private Locale locale = Locale.getDefault();

    public RustHttpServletResponse() {
        this.outputStream = new ServletOutputStream() {
            @Override
            public boolean isReady() { return true; }

            @Override
            public void setWriteListener(WriteListener listener) {}

            @Override
            public void write(int b) {
                bodyBuffer.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) {
                bodyBuffer.write(b, off, len);
            }
        };
    }

    // ═══════════════════════════════════════════
    //  序列化到 Rust 缓冲区
    // ═══════════════════════════════════════════

    /**
     * 将响应写入 Rust 共享内存缓冲区。
     *
     * 缓冲区布局：
     *   [0..1]   status: u16 BE
     *   [2..5]   headers_len: i32 LE
     *   [6..9]   body_len: i32 LE
     *   [10..]   headers: TLV (key_len:u16BE + key + val_len:u16BE + val)
     *   [10+headers_len..] body
     */
    public void writeTo(MemorySegment buf, int capacity) {
        flushBuffer();

        // 序列化响应头
        byte[] headerBytes = serializeHeaders();

        // 获取响应体
        byte[] bodyBytes = bodyBuffer.toByteArray();

        int totalLen = 10 + headerBytes.length + bodyBytes.length;
        if (totalLen > capacity) {
            // 截断
            int maxBody = capacity - 10 - headerBytes.length;
            if (maxBody < 0) {
                maxBody = 0;
            }
            bodyBytes = Arrays.copyOf(bodyBytes, maxBody);
        }

        // 写入缓冲区
        buf.set(JAVA_SHORT_UNALIGNED, 0, (short) ((status >> 8) & 0xFF | (status & 0xFF) << 8)); // BE u16
        buf.set(JAVA_INT_UNALIGNED, 2, headerBytes.length);
        buf.set(JAVA_INT_UNALIGNED, 6, bodyBytes.length);

        if (headerBytes.length > 0) {
            MemorySegment.copy(MemorySegment.ofArray(headerBytes), 0, buf, 10, headerBytes.length);
        }
        if (bodyBytes.length > 0) {
            MemorySegment.copy(MemorySegment.ofArray(bodyBytes), 0, buf, 10 + headerBytes.length, bodyBytes.length);
        }
    }

    private byte[] serializeHeaders() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        headers.forEach((name, values) -> {
            if (name == null) return;
            for (String value : values) {
                if (value == null) continue;
                try {
                    byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);
                    byte[] valBytes = value.getBytes(StandardCharsets.UTF_8);
                    out.write((byte) (nameBytes.length >> 8));
                    out.write((byte) nameBytes.length);
                    out.write(nameBytes);
                    out.write((byte) (valBytes.length >> 8));
                    out.write((byte) valBytes.length);
                    out.write(valBytes);
                } catch (IOException ignored) {}
            }
        });
        return out.toByteArray();
    }

    // ═══════════════════════════════════════════
    //  Cookie
    // ═══════════════════════════════════════════

    @Override
    public void addCookie(Cookie cookie) {
        StringBuilder sb = new StringBuilder();
        sb.append(cookie.getName()).append('=').append(cookie.getValue());
        if (cookie.getDomain() != null) sb.append("; Domain=").append(cookie.getDomain());
        if (cookie.getPath() != null) sb.append("; Path=").append(cookie.getPath());
        if (cookie.getMaxAge() >= 0) sb.append("; Max-Age=").append(cookie.getMaxAge());
        if (cookie.getSecure()) sb.append("; Secure");
        if (cookie.isHttpOnly()) sb.append("; HttpOnly");
        addHeader("Set-Cookie", sb.toString());
    }

    // ═══════════════════════════════════════════
    //  状态码
    // ═══════════════════════════════════════════

    @Override
    public void setStatus(int sc) { this.status = sc; }

    @Override
    public int getStatus() { return status; }

    @Override
    public void sendError(int sc) throws IOException {
        this.status = sc;
        this.statusMessage = getStatusMessage(sc);
        committed = true;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.status = sc;
        this.statusMessage = msg != null ? msg : getStatusMessage(sc);
        committed = true;
        try {
            setContentType("text/plain; charset=UTF-8");
            getWriter().write(this.statusMessage);
        } catch (Exception ignored) {}
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        sendRedirect(location, 302, true);
    }

    public void sendRedirect(String location, int sc, boolean clearBuffer) throws IOException {
        this.status = sc;
        setHeader("Location", location);
        if (clearBuffer) bodyBuffer.reset();
        committed = true;
    }

    // ═══════════════════════════════════════════
    //  响应头
    // ═══════════════════════════════════════════

    @Override
    public void setHeader(String name, String value) {
        if (name == null) return;
        if (value == null) { headers.remove(name); return; }
        List<String> list = new ArrayList<>();
        list.add(value);
        headers.put(name, list);
    }

    @Override
    public void addHeader(String name, String value) {
        if (name == null || value == null) return;
        headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        setHeader(name, String.valueOf(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        addHeader(name, String.valueOf(value));
    }

    @Override
    public void setDateHeader(String name, long date) {
        setHeader(name, formatDate(date));
    }

    @Override
    public void addDateHeader(String name, long date) {
        addHeader(name, formatDate(date));
    }

    @Override
    public String getHeader(String name) {
        List<String> list = headers.get(name);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return headers.getOrDefault(name, Collections.emptyList());
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    // ═══════════════════════════════════════════
    //  Content-Type / Encoding
    // ═══════════════════════════════════════════

    @Override
    public void setContentType(String type) {
        this.contentType = type;
        if (type != null) {
            setHeader("Content-Type", type);
            int ci = type.toLowerCase().indexOf("charset=");
            if (ci >= 0) {
                String enc = type.substring(ci + 8).trim();
                int sc = enc.indexOf(';');
                if (sc >= 0) enc = enc.substring(0, sc);
                this.characterEncoding = enc.trim();
            }
        } else {
            headers.remove("Content-Type");
        }
    }

    @Override
    public String getContentType() { return contentType; }

    @Override
    public void setCharacterEncoding(String charset) {
        this.characterEncoding = charset;
    }

    @Override
    public String getCharacterEncoding() { return characterEncoding; }

    @Override
    public void setContentLength(int len) {
        setIntHeader("Content-Length", len);
    }

    @Override
    public void setContentLengthLong(long len) {
        setHeader("Content-Length", String.valueOf(len));
    }

    // ═══════════════════════════════════════════
    //  输出流
    // ═══════════════════════════════════════════

    @Override
    public ServletOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(outputStream, true, Charset.forName(characterEncoding));
        }
        return writer;
    }

    // ═══════════════════════════════════════════
    //  缓冲区
    // ═══════════════════════════════════════════

    @Override
    public void setBufferSize(int size) { this.bufferSize = size; }

    @Override
    public int getBufferSize() { return bufferSize; }

    @Override
    public void flushBuffer() {
        if (writer != null) writer.flush();
        try { outputStream.flush(); } catch (IOException ignored) {}
        committed = true;
    }

    @Override
    public boolean isCommitted() { return committed; }

    @Override
    public void reset() {
        if (committed) throw new IllegalStateException("Response already committed");
        status = 200;
        statusMessage = null;
        headers.clear();
        bodyBuffer.reset();
    }

    @Override
    public void resetBuffer() {
        if (committed) throw new IllegalStateException("Response already committed");
        bodyBuffer.reset();
    }

    // ═══════════════════════════════════════════
    //  URL 编码
    // ═══════════════════════════════════════════

    @Override
    public String encodeURL(String url) { return url; }

    @Override
    public String encodeRedirectURL(String url) { return url; }

    // ═══════════════════════════════════════════
    //  Locale
    // ═══════════════════════════════════════════

    @Override
    public void setLocale(Locale loc) { this.locale = loc; }

    @Override
    public Locale getLocale() { return locale; }

    // ═══════════════════════════════════════════
    //  杂项
    // ═══════════════════════════════════════════

    private static String formatDate(long date) {
        return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
                .format(new Date(date));
    }

    private static String getStatusMessage(int sc) {
        // 常用状态码消息
        switch (sc) {
            case 200: return "OK";
            case 201: return "Created";
            case 204: return "No Content";
            case 301: return "Moved Permanently";
            case 302: return "Found";
            case 304: return "Not Modified";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 408: return "Request Timeout";
            case 500: return "Internal Server Error";
            case 502: return "Bad Gateway";
            case 503: return "Service Unavailable";
            default: return "";
        }
    }
}
