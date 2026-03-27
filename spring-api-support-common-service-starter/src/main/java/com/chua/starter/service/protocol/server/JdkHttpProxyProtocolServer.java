package com.chua.starter.service.protocol.server;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.core.annotation.SpiDescribe;
import com.chua.common.support.network.protocol.ServerSetting;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 基于 JDK Socket 的 HTTP 代理服务端。
 */
@Slf4j
@Spi({"http-proxy", "httpproxy", "httpProxy", "proxy-http"})
@SpiDescribe("JDK HTTP代理服务端")
public class JdkHttpProxyProtocolServer extends AbstractJdkProxyProtocolServer {

    private static final int MAX_HEADER_SIZE = 64 * 1024;

    public JdkHttpProxyProtocolServer(ServerSetting serverSetting) {
        super(serverSetting);
    }

    @Override
    protected void handleConnection(String connectionId, Socket clientSocket, ProxySocketSession session) throws Exception {
        BufferedInputStream clientInput = new BufferedInputStream(clientSocket.getInputStream());
        HttpRequestHead head = readRequestHead(clientInput);
        if (head == null) {
            return;
        }
        if ("CONNECT".equalsIgnoreCase(head.method())) {
            handleConnect(clientSocket, head);
            return;
        }
        handleForward(clientSocket, clientInput, head);
    }

    private void handleConnect(Socket clientSocket, HttpRequestHead head) throws Exception {
        HostPort hostPort = parseAuthority(head.requestTarget());
        if (hostPort == null) {
            writeHttpError(clientSocket, 400, "Bad Request", "invalid CONNECT target");
            return;
        }
        try (Socket remote = openRemoteSocket(hostPort.host(), hostPort.port())) {
            writeAsciiResponse(clientSocket, "HTTP/1.1 200 Connection Established\r\n\r\n");
            tunnel(clientSocket, remote);
        } catch (Exception e) {
            log.warn("[http-proxy] CONNECT失败: {}:{}", hostPort.host(), hostPort.port(), e);
            writeHttpError(clientSocket, 502, "Bad Gateway", e.getMessage());
        }
    }

    private void handleForward(Socket clientSocket, BufferedInputStream clientInput, HttpRequestHead head) throws Exception {
        ForwardTarget target = resolveForwardTarget(head);
        if (target == null) {
            writeHttpError(clientSocket, 400, "Bad Request", "unable to resolve upstream target");
            return;
        }

        try (Socket remote = openRemoteSocket(target.host(), target.port())) {
            OutputStream remoteOut = remote.getOutputStream();
            remoteOut.write(buildForwardRequest(head, target.path()).getBytes(StandardCharsets.ISO_8859_1));
            remoteOut.flush();

            long contentLength = head.contentLength();
            if (contentLength > 0) {
                copyFixedLength(clientInput, remoteOut, contentLength);
            } else if (head.chunked()) {
                transferChunkedBody(clientInput, remoteOut);
            }
            remote.shutdownOutput();

            remote.getInputStream().transferTo(clientSocket.getOutputStream());
            clientSocket.getOutputStream().flush();
        } catch (Exception e) {
            log.warn("[http-proxy] 转发失败: {}:{} {}", target.host(), target.port(), head.requestTarget(), e);
            writeHttpError(clientSocket, 502, "Bad Gateway", e.getMessage());
        }
    }

