package com.chua.starter.service.configuration;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spring MVC 对 Reactor 返回值的响应式处理自检 — 纯 main 启动，无 JUnit 依赖。
 *
 * <p>验证三类场景：Mono 单值异步、Flux 聚合 JSON、Flux SSE 流式。
 * 底层机制为 {@code ReactiveTypeHandler} + Servlet 异步（AsyncContext），
 * 无需 WebFlux，classpath 存在 reactor-core 即自动激活。</p>
 *
 * <h2>运行方式</h2>
 * <pre>
 *   mvn test-compile exec:java -Dexec.mainClass="com.chua.starter.service.configuration.ReactiveReturnCheck" -Dexec.classpathScope=test
 * </pre>
 *
 * @author CH
 * @since 4.0.0.42
 */
public class ReactiveReturnCheck {

    /**
     * 演示控制器：返回值全部为 Reactor 类型，写法与生产代码完全一致。
     */
    @RestController
    static class DemoController {

        /**
         * Mono 单值：延迟 100ms 返回，期间请求线程已释放。
         */
        @GetMapping("/mono")
        public Mono<String> mono() {
            return Mono.just("hello-reactive").delayElement(Duration.ofMillis(100));
        }

        /**
         * Flux 多值 + JSON：聚合为数组输出。
         */
        @GetMapping("/flux")
        public Flux<Integer> fluxJson() {
            return Flux.range(1, 3);
        }

        /**
         * Flux + SSE：真流式逐条推送。
         */
        @GetMapping("/sse")
        public Flux<ServerSentEvent<Integer>> sse() {
            return Flux.range(1, 3)
                    .map(n -> ServerSentEvent.builder(n).id(String.valueOf(n)).build());
        }
    }

    /**
     * 自检通过计数
     */
    private static int passed = 0;

    /**
     * 入口：依次执行三个场景并输出结果。
     *
     * @param args 命令行参数（未使用）
     * @throws Exception 请求异常
     */
    public static void main(String[] args) throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DemoController()).build();

        checkMonoAsync(mockMvc);
        checkFluxJson(mockMvc);
        checkFluxSse(mockMvc);

        System.out.println("-----");
        System.out.println("[RESULT] " + passed + "/3 通过");
        if (passed < 3) {
            System.exit(1);
        }
    }

    /**
     * 场景一：Mono 单值异步 — 断言请求进入异步模式且完成后输出正确内容。
     *
     * @param mockMvc MVC 测试实例
     */
    private static void checkMonoAsync(MockMvc mockMvc) throws Exception {
        MvcResult result = mockMvc.perform(get("/mono"))
                .andExpect(request().asyncStarted())
                .andReturn();

        String body = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        report("Mono 单值异步", "hello-reactive".equals(body));
    }

    /**
     * 场景二：Flux + JSON — 断言自动聚合为 JSON 数组。
     *
     * @param mockMvc MVC 测试实例
     */
    private static void checkFluxJson(MockMvc mockMvc) throws Exception {
        MvcResult result = mockMvc.perform(get("/flux").accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        String body = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        report("Flux 聚合 JSON", body.replace(" ", "").equals("[1,2,3]"));
    }

    /**
     * 场景三：Flux + SSE — 断言流式推送 3 条事件。
     *
     * @param mockMvc MVC 测试实例
     */
    private static void checkFluxSse(MockMvc mockMvc) throws Exception {
        MvcResult result = mockMvc.perform(get("/sse").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        // SSE 无结果值，仅等待流完成
        result.getAsyncResult(3000);

        String body = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        report("Flux SSE 流式", body.split("\n\n").length == 3 && body.contains("data:1"));
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
            ReactiveReturnCheck.passed++;
        }
    }
}
