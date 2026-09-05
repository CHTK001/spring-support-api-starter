package com.chua.starter.rpc.test.controller;

import com.chua.starter.rpc.test.service.EchoServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v2/rpc/test")
@RequiredArgsConstructor
public class RpcTestController {

    private final EchoServiceClient echoServiceClient;

    @GetMapping("/echo")
    public Map<String, Object> echo(@RequestParam(defaultValue = "hello world") String message) {
        long start = System.currentTimeMillis();
        String result = echoServiceClient.callEcho(message);
        long cost = System.currentTimeMillis() - start;
        log.info("[RPC] echo cost={}ms result={}", cost, result);
        return ok(result, cost);
    }

    @GetMapping("/add")
    public Map<String, Object> add(@RequestParam int a, @RequestParam int b) {
        long start = System.currentTimeMillis();
        int result = echoServiceClient.callAdd(a, b);
        long cost = System.currentTimeMillis() - start;
        log.info("[RPC] add({},{}) cost={}ms result={}", a, b, cost, result);
        return ok(Map.of("a", a, "b", b, "result", result), cost);
    }

    @GetMapping("/hello/{name}")
    public Map<String, Object> hello(@PathVariable String name) {
        long start = System.currentTimeMillis();
        String result = echoServiceClient.callHello(name);
        long cost = System.currentTimeMillis() - start;
        log.info("[RPC] hello({}) cost={}ms result={}", name, cost, result);
        return ok(result, cost);
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("rpc", "ok");
        status.put("service", "spring-api-support-rpc-test-starter");
        try {
            String r = echoServiceClient.callEcho("ping");
            status.put("rpcCall", "ok");
            status.put("echoResult", r);
        } catch (Exception e) {
            status.put("rpcCall", "error");
            status.put("rpcError", e.getMessage());
        }
        return status;
    }

    private Map<String, Object> ok(Object data, long cost) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", data);
        result.put("costMs", cost);
        return result;
    }
}
