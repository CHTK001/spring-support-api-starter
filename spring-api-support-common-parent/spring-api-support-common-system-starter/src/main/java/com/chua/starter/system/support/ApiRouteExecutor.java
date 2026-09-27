package com.chua.starter.system.support;

import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.entity.system.SysPolicyConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;

/**
 * 接口停用 / 转发执行器。
 *
 * <p>只做两件事，都是纯函数式的规则求值，不持有状态：</p>
 * <ol>
 *   <li>{@link #resolve(HttpServletRequest)} 命中停用规则 → 返回响应动作，由调用方写回 503</li>
 *   <li>命中转发规则且目标可解析 → 返回目标路径，由调用方改写 URI</li>
 * </ol>
 *
 * <p><b>安全取向</b>：转发目标只能来自 {@link RegisteredRouteIndex}，配置里的
 * {@code targetKey} 解析不到就<b>按停用处理</b>而不是放行 —— 宁可拒绝也不能把
 * 未知的转发目标原样透传出去。规则解析异常同样按放行处理，与限流、黑白名单一致。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiRouteExecutor {

    /**
     * JSON 解析器
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 策略运行时缓存
     */
    private final PolicyCacheService policyCacheService;

    /**
     * 求值当前请求的接口路由决策。
     *
     * @param request 当前请求
     * @return 决策结果；无规则或规则不可用时返回 {@link Decision#passThrough()}
     */
    public Decision resolve(HttpServletRequest request) {
        String path = RequestUtils.getPathWithinApplication(request);
        if (StringUtils.isBlank(path)) {
            return Decision.allow();
        }
        String ruleJson = policyCacheService.getApiRoute(path);
        if (StringUtils.isBlank(ruleJson)) {
            return Decision.allow();
        }
        ApiRouteRule rule = ApiRouteRule.fromJson(ruleJson);
        if (null == rule) {
            return Decision.allow();
        }
        if (rule.isDisable()) {
            int status = rule.getStatus() > 0 ? rule.getStatus() : ApiRouteRule.DEFAULT_DISABLE_STATUS;
            String message = StringUtils.isNotBlank(rule.getMessage())
                    ? rule.getMessage() : ApiRouteRule.DEFAULT_DISABLE_MESSAGE;
            log.warn("[ApiRoute] 接口已停用: path={}, status={}", path, status);
            return Decision.disabled(status, message);
        }
        if (rule.isForward()) {
            var target = RegisteredRouteIndex.resolve(rule.getTargetKey());
            if (null == target) {
                // 目标不在已注册路由里 —— 拒绝而不是放行
                log.warn("[ApiRoute] 转发目标未注册，拒绝转发: path={}, targetKey={}", path, rule.getTargetKey());
                return Decision.disabled(ApiRouteRule.DEFAULT_DISABLE_STATUS,
                        "转发目标不存在，接口已停用");
            }
            if (target.path().equals(path)) {
                return Decision.allow();
            }
            log.info("[ApiRoute] 接口转发: {} -> {}", path, target.path());
            return Decision.forward(target.path(), target.key());
        }
        return Decision.allow();
    }

    /**
     * 决策结果。
     *
     * @param passThrough 是否直接放行
     * @param disabled    是否按停用拒绝
     * @param status      停用响应码
     * @param message     停用提示
     * @param targetPath  转发目标路径
     * @param targetKey   转发目标注册 key
     */
    public record Decision(boolean passThrough, boolean disabled, int status, String message,
                           String targetPath, String targetKey) {

        /**
         * 构造放行决策
         *
         * @return 放行决策
         */
        public static Decision allow() {
            return new Decision(true, false, 0, null, null, null);
        }

        /**
         * 构造停用决策
         *
         * @param status  响应码
         * @param message 提示
         * @return 停用决策
         */
        public static Decision disabled(int status, String message) {
            return new Decision(false, true, status, message, null, null);
        }

        /**
         * 构造转发决策
         *
         * @param targetPath 目标路径
         * @param targetKey  目标注册 key
         * @return 转发决策
         */
        public static Decision forward(String targetPath, String targetKey) {
            return new Decision(false, false, 0, null, targetPath, targetKey);
        }
    }

    /**
     * 把当前策略表内容投影进运行时缓存。
     *
     * <p>与 IP/账号名单的 {@code IpPolicyCacheProjector} 同构：只投影启用中的行。
     * 接口停用/转发没有 {@code listType} 名单语义，因此这里只看 {@code status}。</p>
     *
     * @param rows 统一表行
     */
    public void rebuild(List<SysPolicyConfig> rows) {
        policyCacheService.clearAllApiRoute();
        if (null == rows) {
            return;
        }
        for (SysPolicyConfig row : rows) {
            if (null == row || null == row.getStatus()
                    || row.getStatus() != SysPolicyConfig.STATUS_ENABLED) {
                continue;
            }
            String ruleJson = buildRuleJson(row);
            if (StringUtils.isNotBlank(ruleJson)) {
                policyCacheService.putApiRoute(row.getBizKey(), ruleJson);
            }
        }
    }

    /**
     * 事务提交后同步单行。
     *
     * <p>先清掉旧规则再放新的：{@code bizKey} 是缓存 key，改内容不改 key，
     * 不先清就会留下旧规则。</p>
     *
     * @param previous 旧行，新增时为 null
     * @param current  新行，删除时为 null
     */
    public void syncAfterCommit(SysPolicyConfig previous, SysPolicyConfig current) {
        Runnable action = () -> {
            if (null != previous) {
                policyCacheService.evictApiRoute(previous.getBizKey());
            }
            if (null != current && null != current.getStatus()
                    && current.getStatus() == SysPolicyConfig.STATUS_ENABLED) {
                String ruleJson = buildRuleJson(current);
                if (StringUtils.isNotBlank(ruleJson)) {
                    policyCacheService.putApiRoute(current.getBizKey(), ruleJson);
                }
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }

    /**
     * 从统一表行取出规则内容。
     *
     * @param row 统一表行
     * @return 规则 JSON；无有效规则返回 null
     */
    public static String buildRuleJson(SysPolicyConfig row) {
        if (null == row || StringUtils.isBlank(row.getBizKey())) {
            return null;
        }
        Map<String, Object> config = parseConfig(row.getConfig());
        String action = String.valueOf(config.getOrDefault("action", ApiRouteRule.ACTION_PASS));
        Integer status = null;
        String message = null;
        String targetKey = null;
        try {
            if (null != config.get("status")) {
                status = Integer.valueOf(config.get("status").toString());
            }
            message = (String) config.get("message");
            targetKey = (String) config.get("targetKey");
        } catch (Exception e) {
            log.warn("[ApiRoute] 规则 config 解析异常，丢弃该行: id={}", row.getSysPolicyConfigId(), e);
            return null;
        }
        if (ApiRouteRule.ACTION_FORWARD.equalsIgnoreCase(action) && !RegisteredRouteIndex.exists(targetKey)) {
            log.warn("[ApiRoute] targetKey 不在已注册路由中，规则不生效: id={}, targetKey={}",
                    row.getSysPolicyConfigId(), targetKey);
            return null;
        }
        return ApiRouteRule.toJson(action, status, message, targetKey);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseConfig(String config) {
        if (StringUtils.isBlank(config)) {
            return Map.of();
        }
        try {
            return OBJECT_MAPPER.readValue(config, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
}
