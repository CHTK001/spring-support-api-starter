/**
 * 全局请求追踪过滤器。
 *
 * <p>仅对带有 {@link RequestTraceStrategy} 注解的控制器或方法记录行为数据。</p>
 *
 * @author CH
 * @since 2026/03/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = {
        "org.springframework.web.filter.OncePerRequestFilter",
        "jakarta.servlet.http.HttpServletRequest"
})
/**
 * public class GlobalRequestTraceFilter
 *
 * @author CH
 * @since 4.0.0.42
 */
public class GlobalRequestTraceFilter