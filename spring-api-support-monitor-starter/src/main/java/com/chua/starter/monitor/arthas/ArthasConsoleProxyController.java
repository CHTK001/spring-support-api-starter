package com.chua.starter.monitor.arthas;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.net.NetAddress;
import com.chua.starter.monitor.pojo.OnlineNodeInfo;
import com.chua.starter.monitor.service.NodeManagementService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Arthas 控制台后端代理控制器
 *
 * 提供： 1) 连接节点（由后端代理连接 arthas-console），返回前端可用的控制台代理URL 2) 控制台静态资源/页面的 HTTP 反向代理
 * 3) 可选：仅返回包含 REPORT_ARTHAS_CLIENT_PORT 的在线节点
 *
 * 说明： - 元数据键名：report.client.arthas.port（对应 REPORT_ARTHAS_CLIENT_PORT） -
 * 典型值：ws://arthas-tunnel:7777/ws - 归一化：ws(s)://.../ws -> http(s)://.../
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/arthas")
@Api(tags = "Arthas 控制台代理")
@Tag(name = "Arthas 控制台代理")
public class ArthasConsoleProxyController {

    /** 节点元数据中的 Arthas Tunnel 地址键名 */
    private static final String ARTHAS_META_KEY = com.chua.starter.monitor.arthas.support.ArthasProxyUtils.ARTHAS_META_KEY;

    private final NodeManagementService nodeManagementService;
    private final RestTemplate restTemplate;

    /**
     * 连接指定节点，返回由后端代理的控制台访问地址
     * 
     * @param nodeId 节点ID
     * @return { consoleUrl: "/monitor/api/v1/arthas/console/{nodeId}/" }
     */
    @PostMapping("/connect")
    @Operation(summary = "连接 Arthas 控制台", description = "返回后端代理地址，前端以 iframe 加载")
    public ReturnResult<Map<String, Object>> connect(@RequestParam("nodeId") String nodeId) {
        try {
            if (StringUtils.isBlank(nodeId)) {
                return ReturnResult.error("节点ID不能为空");
            }
            ReturnResult<OnlineNodeInfo> nodeResult = nodeManagementService.getNodeDetails(nodeId);
            if (!nodeResult.isSuccess() || nodeResult.getData() == null) {
                return ReturnResult.error("节点不存在或不可用");
            }
            OnlineNodeInfo node = nodeResult.getData();
            String base = buildConsoleBaseUrl(node);
            if (base == null) {
                return ReturnResult.error("节点未携带 Arthas 元数据或地址无效");
            }
            Map<String, Object> data = new HashMap<>();
            data.put("consoleUrl", "/monitor/api/v1/arthas/console/" + nodeId + "/");
            return ReturnResult.ok(data);
        } catch (Exception e) {
            log.error("连接 Arthas 控制台失败: nodeId={}", nodeId, e);
            return ReturnResult.error("连接 Arthas 控制台失败: " + e.getMessage());
        }
    }

    /**
     * 断开连接（占位接口，当前不持有会话资源，可直接返回成功）
     * 
     * @param nodeId 节点ID
     */
    @PostMapping("/disconnect")
    @Operation(summary = "断开 Arthas 控制台连接")
    public ReturnResult<Boolean> disconnect(@RequestParam("nodeId") String nodeId) {
        return ReturnResult.ok(Boolean.TRUE);
    }

    /**
     * 获取仅包含 Arthas 元数据的在线节点列表
     */
    @GetMapping("/nodes")
    @Operation(summary = "获取 Arthas 节点", description = "仅返回包含 report.client.arthas.port 的在线节点")
    public ReturnResult<List<OnlineNodeInfo>> getArthasNodes() {
        try {
            ReturnResult<List<OnlineNodeInfo>> allNodes = nodeManagementService.getAllOnlineNodes();
            if (!allNodes.isSuccess()) {
                return ReturnResult.error(allNodes.getMsg());
            }
            List<OnlineNodeInfo> nodes = allNodes.getData();
            if (nodes == null)
                return ReturnResult.ok(Collections.emptyList());
            List<OnlineNodeInfo> filtered = nodes.stream()
                    .filter(n -> n.getMetadata() != null && n.getMetadata().containsKey(ARTHAS_META_KEY))
                    .collect(Collectors.toList());
            return ReturnResult.ok(filtered);
        } catch (Exception e) {
            log.error("获取 Arthas 节点失败", e);
            return ReturnResult.error("获取 Arthas 节点失败: " + e.getMessage());
        }
    }

