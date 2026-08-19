/**
 * 请求追踪分析控制器。
 *
 * @author CH
 * @since 2026/03/25
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/request-trace/analytics")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = {
        "org.springframework.web.bind.annotation.RestController",
        "com.chua.common.support.lang.code.ReturnResult"
})
/**
 * public class RequestTraceAnalyticsController
 *
 * @author CH
 * @since 4.0.0.42
 */
public class RequestTraceAnalyticsController