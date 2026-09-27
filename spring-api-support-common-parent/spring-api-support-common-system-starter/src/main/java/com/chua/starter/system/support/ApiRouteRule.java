package com.chua.starter.system.support;

import com.chua.common.support.utils.StringUtils;
import com.chua.starter.system.constant.PolicyCacheName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 接口停用 / 转发规则。
 *
 * <p>一条规则对应 {@code sys_policy_config} 里 {@code type=API_ROUTE} 的一行，
 * {@code biz_key} 是去掉 context-path 的请求路径（如 {@code /v2/user/me}）。</p>
 *
 * <p>转发目标<b>不是配置来的字符串</b>，而是由
 * {@link com.chua.starter.system.support.RegisteredRouteIndex} 从 Spring 已注册路由里
 * 按目标 key 反查得到。配置里只写 {@code targetKey}，目标路径由系统按当前注册表解析。
 * 这样配置面根本无法表达任意外部地址，不存在把应用变成代理/跳板的可能。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Slf4j
@Getter
@Setter
public class ApiRouteRule {

    /**
     * 放行
     */
    public static final String ACTION_PASS = "PASS";

    /**
     * 停用：直接拒绝，不再进入业务
     */
    public static final String ACTION_DISABLE = "DISABLE";

    /**
     * 转发：改写到另一个已注册的处理方法
     */
    public static final String ACTION_FORWARD = "FORWARD";

    /**
     * 默认停用响应码
     */
    public static final int DEFAULT_DISABLE_STATUS = 503;

    /**
     * 默认停用提示
     */
    public static final String DEFAULT_DISABLE_MESSAGE = "接口已停用，请稍后再试";

    /**
     * JSON 解析器
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 动作
     */
    private String action = ACTION_PASS;

    /**
     * 停用响应码
     */
    private int status = DEFAULT_DISABLE_STATUS;

    /**
     * 停用提示
     */
    private String message = DEFAULT_DISABLE_MESSAGE;

    /**
     * 目标注册 key
     */
    private String targetKey;

    /**
     * 解析后的目标路径（由目标 key 反查得到，不来自配置）
     */
    private String targetPath;

    /**
     * 反向构造规则 JSON
     *
     * @param action     动作
     * @param status     停用响应码
     * @param message    停用提示
     * @param targetKey  目标注册 key
     * @return 规则 JSON
     */
    public static String toJson(String action, Integer status, String message, String targetKey) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("action", StringUtils.isBlank(action) ? ACTION_PASS : action);
        node.put("status", null == status ? DEFAULT_DISABLE_STATUS : status);
        if (StringUtils.isNotBlank(message)) {
            node.put("message", message);
        }
        if (StringUtils.isNotBlank(targetKey)) {
            node.put("targetKey", targetKey);
        }
        return node.toString();
    }

    /**
     * 解析规则 JSON。
     *
     * <p>解析失败一律降级为 {@link #PASS}：策略坏掉不该导致接口不可用，
     * 与限流/黑白名单「异常放行」的取向一致。</p>
     *
     * @param json 规则 JSON
     * @return 规则；无法解析返回 null（调用方按放行处理）
     */
    public static ApiRouteRule fromJson(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            JsonNode node = MAPPER.readTree(json);
            ApiRouteRule rule = new ApiRouteRule();
            rule.setAction(node.path("action").asText(ACTION_PASS));
            rule.setStatus(node.path("status").asInt(DEFAULT_DISABLE_STATUS));
            rule.setMessage(node.path("message").asText(DEFAULT_DISABLE_MESSAGE));
            rule.setTargetKey(node.path("targetKey").asText(null));
            return rule;
        } catch (Exception e) {
            log.warn("[Policy] 接口路由规则解析失败，按放行处理: {}", json, e);
            return null;
        }
    }

    /**
     * 是否为转发
     *
     * @return true 表示需要改写路径
     */
    public boolean isForward() {
        return ACTION_FORWARD.equalsIgnoreCase(action) && StringUtils.isNotBlank(targetKey);
    }

    /**
     * 是否为停用
     *
     * @return true 表示需要拒绝
     */
    public boolean isDisable() {
        return ACTION_DISABLE.equalsIgnoreCase(action);
    }

    /**
     * 缓存名
     *
     * @return 接口路由缓存名
     */
    public static String cacheName() {
        return PolicyCacheName.API_ROUTE;
    }
}