    /**
     * 反向代理 Arthas 控制台的 GET 请求 路径：/v1/arthas/console/{nodeId}/**
     */
    @GetMapping("/console/{nodeId}/**")
    @Operation(summary = "Arthas 控制台反向代理(GET)")
    public ResponseEntity<byte[]> proxyConsoleGet(@PathVariable("nodeId") String nodeId, HttpServletRequest request,
            @RequestHeader HttpHeaders headers) {
        try {
            ReturnResult<OnlineNodeInfo> nodeResult = nodeManagementService.getNodeDetails(nodeId);
            if (!nodeResult.isSuccess() || nodeResult.getData() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(("节点不可用").getBytes());
            }
            String path = extractRemainingPath(request);
            String base = buildConsoleBaseUrl(nodeResult.getData());
            if (base == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(("节点未携带 Arthas 元数据").getBytes());
            }
            String target = buildTargetUrl(base, path, request.getQueryString());

            HttpHeaders forwardHeaders = new HttpHeaders();
            forwardHeaders.putAll(headers);
            forwardHeaders.remove(HttpHeaders.HOST);

            RequestEntity<Void> req = new RequestEntity<>(forwardHeaders, HttpMethod.GET, URI.create(target));
            ResponseEntity<byte[]> resp = restTemplate.exchange(req, byte[].class);

            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.putAll(resp.getHeaders());

            // 若为HTML，则重写内嵌的 WebSocket 路径到本代理前缀，确保控制台使用后端WS代理
            MediaType ct = resp.getHeaders().getContentType();
            byte[] body = resp.getBody();
            if (body != null && ct != null && (MediaType.TEXT_HTML.includes(ct) || MediaType.TEXT_PLAIN.includes(ct))) {
                String html = new String(body, java.nio.charset.StandardCharsets.UTF_8);
                String proxyPrefix = "/v1/arthas/console/" + nodeId;
                html = html.replace("\"/ws\"", "\"" + proxyPrefix + "/ws\"").replace("'/ws'",
                        "'" + proxyPrefix + "/ws'");
                body = html.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                respHeaders.setContentLength(body.length);
            }
            return new ResponseEntity<>(body, respHeaders, resp.getStatusCode());
        } catch (Exception e) {
            log.error("代理控制台失败: nodeId={}, uri={}", nodeId, request.getRequestURI(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN)
                    .body(("代理失败: " + e.getMessage()).getBytes());
        }
    }

    // ================= 工具方法 =================

    /** 提取 /console/{nodeId}/ 之后的路径 */
    private String extractRemainingPath(HttpServletRequest request) {
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String pathWithin = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        AntPathMatcher matcher = new AntPathMatcher();
        String remaining = matcher.extractPathWithinPattern(pattern, pathWithin);
        return remaining == null ? "" : remaining;
    }

    /** 根据元数据构建 Arthas 控制台 http(s) 基础地址 */
    private String buildConsoleBaseUrl(OnlineNodeInfo node) {
        if (node == null || node.getMetadata() == null)
            return null;
        Object tunnel = node.getMetadata().get(ARTHAS_META_KEY);
        if (tunnel == null)
            return null;
        String addr = String.valueOf(tunnel).trim();
        if (StringUtils.isBlank(addr))
            return null;
        try {
            String url = addr.replaceAll("/+$", "");
            if (StringUtils.startsWithIgnoreCase(url, "ws://")) {
                url = "http://" + url.substring("ws://".length());
            } else if (StringUtils.startsWithIgnoreCase(url, "wss://")) {
                url = "https://" + url.substring("wss://".length());
            }
            if (StringUtils.endsWithIgnoreCase(url, "/ws")) {
                url = url.substring(0, url.length() - 3);
            }
            if (!url.endsWith("/"))
                url = url + "/";
            NetAddress.of(url); // 简单校验
            return url;
        } catch (Exception e) {
            log.warn("解析 Arthas 地址失败: {}", addr, e);
            return null;
        }
    }

    /** 拼接目标地址 */
    private String buildTargetUrl(String base, String path, String query) {
        StringBuilder sb = new StringBuilder(base);
        if (StringUtils.isNotBlank(path))
            sb.append(path);
        if (StringUtils.isNotBlank(query))
            sb.append(sb.indexOf("?") < 0 ? '?' : '&').append(query);
        return sb.toString();
    }
}
