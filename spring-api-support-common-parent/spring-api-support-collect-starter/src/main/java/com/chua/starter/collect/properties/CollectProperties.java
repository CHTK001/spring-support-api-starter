package com.chua.starter.collect.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 数据采集平台配置。
 *
 * <p>配置前缀 {@code plugin.collect}。每一项都在执行引擎或调度器中被真实读取，
 * 不接受只写不读的参数。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@ConfigurationProperties(prefix = CollectProperties.PRE)
public class CollectProperties {

    /**
     * 配置前缀。
     *
     * <p>与 job-starter 的 {@code JobProperties.PRE} 一样抽成常量：自动装配的
     * {@code @ConditionalOnProperty} 与这里的 {@code @ConfigurationProperties}
     * 共用同一处前缀，改名时不会漏改。</p>
     */
    public static final String PRE = "plugin.collect";

    /**
     * 默认 User-Agent 请求头。
     */
    public static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * 平台总开关，关闭后不装配调度任务与执行引擎。
     */
    private boolean enable = true;

    /**
     * 流程默认并行线程数，可被流程定义覆盖。
     */
    private int defaultThreadCount = 4;

    /**
     * 单次抓取默认超时毫秒，节点未显式配置时使用。
     */
    private int requestTimeoutMs = 30000;

    /**
     * 默认重试次数，节点未显式配置时使用。
     */
    private int requestRetryTimes = 1;

    /**
     * 默认 User-Agent，节点未显式配置时使用。
     */
    private String userAgent = DEFAULT_USER_AGENT;

    /**
     * SQL 节点是否允许执行写语句（非 SELECT/WITH/SHOW）。
     *
     * <p>SQL 节点整段支持 {@code ${}} 插值、且不做语句白名单，插值内容可能来自被抓取的页面，
     * 因此写语句的注入面明显大于查询。默认 {@code true} 保持既有行为；置 {@code false} 后
     * SQL 节点只放行查询，写库改走 database 节点（PreparedStatement 绑定、无字符串拼接）。</p>
     */
    private boolean sqlAllowWrite = true;

    /**
     * 单任务最长执行毫秒，超时后置为失败并中断流程线程池。
     */
    private long taskTimeoutMs = 600000L;

    /**
     * 单任务最多落库记录数，超出后丢弃并写任务日志，防止失控抓取撑爆结果表。
     */
    private int maxRecordPerTask = 100000;

    /**
     * 节点出参预览保留字符数。
     */
    private int previewLength = 500;

    /**
     * 脚本节点配置。
     */
    private Script script = new Script();

    /**
     * 执行通知配置。
     */
    private Notify notify = new Notify();

    /**
     * 自定义函数库配置。
     */
    private Function function = new Function();

    /**
     * 子流程节点配置。
     */
    private Process process = new Process();

    /**
     * AI 生成流程配置（设计器「AI 生成」）。
     */
    private Ai ai = new Ai();

    /**
     * 脚本节点开关。
     *
     * <p>关闭后脚本执行器不再注册，工具箱里不会出现脚本节点，
     * 已保存的含脚本节点的流程在保存与运行校验时报"类型未注册"。</p>
     */
    @Data
    public static class Script {

        /**
         * 是否启用脚本节点。
         */
        private boolean enable = true;
    }

    /**
     * 自定义函数库开关。
     *
     * <p>关闭后注册表不再挂到函数解析链上，{@code ${}} 表达式与脚本里的 {@code fn} 都只剩内置函数；
     * 函数管理接口不再装配，已登记的函数定义保留在库里不动。</p>
     */
    @Data
    public static class Function {

        /**
         * 是否启用自定义函数库。
         */
        private boolean enable = true;
    }

    /**
     * 子流程节点配置。
     *
     * <p>关闭后子流程执行器不再注册，工具箱里不会出现子流程节点，已保存的含该节点的流程
     * 在保存与运行校验时报"类型未注册"。</p>
     */
    @Data
    public static class Process {

        /**
         * 是否启用子流程节点。
         */
        private boolean enable = true;

        /**
         * 子流程嵌套层数上限，超过后节点直接失败，防止引用链过深耗尽线程池。
         */
        private int maxDepth = 5;
    }

    /**
     * 执行通知开关。
     *
     * <p>关闭后任务终态不再投递通知，配置页的试发也会直接失败并给出原因。</p>
     */
    @Data
    public static class Notify {

        /**
         * 是否启用执行通知。
         */
        private boolean enable = true;
    }

    /**
     * AI 生成流程配置。
     *
     * <p>设计器「AI 生成」把自然语言描述发给 ai-starter（OpenAI 兼容
     * {@code /v2/v1/chat/completions}），由模型产出流程 DSL；生成的 DSL 走与保存
     * 相同的图校验（{@code CollectFlowService#normalizeDsl}）后才返回给前端。</p>
     *
     * <p>ai-starter 的 {@code LocalTrustAuthFilter} 对回环来源放行
     * {@code x-nomi-local-trust} 头，因此默认配置只在采集后端与 ai-starter
     * 同机部署时可用（与 198 的实际部署形态一致）。</p>
     */
    @Data
    public static class Ai {

        /**
         * 是否启用「AI 生成流程」。
         */
        private boolean enable = true;

        /**
         * ai-starter 服务地址（与采集后端同机时用回环）。
         */
        private String baseUrl = "http://127.0.0.1:8787";

        /**
         * 模型名，须为 ai-starter 中已启用的模型（见其 GET /v2/v1/models 与供应商模型清单）。
         */
        private String model = "deepseek-v4.1-flash";

        /**
         * 本地信任头名（ai-starter 约定）。
         */
        private String trustHeader = "x-nomi-local-trust";

        /**
         * 本地信任头的值（服务端只要求非空，内容不校验）。
         */
        private String trustValue = "collect-designer";

        /**
         * 单次生成总超时毫秒（整图 JSON 生成较慢，放宽）。
         */
        private int timeoutMs = 180000;
    }

}
