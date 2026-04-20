package com.chua.starter.service.protocol.server;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.core.annotation.SpiDescribe;
import com.chua.common.support.network.discovery.Discovery;
import com.chua.common.support.network.protocol.ServerSetting;
import com.chua.common.support.network.protocol.filter.ServletFilter;
import com.chua.common.support.network.protocol.request.BadServletResponse;
import com.chua.common.support.network.protocol.request.RequestAttributes;
import com.chua.common.support.network.protocol.request.RequestHeaders;
import com.chua.common.support.network.protocol.request.RequestParameters;
import com.chua.common.support.network.protocol.request.SocketServletRequest;
import com.chua.common.support.network.protocol.server.DefaultServletFilterChain;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 基于 JDK Socket 的 SOCKS5 代理服务端。
 */
@Slf4j
@Spi({"socks5", "sock5", "socks", "socks5-proxy"})
@SpiDescribe("JDK SOCKS5代理服务端")
public class JdkSocks5ProxyProtocolServer extends AbstractJdkProxyProtocolServer {

    private static final int SOCKS_VERSION = 0x05;
    private static final int CMD_CONNECT = 0x01;
    private static final int ATYP_IPV4 = 0x01;
    private static final int ATYP_DOMAIN = 0x03;
    private static final int ATYP_IPV6 = 0x04;

    public JdkSocks5ProxyProtocolServer(ServerSetting serverSetting) {
        super(serverSetting);
    }

    @Override
    protected void handleConnection(String connectionId, Socket clientSocket, ProxySocketSession session) throws Exception {
        BufferedInputStream inputStream = new BufferedInputStream(clientSocket.getInputStream());
        OutputStream outputStream = clientSocket.getOutputStream();

        if (!negotiate(inputStream, outputStream)) {
            return;
        }
        Socks5Target target = readRequest(inputStream);
        if (target == null) {
            sendReply(outputStream, 0x01, InetAddress.getByName("0.0.0.0"), 0);
            return;
        }
        Socks5Target resolvedTarget = resolveMappedTarget(connectionId, clientSocket, session, target);

        try (Socket remoteSocket = openRemoteSocket(resolvedTarget.host(), resolvedTarget.port())) {
            sendReply(outputStream, 0x00, remoteSocket.getLocalAddress(), remoteSocket.getLocalPort());
            tunnel(clientSocket, remoteSocket);
        } catch (Exception e) {
            log.warn("[socks5] 连接上游失败: {}:{} (原始目标 {}:{})",
                    resolvedTarget.host(), resolvedTarget.port(), target.host(), target.port(), e);
            sendReply(outputStream, 0x05, InetAddress.getByName("0.0.0.0"), 0);
        }
    }

    private Socks5Target resolveMappedTarget(String connectionId,
                                             Socket clientSocket,
                                             ProxySocketSession session,
                                             Socks5Target originalTarget) {
        List<ServletFilter> filters = getFilters();
        if (filters == null || filters.isEmpty()) {
            return originalTarget;
        }

        SocketServletRequest request = SocketServletRequest.builder()
                .clientId(connectionId)
                .remoteAddress(resolveHostAddress(clientSocket.getInetAddress()))
                .remotePort(clientSocket.getPort())
                .localAddress(resolveHostAddress(clientSocket.getLocalAddress()))
                .localPort(clientSocket.getLocalPort())
                .protocol("socks5")
                .headers(new RequestHeaders())
                .parameters(new RequestParameters())
                .attributes(new RequestAttributes())
                .session(session)
                .build()
                .init();
        request.setMethod("CONNECT");
        request.setPath("/" + originalTarget.host() + ":" + originalTarget.port());
        request.setUrl("socks5://" + originalTarget.host() + ":" + originalTarget.port());
        request.getParameters().set("target.host", originalTarget.host());
        request.getParameters().set("target.port", originalTarget.port());
        request.getParameters().set("protocol", "socks5");

        BadServletResponse response = new BadServletResponse();
        boolean invoked = false;
        for (ServletFilter filter : filters) {
            if (!isServiceDiscoveryFilter(filter) || !filter.isEnabled()) {
                continue;
            }
            invoked = true;
            try {
                new DefaultServletFilterChain(List.of(filter)).doFilter(request, response);
            } catch (Exception e) {
                log.warn("[socks5] 执行服务发现过滤器失败: {}", filter.getFilterName(), e);
            }
        }
        if (!invoked) {
            return originalTarget;
        }

        Object discoveryObject = request.getAttribute("ServiceDiscoveryServletFilter:discovery");
        if (!(discoveryObject instanceof Discovery discovery)) {
            return originalTarget;
        }

        Socks5Target mappedTarget = resolveTargetFromDiscovery(discovery, originalTarget);
        if (mappedTarget == null) {
            return originalTarget;
        }

        if (!Objects.equals(mappedTarget.host(), originalTarget.host()) || mappedTarget.port() != originalTarget.port()) {
            log.info("[socks5] 服务发现映射命中: {}:{} -> {}:{}",
                    originalTarget.host(), originalTarget.port(), mappedTarget.host(), mappedTarget.port());
        }
        return mappedTarget;
    }

