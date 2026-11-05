// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.video.live.service;

import com.chua.starter.video.live.model.LiveChannel;
import com.chua.starter.video.properties.VideoProperties;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HLS 反向代理服务。
 *
 * <p>许多公开/官方直播源不返回 CORS 头，或仅允许特定 Referer/User-Agent，
 * 浏览器直连会失败；本服务将 m3u8 拉取到后端，重写其中的分片、子清单、密钥
 * 地址再回吐给前端，从而在浏览器中稳定播放。</p>
 *
 * <p>为防 SSRF 与开放代理滥用，仅允许代理与频道源同根域的地址，并可通过
 * {@code spring.video.live-allowed-hosts} 追加白名单后缀。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
public class HlsProxyService {

    /**
     * m3u8 内容类型。
     */
    public static final String M3U8_CONTENT_TYPE = "application/vnd.apple.mpegurl";

    /**
     * 默认分片内容类型。
     */
    private static final String DEFAULT_SEGMENT_TYPE = "video/mp2t";

    /**
     * 播放列表 URI 属性（KEY / MAP / MEDIA 等）。
     */
    private static final Pattern URI_ATTR = Pattern.compile("URI=\"([^\"]+)\"");

    /**
     * 视频服务配置。
     */
    private final VideoProperties properties;

    /**
     * 直播频道目录。
     */
    private final LiveChannelService liveChannelService;

    /**
     * HTTP 客户端。
     */
    private final HttpClient httpClient;

