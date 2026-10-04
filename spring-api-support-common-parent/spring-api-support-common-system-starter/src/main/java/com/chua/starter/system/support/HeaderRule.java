package com.chua.starter.system.support;

import com.chua.common.support.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 消息头拦截规则。
 *
 * <p>一条规则 = 一个消息头（主要是 {@code User-Agent}）+ 一组匹配模式 + 一个处置动作。
 * 存在统一策略表 {@code sys_firewall_config} 的 {@code config} 字段里，运行时投影进
 * {@link PolicyCacheService}，保存即生效（热重载），不需要重启。</p>
 *
 * <p><b>为什么默认匹配方式是「包含」而不是「正则」</b>：UA 串里识别爬虫/自动化的是
 * 特征片段（{@code Googlebot}、{@code HeadlessChrome}、{@code python-requests}），
 * 管理员要的是「含有这个词就拦」。正则更强但配错一行就是 500 或全站误拦，
 * 放在「点一下就生效」的在线配置里风险不对等。所以正则是可选项，不是默认项。</p>
 *
 * @param headerName 消息头名，如 {@code User-Agent}
 * @param matchType  匹配方式：CONTAINS / EQUALS / PREFIX / REGEX
 * @param patterns   模式列表
 * @param action     处置：BLOCK（直接拒绝）
 * @param status     拒绝时返回的 HTTP 状态码
 * @param message    拒绝时返回的提示
 * @author CH
 * @since 4.0.0.43
 */
public record HeaderRule(String headerName,
                         String matchType,
                         List<String> patterns,
                         String action,
                         int status,
                         String message) {

    /**
     * 包含（忽略大小写）
     */
    public static final String MATCH_CONTAINS = "CONTAINS";

    /**
     * 完全相等（忽略大小写）
     */
    public static final String MATCH_EQUALS = "EQUALS";

    /**
     * 前缀（忽略大小写）
     */
    public static final String MATCH_PREFIX = "PREFIX";

    /**
     * 正则
     */
    public static final String MATCH_REGEX = "REGEX";

    /**
     * 拒绝
     */
    public static final String ACTION_BLOCK = "BLOCK";

    /**
     * 拒绝时默认状态码
     */
    public static final int DEFAULT_STATUS = 403;

    /**
     * JSON 解析器
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 缓存已编译的正则，避免每请求重新 compile
     *
     * <p>key = 模式文本。规则本身数量很少（通常个位数），这个缓存不会无界增长；
     * 之所以要缓存：正则匹配在每请求路径上，重复 compile 是纯浪费。</p>
     */
    private static final Map<String, Pattern> REGEX_CACHE = new ConcurrentHashMap<>();

    /**
     * 紧凑构造器：归一化并做防御性拷贝。
     *
     * <p>value class 前置条件——集合组件必须深不可变。{@code patterns} 来自配置 JSON，
     * 元素不允许为 null（null 模式会被 {@code contains(x, null)} 抛 NPE）。</p>
     *
     * @param headerName 消息头名
     * @param matchType  匹配方式
     * @param patterns   模式列表
     * @param action     处置动作
     * @param status     状态码
     * @param message    提示
     */
    public HeaderRule {
        headerName = null == headerName ? "" : headerName.trim();
        matchType = StringUtils.isBlank(matchType) ? MATCH_CONTAINS : matchType.trim().toUpperCase(Locale.ROOT);
        patterns = patterns == null ? List.of() : List.copyOf(patterns);
        action = StringUtils.isBlank(action) ? ACTION_BLOCK : action.trim().toUpperCase(Locale.ROOT);
        status = status > 0 ? status : DEFAULT_STATUS;
        message = null == message ? "" : message;
    }

    /**
     * 从规则 JSON 解析。
     *
     * <p>解析失败返回 {@code null} 而不是抛异常：一条坏规则不该让整个过滤器挂掉，
     * 更不该让所有请求 500 —— 那种情况下「放行 + 记一条 warn」才是对的。</p>
     *
     * @param headerName 消息头名
     * @param ruleJson   规则 JSON
     * @return 规则；解析失败返回 null
     */
    public static HeaderRule parse(String headerName, String ruleJson) {
        if (StringUtils.isBlank(ruleJson)) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(ruleJson);
            List<String> patterns = new ArrayList<>();
            JsonNode arr = node.get("patterns");
            if (null != arr && arr.isArray()) {
                for (JsonNode p : arr) {
                    if (null != p && !p.asText("").isBlank()) {
                        patterns.add(p.asText("").trim());
                    }
                }
            }
            if (patterns.isEmpty()) {
                return null;
            }
            return new HeaderRule(
                    headerName,
                    node.path("matchType").asText(MATCH_CONTAINS),
                    patterns,
                    node.path("action").asText(ACTION_BLOCK),
                    node.path("status").asInt(DEFAULT_STATUS),
                    node.path("message").asText(""));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 序列化为规则 JSON，供写入缓存。
     *
     * @return JSON 文本
     */
    public String toJson() {
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("matchType", matchType);
            map.put("patterns", patterns);
            map.put("action", action);
            map.put("status", status);
            map.put("message", message);
            return MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断请求头值是否命中本规则。
     *
     * @param headerValue 请求携带的消息头值，可为 null
     * @return 是否命中
     */
    public boolean matches(String headerValue) {
        return null != firstMatch(headerValue);
    }

    /**
     * 找出实际命中的那个模式。
     *
     * <p>命中统计按「头名|模式」归因，这样能看出到底是哪条特征在拦，
     * 而不是只知道「User-Agent 被拦了」。</p>
     *
     * <p>单个模式的匹配失败（正则写错）只跳过该模式，不影响其余模式 ——
     * 一条写错的正则不该导致整条规则失效，更不该误放行本该拦的流量。</p>
     *
     * @param headerValue 请求携带的消息头值，可为 null
     * @return 命中的模式文本；未命中返回 null
     */
    public String firstMatch(String headerValue) {
        if (null == headerValue || headerValue.isBlank() || patterns.isEmpty()) {
            return null;
        }
        for (String pattern : patterns) {
            if (null == pattern || pattern.isBlank()) {
                continue;
            }
            if (MATCH_REGEX.equals(matchType)) {
                Pattern compiled = compile(pattern);
                if (null != compiled && compiled.matcher(headerValue).find()) {
                    return pattern;
                }
                continue;
            }
            String lowerValue = headerValue.toLowerCase(Locale.ROOT);
            String lowerPattern = pattern.toLowerCase(Locale.ROOT);
            boolean hit = switch (matchType) {
                case MATCH_EQUALS -> lowerValue.equals(lowerPattern);
                case MATCH_PREFIX -> lowerValue.startsWith(lowerPattern);
                default -> lowerValue.contains(lowerPattern);
            };
            if (hit) {
                return pattern;
            }
        }
        return null;
    }

    /**
     * 取（并缓存）编译后的正则。
     *
     * @param pattern 正则文本
     * @return 编译结果；语法错误返回 null
     */
    private static Pattern compile(String pattern) {
        Pattern cached = REGEX_CACHE.get(pattern);
        if (null != cached) {
            return cached;
        }
        try {
            Pattern compiled = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            REGEX_CACHE.put(pattern, compiled);
            return compiled;
        } catch (PatternSyntaxException e) {
            return null;
        }
    }
}
