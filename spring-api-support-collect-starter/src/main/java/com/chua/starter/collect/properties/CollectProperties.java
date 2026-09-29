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
@ConfigurationProperties(prefix = "plugin.collect")
public class CollectProperties {

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
     * 内置控制台登录配置。
     */
    private Console console = new Console();

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
     * 控制台账号配置。
     *
     * <p>控制台与 {@code /v2/collect/**} 接口共用这一套会话认证，密码留空时拒绝登录，
     * 部署时必须通过 {@code plugin.collect.console.password} 指定。</p>
     */
    @Data
    public static class Console {

        /**
         * 是否启用控制台登录，关闭后接口对本网络开放。
         */
        private boolean authEnabled = true;

        /**
         * 控制台账号。
         */
        private String username = "admin";

        /**
         * 控制台密码，必须显式配置。
         */
        private String password = "";

        /**
         * 登录会话有效期秒。
         */
        private int sessionTimeoutSeconds = 7200;
    }
}
