package com.chua.starter.rpc.test.controller;

import com.chua.starter.rpc.test.service.EchoServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RPC 测试控制器，通过 {@code @RpcResource} 注入的远程代理验证 RPC 调用。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@RestController
@RequestMapping("/v2/rpc/test")
@RequiredArgsConstructor
public class RpcTestController {

    /**
     * 远程服务客户端（注入的 RPC 代理）
     */
    private final EchoServiceClient echoServiceClient;

    /**
     * 调用远程 echo 方法。
     *
     * @param message 待回显消息
     * @return 调用结果
     * @param world" world"
     */
    @GetMapping("/echo")
    public RpcTestResult echo(@RequestParam(defaultValue = "hello world") String message) {
        long start = System.currentTimeMillis();
        String result = echoServiceClient.callEcho(message);
        long cost = System.currentTimeMillis() - start;
        log.info("[RPC] echo cost={}ms result={}", cost, result);
        return new RpcTestResult(true, result, cost);
    }

    /**
      * 调用远程 添加 方法。
     *
     * @param a 加数
     * @param b 加数
     * @return 调用结果
     */
    @GetMapping("/add")
    public RpcTestResult add(@RequestParam int a, @RequestParam int b) {
        long start = System.currentTimeMillis();
        int result = echoServiceClient.callAdd(a, b);
        long cost = System.currentTimeMillis() - start;
        log.info("[RPC] add({},{}) cost={}ms result={}", a, b, cost, result);
        return new RpcTestResult(true, new AddResult(a, b, result), cost);
    }

    /**
     * 调用远程 hello 方法。
     *
     * @param name 姓名
     * @return 调用结果
     */
    @GetMapping("/hello/{name}")
    public RpcTestResult hello(@PathVariable String name) {
        long start = System.currentTimeMillis();
        String result = echoServiceClient.callHello(name);
        long cost = System.currentTimeMillis() - start;
        log.info("[RPC] hello({}) cost={}ms result={}", name, cost, result);
        return new RpcTestResult(true, result, cost);
    }

    /**
     * RPC 健康检查。
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    public RpcHealthInfo health() {
        try {
            String result = echoServiceClient.callEcho("ping");
            return new RpcHealthInfo("ok", "spring-api-support-rpc-test-starter", "ok", result, null);
        } catch (Exception e) {
            log.warn("[RPC] health check failed: {}", e.getMessage());
            return new RpcHealthInfo("ok", "spring-api-support-rpc-test-starter", "error", null, e.getMessage());
        }
    }

    /**
      * 触发远程异常（传 空 参数），用于验证失败统计。
     *
     * @return 失败结果
     */
    @GetMapping("/fail")
    public RpcTestResult fail() {
        try {
            echoServiceClient.callEcho(null);
            return new RpcTestResult(true, "unexpected success", 0);
        } catch (Exception e) {
            log.warn("[RPC] fail endpoint caught: {}", e.getMessage());
            return new RpcTestResult(false, e.getClass().getSimpleName(), 0);
        }
    }

    /**
     * RPC 调用结果。
     *
     * @param success 是否成功
     * @param data    返回数据
     * @param costMs  调用耗时（毫秒）
     * @return rpc测试结果的结果
     */
    public record RpcTestResult(boolean success, Object data, long costMs) {
    }

    /**
     * 加法调用返回数据。
     *
     * @param a      加数
     * @param b      加数
     * @param result 和
     * @return 添加结果的结果
     */
    public record AddResult(int a, int b, int result) {
    }

    /**
     * 健康检查结果。
     *
     * @param rpc        RPC 模块状态
     * @param service    服务标识
     * @param rpcCall    RPC 调用状态
     * @param echoResult 回显结果
     * @param rpcError   调用错误信息
     * @return rpc健康信息的结果
     */
    public record RpcHealthInfo(String rpc, String service, String rpcCall, String echoResult, String rpcError) {
    }
}