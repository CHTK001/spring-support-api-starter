package com.chua.starter.system.support;

import com.chua.starter.common.support.api.control.ApiVersionRequestMappingHandlerMapping;
import com.chua.starter.common.support.api.properties.ApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 已注册路由索引。
 *
 * <p>数据来源只有一处：Spring 的 {@link RequestMappingHandlerMapping} 在启动时探测到的
 * 全部 {@code @RequestMapping}。转发目标必须从这里反查，因此<b>配置面无法表达任意外部地址</b>
 * —— 一条 {@code targetKey} 只可能指向本进程真实存在的处理方法，不存在把应用变成代理
 * 或 SSRF 跳板的可能。</p>
 *
 * <p>启动时快照一次；调用 {@link #refresh(RequestMappingHandlerMapping)} 可热更新
 * （开发期加了新接口、或用 {@code /v2/policy/api-route/refresh} 手工触发之后）。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Slf4j
public class RegisteredRouteIndex {

    /**
     * 索引快照
     */
    private static final AtomicReference<Map<String, RegisteredRoute>> INDEX =
            new AtomicReference<>(Map.of());

    /**
     * 工具类不可实例化
     */
    private RegisteredRouteIndex() {
    }

    /**
     * 采集已注册路由。
     *
     * <p><b>不用 {@code afterPropertiesSet()} 全量重扫</b>。Spring Boot 4 自带的
     * {@code BasicErrorController} 已经在容器里，再让一个临时映射重扫一遍会把它注册两次，
     * 同路径 {@code /error} 撞车 → {@code IllegalStateException: Invalid mapping on handler
     * ... BasicErrorController#error} → 整批路由一条都采不到（实测索引 0 条）。</p>
     *
     * <p>改为只登记<b>控制器 bean</b>：先拿到生产同款的
     * {@link ApiVersionRequestMappingHandlerMapping}（必须同款，否则缺少
     * {@code getMappingForMethod} 的平台/环境过滤覆写，同名映射不再消解也会抛同样的异常），
     * 再逐个 {@code detectHandlerMethods} 跳过 {@code BasicErrorController}。</p>
     *
     * @param context 应用上下文
     */
    public static void refresh(ApplicationContext context) {
        if (null == context) {
            return;
        }
        // 逐个控制器各用一个独立映射实例：某个控制器本身存在映射冲突时只丢它自己，
        // 不会像共用一个实例那样让 detectHandlerMethods 抛异常、把整批路由全丢掉。
        Map<String, RegisteredRoute> snapshot = new LinkedHashMap<>();
        int failed = 0;
        for (String beanName : context.getBeanNamesForAnnotation(Controller.class)) {
            try {
                Object bean = context.getBean(beanName);
                Class<?> type = AopUtils.getTargetClass(bean);
                if (isFrameworkErrorController(type)) {
                    continue;
                }
                MappingAccess mapping = new MappingAccess(
                        context.getBean(ApiProperties.class), context.getEnvironment());
                mapping.setApplicationContext(context);
                mapping.setContentNegotiationManager(
                        context.getBean(ContentNegotiationManager.class));
                mapping.detect(bean);
                collect(mapping, snapshot);
            } catch (Exception e) {
                failed++;
                log.debug("[ApiRoute] 跳过映射冲突的控制器: {} ({})", beanName, e.getMessage());
            }
        }
        if (snapshot.isEmpty()) {
            log.warn("[ApiRoute] 路由索引为空，转发功能不可用（接口停用不受影响）");
            return;
        }
        INDEX.set(Map.copyOf(snapshot));
        if (failed > 0) {
            log.warn("[ApiRoute] 路由索引就绪，共 {} 条；{} 个控制器因自身映射冲突被跳过", snapshot.size(), failed);
        } else {
            log.info("[ApiRoute] 已注册路由索引就绪，共 {} 条", snapshot.size());
        }
    }

    /**
     * 把一个映射实例里的路由并入快照。
     *
     * @param mapping  映射实例
     * @param snapshot 目标快照
     */
    private static void collect(RequestMappingHandlerMapping mapping,
                                Map<String, RegisteredRoute> snapshot) {
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod method = entry.getValue();
            String pattern = toPattern(info);
            if (null == pattern) {
                continue;
            }
            String handler = method.getBeanType().getSimpleName()
                    + "#" + method.getMethod().getName();
            for (String verb : verbs(info)) {
                String key = verb + " " + pattern;
                snapshot.putIfAbsent(key, new RegisteredRoute(key, verb, pattern, handler));
            }
        }
    }

    /**
     * 是否是 Spring Boot 内置的异常转发控制器。
     *
     * <p>已由框架注册过，再登记一次会造成 {@code /error} 重复映射。</p>
     *
     * @param type 控制器类型
     * @return 是内置控制器返回 true
     */
    private static boolean isFrameworkErrorController(Class<?> type) {
        String name = type.getName();
        return name.startsWith("org.springframework.boot.")
                && name.contains("ErrorController");
    }

    /**
     * 映射壳：只为了能调用 {@code detectHandlerMethods} 这个 protected 方法。
     *
     * <p>继承生产同款映射类而不是裸 {@code RequestMappingHandlerMapping}，是为了保留
     * {@code getMappingForMethod} 的平台/环境过滤覆写 —— 少了它，被过滤掉的同名映射不再消解，
     * {@code detectHandlerMethods} 会抛 {@code Invalid mapping on handler ...}。</p>
     */
    private static final class MappingAccess extends ApiVersionRequestMappingHandlerMapping {

        private MappingAccess(ApiProperties apiProperties, Environment environment) {
            super(apiProperties, environment);
        }

        /**
         * 登记一个控制器。
         *
         * @param bean 控制器 bean
         */
        private void detect(Object bean) {
            detectHandlerMethods(bean);
        }
    }

    /**
     * 按注册 key 反查目标路由。
     *
     * @param targetKey 目标注册 key，形如 {@code GET /v2/user/me}
     * @return 目标路由；不存在返回 null（调用方应拒绝转发而不是原样放行）
     */
    public static RegisteredRoute resolve(String targetKey) {
        if (null == targetKey || targetKey.isBlank()) {
            return null;
        }
        return INDEX.get().get(targetKey.trim());
    }

    /**
     * 判断某个注册 key 是否存在。
     *
     * @param targetKey 目标注册 key
     * @return 存在返回 true
     */
    public static boolean exists(String targetKey) {
        return resolve(targetKey) != null;
    }

    /**
     * 列出全部已注册路由，供管理端下拉选择。
     *
     * @return 路由列表
     */
    public static List<RegisteredRoute> list() {
        return new ArrayList<>(INDEX.get().values());
    }

    /**
     * 当前索引条数。
     *
     * @return 条数
     */
    public static int size() {
        return INDEX.get().size();
    }

    /**
     * 取路径模式。
     *
     * <p>只走 {@code getPathPatternsCondition()}：Spring 6 起
     * {@code getPatternsCondition()} 已废弃，PathPatterns 是唯一来源，没有可回退的旧 API。</p>
     *
     * @param info 路由信息
     * @return 路径模式；取不到返回 null
     */
    private static String toPattern(RequestMappingInfo info) {
        var condition = info.getPathPatternsCondition();
        if (null == condition) {
            return null;
        }
        var patterns = condition.getPatterns();
        if (null == patterns || patterns.isEmpty()) {
            return null;
        }
        return patterns.iterator().next().getPatternString();
    }

    /**
     * 取 HTTP 方法集合。
     *
     * @param info 路由信息
     * @return 方法列表，未限定时返回 {@code ANY}
     */
    private static List<String> verbs(RequestMappingInfo info) {
        var condition = info.getMethodsCondition();
        if (null != condition && !condition.getMethods().isEmpty()) {
            return condition.getMethods().stream().map(Object::toString).sorted().toList();
        }
        return List.of("ANY");
    }

    /**
     * 已注册路由。
     *
     * @param key     注册 key，形如 {@code METHOD 路径}
     * @param method  HTTP 方法
     * @param path    路径模式
     * @param handler 处理方法
     */
    public record RegisteredRoute(String key, String method, String path, String handler) {
    }
}
