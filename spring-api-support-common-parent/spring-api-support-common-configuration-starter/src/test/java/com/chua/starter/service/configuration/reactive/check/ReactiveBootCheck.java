package com.chua.starter.service.configuration.reactive.check;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Reactor 响应式返回值完整 Spring Boot 自检 — {@code @SpringBootApplication} 全量自动配置启动。
 *
 * <p>与 {@link ReactiveServerCheck}（手工最小装配）互补：本类验证真实业务形态 ——
 * 完整 Boot 自动配置环境下 Controller 返回 Mono/Flux 同样自动异步化。</p>
 *
 * <h2>运行方式</h2>
 * <pre>
 *   mvn test-compile exec:java -Dexec.mainClass="com.chua.starter.service.configuration.reactive.check.ReactiveBootCheck" -Dexec.classpathScope=test
 * </pre>
 *
 * @author CH
 * @since 4.0.0.42
 */
@SpringBootApplication
public class ReactiveBootCheck {

    /**
     * 自检通过计数
     */
    private static int passed = 0;

    /**
     * HTTP 客户端（超时 10 秒）
     */
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 入口：以完整自动配置启动 Boot，真实 HTTP 请求验证后关闭。
     *
     * @param args 命令行参数
     * @throws Exception 请求异常
     */
    public static void main(String[] args) throws Exception {
        ConfigurableContextHolder holder = new ConfigurableContextHolder();
        // 追加自检参数：关闭横幅 + 指向内网真实 Redis + 自检端点放行 + 开启虚拟线程
        String[] extra = {
                "--spring.main.banner-mode=off",
                "--spring.data.redis.host=172.16.0.40",
                "--spring.data.redis.port=6379",
                "--plugin.oauth.whitelist=/mono,/flux,/sse,/db",
                "--plugin.oauth.enable=false",
                "--spring.threads.virtual.enabled=true"
        };
        String[] all = new String[args.length + extra.length];
        System.arraycopy(args, 0, all, 0, args.length);
        System.arraycopy(extra, 0, all, args.length, extra.length);

        org.springframework.context.ConfigurableApplicationContext context =
                SpringApplication.run(ReactiveBootCheck.class, all);
        int port = ((WebServerApplicationContext) context).getWebServer().getPort();
        String base = "http://127.0.0.1:" + port;

        // 初始化真实 MySQL R2DBC 数据源（172.16.0.40 report 库）
        DemoController.DB_ENGINE.addDataSource("it",
                "jdbc:mysql://172.16.0.40:3306/report?useSSL=false&allowPublicKeyRetrieval=true",
                "root", "root@");

        // --keep 模式：跳过自检，保持服务常驻供人工验证
        if (java.util.Arrays.asList(all).contains("--keep")) {
            System.out.println("[KEEP-ALIVE] 服务已常驻: " + base + "/mono | /flux | /sse （结束进程即停止）");
            new java.util.concurrent.CountDownLatch(1).await();
            return;
        }
        try {
            checkMonoAsync(base);
            checkFluxJson(base);
            checkFluxSse(base);
        } finally {
            context.close();
        }

        System.out.println("-----");
        System.out.println("[RESULT] " + passed + "/3 通过");
        if (passed < 3) {
            System.exit(1);
        }
        holder.touch();
    }

    /**
     * 场景一：Mono 单值异步。
     *
     * @param base 服务根地址
     * @throws Exception 请求异常
     */
    private static void checkMonoAsync(String base) throws Exception {
        HttpResponse<String> resp = CLIENT.send(
                HttpRequest.newBuilder(URI.create(base + "/mono")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        report("Mono 单值异步", resp.statusCode() == 200 && resp.body().contains("hello-reactive"));
    }

    /**
     * 场景二：Flux + JSON 聚合。
     *
     * @param base 服务根地址
     * @throws Exception 请求异常
     */
    private static void checkFluxJson(String base) throws Exception {
        HttpResponse<String> resp = CLIENT.send(
                HttpRequest.newBuilder(URI.create(base + "/flux"))
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        report("Flux 聚合 JSON", resp.statusCode() == 200 && resp.body().replace(" ", "").equals("[1,2,3]"));
    }

    /**
     * 场景三：Flux + SSE 流式。
     *
     * @param base 服务根地址
     * @throws Exception 请求异常
     */
    private static void checkFluxSse(String base) throws Exception {
        HttpResponse<String> resp = CLIENT.send(
                HttpRequest.newBuilder(URI.create(base + "/sse"))
                        .header("Accept", MediaType.TEXT_EVENT_STREAM_VALUE)
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        String body = resp.body();
        report("Flux SSE 流式", resp.statusCode() == 200
                && body.split("\n\n").length >= 3
                && body.contains("data:1")
                && body.contains("data:3"));
    }

    /**
     * 输出单项检查结果。
     *
     * @param name   场景名称
     * @param passed 是否通过
     */
    private static void report(String name, boolean passed) {
        System.out.println((passed ? "[PASS] " : "[FAIL] ") + name);
        if (passed) {
            ReactiveBootCheck.passed++;
        }
    }

    /**
     * 占位持有者（保留扩展位）。
     *
     * @author CH
     * @since 4.0.0.42
     */
    private static final class ConfigurableContextHolder {
        /**
         * 无操作标记方法。
         */
        void touch() {
            // 预留扩展
        }
    }

    /**
     * 演示控制器：返回值全部为 Reactor 类型，与生产写法一致。
     */
    @RestController
    static class DemoController {

        /**
         * 真实 MySQL R2DBC 引擎（main 启动时初始化）
         */
        static final com.chua.datasource.support.engine.JdbcReactorEngine DB_ENGINE =
                new com.chua.datasource.support.engine.JdbcReactorEngine();

        /**
         * Mono 单值：延迟 100ms 返回。
         */
        @GetMapping("/mono")
        public Mono<String> mono() {
            return Mono.just("hello-reactive").delayElement(Duration.ofMillis(100));
        }

        /**
         * Flux 多值 + JSON。
         */
        @GetMapping("/flux")
        public Flux<Integer> fluxJson() {
            return Flux.range(1, 3);
        }

        /**
         * Flux + SSE 流式。
         */
        @GetMapping("/sse")
        public Flux<ServerSentEvent<Integer>> sse() {
            return Flux.range(1, 3)
                    .delayElements(Duration.ofMillis(50))
                    .map(n -> ServerSentEvent.builder(n).id(String.valueOf(n)).build());
        }

        /**
         * 真实数据库查询：经 R2DBC 非阻塞路径访问 172.16.0.40 MySQL。
         *
         * <p>打印数据产生线程以证明非 Tomcat 请求线程。</p>
         */
        @GetMapping("/db")
        public Flux<java.util.Map<String, Object>> db() {
            return DB_ENGINE.query("SELECT NOW() AS db_time, CONNECTION_ID() AS conn_id, VERSION() AS ver")
                    .doOnNext(row -> System.out.println("[DB] 数据线程=" + Thread.currentThread().getName()));
        }
    }
}
