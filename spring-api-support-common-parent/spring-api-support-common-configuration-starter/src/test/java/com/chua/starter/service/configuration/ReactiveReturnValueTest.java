package com.chua.starter.service.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spring MVC 对 Reactor 返回值的响应式处理验证 — 无需 WebFlux。
 *
 * <p>验证三类场景：Mono 单值异步、Flux 聚合 JSON、Flux SSE 流式。
 * 底层机制为 {@code ReactiveTypeHandler} + Servlet 异步（AsyncContext）。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
class ReactiveReturnValueTest {

    /**
     * 演示控制器：返回值全部为 Reactor 类型。
     */
    @RestController
    static class DemoController {

        /**
         * Mono 单值：延迟 100ms 后返回，期间不占用容器线程。
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
         * Flux + SSE：真流式推送，逐条写出。
         */
        @GetMapping("/sse")
        public Flux<ServerSentEvent<Integer>> sse() {
            return Flux.range(1, 3)
                    .map(n -> ServerSentEvent.builder(n).id(String.valueOf(n)).build());
        }
    }

    /**
     * MockMvc 实例
     */
    private MockMvc mockMvc;

    /**
     * 初始化独立 MVC 环境。
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DemoController()).build();
    }

    /**
     * Mono 返回值：请求线程立即释放（异步启动），完成后输出单个 JSON 值。
     *
     * @throws Exception 请求异常
     */
    @Test
    void mono_return_async_completed() throws Exception {
        MvcResult result = mockMvc.perform(get("/mono"))
                .andExpect(request().asyncStarted())
                .andReturn();

        // 结果在异步分派后可用
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().string("hello-reactive"));
    }

    /**
     * Flux 返回值 + JSON：自动聚合为 JSON 数组。
     *
     * @throws Exception 请求异常
     */
    @Test
    void flux_json_collected_as_array() throws Exception {
        MvcResult result = mockMvc.perform(get("/flux").accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().json("[1,2,3]"));
    }

    /**
     * Flux 返回值 + SSE：流式逐条推送，Content-Type 为 text/event-stream。
     *
     * @throws Exception 请求异常
     */
    @Test
    void flux_sse_streamed() throws Exception {
        MvcResult result = mockMvc.perform(get("/sse").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        String body = (String) result.getAsyncResult(3000);
        assertEquals(3, body.split("\n\n").length, "应推送 3 条 SSE 事件");
        assertTrue(body.contains("data:1") || body.contains("data:1\r"), "首条事件 data 应为 1");
    }
}