    private HttpRequestHead readRequestHead(BufferedInputStream inputStream) throws IOException {
        ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        int matched = 0;
        while (headerBuffer.size() < MAX_HEADER_SIZE) {
            int current = inputStream.read();
            if (current == -1) {
                break;
            }
            headerBuffer.write(current);
            if ((matched == 0 || matched == 2) && current == '\r') {
                matched++;
            } else if ((matched == 1 || matched == 3) && current == '\n') {
                matched++;
                if (matched == 4) {
                    break;
                }
            } else {
                matched = 0;
            }
        }

        if (headerBuffer.size() == 0) {
            return null;
        }
        String headerText = headerBuffer.toString(StandardCharsets.ISO_8859_1);
        String[] lines = headerText.split("\r\n");
        if (lines.length == 0) {
            return null;
        }
        String[] requestLine = lines[0].split(" ", 3);
        if (requestLine.length < 3) {
            return null;
        }
        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) {
                break;
            }
            int index = line.indexOf(':');
            if (index <= 0) {
                continue;
            }
            headers.put(line.substring(0, index).trim(), line.substring(index + 1).trim());
        }
        long contentLength = parseLong(headers, "Content-Length");
        boolean chunked = containsIgnoreCase(headers, "Transfer-Encoding", "chunked");
        return new HttpRequestHead(requestLine[0], requestLine[1], requestLine[2], headers, contentLength, chunked);
    }

    private String buildForwardRequest(HttpRequestHead head, String path) {
        StringBuilder builder = new StringBuilder();
        builder.append(head.method()).append(' ').append(path).append(' ').append(head.version()).append("\r\n");
        for (Map.Entry<String, String> entry : head.headers().entrySet()) {
            String name = entry.getKey();
            if (equalsIgnoreCase(name, "Proxy-Connection")
                    || equalsIgnoreCase(name, "Proxy-Authorization")
                    || equalsIgnoreCase(name, "Connection")) {
                continue;
            }
            builder.append(name).append(": ").append(entry.getValue()).append("\r\n");
        }
        builder.append("Connection: close\r\n\r\n");
        return builder.toString();
    }

    private ForwardTarget resolveForwardTarget(HttpRequestHead head) {
        try {
            if (head.requestTarget().startsWith("http://") || head.requestTarget().startsWith("https://")) {
                URI uri = URI.create(head.requestTarget());
                String host = uri.getHost();
                if (host == null) {
                    return null;
                }
                int port = uri.getPort() > 0 ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
                String path = uri.getRawPath();
                if (path == null || path.isEmpty()) {
                    path = "/";
                }
                if (uri.getRawQuery() != null) {
                    path += "?" + uri.getRawQuery();
                }
                return new ForwardTarget(host, port, path);
            }
            String hostHeader = findHeader(head.headers(), "Host");
            if (hostHeader == null) {
                return null;
            }
            HostPort hostPort = parseAuthority(hostHeader);
            if (hostPort == null) {
                return null;
            }
            return new ForwardTarget(hostPort.host(), hostPort.port(), head.requestTarget());
        } catch (Exception e) {
            log.warn("[http-proxy] 解析上游目标失败: {}", head.requestTarget(), e);
            return null;
        }
    }

    private HostPort parseAuthority(String authority) {
        if (authority == null || authority.isBlank()) {
            return null;
        }
        String text = authority.trim();
        if (text.startsWith("[")) {
            int end = text.indexOf(']');
            if (end < 0) {
                return null;
            }
            String host = text.substring(1, end);
            int port = 80;
            if (end + 2 <= text.length() && text.charAt(end + 1) == ':') {
                port = Integer.parseInt(text.substring(end + 2));
            }
            return new HostPort(host, port);
        }
        int lastColon = text.lastIndexOf(':');
        if (lastColon > 0 && text.indexOf(':') == lastColon) {
            return new HostPort(text.substring(0, lastColon), Integer.parseInt(text.substring(lastColon + 1)));
        }
        return new HostPort(text, 80);
    }

    private void copyFixedLength(BufferedInputStream inputStream, OutputStream outputStream, long length) throws IOException {
        byte[] buffer = new byte[8192];
        long remaining = length;
        while (remaining > 0) {
            int len = inputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (len == -1) {
                break;
            }
            outputStream.write(buffer, 0, len);
            outputStream.flush();
            remaining -= len;
        }
    }

    private void transferChunkedBody(BufferedInputStream inputStream, OutputStream outputStream) throws IOException {
        while (true) {
            String sizeLine = readAsciiLine(inputStream);
            if (sizeLine == null) {
                return;
            }
            outputStream.write((sizeLine + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
            int semicolonIndex = sizeLine.indexOf(';');
            String numberText = semicolonIndex >= 0 ? sizeLine.substring(0, semicolonIndex) : sizeLine;
            int chunkSize = Integer.parseInt(numberText.trim(), 16);
            if (chunkSize == 0) {
                String trailer;
                do {
                    trailer = readAsciiLine(inputStream);
                    outputStream.write((trailer == null ? "" : trailer).getBytes(StandardCharsets.ISO_8859_1));
                    outputStream.write("\r\n".getBytes(StandardCharsets.ISO_8859_1));
                } while (trailer != null && !trailer.isEmpty());
                outputStream.flush();
                return;
            }
            copyFixedLength(inputStream, outputStream, chunkSize);
            outputStream.write('\r');
            outputStream.write('\n');
            int cr = inputStream.read();
            int lf = inputStream.read();
            if (cr != '\r' || lf != '\n') {
                throw new IOException("invalid chunk tail");
            }
            outputStream.flush();
        }
    }

    private String readAsciiLine(BufferedInputStream inputStream) throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int previous = -1;
        while (true) {
            int current = inputStream.read();
            if (current == -1) {
                return line.size() == 0 ? null : line.toString(StandardCharsets.ISO_8859_1);
            }
            if (previous == '\r' && current == '\n') {
                line.write('\r');
                byte[] bytes = line.toByteArray();
                return new String(bytes, 0, Math.max(0, bytes.length - 1), StandardCharsets.ISO_8859_1);
            }
            if (previous != -1) {
                line.write(previous);
            }
            previous = current;
        }
    }

    private long parseLong(Map<String, String> headers, String name) {
        try {
            String value = findHeader(headers, name);
            return value == null ? -1L : Long.parseLong(value.trim());
        } catch (Exception e) {
            return -1L;
        }
    }

    private boolean containsIgnoreCase(Map<String, String> headers, String name, String expected) {
        String value = findHeader(headers, name);
        return value != null && value.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && left.equalsIgnoreCase(right);
    }

    private String findHeader(Map<String, String> headers, String name) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private record HttpRequestHead(String method, String requestTarget, String version,
                                   Map<String, String> headers, long contentLength, boolean chunked) {
    }

    private record ForwardTarget(String host, int port, String path) {
    }

    private record HostPort(String host, int port) {
    }
}