    /**
     * 构造 HLS 代理服务。
     *
     * @param properties         视频服务配置
     * @param liveChannelService 直播频道目录
     */
    public HlsProxyService(VideoProperties properties, LiveChannelService liveChannelService) {
        this.properties = properties;
        this.liveChannelService = liveChannelService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000, properties.getConnectTimeout())))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * 拉取并重写频道主播放列表。
     *
     * @param channelId 频道标识
     * @param sourceIndex 候选源下标
     * @return 代理响应
     */
    public ProxyResponse playlist(String channelId, int sourceIndex) {
        String source = liveChannelService.source(channelId, sourceIndex);
        if (source == null) {
            return ProxyResponse.error(404, "直播源不存在");
        }
        try {
            URI base = URI.create(source);
            ProxyResponse response = fetch(base, null);
            if (response.getStatus() != 200) {
                return response;
            }
            String body = new String(response.getBody(), StandardCharsets.UTF_8);
            if (!looksLikePlaylist(body, response.getContentType())) {
                return response;
            }
            String rewritten = rewrite(body, base, channelId, sourceIndex);
            return ProxyResponse.of(rewritten.getBytes(StandardCharsets.UTF_8), M3U8_CONTENT_TYPE, 200);
        } catch (Exception e) {
            log.warn("[Video] 直播代理拉流失败 channel={} src={}: {}", channelId, sourceIndex, e.getMessage());
            return ProxyResponse.error(502, "直播源拉取失败");
        }
    }

    /**
     * 代理子清单或分片资源。
     *
     * @param channelId   频道标识
     * @param sourceIndex 候选源下标
     * @param encoded     目标地址（Base64URL）
     * @param range       HTTP Range 头，可空
     * @return 代理响应
     */
    public ProxyResponse resource(String channelId, int sourceIndex, String encoded, String range) {
        String source = liveChannelService.source(channelId, sourceIndex);
        if (source == null) {
            return ProxyResponse.error(404, "直播源不存在");
        }
        URI target;
        try {
            target = URI.create(new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ProxyResponse.error(400, "非法资源地址");
        }
        if (!isAllowed(URI.create(source), target)) {
            log.warn("[Video] 拒绝代理越权地址: {}", target);
            return ProxyResponse.error(403, "目标地址不在允许范围");
        }
        try {
            ProxyResponse response = fetch(target, range);
            if (response.getStatus() != 200 && response.getStatus() != 206) {
                return response;
            }
            String body = new String(response.getBody(), StandardCharsets.UTF_8);
            if (looksLikePlaylist(body, response.getContentType())) {
                String rewritten = rewrite(body, target, channelId, sourceIndex);
                return ProxyResponse.of(rewritten.getBytes(StandardCharsets.UTF_8), M3U8_CONTENT_TYPE, 200);
            }
            String contentType = response.getContentType() == null || response.getContentType().isBlank()
                    ? DEFAULT_SEGMENT_TYPE : response.getContentType();
            return ProxyResponse.full(response.getBody(), contentType, response.getStatus(), response.getContentRange());
        } catch (Exception e) {
            log.warn("[Video] 直播分片代理失败 channel={}: {}", channelId, e.getMessage());
            return ProxyResponse.error(502, "直播分片拉取失败");
        }
    }

    /**
     * 是否允许代理目标地址（同根域或白名单后缀）。
     *
     * @param source 频道源地址
     * @param target 目标地址
     * @return 允许返回 true
     */
    private boolean isAllowed(URI source, URI target) {
        String targetHost = target.getHost();
        if (targetHost == null) {
            return false;
        }
        String sourceHost = source.getHost();
        if (sourceHost != null && sourceHost.equalsIgnoreCase(targetHost)) {
            return true;
        }
        String root = rootDomain(sourceHost);
        if (root != null && (targetHost.equalsIgnoreCase(root) || targetHost.toLowerCase().endsWith("." + root))) {
            return true;
        }
        List<String> allowed = properties.getLiveAllowedHosts();
        if (allowed != null) {
            String lower = targetHost.toLowerCase();
            for (String host : allowed) {
                if (host != null && !host.isBlank() && lower.endsWith(host.trim().toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 取域名根域（朴素实现：取最后两段；IP 或单段返回自身）。
     *
     * @param host 主机名
     * @return 根域
     */
    private String rootDomain(String host) {
        if (host == null) {
            return null;
        }
        String lower = host.toLowerCase();
        if (lower.matches("\\d+(\\.\\d+){3}")) {
            return lower;
        }
        String[] parts = lower.split("\\.");
        if (parts.length <= 2) {
            return lower;
        }
        return parts[parts.length - 2] + "." + parts[parts.length - 1];
    }

    /**
     * 是否播放列表内容。
     *
     * @param body        文本
     * @param contentType 内容类型
     * @return 是播放列表返回 true
     */
    private boolean looksLikePlaylist(String body, String contentType) {
        if (body != null && body.stripLeading().startsWith("#EXTM3U")) {
            return true;
        }
        return contentType != null && contentType.toLowerCase().contains("mpegurl");
    }

    /**
     * 重写播放列表内的地址为代理地址。
     *
     * @param playlist    播放列表文本
     * @param base        当前地址（相对地址解析基准）
     * @param channelId   频道标识
     * @param sourceIndex 候选源下标
     * @return 重写后的播放列表
     */
    private String rewrite(String playlist, URI base, String channelId, int sourceIndex) {
        StringBuilder sb = new StringBuilder(playlist.length() + 256);
        for (String raw : playlist.split("\\r?\\n")) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                sb.append(rewriteTag(raw, base, channelId, sourceIndex)).append('\n');
            } else {
                sb.append(proxyUrl(base, line, channelId, sourceIndex)).append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * 重写标签行中的 URI 属性（如 EXT-X-KEY / EXT-X-MAP）。
     *
     * @param line        原始行
     * @param base        解析基准
     * @param channelId   频道标识
     * @param sourceIndex 候选源下标
     * @return 重写后的行
     */
    private String rewriteTag(String line, URI base, String channelId, int sourceIndex) {
        if (!line.contains("URI=\"")) {
            return line;
        }
        Matcher matcher = URI_ATTR.matcher(line);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String replaced = "URI=\"" + proxyUrl(base, matcher.group(1), channelId, sourceIndex) + "\"";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replaced));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 生成代理地址（相对，交由浏览器按当前路径解析）。
     *
     * @param base        解析基准
     * @param ref         原始引用
     * @param channelId   频道标识
     * @param sourceIndex 候选源下标
     * @return 代理地址
     */
    private String proxyUrl(URI base, String ref, String channelId, int sourceIndex) {
        try {
            String absolute = base.resolve(ref.trim()).toString();
            String encoded = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(absolute.getBytes(StandardCharsets.UTF_8));
            return "resource?src=" + sourceIndex + "&u=" + encoded;
        } catch (Exception e) {
            return ref;
        }
    }

    /**
     * 拉取上游资源。
     *
     * @param uri   目标地址
     * @param range Range 头，可空
     * @return 代理响应
     * @throws Exception 请求失败
     */
    private ProxyResponse fetch(URI uri, String range) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMillis(Math.max(1000, properties.getReadTimeout())))
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "*/*");
        if (uri.getHost() != null) {
            String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
            int port = uri.getPort();
            String origin = scheme + "://" + uri.getHost() + (port > 0 ? ":" + port : "");
            builder.header("Referer", origin + "/");
        }
        if (range != null && !range.isBlank()) {
            builder.header("Range", range);
        }
        HttpResponse<byte[]> response = httpClient.send(builder.GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());
        int maxBytes = Math.max(64 * 1024, properties.getLiveProxyMaxBytes());
        if (response.body() != null && response.body().length > maxBytes) {
            return ProxyResponse.error(502, "资源超出代理大小上限");
        }
        return ProxyResponse.full(
                response.body(),
                response.headers().firstValue("Content-Type").orElse(null),
                response.statusCode(),
                response.headers().firstValue("Content-Range").orElse(null));
    }

    /**
     * 代理响应。
     */
    @lombok.Getter
    public static class ProxyResponse {
        /**
         * 响应体。
         */
        private final byte[] body;

        /**
         * 内容类型。
         */
        private final String contentType;

        /**
         * HTTP 状态码。
         */
        private final int status;

        /**
         * 内容范围（Range 请求）。
         */
        private final String contentRange;

        /**
         * 构造代理响应。
         *
         * @param body         响应体
         * @param contentType  内容类型
         * @param status       状态码
         * @param contentRange 内容范围
         */
        private ProxyResponse(byte[] body, String contentType, int status, String contentRange) {
            this.body = body;
            this.contentType = contentType;
            this.status = status;
            this.contentRange = contentRange;
        }

        /**
         * 构造完整响应。
         *
         * @param body         响应体
         * @param contentType  内容类型
         * @param status       状态码
         * @param contentRange 内容范围
         * @return 响应
         */
        public static ProxyResponse full(byte[] body, String contentType, int status, String contentRange) {
            return new ProxyResponse(body, contentType, status, contentRange);
        }

        /**
         * 构造仅含状态的响应。
         *
         * @param status 状态码
         * @param message 消息
         * @return 响应
         */
        public static ProxyResponse of(byte[] body, String contentType, int status) {
            return new ProxyResponse(body, contentType, status, null);
        }

        /**
         * 构造错误响应。
         *
         * @param status  状态码
         * @param message 消息
         * @return 响应
         */
        public static ProxyResponse error(int status, String message) {
            return new ProxyResponse(message.getBytes(StandardCharsets.UTF_8),
                    "text/plain;charset=UTF-8", status, null);
        }
    }
}