    private boolean isServiceDiscoveryFilter(ServletFilter filter) {
        if (filter == null) {
            return false;
        }
        String filterName = filter.getFilterName();
        String className = filter.getClass().getSimpleName();
        String identity = ((filterName == null ? "" : filterName) + " " + className).toLowerCase(Locale.ROOT);
        return identity.contains("servicediscovery") || identity.contains("discoveryservletfilter");
    }

    private Socks5Target resolveTargetFromDiscovery(Discovery discovery, Socks5Target fallback) {
        String host = discovery.getHost();
        int port = discovery.getPort();
        if (host == null || host.isBlank()) {
            return fallback;
        }
        String resolvedHost = host;
        int resolvedPort = port;
        if (host.startsWith("http://") || host.startsWith("https://")) {
            try {
                URI uri = URI.create(host);
                if (uri.getHost() != null && !uri.getHost().isBlank()) {
                    resolvedHost = uri.getHost();
                }
                if (uri.getPort() > 0) {
                    resolvedPort = uri.getPort();
                }
            } catch (Exception ignored) {
                // ignore
            }
        }
        if (resolvedPort <= 0) {
            resolvedPort = fallback.port();
        }
        return new Socks5Target(resolvedHost, resolvedPort);
    }

    private String resolveHostAddress(InetAddress address) {
        return address == null ? "0.0.0.0" : address.getHostAddress();
    }

    private boolean negotiate(BufferedInputStream inputStream, OutputStream outputStream) throws IOException {
        int version = inputStream.read();
        if (version != SOCKS_VERSION) {
            return false;
        }
        int methodCount = inputStream.read();
        if (methodCount <= 0) {
            return false;
        }
        boolean noAuth = false;
        for (int i = 0; i < methodCount; i++) {
            if (inputStream.read() == 0x00) {
                noAuth = true;
            }
        }
        outputStream.write(new byte[]{SOCKS_VERSION, noAuth ? (byte) 0x00 : (byte) 0xFF});
        outputStream.flush();
        return noAuth;
    }

    private Socks5Target readRequest(BufferedInputStream inputStream) throws IOException {
        int version = inputStream.read();
        int command = inputStream.read();
        inputStream.read(); // reserved
        int addressType = inputStream.read();
        if (version != SOCKS_VERSION || command != CMD_CONNECT) {
            return null;
        }
        String host;
        if (addressType == ATYP_IPV4) {
            byte[] ipv4 = inputStream.readNBytes(4);
            host = InetAddress.getByAddress(ipv4).getHostAddress();
        } else if (addressType == ATYP_DOMAIN) {
            int length = inputStream.read();
            host = new String(inputStream.readNBytes(length), StandardCharsets.UTF_8);
        } else if (addressType == ATYP_IPV6) {
            byte[] ipv6 = inputStream.readNBytes(16);
            host = InetAddress.getByAddress(ipv6).getHostAddress();
        } else {
            return null;
        }
        int port = ((inputStream.read() & 0xFF) << 8) | (inputStream.read() & 0xFF);
        return new Socks5Target(host, port);
    }

    private void sendReply(OutputStream outputStream, int replyCode, InetAddress bindAddress, int bindPort) throws IOException {
        byte[] addressBytes = bindAddress.getAddress();
        int atyp;
        if (bindAddress instanceof Inet4Address) {
            atyp = ATYP_IPV4;
        } else if (bindAddress instanceof Inet6Address) {
            atyp = ATYP_IPV6;
        } else {
            atyp = ATYP_IPV4;
            addressBytes = new byte[]{0, 0, 0, 0};
        }
        byte[] response = new byte[6 + addressBytes.length];
        response[0] = SOCKS_VERSION;
        response[1] = (byte) replyCode;
        response[2] = 0x00;
        response[3] = (byte) atyp;
        System.arraycopy(addressBytes, 0, response, 4, addressBytes.length);
        response[response.length - 2] = (byte) ((bindPort >> 8) & 0xFF);
        response[response.length - 1] = (byte) (bindPort & 0xFF);
        outputStream.write(response);
        outputStream.flush();
    }

    private record Socks5Target(String host, int port) {
    }
}
